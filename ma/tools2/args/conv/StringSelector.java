package ma.tools2.args.conv;

import ma.tools2.args.*;

public class StringSelector implements TypeConverter<String> {

	private final String[] allowedValues;

	public StringSelector(String...allowedValues) {
		super();
		this.allowedValues = allowedValues;
	}

	public void parse(InputParameter param, Parameter<String> into)
					throws ParameterFormatException {
		String val = param.getCurrentValue();
		for(int i = 0; i < allowedValues.length; i++) {
			if(allowedValues[i].equals(val)) {
				into.setValue(val);
				return;
			}
		}
		throw new ParameterFormatException(
			"Der angegebene Wert ist nicht aus der Menge " + 
			"gültigen Werte.", val
		);
	}

	public String getUsagePattern() {
		StringBuffer ret = new StringBuffer();
		for(int i = 0; i < allowedValues.length; i++) {
			ret.append(allowedValues[i]);
			if(i != allowedValues.length - 1) {
				ret.append('|');
			}
		}
		return ret.toString();
	}

}
