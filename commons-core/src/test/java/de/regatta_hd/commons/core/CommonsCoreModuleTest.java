package de.regatta_hd.commons.core;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

class CommonsCoreModuleTest {

	private static Injector injector;

	@BeforeAll
	static void setUpBeforeClass() {
		injector = Guice.createInjector(new CommonsCoreModule());
	}

	@AfterAll
	static void tearDownAfterClass() {
		injector = null;
	}

	@Test
	void testGetHostName() {
		String hostName = injector.getProvider(Key.get(String.class, Names.named("hostName"))).get();
		assertNotNull(hostName);
	}

	@Test
	void testGetHostAddress() {
		String hostAddress = injector.getProvider(Key.get(String.class, Names.named("hostAddress"))).get();
		assertNotNull(hostAddress);
	}

	@Test
	void testGetVersion() {
		String version = injector.getProvider(Key.get(String.class, Names.named("version"))).get();
		assertNotNull(version);
	}
}
