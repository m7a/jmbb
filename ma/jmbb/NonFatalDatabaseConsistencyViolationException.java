package ma.jmbb;

public class NonFatalDatabaseConsistencyViolationException
						extends MBBFailureException {

	NonFatalDatabaseConsistencyViolationException(String msg) {
		super(msg);
	}

}
