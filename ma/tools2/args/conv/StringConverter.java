package ma.tools2.args.conv;

import ma.tools2.args.*;

public class StringConverter implements TypeConverter<String> {

	public void parse(InputParameter param, Parameter<String> into)
					throws ParameterFormatException {
		into.setValue(param.getCurrentValue());
	}

	public String getUsagePattern() {
		return "string";
	}

}
