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
					RestorationMode mode, int version)
						throws MBBFailureException {
		new Restorer(o, src, dst, pattern, mode, version).run();
	}

	//public void drop(File mirror) throws MBBFailureException {
	//	throw new MBBFailureException(
	//		new ma.tools2.util.NotImplementedException("N_IMPL"));
	//}

	//public void grab(File dst) throws MBBFailureException {
	//	throw new MBBFailureException(
	//		new ma.tools2.util.NotImplementedException("N_IMPL"));
	//}

	public void edit(File db) throws MBBFailureException {
		new Editor(o, db).run();
	}

}
