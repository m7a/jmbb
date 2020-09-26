package ma.jmbb;

import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Arrays;

import org.tukaani.xz.XZInputStream;

/**
 * Represents a block (file) as used for integrity checking.
 * This is a different attribute set compared to DBBlock, although some of
 * the fields are in common.
 *
 * Implements Runnable to serve as a task.
 */
class IRBlock implements Runnable {

	static final char YES = ':';
	static final char NO  = 'E';
	static final char ANY = '_';

	/** Always set */
	private final long          id;
	/** If null, means this block is only in DB */
	private final Path          osFile;
	private final byte[]        expectedDBChecksum;
	/** if null, means this block is not in DB */
	private final String        password;
	private final MessageDigest md;

	private char statusKnownInDatabase         = ANY;
	private char statusActive                  = ANY;
	private char statusExistsOnHDD             = ANY; 
	private char statusChecksumMatchesDatabase = ANY;

	IRBlock(Path osFile, long id, DB db) throws MBBFailureException {
		this.osFile = osFile;
		this.id     = id;

		// Assign block from database if possible slow linear search...
		DBBlock block = null;
		for(DBBlock cdbb: db.blocks) {
			if(cdbb.getId() == id) {
				block = cdbb;
				break;
			}
		}
		if(block == null) {
			statusKnownInDatabase = NO;
		} else {
			statusKnownInDatabase = YES;
			if(block.isObsoletedInTheFirstPlace()) {
				statusActive = NO;
			} else {
				statusActive = YES;
			}
		}

		if(osFile == null) {
			statusExistsOnHDD  = NO;
			password           = null;
			expectedDBChecksum = null;
			md                 = null;
		} else {
			statusExistsOnHDD = YES;
			if(block == null) {
				password           = null;
				expectedDBChecksum = null;
				md                 = null;
			} else {
				password           = db.passwords.get(block.
							passwordId).password;
				expectedDBChecksum = block.checksum;
				md                 = db.header.
							newMessageDigest();
			}
		}
	}

	boolean isProcessingRequired() {
		return statusExistsOnHDD == YES && statusKnownInDatabase == YES
					&& statusChecksumMatchesDatabase == ANY;
	}

	@Override
	public void run() {
		try(InputStream is = new XZInputStream(Security.
					newAESInputFilter(password,
					Files.newInputStream(osFile)))) {
			StreamUtility.computeDigestOnly(is, md);
			byte[] checksum = md.digest();
			if(Arrays.equals(checksum, expectedDBChecksum)) {
				statusChecksumMatchesDatabase = YES;
			} else {
				statusChecksumMatchesDatabase = NO;
			}
		} catch(MBBFailureException ex) {
			statusChecksumMatchesDatabase = NO;
		} catch(IOException ex) {
			statusChecksumMatchesDatabase = NO;
		}
	}

	@Override
	public String toString() {
		return String.format(
			"%016x %c%c%c%c%c", id,
			statusKnownInDatabase, statusActive, statusExistsOnHDD,
			statusChecksumMatchesDatabase, getStatusGood()
		);
	}

	private char getStatusGood() {
		return isGood()? YES: NO;
	}

	private boolean isGood() {
		return (statusKnownInDatabase == YES) && (
			((statusActive == YES) && (statusExistsOnHDD == YES) &&
				(statusChecksumMatchesDatabase == YES)) ||
			((statusActive == NO) && (statusExistsOnHDD == NO))
		);
	}

	void addToStats(IRStats s) {
		if(statusKnownInDatabase == YES)
			s.databaseY++;
		else
			s.databaseN++;

		if(statusActive == YES)
			s.activeY++;
		else if(statusActive == NO)
			s.activeN++;
		else
			s.activeAny++;

		if(statusExistsOnHDD == YES)
			s.hddY++;
		else if(statusExistsOnHDD == NO)
			s.hddN++;
		else
			s.hddAny++;

		if(statusChecksumMatchesDatabase == YES)
			s.equalY++;
		else if(statusChecksumMatchesDatabase == NO)
			s.equalN++;
		else
			s.equalAny++;

		if(isGood())
			s.goodY++;
		else
			s.goodN++;
	}

}
