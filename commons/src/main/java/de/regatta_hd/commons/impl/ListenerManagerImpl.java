package de.regatta_hd.commons.impl;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.EventListener;
import java.util.List;

import javax.swing.event.EventListenerList;

import com.google.inject.Singleton;

import de.regatta_hd.commons.ListenerManager;

@Singleton
public class ListenerManagerImpl implements ListenerManager {

	private final EventListenerList listeners = new EventListenerList();

	@Override
	public <T extends EventListener> void addListener(Class<T> listenerClass, T listener) {
		this.listeners.add(requireNonNull(listenerClass, "listenerClass must not be null"),
				requireNonNull(listener, "listener must not be null"));
	}

	@Override
	public <T extends EventListener> List<T> getListeners(Class<T> listenerClass) {
		return Arrays
				.asList(this.listeners.getListeners(requireNonNull(listenerClass, "listenerClass must not be null")));
	}

	@Override
	public <T extends EventListener> void removeListener(Class<T> listenerClass, T listener) {
		this.listeners.remove(requireNonNull(listenerClass, "listenerClass must not be null"),
				requireNonNull(listener, "listener must not be null"));
	}
}
