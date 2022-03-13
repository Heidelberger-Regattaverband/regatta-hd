package de.regatta_hd.ui.util;

public interface DBCallable<R> {

	R call(DBProgressProvider progress) throws Exception;
}
