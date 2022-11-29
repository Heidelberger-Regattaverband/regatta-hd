package de.regatta_hd.aquarius;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.ExecutionException;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import de.regatta_hd.aquarius.model.AgeClass;
import de.regatta_hd.aquarius.model.Crew;
import de.regatta_hd.aquarius.model.Heat;
import de.regatta_hd.aquarius.model.HeatRegistration;
import de.regatta_hd.aquarius.model.Race;
import de.regatta_hd.aquarius.model.Regatta;
import de.regatta_hd.aquarius.model.Registration;
import de.regatta_hd.aquarius.model.RegistrationLabel;
import de.regatta_hd.aquarius.model.Result;

@ExtendWith(BaseDBTest.class)
class AquariusDBTests extends BaseDBTest {

	private static final int regattaId = 4;

	private static RegattaDAO regattaDAO;

	private static MasterDataDAO masterData;

	@BeforeAll
	static void setUpBeforeClass() throws InterruptedException, ExecutionException {
		regattaDAO = injector.getInstance(RegattaDAO.class);
		masterData = injector.getInstance(MasterDataDAO.class);

		aquariusDb.getExecutor().submit(() -> {
			Regatta regatta = aquariusDb.getEntityManager().getReference(Regatta.class, Integer.valueOf(regattaId));
			assertEquals(regattaId, regatta.getId());
			assertNotNull(regatta);
			regattaDAO.setActiveRegatta(regatta);
		}).get();
	}

	@AfterAll
	static void tearDownAfterClass() {
		if (aquariusDb != null) {
			aquariusDb.close();
			aquariusDb = null;
		}
	}

	@Test
	void testGetVersion() {
		assertNotNull(aquariusDb.getVersion());
	}

	@Test
	void testGetOfficialHeats() throws InterruptedException, ExecutionException {
		List<ResultEntry> results = aquariusDb.getExecutor().submit(() -> {
			return regattaDAO.getOfficialResults();
		}).get();
		assertFalse(results.isEmpty());
	}

	@Test
	void testIsOpen() {
		assertTrue(aquariusDb.isOpen());
	}

	@Test
	void testGetEvents() throws InterruptedException, ExecutionException {
		List<Regatta> events = aquariusDb.getExecutor().submit(() -> {
			return regattaDAO.getRegattas();
		}).get();
		assertFalse(events.isEmpty());
	}

	@Test
	void testGetEventOK() throws InterruptedException, ExecutionException {
		Regatta regatta = aquariusDb.getExecutor().submit(() -> {
			return regattaDAO.getActiveRegatta();
		}).get();
		System.out.println(regatta.toString());

		Race offer = aquariusDb.getExecutor().submit(() -> {
			return regattaDAO.getRace("104", null);
		}).get();

		assertEquals("104", offer.getNumber());
		trace(offer, 1);
	}

	@Test
	void testGetEventFailed() throws InterruptedException, ExecutionException {
		Regatta regatta = aquariusDb.getExecutor().submit(() -> {
			return aquariusDb.getEntityManager().getReference(Regatta.class, Integer.valueOf(10));
		}).get();

		assertThrows(EntityNotFoundException.class, () -> {
			// as event with ID == 10 doesn't exist, calling any getter causes an
			// EntityNotFoundException
			regatta.getClub();
		});
	}

	@Test
	void testGetAgeClasses() throws InterruptedException, ExecutionException {
		aquariusDb.getExecutor().submit(() -> {
			List<AgeClass> ageClasses = masterData.getAgeClasses();
			assertFalse(ageClasses.isEmpty());

			AgeClass ageClass = ageClasses.get(0);
			assertEquals(1500, ageClass.getDistance());
		}).get();
	}

	// static helpers

	private static void trace(Race offer, int indent) {
		indent(indent);
		System.out.println(offer.toString());

		offer.getHeats().forEach(heat -> trace(heat, indent + 1));
		offer.getRegistrations().forEach(registration -> trace(registration, indent + 1));
	}

	private static void trace(Heat heat, int indent) {
		indent(indent);
		System.out.println(heat.toString());

		heat.getEntriesSortedByRank().forEach(compEntries -> trace(compEntries, indent + 1));
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
