package de.regatta_hd.aquarius.impl;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

import com.google.inject.Singleton;

import de.regatta_hd.aquarius.MasterDataDAO;
import de.regatta_hd.aquarius.model.AgeClass;
import de.regatta_hd.aquarius.model.BoatClass;
import de.regatta_hd.aquarius.model.Club;
import de.regatta_hd.aquarius.model.LogRecord;
import de.regatta_hd.aquarius.model.Referee;
import de.regatta_hd.commons.core.concurrent.ProgressMonitor;
import de.regatta_hd.schemas.xml.XMLDataLoader;
import de.rudern.schemas.service.wettkampfrichter._2017.Liste;
import de.rudern.schemas.service.wettkampfrichter._2017.TWKR;
import jakarta.persistence.EntityManager;
import jakarta.xml.bind.JAXBException;

@Singleton
public class MasterDataDAOImpl extends AbstractDAOImpl implements MasterDataDAO {

	@Override
	public List<AgeClass> getAgeClasses() {
		return getEntities(AgeClass.class);
	}

	@Override
	public List<BoatClass> getBoatClasses() {
		return getEntities(BoatClass.class);
	}

	@Override
	public List<Club> getClubs() {
		return getEntities(Club.class);
	}

	@Override
	public AgeClass getAgeClass(int id) {
		return super.db.getEntityManager().find(AgeClass.class, Integer.valueOf(id));
	}

	@Override
	public BoatClass getBoatClass(int id) {
		return super.db.getEntityManager().find(BoatClass.class, Integer.valueOf(id));
	}

	@Override
	public List<LogRecord> getLogRecords(String hostName) {
		return super.db.getEntityManager()
				.createQuery("SELECT lr FROM LogRecord lr WHERE lr.hostName = :hostName ORDER BY lr.instant DESC",
						LogRecord.class)
				.setParameter("hostName", hostName).getResultList();
	}

	@Override
	public List<String> getHostNames() {
		return super.db.getEntityManager().createQuery("SELECT DISTINCT lr.hostName FROM LogRecord lr", String.class)
				.getResultList();
	}

	@Override
	public String getAquariusVersion() {
		return super.db.getVersion();
	}

	@Override
	public List<Referee> getReferees() {
		return getEntities(Referee.class);
	}

	@Override
	public int updateAllRefereesLicenceState(boolean licenceState) {
		return super.db.getEntityManager().createQuery("UPDATE Referee SET licenceState = :licenceState")
				.setParameter("licenceState", Boolean.valueOf(licenceState)).executeUpdate();
	}

	@Override
	public int importReferees(InputStream input, ProgressMonitor progress) throws JAXBException {
		Liste referees = XMLDataLoader.loadWkrListe(input);
		if (referees != null) {
			EntityManager entityManager = super.db.getEntityManager();

			List<TWKR> twkrs = referees.getWettkampfrichter();
			Integer maxWork = Integer.valueOf(twkrs.size());

			for (int i = 0; i < twkrs.size(); i++) {
				TWKR twkr = twkrs.get(i);
				progress.update(i, twkrs.size(), MessageFormat.format("Importiere Wettkampfrichter {0} von {1}.",
						Integer.valueOf(i + 1), maxWork));

				Optional<Referee> refereeOpt = entityManager
						.createQuery("SELECT r FROM Referee r WHERE r.externID = :externID", Referee.class)
						.setParameter("externID", twkr.getLizenznummer()).getResultStream().findFirst();

				Referee referee = null;
				if (refereeOpt.isPresent()) {
					referee = refereeOpt.get();
					referee.setFirstName(twkr.getVorname());
					referee.setLastName(twkr.getNachname());
					referee.setCity(twkr.getOrt());
				} else {
					referee = Referee.builder().city(twkr.getOrt()).externID(twkr.getLizenznummer())
							.firstName(twkr.getVorname()).lastName(twkr.getVorname()).build();
				}
				entityManager.merge(referee);
			}
			entityManager.flush();

			return twkrs.size();
		}
		return 0;
	}
}
