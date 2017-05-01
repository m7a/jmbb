package ma.jmbb;

import java.util.Map;
import java.util.HashMap;

class EditCheckConsistency extends EditorCommand {

	EditCheckConsistency(PrintfIO o, DB db) {
		super(o, db);
	}

	@Override
	public String getCommandName() {
		return "check";
	}

	@Override
	public String getDescription() {
		return "Detect duplicate version numbers.";
	}

	@Override
	public void call(String[] args) throws Exception {
		findDuplicateVersionNumbers();
	}

	private void findDuplicateVersionNumbers() {
		int numInc = 0;
		int numFil = 0;
		Map<String, REntry> rtbl = Restorer.createRestorePathTable(db,
								null, -1);
		for(REntry re: rtbl.values()) {
			boolean hashdr = false;
			Map<Integer,RFileVersionEntry> knownVersions =
				new HashMap<Integer,RFileVersionEntry>();
			for(RFileVersionEntry fe: re) {
				int version = fe.file.version;
				if(knownVersions.containsKey(version)) {
					if(!hashdr) {
						o.printf("Duplicate version " +
							"of %s:\n",
							fe.file.getPath());
						numFil++;
						hashdr = true;
					}
					o.printf("  Version %d\n", version);
					RFileVersionEntry oe =
						knownVersions.get(version);
					o.printf("    (%s) %s\n",
						DBBlock.formatBlockIDNice(
							oe.inBlk.getId()),
						oe.file.formatXML());
					o.printf("    (%s) %s\n",
						DBBlock.formatBlockIDNice(
							fe.inBlk.getId()),
						fe.file.formatXML());
					numInc++;
				} else {
					knownVersions.put(version, fe);
				}
			}
		}
		o.printf("%d duplicate version numbers for %d files " +
						"detected.\n", numInc, numFil);
	}

	@Override
	public void printDocumentation() {
		o.printf("Duplicate version numbers should not occur with " +
			"normal single-database operation.\n");
		o.printf("In case the database was messed up, you can find " +
			"duplicate version numbers using this command.\n");
		o.printf("If you ever face duplicate version numbers, check " +
			"if any of the output entries has\n");
		o.printf("obsolete=\"false\". In that case, a backup-" +
			"relevant file has a duplicate version which means\n");
		o.printf("that your backup is likely to be inconsistent. " +
			"In this case, you should test the backup ASAP.\n");
	}


}
