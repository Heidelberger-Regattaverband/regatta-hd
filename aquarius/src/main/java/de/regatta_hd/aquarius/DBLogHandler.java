package de.regatta_hd.aquarius;

import static java.util.Objects.requireNonNull;

import java.util.Deque;
import java.util.LinkedList;
import java.util.logging.Handler;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import org.apache.commons.lang3.ArrayUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import de.regatta_hd.aquarius.impl.AquariusDBImpl;
import de.regatta_hd.aquarius.model.LogRecord;
import de.regatta_hd.commons.core.ListenerManager;
import de.regatta_hd.commons.db.DBConnection;
import de.regatta_hd.commons.db.DBThreadPoolExecutor;

@Singleton
public class DBLogHandler extends Handler {

	private static final String[] FILTERED_CLASSES = { DBLogHandler.class.getName(), LogRecord.class.getName(),
			AquariusDBImpl.class.getName(), DBThreadPoolExecutor.class.getName() };

	private final Deque<LogRecord> logRecords = new LinkedList<>();

	private final DBConnection db;
	private final String hostName;
	private final String hostAddress;

	@Inject
	DBLogHandler(DBConnection db, ListenerManager manager, @Named("hostName") String hostName,
			@Named("hostAddress") String hostAddress) {
		this.db = requireNonNull(db, "db must not be null");
		this.hostName = hostName;
		this.hostAddress = hostAddress;

		setFilter(logRecord -> {
			boolean contains = ArrayUtils.contains(FILTERED_CLASSES, logRecord.getSourceClassName());
			return !contains;
		});

		manager.addListener(DBConnection.StateChangedEventListener.class, event -> {
			if (event.getDBConnection().isOpen()) {
				persist();
			}
		});
	}

	@Override
	public void publish(java.util.logging.LogRecord logRecord) {
		if (isLoggable(logRecord)) {
			LogRecord dbRecord = new LogRecord(this.hostName, this.hostAddress, logRecord);

			if (this.db.isOpen()) {
				persist(dbRecord);
			} else {
				this.logRecords.addLast(dbRecord);
			}
		}
	}

	@Override
	public void flush() {
		// nothing to flush here
	}

	@Override
	public void close() {
		// nothing to do yet
	}

	private void persist(LogRecord logRecord) {
		this.db.getExecutor().execute(() -> {
			EntityManager entityManager = this.db.getEntityManager();
			EntityTransaction transaction = entityManager.getTransaction();
			if (!transaction.isActive()) {
				transaction.begin();
			}
			try {
				this.db.getEntityManager().persist(logRecord);
				entityManager.flush();
				transaction.commit();
			} catch (Exception e) {
				transaction.rollback();
				e.printStackTrace();
				this.logRecords.addLast(logRecord);
			}
		});
	}

	private void persist() {
		this.db.getExecutor().execute(() -> {
			EntityManager entityManager = this.db.getEntityManager();
			EntityTransaction transaction = entityManager.getTransaction();
			if (!transaction.isActive()) {
				transaction.begin();
			}
			try {
				while (!this.logRecords.isEmpty()) {
					entityManager.persist(this.logRecords.pop());
				}
				entityManager.flush();
				transaction.commit();
			} catch (Exception e) {
				transaction.rollback();
				e.printStackTrace();
			}
		});
	}
}
