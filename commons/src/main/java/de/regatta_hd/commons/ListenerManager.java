package de.regatta_hd.commons;

import java.util.EventListener;
import java.util.List;

/**
 * This interface provides means to manage event listeners.
 */
public interface ListenerManager {

	/**
	 * Adds an event listener instance for a given event listener class to the manager.
	 *
	 * @param <T>           The type of the event listener class
	 * @param listenerClass the class of the event listener
	 * @param listener      the event listener instance
	 */
	<T extends EventListener> void addListener(Class<T> listenerClass, T listener);

	<T extends EventListener> List<T> getListeners(Class<T> listenerClass);
}
