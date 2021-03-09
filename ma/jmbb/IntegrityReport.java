package ma.jmbb;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.nio.file.Path;

class IntegrityReport {

	private final PrintfIO o;
	private final File dbFile;
	private final File root;

	IntegrityReport(PrintfIO o, File db, File root) {
		super();
		this.o    = o;
		dbFile    = db;
		this.root = root;
	}

	void run() throws MBBFailureException {
		// == Stage 1: Metadata acquisition ==

		// 1a: Load database
		// Note: dbFile.getParent() is just "pro forma".
		//       It needs to be a valid path despite not being used
		//       for any of the integrity checks...
		final Path dbFilePath = dbFile.toPath();
		final DB db = new DB(dbFilePath.getParent(), o);
		FailableThread dbLoader = new FailableThread() {
			@Override
			public void runFailable() throws Exception {
				db.initFromLoc(dbFilePath);
			}
		};
		dbLoader.start();

		// 1b: Scan file system
		Map<Long,IRBlock> results = new TreeMap<Long,IRBlock>();
		IRFileScanner scanner = new IRFileScanner(o,
						Arrays.asList(root), results);
		scanner.start();

		// synchronize
		try {
			dbLoader.join();
			scanner.join();
		} catch(InterruptedException ex) {
			throw new MBBFailureException(ex);
		}
		// record all possible failures
		try {
			dbLoader.throwPossibleFailure();
		} finally {
			scanner.throwPossibleFailure();
		}

		// == Stage 2: Process ==
		ExecutorService pool = Executors.newFixedThreadPool(
					Multithreading.determineThreadCount());
		for(IRBlock block: results.values()) {
			block.assignMetadataFromDB(db);
			if(block.isProcessingRequired()) {
				pool.execute(block);
			}
		}
		
		// everything submitted
		pool.shutdown();

		// Check for entries in DB which are not in results list yet.
		// By construction this means they are _only_ in the DB and
		// not on the FS.
		for(DBBlock dbb: db.blocks) {
			if(!results.containsKey(dbb.getId())) {
				results.put(dbb.getId(), new IRBlock(dbb));
			}
		}

		// synchronize
		Multithreading.awaitPoolTermination(pool);

		// == Stage 3: Print results ==
		int[] counters = new int[IRStatus.values().length];
		boolean isFail = false;
		Arrays.fill(counters, 0);
		o.printf("Details\n");
		o.printf("=======\n\n");
		for(IRBlock irb: results.values()) {
			IRStatus status = irb.getStatus();
			if(status.summary == IRStatusSummary.FAILURE) {
				isFail = true;
			}
			o.printf("%016x  %s\n", irb.id, status.toString());
			if(status.isDuplicateMismatch()) {
				irb.printMatchDetails(o);
			}
			counters[status.ordinal()]++;
		}

		o.printf("\nStatistics\n");
		o.printf("==========\n\n");

		int sum = 0;
		IRStatusSummary cat = null;
		for(IRStatus stat: IRStatus.values()) {
			int i = stat.ordinal();
			if(cat == stat.summary) {
				sum += counters[i];
			} else {
				if(cat != null && sum != 0) {
					o.printf("%-40s %d\n",
							"        -- SUM", sum);
				}
				sum = counters[i];
				cat = stat.summary;
			}
			if(counters[i] != 0) {
				o.printf("%-40s %d\n", stat.toString(),
								counters[i]);
			}
		}

		o.printf("\nSummary\n");
		o.printf("=======\n\n");
		o.printf("%s\n", (isFail? "BACKUP IS INCONSISTENT!":
						"Backup is consistent."));
	}

}
