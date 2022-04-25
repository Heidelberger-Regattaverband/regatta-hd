package de.regatta_hd.commons.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;

import de.regatta_hd.commons.core.impl.ConfigServiceImpl;
import de.regatta_hd.commons.core.impl.ListenerManagerImpl;

public class CommonsCoreModule extends AbstractModule {
	private static final Logger logger = Logger.getLogger(CommonsCoreModule.class.getName());

	private final Properties properties = new Properties();

	@Override
	protected void configure() {
		bind(ConfigService.class).to(ConfigServiceImpl.class);
		bind(ListenerManager.class).to(ListenerManagerImpl.class);

		try (InputStream input = CommonsCoreModule.class.getResourceAsStream("/project.properties")) {
			this.properties.load(input);
		} catch (IOException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
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

	@Provides
	@Named("version")
	public String getVersion() {
		return this.properties.getProperty("version");
	}

}
