package ma.jmbb;

import java.util.*;

import java.text.SimpleDateFormat;

class RDateFormatter {

	private static final Calendar c = new GregorianCalendar();
	private static final SimpleDateFormat sdf =
				new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

	static {
		c.setTimeZone(TimeZone.getTimeZone("CET"));
	}

	static String format(long ms) {
		return sdf.format(new Date(ms));
	}

}
