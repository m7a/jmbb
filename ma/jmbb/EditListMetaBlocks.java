package ma.jmbb;

/**
 * @since JMBB 1.0.7
 */
class EditListMetaBlocks extends EditorCommand {

	public EditListMetaBlocks(PrintfIO o, DB db) {
		super(o, db);
	}

	public String getCommandName() {
		return "meta";
	}

	public String getDescription() {
		return "Displays all meta blocks";
	}

	public void call(String[] args) throws Exception {
		for(DBBlock blk: db.blocks) {
			if(blk.isMetaBlock()) {
				blk.printShort(o);
			}
		}
	}

}
