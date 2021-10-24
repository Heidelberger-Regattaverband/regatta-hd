package de.regatta_hd.ui.pane;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

public class TaskUtils {
	private static final Logger logger = Logger.getLogger(TaskUtils.class.getName());

	// executes database operations concurrent to JavaFX operations.
	private static ExecutorService databaseExecutor = Executors.newFixedThreadPool(1, new DatabaseThreadFactory());

	private TaskUtils() {
	}

	public static <V> Task<V> createAndRunTask(Callable<V> callable) {
		return runTask(createTask(callable));
	}

	public static <V> Task<V> createAndRunTask(Callable<V> callable,
			EventHandler<WorkerStateEvent> onSucceededHandler) {
		return runTask(createTask(callable, onSucceededHandler));
	}

	public static <V> Task<V> createTask(Callable<V> callable) {
		return createTask(callable, null);
	}

	public static <V> Task<V> createTask(Callable<V> callable, EventHandler<WorkerStateEvent> onSucceededHandler) {
		Task<V> task = new Task<>() {
			@Override
			protected V call() throws Exception {
				return callable.call();
			}
		};

		if (onSucceededHandler != null) {
			task.setOnSucceeded(onSucceededHandler);
		}
		task.setOnFailed(t -> logger.log(Level.SEVERE, null, task.getException()));
		return task;
	}

	public static <V> Task<V> runTask(Task<V> task) {
		databaseExecutor.submit(task);
		return task;
	}

	static class DatabaseThreadFactory implements ThreadFactory {
		static final AtomicInteger poolNumber = new AtomicInteger(1);

		@Override
		public Thread newThread(Runnable runnable) {
			Thread thread = new Thread(runnable, "Database-Connection-" + poolNumber.getAndIncrement() + "-thread");
			thread.setDaemon(true);
			return thread;
		}
	}
}
