package ma.jmbb;

import java.nio.file.Path;
import java.io.InputStream;

class RCPIOMetaBlockReader extends FailableThread {

	private final Path f;
	private final InputStream in;
	private final PrintfIO o;
	private final RDB db;

	RCPIOMetaBlockReader(InputStream in, PrintfIO o, Path f, RDB db) {
		super();
		this.in = in;
		this.o  = o;
		this.f  = f;
		this.db = db;
	}

	@Override
	public void runFailable() throws Exception {
		new DBReader(db, f, o).readDatabase(in);
	}

}
