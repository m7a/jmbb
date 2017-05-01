package ma.tools2.args.conv;

import java.io.*;
import ma.tools2.args.*;

/**
 * Hinweis: Der <code>Stream</code> wird sofort geöffnet.
 */
public class UnixInputConverter implements TypeConverter<InputStream> {

	public UnixInputConverter() {
		super();
	}

	public void parse(InputParameter param, Parameter<InputStream> into)
					throws ParameterFormatException {
		String val = param.getCurrentValue();
		if(val.equals("-")) {
			into.setValue(System.in);
		} else {
			try {
				into.setValue(new FileInputStream(val));
			} catch(IOException ex) {
				throw new ParameterFormatException(
					"Konnte die Eingabedatei \"" + val +
					"\" nicht öffnen.", ex
				);
			}
		}
	}

	public String getUsagePattern() {
		return "datei|-";
	}

}
