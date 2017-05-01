package ma.tools2.args.conv;

import java.io.*;
import ma.tools2.args.*;

/**
 * Hinweis: Der <code>Stream</code> wird sofort geöffnet.
 */
public class UnixOutputConverter implements TypeConverter<PrintStream> {

	private boolean stdout;

	public UnixOutputConverter() {
		super();
		stdout = true;
	}

	public UnixOutputConverter(boolean stdout) {
		super();
		this.stdout = stdout;
	}

	public void parse(InputParameter param, Parameter<PrintStream> into)
					throws ParameterFormatException {
		String val = param.getCurrentValue();
		if(val.equals("-")) {
			if(stdout) {
				into.setValue(System.out);
			} else {
				into.setValue(System.err);
			}
		} else {
			try {
				into.setValue(new PrintStream(val));
			} catch(FileNotFoundException ex) {
				throw new ParameterFormatException(
					"Konnte die Ausgabedatei \"" + val +
					"\" nicht öffnen.", ex
				);
			}
		}
	}

	public String getUsagePattern() {
		return "datei|-";
	}

}
