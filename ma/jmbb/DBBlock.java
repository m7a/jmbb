package ma.jmbb;

import java.util.ArrayList;
import java.util.Map;
import java.util.Iterator;
import java.io.IOException;

import java.nio.file.*;

import org.xml.sax.Attributes;

import ma.tools2.util.BinaryOperations;
import ma.tools2.util.NotImplementedException;

class DBBlock {

	private static final short TYPE_UNDEFINED = 0;
	private static final short TYPE_DATA      = 1;
	private static final short TYPE_META      = 2;
	private static final short TYPE_CTRL      = 4;

	static final int FILENAME_ID_LEN               = 16;
	private static final int FILENAME_MAKE_FOLDERS = 14;
	private static final String SUBDIR = "cnt";

	private static final String BLOCK_CHANGED_PREFIX = "DELTABLK";

	private final long id;
	final long passwordId;
	private final long blocksizeKiB;
	private final long timestamp;
	private final ArrayList<DBFile> files;
	private Path osFile;

	private short type; // Not actively used yet.

	private boolean tbd;

	// If each of the contained files is obsolete at the time of being added
	// to the block
	private boolean obsoletedInTheFirstPlace;

	long outwardSize;
	byte[] checksum;

	// For new block creation
	DBBlock(DB db, long id, long password, long blocksizeKiB)
						throws MBBFailureException {
		super();
		this.id                  = id;
		timestamp                = System.currentTimeMillis();
		this.passwordId          = password;
		this.blocksizeKiB        = blocksizeKiB;
		files                    = new ArrayList<DBFile>();
		outwardSize              = -1;
		type                     = TYPE_DATA;
		tbd                      = true;
		obsoletedInTheFirstPlace = false;
		osFile                   = getFile(db, id);
		createParentsIfNecessary(osFile);
	}

	DBBlock(DB db, Attributes attrs) {
		this(getFile(db, parseID(attrs)), attrs,
						parseBlocksize(db, attrs));
	}

	DBBlock(Path osFile, Attributes attrs, long blocksize) {
		super();
		id           = parseID(attrs);
		passwordId   = Long.parseLong(attrs.getValue("psswrd_id"));
		blocksizeKiB = blocksize;
		timestamp    = Long.parseLong(attrs.getValue("timestamp"));
		files        = new ArrayList<DBFile>();
		type         = parseType(attrs.getValue("type"));
		outwardSize  = Long.parseLong(attrs.getValue("outward_size"));
		checksum     = parseChecksum(attrs.getValue("chcksm"));
		tbd          = false;
		this.osFile  = osFile.toAbsolutePath();
		obsoletedInTheFirstPlace = true;
	}

	/**
	 * minimalistic constructor used in certain restoration scenarios
	 */
	DBBlock(Path osFile, long id) {
		super();
		this.osFile  = osFile.toAbsolutePath();
		this.id      = id;
		passwordId   = -1;
		blocksizeKiB = -1;
		timestamp    = -1;
		files        = new ArrayList<DBFile>();
	} 

	private static long parseID(Attributes attrs) {
		return parseID(attrs.getValue("id"));
	}

	static long parseID(String str) {
		return Long.parseLong(str.substring(1), 16);
	}

	static Path getFile(DB db, long id) {
		String name = formatID(id);
		String dir  = name.substring(0, FILENAME_MAKE_FOLDERS);
		Path ret    = db.loc.resolve(SUBDIR);

		while(dir.length() != 0) {
			ret = ret.resolve(dir.substring(0, 2));
			dir = dir.substring(2);
		}

		return ret.resolve(name + ".cxe").toAbsolutePath();
	}

	private static String formatID(long id) {
		return String.format("%0" + FILENAME_ID_LEN + "x", id);
	}

	static void createParentsIfNecessary(Path osFile)
						throws MBBFailureException {
		Path parent = osFile.getParent();
		if(!Files.exists(parent)) {
			try {
				Files.createDirectories(parent);
			} catch(IOException ex) {
				throw new MBBFailureException(ex);
			}
		}
	}

	private static long parseBlocksize(DB db, Attributes attrs) {
		if(attrs.getValue("blocksize") == null)
			return db.header.getBlocksizeKiB();
		else
			return Long.parseLong(attrs.getValue("blocksize"));
	}

	private static short parseType(String s) {
		if(s.equals("data"))
			return TYPE_DATA;
		else if(s.equals("data-meta"))
			return TYPE_DATA | TYPE_META;
		else if(s.equals("meta"))
			return TYPE_META;
		else if(s.equals("ctrl-var"))
			return TYPE_CTRL;
		else
			throw new NotImplementedException("Block Type " + s +
								" unknown.");
	}

	private static byte[] parseChecksum(String raw) {
		if(raw.equals("TBD"))
			return null;
		else
			return BinaryOperations.decodeHexString(raw);
	}

	void write(XMLWriter out) throws IOException {
		out.tol(
			"<block psswrd_id=\"" + passwordId +
			"\" blocksize=\"" + blocksizeKiB +
			"\" chcksm=\"" + getChecksum() +
			"\" id=\"" + formatXMLBlockId() +
			"\" outward_size=\"" + outwardSize +
			"\" timestamp=\"" + timestamp +
			"\" type=\"" + formatXMLBlockType(type) + "\">"
		);
		for(DBFile i: files)
			i.write(out);
		out.tcl("</block>");
	}

	private String getChecksum() {
		if(tbd)
			return "TBD";
		else
			return formatChecksum(checksum);
	}

	static String formatChecksum(byte[] data) {
		StringBuilder ret = new StringBuilder(data.length * 2);
		for(byte i: data)
			ret.append(String.format("%02x", i));

		return ret.toString();
	}

	String formatXMLBlockId() {
		return String.format("x%x", id);
	}

	private static String formatXMLBlockType(short type) {
		if(type == TYPE_DATA)
			return "data";
		else if(type == (TYPE_DATA | TYPE_META))
			return "data-meta";
		else if(type == TYPE_META)
			return "meta";
		else if((type & TYPE_CTRL) != 0)
			return "ctrl-var";
		else
			throw new NotImplementedException();
	}

	// TODO z might it be necessary to call transformToDB before putting to map?
	void fillRedundantProcessingData(Map<String,DBFile> s,
							Map<String,DBFile> o) {
		for(DBFile i: files) {
			if(i.isObsolete()) {
				DBFile c = o.get(i.getPath());
				if(c == null || c.version < i.version)
					o.put(i.getPath(), i);
			} else {
				s.put(i.getPath(), i);
			}
		}
	}

	void addFile(DBFile f) {
		if(!f.isObsolete() && obsoletedInTheFirstPlace)
			obsoletedInTheFirstPlace = false;

		if(f.isMeta())
			type |= TYPE_META;

		files.add(f);
	}

	void setDetermined() {
		tbd = false;
	}

	long getId() {
		return id;
	}

	void printCreationNotice(PrintfIO o) throws MBBFailureException {
		printNotice(o, "NEW");
	}

	private void printNotice(PrintfIO o, String action) {
		o.printf(BLOCK_CHANGED_PREFIX + ",%s,%s,%s\n", action,
				formatXMLBlockId(), getFile().toString());
	}

	void deleteIfNewlyObsolete(PrintfIO o) throws Exception {
		if(isNewlyObsolete())
			deleteAsObsolete(o);
	}

	private boolean isNewlyObsolete() {
		if(obsoletedInTheFirstPlace) {
			// obsoleted since creation => obsoletion can not be new
			return false;
		}

		boolean newlyObsolete = false;

		for(DBFile i: files) {
			if(i.isObsolete())
				newlyObsolete |= i.isNewlyObsolete();
			else
				return false; // => not obsolete at all
		}

		return newlyObsolete;
	}

	void deleteAsObsolete(PrintfIO o)  throws Exception {
		Path file = getFile();
		boolean deletionConfirmed;
		try {
			deletionConfirmed = Files.deleteIfExists(file);
		} catch(Exception ex) {
			throw ex;
		}
		String add = deletionConfirmed? "/DELETED": "";
		printNotice(o, "OBSOLETED" + add);
	}

	Path getFile() {
		return osFile;
	}

	// WARNING: This is a great step. It is only used upon restoration times
	void changeOSFile(Path p) {
		osFile = p;
	}

	Iterator<DBFile> getFileIterator() {
		return files.iterator();
	}

	boolean isObsoletedInTheFirstPlace() {
		return obsoletedInTheFirstPlace;
	}

	boolean activelyUsesPassword(long id) {
		// Assumption
		// ----------
		// We use isObsoletedInTheFirstPlace to improve performace by
		// postponing a password obsoletion by probably one invocation.
		return id == passwordId && !isObsoletedInTheFirstPlace();
	}

	boolean isMetaBlock() {
		return (type & TYPE_META) != 0;
	}

	void printMirrorNotice(PrintfIO o) {
		printNotice(o, "NEW/MIRRORED");
	}

	void print(PrintfIO o) {
		printShort(o);
		long n = 0;
		for(DBFile i: files) {
			o.printf("%s\n", i.getPath());
			REntry.print(o, i, -1);
			n++;
		}
		o.printf("This block contains %d files.\n", n);
	}

	void printShort(PrintfIO o) {
		o.printf("DBBlock %6s  size %6d KiB  password %d  created %s\n",
			formatXMLBlockId(), outwardSize / 1024, passwordId,
			RDateFormatter.format(timestamp));
	}

	static String formatBlockIDNice(long blockID) {
		return blockID == -1? "": formatID(blockID);
	}

}
