package ma.jmbb;

import java.io.IOException;

import org.xml.sax.Attributes;

import ma.tools2.util.NotImplementedException;

class DBSpecs {

	private final int cpus;
	private final String osName;
	private final String javaHome;
	private final String vmName;
	private final String userName;
	private final String userHome;

	private String osVersion;
	private String vmVersion;
	private long diskUsageUsed;
	private long diskUsageFree;

	DBSpecs(Attributes attr) {
		super();
		throw new NotImplementedException();
	}

	void write(XMLWriter out) throws IOException {
		out.txl(
			"<specs cpus=\"" + cpus +
			"\" dused=\"" + diskUsageUsed +
			"\" dfree=\"" + diskUsageFree +
			"\" oname=\"" + osName +
			"\" over=\"" + osVersion +
			"\" jvmn=\"" + vmName +
			"\" jver=\"" + vmVersion +
			"\" jhome=\"" + javaHome +
			"\" usrname=\"" + userName +
			"\" usrhome=\"" + userHome + "\" />"
		);
	}

}
