package ma.tools2.args;

import ma.tools2.util.StringUtils;

abstract class PartialArgs {

	private static final int  WRAP_AFTER  = 80;
	private static final int  TABSIZE     = 8;
	private static final char INDENTATION = '\t';

	static final int NOT_MATCHED = -1;

	private final String description;

	protected PartialArgs(String description) {
		super();
		this.description = description;
	}

	abstract void parse(InputParameter in) throws ParameterFormatException;

	abstract boolean isSet();

	abstract String getUsage();

	/**
	 * Gibt eine Hilfe zum implementierenden Objekt aus. Standardmäßig
	 * wird hier nur die Beschreibung ausgegebenen. Unterklassen
	 * implementieren dann ausführlichere Ausgaben über
	 * <code>printI</code>.
	 *
	 * Diese Methode schlägt nicht fehl. Das heißt, wenn keine Beschreibung
	 * gesetzt ist, sondern der Pseudo-Wert <code>null</code>, wird einfach
	 * kommentarlos keine Ausgabe gemacht.
	 *
	 * @see #printI(int, String)
	 */
	protected void printHelp(int indentationLevel) {
		if(description != null) {
			printI(indentationLevel, description);
		}
	}

	protected void printI(int level, String message) {
		printI(level, message, false);
	}

	/**
	 * Gibt die angegebene Nachricht mit der angegebenen Einrückung aus.
	 *
	 * Dabei werden zu lange Zeilen unter Berücksichtigung manueller
	 * Umbrüche, automatisch umgebrochen.
	 *
	 * I steht für "`indented"' (eingerückt).
	 *
	 * @param level Einrückungsgrad in Tabulatoren
	 * @param message
	 * 	Auszugebene Nachricht (auch mehrzeilige mit
	 * 	<code>\n</code> getrennt);
	 * @param indentAdd
	 * 	Gibt an, ob die erste Zeile auch eingerückt werden soll.
	 */
	protected void printI(int level, String message, boolean indentAdd) {
		final String indent = StringUtils.repeat(INDENTATION, level);
		String[] lines = message.split("\n");
		for(int i = 0; i < lines.length; i++) {
			printLine(indent, lines[i], indentAdd);
		}
	}

	/**
	 * Gibt eine einzelne Zeile der Hilfeausgabe aus. Dabei wird der
	 * angegebene String zum Einrücken verwendet und die angegebene
	 * Zeile ausgegeben. Zur Ausgabe dient <code>System.out</code>
	 *
	 * @param indent Einrückstring. Nur ein Zeichen pro Einrückung.
	 * @param line Auszugebende Zeile.
	 */
	private void printLine(String indent, String line,
							boolean indentAdd) {
		final String[] words = line.split(" ");
		int indentationLength = indent.length() * TABSIZE;
		int cLineWidth = 0;
		int add;
		for(int i = 0; i < words.length; i++) {
			add = words[i].length() + 1;
			cLineWidth += add;
			if(i == 0 || cLineWidth >= WRAP_AFTER) {
				if(indentAdd && i != 0) {
					// Diese Sache wird nur einmal 
					// ausgeführt.
					indentAdd          = false;
					indentationLength += TABSIZE;
					indent            += INDENTATION;
				}
				cLineWidth = indentationLength + add;
				if(i != 0) {
					System.out.println();
				}
				System.out.print(indent);
			}
			System.out.print(words[i]);
			System.out.print(' ');
		}
		System.out.println(); // Zeile beenden
	}

	boolean hasDescription() {
		return description != null;
	}

}
