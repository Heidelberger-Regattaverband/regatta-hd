package de.regatta_hd.common;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;

import de.regatta_hd.common.impl.ConfigServiceImpl;
import de.regatta_hd.common.impl.ListenerManagerImpl;

public class CommonModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(ConfigService.class).to(ConfigServiceImpl.class);
		bind(ListenerManager.class).to(ListenerManagerImpl.class);
	}

	@Provides
	@Named("hostName")
	public String getHostName() throws UnknownHostException {
		return InetAddress.getLocalHost().getHostName();
	}

	@Provides
	@Named("hostAddress")
	public String getHostAddress() throws UnknownHostException {
		return InetAddress.getLocalHost().getHostAddress();
	}
}
