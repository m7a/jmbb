package ma.jmbb;

import java.io.*;

class RCPIOMetaExtractor extends RCPIOAbstractMetaExtractor {

	private final File src;

	RCPIOMetaExtractor(File f, RDB db, PrintfIO o) {
		super(db, o);
		src = f;
	}

	@Override
	protected InputStream openSrcFile() throws IOException {
		return new FileInputStream(src);
	}

	void run() throws MBBFailureException {
		try {
			DBBlock blk = createBlockObject();
			db.addRestorationBlock(blk);
		} catch(MBBFailureException ex) {
			throw ex;
		} catch(Exception ex) {
			throw new MBBFailureException(ex);
		}
	}

	private DBBlock createBlockObject() throws Exception {
		NativeCPIOProcess cpio = new NativeCPIOProcess(null,
						NativeCPIOMode.RESTORE_META);
		DBBlock ret;
		cpio.open();
		try {
			ret = processCPIO(cpio);
		} finally {
			cpio.close();
		}

		cpio.throwPossibleFailure();
		return ret;
	}

	private DBBlock processCPIO(NativeCPIOProcess cpio) throws Exception {
		Process cpioP = cpio.getUnderlyingProcess();
		RCPIOMetaReader r = createCPIOReader(cpioP);

		InputStream in = createInputStream();
		try {
			writeDecryptedToCPIO(in, cpioP);
			r.join();
			r.throwPossibleFailure();
			return r.getResult();
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
