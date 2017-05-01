package ma.tools2.util;

/**
 * @version 1.0.0.1
 * @author Linux-Fan, Ma_Sys.ma
 * @since Tools 2.1 (früher MathUtils)
 */
public class BinaryOperations {

	// Static
	private BinaryOperations() {
		super();
	}
	
	/**
	 * @param bdata Hexadezimalzeichen
	 * @return Mit {@link #signBytes(short[])} behandelte Rückgabe.
	 */
	public static byte[] decodeHexString(String bdata) {
		String cleanedDataIn = bdata.replace(" ", "");
		short[] numberData = new short[cleanedDataIn.length() / 2];
		int j;
		for(int i = 0; i < numberData.length; i++) {
			j = 2 * i;
			numberData[i] = Short.parseShort(
				cleanedDataIn.substring(j, j + 2), 0x10
			);
		}
		return signBytes(numberData);
	}

	/**
	 * Macht aus notierten Hexadezimalzahlen (0xff, 0xa5, 0x17, etc.)
	 * entsprechende Java-Bytes und fügt dafür entsprechend Vorzeichen ein.
	 * 
	 * @param unsignedInput
	 * 	Nicht vorzeichenbehaftete Hexadezimalzahlen 0-255
	 * @return Vorzeichenbehaftete bytes
	 */
	public static byte[] signBytes(short[] unsignedInput) {
		byte[] ret = new byte[unsignedInput.length];
		for(int i = 0; i < unsignedInput.length; i++) {
			if(unsignedInput[i] > 127) {
				ret[i] = (byte)(unsignedInput[i] - 0x100);
			} else {
				ret[i] = (byte)unsignedInput[i];
			}
		}
		return ret;
	}
	
}
