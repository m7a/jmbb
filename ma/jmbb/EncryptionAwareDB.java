package ma.jmbb;

import java.nio.file.Path;
import java.nio.file.Files;

import java.io.IOException;

/**
 * Used for restoring and creating mirrors. As restoration can always happen
 * from a mirror, RDB is derived from this class.
 *
 * @see ma.jmbb.RDB
 */
class EncryptionAwareDB extends DB {

	private static final String ENCRYPTED_SUFFIX = ".aes";
	private static final String ENCRYPTED_MIRROR_DIR = "cnt";
	private static final String ENCRYPTED_FN = DB.DEFAULT_DB_FILENAME +
							ENCRYPTED_SUFFIX;

	EncryptionAwareDB(Path root, PrintfIO o) throws MBBFailureException {
		super(root, o);
	}

	boolean initFromLocAndReportSuccess() throws MBBFailureException {
		Path decrypted = getDBFile();
		Path encrypted = getEncryptedDBFile();
		if(Files.exists(decrypted)) {
			try {
				initFromLoc(decrypted);
			} catch(IOException ex) {
				throw new MBBFailureException(ex);
			}
			return true;
		} else if(Files.exists(encrypted)) {
			initFromEncryptedLoc(encrypted);
			return true;
		} else {
			return false;
		}
	}

	private void initFromEncryptedLoc(Path dbf) throws MBBFailureException {
		try {
			String password = o.readLn("Enter password for %s",
								dbf.toString());
			initFromEncryptedLoc(dbf, password);
		} catch(IOException ex) {
			throw new MBBFailureException(ex);
		}
	}

	void initFromEncryptedLoc(Path dbf, String password) throws IOException,
							MBBFailureException {
		DBReader r = new DBReader(this, dbf, o);
		r.readDatabase(Security.newAESInputFilter(password,
						Files.newInputStream(dbf)));
	}

	Path getEncryptedDBFile() {
		return loc.resolve(ENCRYPTED_MIRROR_DIR).resolve(ENCRYPTED_FN);
	}

}
