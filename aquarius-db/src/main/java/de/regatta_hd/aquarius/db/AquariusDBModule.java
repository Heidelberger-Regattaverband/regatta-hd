package de.regatta_hd.aquarius.db;

import com.google.inject.AbstractModule;

import de.regatta_hd.aquarius.db.impl.AqauriusDBImpl;
import de.regatta_hd.aquarius.db.impl.DBConfigurationStoreImpl;
import de.regatta_hd.aquarius.db.impl.MasterDataDAOImpl;
import de.regatta_hd.aquarius.db.impl.RegattaDAOImpl;

/**
 * The guice module to register aquarius database bindings and additional
 * services.
 */
public class AquariusDBModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(AquariusDB.class).to(AqauriusDBImpl.class);
		bind(RegattaDAO.class).to(RegattaDAOImpl.class);
		bind(MasterDataDAO.class).to(MasterDataDAOImpl.class);
		bind(DBConfigurationStore.class).to(DBConfigurationStoreImpl.class);
	}
}
