package ma.jmbb;

import java.nio.file.Paths;

class EditBackup extends EditorCommand {

	public EditBackup(PrintfIO o, DB db) {
		super(o, db);
	}

	public String getCommandName() {
		return "backup";
	}

	public String getDescription() {
		return "Backup database to user directory.";
	}

	public void call(String[] args) throws Exception {
		db.save(Paths.get(".", "db_backup_" +
				System.currentTimeMillis() + ".xml.gz"));
	}

}
