package de.regatta_hd.aquarius;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import de.regatta_hd.aquarius.db.model.Comp;
import de.regatta_hd.aquarius.db.model.CompEntries;
import de.regatta_hd.aquarius.db.model.Entry;
import de.regatta_hd.aquarius.db.model.Event;
import de.regatta_hd.aquarius.db.model.EventId;
import de.regatta_hd.aquarius.db.model.Offer;

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

		event.getOffers().forEach(this::trace);
//		event.getComps().forEach(this::trace);
//		event.getEntrys().forEach(this::trace);
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

	@Test
	@Disabled
	void testPokal() {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Offer> cq = builder.createQuery(Offer.class);
		Root<Offer> from = cq.from(Offer.class);
		cq.select(from);

		TypedQuery<Offer> query = entityManager.createQuery(cq);
		List<Offer> resultList = query.getResultList();

		resultList.forEach(this::trace);
	}

	private void trace(Offer offer) {
		System.out.println("Offer: ID=" + offer.getOfferID() + ", RaceNr=" + offer.getOfferRaceNumber() + " - "
				+ offer.getOfferLongLabel());
		offer.getComps().forEach(this::trace);
	}

	private void trace(Comp comp) {
//		Label label = entityManager.getReference(Label.class, new LabelId(comp.getCompLabel()));
		System.out.println("\tComp: ID=" + comp.getCompID() + ", Label=" + comp.getCompLabel() + ", Round="
				+ comp.getCompRound() + ", HeatNr=" + comp.getCompHeatNumber() + ", Number=" + comp.getCompNumber());
		comp.getCompEntries().forEach(this::trace);
	}

	private void trace(Entry entry) {
		System.out
				.println("Entry: ID=" + entry.getEntryID() + ": " + entry.getLabel() + ", " + entry.getEntryComment());
	}

	private void trace(CompEntries compentries) {
		System.out.println("\t\tCompEntries: ID=" + compentries.getCeId() + ", Lane=" + compentries.getcELane());
	}
}
