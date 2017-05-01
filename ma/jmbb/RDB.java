package ma.jmbb;

import java.io.File;
import java.io.IOException;

import java.nio.file.Path;
import java.nio.file.Files;

import java.util.Iterator;

class RDB extends EncryptionAwareDB {
	
	RDB(Path root, PrintfIO o) throws MBBFailureException {
		super(root, o);
	}
	
	void addRestorationBlock(DBBlock blk) {
		performManualObsoletion(blk);
		blocks.add(blk);
	}

	private void performManualObsoletion(DBBlock blk) {
		// Not too efficient but should be OK for the task
		// (n*m complexity where n*log(m) would be possible)
		Iterator<DBFile> cnt = blk.getFileIterator();
		while(cnt.hasNext()) {
			// Assertion: Each file is non-obsolete because it is
			//            from a block file where each of the files
			//            were still newly added.
			performManualFileObsoletion(cnt.next());
		}
	}

	private void performManualFileObsoletion(DBFile f) {
		for(DBBlock j: blocks) {
			Iterator<DBFile> prevc = j.getFileIterator();
			while(prevc.hasNext()) {
				DBFile k = prevc.next();
				if(!k.isObsolete() && k.matches(f.getPath())) {
					k.obsolete();
				}
			}
		}
	}

}
