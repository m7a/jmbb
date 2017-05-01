package ma.jmbb;

import java.nio.file.Path;
import java.io.InputStream;

class RCPIOMetaReader extends FailableThread {

	private final Path f;
	private final InputStream in;
	private final PrintfIO o;

	private DBBlock result;

	RCPIOMetaReader(InputStream in, PrintfIO o, Path f) {
		super();
		this.in = in;
		this.o  = o;
		result  = null;
		this.f  = f;
	}

	public void runFailable() throws Exception {
		result = DBReader.readSingleBlock(o, in, f);
	}

	DBBlock getResult() {
		return result;
	}

}
