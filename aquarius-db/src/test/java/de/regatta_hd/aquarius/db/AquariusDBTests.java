package de.regatta_hd.aquarius.db;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import de.regatta_hd.aquarius.db.AquariusDB;
import de.regatta_hd.aquarius.db.AquariusDBModule;
import de.regatta_hd.aquarius.db.DBConfig;
import de.regatta_hd.aquarius.db.DBConfigStore;
import de.regatta_hd.aquarius.db.MasterDataDAO;
import de.regatta_hd.aquarius.db.RegattaDAO;
import de.regatta_hd.aquarius.db.model.AgeClass;
import de.regatta_hd.aquarius.db.model.AgeClassExt;
import de.regatta_hd.aquarius.db.model.BoatClass;
import de.regatta_hd.aquarius.db.model.Crew;
import de.regatta_hd.aquarius.db.model.Heat;
import de.regatta_hd.aquarius.db.model.HeatRegistration;
import de.regatta_hd.aquarius.db.model.Offer;
import de.regatta_hd.aquarius.db.model.Regatta;
import de.regatta_hd.aquarius.db.model.Registration;
import de.regatta_hd.aquarius.db.model.RegistrationLabel;
import de.regatta_hd.aquarius.db.model.Result;
import jakarta.persistence.EntityNotFoundException;

class AquariusDBTests {

	private static AquariusDB aquariusDb;

	private static RegattaDAO regattaDAO;

	private static MasterDataDAO masterData;

	private static DBConfig connectionData;

	@BeforeAll
	static void setUpBeforeClass() throws IOException {
		com.google.inject.Module testModules = Modules.override(new AquariusDBModule()).with(new TestModule());
		Injector injector = Guice.createInjector(testModules);

		DBConfigStore connStore = injector.getInstance(DBConfigStore.class);
		connectionData = connStore.getLastSuccessful();

		aquariusDb = injector.getInstance(AquariusDB.class);
		aquariusDb.open(connectionData);

		regattaDAO = injector.getInstance(RegattaDAO.class);
		masterData = injector.getInstance(MasterDataDAO.class);
	}

	@AfterAll
	static void tearDownAfterClass() {
		if (aquariusDb != null) {
			aquariusDb.close();
			aquariusDb = null;
		}
	}

	@Test
	void testOpen() {
		aquariusDb.open(connectionData);
		Assertions.assertTrue(aquariusDb.isOpen());
	}

	@Test
	void testIsOpen() {
		Assertions.assertTrue(aquariusDb.isOpen());
	}

	@Test
	void testGetEvents() {
		List<Regatta> events = regattaDAO.getRegattas();
		Assertions.assertFalse(events.isEmpty());
	}

	@Test
	void testFindOffers() {
		BoatClass boatClass = masterData.getBoatClass(1);
		AgeClass ageClass = masterData.getAgeClass(11);

		Regatta event = aquariusDb.getEntityManager().getReference(Regatta.class, 3);
		regattaDAO.setActiveRegatta(event);
		List<Offer> offers = regattaDAO.findOffers("1%", boatClass, ageClass, true);
		Assertions.assertTrue(offers.isEmpty());

		offers.forEach(offer -> trace(offer, 0));
	}

	@Test
	void testGetEventOK() {
		Regatta regatta = aquariusDb.getEntityManager().getReference(Regatta.class, 2);
		Assertions.assertEquals(2, regatta.getId());
		Assertions.assertNotNull(regatta);

		System.out.println(regatta.toString());
		regattaDAO.setActiveRegatta(regatta);
		Offer offer = regattaDAO.getOffer("104");
		Assertions.assertEquals("104", offer.getRaceNumber());

		trace(offer, 1);
	}

	@Test
	void testGetEventFailed() {
		Regatta regatta = aquariusDb.getEntityManager().getReference(Regatta.class, 10);
		Assertions.assertThrows(EntityNotFoundException.class, () -> {
			// as event with ID == 10 doesn't exist, calling any getter causes an
			// EntityNotFoundException
			regatta.getClub();
		});
	}

	@Test
	void testGetAgeClasses() {
		List<AgeClass> ageClasses = masterData.getAgeClasses();
		Assertions.assertFalse(ageClasses.isEmpty());

		AgeClass ageClass = ageClasses.get(0);
		AgeClassExt ageClassExt = ageClass.getExtension();
	}

	private static void trace(Offer offer, int indent) {
		indent(indent);
		System.out.println(offer.toString());

		offer.getHeats().forEach(heat -> trace(heat, indent + 1));
		offer.getRegistrations().forEach(registration -> trace(registration, indent + 1));
	}

	private static void trace(Heat heat, int indent) {
		indent(indent);
		System.out.println(heat.toString());

		heat.getHeatRegistrationsOrderedByRank().forEach(compEntries -> trace(compEntries, indent + 1));
	}

	private static void trace(Registration registration, int indent) {
		indent(indent);
		System.out.println(registration.toString());
		registration.getCrews().forEach(crew -> trace(crew, indent + 1));
		registration.getLabels().forEach(label -> trace(label, indent + 1));
	}

	private static void trace(Crew crew, int indent) {
		indent(indent);
		System.out.println(crew.toString());
	}

	private static void trace(RegistrationLabel registrationLabel, int indent) {
		indent(indent);
		System.out.println(registrationLabel.toString());
	}

	private static void trace(HeatRegistration heatEntry, int indent) {
		indent(indent);
		System.out.println(heatEntry.toString());
		trace(heatEntry.getRegistration(), indent + 1);
		heatEntry.getResults().forEach(result -> trace(result, indent + 1));
	}

	private static void trace(Result result, int indent) {
		indent(indent);
		System.out.println(result.toString());
	}

	private static void indent(int depth) {
		for (int i = 0; i < depth; i++) {
			System.out.print("\t");
		}
	}
}
