package ma.jmbb;

import java.io.*;
import java.nio.file.Files;
import java.util.zip.GZIPInputStream;

class RCPIOMetaBlockExtractor extends RCPIOAbstractMetaExtractor {

	private final DBBlock metaBlock;

	RCPIOMetaBlockExtractor(long id, RDB db, PrintfIO o)
						throws MBBFailureException {
		super(db, o);
		DBBlock metaBlockInterm = null;
		for(DBBlock i: db.blocks) {
			if(i.getId() == id) {
				metaBlockInterm = i;
				break;
			}
		}
		if(metaBlockInterm == null) {
			throw new MBBFailureException(String.format(
				"Unable to find file for meta block x%x.", id));
		} else {
			metaBlock = metaBlockInterm;
		}
	}

	@Override
	String getPassword() {
		return db.passwords.get(metaBlock.passwordId).password;
	}

	@Override
	protected InputStream openSrcFile() throws IOException {
		return Files.newInputStream(metaBlock.getFile());
	}

	RDB readDatabaseFromMetaBlock() throws MBBFailureException {
		try {
			return readDatabaseFromMetaBlockInner();
		} catch(MBBFailureException ex) {
			throw ex;
		} catch(Exception ex) {
			throw new MBBFailureException(ex);
		}
	}

	private RDB readDatabaseFromMetaBlockInner() throws Exception {
		RDB ret = new RDB(db.loc, o);
		NativeCPIOProcess cpio = new NativeCPIOProcess(null,
					NativeCPIOMode.RESTORE_META_BLOCK);
		cpio.open();
		try {
			Process cpioP = cpio.getUnderlyingProcess();
			RCPIOMetaBlockReader r = new RCPIOMetaBlockReader(
						cpioP.getInputStream(), o,
						metaBlock.getFile(), ret);
			r.start();
			try(InputStream in = createInputStream()) {
				writeDecryptedToCPIO(in, cpioP);
				r.join();
				r.throwPossibleFailure();
			}
		} finally {
			cpio.close();
		}

		cpio.throwPossibleFailure();
		return ret;
	}

}
