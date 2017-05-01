package ma.jmbb;

import java.util.Map;
import java.util.HashMap;

class EditCheckConsistency extends EditorCommand {

	public EditCheckConsistency(PrintfIO o, DB db) {
		super(o, db);
	}

	@Override
	public String getCommandName() {
		return "check";
	}

	@Override
	public String getDescription() {
		return "Finds out about duplicate version numbers.";
	}

	@Override
	public void call(String[] args) throws Exception {
		long numInc = 0;
		Map<String, REntry> rtbl = Restorer.createRestorePathTable(db,
								null, -1);
		for(REntry re: rtbl.values()) {
			Map<Integer,RFileVersionEntry> knownVersions =
				new HashMap<Integer,RFileVersionEntry>();
			for(RFileVersionEntry fe: re) {
				int version = fe.file.version;
				if(knownVersions.containsKey(version)) {
					o.printf("Duplicate version of %s:\n",
							fe.file.getPath());
					RFileVersionEntry oe =
						knownVersions.get(version);
					o.printf(" (%s) %s\n",
						DBBlock.formatBlockIDNice(
							oe.inBlk.getId()),
						oe.file.formatXML());
					o.printf(" (%s) %s\n",
						DBBlock.formatBlockIDNice(
							fe.inBlk.getId()),
						fe.file.formatXML());
					numInc++;
				} else {
					knownVersions.put(version, fe);
				}
			}
		}
		o.printf("%d inconsistencies detected.\n", numInc);
	}

	@Override
	public void printDocumentation() {
		o.printf("This is useful for debugging.");
	}

}
