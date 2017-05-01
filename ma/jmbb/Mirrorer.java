package ma.jmbb;

import java.io.File;
import java.io.IOException;

import java.nio.file.Path;
import java.nio.file.Files;

class Mirrorer {
	
	private static final String NO_MATCH_FOUND =
		"WARNING: Could not find a suitable password for decrypting " +
		"the mirror database. Causes might be invalid timestamps on " +
		"the mirror's filesystem or the mirror's source database " +
		"might be another database than the database you want to " +
		"synchronize against it. Either way, JMBB now tries to use " +
		"the most recent password from the source database. If you " +
		"are trying to synchronize against the wrong database this " +
		"will most likely fail.\n";

	private final PrintfIO o;
	private final Path db;
	private final Path dst;

	Mirrorer(PrintfIO o, File db, File dst) {
		super();
		this.o   = o;
		this.db  = db.toPath();
		this.dst = dst.toPath();
	}

	void run() throws MBBFailureException {
		try {
			DB src = loadSourceDB();
			EncryptionAwareDB mir = loadMirroredDB(src);

			sync(src, mir);
			saveEncrypted(src, mir);
		} catch(MBBFailureException ex) {
			throw ex;
		} catch(Exception ex) {
			throw new MBBFailureException(ex);
		}
	}

	private DB loadSourceDB() throws MBBFailureException {
		DB src = new DB(db, o);
		src.initFromLoc();
		return src;
	}

	private EncryptionAwareDB loadMirroredDB(DB src)
				throws MBBFailureException, IOException {
		EncryptionAwareDB ret = new EncryptionAwareDB(dst, o);
		Path dbf = ret.getEncryptedDBFile();
		if(Files.exists(dbf)) {
			String password = getNewestPossiblePassword(src, dbf);
			if(password == null) {
				o.eprintf(NO_MATCH_FOUND);
				password = src.passwords.getCurrentValue();
			}
			ret.initFromEncryptedLoc(dbf, password);
		}
		return ret;
	}

	private static String getNewestPossiblePassword(DB src, Path dbf)
							throws IOException {
		long dbmod = Files.getLastModifiedTime(dbf).toMillis();
		DBPassword bestMatch = null;
		for(DBPassword i: src.passwords) {
			if(i.timestamp < dbmod && (bestMatch == null ||
					bestMatch.timestamp < i.timestamp)) {
				bestMatch = i;
			}
		}
		if(bestMatch == null) {
			return null;
		} else {
			return bestMatch.password;
		}
	}

	private void sync(DB source, DB mirror) throws Exception {
		new MDBSynchronizer(source, mirror, o).run();
	}

	private static void saveEncrypted(DB source, EncryptionAwareDB mirror)
							throws Exception {
		source.save(Security.newAESOutputFilter(
			source.passwords.getCurrentValue(),
			Files.newOutputStream(mirror.getEncryptedDBFile())
		));
	}

}
