package de.regatta_hd.aquarius.impl;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.Session;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.microsoft.sqlserver.jdbc.SQLServerException;

import de.regatta_hd.aquarius.model.MetaData;
import de.regatta_hd.commons.core.ListenerManager;
import de.regatta_hd.commons.db.AbstractDBConnection;
import de.regatta_hd.commons.db.DBConfig;
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
		super("aquarius", listenerManager);
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

	public String getVersion() {
		return this.version;
	}

	@Override
	protected Map<String, String> getProperties(DBConfig dbConfig) {
		Map<String, String> properties = new HashMap<>();

		String url = String.format("jdbc:sqlserver://%s;databaseName=%s;encrypt=%s", dbConfig.getDbHost(),
				dbConfig.getDbName(), Boolean.toString(dbConfig.isEncrypt()));

		if (dbConfig.isEncrypt() && dbConfig.isTrustServerCertificate()) {
			url += ";trustServerCertificate=true";
		}

		properties.put("javax.persistence.jdbc.url", url);
		properties.put("javax.persistence.jdbc.user", dbConfig.getUsername());
		properties.put("javax.persistence.jdbc.password", dbConfig.getPassword());
		return properties;
	}

	@Override
	protected void openImpl() {
		this.version = readVersion();
	}

	@Override
	protected void convertException(PersistenceException ex) throws SQLException {
		Throwable rootCause = ExceptionUtils.getRootCause(ex);
		if (rootCause instanceof SQLServerException) {
			throw (SQLServerException) rootCause;
		}
		throw ex;
	}

	private String readVersion() {
		MetaData metaData = getEntityManagerImpl()
				.createQuery("SELECT m FROM MetaData m WHERE m.key = 'PatchLevel'", MetaData.class).getSingleResult();
		return metaData.getValue();
	}

	@Override
	public <R> Future<R> execute(DBCallable<R> callable) {
		return getExecutor().submit(() -> {
			return callable.execute(getEntityManager());
		});
	}

}
