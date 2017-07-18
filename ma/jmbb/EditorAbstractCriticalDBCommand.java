package ma.jmbb;

import java.io.IOException;
import java.util.Iterator;

abstract class EditorAbstractCriticalDBCommand extends EditorCommand {

	private static final String FATAL_DB_ERR =
		"FATAL ERROR: EMERGENCY BACKUP FAILED. DO NOT \"exit\". " +
		"YOUR BACKUP IS IN DANGER. ADJUST PERMISSIONS TO \"save\" " +
		"or \"backup\" DATABSE. OTHERWISE TRY TO CREATE AN ENTIRELY " +
		"NEW BACKUP.\n";

	public EditorAbstractCriticalDBCommand(PrintfIO o, DB db) {
		super(o, db);
	}

	void saveDBCritical() {
		// Very critical
		// -------------
		// If we fail to save the DB here, backup blocks have been
		// deleted without the DB noticing => this leads to inconsitency
		// from partially-covered backups to restoration failures.

		try {
			db.save();
		} catch(IOException ex) {
			o.edprintf(ex, "ERROR: Database saving failed. " +
					"Trying to backup DB to user dir.\n");
			try {
				new EditBackup(o, db).call(null);
			} catch(Exception exS) {
				o.edprintf(exS, FATAL_DB_ERR);
			}
		}
	}

	void obsoleteWholeBlock(DBBlock blk) throws Exception {
		for(Iterator<DBFile> f = blk.getFileIterator(); f.hasNext();) {
			DBFile i = f.next();
			if(!i.isObsolete()) {
				i.obsolete();
				db.times.updateFileObsolete(i.getPath());
			}
		}
		blk.deleteIfNewlyObsolete(o);
	}

}
