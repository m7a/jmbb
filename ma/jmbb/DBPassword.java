package ma.jmbb;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.xml.sax.Attributes;

import ma.tools2.util.HexUtils;
import ma.tools2.util.BinaryOperations;
import ma.tools2.util.NotImplementedException;

import static java.nio.charset.StandardCharsets.UTF_8;

class DBPassword {

	enum State { DEPRECATED, INACTIVE, CURRENT };

	final long id;
	final String password;
	final long timestamp;

	private State state;

	// For new password creation (DB INIT)
	DBPassword(String password) {
		super();
		this.password = password;
		id            = 1;
		timestamp     = System.currentTimeMillis();
		state         = State.CURRENT;
	}

	// For new password creation (Editor)
	DBPassword(String password, long id) {
		this.password = password;
		this.id       = id;
		timestamp     = System.currentTimeMillis();
		state         = State.CURRENT;
	}

	// For DB loading
	DBPassword(Attributes attr, String currentVal)
						throws MBBFailureException {
		super();
		password  = parsePassword(currentVal, attr.getValue("val_hex"));
		id        = Long.parseLong(attr.getValue("id"));
		timestamp = Long.parseLong(attr.getValue("timestamp"));
		state     = parseState(attr.getValue("state"));
	}

	private static String parsePassword(String current, String raw)
						throws MBBFailureException {
		byte[] rawB = BinaryOperations.decodeHexString(raw);
		if(current == null) {
			return new String(rawB, UTF_8);
		} else {
			return new String(Security.decrypt(current, rawB),
									UTF_8);
		}
	}

	private static State parseState(String sr) { // sr: state raw
		if(sr.equals("deprecated")) {
			return State.DEPRECATED;
		} else if(sr.equals("inactive")) {
			return State.INACTIVE;
		} else if(sr.equals("current")) {
			return State.CURRENT;
		} else {
			throw new NotImplementedException();
		}
	}

	void write(String current, XMLWriter out) throws IOException {
		try {
			out.txl(
				"<password val_hex=\"" +
						formatPassword(current) +
				"\" id=\"" + id +
				"\" timestamp=\"" + timestamp +
				"\"" + formatState() + "/>"
			);
		} catch(Exception ex) {
			throw new IOException(ex);
		}
	}

	private String formatPassword(String current) throws Exception {
		byte[] passwordDataDecrypted = password.getBytes(UTF_8);
		byte[] passwordData;
		if(current == null) {
			passwordData = passwordDataDecrypted;
		} else {
			passwordData = Security.encrypt(current,
							passwordDataDecrypted);
		}
		return HexUtils.formatAsHex(passwordData, false, false
								).toString();
	}

	private String formatState() {
		String ret = null;
		switch(state) {
		case DEPRECATED: return "";
		case INACTIVE:   ret = "inactive"; break;
		case CURRENT:    ret = "current";  break;
		}
		return " state=\"" + ret + "\"";
	}

	boolean isCurrent() {
		return state == State.CURRENT;
	}

	void setState(State s) {
		state = s;
	}

	String getStateString() {
		switch(state) {
		case DEPRECATED: return "deprecated";
		case INACTIVE:   return "inactive  ";
		case CURRENT:    return "current   ";
		default: throw new NotImplementedException();
		}
	}

	State getState() {
		return state;
	}

}
