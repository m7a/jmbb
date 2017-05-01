package ma.jmbb;

import java.io.*;
import ma.tools2.args.*;

public class NamedFileConverter implements TypeConverter<File> {

	private String name;

	NamedFileConverter(String name) {
		super();
		this.name = name;
	}

	public void parse(InputParameter param, Parameter<File> into)
					throws ParameterFormatException {
		into.setValue(new File(param.getCurrentValue()));
	}

	public String getUsagePattern() {
		return name;
	}

}
