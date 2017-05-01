package ma.jmbb;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;

class REntry {

	private static final String TIMESTAMP_MISMATCH =
		"ERROR (non-failing): Different timestamps for the same " +
		"path %d != %d. This means that either you are about to " +
		"restore from different databases (which will cause " +
		"extremly inconsistent restoration data) or that there is " +
		"an implementation error and the database contains two " +
		"times the same version of a single path.\n";

	private final ArrayList<RFileVersionEntry> fileVersions;

	REntry(DBFile file, DBBlock inBlk) {
		super();
		fileVersions = new ArrayList<RFileVersionEntry>();
		add(file, inBlk);
	}

	void add(DBFile file, DBBlock inBlk) {
		fileVersions.add(new RFileVersionEntry(file, inBlk));
	}

	/**
	 * Print out information about all versions of this entry in a mainly
	 * human readable but ideally also machine-parsable manner which
	 * allows one to decide about wether to choose to restore this file.
	 */
	void print(PrintfIO o) {
		Collections.sort(fileVersions, new FileVersionComparator());
		final DBFile first = fileVersions.get(0).file;

		printInformationHeader(o, first);
		for(RFileVersionEntry i: fileVersions) {
			print(o, i, first.timestamp);
		}
	}

	private static void printInformationHeader(PrintfIO o, DBFile file) {
		o.printf("FILE \"%s\" first seen %s\n", file.getPath(),
					RDateFormatter.format(file.timestamp));
	}

	private static void print(PrintfIO o, RFileVersionEntry i,
							long commonTimestamp) {
		if(i.file.timestamp != commonTimestamp) {
			o.eprintf(TIMESTAMP_MISMATCH, i.file.timestamp,
								commonTimestamp);
		}

		print(o, i.file, i.inBlk.getId());
	}

	static void print(PrintfIO o, DBFile file, long blockId) {
		String blkS;
		if(blockId == -1) {
			blkS = "";
		} else {
			blkS = String.format("0x%0" + DBBlock.FILENAME_ID_LEN +
								"x ", blockId);
		}

		// Version, Modification Time, Size, Mode, (Block ID), Obsolete
		o.printf("  %3d %s %6d %6o %s%s\n",
			file.version,
			RDateFormatter.format(file.modificationTime),
			file.size / 1024,
			file.mode,
			blkS,
			file.isObsolete() ? "obsolete": ""
		);
	}

	/**
	 * That is: the greatest version of the newest file entry.
	 *
	 * @return
	 * 	If the file selected for restoration is deprecated,
	 * 	this returns null.
	 */
	RFileVersionEntry getSelectedForRestoration() {
		RFileVersionEntry sel = fileVersions.get(0);

		for(RFileVersionEntry i: fileVersions) {
			if(i.file.timestamp > sel.file.timestamp) {
				sel = i;
			} else if(i.file.version > sel.file.version) {
				sel = i;
			}
		}

		if(sel.file.isObsolete()) {
			return null;
		} else {
			return sel;
		}
	}

	private class FileVersionComparator
				implements Comparator<RFileVersionEntry> {

		private FileVersionComparator() {
			super();
		}

		public int compare(RFileVersionEntry a, RFileVersionEntry b) {
			if(a.file.version > b.file.version) {
				return  1;
			} else if(a.file.version == b.file.version) {
				return  0;
			} else {
				return -1;
			}
		}

	}

}
