package ma.jmbb;

import java.util.Iterator;

class EditObsoleteBlock extends EditorAbstractCriticalDBCommand {

	public EditObsoleteBlock(PrintfIO o, DB db) {
		super(o, db);
	}

	public String getCommandName() {
		return "obsolete";
	}

	public String getArgsString() {
		return "[id]";
	}

	public String getDescription() {
		return "DANGER! Obsoletes a single block. Backup afterwards!";
	}

	public void call(String[] args) throws Exception {
		long id = DBBlock.parseID(args[1]);
		DBBlock blk;
		for(DBBlock i: db.blocks) {
			if(i.getId() == id) {
				obsoleteWholeBlock(i);
				o.printf("The backup is now inconsistent. " +
					"Update your backup to correct " + 
					"this.\n");
				saveDBCritical();
				return;
			}
		}
		o.printf("No block 0x%x found.");
	}

}
