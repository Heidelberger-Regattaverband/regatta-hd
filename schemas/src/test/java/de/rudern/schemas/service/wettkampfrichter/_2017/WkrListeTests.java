package de.rudern.schemas.service.wettkampfrichter._2017;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.regatta_hd.schemas.xml.XMLDataLoader;
import jakarta.xml.bind.JAXBException;

class WkrListeTests {

	private static Liste liste;

	@BeforeAll
	static void setUpBeforeClass() throws IOException, JAXBException {
		try (InputStream input = WkrListeTests.class.getResourceAsStream("/wkr-2022-05-18.xml")) {
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

		assertFalse(wettkampfrichter.isEmpty());
	}

}
