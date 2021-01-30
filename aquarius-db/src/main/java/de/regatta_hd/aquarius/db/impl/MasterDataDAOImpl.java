package de.regatta_hd.aquarius.db.impl;

import java.util.List;

import com.google.inject.Singleton;

import de.regatta_hd.aquarius.db.MasterDataDAO;
import de.regatta_hd.aquarius.db.model.AgeClass;
import de.regatta_hd.aquarius.db.model.BoatClass;
import de.regatta_hd.aquarius.db.model.Club;

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
		return getEntity(AgeClass.class, id);
	}

	@Override
	public BoatClass getBoatClass(int id) {
		return getEntity(BoatClass.class, id);
	}
}
