package de.regatta_hd.commons.fx.impl;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.CancellationException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.regatta_hd.commons.core.concurrent.AsyncCallable;
import de.regatta_hd.commons.core.concurrent.AsyncResult;
import de.regatta_hd.commons.core.concurrent.ProgressMonitor;
import de.regatta_hd.commons.db.DBConnection;
import de.regatta_hd.commons.fx.db.DBTask;
import jakarta.persistence.EntityTransaction;
import javafx.application.Platform;

class DBTaskImpl<V> extends DBTask<V> {
	private static final Logger logger = Logger.getLogger(DBTaskImpl.class.getName());

	private final AsyncCallable<V> callable;
	private final Consumer<AsyncResult<V>> resultConsumer;
	private volatile Consumer<String> progressMessageConsumer;

	private EntityTransaction transaction;

	DBTaskImpl(AsyncCallable<V> callable, Consumer<AsyncResult<V>> resultConsumer, boolean inTransaction,
			DBConnection db) {
		this.callable = requireNonNull(callable, "callable must not be null");
		this.transaction = inTransaction ? requireNonNull(db, "db must not be null").getEntityManager().getTransaction()
				: null;
		this.resultConsumer = requireNonNull(resultConsumer, "resultConsumer must not be null");

		setOnSucceeded(event -> {
			@SuppressWarnings("unchecked")
			// get result from worker
			AsyncResult<V> result = (AsyncResult<V>) event.getSource().getValue();

			// call given consumer with result in UX thread
			Platform.runLater(() -> this.resultConsumer.accept(result));
		});

		setOnFailed(event -> {
			Exception exception = (Exception) getException();
			logger.log(Level.SEVERE, exception.getMessage(), exception);
			Platform.runLater(() -> this.resultConsumer.accept(new DBResultImpl<>(exception)));
		});

		setOnCancelled(event -> {
			CancellationException exception = new CancellationException("DBTask is cancelled.");
			Platform.runLater(() -> this.resultConsumer.accept(new DBResultImpl<>(exception)));
		});
	}

	@Override
	protected AsyncResult<V> call() throws Exception {
		// begin transaction if required
		if (this.transaction != null && !this.transaction.isActive()) {
			this.transaction.begin();
		}

		V result = this.callable.call(new ProgressMonitor() {

			@Override
			public void update(double workDone, double max, String message) {
				updateProgress(workDone, max, message);
			}

			@Override
			public boolean isCancelled() {
				return isCancelled();
			}
		});

		// if an active transaction exists it is committed
		if (this.transaction != null && this.transaction.isActive()) {
			this.transaction.commit();
		}

		return new DBResultImpl<>(result);
	}

	@Override
	public void setProgressMessageConsumer(Consumer<String> progressMessageConsumer) {
		this.progressMessageConsumer = progressMessageConsumer;
	}

	protected void updateProgress(double workDone, double max, String msg) {
		super.updateProgress(workDone, max);

		if (this.progressMessageConsumer != null) {
			this.progressMessageConsumer.accept(msg);
		}
	}

	private class DBResultImpl<R> implements AsyncResult<R> {

		private final R result;

		private final Exception exception;

		private DBResultImpl(R result) {
			this.result = result;
			this.exception = null;
		}

		private DBResultImpl(Exception exception) {
			this.exception = requireNonNull(exception, "exception must not be null");
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
