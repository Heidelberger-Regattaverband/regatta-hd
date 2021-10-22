package de.regatta_hd.ui.pane;

import java.util.Objects;
import java.util.function.Supplier;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

public class TaskUtils {

	private TaskUtils() {
	}

	public static <V> Task<V> createAndRunTask(Supplier<V> supplier) {
		return runTask(createTask(supplier));
	}

	public static <V> Task<V> createAndRunTask(Supplier<V> supplier,
			EventHandler<WorkerStateEvent> onSucceededHandler) {
		return runTask(createTask(supplier, onSucceededHandler));
	}

	public static <V> Task<V> createTask(Supplier<V> supplier) {
		return createTask(supplier, null);
	}

	public static <V> Task<V> createTask(Supplier<V> supplier, EventHandler<WorkerStateEvent> onSucceededHandler) {
		Task<V> task = new Task<>() {
			@Override
			protected V call() throws Exception {
				return supplier.get();
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
