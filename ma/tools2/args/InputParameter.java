package ma.tools2.args;

public class InputParameter {

	static final char   SHORT_PARAMETER    = '-';
	static final String LONG_PARAMETER     = "--";
	private static final int PAD           = LONG_PARAMETER.length();
	static final char   LONG_PARAMETER_SEP = '=';

	private String[] args;

	private String  lName;
	private char    sName;

	private int     pos;
	private boolean processed;

	InputParameter(String[] args) {
		super();
		this.args = args;
		pos       = 0;
	}

	void process(ArgumentList list) throws ArgumentListParsingException {
		while(pos < args.length) {
			processed = false;
			int len   = args[pos].length();
			if(len >= 2) {
				if(args[pos].startsWith(LONG_PARAMETER)) {
					processLong(args[pos], len, list);
				} else {
					processShort(args[pos], len, list);
				}
			} else {
				throw new ArgumentListParsingException(
					"Mit Parametern kürzer als zwei " +
					"Zeichen kann das Programm nichts " +
					"anfangen."
				);
			}
		}
	}

	private void processLong(String param, int len, ArgumentList list)
					throws ArgumentListParsingException {
		final int sep = param.indexOf(LONG_PARAMETER_SEP);
		if(sep == -1) {
			lName = param.substring(PAD);
			args[pos] = "true";
		} else {
			lName = param.substring(PAD, sep);
			args[pos] = param.substring(sep + 1);
		}
		parseList(param, list);
		pos++;
	}

	private void parseList(String param, ArgumentList list)
					throws ArgumentListParsingException {
		processed = false;
		try {
			list.parse(this);
		} catch(ParameterFormatException ex) {
			throw new ArgumentListParsingException(ex);
		}
		if(!processed) {
			throw new ArgumentListParsingException(
				"Argument \"" + param + "\" wurde nicht " + 
				"verarbeitet."
			);
		}
	}

	private void processShort(String param, int len, ArgumentList list)
					throws ArgumentListParsingException {
		lName = null;
		args[pos] = "true";
		int subPos = 1;
		final int prePos = pos;
		while(subPos < len) {
			sName = param.charAt(subPos++);
			if(subPos == len) {
				pos++;
			}
			parseList(param, list);
			if(pos > prePos && subPos < len) {
				throw new ArgumentListParsingException(
					"Der Parameter \"" + sName + "\" " +
					"erwartet einen Wert, steht aber " +
					"so, dass es ein binärer Parameter " +
					"sein müsste."
				);
			}
		}
		pos++;
	}

	String getLongName() {
		return lName;
	}

	char getShortName() {
		return sName;
	}

	void setProcessed() {
		processed = true;
	}

	boolean isProcessed() {
		return processed;
	}

	public String getCurrentValue() throws ParameterFormatException {
		if(!hasCurrentValue()) {
			throw new ParameterFormatException(
				"Bei einem Argument, welches einen Wert " + 
				"benötigt, wurde keiner angegeben."
			);
		}
		return args[pos];
	}

	public boolean hasCurrentValue() {
		return inRange(pos);
	}

	public String getArg(int pos) {
		return args[pos];
	}

	public int getPos() {
		return pos;
	}

	public boolean inRange(int pos) {
		return pos >= 0 && pos < args.length;
	}

	/**
	 * Hinweis: Vorsicht bei direkter Positionsmodifikation.
	 */
	public void setPos(int pos) {
		this.pos = pos;
	}

}
