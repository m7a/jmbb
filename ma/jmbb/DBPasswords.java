package ma.jmbb;

import java.io.IOException;

import java.util.ArrayList;

class DBPasswords extends ArrayList<DBPassword> {

	private DBPassword current; // This is never written to a file.

	DBPasswords() {
		super();
		current = null;
	}

	void write(XMLWriter out) throws IOException {
		out.tol("<passwords>");
		String currentReal = writeDBCurrent(out);
		writeDBInactiveAndObsolete(out, currentReal);
		out.tcl("</passwords>");
	}

	private String writeDBCurrent(XMLWriter out) throws IOException {
		for(DBPassword i: this) {
			if(i.isCurrent()) {
				i.write(null, out);
				return i.password;
			}
		}
		return current.password;
	}

	private void writeDBInactiveAndObsolete(XMLWriter out,
					String realCurrent) throws IOException {
		for(DBPassword i: this) {
			if(i.isCurrent()) {
				if(!i.password.equals(realCurrent)) {
					passwordAssertionFailed();
				}
			} else {
				i.write(realCurrent, out);
			}
		}
	}

	private static void passwordAssertionFailed() throws IOException {
		throw new IOException(new MBBFailureException("Multiple " +
				"current passwords detected. Aborting."));
	}

	long getCurrentId() {	
		return current.id;
	}

	public boolean add(DBPassword password) {
		if(password.isCurrent()) {
			try {
				setCurrent(password);
			} catch(MBBFailureException ex) {
				throw new RuntimeException(ex);
			}
		}
		return super.add(password);
	}

	private void setCurrent(DBPassword password)
						throws MBBFailureException {
		if(current == null) {
			current = password;
		} else {
			throw new MBBFailureException(
				"Can not add another current password as " +
				"long there exists a current password. This " +
				"might be an error in the implementation."
			);
		}
	}

	String getCurrentValue() {
		return current.password;
	}

	boolean hasCurrent() {
		return current != null;
	}

	void performAutomaticPasswordObsoletion(DBBlocks blk, PrintfIO o) {
		for(DBPassword i: this) {
			if(i.getState() == DBPassword.State.INACTIVE) {
				performPossibleObsoletion(i, blk, o);
			}
		}
	}

	private static void performPossibleObsoletion(DBPassword p,
						DBBlocks blk, PrintfIO o) {
		if(!isUsed(p.id, blk)) {
			p.setState(DBPassword.State.DEPRECATED);
			o.printf("DELTAPASS,ID %d,DEPRECATED\n", p.id);
		}
	}

	// Linear search... not too efficient but OK because only few passwords
	// will ever be inactive.
	private static boolean isUsed(long id, DBBlocks blk) {
		for(DBBlock i: blk) {
			if(i.activelyUsesPassword(id)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * A password read via this method will never be written to disk.
	 */
	void readInteractively(PrintfIO io)
				throws IOException, MBBFailureException {
		String raw = io.readLn("Enter password interactively");
		io.printf("ACK\n");
		setCurrent(new DBPassword(raw));
	}

	void printStats(PrintfIO o) {
		// Currently there are no useful statistics for passwords.
		//  => no output
	}

	void disableCurrentPassword() {
		for(DBPassword i: this) {
			if(i.isCurrent()) {
				i.setState(DBPassword.State.INACTIVE);
			}
		}
		current = null;
	}
	
	long getGreatestId() {
		long greatest = 0;
		for(DBPassword i: this) {
			if(i.id > greatest) {
				greatest = i.id;
			}
		}
		return greatest;
	}

	void print(PrintfIO o) {
		for(DBPassword i: this) {
			o.printf("ID %4d STATUS %s VALUE \"%s\"\n", i.id,
						i.getStateString(), i.password);
		}
	}

	DBPassword get(long id) {
		for(DBPassword i: this) {
			if(i.id == id) {
				return i;
			}
		}
		return null;
	}

}
