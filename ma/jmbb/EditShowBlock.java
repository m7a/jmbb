package ma.jmbb;

class EditShowBlock extends EditorCommand {

	public EditShowBlock(PrintfIO o, DB db) {
		super(o, db);
	}

	public String getCommandName() {
		return "cat";
	}

	public String getArgsString() {
		return "[id]";
	}

	public String getDescription() {
		return "Show contents of block w/ given id.";
	}

	public void call(String[] args) throws Exception {
		long id = DBBlock.parseID(args[1]);
		DBBlock blk;
		for(DBBlock i: db.blocks) {
			if(i.getId() == id) {
				i.print(o);
				break;
			}
		}
	}

}
