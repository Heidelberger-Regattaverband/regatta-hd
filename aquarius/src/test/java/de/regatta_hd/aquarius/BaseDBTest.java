package de.regatta_hd.aquarius;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import de.regatta_hd.commons.db.DBConfig;
import de.regatta_hd.commons.db.DBConfigStore;
import de.regatta_hd.commons.db.DBConnection;

class BaseDBTest implements BeforeAllCallback {

	protected static DBConnection aquariusDb;

	protected static Injector injector;

	private static DBConfig connectionData;

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		com.google.inject.Module testModules = Modules.override(new AquariusModule()).with(new TestModule());
		injector = Guice.createInjector(testModules);

		DBConfigStore connStore = injector.getInstance(DBConfigStore.class);
		connectionData = connStore.getLastSuccessful();

		aquariusDb = injector.getInstance(DBConnection.class);

		aquariusDb.getExecutor().execute(() -> {
			try {
				aquariusDb.open(connectionData);
				aquariusDb.updateSchema();
			} catch (SQLException e) {
				fail(e);
			}
		});
	}

	@Test
	void testOpen() {
		aquariusDb.getExecutor().execute(() -> {
			try {
				aquariusDb.open(connectionData);
				assertTrue(aquariusDb.isOpen());
			} catch (SQLException e) {
				fail(e);
			}
		});
	}

}
