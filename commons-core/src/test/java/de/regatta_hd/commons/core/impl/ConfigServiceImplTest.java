package de.regatta_hd.commons.core.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConfigServiceImplTest {

	private static ConfigServiceImpl cfgService;

	@BeforeAll
	static void setUpBeforeClass() {
		ClassLoader classLoader = ConfigServiceImplTest.class.getClassLoader();
		Path path = Paths.get(classLoader.getResource("config.properties").getFile());
		cfgService = new ConfigServiceImpl(path);
	}

	@AfterAll
	static void tearDownAfterClass() {
		cfgService = null;
	}

	@BeforeEach
	void setUp() {
	}

	@AfterEach
	void tearDown() {
	}

	@Test
	void testGetProperty() throws IOException {
		assertEquals("my test string", cfgService.getProperty("stringProperty"));
	}

	@Test
	void testGetBooleanProperty() throws IOException {
		assertTrue(cfgService.getBooleanProperty("booleanPropertyTrue"));
		assertFalse(cfgService.getBooleanProperty("booleanPropertyFalse"));
		assertFalse(cfgService.getBooleanProperty("invalid_key"));
	}

	@Test
	void testGetIntegerProperty() throws NumberFormatException, IOException {
		assertEquals(2, cfgService.getIntegerProperty("intProperty"));
		assertNull(cfgService.getIntegerProperty("invalid_key"));

		assertThrows(NumberFormatException.class, () -> {
			cfgService.getIntegerProperty("intPropertyInvalid");
		});
	}

	@Test
	void testSetPropertyStringString() throws IOException {
		cfgService.setProperty("newStringProperty", "my new test value");

		assertEquals("my new test value", cfgService.getProperty("newStringProperty"));
	}

	@Test
	void testSetPropertyStringBoolean() throws IOException {
		cfgService.setProperty("newBooleanProperty", true);

		assertTrue(cfgService.getBooleanProperty("newBooleanProperty"));

		cfgService.removeProperty("newBooleanProperty");

		assertNull(cfgService.getProperty("newBooleanProperty"));

	}

	@Test
	void testSetPropertyStringInt() throws IOException {
		cfgService.setProperty("newIntProperty", -119);

		assertEquals(-119, cfgService.getIntegerProperty("newIntProperty"));
	}

}
