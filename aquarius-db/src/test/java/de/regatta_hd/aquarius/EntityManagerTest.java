package de.regatta_hd.aquarius;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Persistence;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.regatta_hd.aquarius.db.Event;
import de.regatta_hd.aquarius.db.EventId;

class EntityManagerTest {

	private static EntityManager entityManager;

	@BeforeAll
	static void setUpBeforeClass() {
		EntityManagerFactory factory = Persistence.createEntityManagerFactory("aquarius");
		entityManager = factory.createEntityManager();
	}

	@AfterAll
	static void tearDownAfterClass() {
		if (entityManager != null) {
			entityManager.close();
			entityManager = null;
		}
	}

	@BeforeEach
	void setUp() {
	}

	@AfterEach
	void tearDown() {
	}

	@Test
	void testGetEventOK() {
		EventId id = new EventId("1");
		Event event = entityManager.getReference(Event.class, id);
		Assertions.assertEquals(id.eventID, event.getEventID());
		Assertions.assertNotNull(event);
	}

	@Test
	void testGetEventFailed() {
		Assertions.assertThrows(EntityNotFoundException.class, () -> {
			EventId id = new EventId("2");
			Event event = entityManager.getReference(Event.class, id);
			// as event with ID == 2 doesn't exist, calling any getter causes an EntityNotFoundException
			event.getEventID();
		});
	}
}
