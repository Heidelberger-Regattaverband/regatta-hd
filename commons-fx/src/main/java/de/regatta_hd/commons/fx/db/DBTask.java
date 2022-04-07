package de.regatta_hd.commons.fx.db;

import java.util.function.Consumer;

import de.regatta_hd.commons.core.concurrent.AsyncResult;
import javafx.concurrent.Task;

public abstract class DBTask<V> extends Task<AsyncResult<V>> {

	public abstract void setProgressMessageConsumer(Consumer<String> progressMessageConsumer);

}
