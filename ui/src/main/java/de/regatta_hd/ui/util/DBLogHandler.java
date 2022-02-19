package de.regatta_hd.ui.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.logging.Handler;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.regatta_hd.aquarius.AquariusDB;
import de.regatta_hd.aquarius.model.LogRecord;
import de.regatta_hd.common.ListenerManager;

@Singleton
public class DBLogHandler extends Handler {

	private final AquariusDB db;

	private final DBTaskRunner dbRunner;

	private final LinkedList<LogRecord> logRecords = new LinkedList<>();

	private final String hostName;

	private final String hostAddress;

	@Inject
	DBLogHandler(AquariusDB db, DBTaskRunner dbRunner, ListenerManager manager) throws UnknownHostException {
		this.db = db;
		this.dbRunner = dbRunner;
		InetAddress host = InetAddress.getLocalHost();
		this.hostName = host.getHostName();
		this.hostAddress = host.getHostAddress();

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
			this.db.getEntityManager().merge(logRecord);
			return null;
		}, result -> {
			// nothing to do with result
		});
	}

	private void persist() {
		this.dbRunner.runInTransaction(() -> {
			while (!this.logRecords.isEmpty()) {
				LogRecord logRecord = this.logRecords.pop();
				this.db.getEntityManager().merge(logRecord);
			}
			return null;
		}, result -> {
			// nothing to do with result
		});
	}
}
