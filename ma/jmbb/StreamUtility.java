package ma.jmbb;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import java.security.MessageDigest;

class StreamUtility {

	// never create instances of this class.
	private StreamUtility() {
		super();
	}

	static void copy(InputStream in, MessageDigest md, OutputStream out)
							throws IOException {
		byte[] buf = new byte[JMBBInterface.DEFAULT_BUFFER];
		int len;
		while((len = in.read(buf, 0, buf.length)) != -1) {
			out.write(buf, 0, len);
			if(md != null) {
				md.update(buf, 0, len);
			}
		}
	}

}
