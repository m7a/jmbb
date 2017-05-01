package ma.tools2.args;

public interface TypeConverter<T> {

	void parse(InputParameter param, Parameter<T> into)
					throws ParameterFormatException;

	String getUsagePattern();

}
