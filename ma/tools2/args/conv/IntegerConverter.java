package ma.tools2.args.conv;

import ma.tools2.args.*;

public class IntegerConverter implements TypeConverter<Integer> {

	private boolean allowNegative;

	public IntegerConverter() {
		super();
		allowNegative = false;
	}

	public IntegerConverter(boolean allowNegative) {
		super();
		this.allowNegative = allowNegative;
	}

	public void parse(InputParameter param, Parameter<Integer> into)
					throws ParameterFormatException {
		int in;
		try {
			in = Integer.parseInt(param.getCurrentValue());
		} catch(NumberFormatException ex) {
			throw new ParameterFormatException(
				"Die Eingabe repräsentiert keine gültige " +
				"32-Bit Ganzzahl.", ex
			);
		}
		if(in < 0 && !allowNegative) {
			throw new ParameterFormatException(
				"Es sind nur positive Eingabezahlen erlaubt.",
				param.getCurrentValue()
			);
		}
		into.setValue(in);
	}

	public String getUsagePattern() {
		if(allowNegative) {
			return "int";
		} else {
			return "uint";
		}
	}

}
