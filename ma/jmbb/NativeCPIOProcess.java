package ma.jmbb;

import java.io.*;

import ma.tools2.util.NotImplementedException;

class NativeCPIOProcess {

	private final File workdir;
	private final NativeCPIOMode mode;

	private File restorePatternFile;

	private Process cpio;
	private StderrCacher stderr;

	NativeCPIOProcess(File workDir, NativeCPIOMode mode) {
		super();
		this.workdir       = workDir;
		this.mode          = mode;
		cpio               = null;
		stderr             = null;
		restorePatternFile = null;
	}

	void setRestorePatternFile(File f) {
		restorePatternFile = f;
	}

	void open() throws IOException, MBBFailureException {
		cpio = openCpioConnection();
		try {
			stderr = startStderrCacher(cpio);
		} catch(Exception ex) {
			closeCPIO();
			throw ex;
		}
	}

	private StderrCacher startStderrCacher(Process cpio) {
		StderrCacher stderrCacher = new StderrCacher(cpio);
		stderrCacher.setName(Thread.currentThread().getName() +
								"StderrCacher");
		stderrCacher.start();
		return stderrCacher;
	}

	private Process openCpioConnection() throws IOException {
		// Backup creation
		// ---------------
		// Tar adds directories recursively => can not be used. Instead,
		// cpio ("Portable ASCII Format" from SUSv2) was chosen. PAX
		// would have been better but is unfortunately not widely
		// supported yet. This also means that we are limited to files
		// of max. 8 GiB (-1 byte). In the future, the new pax format
		// should be preferred.

		String[] cmdline = getCmdline();

		ProcessBuilder cpioB = new ProcessBuilder(cmdline);
		if(mode != NativeCPIOMode.RESTORE_META) {
			cpioB.directory(workdir);
		}

		return cpioB.start();
	}

	private String[] getCmdline() {
		switch(mode) {
		case CREATE:
			return new String[] { "cpio", "-o", "-c", "--null",
								"--quiet", };
		case RESTORE:
			if(restorePatternFile == null) {
				throw new RuntimeException(
					"a restore pattern file needs to be " +
					"associated before calling open(). " +
					"This is possibly a program bug."
				);
			}
			// -c makes trouble on windows systems
			return new String[] {
				"cpio", "-i", /* "-c", */ "--quiet",
				"--make-directories",
				"--preserve-modification-time",
				"--no-absolute-filenames", "-E",
				restorePatternFile.getAbsolutePath()
			};
		case RESTORE_META:
			return new String[] {
				"cpio", "-i", /* "-c", */ "--quiet",
				"--to-stdout", "meta.xml"
			};
		case RESTORE_META_BLOCK:
			// TODO z: Not optimal because it may fail in case
			//         the meta block contains another file called
			//         db.xml.gz which is not the original meta
			//         file?
			return new String [] {
				"cpio", "-i", /* "-c", */ "--quiet",
				"--to-stdout", "*/db.xml.gz"
			};
		default:
			throw new NotImplementedException(mode + " N_IMPL");
		}
	}

	Process getUnderlyingProcess() {
		return cpio;
	}

	void close() throws MBBFailureException {
		try {
			// Do not leak file descriptors.
			cpio.getOutputStream().close();
			cpio.getInputStream().close();
			stderr.join();
		} catch(Exception ex) {
			throw new MBBFailureException(ex);
		} finally {
			closeCPIO();
		}
	}

	private void closeCPIO() throws MBBFailureException {
		try {
			cpio.waitFor();
		} catch(Exception ex) {
			cpio.destroy();
			throw new MBBFailureException(ex);
		}
	}

	void throwPossibleFailure() throws MBBFailureException {
		stderr.throwPossibleFailure();

		if(cpio.exitValue() != 0) {
			handleCpioError();
		}
	}

	private void handleCpioError() throws MBBFailureException {
		String eo;
		if(stderr.hasOutput()) {
			eo = "Check stderr output:\n" + stderr.getOutput();
		} else {
			eo = "No stderr output available.";
		}
		throw new MBBFailureException("Cpio returned status " +
						cpio.exitValue() + ". " + eo);
	}

}
