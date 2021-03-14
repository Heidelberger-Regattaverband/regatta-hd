package de.regatta_hd.aquarius;

import java.io.IOException;
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
import de.regatta_hd.aquarius.db.DBConfiguration;
import de.regatta_hd.aquarius.db.DBConfigurationStore;
import de.regatta_hd.aquarius.db.RegattaDAO;
import de.regatta_hd.aquarius.db.MasterDataDAO;
import de.regatta_hd.aquarius.db.model.AgeClass;
import de.regatta_hd.aquarius.db.model.BoatClass;
import de.regatta_hd.aquarius.db.model.Heat;
import de.regatta_hd.aquarius.db.model.HeatRegistration;
import de.regatta_hd.aquarius.db.model.Crew;
import de.regatta_hd.aquarius.db.model.Registration;
import de.regatta_hd.aquarius.db.model.RegistrationLabel;
import de.regatta_hd.aquarius.db.model.Regatta;
import de.regatta_hd.aquarius.db.model.Offer;
import de.regatta_hd.aquarius.db.model.Result;

class AquariusDBTests {

	private static AquariusDB aquariusDb;

	private static RegattaDAO eventDAO;

	private static MasterDataDAO masterData;

	private static DBConfiguration connectionData;

	@BeforeAll
	static void setUpBeforeClass() throws IOException {
		Injector injector = Guice.createInjector(new AquariusDBModule());

		DBConfigurationStore connStore = injector.getInstance(DBConfigurationStore.class);
		connectionData = connStore.getLastSuccessful();

		aquariusDb = injector.getInstance(AquariusDB.class);
		aquariusDb.open(connectionData);

		eventDAO = injector.getInstance(RegattaDAO.class);
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
		aquariusDb.open(connectionData);
	}

	@Test
	void testIsOpen() {
		Assertions.assertTrue(aquariusDb.isOpen());
	}

	@Test
	void testGetEvents() {
		List<Regatta> events = eventDAO.getRegattas();
		Assertions.assertFalse(events.isEmpty());
	}

	@Test
	void testFindOffers() {
		BoatClass boatClass = masterData.getBoatClass(1);
		AgeClass ageClass = masterData.getAgeClass(11);

		Regatta event = aquariusDb.getEntityManager().getReference(Regatta.class, 1);
		List<Offer> offers = eventDAO.findOffers(event, boatClass, ageClass, true);
		Assertions.assertFalse(offers.isEmpty());

		offers.forEach(offer -> trace(offer, 0));
	}

	@Test
	void testGetEventOK() {
		Regatta event = aquariusDb.getEntityManager().getReference(Regatta.class, 1);
		Assertions.assertEquals(1, event.getId());
		Assertions.assertNotNull(event);

		System.out.println(event.toString());

		Offer offer = eventDAO.getOffer(event, "104");
		Assertions.assertEquals("104", offer.getRaceNumber());

		trace(offer, 1);
	}

	@Test
	void testGetEventFailed() {
		Assertions.assertThrows(EntityNotFoundException.class, () -> {
			Regatta event = aquariusDb.getEntityManager().getReference(Regatta.class, 10);
			// as event with ID == 10 doesn't exist, calling any getter causes an
			// EntityNotFoundException
			event.getClub();
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

		offer.getHeats().forEach(heat -> trace(heat, indent + 1));
		offer.getRegistrations().forEach(registration -> trace(registration, indent + 1));
	}

	private void trace(Heat heat, int indent) {
		indent(indent);
		System.out.println(heat.toString());

		heat.getHeatRegistrationsOrderedByRank().forEach(compEntries -> trace(compEntries, indent + 1));
	}

	private void trace(Registration registration, int indent) {
		indent(indent);
		System.out.println(registration.toString());
		registration.getCrews().forEach(crew -> trace(crew, indent + 1));
		registration.getLabels().forEach(label -> trace(label, indent + 1));
	}

	private void trace(Crew crew, int indent) {
		indent(indent);
		System.out.println(crew.toString());
	}

	private void trace(RegistrationLabel registrationLabel, int indent) {
		indent(indent);
		System.out.println(registrationLabel.toString());
	}

	private void trace(HeatRegistration heatEntry, int indent) {
		indent(indent);
		System.out.println(heatEntry.toString());
		trace(heatEntry.getRegistration(), indent + 1);
		heatEntry.getResults().forEach(result -> trace(result, indent + 1));
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
