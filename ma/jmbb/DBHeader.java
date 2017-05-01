package ma.jmbb;

import java.io.*;

import java.security.MessageDigest;

import org.xml.sax.Attributes;

class DBHeader {

	private long blocksizeKiB;
	private char sep;
	private String checksumingMethod;

	private long timestamp;

	DBHeader() {
		super();
		sep               = '/'; // File.separatorChar;
		checksumingMethod = "SHA-256";
		blocksizeKiB      = DB.DEFAULT_BLOCKSIZE_KIB;
		// A RAM DB is always an OLD db
		// This becomes important upon Restoration times where "real"
		// DBs are preferred over "old" DBs
		timestamp         = 0;
	}

	void write(XMLWriter out) throws IOException {
		// Timestamp is updated on every write.
		out.tol("<db db_version=\"1.1.0.0\" sep=\"" + sep +
			"\" chcksm=\"" + checksumingMethod +
			"\" blocksize_kib=\"" + blocksizeKiB +
			"\" timestamp=\"" + System.currentTimeMillis() +
			"\">");
	}

	MessageDigest newMessageDigest() throws MBBFailureException {
		try {
			return MessageDigest.getInstance(checksumingMethod);
		} catch(Exception ex) {
			throw new MBBFailureException(ex);
		}
	}

	long getBlocksizeKiB() {
		return blocksizeKiB;
	}

	char getSep() {
		return sep;
	}

	void readFrom(Attributes attrs) {
		checksumingMethod = attrs.getValue("chcksm");
		blocksizeKiB = Long.parseLong(attrs.getValue("blocksize_kib"));
		sep = attrs.getValue("sep").charAt(0);
		timestamp = Long.parseLong(attrs.getValue("timestamp"));
	}

	long getTimestamp() {
		return timestamp;
	}

	void printStats(PrintfIO o) {
		o.printf(
			"DB CHG %s, BLK %d KiB, CHCKSM %s, SEP '%c'\n",
			RDateFormatter.format(timestamp), blocksizeKiB,
			checksumingMethod, sep
		);
	}

	void setDefaultBlockSize(long n) {
		blocksizeKiB = n;
	}

}
