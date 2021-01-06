package de.regatta_hd.aquarius;

import java.util.List;

import javax.persistence.EntityNotFoundException;
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

import com.google.inject.Guice;
import com.google.inject.Injector;

import de.regatta_hd.aquarius.db.AquariusDB;
import de.regatta_hd.aquarius.db.AquariusDBModule;
import de.regatta_hd.aquarius.db.EventDAO;
import de.regatta_hd.aquarius.db.MasterDataDAO;
import de.regatta_hd.aquarius.db.model.AgeClass;
import de.regatta_hd.aquarius.db.model.Comp;
import de.regatta_hd.aquarius.db.model.CompEntries;
import de.regatta_hd.aquarius.db.model.Entry;
import de.regatta_hd.aquarius.db.model.Event;
import de.regatta_hd.aquarius.db.model.EventId;
import de.regatta_hd.aquarius.db.model.Offer;

class AquariusDBTests {

	private static final String PASSWORD = "regatta";

	private static final String USER_NAME = "sa";

	private static final String DB_NAME = "rudern";

	private static final String HOST_NAME = "192.168.0.130";
	
	private static AquariusDB aquariusDb;

	private static EventDAO eventDAO;

	private static MasterDataDAO masterData;

	@BeforeAll
	static void setUpBeforeClass() {
		Injector injector = Guice.createInjector(new AquariusDBModule());
		aquariusDb = injector.getInstance(AquariusDB.class);
		aquariusDb.open(HOST_NAME, DB_NAME, USER_NAME, PASSWORD);
		
		eventDAO = injector.getInstance(EventDAO.class);
		masterData = injector.getInstance(MasterDataDAO.class);
	}

	@AfterAll
	static void tearDownAfterClass() {
		if (aquariusDb != null) {
			aquariusDb.close();
			aquariusDb = null;
		}
	}

	@BeforeEach
	void setUp() {
	}

	@AfterEach
	void tearDown() {
	}

	@Test
	void testOpen() {
		aquariusDb.open(HOST_NAME, DB_NAME, USER_NAME, PASSWORD);
	}

	@Test
	void testIsOpen() {
		Assertions.assertTrue(aquariusDb.isOpen());
	}

	@Test
	void testGetEvents() {
		List<Event> events = eventDAO.getEvents();
		Assertions.assertFalse(events.isEmpty());
	}
	
	@Test
	
	void testGetEventOK() {
		EventId id = new EventId("1");
		Event event = aquariusDb.getEntityManager().getReference(Event.class, id);
		Assertions.assertEquals(id.eventID, event.getEventID());
		Assertions.assertNotNull(event);

		System.out.println(event.toString());
		
		event.getOffers().forEach(this::trace);
//		event.getComps().forEach(this::trace);
//		event.getEntrys().forEach(this::trace);
	}

	@Test
	void testGetEventFailed() {
		Assertions.assertThrows(EntityNotFoundException.class, () -> {
			EventId id = new EventId("2");
			Event event = aquariusDb.getEntityManager().getReference(Event.class, id);
			// as event with ID == 2 doesn't exist, calling any getter causes an
			// EntityNotFoundException
			event.getEventID();
		});
	}

	@Test
	@Disabled
	void testPokal() {
		CriteriaBuilder builder = aquariusDb.getEntityManager().getCriteriaBuilder();
		CriteriaQuery<Offer> cq = builder.createQuery(Offer.class);
		Root<Offer> from = cq.from(Offer.class);
		cq.select(from);

		TypedQuery<Offer> query = aquariusDb.getEntityManager().createQuery(cq);
		List<Offer> resultList = query.getResultList();

		resultList.forEach(this::trace);
	}

	@Test
	void testGetAgeClasses() {
		List<AgeClass> ageClasses = masterData.getAgeClasses();
		Assertions.assertFalse(ageClasses.isEmpty());
	}
	
	private void trace(Offer offer) {
		System.out.println(offer.toString());
		offer.getComps().forEach(this::trace);
	}

	private void trace(Comp comp) {
//		Label label = entityManager.getReference(Label.class, new LabelId(comp.getCompLabel()));
		System.out.println("\t\tComp: ID=" + comp.getCompID() + ", Label=" + comp.getCompLabel() + ", Round="
				+ comp.getCompRound() + ", HeatNr=" + comp.getCompHeatNumber() + ", Number=" + comp.getCompNumber());
		comp.getCompEntries().forEach(this::trace);
	}

	private void trace(Entry entry) {
		System.out.println(
				"\t\t\tEntry: ID=" + entry.getEntryID() + ": " + entry.getLabel() + ", " + entry.getEntryComment());
	}

	private void trace(CompEntries compentries) {
		System.out.println("\t\tCompEntries: ID=" + compentries.getCeId() + ", Lane=" + compentries.getcELane());
		trace(compentries.getEntry());
	}
}
