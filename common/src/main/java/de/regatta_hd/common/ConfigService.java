package de.regatta_hd.common;

import java.io.IOException;

public interface ConfigService {

	// getter

	String getProperty(String key) throws IOException;

	boolean getBooleanProperty(String key) throws IOException;

	int getIntegerProperty(String key) throws IOException;

	// setter

	void setProperty(String key, String value) throws IOException;

	void setProperty(String key, int value) throws IOException;

	void setProperty(String key, boolean value) throws IOException;

	// cleanup

	void removeProperty(String key) throws IOException;
}
