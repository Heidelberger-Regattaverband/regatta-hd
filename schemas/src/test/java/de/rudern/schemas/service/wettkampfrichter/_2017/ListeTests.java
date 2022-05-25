package de.rudern.schemas.service.wettkampfrichter._2017;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import de.regatta_hd.schemas.xml.XMLDataLoader;
import jakarta.xml.bind.JAXBException;

class ListeTests {

	private static Liste liste;

	@BeforeAll
	static void setUpBeforeClass() throws IOException, JAXBException, SAXException, ParserConfigurationException {
		try (InputStream input = ListeTests.class.getResourceAsStream("/wkr-2022-05-18.xml")) {
			liste = XMLDataLoader.loadWkrListe(input);
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
