package de.regatta_hd.schemas.xml;

import java.io.InputStream;
import java.util.Set;

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

	public static Liste loadWkrListe(InputStream input) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(Liste.class);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

		SAXParserFactory saxFactory = SAXParserFactory.newInstance();
		saxFactory.setNamespaceAware(true);
		saxFactory.setValidating(false);
		XMLReader xmlReader;
		try {
			xmlReader = saxFactory.newSAXParser().getXMLReader();

			// Create the filter (to add namespace) and set the xmlReader as its parent.
			NamespaceFilter namespaceFilter = new NamespaceFilter(Set.of("Liste", "Wettkampfrichter"),
					"http://schemas.rudern.de/service/wettkampfrichter/2017", true);
			namespaceFilter.setParent(xmlReader);

			InvalidNumberFilter numberFilter = new InvalidNumberFilter("Wettkampfrichter", "Lizenznummer");
			numberFilter.setParent(namespaceFilter);

			Source source = new SAXSource(numberFilter, new InputSource(input));

			return (Liste) unmarshaller.unmarshal(source);
		} catch (SAXException | ParserConfigurationException e) {
			throw new JAXBException(e);
		}
	}
}
