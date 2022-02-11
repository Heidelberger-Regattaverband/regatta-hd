package de.regatta_hd.ui.util;

@FunctionalInterface
public interface DBResult<R> {
	R getResult() throws Exception; // NOSONAR
}