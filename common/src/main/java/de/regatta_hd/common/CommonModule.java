package de.regatta_hd.common;

import com.google.inject.AbstractModule;

import de.regatta_hd.common.impl.ListenerManagerImpl;
import de.regatta_hd.common.impl.ConfigServiceImpl;

public class CommonModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(ConfigService.class).to(ConfigServiceImpl.class);
		bind(ListenerManager.class).to(ListenerManagerImpl.class);
	}
}
