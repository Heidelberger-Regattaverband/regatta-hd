package de.regatta_hd.aquarius;

import com.google.inject.AbstractModule;

import de.regatta_hd.aquarius.impl.AquariusDBImpl;
import de.regatta_hd.aquarius.impl.MasterDataDAOImpl;
import de.regatta_hd.aquarius.impl.RegattaDAOImpl;
import de.regatta_hd.commons.db.CommonsDBModule;
import de.regatta_hd.commons.db.DBConnection;

/**
 * The guice module to register aquarius database bindings and additional services.
 */
public class AquariusModule extends AbstractModule {

	@Override
	protected void configure() {
		install(new CommonsDBModule());

		bind(DBConnection.class).to(AquariusDBImpl.class);
		bind(RegattaDAO.class).to(RegattaDAOImpl.class);
		bind(MasterDataDAO.class).to(MasterDataDAOImpl.class);
		bind(DBLogHandler.class);
	}
}
