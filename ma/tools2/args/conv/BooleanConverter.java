package ma.tools2.args.conv;

import ma.tools2.args.*;

public class BooleanConverter implements TypeConverter<Boolean> {

	public void parse(InputParameter param, Parameter<Boolean> into)
					throws ParameterFormatException {
		int nP = param.getPos() - 1;
		if(param.inRange(nP) && param.getArg(nP).equals("true")) {
			into.setValue(true);
		} else {
			String val = param.getCurrentValue();
			if(val.equals("true")) {
				into.setValue(true);
			} else if(val.equals("false")) {
				into.setValue(false);
			} else {
				throw new ParameterFormatException(
					"Für Boolean-Parameter sind nur die " +
					"Werte \"true\" und \"false\" " + 
					"zulässig.", val
				);
			}
		}
	}

	public String getUsagePattern() {
		return "true|false";
	}

}
