package de.regatta_hd.commons.fx.db;

import java.util.function.Consumer;

import javafx.concurrent.Task;

import de.regatta_hd.commons.core.concurrent.AsyncResult;

public abstract class DBTask<V> extends Task<AsyncResult<V>> {

	public abstract void setProgressMessageConsumer(Consumer<String> progressMessageConsumer);

}
