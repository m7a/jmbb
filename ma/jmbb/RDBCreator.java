package ma.jmbb;

import java.io.*;

import java.util.List;
import java.util.ArrayList;

/**
 * Responsible for creating database objects from various forms of source data.
 */
class RDBCreator {

	private final PrintfIO o;
	private final List<File> src;

	RDBCreator(PrintfIO io, List<File> src) {
		super();
		o = io;
		this.src = src;
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
		try {
			pseudoDB.passwords.readInteractively(o);
		} catch(IOException ex) {
			throw new MBBFailureException(ex);
		}
		rparseFileIntoDB(root, pseudoDB);
	}

	// Use good old java.io.File interface (good enough for this very task).
	private void rparseFileIntoDB(File f, RDB db)
						throws MBBFailureException {
		if(f.isDirectory()) {
			File[] sub = f.listFiles();
			for(File i: sub) {
				rparseFileIntoDB(i, db);
			}
		} else if(f.isFile()) {
			parseFileIntoDB(f, db);
		} else {
			throw new MBBFailureException(
				"Can not scan file structures which contain " +
				"something apart from regular files and " +
				"directories. Suspicious part of the " +
				"directory structure: " + f.getAbsolutePath()
			);
		}
	}

	private void parseFileIntoDB(File f, RDB db) {
		try {
			new RCPIOMetaExtractor(f, db, o).run();
		} catch(MBBFailureException ex) {
			o.edprintf(ex, "Unable to parse file \"%s\" into DB.\n",
							f.getAbsolutePath());
		}
	}

}
