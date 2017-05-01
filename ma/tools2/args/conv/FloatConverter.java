package ma.tools2.args.conv;

import ma.tools2.args.*;

public class FloatConverter implements TypeConverter<Float> {

	public void parse(InputParameter param, Parameter<Float> into)
					throws ParameterFormatException {
		float in;
		try {
			in = Float.parseFloat(param.getCurrentValue());
		} catch(NumberFormatException ex) {
			throw new ParameterFormatException(
				"Die Eingabe ist keine gültige " + 
				"Fließkommazahl.", ex
			);
		}
		into.setValue(in);
	}

	public String getUsagePattern() {
		return "float";
	}

}
