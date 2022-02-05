package de.regatta_hd.common.impl;

import java.util.Arrays;
import java.util.EventListener;
import java.util.List;
import java.util.Objects;

import javax.swing.event.EventListenerList;

import com.google.inject.Singleton;

import de.regatta_hd.common.ListenerManager;

@Singleton
public class ListenerManagerImpl implements ListenerManager {

	private final EventListenerList listeners = new EventListenerList();

	@Override
	public <T extends EventListener> void addListener(Class<T> listenerType, T listener) {
		this.listeners.add(listenerType, Objects.requireNonNull(listener, "listener must not be null."));
	}

	@Override
	public <T extends EventListener> List<T> getListener(Class<T> listenerType) {
		return Arrays.asList(this.listeners.getListeners(listenerType));
	}
}
