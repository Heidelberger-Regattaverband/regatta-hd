package de.regatta_hd.aquarius.db;

import java.io.IOException;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import de.regatta_hd.common.ConfigService;

class TestConfigService implements ConfigService {

	@Override
	public String getProperty(String key) throws IOException {
		String property = System.getProperty(key);
		if (StringUtils.isBlank(property)) {
			property = System.getenv(key);
			if (StringUtils.isBlank(property)) {
				String envKey = key.replaceAll("([A-Z])", "_$1").toUpperCase(Locale.ENGLISH);
				property = System.getenv(envKey);
			}
		}
		return property;
	}

	@Override
	public void setProperty(String key, String value) throws IOException {
		// not implemented
	}

	@Override
	public void setProperty(String key, int value) throws IOException {
		// not implemented
	}

	@Override
	public void removeProperty(String key) throws IOException {
		// not implemented
	}
}
