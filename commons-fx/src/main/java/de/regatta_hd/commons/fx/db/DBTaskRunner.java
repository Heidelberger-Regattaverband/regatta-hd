package de.regatta_hd.commons.fx.db;

import java.util.function.Consumer;

import javafx.concurrent.Task;

import de.regatta_hd.commons.core.concurrent.AsyncCallable;
import de.regatta_hd.commons.core.concurrent.AsyncResult;

public interface DBTaskRunner {

	<V> DBTask<V> createTask(AsyncCallable<V> callable, Consumer<AsyncResult<V>> resultConsumer, boolean inTransaction);

	<V> DBTask<V> runTask(DBTask<V> task);

	<V> DBTask<V> run(AsyncCallable<V> callable, Consumer<AsyncResult<V>> resultHandler);

	/**
	 * Executes the given {@link AsyncCallable} in a DB task within a transaction.
	 *
	 * @param callable       the {@link AsyncCallable} to execute
	 * @param resultConsumer the onSucceeded event handler is called whenever the Task state transitions to the
	 *                       SUCCEEDED state.
	 * @return the {@link Task} executing the given {@link AsyncCallable}
	 */
	<V> DBTask<V> runInTransaction(AsyncCallable<V> callable, Consumer<AsyncResult<V>> resultConsumer);
}
