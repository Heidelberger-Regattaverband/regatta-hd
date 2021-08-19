package de.regatta_hd.common;

import java.io.IOException;

public interface ConfigService {

	String getProperty(String key) throws IOException;

	void setProperty(String key, String value) throws IOException;
}
