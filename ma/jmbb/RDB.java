package ma.jmbb;

import java.io.File;
import java.io.IOException;

import java.nio.file.Path;
import java.nio.file.Files;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

class RDB extends EncryptionAwareDB {

	/**
	 * Maintain this for performance of looking up files.
	 *
	 * This is deliberatly initialized to null s.t. the program will crash
	 * if initializeKnownFilesBlocks() was not called. This would signal
	 * potential bugs avoiding inconsistent restores due to improperly
	 * populated cache.
	 */
	private Map<String,DBBlock> knownFilesBlocks;
	
	RDB(Path root, PrintfIO o) throws MBBFailureException {
		super(root, o);
		knownFilesBlocks = null;
	}

	void initializeKnownFilesBlocks() {
		knownFilesBlocks = new HashMap<String,DBBlock>();
		for(DBBlock j: blocks) {
			Iterator<DBFile> prevc = j.getFileIterator();
			while(prevc.hasNext()) {
				DBFile k = prevc.next();
				if(!k.isObsolete()) {
					knownFilesBlocks.put(k.getPath(), j);
				}
			}
		}
	}
	
	void addRestorationBlock(DBBlock blk) {
		performManualObsoletion(blk);
		blocks.add(blk);
	}

	private void performManualObsoletion(DBBlock blk) {
		Iterator<DBFile> cnt = blk.getFileIterator();
		while(cnt.hasNext()) {
			// Assertion: Each file is non-obsolete because it is
			//            from a block file where each of the files
			//            were still newly added.
			DBFile f = cnt.next();
			String path = f.getPath();
			if(knownFilesBlocks.containsKey(f.getPath())) {
				Iterator<DBFile> inner = knownFilesBlocks.get(
							path).getFileIterator();
				while(inner.hasNext()) {
					DBFile innerf = inner.next();
					if(innerf.matches(path)) {
						innerf.obsolete();
						break;
					}
				}
			}
			knownFilesBlocks.put(path, blk);
		}
	}

}
