package de.regatta_hd.ui.pane;

import java.util.Objects;
import java.util.concurrent.Callable;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

public class TaskUtils {

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
		return task;
	}

	public static <V> Task<V> runTask(Task<V> task) {
		Thread th = new Thread(Objects.requireNonNull(task, "task must not be null"));
		th.setDaemon(true);
		th.start();
		return task;
	}
}
