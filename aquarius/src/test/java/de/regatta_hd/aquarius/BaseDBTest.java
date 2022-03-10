package de.regatta_hd.aquarius;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

class BaseDBTest implements BeforeAllCallback {

	protected static AquariusDB aquariusDb;

	protected static Injector injector;

	private static DBConfig connectionData;

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		com.google.inject.Module testModules = Modules.override(new AquariusDBModule()).with(new TestModule());
		injector = Guice.createInjector(testModules);

		DBConfigStore connStore = injector.getInstance(DBConfigStore.class);
		connectionData = connStore.getLastSuccessful();

		aquariusDb = injector.getInstance(AquariusDB.class);
		aquariusDb.open(connectionData);

		aquariusDb.updateSchema();
	}

	@Test
	void testOpen() {
		aquariusDb.open(connectionData);
		Assertions.assertTrue(aquariusDb.isOpen());
	}

}
