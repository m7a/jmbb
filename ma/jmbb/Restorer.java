package ma.jmbb;

import java.io.File;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import ma.tools2.util.NotImplementedException;

class Restorer {

	private final PrintfIO        o;
	private final List<File>      src;
	private final File            dst;
	private final String          pattern;
	private final RestorationMode mode;
	private final int             version;

	Restorer(PrintfIO o, List<File> src, File dst, String pattern,
					RestorationMode mode, int version) {
		super();
		this.o       = o;
		this.src     = src;
		this.dst     = dst;
		this.pattern = pattern;
		this.mode    = mode;
		this.version = version;
	}

	void run() throws MBBFailureException {
		mkdst();

		RDB[] sourceDBs = createDBForEachSource();
		RDB main = mergeDBs(sourceDBs);

		restoreFromDB(main);
	}

	private void mkdst() throws MBBFailureException {
		if(!dst.exists())
			if(!dst.mkdirs())
				throw new MBBFailureException("Unable to " +
					"create destination directory.");
	}

	private RDB[] createDBForEachSource() throws MBBFailureException {
		return new RDBCreator(o, src).createDBForEachSource();
	}

	private RDB mergeDBs(RDB[] sourceDBs) throws MBBFailureException {
		return new RDBMerger(o, mode).mergeDBs(sourceDBs);
	}

	private void restoreFromDB(DB db) throws MBBFailureException {
		Map<String, REntry> restorePaths = createRestorePathTable(db);
		Iterator<REntry> entries = restorePaths.values().iterator();

		if(mode == RestorationMode.LIST_VERSIONS_ONLY)
			listVersionsOnly(entries);
		else
			restoreNewest(db, entries);
	}

	/**
	 * Creates a table of all files matching the pattern and all available
	 * versions of these files.
	 */
	private Map<String, REntry> createRestorePathTable(DB db) {
		Pattern regex = compileRegex();
		return createRestorePathTable(db, regex, version);
	}

	static Map<String, REntry> createRestorePathTable(DB db, Pattern regex,
								int version) {
		// Unsorted... we will sort after blocks later.
		Map<String, REntry> rpt = new HashMap<String, REntry>();

		for(DBBlock i: db.blocks) {
			Iterator<DBFile> containedFiles = i.getFileIterator();
			while(containedFiles.hasNext()) {
				DBFile j = containedFiles.next();
				addFileToRestorePathTable(rpt, regex, version,
									j, i);
			}
		}

		return rpt;
	}

	/**
	 * The result of this function is designed to be passed to
	 * <code>addFileToRestorePathTable(Map, Pattern, int, DBFile,
	 * DBBlock)</code> and <code>matchPattern(Pattern, DBFile)</code>
	 * which both accept null values to be passed for a simple
	 * "all input is ok".
	 *
	 * @return null if no pattern was wanted.
	 * @see #addFileToRestorePathTable(Map<String, REntry>, Pattern, DBFile,
	 * 	DBBlock)
	 * @see #matchPattern(Pattern, DBFile)
	 */
	private Pattern compileRegex() {
		if(pattern == null)
			return null;
		else
			return Pattern.compile(pattern);
	}

	/**
	 * Too many parameters but this function needed to be externalized.
	 *
	 * @param regex may be null if not used.
	 */
	private static void addFileToRestorePathTable(Map<String, REntry> rpt,
						Pattern regex, int version,
						DBFile file, DBBlock inBlk) {
		if(matchPattern(regex, file) && matchVersion(version, file) &&
							!file.isMeta()) {
			REntry alreadyHave = rpt.get(file.getPath());
			if(alreadyHave == null)
				rpt.put(file.getPath(),
						new REntry(file, inBlk));
			else
				alreadyHave.add(file, inBlk);
		}
	}

	/**
	 * @param regex always returns true if regex == null.
	 */
	private static boolean matchPattern(Pattern regex, DBFile file) {
		return regex == null || regex.matcher(file.getPath()).matches();
	}

	private static boolean matchVersion(int version, DBFile file) {
		return version == -1 || file.version == version;
	}

	private void listVersionsOnly(Iterator<REntry> entries) {
		while(entries.hasNext())
			entries.next().print(o);
	}

	private void restoreNewest(DB db, Iterator<REntry> entries)
						throws MBBFailureException {
		Map<Long, RGroup> tab = newTableOfFilesGroupBlocks(entries);

		for(RGroup i: tab.values()) {
			try {
				i.restore(db, dst);
			} catch(MBBFailureException ex) {
				o.edprintf(ex, "Unable to restore group of " +
						"block %s.\n",
						i.formatBlockId());
			}
		}
	}

	private static Map<Long, RGroup> newTableOfFilesGroupBlocks(
						Iterator<REntry> entries) {
		// TreeMap => sort by block id.
		Map<Long, RGroup> retTable = new TreeMap<Long, RGroup>();

		while(entries.hasNext())
			addEntryToGroupTable(retTable, entries.next());

		return retTable;
	}

	private static void addEntryToGroupTable(Map<Long, RGroup> tab,
								REntry e) {
		RFileVersionEntry sel = e.getSelectedForRestoration();
		if(sel == null) // File entry deprecated.
			return;

		RGroup grp = tab.get(sel.inBlk.getId());
		if(grp == null) {
			grp = new RGroup(sel.inBlk);
			tab.put(sel.inBlk.getId(), grp);
		}
		grp.add(sel.file);
	}

}
