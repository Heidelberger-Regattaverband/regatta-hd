package de.regatta_hd.commons.db;

import com.google.inject.AbstractModule;

import de.regatta_hd.commons.core.CommonsCoreModule;
import de.regatta_hd.commons.db.impl.DBConfigStoreImpl;

/**
 * The guice module to register common database bindings and additional services.
 */
public class CommonsDBModule extends AbstractModule {

	@Override
	protected void configure() {
		install(new CommonsCoreModule());

		bind(DBConfigStore.class).to(DBConfigStoreImpl.class);
	}
}
