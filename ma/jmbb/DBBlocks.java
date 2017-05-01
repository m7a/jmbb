package ma.jmbb;

import java.io.IOException;

import java.util.*;

import java.nio.file.Files;

class DBBlocks extends ArrayList<DBBlock> {

	private static final long META_BLOCK_EVERY = 16;
	private static final long NO_ID = 0;

	private long lastBlockId;
	private long lastMetaBlockId;

	DBBlocks() {
		super();
		lastBlockId = NO_ID;
		lastMetaBlockId = NO_ID;
	}

	void write(XMLWriter out) throws IOException {
		out.tol("<blocks>");
		writeBlockList(out, this);
		out.tcl("</blocks>");
	}

	private static void writeBlockList(XMLWriter out, List<DBBlock> list)
							throws IOException {
		for(DBBlock i: list) {
			i.write(out);
		}
	} 

	DBBlock createNewBlock(DB db, long password, long blocksizeKiB)
						throws MBBFailureException {
		return new DBBlock(db, ++lastBlockId, password, blocksizeKiB);
	}

	void fillRedundantProcessingData(Map<String,DBFile> s,
							Map<String,DBFile> o) {
		for(DBBlock i: this) {
			i.fillRedundantProcessingData(s, o);
		}
	}

	/**
	 * Updates <code>lastBlockId</code> and <code>lastMetaBlockId</code> if
	 * necessary.
	 *
	 * @see #lastBlockId
	 */
	public boolean add(DBBlock b) {
		if(b.getId() > lastBlockId) {
			lastBlockId = b.getId();
		}
		if(b.getId() > lastMetaBlockId && b.isMetaBlock()) {
			lastMetaBlockId = b.getId();
		}
		return super.add(b);
	}

	void deleteNewlyObsolete(PrintfIO o) throws MBBFailureException {
		for(DBBlock i: this) {
			try {
				i.deleteIfNewlyObsolete(o);
			} catch(Exception ex) {
				throw new MBBFailureException(ex);
			}
		}
	}

	long getLastBlockId() {
		return lastBlockId;
	}

	void printStats(PrintfIO o) {
		long blocks = 0, blkact = 0, blksiz = 0, blkactsiz = 0;
		long metaBlocks = 0, activeMetaBlocks = 0;
		long filecount = 0, fileact = 0, filesiz = 0, fileactsiz = 0;

		for(DBBlock i: this) {
			blocks++;
			blksiz += i.outwardSize;
			boolean meta = i.isMetaBlock();
			if(meta) {
				metaBlocks++;
			}
			if(!i.isObsoletedInTheFirstPlace()) {
				blkact++;
				blkactsiz += i.outwardSize;
				if(meta) {
					activeMetaBlocks++;
				}
			}
			Iterator<DBFile> jI = i.getFileIterator();
			while(jI.hasNext()) {
				DBFile j = jI.next();
				filecount++;
				filesiz += j.size;
				if(!j.isObsolete()) {
					fileact++;
					fileactsiz += j.size;
				}
			}
		}

		o.printf("  Block count %11d, active %11d  %3d %%, meta %d/%d" +
				"\n", blocks, blkact, blkact * 100 / blocks,
						metaBlocks, activeMetaBlocks);
		o.printf("  Block size  %7d MiB, active %7d MiB  %3d %%\n",
					mib(blksiz), mib(blkactsiz),
					blkactsiz * 100 / blksiz);
		o.printf("  Files       %11d, active %11d  %3d %%\n",
				filecount, fileact, fileact * 100 / filecount);
		o.printf("  Data size   %7d MiB, active %7d MiB  %3d %%, " +
			"comp/orig %d %%\n", mib(filesiz),
			mib(fileactsiz), fileactsiz * 100 / filesiz,
			blkactsiz * 100 / fileactsiz
		);
	}

	private static long mib(long in) {
		return in / 1024 / 1024;
	}

	boolean isAddingMetaFileNecessary(DB db) {
		return ((lastBlockId - lastMetaBlockId) >= META_BLOCK_EVERY &&
						Files.exists(db.getDBFile()));
	}

}
