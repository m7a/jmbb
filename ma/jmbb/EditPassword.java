package ma.jmbb;

class EditPassword extends EditorCommand {

	public EditPassword(PrintfIO o, DB db) {
		super(o, db);
	}

	public String getCommandName() {
		return "passwd";
	}

	public String getDescription() {
		return "Set new current databse password.";
	}

	public void call(String[] args) throws Exception {
		try {
			String newPW = o.readLn("Enter new password");
			changeSetCurrentPassword(newPW);
		} catch(UserAbortException ex) {
			// return.
		}
	}

	private void changeSetCurrentPassword(String pass) {
		if(db.passwords.hasCurrent())
			db.passwords.disableCurrentPassword();

		long greatestKnownId = db.passwords.getGreatestId();
		DBPassword np = new DBPassword(pass,
						greatestKnownId + 1);
		// automatically makes that password current.
		db.passwords.add(np);
	}

	public void printDocumentation() {
		o.printf("Interactively updates database password.\n");
		o.printf("This does not automatically obsolete any blocks.\n");
	}

}
