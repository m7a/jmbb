package ma.jmbb;

import java.util.*;

import java.nio.file.Path;
import java.nio.file.Files;

class RAllBlockMap {

	private final HashMap<Long, DBBlock> knownBlocks;

	// Sorted by block ID -- Important.
	private final TreeMap<Long, DBBlock> deltaBlocks;

	RAllBlockMap(DB result) {
		super();

		knownBlocks = new HashMap<Long, DBBlock>();
		for(DBBlock i: result.blocks) {
			knownBlocks.put(i.getId(), i);
		}

		deltaBlocks = new TreeMap<Long, DBBlock>();
	}

	void merge(DB db) {
		for(DBBlock i: db.blocks) {
			if(blockExists(i)) {
				mergeExistingBlock(i);
			}
		}
	}

	private static boolean blockExists(DBBlock blk) {
		return Files.exists(blk.getFile());
	}

	private void mergeExistingBlock(DBBlock blk) {
		DBBlock inDB = get(blk.getId());
		if(inDB == null) {
			deltaBlocks.put(blk.getId(), blk);
		} else if(!blockExists(inDB)) {
			inDB.changeOSFile(blk.getFile());
		}
	}

	DBBlock get(long key) {
		DBBlock ret = knownBlocks.get(key);
		if(ret == null) {
			ret = deltaBlocks.get(key);
		}
		return ret;
	}

	Iterator<DBBlock> iterateBlocks() {
		return deltaBlocks.values().iterator();
	}

}
