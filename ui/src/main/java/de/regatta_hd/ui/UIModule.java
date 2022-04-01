package de.regatta_hd.ui;

import com.google.inject.AbstractModule;

import de.regatta_hd.ui.util.DBTaskRunner;

/**
 * The guice module to register aquarius database bindings and additional services.
 */
public class UIModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(DBTaskRunner.class);
	}
}
