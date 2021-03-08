package ma.jmbb;

import ma.tools2.args.*;

/**
 * @since JMBB 1.0.7
 */
class MetaBlockConverter implements TypeConverter<Long> {

	static final long USE_META_BLOCK_NO   = -1;
	static final long USE_META_BLOCK_AUTO = -2; // idea/planned

	MetaBlockConverter() {
		super();
	}

	public void parse(InputParameter param, Parameter<Long> into)
					throws ParameterFormatException {
		String strval = param.getCurrentValue();
		switch(strval) {
		case "no":
			into.setValue(USE_META_BLOCK_NO);
			break;
		case "auto":
			into.setValue(USE_META_BLOCK_AUTO);
			break;
		default:
			try {
				into.setValue(DBBlock.parseID(strval));
			} catch(NumberFormatException ex) {
				throw new ParameterFormatException(
					"Unable to parse `" + strval +
					"` as JMBB block ID.", ex
				);
			}
		}
	}

	public String getUsagePattern() {
		return "no|auto|x...";
	}

}
