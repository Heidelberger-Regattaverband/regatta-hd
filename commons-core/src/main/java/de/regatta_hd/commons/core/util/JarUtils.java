package de.regatta_hd.commons.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JarUtils {

	private static final Logger logger = Logger.getLogger(JarUtils.class.getName());

	private JarUtils() {
		// private to avoid instances
	}

	public static List<Manifest> getManifests() {
		List<Manifest> manifests = new ArrayList<>();

		try {
			Enumeration<URL> resEnum = Thread.currentThread().getContextClassLoader()
					.getResources(JarFile.MANIFEST_NAME);
			while (resEnum.hasMoreElements()) {
				URL url = resEnum.nextElement();

				try (InputStream is = url.openStream()) { // NOSONAR
					manifests.add(new Manifest(is));
				} catch (Exception e) {
					logger.log(Level.FINEST, e.getMessage(), e);
				}
			}
		} catch (IOException e) {
			logger.log(Level.FINEST, e.getMessage(), e);
		}

		return manifests;
	}

}
