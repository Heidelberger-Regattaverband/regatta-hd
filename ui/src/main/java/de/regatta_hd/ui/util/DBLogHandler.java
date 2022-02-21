package de.regatta_hd.ui.util;

import java.util.LinkedList;
import java.util.logging.Handler;

import org.apache.commons.lang3.ArrayUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import de.regatta_hd.aquarius.AquariusDB;
import de.regatta_hd.aquarius.model.LogRecord;
import de.regatta_hd.common.ListenerManager;

@Singleton
public class DBLogHandler extends Handler {

	private static final String[] FILTERED_CLASSES = { DBLogHandler.class.getName(), LogRecord.class.getName() };

	private final AquariusDB db;

	private final DBTaskRunner dbRunner;

	private final LinkedList<LogRecord> logRecords = new LinkedList<>();

	@Inject
	@Named("hostName")
	private String hostName;

	@Inject
	@Named("hostAddress")
	private String hostAddress;

	@Inject
	DBLogHandler(AquariusDB db, DBTaskRunner dbRunner, ListenerManager manager) {
		this.db = db;
		this.dbRunner = dbRunner;
		setFilter(logRecord -> {
			boolean contains = ArrayUtils.contains(FILTERED_CLASSES, logRecord.getSourceClassName());
			return !contains;
		});
		manager.addListener(AquariusDB.StateChangedEventListener.class, event -> {
			if (event.getAquariusDB().isOpen()) {
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
		this.db.getEntityManager().flush();
	}

	@Override
	public void close() {
		// nothing to do yet
	}

	private void persist(LogRecord logRecord) {
		this.dbRunner.runInTransaction(() -> {
			this.db.getEntityManager().persist(logRecord);
			return null;
		}, result -> {
			// nothing to do with result
		});
	}

	private void persist() {
		this.dbRunner.runInTransaction(() -> {
			while (!this.logRecords.isEmpty()) {
				LogRecord logRecord = this.logRecords.pop();
				this.db.getEntityManager().persist(logRecord);
			}
			return null;
		}, result -> {
			// nothing to do with result
		});
	}
}
