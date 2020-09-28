package ma.jmbb;

import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import org.tukaani.xz.XZInputStream;

/**
 * Represents a block (file) as used for integrity checking.
 * This is a different attribute set compared to DBBlock, although some of
 * the fields are in common.
 *
 * Implements Runnable to serve as a task.
 */
class IRBlock implements Runnable {

	/** Always set */
	final long id;

	/**
	 * size() == 0 means only in DB
	 * size() == 1 is normal
	 * size() &gt; 2 is a hint towards spurious/duplicate blocks.
	 */
	private final List<Path> files;

	/** if null, means this block is not in DB */
	private String        password;
	private boolean       isActive;
	private MessageDigest md;
	private byte[]        expectedDBChecksum;

	private boolean[]     matchingFiles;
	private IRStatus      status;

	IRBlock(long id, Path file) {
		this.id = id;
		status = IRStatus.UNKNOWN;
		files = new ArrayList<Path>();
		if(file != null) {
			files.add(file);
		}
	}

	/** Create from database only */
	IRBlock(DBBlock block) {
		this.id = block.getId();
		files = new ArrayList<Path>();
		status = IRStatus.absent(!block.isObsoletedInTheFirstPlace());
	}

	void addFile(Path file) {
		files.add(file);
	}

	void assignMetadataFromDB(DB db) throws MBBFailureException {
		// Assign block from database if possible slow linear search...
		DBBlock block = null;
		for(DBBlock cdbb: db.blocks) {
			if(cdbb.getId() == id) {
				block = cdbb;
				break;
			}
		}
		if(block == null) {
			status = IRStatus.NOT_IN_DATABASE;
		} else {
			isActive = !block.isObsoletedInTheFirstPlace();
			if(files.size() != 0) {
				password = db.passwords.get(block.passwordId).
								password;
				expectedDBChecksum = block.checksum;
				md = db.header.newMessageDigest();
			}
		}
	}

	void printMatchDetails(PrintfIO o) {
		for(int i = 0; i < matchingFiles.length; i++) {
			String match = matchingFiles[i]? "good": "BAD  ";
			o.printf("                          %s %s\n", match,
								files.get(i));
		}
	}

	boolean isProcessingRequired() {
		return status == IRStatus.UNKNOWN;
	}

	IRStatus getStatus() {
		return status;
	}

	@Override
	public void run() {
		boolean allMatch = true;
		matchingFiles = new boolean[files.size()];

		for(int i = 0; i < matchingFiles.length; i++) {
			try(InputStream is = new XZInputStream(Security.
					newAESInputFilter(password,
					Files.newInputStream(files.get(i))))) {
				StreamUtility.computeDigestOnly(is, md);
				byte[] checksum = md.digest();
				matchingFiles[i] = Arrays.equals(checksum,
							expectedDBChecksum);
				allMatch = (allMatch && matchingFiles[i]);
			} catch(MBBFailureException ex) {
				matchingFiles[i] = false;
				allMatch = false;
			} catch(IOException ex) {
				matchingFiles[i] = false;
				allMatch = false;
			}
		}

		if(matchingFiles.length == 0) {
			status = IRStatus.absent(isActive);
		} else if(matchingFiles.length == 1) {
			status = IRStatus.simpleComparison(isActive, allMatch);
		} else {
			status = IRStatus.duplicate(isActive, allMatch);
		}
	}

}
