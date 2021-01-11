package de.regatta_hd.aquarius;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import de.regatta_hd.aquarius.db.AquariusDB;
import de.regatta_hd.aquarius.db.AquariusDBModule;
import de.regatta_hd.aquarius.db.EventDAO;
import de.regatta_hd.aquarius.db.MasterDataDAO;
import de.regatta_hd.aquarius.db.model.AgeClass;
import de.regatta_hd.aquarius.db.model.BoatClass;
import de.regatta_hd.aquarius.db.model.Comp;
import de.regatta_hd.aquarius.db.model.CompEntries;
import de.regatta_hd.aquarius.db.model.Crew;
import de.regatta_hd.aquarius.db.model.Entry;
import de.regatta_hd.aquarius.db.model.EntryLabel;
import de.regatta_hd.aquarius.db.model.Event;
import de.regatta_hd.aquarius.db.model.EventId;
import de.regatta_hd.aquarius.db.model.Offer;
import de.regatta_hd.aquarius.db.model.Result;

class AquariusDBTests {

	private static final String PASSWORD = "regatta";

	private static final String USER_NAME = "sa";

	private static final String DB_NAME = "rudern";

	private static final String HOST_NAME = "192.168.3.104";

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
	void testFindOffers() {
		BoatClass boatClass = masterData.getBoatClass(1);
		AgeClass ageClass = masterData.getAgeClass(11);

		Event event = aquariusDb.getEntityManager().getReference(Event.class, new EventId(1));
		List<Offer> offers = eventDAO.findOffers(event, boatClass, ageClass, true);
		Assertions.assertFalse(offers.isEmpty());

		offers.forEach(offer -> trace(offer, 0));
	}

	@Test
	void testGetEventOK() {
		EventId id = new EventId(1);
		Event event = aquariusDb.getEntityManager().getReference(Event.class, id);
		Assertions.assertEquals(id.getId(), event.getId());
		Assertions.assertNotNull(event);

		System.out.println(event.toString());

		Offer offer = eventDAO.getOffer(event, "104");
		Assertions.assertEquals("104", offer.getRaceNumber());

		trace(offer, 1);
	}

	@Test
	void testGetEventFailed() {
		Assertions.assertThrows(EntityNotFoundException.class, () -> {
			EventId id = new EventId(2);
			Event event = aquariusDb.getEntityManager().getReference(Event.class, id);
			// as event with ID == 2 doesn't exist, calling any getter causes an
			// EntityNotFoundException
			event.getId();
		});
	}

	@Test
	void testGetAgeClasses() {
		List<AgeClass> ageClasses = masterData.getAgeClasses();
		Assertions.assertFalse(ageClasses.isEmpty());
	}

	private void trace(Offer offer, int indent) {
		indent(indent);
		System.out.println(offer.toString());

		offer.getComps().forEach(comp -> trace(comp, indent + 1));
		offer.getEntrys().forEach(entry -> trace(entry, indent + 1));
	}

	private void trace(Comp comp, int indent) {
		indent(indent);
		System.out.println(comp.toString());

		comp.getCompEntries().forEach(compEntries -> trace(compEntries, indent + 1));
	}

	private void trace(Entry entry, int indent) {
		indent(indent);
		System.out.println(entry.toString());
		entry.getCrews().forEach(crew -> trace(crew, indent + 1));
		entry.getLabels().forEach(label -> trace(label, indent + 1));
	}

	private void trace(Crew crew, int indent) {
		indent(indent);
		System.out.println(crew.toString());
	}

	private void trace(EntryLabel entryLabel, int indent) {
		indent(indent);
		System.out.println(entryLabel.toString());
	}

	private void trace(CompEntries compEntries, int indent) {
		indent(indent);
		System.out.println(compEntries.toString());
		trace(compEntries.getEntry(), indent + 1);
		compEntries.getResults().forEach(result -> trace(result, indent + 1));
	}

	private void trace(Result result, int indent) {
		indent(indent);
		System.out.println(result.toString());
	}

	private static void indent(int depth) {
		for (int i = 0; i < depth; i++) {
			System.out.print("\t");
		}
	}
}
