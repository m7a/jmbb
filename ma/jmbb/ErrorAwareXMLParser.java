package ma.jmbb;

import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

class ErrorAwareXMLParser extends DefaultHandler {

	private final boolean hints;
	final PrintfIO o;

	ErrorAwareXMLParser(boolean printHints, PrintfIO o) {
		super();
		hints = printHints;
		this.o = o;
	}

	@Override
	public void warning(SAXParseException ex) throws SAXException {
		super.warning(ex);
		if(hints) {
			saxMessage("SAX generated a warning", ex);
		}
	}
	
	@Override
	public void error(SAXParseException ex) throws SAXException {
		super.error(ex);
		saxMessage("Error", ex);
	}
	
	@Override
	public void fatalError(SAXParseException ex) throws SAXException {
		super.fatalError(ex);
		saxMessage("Fatal error", ex);
	}

	private void saxMessage(String prefix, SAXParseException ex) {
		o.edprintf(ex, "%s while processing the input XML file.\n",
								prefix);
	}
	
	static SAXParser createParser() throws MBBFailureException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(true);
		factory.setXIncludeAware(true);
		try {
			return factory.newSAXParser();
		} catch(ParserConfigurationException ex) {
			throw new MBBFailureException("Could not obtain " +
				"parser instance via SAXParserFactory: " +
				"Configuration Exception.", ex);
		} catch(SAXException ex) {
			throw new MBBFailureException("Could not obtain " +
							"parser instance.", ex);
		}
	}

}
