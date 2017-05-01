package ma.jmbb;

import java.security.MessageDigest;

import java.io.*;

import java.nio.file.Path;
import java.nio.file.Files;

import org.tukaani.xz.*;

class BCCpioProcessor extends FailableThread {

	private final Process cpio;
	private final DBBlock result;
	private final DB db;

	BCCpioProcessor(Process cpio, DBBlock result, DB db) {
		super();
		this.cpio     = cpio;
		this.result   = result;
		this.db       = db;
	}

	public void runFailable() throws Exception {
		MessageDigest md = db.header.newMessageDigest();
		Path blkFile = result.getFile();

		InputStream in = cpio.getInputStream();
		try {
			OutputStream out = createOutputStream(blkFile);
			try {
				StreamUtility.copy(in, md, out);
			} finally {
				out.close();
			}
		} finally {
			in.close();
		}

		result.checksum = md.digest();
		result.outwardSize = Files.size(blkFile);
		result.setDetermined();
	}

	private OutputStream createOutputStream(final Path blk)
				throws MBBFailureException, IOException {
		return new XZOutputStream(
			Security.newAESOutputFilter(
				db.passwords.getCurrentValue(), 
				Files.newOutputStream(blk)
			),
			new LZMA2Options(db.xzPreset)
		);
	}

}
