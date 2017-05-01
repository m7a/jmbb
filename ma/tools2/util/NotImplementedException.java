package ma.tools2.util;

public class NotImplementedException extends RuntimeException {

	public NotImplementedException() {
		super(
			"Der aufgerufene Code ist nicht implementiert. " +
			"Entweder ist das Programm unvollständig, oder Sie " +
			"haben Daten eingegeben, mit denen das Programm " +
			"ohne Weiteres nichts anfangen kann. "
		);
	}

	public NotImplementedException(String msg) {
		super(msg);
	}

	public NotImplementedException(String msg, Exception cause) {
		super(msg, cause);
	}

	public NotImplementedException(Exception cause) {
		super(cause);
	}

}
