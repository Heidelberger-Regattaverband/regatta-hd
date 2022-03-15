package de.regatta_hd.ui.util;

import java.util.function.Consumer;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.regatta_hd.aquarius.AquariusDB;
import de.regatta_hd.common.concurrent.AsyncCallable;
import de.regatta_hd.common.concurrent.AsyncResult;
import javafx.concurrent.Task;

@Singleton
public class DBTaskRunner {

	@Inject
	private AquariusDB db;

	/**
	 * Executes the given {@link AsyncCallable} in a DB task.
	 *
	 * @param callable      the {@link AsyncCallable} to execute, must not be null
	 * @param resultHandler the onSucceeded event handler is called whenever the Task state transitions to the SUCCEEDED
	 *                      state.
	 * @return the {@link Task} executing the given {@link AsyncCallable}
	 */
	public <V> DBTask<V> run(AsyncCallable<V> callable, Consumer<AsyncResult<V>> resultHandler) {
		return runTask(createTask(callable, resultHandler, false));
	}

	/**
	 * Executes the given {@link AsyncCallable} in a DB task within a transaction.
	 *
	 * @param callable       the {@link AsyncCallable} to execute
	 * @param resultConsumer the onSucceeded event handler is called whenever the Task state transitions to the
	 *                       SUCCEEDED state.
	 * @return the {@link Task} executing the given {@link AsyncCallable}
	 */
	public <V> DBTask<V> runInTransaction(AsyncCallable<V> callable, Consumer<AsyncResult<V>> resultConsumer) {
		return runTask(createTask(callable, resultConsumer, true));
	}

	public <V> DBTask<V> createTask(AsyncCallable<V> callable, Consumer<AsyncResult<V>> resultConsumer,
			boolean inTransaction) {
		return new DBTask<>(callable, resultConsumer, inTransaction, this.db);
	}

	public <V> DBTask<V> runTask(DBTask<V> task) {
		this.db.getExecutor().execute(task);
		return task;
	}
}
