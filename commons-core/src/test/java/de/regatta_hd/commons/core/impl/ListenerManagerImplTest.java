package de.regatta_hd.commons.core.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.EventListener;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ListenerManagerImplTest {

	private ListenerManagerImpl listenerManager;
	private TestListener listener = new TestListener();

	@BeforeAll
	static void setUpBeforeClass() {
		// nothing to setup yet
	}

	@AfterAll
	static void tearDownAfterClass() {
		// nothing to tear down yet
	}

	@BeforeEach
	void setUp() {
		this.listenerManager = new ListenerManagerImpl();
		this.listener = new TestListener();
	}

	@AfterEach
	void tearDown() {
		this.listenerManager = null;
		this.listener = null;
	}

	@Test
	void testAddListener() {
		assertThrows(NullPointerException.class, () -> {
			this.listenerManager.addListener(TestListener.class, null);
		});
		assertThrows(NullPointerException.class, () -> {
			this.listenerManager.addListener(null, this.listener);
		});

		this.listenerManager.addListener(TestListener.class, this.listener);

		List<TestListener> listeners = this.listenerManager.getListeners(TestListener.class);
		Assertions.assertTrue(listeners.contains(this.listener));
		Assertions.assertEquals(1, listeners.size());
	}

	@Test
	void testRemoveListener() {
		assertThrows(NullPointerException.class, () -> {
			this.listenerManager.removeListener(TestListener.class, null);
		});

		assertThrows(NullPointerException.class, () -> {
			this.listenerManager.removeListener(null, this.listener);
		});

		this.listenerManager.removeListener(TestListener.class, this.listener);
	}

	class TestListener implements EventListener {
		// nothing implemented yet
	}
}
