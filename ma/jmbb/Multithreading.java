package ma.jmbb;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

class Multithreading {

	static int determineThreadCount() throws MBBFailureException {
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

	static void awaitPoolTermination(ExecutorService pool)
						throws MBBFailureException {
		try {
			while(!pool.isTerminated()) {
				pool.awaitTermination(300, TimeUnit.SECONDS);
			}
		} catch(InterruptedException ex) {
			throw new MBBFailureException(ex);
		}
	}

}
