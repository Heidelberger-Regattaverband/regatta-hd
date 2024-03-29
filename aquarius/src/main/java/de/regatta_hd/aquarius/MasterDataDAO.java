package de.regatta_hd.aquarius;

import java.io.InputStream;
import java.util.List;

import jakarta.xml.bind.JAXBException;

import de.regatta_hd.aquarius.model.AgeClass;
import de.regatta_hd.aquarius.model.Athlet;
import de.regatta_hd.aquarius.model.BoatClass;
import de.regatta_hd.aquarius.model.Club;
import de.regatta_hd.aquarius.model.LogRecord;
import de.regatta_hd.aquarius.model.Referee;
import de.regatta_hd.commons.core.concurrent.ProgressMonitor;

/**
 * Provides access to the master data of the Aquarius database.
 */
public interface MasterDataDAO {

	/**
	 * @param id the unique age class identifier.
	 * @return a {@link AgeClass age class} or <code>null</code> if not available.
	 */
	AgeClass getAgeClass(int id);

	/**
	 * @return a {@link List} with all known {@link AgeClass age classes}
	 */
	List<AgeClass> getAgeClasses();

	/**
	 * @param id the unique boat class identifier.
	 * @return a {@link BoatClass boat class} or <code>null</code> if not available.
	 */
	BoatClass getBoatClass(int id);

	/**
	 * @return a {@link List} with all known {@link BoatClass boat classes}
	 */
	List<BoatClass> getBoatClasses();

	Athlet getAthletViaExternalId(String extId);

	// clubs
	/**
	 * @return a {@link List} with all known {@link Club clubs}
	 */
	List<Club> getClubs();

	Club getClubViaExternalId(int extId);

	// log records

	List<LogRecord> getLogRecords(String hostName);

	int deleteLogRecords(String hostName);

	List<String> getHostNames();

	/**
	 * @return version of the Aquarius database or null if not available, e.g. DB connection closed.
	 */
	String getAquariusVersion();

	// referees

	/**
	 * @return a {@link List} with all available {@link Referee referees}.
	 */
	List<Referee> getReferees();

	int updateAllRefereesLicenceState(boolean licenceState);

	int importReferees(InputStream inpute, ProgressMonitor progress) throws JAXBException;

}
