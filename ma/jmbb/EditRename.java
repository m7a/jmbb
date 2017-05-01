package ma.jmbb;

import java.util.Iterator;

class EditRename extends EditorCommand {

	public EditRename(PrintfIO o, DB db) {
		super(o, db);
	}

	public String getCommandName() {
		return "mv";
	}

	public String getDescription() {
		return "DANGER! Renames o to n causing inconsistency.";
	}

	public String getArgsString() {
		return "[o] [n]";
	}

	public void call(String[] args) throws Exception {
		if(args.length != 3) {
			o.eprintf("mv requires exactly two parameters.\n");
			return;
		}

		warn(args[1], args[2]);
		rename(args[1], args[2]);
	}

	private void warn(String prev, String result) {
		o.printf("WARNING: Renaming directory \"%s\" to \"%s\".\n",
								prev, result);
		o.printf("This will render all exsisting blocks invalid " +
						"without recreating them.\n");
		o.printf("If you are not about to restore the backup, " +
					"consider recompiling instead.\n");
	}

	private void rename(String prev, String result) throws Exception {
		long chg = 0, sum = 0;

		for(DBBlock i: db.blocks) {
			for(Iterator<DBFile> f = i.getFileIterator();
								f.hasNext();) {
				if(f.next().rename(prev, result))
					chg++;
				sum++;
			}
		}

		o.printf("%d/%d file names changed.\n", chg, sum);
	}

}
