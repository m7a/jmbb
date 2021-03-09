package ma.jmbb;

import java.io.*;

class RCPIOMetaExtractor extends RCPIOAbstractMetaExtractor
							implements Runnable {

	private final File src;
	private DBBlock blk;

	RCPIOMetaExtractor(File f, RDB db, PrintfIO o) {
		super(db, o);
		src = f;
		blk = null;
	}

	/** @return null if there were errors (already printed) */
	DBBlock getBlock() {
		return blk;
	}

	@Override
	protected InputStream openSrcFile() throws IOException {
		return new FileInputStream(src);
	}

	@Override
	public void run() {
		try {
			runFailable();
		} catch(MBBFailureException ex) {
			o.edprintf(ex, "Unable to parse file \"%s\" into DB.\n",
							src.getAbsolutePath());
		}
	}

	private void runFailable() throws MBBFailureException {
		try {
			createBlockObject();
		} catch(MBBFailureException ex) {
			throw ex;
		} catch(Exception ex) {
			throw new MBBFailureException(ex);
		}
	}

	private void createBlockObject() throws Exception {
		NativeCPIOProcess cpio = new NativeCPIOProcess(null,
						NativeCPIOMode.RESTORE_META);
		cpio.open();
		try {
			processCPIO(cpio);
		} finally {
			cpio.close();
		}

		cpio.throwPossibleFailure();
	}

	private void processCPIO(NativeCPIOProcess cpio) throws Exception {
		Process cpioP = cpio.getUnderlyingProcess();
		RCPIOMetaReader r = createCPIOReader(cpioP);

		InputStream in = createInputStream();
		try {
			writeDecryptedToCPIO(in, cpioP);
			r.join();
			r.throwPossibleFailure();
			blk = r.getResult();
		} finally {
			in.close();
		}
	}

	private RCPIOMetaReader createCPIOReader(Process cpioP) {
		RCPIOMetaReader reader = new RCPIOMetaReader(
				cpioP.getInputStream(), o, src.toPath());
		reader.start();	
		return reader;
	}

}
