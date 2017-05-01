package ma.jmbb;

import java.io.*;

import java.nio.file.Files;

import java.security.MessageDigest;

import java.util.Arrays;

import org.tukaani.xz.XZInputStream;

class RCpioRestorer {

	private final DB     db;
	private final File   dst;
	private final RGroup grp;

	RCpioRestorer(DB db, File dst, RGroup group) {
		super();
		this.db  = db;
		this.dst = dst;
		grp      = group;
	}

	void run() throws MBBFailureException {
		try {
			File list = createListFile();
			try {
				restoreFromListFile(list);
			} finally {
				list.delete();
			}
		} catch(MBBFailureException ex) {
			throw ex;
		} catch(Exception ex) {
			throw new MBBFailureException(ex);
		}
	}

	private File createListFile() throws IOException {
		File listF = File.createTempFile("ma_jmbb_restore_", ".txt");
		BufferedWriter out = new BufferedWriter(new FileWriter(listF));
		try {
			for(DBFile i: grp.getFiles()) {
				out.write(i.getCPIOPath());
				out.newLine();
			}
		} finally {
			out.close();
		}
		return listF;
	}

	private void restoreFromListFile(File list) throws Exception {
		NativeCPIOProcess cpio =
			new NativeCPIOProcess(dst, NativeCPIOMode.RESTORE);
		cpio.setRestorePatternFile(list);
		cpio.open();

		try {
			restoreFromCPIO(cpio);
		} finally {
			cpio.close();
		}

		cpio.throwPossibleFailure();
	}

	private void restoreFromCPIO(NativeCPIOProcess cpio) throws Exception {
		Process cpioP = cpio.getUnderlyingProcess();

		MessageDigest md = db.header.newMessageDigest();
		InputStream in = createInputStream();
		try {
			OutputStream out = cpioP.getOutputStream();
			try {
				StreamUtility.copy(in, md, out);
			} finally {
				out.close();
			}
		} finally {
			in.close();
		}

		byte[] checksum = md.digest();
		byte[] dbChecksum = grp.getBlock().checksum;
		if(dbChecksum != null && !Arrays.equals(checksum, dbChecksum)) {
			throw new MBBFailureException(
				"Checksum mismatch. CPIO Data possibly " +
				"corrupted."
			);
		}
		// TODO MIGHT WANT TO TEST CHECKSUM ON FILE LEVEL
	}

	// analogous to BCCpioProcessor.createOutputStream(Path)
	private InputStream createInputStream() throws Exception {
		return new XZInputStream(
			Security.newAESInputFilter(
				db.passwords.get(grp.getBlock().passwordId
								).password,
				Files.newInputStream(grp.getBlock().getFile())
			)
		);
	}

}
