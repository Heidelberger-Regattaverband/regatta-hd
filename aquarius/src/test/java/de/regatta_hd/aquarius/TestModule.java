package de.regatta_hd.aquarius;

import com.google.inject.AbstractModule;

import de.regatta_hd.common.ConfigService;
import de.regatta_hd.common.impl.ConfigServiceImpl;

class TestModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(ConfigService.class).to(TestConfigService.class);

		bind(ConfigServiceImpl.class).toInstance(new ConfigServiceImpl());
	}
}
