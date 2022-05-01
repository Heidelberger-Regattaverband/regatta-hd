package de.regatta_hd.commons.core;

import java.io.IOException;

public interface ConfigService {

	// getter

	String getProperty(String key);

	boolean getBooleanProperty(String key);

	Integer getIntegerProperty(String key) throws NumberFormatException;

	// setter

	void setProperty(String key, String value) throws IOException;

	void setProperty(String key, int value) throws IOException;

	void setProperty(String key, boolean value) throws IOException;

	// cleanup

	void removeProperty(String key) throws IOException;
}
