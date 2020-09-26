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
		// 1. Load database
		// Note: dbFile.getParent() is just "pro forma".
		//       It needs to be a valid path despite not being used
		//       for any of the integrity checks...
		Path dbFilePath = dbFile.toPath();
		DB db = new DB(dbFilePath.getParent(), o);
		try {
			db.initFromLoc(dbFilePath);
		} catch(IOException ex) {
			throw new MBBFailureException(ex);
		}

		Map<Long,IRBlock> results = new TreeMap<Long,IRBlock>();

		// 2. Scan for blocks (and process them in the executor service)
		ExecutorService pool = Executors.newFixedThreadPool(Runtime.
					getRuntime().availableProcessors());
		IRFileScanner scanner = new IRFileScanner(o, Arrays.asList(root
							), db, results, pool);
		scanner.performSourceDirectoryScan();
		pool.shutdown();

		// 3. Check for entries in DB which are not in results list yet.
		//    By construction this means they are _only_ in the DB and
		//    not on the FS.
		for(DBBlock dbb: db.blocks) {
			if(!results.containsKey(dbb.getId())) {
				results.put(dbb.getId(), new IRBlock(null,
							dbb.getId(), db));
			}
		}

		// 4. Await results
		try {
			while(!pool.isTerminated()) {
				pool.awaitTermination(300, TimeUnit.SECONDS);
			}
		} catch(InterruptedException ex) {
			throw new MBBFailureException(ex);
		}


		// 5. Print results
		IRStats stats = new IRStats();
		o.printf("D -- Database? Block is known in Database (Y/N)\n");
		o.printf("A -- Active?   (Y/N)\n");
		o.printf("H -- HDD?      Block exists on HDD (Y/N)\n");
		o.printf("E -- Equal?    Block checksum matches the one " +
					"reported in the database (Y/N)\n");
		o.printf("G -- Good?     (Y/N)\n\n");
		o.printf(": -- yes\n");
		o.printf("E -- no\n");
		o.printf("_ -- any\n\n");
		o.printf("%-16s DAHEG | %-16s DAHEG | %-16s DAHEG\n",
					"BlockID", "BlockID", "BlockID");
		int e = 0;
		for(IRBlock irb: results.values()) {
			o.printf("%s", irb.toString());
			irb.addToStats(stats);
			if(++e == 3) {
				o.printf("\n");
				e = 0;
			} else {
				o.printf(" | ");
			}
		}

		// 6. Print statistics
		stats.print(o);
	}

}
