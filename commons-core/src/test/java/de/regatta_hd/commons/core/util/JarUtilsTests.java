package de.regatta_hd.commons.core.util;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import java.util.jar.Manifest;

import org.junit.jupiter.api.Test;

class JarUtilsTests {

	@Test
	void getManifestsTest() {
		List<Manifest> manifests = JarUtils.getManifests();
		assertFalse(manifests.isEmpty());
	}

}
