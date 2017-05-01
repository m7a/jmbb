package ma.tools2.args.conv;

import ma.tools2.args.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class DateConverter implements TypeConverter<Date> {

	public static final String SCREENINDEX_DEFAULT = "dd.MM.yyyy HH:mm:ss";

	private final String fmt;
	private SimpleDateFormat sdf;

	public DateConverter(String fmt) {
		super();
		this.fmt = fmt;
		sdf = new SimpleDateFormat(fmt);
	}

	public void parse(InputParameter param, Parameter<Date> into) 
					throws ParameterFormatException {
		try {
			into.setValue(sdf.parse(param.getCurrentValue()));
		} catch(ParseException ex) {
			throw new ParameterFormatException(
				"Die Eingabe \"" + param.getCurrentValue() + 
				"\" ist kein gültiges Datum nach dem Format " +
				getUsagePattern() + ".", ex
			);
		}
	}

	public String getUsagePattern() {
		return '\"' + fmt + '\"';
	}

}
