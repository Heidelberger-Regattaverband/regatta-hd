package de.regatta_hd.aquarius.impl;

import java.util.List;

import com.google.inject.Singleton;

import de.regatta_hd.aquarius.MasterDataDAO;
import de.regatta_hd.aquarius.model.AgeClass;
import de.regatta_hd.aquarius.model.BoatClass;
import de.regatta_hd.aquarius.model.Club;
import de.regatta_hd.aquarius.model.LogRecord;
import de.regatta_hd.aquarius.model.Referee;

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
}
