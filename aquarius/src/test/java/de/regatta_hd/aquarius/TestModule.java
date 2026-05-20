package de.regatta_hd.aquarius;

import java.io.IOException;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import de.regatta_hd.commons.core.ConfigService;

class TestModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(ConfigService.class).to(TestConfigService.class);

		try {
			bind(ConfigService.class).annotatedWith(Names.named("default")).toInstance(ConfigService.createDefault());
		} catch (IOException e) {
			addError(e);
		}
	}
}
