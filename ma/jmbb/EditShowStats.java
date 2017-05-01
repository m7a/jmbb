package ma.jmbb;

/*
 * TODO Statistics: Add statistics about block obsoletion (table/diagram?)
 * 			Statistics should give clues for defragmentation.
 */
class EditShowStats extends EditorCommand {

	public EditShowStats(PrintfIO o, DB db) {
		super(o, db);
	}

	public String getCommandName() {
		return "stat";
	}

	public String getDescription() {
		return "Display database statistics.";
	}

	public void call(String[] args) throws Exception {
		db.header.printStats(o);
		db.blocks.printStats(o);
		db.passwords.printStats(o);
	}

}
