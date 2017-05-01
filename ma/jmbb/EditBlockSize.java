package ma.jmbb;

class EditBlockSize extends EditorCommand {

	public EditBlockSize(PrintfIO o, DB db) {
		super(o, db);
	}

	public String getCommandName() {
		return "blk";
	}

	public String getArgsString() {
		return "[sk]";
	}

	public String getDescription() {
		return "Set default block size to sk KiB.";
	}

	public void call(String[] args) throws Exception {
		db.header.setDefaultBlockSize(Integer.parseInt(args[1]));
	}

}
