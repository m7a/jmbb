package ma.tools2.args;

class ArgumentParsingException extends Exception {

	ArgumentParsingException(String msg, String part) {
		super(String.format(
			"%s. Der Fehler trat an dieser Stelle der " + 
			"Parameterliste auf: \"%s\"", msg, part
		));
	}

	ArgumentParsingException(String msg, Exception cause) {
		super(msg, cause);
	}

}
