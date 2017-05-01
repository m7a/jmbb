package ma.jmbb;

import java.io.*;
import ma.tools2.args.*;

public class NamedSourceDirectoryConverter implements TypeConverter<File> {

	private String name;

	NamedSourceDirectoryConverter(String name) {
		super();
		this.name = name;
	}

	public void parse(InputParameter param, Parameter<File> into)
					throws ParameterFormatException {
		File f = new File(param.getCurrentValue());
		if(!f.exists()) {
			throw new ParameterFormatException(
				"\"" + param.getCurrentValue() +
				"\" does not exist."
			);
		}
		if(!f.isDirectory()) {
			throw new ParameterFormatException(
				"\"" + param.getCurrentValue() +
				"\" is not a directory."
			);
		}
		into.setValue(f);
	}

	public String getUsagePattern() {
		return name;
	}

}
