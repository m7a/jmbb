package ma.jmbb;

import java.io.IOException;
import java.io.File;

import java.util.ArrayList;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Editor {

	private final PrintfIO o;
	private final File dbFile;

	private final Pattern tokenizer;

	private DB db;
	private EditorCommand[] cmds;

	private String input;

	Editor(PrintfIO o, File db) {
		super();
		this.o    = o;
		dbFile    = db;
		tokenizer = Pattern.compile("([^\"]\\S*|\".+?\")\\s*");
		input     = null;
		this.db   = null;
	}

	void run() throws MBBFailureException {
		open();
		registerEditorCommands();
		printHeader();

		try {
			query();
		} catch(IOException ex) {
			throw new MBBFailureException(ex);
		}
	}

	private void open() throws MBBFailureException {
		db = new DB(dbFile.toPath(), o);
		db.initFromLoc();
	}

	private void registerEditorCommands() {
		this.cmds = new EditorCommand[] {
			new EditBackup(o, db),
			new EditBlockSize(o, db),
			new EditDefrag(o, db),
			new EditDeprecatePassword(o, db),
			new EditEmpty(o, db),
			new EditMissing(o, db),
			new EditListPasswords(o, db),
			new EditPassword(o, db),
			new EditRename(o, db),
			new EditSave(o, db),
			new EditShowBlock(o, db),
			new EditObsoleteBlock(o, db),
			new EditShowStats(o, db),
			new EditCheckConsistency(o, db),
		};
	}

	private void printHeader() {
		o.printf("Welcome to the JMBB database editor.\n");
		o.printf("Enter \"help\" for a list of commands or \"exit\" " +
					"to leave without saving changes.\n\n");
	}

	private void query() throws IOException {
		while(prompt()) {
			try {
				execute();
			} catch(MBBFailureException ex) {
				o.edprintf(ex, "Error occurred.\n");
			}
		}
	}

	private boolean prompt() throws IOException {
		input = o.readRawLn("jmbb$ ");
		if(input == null)
			return false;
		input = input.trim();
		return !(input.equals("exit") || input.equals("q") ||
							input.equals("quit"));
	}

	private void execute() throws MBBFailureException {
		if(input.equals(""))
			return;

		String[] parts = tokenize(input);

		if(parts[0].equals("help")) {
			callInternalHelp(parts);
			return;
		}

		for(EditorCommand i: cmds) {
			if(i.getCommandName().equals(parts[0])) {
				processCommand(i, parts);
				return;
			}
		}

		o.printf("Command not found.\n");
	}

	// http://stackoverflow.com/questions/7804335/split-string-on-spaces-
	// 		except-if-between-quotes-i-e-treat-hello-world-as
	private String[] tokenize(String input) {
		ArrayList<String> list = new ArrayList<String>();
		Matcher m = tokenizer.matcher(input);
		while(m.find())
			list.add(m.group(1).replace("\"", ""));

		return list.toArray(new String[list.size()]);
	}

	private void callInternalHelp(String[] parts)
						throws MBBFailureException {
		if(parts.length == 2 && !parts[1].equals("--help")) {
			input = parts[1] + " --help";
			execute();
		} else {
			printHelp();
		}
	}

	private void printHelp() {
		for(EditorCommand i: cmds)
			o.printf("%-12s %s\n", i.getCommandName() + " " +
					i.getArgsString(), i.getDescription());
	}

	private static void processCommand(EditorCommand c, String[] parts)
						throws MBBFailureException {
		if(parts.length != 1 && parts[1].equals("--help")) {
			c.printDocumentation();
		} else {
			try {
				c.call(parts);
			} catch(Exception ex) {
				throw new MBBFailureException(ex);
			}
		}
	}

}
