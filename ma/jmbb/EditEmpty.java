package ma.jmbb;

class EditEmpty extends EditorCommand {

	public EditEmpty(PrintfIO o, DB db) {
		super(o, db);
	}

	public String getCommandName() {
		return "empty";
	}

	public String getArgsString() {
		return "[rm]";
	}

	public String getDescription() {
		return "Displays/Removes empty blocks.";
	}

	public void call(String[] args) throws Exception {
		boolean del = args.length == 2 && args[1].equals("rm");

		int n = 0;
		for(DBBlock i: db.blocks) {
			if(!i.getFileIterator().hasNext()) {
				n++;
				procEmpty(i, del);
			}
		}

		o.printf("\n%d empty blocks in DB.\n", n);
	}

	private void procEmpty(DBBlock i, boolean del) throws Exception {
		if(del)
			i.deleteAsObsolete(o);
		else
			o.printf("%s\n", i.formatXMLBlockId());
	}

	public void printDocumentation() {
		o.printf("If rm is given, all blocks which are empty will be " +
						"tried to be deleted.\n");
		o.printf("Otherwise, all block IDs of empty blocks are " +
								"listed.\n");
	}

}
