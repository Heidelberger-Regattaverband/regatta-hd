package de.regatta_hd.aquarius;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

import de.regatta_hd.commons.core.ConfigService;
import de.regatta_hd.commons.core.impl.ConfigServiceImpl;

class TestConfigService implements ConfigService {

	@Inject
	private ConfigServiceImpl cfgService;

	@Override
	public String getProperty(String key) {
		// 1. try to get it from config service
		String property = this.cfgService.getProperty(key);

		// 2. try to get it from system property (command line argument -D....)
		if (StringUtils.isBlank(property)) {
			property = System.getProperty(key);

			// 3. try to get it from environment variable
			if (StringUtils.isBlank(property)) {
				property = System.getenv(key);
				if (StringUtils.isBlank(property)) {
					String envKey = key.replaceAll("([A-Z])", "_$1").toUpperCase(Locale.ENGLISH);
					property = System.getenv(envKey);
				}
			}
		}
		return property;
	}

	@Override
	public boolean getBooleanProperty(String key) {
		return Boolean.parseBoolean(getProperty(key));
	}

	@Override
	public Integer getIntegerProperty(String key) {
		String property = getProperty(key);
		return property != null ? Integer.valueOf(property) : null;
	}

	@Override
	public void setProperty(String key, String value) {
		// not implemented
	}

	@Override
	public void setProperty(String key, int value) {
		// not implemented
	}

	@Override
	public void removeProperty(String key) {
		// not implemented
	}

	@Override
	public void setProperty(String key, boolean value) {
		// not implemented
	}
}
