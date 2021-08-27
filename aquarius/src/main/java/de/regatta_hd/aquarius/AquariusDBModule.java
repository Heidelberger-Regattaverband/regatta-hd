package de.regatta_hd.aquarius;

import com.google.inject.AbstractModule;

import de.regatta_hd.aquarius.impl.AqauriusDBImpl;
import de.regatta_hd.aquarius.impl.DBConfigStoreImpl;
import de.regatta_hd.aquarius.impl.MasterDataDAOImpl;
import de.regatta_hd.aquarius.impl.RegattaDAOImpl;
import de.regatta_hd.common.CommonModule;

/**
 * The guice module to register aquarius database bindings and additional
 * services.
 */
public class AquariusDBModule extends AbstractModule {

	@Override
	protected void configure() {
		install(new CommonModule());

		bind(AquariusDB.class).to(AqauriusDBImpl.class);
		bind(RegattaDAO.class).to(RegattaDAOImpl.class);
		bind(MasterDataDAO.class).to(MasterDataDAOImpl.class);
		bind(DBConfigStore.class).to(DBConfigStoreImpl.class);
	}
}
