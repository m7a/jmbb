package ma.jmbb;

import java.util.List;

import java.io.IOException;
import java.io.OutputStream;

import java.nio.file.Path;
import java.nio.file.Files;

import static java.nio.charset.StandardCharsets.UTF_8;

class BCNativeBlockCreatorProcess extends Thread {

	private static final String META_XML = "meta.xml";

	private final BCDB db;
	private final List<BCChangedFile> cnt;
	private final long inwardSize;
	private final Path tmpDir;
	private final Path metaFile;

	private DBBlock result;
	private MBBFailureException error;

	BCNativeBlockCreatorProcess(BCDB db, List<BCChangedFile> cnt,
			long inwardSize, Path tmp) throws MBBFailureException {
		super();
		this.db         = db;
		this.cnt        = cnt;
		this.inwardSize = inwardSize;
		tmpDir          = tmp;
		result          = db.createNewBlock();
		metaFile        = tmp.resolve(META_XML);
		error           = null;
	}

	public void run() {
		try {
			performCpioOperations();
			obsoleteFilesInDB();
		} catch(MBBFailureException ex) {
			error = ex;
		} catch(Throwable ex) {
			error = new MBBFailureException(ex);
		}
	}

	private void performCpioOperations() throws MBBFailureException,
					IOException, InterruptedException {
		NativeCPIOProcess cpioN = new NativeCPIOProcess(tmpDir.toFile(),
							NativeCPIOMode.CREATE);
		cpioN.open();
		Process cpio = cpioN.getUnderlyingProcess();

		BCCpioProcessor processor = startCpioProcessor(cpio);

		try {
			OutputStream cpioFiles = cpio.getOutputStream();
			try {
				writeFileList(cpioFiles);
				writeMetaFile(cpioFiles);
			} finally {
				cpioFiles.close();
			}
			try {
				processor.join();
			} finally {
				Files.delete(metaFile);
			}
		} finally {
			cpioN.close();
		}

		cpioN.throwPossibleFailure();
	}

	private BCCpioProcessor startCpioProcessor(Process cpio) {
		BCCpioProcessor processor = new BCCpioProcessor(cpio, result,
									db);
		processor.setName(getName() + "CpioProcessor");
		processor.start();
		return processor;
	}

	private void writeFileList(OutputStream cpioFiles)
				throws MBBFailureException, IOException {
		for(BCChangedFile i: cnt) {
			// Checksums only if not already done upon comparison
			// for previous version. This ensures that not all
			// checksuming is single threaded but only that which
			// is necessary instead.
			i.checksumIfNecessary(db);
			result.addFile(i.change);
			writeFile(cpioFiles, i.osPath);
		}
	}

	private void writeFile(OutputStream cpioFiles, String f)
				throws MBBFailureException, IOException {
		cpioFiles.write(f.getBytes(UTF_8));
		cpioFiles.write(0);
	}

	private void writeMetaFile(OutputStream cpioFiles)
				throws MBBFailureException, IOException {
		final XMLWriter out = new XMLWriter(Files.newOutputStream(
								metaFile));
		try {
			writeMetaData(out);
		} finally {
			out.close();
		}
		writeFile(cpioFiles, META_XML);
	}

	private void writeMetaData(XMLWriter out) throws IOException {
		DB.writeXMLHeader(out, "block");
		result.write(out);
	}

	private void obsoleteFilesInDB() {
		for(BCChangedFile i: cnt)
			possiblyObsolete(i);
	}

	private void possiblyObsolete(BCChangedFile i) {
		if(i.prev != null) {
			assert !i.isContentwiseEqualToPreviousVersion();
			i.prev.obsolete();
		}
	}

	DBBlock getResult() throws MBBFailureException {
		if(error != null)
			throw error;
		return result;
	}

}
