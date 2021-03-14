package de.regatta_hd.aquarius.db;

import com.google.inject.AbstractModule;

import de.regatta_hd.aquarius.db.impl.AqauriusDBImpl;
import de.regatta_hd.aquarius.db.impl.DBConfigurationStoreImpl;
import de.regatta_hd.aquarius.db.impl.RegattaDAOImpl;
import de.regatta_hd.aquarius.db.impl.MasterDataDAOImpl;

public class AquariusDBModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(AquariusDB.class).to(AqauriusDBImpl.class);
		bind(RegattaDAO.class).to(RegattaDAOImpl.class);
		bind(MasterDataDAO.class).to(MasterDataDAOImpl.class);
		bind(DBConfigurationStore.class).to(DBConfigurationStoreImpl.class);
	}
}
