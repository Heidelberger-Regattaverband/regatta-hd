package de.regatta_hd.commons.core;

import java.util.EventListener;
import java.util.List;

/**
 * This interface provides means to manage event listeners.
 */
public interface ListenerManager {

	/**
	 * Adds an event listener instance for the given event listener class to the manager.
	 *
	 * @param <T>           The type of the event listener class
	 * @param listenerClass the class of the event listener
	 * @param listener      the event listener instance
	 * @throws NullPointerException if any of the arguments is {@code null}
	 */
	<T extends EventListener> void addListener(Class<T> listenerClass, T listener);

	/**
	 * Removes an event listener instance from the given event listener class to the manager.
	 *
	 * @param <T>           The type of the event listener class
	 * @param listenerClass the class of the event listener
	 * @param listener      the event listener instance
	 * @throws NullPointerException if any of the arguments is {@code null}
	 */
	<T extends EventListener> void removeListener(Class<T> listenerClass, T listener);

	/**
	 * Returns a list with all listeners for the given event listener class.
	 *
	 * @param <T>           The type of the event listener class
	 * @param listenerClass the class of the event listener
	 * @return a {@link List} with event listeners, could be empty but never {@code null}
	 */
	<T extends EventListener> List<T> getListeners(Class<T> listenerClass);

}
