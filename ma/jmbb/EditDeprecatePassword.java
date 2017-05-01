package ma.jmbb;

class EditDeprecatePassword extends EditorAbstractCriticalDBCommand {

	public EditDeprecatePassword(PrintfIO o, DB db) {
		super(o, db);
	}

	public String getCommandName() {
		return "dep";
	}

	public String getArgsString() {
		return "[id]";
	}

	public String getDescription() {
		return "DANGER! Deprecate password. autosav. " +
							"backup update REQ!";
	}

	public void call(String[] args) throws Exception {
		long id = Long.parseLong(args[1].substring(1));

		for(DBBlock i: db.blocks)
			if(i.activelyUsesPassword(id))
				obsoleteWholeBlock(i);

		db.passwords.performAutomaticPasswordObsoletion(db.blocks, o);
		saveDBCritical();
	}

}
