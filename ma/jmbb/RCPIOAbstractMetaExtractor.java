package ma.jmbb;

import java.io.*;

import org.tukaani.xz.XZInputStream;

/**
 * Any method and field declared by this class should only be accessed by
 * subclasses.
 */
abstract class RCPIOAbstractMetaExtractor {

	final PrintfIO o;
	final RDB      db;

	RCPIOAbstractMetaExtractor(RDB db, PrintfIO o) {
		super();
		this.db = db;
		this.o  = o;
	}

	protected abstract InputStream openSrcFile() throws IOException;

	InputStream createInputStream() throws Exception {
		return new XZInputStream(new AESCryptInputFilter(getPassword(),
					openSrcFile()));
	}

	/**
	 * Can be overriden in subclass to return block-specific password.
	 * Defaults to the latest one.
	 */
	String getPassword() {
		return db.passwords.getCurrentValue();
	}

	void writeDecryptedToCPIO(InputStream in, Process cpioP)
							throws Exception {
		OutputStream out = cpioP.getOutputStream();
		try {
			StreamUtility.copy(in, null, out);
		} finally {
			out.close();
		}
	}

}
