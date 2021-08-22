package de.regatta_hd.aquarius.db;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import de.regatta_hd.common.ConfigService;

class TestConfigService implements ConfigService {

	@Override
	public String getProperty(String key) throws IOException {
		String property = System.getProperty(key);
		if (StringUtils.isBlank(property)) {
			property = System.getenv(key);
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
