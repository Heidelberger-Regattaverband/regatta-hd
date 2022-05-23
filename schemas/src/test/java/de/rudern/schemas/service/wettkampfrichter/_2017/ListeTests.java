package de.rudern.schemas.service.wettkampfrichter._2017;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

class ListeTests {

	private static Liste liste;

	@BeforeAll
	static void setUpBeforeClass() throws IOException, JAXBException, SAXException, ParserConfigurationException {
		try (InputStream input = ListeTests.class.getResourceAsStream("/wkr-2022-05-18.xml")) {
			JAXBContext jaxbContext = JAXBContext.newInstance(Liste.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

			SAXParserFactory saxFactory = SAXParserFactory.newInstance();
			saxFactory.setNamespaceAware(true);
			saxFactory.setValidating(false);
			XMLReader xmlReader = saxFactory.newSAXParser().getXMLReader();
			Source source = new SAXSource(xmlReader, new InputSource(input));

			liste = (Liste) unmarshaller.unmarshal(source);
		}
	}

	@AfterAll
	static void tearDownAfterClass() {
		liste = null;
	}

	@Test
	void testGetWettkampfrichter() {
		List<TWKR> wettkampfrichter = liste.getWettkampfrichter();

		Assertions.assertFalse(wettkampfrichter.isEmpty());
	}

}
