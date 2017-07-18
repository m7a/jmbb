package ma.jmbb;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import ma.tools2.util.StringUtils;

/**
 * ``New Times'' are a an advanced JMBB concept to cope with the fact that
 * sometimes files' timestamps may change with their contents remaining equal.
 *
 * To detect such cases, JMBB employs SHA-256 checksums and calculates the
 * checksum for each file whose timestamp has changed compared to the most
 * recent version of the file in the database. If the checksums (and all
 * metadata except for the modification time) match, the new file is not added
 * to the backup. This way, MBOX files from e-mail clients and other large files
 * potentially rewritten without changing their content need not inefficiently
 * be backed up again.
 *
 * While this approach works well most of the times, it does not scale: One of
 * the most time consuming tasks in a JMBB invocation is the traversal of files
 * in the file system. If large amounts of files have been rewritten but not
 * changed, JMBB will calculate the checksum for each of them
 * <em>upon each invocation</em>. If a large amount of files' timestamps was
 * changed just once, this is even less efficient than just backing them up
 * once more and then using the new timestamps to detect that files have
 * not changed (again) in all following backups.
 *
 * While one might argue that such scenarios are artificial and not of relevance
 * in practice, there is actually a very interesting special case which
 * shows the same symptoms: Upon upgrading to newer Java/Linux versions the
 * timestamps of files can become more accurate making JMBB detect all files
 * to be ``changed'' as per the timestamp but not in terms of checksum. This can
 * cause large running times (the whole file structure's checksums have to
 * be computed upon each invocation).
 *
 * Two solutions have been considered for solving this problem:
 *
 * <ol>
 * <li>Making the comparison of modification times less accurate.</li>
 * <li>Creating a cache of the ``new'' modification times to not detect the same
 * not-changed file again upon each invocation. (This requires/adds the
 * assumption that a file with newer modification time which was detected
 * <em>once</em> not to be changed will not change unless its modification
 * time changes <em>again</em>.)</li>
 * </ol>
 *
 * Although the first one is easier (and safer/more robust in terms of adding
 * bugs) to implement, the second one has actually been chosen because it
 * also catches all other cases where large amounts files have been rewritten
 * without change (possibly with a large difference in modification time).
 */
class DBNewTimes implements Iterable<Map.Entry<String,Long>> {

	/**
	 * Stores paths for which newer times than in the latest block
	 * containing that file are known. This means the map normally consumes
	 * little RAM and one can inspect the database easily seeing
	 * if it contains a lot of files which only changed in timestamp but
	 * not in content.
	 */
	private final Map<String,Long> dbNewTimes = new HashMap<String,Long>();

	/**
	 * This is a raw put access to the {@link #dbNewTimes} field.
	 * Backup cration should use the more specific methods (this allows
	 * adding useful consistency checks once they are invented)!
	 */
	void putDeserialized(String k, long v) {
		dbNewTimes.put(k, v);
	}

	/**
	 * @return newer time or <code>current</code> if no new time in DB.
	 */
	long getNewestKnownTimeFor(String fn, long current) {
		return dbNewTimes.containsKey(fn)? dbNewTimes.get(fn): current;
	}

	/**
	 * Records that the given file has changed (thus should be removed
	 * from ``new times'' because now the block has the most recent
	 * version).
	 */
	void updateTimeFileChanged(String fn, long newTime) {
		dbNewTimes.remove(fn);
	}

	void updateTimeFileNotChanged(String fn, long newTime) {
		dbNewTimes.put(fn, newTime);
	}

	void updateFileObsolete(String fn) {
		dbNewTimes.remove(fn);
	}

	void write(XMLWriter out) throws IOException {
		out.tol("<newtimes>");
		for(Map.Entry<String,Long> pair: dbNewTimes.entrySet())
			out.txl("<nte path=\"" +
				StringUtils.htmlentities(pair.getKey()) +
				"\" mtime=\"" + pair.getValue() + "\"/>");
		out.tcl("</newtimes>");
	}

	@Override
	public Iterator<Map.Entry<String,Long>> iterator() {
		return dbNewTimes.entrySet().iterator();
	}

}
