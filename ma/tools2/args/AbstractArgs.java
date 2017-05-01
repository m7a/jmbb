package ma.tools2.args;

import ma.tools2.args.conv.BooleanConverter;
import java.io.*;

public abstract class AbstractArgs extends ArgumentList {

	private static final String MESSAGE =
		"Ma_Sys.ma Argument Evaluation, Copyright (c) 2012 " + 
		"Ma_Sys.ma.\nFor further info send an e-mail to " + 
		"Ma_Sys.ma@web.de.";

	private static final int BUFFER_SIZE = 0x400;

	private Parameter<Boolean> help;
	private Parameter<Boolean> version;
	private Parameter<Boolean> license;

	protected AbstractArgs() {
		super(MESSAGE, ArgumentComposition.XOR);
		// Standardparameter intialisieren
		// Dabei werden Symbole verwendet, weil diese in normalen
		// Programmen nicht vorkommen. Screenindex belegt z.B.
		// schon nützliche Optionen wie z.B. -v, welches für 
		// "version" passender wäre
		add(help = new Parameter<Boolean>(
			"help", '?', false, new BooleanConverter(),
			"Zeigt eine automatisch generierte Hilfeseite an."
		));
		add(version = new Parameter<Boolean>(
			"version", '!', false, new BooleanConverter(),
			"Zeigt einen kurzen Versionshinweis an."
		));
		add(license = new Parameter<Boolean>(
			"license", '$', false, new BooleanConverter(),
			"Zeigt die Lizenz an."
		));
		// Anwendungsentwickler initialisieren ihre eigenen Parameter
		// in unterklassen, nachdem sie super() aufgerufen haben.
	}

	/**
	 * @return Anwendung sollte terminieren.
	 */
	public boolean parseAndReact(String[] args) {
		try {
			parse(args);
		} catch (ArgumentListParsingException ex) {
			// Bei fehlerhafter Eingabe keine Hilfe ausgeben.
			ex.printStackTrace();
			return true;
		}
		if(help.getValue()) {
			printHelp(0);
		} else if(version.getValue()) {
			printCopyright();
		} else if(license.getValue()) {
			printLicense();
		} else {
			// Erfolg ist immer dann, wenn die Argumente
			// verarbeitet wurden und keine Standardargumente
			// gesetzt waren.
			return false;
		}
		return true;
	}

	public void parse(String[] args) throws ArgumentListParsingException {
		final InputParameter param = new InputParameter(args);
		param.process(this);
		if(!isSet()) {
			throw new ArgumentListParsingException(
				"Die angegebenen Parameter ergeben keine " +
				"gültige Kombination. Versuchen Sie einen " + 
				"Aufruf mit --help als einzigem Parameter."
			);
		}
	}

	protected void printHelp(int level) {
		printCopyright();
		super.printHelp(level);
		System.out.println();
	}

	private void printCopyright() {
		final String[] cprght = getCopyrightLines();
		for(int i = 0; i < cprght.length; i++) {
			System.out.println(cprght[i]);
		}
	}

	protected String[] getCopyrightLines() {
		return new String[] {
			getApplicationName() + ' ' + getApplicationVersion() +
				", Copyright (c) " + getCopyrightYears() +
				" Ma_Sys.ma.",
			"For further info send an e-mail to Ma_Sys.ma@web.de.",
			""
		};
	}

	private void printLicense() {
		try {
			InputStream in = getClass().getResourceAsStream(
						getLicenseResourceName());
			byte[] buf = new byte[BUFFER_SIZE];
			int count;
			while((count = in.read(buf, 0, buf.length)) > 0) {
				System.out.write(buf, 0, count);
			}
			in.close();
		} catch(IOException ex) {
			System.err.println(
				"Lizenzdatei konnte nicht gelsen werden."
			);
			ex.printStackTrace();
		}
		System.out.println();
	}

	/**
	 * Format: <code>/package/in/unix/pfad/notation/datei.txt</code>
	 */
	protected String getLicenseResourceName() {
		return "/ma/tools2/license.txt";
	}

	protected abstract String getApplicationName();
	protected abstract String getApplicationVersion();

	protected String getCopyrightYears() {
		return "2012";
	}

	protected String getUsage() {
		return "USAGE: " + getInvocationCommand() + ' ' +
							super.getUsage();
	}

	// TODO DETERMINE DYNAMICALLY
	protected String getInvocationCommand() {
		return "java <Application>";
	}

}
