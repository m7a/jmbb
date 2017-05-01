package ma.tools2.args.conv;

import ma.tools2.args.*;

/**
 * Ergibt nur Sinn, wenn als letzes Argument gesetzt.
 */
public class GreedyStringConverter implements TypeConverter<String> {

	public void parse(InputParameter param, Parameter<String> into)
					throws ParameterFormatException {
		StringBuffer strOut = new StringBuffer();
		int pos = param.getPos();
		while(param.inRange(pos)) {
			param.setPos(pos);
			strOut.append(param.getCurrentValue());
			pos++;
			if(param.inRange(pos)) {
				strOut.append(' ');
			}
		}
		into.setValue(strOut.toString());
	}

	public String getUsagePattern() {
		return "string ...";
	}

}
