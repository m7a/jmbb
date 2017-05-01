package ma.jmbb;

class EditSave extends EditorCommand {

	public EditSave(PrintfIO o, DB db) {
		super(o, db);
	}

	public String getCommandName() {
		return "save";
	}

	public String getDescription() {
		return "Save database.";
	}

	public void call(String[] args) throws Exception {
		db.save();
	}

}
