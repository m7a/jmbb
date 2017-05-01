package ma.jmbb;

import java.io.*;

public class PrintfIO {

	private final BufferedReader in;

	public PrintfIO() throws IOException {
		super();
		in = new BufferedReader(new InputStreamReader(System.in));
	}

	// -- To be implemented by subclasses ----------------------------------

	protected void printf(String fmt, Object... args) {
		System.out.print(String.format(fmt, args));
	}

	/**
	 * Printf to error stream
	 */
	protected void eprintf(String fmt, Object... args) {
		System.err.print(String.format(fmt, args));
	}

	/**
	 * eprintf with appended information about the given Throwable.
	 */
	protected void edprintf(Throwable t, String fmt, Object... args) {
		eprintf(fmt, args);
		t.printStackTrace();
	}

	/**
	 * Read a line of user input prompting with the given format string and
	 *
	 * @throws IOException Only upon failure to read input data.
	 * @throws UserAbortException If the user chooses to quit.
	 */
	protected String readLn(String fmt, Object... args) throws IOException {
		String data = readRawLn(fmt + " [q: quit] ", args).trim();

		if(data.length() == 0 || data.equals("q")) {
			throw new UserAbortException();
		}

		return data;
	}
	
	protected String readRawLn(String fmt, Object... args)
							throws IOException {
		printf(fmt, args);
		String data;
		return in.readLine();
	}

}
