package ma.jmbb;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.List;

class BCBlockCreator {

	private static final int WAIT_BETWEEN_SLOT_SEARCHES = 128;

	private final BCDB db;
	private final PrintfIO o;
	private final Path[] tmp;

	private BCNativeBlockCreatorProcess[] proc;

	private boolean removeMetaFilesFromObsoletionList;

	BCBlockCreator(PrintfIO o, final BCDB db) throws MBBFailureException {
		super();
		this.o  = o;
		this.db = db;

		int procCount = determineThreadCount();

		proc = new BCNativeBlockCreatorProcess[procCount];
		tmp = new Path[proc.length];
		try {
			for(int i = 0; i < proc.length; i++) {
				proc[i] = null;
				tmp[i] = Files.createTempDirectory("ma_jmbb_");
			}
		} catch(IOException ex) {
			throw new MBBFailureException(ex);
		}

		removeMetaFilesFromObsoletionList = false;
	}

	private int determineThreadCount() throws MBBFailureException {
		String envProc = System.getenv("JMBB_THREADS");
		if(envProc == null) {
			return Runtime.getRuntime().availableProcessors();
		} else {
			try {
				return Integer.parseInt(envProc);
			} catch(NumberFormatException ex) {
				throw new MBBFailureException(
					"Invalid format of JMBB_THREADS " +
					"environment variable. Integer " +
					"required.", ex
				);
			}
		}
	}

	void scheduleCreation(List<BCChangedFile> cnt, long inwardSize)
						throws MBBFailureException {
		int slot;
		slot = acquireSlot();
		proc[slot] = new BCNativeBlockCreatorProcess(db, cnt,
							inwardSize, tmp[slot]);
		proc[slot].setName("Native Block Creator " + slot + " /");
		proc[slot].start();
	}

	private int acquireSlot() throws MBBFailureException {
		while(true) {
			for(int i = 0; i < proc.length; i++) {
				if(proc[i] == null) {
					return i;
				} else if(!proc[i].isAlive()) {
					evaluateReturnValue(i);
					return i;
				}
			}
			try {
				Thread.sleep(WAIT_BETWEEN_SLOT_SEARCHES);
			} catch(InterruptedException ex) {
				throw new MBBFailureException("Unexpected " +
							"interrupt", ex);
			}
		}
	}

	private void evaluateReturnValue(int slot) throws MBBFailureException {
		DBBlock newBlk = proc[slot].getResult();
		newBlk.printCreationNotice(o);
		db.blocks.add(newBlk);
	}

	void finish() throws MBBFailureException {
		try {
			waitForProcessesToFinish();
		} catch(InterruptedException e) {
			throw new MBBFailureException("Unexpeced interrupt", e);
		} finally {
			try {
				deleteTemporaryDirectories();
			} catch(IOException ex) {
				throw new MBBFailureException(ex);
			}
		}

		evaluateReturnValues();

		// Needs to be done in the BCBlockCreator thread
		// otherwise BCBlockCreator might just perform an obsoletion
		// while this is invoked in BackupCreator.
		if(removeMetaFilesFromObsoletionList) {
			db.removeMetaFilesFromObsoletionList();
		}

		db.markRemainingNonexistentFilesObsolete();
		db.passwords.performAutomaticPasswordObsoletion(db.blocks, o);
	}

	private void waitForProcessesToFinish() throws InterruptedException {
		for(int i = 0; i < proc.length; i++)
			if(proc[i] != null)
				proc[i].join();
	}

	private void deleteTemporaryDirectories() throws IOException {
		for(int i = 0; i < proc.length; i++)
			Files.delete(tmp[i]);
	}

	private void evaluateReturnValues() throws MBBFailureException {
		for(int i = 0; i < proc.length; i++)
			if(proc[i] != null)
				evaluateReturnValue(i);
	}

	void setRemoveMetaFilesFromObsoletionList() {
		removeMetaFilesFromObsoletionList = true;
	}

}
