package de.regatta_hd.aquarius;

import com.google.inject.AbstractModule;

import de.regatta_hd.commons.core.ConfigService;
import de.regatta_hd.commons.core.impl.ConfigServiceImpl;

class TestModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(ConfigService.class).to(TestConfigService.class);

		bind(ConfigServiceImpl.class).toInstance(new ConfigServiceImpl());
	}
}
