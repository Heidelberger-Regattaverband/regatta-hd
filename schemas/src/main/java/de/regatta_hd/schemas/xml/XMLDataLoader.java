package de.regatta_hd.schemas.xml;

import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import de.rudern.schemas.service.wettkampfrichter._2017.Liste;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

public class XMLDataLoader {

	private XMLDataLoader() {
		// avoid instances
	}

	public static Liste loadWkrListe(InputStream input) throws JAXBException, SAXException, ParserConfigurationException {
		JAXBContext jaxbContext = JAXBContext.newInstance(Liste.class);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

		SAXParserFactory saxFactory = SAXParserFactory.newInstance();
		saxFactory.setNamespaceAware(true);
		saxFactory.setValidating(false);
		XMLReader xmlReader = saxFactory.newSAXParser().getXMLReader();

		// Create the filter (to add namespace) and set the xmlReader as its parent.
		NamespaceFilter inFilter = new NamespaceFilter("http://schemas.rudern.de/service/wettkampfrichter/2017", true);
		inFilter.setParent(xmlReader);

		Source source = new SAXSource(inFilter, new InputSource(input));

		return (Liste) unmarshaller.unmarshal(source);
	}
}
