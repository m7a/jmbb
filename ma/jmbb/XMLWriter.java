package ma.jmbb;

import java.io.*;

import static java.nio.charset.StandardCharsets.UTF_8;

class XMLWriter extends BufferedWriter {

	private String tabcache;

	XMLWriter(OutputStream stream) throws IOException {
		super(new OutputStreamWriter(stream, UTF_8));
		tabcache = "";
	}

	void tol(String str) throws IOException {
		txl(str);
		tabcache += "\t";
	}

	void tcl(String str) throws IOException {
		tabcache = tabcache.substring(1);
		txl(str);
	}

	void txl(String str) throws IOException {
		write(tabcache + str);
		newLine();
	}

	void txd(String str) throws IOException {
		String[] lines = str.split("\n");
		for(String i: lines) {
			txl(i);
		}
	}

}
