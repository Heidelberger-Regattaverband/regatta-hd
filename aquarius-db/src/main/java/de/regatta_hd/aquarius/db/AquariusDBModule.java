package de.regatta_hd.aquarius.db;

import com.google.inject.AbstractModule;

public class AquariusDBModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(AquariusDB.class).to(AqauriusDBImpl.class);
	}
}
