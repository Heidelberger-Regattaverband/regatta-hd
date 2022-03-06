package de.regatta_hd.ui.util;

public interface DBExecutable<R> {

	R execute(DBProgress progress);
}
