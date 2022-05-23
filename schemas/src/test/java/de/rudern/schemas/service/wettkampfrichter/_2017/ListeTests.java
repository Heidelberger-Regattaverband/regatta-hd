package de.rudern.schemas.service.wettkampfrichter._2017;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.xml.bind.JAXB;

class ListeTests {

	private static Liste liste;

	@BeforeAll
	static void setUpBeforeClass() throws IOException {
		try (InputStream input = ListeTests.class.getResourceAsStream("/wkr-2022-05-18.xml")) {
			liste = JAXB.unmarshal(input, Liste.class);
		}
	}

	@AfterAll
	static void tearDownAfterClass() {
		liste = null;
	}

	@Test
	void testGetWettkampfrichter() {
		List<Object> wettkampfrichter = liste.getAny();

		Assertions.assertFalse(wettkampfrichter.isEmpty());
	}

}
