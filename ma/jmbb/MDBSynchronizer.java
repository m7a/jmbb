package ma.jmbb;

import java.nio.file.Path;
import java.nio.file.Files;

import java.util.ArrayList;

class MDBSynchronizer {

	private final DB source;
	private final DB mirror;
	private final PrintfIO o;

	MDBSynchronizer(DB source, DB mirror, PrintfIO o) {
		super();
		this.source = source;
		this.mirror = mirror;
		this.o      = o;
	}

	void run() throws Exception {
		ArrayList<DBBlock> toBeDeleted = new ArrayList<DBBlock>();
		
		for(DBBlock i: source.blocks) {
			DBBlock r = getRespectiveMirrorBlock(i.getId());
			if(blockIsInMirror(r)) {
				if(blockHasBeenObsoletedSinceLastSync(i, r)) {
					toBeDeleted.add(r);
				}
			} else if(!i.isObsoletedInTheFirstPlace()) {
				copyBlockToMirror(i);
			}
		}

		deleteSuperflousBlocks(toBeDeleted);
	}

	private DBBlock getRespectiveMirrorBlock(long blockId) {
		for(DBBlock i: mirror.blocks) {
			if(i.getId() == blockId) {
				return i;
			}
		}
		return null;
	}

	private static boolean blockIsInMirror(DBBlock b) { // doc. function
		return b != null;
	}

	// documentatory function
	private static boolean blockHasBeenObsoletedSinceLastSync(DBBlock src,
								DBBlock mir) {
		// IOW src was obsolete when read from db, mir was not
		//  => must be a change from src to mir.
		return src.isObsoletedInTheFirstPlace() &&
					!mir.isObsoletedInTheFirstPlace();
	}

	private void copyBlockToMirror(DBBlock blk) throws Exception {
		Path newBlockFile = DBBlock.getFile(mirror, blk.getId());
		Path srcBlockFile = blk.getFile();
		
		if(!newBlockFile.equals(srcBlockFile)) {
			DBBlock.createParentsIfNecessary(newBlockFile);
		
			Files.copy(blk.getFile(), newBlockFile);
			blk.printMirrorNotice(o);
		}
	}

	private void deleteSuperflousBlocks(ArrayList<DBBlock> blks)
							throws Exception {
		for(DBBlock i: blks) {
			i.deleteAsObsolete(o);
		}
	}

}
