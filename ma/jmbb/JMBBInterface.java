package ma.jmbb;

import java.io.File;
import java.util.List;

public class JMBBInterface {

	static final int DEFAULT_BUFFER = 0x1000;

	private final PrintfIO o;

	public JMBBInterface(PrintfIO o) {
		super();
		this.o = o;
	}

	public void backupUpdateDB(File db, List<File> src)
						throws MBBFailureException {
		new BackupCreator(o, db, src).run();
	}

	public void mirror(File db, File dst) throws MBBFailureException {
		new Mirrorer(o, db, dst).run();
	}

	public void restore(List<File> src, File dst, String pattern,
				RestorationMode mode, int version,
				boolean findBlocksByFileName, long useMetaBlock)
				throws MBBFailureException {
		new Restorer(o, src, dst, pattern, mode, version,
				findBlocksByFileName, useMetaBlock).run();
	}

	public void edit(File db) throws MBBFailureException {
		new Editor(o, db).run();
	}

	public void reportIntegrity(File db, File root)
						throws MBBFailureException {
		new IntegrityReport(o, db, root).run();
	}

}
