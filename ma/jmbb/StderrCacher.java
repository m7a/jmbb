package ma.jmbb;

import java.io.*;

class StderrCacher extends FailableThread {

	private final Process cpio;
	private final StringBuilder stderr;

	StderrCacher(Process cpio) {
		super();
		this.cpio = cpio;
		stderr = new StringBuilder();
	}

	public void runFailable() throws IOException {
		BufferedReader cpioErr = new BufferedReader(
				new InputStreamReader(cpio.getErrorStream()));
		String line;
		try {
			while((line = cpioErr.readLine()) != null) {
				stderr.append(line);
				stderr.append('\n');
			}
		} finally {
			cpioErr.close();
		}
	}

	boolean hasOutput() {
		return stderr.length() != 0;
	}

	String getOutput() {
		return stderr.toString();
	}

}
