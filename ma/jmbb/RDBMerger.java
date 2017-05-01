package ma.jmbb;

import java.util.Iterator;

class RDBMerger {

	private final PrintfIO o;
	private final RestorationMode mode;

	RDBMerger(PrintfIO o, RestorationMode mode) {
		super();
		this.o    = o;
		this.mode = mode;
	}

	RDB mergeDBs(RDB[] dbs) throws MBBFailureException {
		RDB result = determineNewestDB(dbs);

		RAllBlockMap blkMap = new RAllBlockMap(result);
		mergeDBS(blkMap, dbs);
		addNewBlocks(result, blkMap);

		return result;
	}

	private static RDB determineNewestDB(RDB[] dbs)
						throws MBBFailureException {
		long newestCache = -1;
		// Will be initialized even if the compiler does not believe it.
		int newestIndex = -1;
		for(int i = 0; i < dbs.length; i++) {
			if(dbs[i].header.getTimestamp() > newestCache) {
				newestCache = dbs[i].header.getTimestamp();
				newestIndex = i;
			}
		}
		if(newestCache == -1)
			throw new MBBFailureException("No databases found.");
		RDB ret = dbs[newestIndex];
		dbs[newestIndex] = null;
		return ret;
	}

	private static void mergeDBS(RAllBlockMap blkMap, RDB[] dbs) {
		for(int i = 0; i < dbs.length; i++)
			if(dbs[i] != null)
				blkMap.merge(dbs[i]);
	}

	private void addNewBlocks(RDB result, RAllBlockMap blkMap) {
		DBBlock lastOK = blkMap.get(result.blocks.getLastBlockId());
		Iterator<DBBlock> blkI = blkMap.iterateBlocks();
		DBBlock current;
		while(blkI.hasNext() && isAcceptableNextBlock((current =
							blkI.next()), result))
			result.addRestorationBlock(current);
	}

	private boolean isAcceptableNextBlock(DBBlock blk, DB result) { // DOC
		int delta = (int)(blk.getId() - result.blocks.getLastBlockId());
		if(delta != 1 ||
			mode == RestorationMode.RESTORE_AS_NEW_AS_POSSIBLE) {
			return true;
		} else {
			o.eprintf("Inconsistent restoration data. Missing %d " +
					"blocks between x%x and x%x.\n", delta,
				result.blocks.getLastBlockId(), blk.getId());
			return false;
		}
	}

}
