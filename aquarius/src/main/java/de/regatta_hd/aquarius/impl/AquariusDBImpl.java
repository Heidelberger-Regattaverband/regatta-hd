package de.regatta_hd.aquarius.impl;

import static java.util.Objects.requireNonNull;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.Session;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.microsoft.sqlserver.jdbc.SQLServerException;

import de.regatta_hd.aquarius.model.MetaData;
import de.regatta_hd.commons.core.ListenerManager;
import de.regatta_hd.commons.db.AbstractDBConnection;
import de.regatta_hd.commons.db.DBConfig;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

@Singleton
public class AquariusDBImpl extends AbstractDBConnection {

	private String version;

	@Inject
	public AquariusDBImpl(ListenerManager listenerManager) {
		super(listenerManager);
	}

	@Override
	public synchronized void open(DBConfig dbCfg) throws SQLException {
		Map<String, String> props = getProperties(requireNonNull(dbCfg, "dbCfg must not be null"));

		close();

		try {
			this.emFactory = Persistence.createEntityManagerFactory("aquarius", props);

			this.version = readVersion();

			// notify listeners about changed AquariusDB state
			notifyListeners(new AquariusDBStateChangedEventImpl(this));
		} catch (PersistenceException e) {
			if (this.emFactory != null) {
				this.emFactory.close();
				this.emFactory = null;
			}
			Throwable rootCause = ExceptionUtils.getRootCause(e);
			if (rootCause instanceof SQLServerException) {
				throw (SQLServerException) rootCause;
			}
			throw e;
		}
	}

	@SuppressWarnings("resource")
	@Override
	public void updateSchema() {
		ensureOpen();

		Session session = getEntityManager().unwrap(Session.class);
		session.doWork(connection -> {
			try {
				Database database = DatabaseFactory.getInstance()
						.findCorrectDatabaseImplementation(new JdbcConnection(connection));
				database.setDatabaseChangeLogLockTableName("HRV_ChangeLogLock");
				database.setDatabaseChangeLogTableName("HRV_ChangeLog");

				Liquibase liquibase = new Liquibase("/db/liquibase-changeLog.xml", new ClassLoaderResourceAccessor(),
						database);
				liquibase.update(new Contexts(), new LabelExpression());
			} catch (LiquibaseException e) {
				close();
				throw new SQLException(e);
			}
		});
	}

	String getVersion() {
		return this.version;
	}


	@Override
	protected Map<String, String> getProperties(DBConfig dbCfg) {
		Map<String, String> props = new HashMap<>();
		String url = String.format("jdbc:sqlserver://%s;databaseName=%s;encrypt=%s", dbCfg.getDbHost(),
				dbCfg.getDbName(), Boolean.toString(dbCfg.isEncrypt()));

		if (dbCfg.isEncrypt() && dbCfg.isTrustServerCertificate()) {
			url += ";trustServerCertificate=true";
		}

		props.put("javax.persistence.jdbc.url", url);
		props.put("javax.persistence.jdbc.user", dbCfg.getUsername());
		props.put("javax.persistence.jdbc.password", dbCfg.getPassword());
		return props;
	}

	private String readVersion() {
		MetaData metaData = this.getEntityManager()
				.createQuery("SELECT m FROM MetaData m WHERE m.key = 'PatchLevel'", MetaData.class).getSingleResult();
		return metaData.getValue();
	}

}
