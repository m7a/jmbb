package ma.jmbb;

import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;

class Stat {

	// Warning: Octal notation follows
	private static final int DEFAULT_UNIX_PERM_MODE =    0755;
	private static final int MODE_MASK              =  ~07777;
	private static final int S_IFREG                = 0100000;
	private static final int S_IFDIR                =  040000;
	private static final int S_IFLNK                = 0120000;

	private String path;
	final long size;
	final long modificationTime;
	final int mode;

	Stat(String path, long size, long modificationTime, int mode) {
		super();
		this.path             = path;
		this.size             = size;
		this.modificationTime = modificationTime;
		this.mode             = mode;
	}

	Stat(String path, Map<String,Object> raw) throws MBBFailureException {
		super();
		this.path = path;
		try {
			size             = (Long)raw.get("size");
			modificationTime = parseModificationTime(raw);
			mode             = (Integer)raw.get("mode");
		} catch(Exception ex) {
			throw new MBBFailureException(
					"Could not parse raw stat.", ex);
		}
	}

	Stat(String path, BasicFileAttributes a) throws MBBFailureException {
		super();
		this.path             = path;
		this.size             = a.size();
		this.modificationTime = parseFileTime(a.lastModifiedTime());
		this.mode             = createPseudoMode(a);
	}

	private static long parseModificationTime(Map<String,Object> raw) {
		return parseFileTime((FileTime)raw.get("lastModifiedTime"));
	}

	private static long parseFileTime(FileTime in) {
		return in.toMillis();
	}

	private static int createPseudoMode(BasicFileAttributes a)
						throws MBBFailureException {
		int ret = DEFAULT_UNIX_PERM_MODE;
		if(a.isDirectory()) {
			ret |= Stat.S_IFDIR;
		} else if(a.isRegularFile()) {
			ret |= Stat.S_IFREG;
		} else if(a.isSymbolicLink()) {
			ret |= Stat.S_IFLNK;
		} else {
			throw new MBBFailureException("Can not create pseudo " +
								"mode.");
		}
		return ret;
	}

	boolean matches(String fname) {
		return path.equals(fname);
	}

	boolean isRegularFile() {
		return getFiletype() == S_IFREG;
	}

	private int getFiletype() {
		return mode & MODE_MASK;
	}

	boolean isDirectory() {
		return getFiletype() == S_IFDIR;
	}

	String getPath() {	
		return path;
	}

	boolean rename(String a, String b) {
		boolean chg = path.indexOf(a) == 0;
		if(chg) {
			path = b + path.substring(a.length());
		}
		return chg;
	}

	// -- Debug only -------------------------------------------------------

	public String toString() {
		StringBuilder ret = new StringBuilder("Stat(");
		ret.append("path=");
		ret.append(path);
		ret.append(", size=");
		ret.append(size);
		ret.append(", modificationTime=");
		ret.append(modificationTime);
		ret.append(", mode=");
		ret.append(String.format("%o", mode));
		ret.append(")");
		return ret.toString();
	}

}
