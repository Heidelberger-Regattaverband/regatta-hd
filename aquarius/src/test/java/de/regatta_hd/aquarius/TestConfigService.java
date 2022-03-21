package de.regatta_hd.aquarius;

import java.io.IOException;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

import de.regatta_hd.commons.ConfigService;
import de.regatta_hd.commons.impl.ConfigServiceImpl;

class TestConfigService implements ConfigService {

	@Inject
	private ConfigServiceImpl cfgService;

	@Override
	public String getProperty(String key) throws IOException {
		String property = System.getProperty(key);
		if (StringUtils.isBlank(property)) {
			property = System.getenv(key);
			if (StringUtils.isBlank(property)) {
				String envKey = key.replaceAll("([A-Z])", "_$1").toUpperCase(Locale.ENGLISH);
				property = System.getenv(envKey);
				if (StringUtils.isBlank(property)) {
					property = this.cfgService.getProperty(key);
				}
			}
		}
		return property;
	}

	@Override
	public boolean getBooleanProperty(String key) throws IOException {
		return Boolean.parseBoolean(getProperty(key));
	}

	@Override
	public Integer getIntegerProperty(String key) throws IOException {
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
