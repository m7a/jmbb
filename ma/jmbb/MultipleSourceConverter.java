package ma.jmbb;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import ma.tools2.args.*;

public class MultipleSourceConverter implements TypeConverter<List<File>> {

	public void parse(InputParameter param, Parameter<List<File>> into)
					throws ParameterFormatException {
		ArrayList<File> files = new ArrayList<File>();
		int pos = param.getPos();
		while(param.inRange(pos)) {
			param.setPos(pos);
			File f = new File(param.getCurrentValue());
			if(!f.exists()) {
				throw new ParameterFormatException(
					"Source does not exist: \"" +
					param.getCurrentValue() + "\"."
				);
			}
			files.add(f);
			pos++;
		}
		into.setValue(files);
	}

	public String getUsagePattern() {
		return "SRC...";
	}

}
