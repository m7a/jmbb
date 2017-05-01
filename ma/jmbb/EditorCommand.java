package ma.jmbb;

abstract class EditorCommand {

	final PrintfIO o;
	final DB db;

	EditorCommand(PrintfIO o, DB db) {
		super();
		this.o = o;
		this.db = db;
	}

	abstract String getCommandName();

	abstract String getDescription();

	/** Like in C, args[0] will be the command itself */
	abstract void call(String[] args) throws Exception;

	public void printDocumentation() {
		o.printf("This command is not documented in detail.\n");
	}

	String getArgsString() {
		return "";
	}

}
