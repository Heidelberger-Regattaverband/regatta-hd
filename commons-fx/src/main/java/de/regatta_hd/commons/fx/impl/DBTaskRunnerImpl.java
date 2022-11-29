package de.regatta_hd.commons.fx.impl;

import java.util.function.Consumer;

import javafx.concurrent.Task;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.regatta_hd.commons.core.concurrent.AsyncCallable;
import de.regatta_hd.commons.core.concurrent.AsyncResult;
import de.regatta_hd.commons.db.DBConnection;
import de.regatta_hd.commons.fx.db.DBTask;
import de.regatta_hd.commons.fx.db.DBTaskRunner;

@Singleton
public class DBTaskRunnerImpl implements DBTaskRunner {

	@Inject
	private DBConnection db;

	/**
	 * Executes the given {@link AsyncCallable} in a DB task.
	 *
	 * @param callable      the {@link AsyncCallable} to execute, must not be null
	 * @param resultHandler the onSucceeded event handler is called whenever the Task state transitions to the SUCCEEDED
	 *                      state.
	 * @return the {@link Task} executing the given {@link AsyncCallable}
	 */
	@Override
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
	@Override
	public <V> DBTask<V> runInTransaction(AsyncCallable<V> callable, Consumer<AsyncResult<V>> resultConsumer) {
		return runTask(createTask(callable, resultConsumer, true));
	}

	@Override
	public <V> DBTask<V> createTask(AsyncCallable<V> callable, Consumer<AsyncResult<V>> resultConsumer,
			boolean inTransaction) {
		return new DBTaskImpl<>(callable, resultConsumer, inTransaction, this.db);
	}

	@Override
	public <V> DBTask<V> runTask(DBTask<V> task) {
		this.db.getExecutor().execute(task);
		return task;
	}
}
