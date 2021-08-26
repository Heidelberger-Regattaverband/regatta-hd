package de.regatta_hd.aquarius.db;

import com.google.inject.AbstractModule;

import de.regatta_hd.common.ConfigService;

class TestModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(ConfigService.class).to(TestConfigService.class);
	}
}
