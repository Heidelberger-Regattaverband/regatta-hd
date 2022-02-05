package de.regatta_hd.common;

import java.util.EventListener;
import java.util.List;

public interface ListenerManager {

	<T extends EventListener> void addListener(Class<T> listenerType, T listener);

	<T extends EventListener> List<T> getListener(Class<T> listenerType);
}
