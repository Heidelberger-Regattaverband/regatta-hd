package de.regatta_hd.ui.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.AquariusDB;
import jakarta.persistence.EntityTransaction;
import javafx.application.Platform;
import javafx.concurrent.Task;

public class DBTask {
	private static final Logger logger = Logger.getLogger(DBTask.class.getName());

	// executes database operations concurrent to JavaFX operations.
	private static ExecutorService databaseExecutor = Executors.newFixedThreadPool(1, new DatabaseThreadFactory());

	@Inject
	private AquariusDB db;

	/**
	 * Executes the given {@link Callable} in a DB task.
	 *
	 * @param callable the {@link Callable} to execute
	 * @return the {@link Task} executing the given {@link Callable}
	 */
	public <V> Task<V> run(Callable<V> callable) {
		return runTask(createTask(callable, null, false));
	}

	/**
	 * Executes the given {@link DBRunnable} in a DB task.
	 *
	 * @param runnable the {@link DBRunnable} to execute
	 * @return the {@link Task} executing the given {@link DBRunnable}
	 */
	public Task<Void> run(DBRunnable runnable) {
		return runTask(createTask(() -> {
			runnable.run();
			return null;
		}, null, false));
	}

	/**
	 * Runs the given {@link Callable} in a DB task.
	 *
	 * @param callable the {@link Callable} to run
	 * @return the {@link Task} executing the given {@link Callable}
	 */
	public <V> Task<V> run(Callable<V> callable, Consumer<V> onSucceededHandler) {
		return runTask(createTask(callable, onSucceededHandler, false));
	}

	/**
	 * Runs the given {@link DBRunnable} in a DB task.
	 *
	 * @param runnable the {@link DBRunnable} to run
	 * @return the {@link Task} executing the given {@link DBRunnable}
	 */
	public Task<Void> runInTransaction(DBRunnable runnable, Consumer<Void> onSucceededHandler) {
		return runTask(createTask(() -> {
			runnable.run();
			return null;
		}, onSucceededHandler, true));
	}

	public <V> Task<V> runInTransaction(Callable<V> callable, Consumer<V> onSucceededHandler) {
		return runTask(createTask(callable, onSucceededHandler, true));
	}

	private <V> Task<V> createTask(Callable<V> callable, Consumer<V> onSucceededHandler, boolean inTransaction) {
		Task<V> task = new Task<>() {
			@Override
			protected V call() throws Exception {
				EntityTransaction transaction = inTransaction ? DBTask.this.db.getEntityManager().getTransaction()
						: null;

				// begin transaction if required
				if (transaction != null && !transaction.isActive()) {
					transaction.begin();
				}

				V result = callable.call();

				// if an active transaction exists it is committed
				if (transaction != null && transaction.isActive()) {
					transaction.commit();
				}

				return result;
			}
		};

		if (onSucceededHandler != null) {
			task.setOnSucceeded(event -> {
				@SuppressWarnings("unchecked")
				// get result from worker
				V result = (V) event.getSource().getValue();

				// call given consumer with result in UX thread
				Platform.runLater(() -> onSucceededHandler.accept(result));
			});
		}

		task.setOnFailed(t -> logger.log(Level.SEVERE, null, task.getException()));
		return task;
	}

	private static <V> Task<V> runTask(Task<V> task) {
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

	@FunctionalInterface
	public interface DBRunnable {
		void run() throws Exception;
	}
}
