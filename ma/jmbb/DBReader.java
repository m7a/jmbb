package ma.jmbb;

import java.io.*;

import java.util.zip.GZIPInputStream;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

import java.net.URI;

import org.xml.sax.*;

import javax.xml.parsers.SAXParser;

import ma.tools2.util.NotImplementedException;

class DBReader extends ErrorAwareXMLParser {
	
	// Entering something wrong here can lead to problems like
	// Caused by: java.net.MalformedURLException
	// ...
	// at com.sun.org.apache.xerces.internal.impl.XMLEntityManager.
	//			setupCurrentEntity(XMLEntityManager.java:606)
	private static final String DTD_LOC = "/ma/jmbb/mbb_db.dtd";

	private final DB db;
	private final Path file;

	private DBBlock currentBlock;

	DBReader(DB db, Path dbf, PrintfIO o) {
		super(true, o);
		this.db   = db;
		this.file = dbf;
	}

	private DBReader(PrintfIO o, Path f) {
		super(true, o);
		db   = null;
		file = f;
	}

	static DBBlock readSingleBlock(PrintfIO o, InputStream in, Path blkf)
				throws MBBFailureException, IOException {
		DBReader internal = new DBReader(o, blkf);
		internal.parseStream(in);
		return internal.currentBlock;
	}

	// -- Initialization ---------------------------------------------------

	void readDatabase() throws MBBFailureException, IOException {
		readDatabase(Files.newInputStream(file));
	}

	void readDatabase(InputStream inR) throws MBBFailureException,
								IOException {
		InputStream in = new GZIPInputStream(inR);
		parseStream(in);
		
		if(!db.passwords.hasCurrent()) {
			o.printf("Database %s\n", file.toString());
			db.passwords.readInteractively(o);
		}	
	}

	private void parseStream(InputStream in)
				throws IOException, MBBFailureException {
		SAXParser parser = createParser();
		try {
			parser.parse(in, this);
		} catch(SAXException ex) {
			throw new MBBFailureException(ex);
		} finally {
			in.close();
		}
	}
	
	// -- Element parsing --------------------------------------------------

	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes attrs) throws SAXException {
		try {
			if(qName.equals("db")) {
				db.header.readFrom(attrs);
			} else if(qName.equals("block")) {
				procBlock(attrs);
			} else if(qName.equals("file")) {
				currentBlock.addFile(new DBFile(attrs));
			} else if(qName.equals("nte")) {
				procNewTimeEntry(attrs);
			} else if(qName.equals("password")) {
				procPassword(attrs);
			}
		} catch(Exception ex) {
			throw new SAXException(ex);
		}
	}

	private void procBlock(Attributes attrs) {
		if(db == null) {
			currentBlock = new DBBlock(file, attrs,
						DB.DEFAULT_BLOCKSIZE_KIB);
		} else {
			currentBlock = new DBBlock(db, attrs);
		}
	}

	private void procNewTimeEntry(Attributes attrs) {
		db.times.putDeserialized(attrs.getValue("path"),
				Long.parseLong(attrs.getValue("mtime")));
	}

	private void procPassword(Attributes attrs) throws MBBFailureException {
		if(db.passwords.hasCurrent()) {
			db.passwords.add(new DBPassword(attrs,
					db.passwords.getCurrentValue()));
		} else {
			db.passwords.add(new DBPassword(attrs, null));
		}
	}

	@Override
	public void endElement(String namespaceURI, String localName,
					String qName) throws SAXException {
		if(qName.equals("block") && db != null) {
			db.blocks.add(currentBlock);
			currentBlock = null;
		}
	}

	// -- DTD resolving ----------------------------------------------------

	@Override
	public InputSource resolveEntity(String publicId, String systemId)
					throws IOException, SAXException {
		checkId(publicId);

		final URI uri      = constructURI(systemId);
		final Path dtdPath = Paths.get(uri);
		final String name  = dtdPath.getFileName().toString();

		return createDTDFromName(name);
	}

	private void checkId(String publicId) throws SAXException {
		if(publicId != null) {
			throw new SAXException("This parser does not know " +
				"public resources (should normally not be " +
				"necessary... wrong XML file?)");
		}
	}

	private URI constructURI(String systemId) throws SAXException {
		try {
			return new URI(systemId);
		} catch(Exception ex) {
			throw new SAXException("Failed to construct URI " +
					"from \"" + systemId + "\".", ex);
		}
	}

	private InputSource createDTDFromName(String name) throws IOException,
								SAXException {
		if(name.equals("mbb_db.dtd")) {
			return createDTDSource();
		} else {
			throw new SAXException("This parser can only load " +
				"an external mbb_db.dtd. Other DTDs are not " +
				"supported and should not be necessary. If " +
				"you need this feature, go to " +
				getClass().getCanonicalName() + " and add " +
				"code as necessary.");
		}
	}

	private InputSource createDTDSource() throws IOException {
		return new InputSource(getClass().getResourceAsStream(DTD_LOC));
	}

}
