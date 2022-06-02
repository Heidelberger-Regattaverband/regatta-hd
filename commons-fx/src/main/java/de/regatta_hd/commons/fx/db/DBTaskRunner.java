package de.regatta_hd.commons.fx.db;

import java.util.function.Consumer;

import de.regatta_hd.commons.core.concurrent.AsyncCallable;
import de.regatta_hd.commons.core.concurrent.AsyncResult;
import javafx.concurrent.Task;

public interface DBTaskRunner {

	<V> DBTask<V> createTask(DBAsyncCallable<V> callable, Consumer<AsyncResult<V>> resultConsumer, boolean inTransaction);

	<V> DBTask<V> runTask(DBTask<V> task);

	<V> DBTask<V> run(DBAsyncCallable<V> callable, Consumer<AsyncResult<V>> resultHandler);

	/**
	 * Executes the given {@link AsyncCallable} in a DB task within a transaction.
	 *
	 * @param callable       the {@link AsyncCallable} to execute
	 * @param resultConsumer the onSucceeded event handler is called whenever the Task state transitions to the
	 *                       SUCCEEDED state.
	 * @return the {@link Task} executing the given {@link AsyncCallable}
	 */
	<V> DBTask<V> runInTransaction(DBAsyncCallable<V> callable, Consumer<AsyncResult<V>> resultConsumer);
}
