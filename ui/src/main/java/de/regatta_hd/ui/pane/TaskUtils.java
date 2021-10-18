package de.regatta_hd.ui.pane;

import java.util.Objects;
import java.util.function.Supplier;

import javafx.concurrent.Task;

public class TaskUtils {

	private TaskUtils() {
	}

	public static <V> Task<V> createAndRunTask(Supplier<V> supplier) {
		return runTask(createTask(supplier));
	}

	public static <V> Task<V> createTask(Supplier<V> supplier) {
		return new Task<>() {
			@Override
			protected V call() throws Exception {
				return supplier.get();
			}
		};
	}

	public static <V> Task<V> runTask(Task<V> task) {
		Thread th = new Thread(Objects.requireNonNull(task, "task must not be null"));
		th.setDaemon(true);
		th.start();
		return task;
	}
}
