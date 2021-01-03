package de.regatta_hd.aquarius.db;

import java.util.List;

import de.regatta_hd.aquarius.db.model.AgeClass;
import de.regatta_hd.aquarius.db.model.BoatClass;
import de.regatta_hd.aquarius.db.model.Club;

public interface MasterDataDAO {

	List<AgeClass> getAgeClasses();

	List<BoatClass> getBoatClasses();

	List<Club> getClubs();
}
