package ma.jmbb;

import ma.tools2.args.*;

class RestorationModeConverter implements TypeConverter<RestorationMode> {

	RestorationModeConverter() {
		super();
	}

	public void parse(InputParameter param,
					Parameter<RestorationMode> result)
					throws ParameterFormatException {
		String in  = param.getCurrentValue();
		if(in.equals("consistent")) {
			result.setValue(RestorationMode.RESTORE_CONSISTENT);
		} else if(in.equals("new")) {
			result.setValue(
				RestorationMode.RESTORE_AS_NEW_AS_POSSIBLE);
		} else if(in.equals("list")) {
			result.setValue(RestorationMode.LIST_VERSIONS_ONLY);
		} else {
			throw new ParameterFormatException(
				"Unknown restoration mode: \"" + in + "\".", in
			);
		}
	}

	public String getUsagePattern() {
		return "consistent|new|list";
	}

}
