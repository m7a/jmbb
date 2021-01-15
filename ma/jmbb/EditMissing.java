package ma.jmbb;

import java.nio.file.Files;

class EditMissing extends EditorCommand {

	public EditMissing(PrintfIO o, DB db) {
		super(o, db);
	}

	public String getCommandName() {
		return "missing";
	}

	public String getDescription() {
		return "Displays blocks which are not on HDD.";
	}

	public void call(String[] args) throws Exception {
		int miss = 0;
		for(DBBlock i: db.blocks) {
			if(!i.isObsoletedInTheFirstPlace() &&
						!Files.exists(i.getFile())) {
				miss++;
				o.printf("%s\n", i.formatXMLBlockId());
			}
		}
		o.printf("%d blocks missing.\n", miss);
	}

	public void printDocumentation() {
		o.printf("This is useful if the DB got corrputed and you use " +
							"an older version.\n");
	}

}
