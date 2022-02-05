package de.regatta_hd.common;

import com.google.inject.AbstractModule;

import de.regatta_hd.common.impl.ActionListenerManagerImpl;
import de.regatta_hd.common.impl.ConfigServiceImpl;

public class CommonModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(ConfigService.class).to(ConfigServiceImpl.class);
		bind(ActionListenerManager.class).to(ActionListenerManagerImpl.class);
	}
}
