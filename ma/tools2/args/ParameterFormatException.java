package ma.tools2.args;

public class ParameterFormatException extends ArgumentParsingException {

	public ParameterFormatException(String msg, String part) {
		super(msg, part);
	}

	public ParameterFormatException(String msg) {
		super(msg, "<unbekannt>");
	}

	public ParameterFormatException(String msg, Exception cause) {
		super(msg, cause);
	}

}
