package ma.tools2.util;

/**
 * @since Tools 2.1 (Previously in StringUtils)
 * @author Linux-Fan, Ma_Sys.ma
 * @version 1.0
 */
public class HexUtils {

	// Static
	private HexUtils() {
		super();
	}

	public static StringBuilder formatAsHex(byte[] data, boolean smooth) {
		return formatAsHex(data, smooth, true);
	}
	
	public static StringBuilder formatAsHex(byte[] data, boolean smooth,
							boolean spaces) {
		StringBuilder ret = new StringBuilder(data.length * 3);
		for(int i = 0; i < data.length; i++) {
			ret.append(formatAsHex(data[i]));
			if(spaces) {
				ret.append(' ');
			}
			if(smooth && i % 0x10 == 0x0f) {
				ret.append("\n ");
			}
		}
		return ret;
	}
	
	/**
	 * Formatiert den angegebenen Byte als Hexadezimalzahl
	 * mit zwei Stellen.
	 * <br>
	 * Beispiel :
	 * <pre>
	 * import ma.tools.StringUtils;
	 * 
	 * public class FormatHexTest {
	 * 	public static void main(String[] args) {
	 * 		byte theByte = 56;
	 * 		System.out.println(StringUtils.formatAsHex(theByte);
	 * 	}
	 * }
	 * </pre>
	 * 
	 * @param number Der zu formatierende byte.
	 * @return
	 * 	Einen String, der die Hexadezimaldarstellung von
	 * 	number enth&auml;lt.
	 */
	public static String formatAsHex(byte number) {
		String ret = null;
		String hex = Integer.toHexString(number);
		int len = hex.length();
		if(len == 1) {
			ret = "0" + hex;
		} else if(len == 2) {
			ret = hex;
		} else if(len > 2) {
			ret = hex.substring(len-2);
		}
		return ret;
	}
	
}
