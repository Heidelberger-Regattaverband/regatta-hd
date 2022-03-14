package de.regatta_hd.ui.util;

import de.regatta_hd.common.ProgressMonitor;

public interface DBCallable<R> {

	R call(ProgressMonitor monitor) throws Exception; // NOSONAR
}
