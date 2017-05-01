package ma.jmbb;

import java.util.*;

import java.io.File;
import java.nio.file.Path;

/**
 * A database class with additional functionality as required for backup
 * creation.
 *
 * @version 1.1
 */
class BCDB extends DB {

	// Redundant processing data.
	private HashMap<String,DBFile> nonObsolete;

	/**
	 * Redundant processing table storing information about the greatest
	 * version of all known obsolete files. This is used to be able to
	 * set a correct version for a file which was temporarily (between
	 * to backup) nonexistent and therefore marked obsolete. Without this
	 * addtional data, the file (once re-created) would be added as "new"
	 * (version = 1) to the database. As this can be troublesome when
	 * performing backup restoration especailly with partial backups only
	 * to be reconstructed from block files, we aim at creating a uniqe
	 * version number.
	 */
	private HashMap<String,DBFile> newestObsolete;

	BCDB(Path root, PrintfIO o) throws MBBFailureException {
		super(root, o);
		nonObsolete    = null;
		newestObsolete = null;
	}

	DBBlock createNewBlock() throws MBBFailureException {
		return blocks.createNewBlock(this, passwords.getCurrentId(),
						header.getBlocksizeKiB());
	}

	void createNonObsoleteRedundantDataStructure() {
		nonObsolete    = new HashMap<String,DBFile>();
		newestObsolete = new HashMap<String,DBFile>();
		blocks.fillRedundantProcessingData(nonObsolete, newestObsolete);
	}

	BCChangedFile acquireChangedFileIfNecessary(final Stat s, boolean meta)
						throws MBBFailureException {
		// Removes file from map => afterwards only non-processed
		//						files remain.
		final String path = transformToDB(s.getPath());
		final DBFile prev = nonObsolete.remove(path);

		if(prev == null) {
			// Potential checksum equality MUST now be ignored
			// because the previous version obtained from
			// `newestObsolete`  points to a file already marked as
			// obsolete and if checksums are equal the file has just
			// been recreated.
			return new BCChangedFile(this, s,
					newestObsolete.remove(path), meta);
		} else {
			if(prev.logicalEquals(s))
				return null;

			// Checksum comparison happens here TODO single threaded

			BCChangedFile newVersion = new BCChangedFile(this, s,
								prev, meta);
			newVersion.checksumIfNecessary(this);

			if(newVersion.isContentwiseEqualToPreviousVersion())
				return null;
			else
				return newVersion;
		}
	}

	void markRemainingNonexistentFilesObsolete() {
		for(DBFile i: nonObsolete.values())
			i.obsolete();
	}

	String transformToDB(final String fileName) {
		return fileName.replace(File.separatorChar, header.getSep());
	}

	void removeMetaFilesFromObsoletionList() {
		// Cache because otherwise a concurrent modification exception
		// is thrown
		ArrayList<String> removeKeys = new ArrayList<String>();

		for(Map.Entry<String,DBFile> i: nonObsolete.entrySet())
			if(i.getValue().isMeta())
				removeKeys.add(i.getKey());

		for(String i: removeKeys)
			nonObsolete.remove(i);
	}

}
