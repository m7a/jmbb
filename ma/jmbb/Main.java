package ma.jmbb;

public class Main {

	private Args args;
	private JMBBInterface conn;

	private Main(Args args) throws MBBFailureException {
		super();
		this.args = args;
		try {
			this.conn = new JMBBInterface(new PrintfIO());
		} catch(Exception ex) {
			throw new MBBFailureException(ex);
		}
	}

	private void act() throws MBBFailureException {
		ProgramMode mode = args.determineMode();
		switch(mode) {
		case BACKUP_UPDATE_DB:
			conn.backupUpdateDB(args.dstDatabase.getValue(),
						args.srcUpdate.getValue());
			break;
		case MIRROR:
			conn.mirror(args.srcDatabase.getValue(),
						args.dstMirror.getValue());
			break;
		case RESTORE:
			conn.restore(
				args.srcRestore.getValue(),
				args.dstRestore.getValue(),
				args.pattern.getValue(),
				args.restorationMode.getValue(),
				args.restoreVersion.getValue()
			);
			break;
		//case DROP:
		//	conn.drop(args.dropMirror.getValue());
		//	break;
		//case GRAB:
		//	conn.grab(args.dstGrab.getValue());
		//	break;
		case EDIT:
			conn.edit(args.editDatabase.getValue());
			break;
		}
	}

	public static void main(String[] args) {
		Args a = new Args();
		if(a.parseAndReact(args)) {
			// Problem: Exits with 1 on legal invocation $0 --help.
			System.exit(1);
		} else {
			try {
				Main main = new Main(a);
				main.act();
			} catch(UserAbortException ex) {
				System.exit(3);
			} catch(Exception ex) {
				ex.printStackTrace();
				System.exit(1);
			} catch(Throwable t) {
				t.printStackTrace();
				System.exit(2);
			}
		}
	}

}
