package ma.jmbb;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Responsible for creating database objects from various forms of source data.
 */
class RDBCreator {

	private final PrintfIO o;
	private final List<File> src;
	private final boolean findBlocksByFileName;

	RDBCreator(PrintfIO io, List<File> src, boolean findBlocksByFileName) {
		super();
		o = io;
		this.src = src;
		this.findBlocksByFileName = findBlocksByFileName;
	}

	RDB[] createDBForEachSource() throws MBBFailureException {
		ArrayList<RDB> ret = new ArrayList<RDB>();
		for(File i: src) {
			try {
				ret.add(createDBFrom(i));
			} catch(MBBFailureException ex) {
				o.edprintf(ex, "Could not generate Database " +
					"from \"%s\".\n", i.getAbsolutePath());
			}
		}
		return ret.toArray(new RDB[ret.size()]);
	}

	private RDB createDBFrom(File path) throws MBBFailureException {
		RDB ret = new RDB(path.toPath(), o);
		if(!ret.initFromLocAndReportSuccess()) {
			createDBFromDirectoryStructure(path, ret);
		}
		return ret;
	}

	private void createDBFromDirectoryStructure(File root, RDB pseudoDB)
						throws MBBFailureException {
		pseudoDB.initializeKnownFilesBlocks(); // will be empty...
		if(findBlocksByFileName) {
			createDBFromFileNames(root, pseudoDB);
		} else {
			createDBFromBlockContents(root, pseudoDB);
		}
	}

	private void createDBFromFileNames(File root, RDB pseudoDB)
						throws MBBFailureException {
		// As of now uses functions introducted with the integrity
		// report.
		// TODO z It might make sense to somehow reflect in their names
		//        that these parts are shared accross Integrity/Restore
		Map<Long,IRBlock> results = new TreeMap<Long,IRBlock>();
		IRFileScanner scanner = new IRFileScanner(o,
						Arrays.asList(root), results);
		scanner.performSourceDirectoryScan();
		for(IRBlock irb: results.values()) {
			pseudoDB.addRestorationBlock(new DBBlock(irb.getFile(),
								irb.id));
		}
	}

	private void createDBFromBlockContents(File root, RDB pseudoDB)
						throws MBBFailureException {
		try {
			pseudoDB.passwords.readInteractively(o);
		} catch(IOException ex) {
			throw new MBBFailureException(ex);
		}
		ExecutorService pool = Executors.newFixedThreadPool(
					Multithreading.determineThreadCount());
		ArrayList<Future<RCPIOMetaExtractor>> results =
				new ArrayList<Future<RCPIOMetaExtractor>>();
		rparseFileIntoDB(root, pseudoDB, pool, results);
		pool.shutdown();
		try {
			for(Future<RCPIOMetaExtractor> fr: results) {
				RCPIOMetaExtractor r = fr.get();
				DBBlock b = r.getBlock();
				// if it is null, the error message was already
				// printed, otherwise processing was successful.
				if(b != null) {
					pseudoDB.addRestorationBlock(b);
				}
			}
		} catch(ExecutionException|InterruptedException ex) {
			// ExecutionException is fatal because run() is expected
			// to cover all possible failures except those which
			// cannot be anticipated and should hence terminate the
			// whole process for safety. Similarly,
			// InterruptedExceptions are not expected at this point.
			throw new MBBFailureException(ex);
		}
		Multithreading.awaitPoolTermination(pool);
	}

	// Use good old java.io.File interface (good enough for this task).
	private void rparseFileIntoDB(File f, RDB db, ExecutorService pool,
				List<Future<RCPIOMetaExtractor>> results)
				throws MBBFailureException {
		if(f.isDirectory()) {
			File[] sub = f.listFiles();
			for(File i: sub) {
				rparseFileIntoDB(i, db, pool, results);
			}
		} else if(f.isFile()) {
			RCPIOMetaExtractor extr =
					new RCPIOMetaExtractor(f, db, o);
			results.add(pool.submit(extr, extr));
		} else {
			throw new MBBFailureException(
				"Can not scan file structures which contain " +
				"something apart from regular files and " +
				"directories. Suspicious part of the " +
				"directory structure: " + f.getAbsolutePath()
			);
		}
	}

}
