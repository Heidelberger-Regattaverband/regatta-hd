package de.regatta_hd.ui.util;

import java.util.Objects;
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
	 * @param callable      the {@link Callable} to execute, must not be null
	 * @param resultHandler the onSucceeded event handler is called whenever the Task state transitions to the SUCCEEDED state.
	 * @return the {@link Task} executing the given {@link Callable}
	 */
	public <V> Task<DBResult<V>> run(Callable<V> callable, Consumer<DBResult<V>> resultHandler) {
		return runTask(createTask(callable, resultHandler, false));
	}

	/**
	 * Executes the given {@link Callable} in a DB task within a transaction.
	 *
	 * @param callable       the {@link Callable} to execute
	 * @param resultConsumer the onSucceeded event handler is called whenever the Task state transitions to the SUCCEEDED state.
	 * @return the {@link Task} executing the given {@link Callable}
	 */
	public <V> Task<DBResult<V>> runInTransaction(Callable<V> callable, Consumer<DBResult<V>> resultConsumer) {
		return runTask(createTask(callable, resultConsumer, true));
	}

	private <V> Task<DBResult<V>> createTask(Callable<V> callable, Consumer<DBResult<V>> resultConsumer,
			boolean inTransaction) {
		Objects.requireNonNull(callable, "callable must not be null");
		Objects.requireNonNull(resultConsumer, "resultConsumer must not be null");

		Task<DBResult<V>> task = new Task<>() {
			@Override
			protected DBResult<V> call() {
				try {
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

					return new DBResultImpl<>(result);
				} catch (Exception ex) {
					return new DBResultImpl<>(ex);
				}
			}
		};

		task.setOnSucceeded(event -> {
			@SuppressWarnings("unchecked")
			// get result from worker
			DBResult<V> result = (DBResult<V>) event.getSource().getValue();

			// call given consumer with result in UX thread
			Platform.runLater(() -> {
				try {
					resultConsumer.accept(result);
				} catch (Exception ex) {
					logger.log(Level.SEVERE, ex.getMessage(), ex);
					FxUtils.showErrorMessage(ex);
				}
			});
		});

		task.setOnFailed(t -> {
			Throwable exception = task.getException();
			logger.log(Level.SEVERE, exception.getMessage(), exception);
			FxUtils.showErrorMessage(exception);
		});
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
	public interface DBResult<R> {
		R getResult() throws Exception; // NOSONAR
	}

	static class DBResultImpl<R> implements DBResult<R> {

		private final R result;

		private final Exception exception;

		DBResultImpl(R result) {
			this.result = result;
			this.exception = null;
		}

		DBResultImpl(Exception exception) {
			this.exception = Objects.requireNonNull(exception, "exception must not be null");
			this.result = null;
		}

		@Override
		public R getResult() throws Exception {
			if (this.exception != null) {
				throw this.exception;
			}
			return this.result;
		}
	}
}
