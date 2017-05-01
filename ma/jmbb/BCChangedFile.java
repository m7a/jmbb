package ma.jmbb;

import java.io.*;
import java.security.MessageDigest;

class BCChangedFile {

	final DBFile change;
	final DBFile prev;

	final String osPath;

	BCChangedFile(final BCDB db, final Stat s, final DBFile f, boolean meta)
						throws MBBFailureException {
		super();

		final long timestamp;
		final int version;
		if(f == null) {
			timestamp = System.currentTimeMillis();
			version   = 1;
		} else {
			timestamp = f.timestamp;
			version   = f.version + 1;
		}

		// Do not set an obsolete file to be the previous file in order
		// not to compare checksums and decide not to add the file.
		// When the previous file was obsolete, the new file always
		// has to be added.
		if(f == null || f.isObsolete())
			prev = null;
		else
			prev = f;

		osPath = s.getPath();
		change = new DBFile(
			db.transformToDB(osPath), s.size, s.modificationTime,
			s.mode, timestamp, null, false, version, meta
		);
	}

	// Remember (10.2014):
	// Checksums are calculated if needed which sometimes makes them single-
	// threaded which can currently not be avoided.
	void checksumIfNecessary(DB db) throws MBBFailureException {
		if(change.checksum == null)
			checksum(db);
	}

	private void checksum(DB db) throws MBBFailureException {
		if(change.isRegularFile()) {
			MessageDigest md = db.header.newMessageDigest();
			try {
				change.checksum = digest(md);
			} catch(Exception ex) {
				throw new MBBFailureException(ex);
			}
		}
	}

	private byte[] digest(MessageDigest md) throws Exception {
		FileInputStream in = new FileInputStream(osPath);
		byte[] buf = new byte[JMBBInterface.DEFAULT_BUFFER];
		int len;
		try {
			while((len = in.read(buf, 0, buf.length)) != -1)
				md.update(buf, 0, len);

			return md.digest();
		} catch(IOException ex) {
			throw ex;
		} finally {
			try {
				in.close();
			} catch(IOException ex2) {
				throw ex2;
			}
		}
	}

	// Also compares content-related metadata (mode and size)
	// Checksuming of the new file must be possible (cf.
	// isCheckusmingPossible()) in order to make sure, no change has been
	// made. An exception is possible for directories: If a directory has
	// not changed in mode, it is assumed to still be equal to the previous
	// one.
	//
	// Use of this method: BCDB.acquireChangedFileIfNecessary
	boolean isContentwiseEqualToPreviousVersion() {
		return prev != null && !prev.isObsolete() &&
			prev.isMetadataEqual(change) &&
			(change.isDirectory() ||
				(change.isRegularFile() &&
				Security.isChecksumEqual(prev.checksum,
							change.checksum))
			);
	}

	// -- Debug only -------------------------------------------------------

	public String toString() {
		StringBuilder o = new StringBuilder("BCChangedFile(prev=(");
		if(prev == null)
			o.append("null");
		else
			o.append(prev.toString());
		o.append("), change=(");
		o.append(change.toString());
		o.append("), osPath=");
		o.append(osPath);
		o.append(")");
		return o.toString();
	}

}
