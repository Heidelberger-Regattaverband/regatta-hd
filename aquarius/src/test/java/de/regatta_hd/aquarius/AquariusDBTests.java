package de.regatta_hd.aquarius;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

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
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(BaseDBTest.class)
class AquariusDBTests extends BaseDBTest {

	private static final int regattaId = 4;

	private static RegattaDAO regattaDAO;

	private static MasterDataDAO masterData;

	@BeforeAll
	static void setUpBeforeClass() {
		regattaDAO = injector.getInstance(RegattaDAO.class);
		masterData = injector.getInstance(MasterDataDAO.class);

		aquariusDb.getExecutor().execute(() -> {
			Regatta regatta = aquariusDb.getEntityManager().getReference(Regatta.class, Integer.valueOf(regattaId));
			assertEquals(regattaId, regatta.getId());
			assertNotNull(regatta);
			regattaDAO.setActiveRegatta(regatta);
		});
	}

	@AfterAll
	static void tearDownAfterClass() {
		if (aquariusDb != null) {
			aquariusDb.close();
			aquariusDb = null;
		}
	}

	@Test
	void testGetOfficialHeats() {
		aquariusDb.getExecutor().execute(() -> {
			List<ResultEntry> results = regattaDAO.getOfficialResults();
			assertFalse(results.isEmpty());
		});
	}

	@Test
	void testIsOpen() {
		aquariusDb.getExecutor().execute(() -> {
			assertTrue(aquariusDb.isOpen());
		});
	}

	@Test
	void testGetEvents() {
		aquariusDb.getExecutor().execute(() -> {
			List<Regatta> events = regattaDAO.getRegattas();
			assertFalse(events.isEmpty());
		});
	}

	@Test
	void testGetEventOK() {
		aquariusDb.getExecutor().execute(() -> {
			Regatta regatta = regattaDAO.getActiveRegatta();
			System.out.println(regatta.toString());

			Race offer = regattaDAO.getRace("104");
			assertEquals("104", offer.getNumber());
			trace(offer, 1);
		});
	}

	@Test
	void testGetEventFailed() {
		aquariusDb.getExecutor().execute(() -> {
			Regatta regatta = aquariusDb.getEntityManager().getReference(Regatta.class, Integer.valueOf(10));
			assertThrows(EntityNotFoundException.class, () -> {
				// as event with ID == 10 doesn't exist, calling any getter causes an
				// EntityNotFoundException
				regatta.getClub();
			});
		});
	}

	@Test
	void testGetAgeClasses() {
		aquariusDb.getExecutor().execute(() -> {
			List<AgeClass> ageClasses = masterData.getAgeClasses();
			assertFalse(ageClasses.isEmpty());

			AgeClass ageClass = ageClasses.get(0);
			assertEquals(1500, ageClass.getDistance());
		});
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
