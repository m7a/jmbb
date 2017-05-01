package ma.tools2.args.conv;

import ma.tools2.args.*;

public class ModeConverter implements TypeConverter<Character> {

	private final char[] modes;

	public ModeConverter(char...modes) {
		super();
		this.modes = modes;
	}

	public void parse(InputParameter param, Parameter<Character> into)
					throws ParameterFormatException {
		final char inMod = param.getCurrentValue().charAt(0);
		for(int i = 0; i < modes.length; i++) {
			if(inMod == modes[i]) {
				into.setValue(inMod);
				return;
			}
		}
		throw new ParameterFormatException(
			"Der angegebene Parameter muss aus den Modi " +
			getUsagePattern() + " gewählt sein.",
			String.valueOf(inMod)
		);
	}

	public String getUsagePattern() {
		StringBuffer ret = new StringBuffer();
		for(int i = 0; i < modes.length; i++) {
			ret.append(modes[i]);
			if(i != modes.length - 1) {
				ret.append('|');
			}
		}
		return ret.toString();
	}

}
