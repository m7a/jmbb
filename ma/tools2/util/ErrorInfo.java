package ma.tools2.util;

/**
 * Diese Klasse liest Stack-Trace Informationen aus Fehlerobjekten aus.
 * 
 * @author Linux-Fan, Ma_Sys.ma
 * @version 2.0.0.2
 */
public class ErrorInfo {
	
	/**
	 * Liest die StackTrace aus einer Throwable aus.
	 * 
	 * Das Auslesen der Stack Trace ist wichtig, weil man die
	 * Stack-Trace oft auch in grafischen Programmen anzeigen muss.
	 * Die Stack Trace ist der Standart Stack-Trace, die
	 * man über {@link java.lang.Throwable#printStackTrace()}
	 * erh&auml;lt sehr &auml;hnlich.
	 * <br><br>
	 * Ein Beispiel :
	 * <pre>
	 * import ma.tools2.ErrorInfo;
	 * 
	 * public class TTSStackTrace {
	 * 	public static void main(String[] args) {
	 * 		try {
	 * 			int[] array = new int[0];
	 * 			int test = array[1];
	 * 			System.out.println(test);
	 * 		} catch(Exception ex) {
	 * 			System.out.println(
	 * 				ErrorInfo.getStackTrace(ex)
	 * 			);
	 * 		}
	 * 	}
	 * }
	 * </pre>
	 * Diese Methode wird auch von der Klasse ErrorDialog verwendet,
	 * ein kleiner Ausschnitt aus DetailInit :
	 * <pre>
	 * trace.append(ErrorInfo.getStackTrace(owner.t));
	 * </pre>
	 * 
	 * @see ma.tools.gui.ErrorDialog
	 * @see java.lang.Throwable#printStackTrace()
	 * @param t
	 * 	Die Throwable, aus der die Stack-Trace ausgelesen werden soll.
	 * @return 
	 * 	Einen String mit einer standart-Java &auml;hnlichen Stack-Trace
	 * 	die Zeilen sind mittels \n getrennt, sie m&uuml;ssen also vor
	 * 	der Benutzung in Windows-Dateien konvertiert werden, z.B.: 
	 * 	<br>
	 * 	<code>String stack =
	 * 	ErrorInfo.getStackTrace(excpetion).replace("\n",
	 * 	"\r\n");</code>
	 */
	public static StringBuilder getStackTrace(Throwable t) {
		StringBuilder trace = new StringBuilder();
		Throwable current = t;
		boolean causes = false;
		while(current != null) {
			if(causes) {
				trace.append("Caused by: ");
				trace.append(current.toString());
				trace.append('\n');
			} else {
				trace.append(current.toString());
				trace.append('\n');
			}
			StackTraceElement[] elements = current.getStackTrace();
			for(int j = 0; j < elements.length; j++) {
				trace.append("      at ");
				trace.append(elements[j].getClassName());
				trace.append('.');
				trace.append(elements[j].getMethodName());
				trace.append('(');
				if(elements[j].isNativeMethod()) {
					trace.append("Native Method");
				} else {
					trace.append(
						elements[j].getFileName()
					); 
					trace.append(':');
					trace.append(
						elements[j].getLineNumber()
					); 
				}
				trace.append(")\n");
			}
			current = current.getCause();
			causes = true;
		}
		return trace;
	}
	
}
