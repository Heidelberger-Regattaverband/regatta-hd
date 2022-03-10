package de.regatta_hd.ui.util;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.regatta_hd.aquarius.AquariusDB;
import jakarta.persistence.EntityTransaction;
import javafx.application.Platform;
import javafx.concurrent.Task;

public class DBTask<V> extends Task<DBResult<V>> {
	private static final Logger logger = Logger.getLogger(DBTask.class.getName());

	private final DBCallable<V> callable;

	private Consumer<DBResult<V>> resultConsumer;

	private final AquariusDB db;

	private final boolean inTransaction;

	private volatile Consumer<String> progressMessageConsumer;

	DBTask(DBCallable<V> callable, Consumer<DBResult<V>> resultConsumer, boolean inTransaction, AquariusDB db) {
		this.callable = Objects.requireNonNull(callable, "callable must not be null");
		this.resultConsumer = Objects.requireNonNull(resultConsumer, "resultConsumer must not be null");
		this.inTransaction = inTransaction;
		this.db = Objects.requireNonNull(db, "db must not be null");

		setOnSucceeded(event -> {
			@SuppressWarnings("unchecked")
			// get result from worker
			DBResult<V> result = (DBResult<V>) event.getSource().getValue();

			// call given consumer with result in UX thread
			Platform.runLater(() -> this.resultConsumer.accept(result));
		});

		setOnFailed(event -> {
			Exception exception = (Exception) getException();
			logger.log(Level.SEVERE, exception.getMessage(), exception);
			Platform.runLater(() -> this.resultConsumer.accept(new DBResultImpl<>(exception)));
		});
	}

	public void setProgressMessageConsumer(Consumer<String> progressMessageConsumer) {
		this.progressMessageConsumer = progressMessageConsumer;
	}

	@Override
	protected DBResult<V> call() throws Exception {
		EntityTransaction transaction = this.inTransaction ? this.db.getEntityManager().getTransaction() : null;

		// begin transaction if required
		if (transaction != null && !transaction.isActive()) {
			transaction.begin();
		}

		V result = this.callable.call(DBTask.this::updateProgress);

		// if an active transaction exists it is committed
		if (transaction != null && transaction.isActive()) {
			transaction.commit();
		}

		return new DBResultImpl<>(result);
	}

	protected void updateProgress(double workDone, double max, String msg) {
		super.updateProgress(workDone, max);

		if (this.progressMessageConsumer != null) {
			this.progressMessageConsumer.accept(msg);
		}
	}

	private class DBResultImpl<R> implements DBResult<R> {

		private final R result;

		private final Exception exception;

		private DBResultImpl(R result) {
			this.result = result;
			this.exception = null;
		}

		private DBResultImpl(Exception exception) {
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
