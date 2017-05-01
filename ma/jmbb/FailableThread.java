package ma.jmbb;

abstract class FailableThread extends Thread {

	private MBBFailureException failure;

	FailableThread() {
		super();
		failure = null;
	}

	public void run() {
		try {
			runFailable();
		} catch(MBBFailureException ex) {
			failure = ex;
		} catch(Exception ex) {
			failure = new MBBFailureException(ex);
		}
	}

	public abstract void runFailable() throws Exception;

	void throwPossibleFailure() throws MBBFailureException {
		if(failure != null) {
			throw failure;
		}
	}

}
