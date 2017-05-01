package ma.tools2.args;

public class Parameter<T> extends PartialArgs {

	// Einmalig festgelegte Einstellungen
	private final String           lName;
	private final char             sName;
	private final TypeConverter<T> conv;

	// Zur Laufzeit festgelegt.
	private boolean set;
	private T       value;

	public Parameter(String lName, char sName, T defaultValue,
				TypeConverter<T> conv, String description) {
		super(description);
		this.lName = lName;
		this.sName = sName;
		value      = defaultValue;
		this.conv  = conv;
		set        = false;
	}

	void parse(InputParameter param) throws ParameterFormatException {
		if(
			(param.getLongName() == null &&
					param.getShortName() == sName) ||
			param.getLongName() != null &&
					param.getLongName().equals(lName)) {
			param.setProcessed();
			conv.parse(param, this);
		}
	}

	public void setValue(T value) {
		if(!set) {
			set = true;
		}
		this.value = value;
	}

	public void markDefaultValueAsSet() {
		set = true;
	}

	public T getValue() {
		return value;
	}

	public boolean isSet() {
		return set;
	}

	protected void printHelp(int level) {
		printI(level, String.valueOf(InputParameter.SHORT_PARAMETER) +
						sName + "  " + getUsage());
		super.printHelp(level + 1);
	}

	protected String getUsage() {
		return InputParameter.LONG_PARAMETER + lName +
					InputParameter.LONG_PARAMETER_SEP +
							conv.getUsagePattern();
	}

}
