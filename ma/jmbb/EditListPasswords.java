package ma.jmbb;

class EditListPasswords extends EditorCommand {

	public EditListPasswords(PrintfIO o, DB db) {
		super(o, db);
	}

	public String getCommandName() {
		return "ls";
	}

	public String getDescription() {
		return "Displays all passwords in plain text";
	}

	public void call(String[] args) throws Exception {
		db.passwords.print(o);
	}

}
