package ma.tools2.args;

public class ArgumentListParsingException extends Exception {

	ArgumentListParsingException() {
		super();
	}

	ArgumentListParsingException(String msg) {
		super(msg);
	}

	ArgumentListParsingException(Exception cause) {
		super("Fehler beim Verarbeiten der Parameterliste.", cause);
	}

}
