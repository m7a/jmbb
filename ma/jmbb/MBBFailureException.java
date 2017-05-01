package ma.jmbb;

public class MBBFailureException extends Exception {

	MBBFailureException() {
		super();
	}

	MBBFailureException(Throwable parent) {
		super(parent);
	}

	MBBFailureException(String msg) {
		super(msg);
	}

	MBBFailureException(String msg, Throwable parent) {
		super(msg, parent);
	}

}
