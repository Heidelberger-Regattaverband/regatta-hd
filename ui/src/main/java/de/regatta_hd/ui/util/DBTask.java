package de.regatta_hd.ui.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.AquariusDB;
import jakarta.persistence.EntityTransaction;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

public class DBTask {
	private static final Logger logger = Logger.getLogger(DBTask.class.getName());

	// executes database operations concurrent to JavaFX operations.
	private static ExecutorService databaseExecutor = Executors.newFixedThreadPool(1, new DatabaseThreadFactory());

	@Inject
	private AquariusDB db;

	public <V> Task<V> run(Callable<V> callable) {
		return runTask(createTask(callable, false, null));
	}

	public <V> Task<V> runInTransaction(Callable<V> callable) {
		return runTask(createTask(callable, true, null));
	}

	private <V> Task<V> createTask(Callable<V> callable, boolean inTransaction,
			EventHandler<WorkerStateEvent> onSucceededHandler) {
		Task<V> task = new Task<>() {
			@Override
			protected V call() throws Exception {
				EntityTransaction transaction = null;

				if (inTransaction) {
					transaction = DBTask.this.db.getEntityManager().getTransaction();
					if (!transaction.isActive()) {
						transaction.begin();
					}
				}

				V result = callable.call();

				if (transaction != null && transaction.isActive()) {
					transaction.commit();
				}

				return result;
			}
		};

		if (onSucceededHandler != null) {
			task.setOnSucceeded(onSucceededHandler);
		}
		task.setOnFailed(t -> logger.log(Level.SEVERE, null, task.getException()));
		return task;
	}

	public <V> Task<V> runTask(Task<V> task) {
		databaseExecutor.submit(task);
		return task;
	}

	static class DatabaseThreadFactory implements ThreadFactory {
		private static final AtomicInteger poolNumber = new AtomicInteger(1);

		@Override
		public Thread newThread(Runnable runnable) {
			Thread thread = new Thread(runnable, "Database-Connection-" + poolNumber.getAndIncrement() + "-thread");
			thread.setDaemon(true);
			return thread;
		}
	}
}
