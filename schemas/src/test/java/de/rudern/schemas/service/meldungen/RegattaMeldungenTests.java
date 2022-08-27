package de.rudern.schemas.service.meldungen;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.regatta_hd.schemas.xml.XMLDataLoader;
import de.rudern.schemas.service.meldungen._2010.RegattaMeldungen;
import de.rudern.schemas.service.meldungen._2010.RegattaMeldungen.Meldungen;
import jakarta.xml.bind.JAXBException;

class RegattaMeldungenTests {

	private static RegattaMeldungen regattaMeldungen;

	@BeforeAll
	static void setUpBeforeClass() throws IOException, JAXBException {
		try (InputStream input = RegattaMeldungenTests.class
				.getResourceAsStream("/meldungen-88__Heidelberger_Ruder-Regatta-2022-08-19_16-41.xml")) {
			regattaMeldungen = XMLDataLoader.loadRegattaMeldungen(input);
		}
	}

	@AfterAll
	static void tearDownAfterClass() {
		regattaMeldungen = null;
	}

	@Test
	void testGetWettkampfrichter() {
		Meldungen meldungen = regattaMeldungen.getMeldungen();

		assertFalse(meldungen.getRennen().isEmpty());
	}

}
