package ma.jmbb;

import java.io.IOException;

import org.xml.sax.Attributes;

import ma.tools2.util.StringUtils;
import ma.tools2.util.BinaryOperations;

/**
 * @version 1.0.0.1
 */
class DBFile extends Stat {

	private static final long NEW_TIME_NOT_SET = -1;

	// String: makes it easier to replace one character with two...
	private static final String CPIO_ESC = "\\";
	private static final String[] CPIO_PATTERNS = { CPIO_ESC, "[", "?",
									"*" };

	final long timestamp;
	final int version;
	byte[] checksum;

	private boolean obsolete;
	private boolean newlyObsolete; // not writen to XML <=> internal only.

	private final boolean meta;

	DBFile(String path, long size, long modificationTime, int mode,
				long timestamp, byte[] checksum,
				boolean obsolete, int version, boolean meta) {
		super(path, size, modificationTime, mode);
		this.timestamp = timestamp;
		this.checksum  = checksum;
		this.version   = version;
		this.obsolete  = obsolete;
		this.meta      = meta;
		newlyObsolete  = false;
	}

	DBFile(Attributes attrs) {
		super(
			attrs.getValue("path"),
			Long.parseLong(attrs.getValue("size")),
			Long.parseLong(attrs.getValue("mtime")),
			Integer.parseInt(attrs.getValue("mode"), 8) // octal (!)
		);
		timestamp     = Long.parseLong(attrs.getValue("timestamp"));
		checksum      = parseChecksum(attrs.getValue("chcksm"));
		version       = Integer.parseInt(attrs.getValue("version"));
		obsolete      = attrs.getValue("obsolete").equals("true");
		meta          = attrs.getValue("meta").equals("true");
		newlyObsolete = false;
	}

	private static byte[] parseChecksum(String raw) {
		if(raw.equals("unknown"))
			return null;
		else
			return BinaryOperations.decodeHexString(raw);
	}

	void write(XMLWriter out) throws IOException {
		out.txl(formatXML());
	}

	String formatXML() {
		String obsoleteS = obsolete ? "true": "false";
		String metaS = meta ? " meta=\"true\"": "";
		return "<file obsolete=\"" + obsoleteS +
			"\" timestamp=\"" + timestamp +
			"\" mtime=\"" + modificationTime +
			"\" version=\"" + version +
			"\" chcksm=\"" + formatChecksum() +
			"\" mode=\"" + formatMode() +
			"\" size=\"" + size + 
			"\" path=\"" + StringUtils.htmlentities(getPath()) +
			"\"" + metaS + "/>";
	}

	private String formatChecksum() {
		if(checksum == null) {
			return "unknown";
		} else {
			return DBBlock.formatChecksum(checksum);
		}
	}

	private String formatMode() {
		return String.format("%o", mode); // octal formatting
	}

	// Does not check paths for that is already done when this function
	// is called. Checksums are only compared once a BCChangedFile has
	// already been created (checksum comparison only applies if all other
	// metadata points to the file being different because it is
	// performace-intensive to compute)
	boolean logicalEquals(Stat s, DBNewTimes newTimes) {
		return isMetadataEqual(s) && newTimes.getNewestKnownTimeFor(
			getPath(), modificationTime) == s.modificationTime;
	}

	// Similar to logicalEquals(Stat) but does not compare modification time
	boolean isMetadataEqual(Stat s) {
		return mode == s.mode && size == s.size;
	}

	/**
	 * This is not a simple setObsolete(). It is used if a file is "newly"
	 * obsolete, i.e. if it was just replaced by a newer version.
	 */
	void obsolete() {
		if(!obsolete) {
			obsolete      = true;
			newlyObsolete = true;
		}
	}

	boolean isObsolete() {
		return obsolete;
	}

	boolean isNewlyObsolete() {
		return newlyObsolete;
	}
	
	/*
	 * CPIO is a UNIX utility =&gt; fixed DB separator '/' will be accepted
	 * as input. Also, CPIO's special "pattern matching" characers will be
	 * escaped.
	 */
	String getCPIOPath() {
		String path = getPath();
		// Also be able to remove Windows "Root" directories
		String ret = path.substring(path.indexOf("/") + 1);

		// Escapes CPIO pattern matching characters
		for(int i = 0; i < CPIO_PATTERNS.length; i++)
			ret = ret.replace(CPIO_PATTERNS[i], CPIO_ESC +
							CPIO_PATTERNS[i]);

		return ret;
	}

	boolean isMeta() {
		return meta;
	}

	// -- Debug only -------------------------------------------------------

	public String toString() {
		StringBuilder ret = new StringBuilder("DBFile(");
		ret.append(super.toString());
		ret.append(", timestamp=");
		ret.append(timestamp);
		ret.append(", version=");
		ret.append(version);
		ret.append(", checksum=");
		ret.append(formatChecksum());
		ret.append(", obsolete=");
		ret.append(obsolete);
		ret.append(")");
		return ret.toString();
	}

}
