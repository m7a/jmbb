package ma.jmbb;

import java.util.Iterator;

class EditDefrag extends EditorAbstractCriticalDBCommand {

	public EditDefrag(PrintfIO o, DB db) {
		super(o, db);
	}

	public String getCommandName() {
		return "defrag";
	}

	public String getArgsString() {
		return "[x]";
	}

	public String getDescription() {
		return "DANGER! x: allowed frag [0;1]. autosav. " +
							"backup update REQ!";
	}

	public void call(String[] args) throws Exception {
		final float x = Float.parseFloat(args[1]);
		
		long blks   = 0;
		long blkdel = 0;
		for(DBBlock i: db.blocks) {
			blks++;
			int obsoleteF  = 0;
			int totalF     = 0;
			long obsoleteB = 0;
			long totalB    = 0;
			for(Iterator<DBFile> f = i.getFileIterator();
								f.hasNext();) {
				totalF++;
				DBFile j = f.next();
				totalB += j.size;
				if(j.isObsolete()) {
					obsoleteF++;
					obsoleteB += j.size;
				}
			}
			// TODO z ALLOW COOSING BETWEEN FILE COUNT BASED AND
			//        FILE SIZE BASED DEFRAG
			double frag = (double)obsoleteB / (double)totalB;
			// obsolete == total => block wholly obsolete.
			// we do not rely on obsoletedInTheFirstPlace here
			// because the user might invoke defrag multiple times
			// which would cause blocks to be obsoleted multiple
			// times.
			if(obsoleteF != totalF && frag > x) {
				obsoleteWholeBlock(i);
				blkdel++;
			}
		}
		if(blkdel == 0) {
			o.printf("No changes have been performed. Database " +
							"was not saved.\n");
		} else {
			o.printf("%d/%d blocks obsoleted.\n", blkdel, blks);
			o.printf("The backup is now inconsistent. Update " +
					"your backup to correct this.\n");
			saveDBCritical();
		}
	}

}
