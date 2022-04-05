package de.regatta_hd.commons.fx.impl;

import java.util.function.Consumer;

import de.regatta_hd.commons.core.concurrent.AsyncCallable;
import de.regatta_hd.commons.core.concurrent.AsyncResult;
import de.regatta_hd.commons.db.DBConnection;
import de.regatta_hd.commons.fx.db.DBTask;

class DBTaskImpl<V> extends DBTask<V> {

	DBTaskImpl(AsyncCallable<V> callable, Consumer<AsyncResult<V>> resultConsumer, boolean inTransaction,
			DBConnection db) {
		super(callable, resultConsumer, inTransaction, db);
	}

}
