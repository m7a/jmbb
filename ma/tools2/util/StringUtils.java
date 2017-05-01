package ma.tools2.util;

import java.util.StringTokenizer;

/**
 * Stellt verschiedene Funktionen mit und f&uuml;r Strings bereit.
 * Die Methoden lassen sich alle statisch aufrufen.
 * 
 * @author Linux-Fan, Ma_Sys.ma
 * @version 2.0
 * @since Tools 2
 */
public class StringUtils {
	
	private StringUtils() {}
	
	
	/**
	 * Diese Methode r&uuml;ckt Texte oder Strings anhand von Bestimmten
	 * Zeichen ein.
	 * <br>
	 * Beispiel :
	 * <pre>
	 * import ma.tools.StringUtilis;
	 * 
	 * public class IndentTest {
	 * 	public static void main(String[] args) {
	 * 		String xml1 = "&lt;test&gt;\n hallo\n&lt;/test&gt;\n";
	 * 		String xml2 = "&lt;lo&gt;\n" + StringUtilis.indent(
	 * 				xml1, " ", '\n') + "&lt;/lo&gt;";
	 * 		System.out.println(xml2);
	 * 	}
	 * }
	 * </pre>
	 * @param in Der Eingabe String
	 * @param with
	 *	Der String, der die Zeichen, die zum Einr&uuml;cken verwendet
	 *	werden sollen enth&auml;lt.
	 * @param separator Der Separator (meistens "\n" oder "\r\n")
	 * @return Einen neuen String, der die &Auml;nderungen enth&auml;lt.
	 */
	public static String indent(String in, String with, String separator) {
		StringBuffer ret = new StringBuffer("");
		StringTokenizer t = new StringTokenizer(in, separator);
		while (t.hasMoreElements()) {
			ret.append(with + t.nextToken() + separator);
		}
		return ret.toString();
	}

	/**
	 * @param messageIn Eingabestring
	 * @return Ungefär wie bei der PHP htmlspecialchars(messageIn);
	 */
	public static String htmlentities(String messageIn) {
		String message = messageIn.replace("<", "&lt;");
		message = message.replace(">", "&gt;");
		message = message.replace("&", "&amp;");
		message = message.replace("\"", "&quot;");
		return message;
	}
	
	public static int count(String source, char find) {
		int count = 0;
		char[] chars = source.toCharArray();
		for(int i = 0; i < chars.length; i++) {
			if(chars[i] == find) {
				count++;
			}
		}
		return count;
	}

	public static String repeat(char character, int count) {
		char[] ret = new char[count];
		for(int i = 0; i < count; i++) {
			ret[i] = character;
		}
		return new String(ret);
	}
}
