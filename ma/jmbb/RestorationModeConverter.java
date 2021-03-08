package ma.jmbb;

import ma.tools2.args.*;

class RestorationModeConverter implements TypeConverter<RestorationMode> {

	RestorationModeConverter() {
		super();
	}

	public void parse(InputParameter param,
					Parameter<RestorationMode> result)
					throws ParameterFormatException {
		String in = param.getCurrentValue();
		switch(in) {
		case "consistent":
			result.setValue(RestorationMode.RESTORE_CONSISTENT);
			break;
		case "new":
			result.setValue(
				RestorationMode.RESTORE_AS_NEW_AS_POSSIBLE);
			break;
		case "list":
			result.setValue(RestorationMode.LIST_VERSIONS_ONLY);
			break;
		default:
			throw new ParameterFormatException("Unknown " +
				"restoration mode: \"" + in + "\".", in);
		}
	}

	public String getUsagePattern() {
		return "new|list";
	}

}
