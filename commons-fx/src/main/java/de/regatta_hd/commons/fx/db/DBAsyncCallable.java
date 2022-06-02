package de.regatta_hd.commons.fx.db;

import de.regatta_hd.commons.core.concurrent.ProgressMonitor;
import jakarta.persistence.EntityManager;

/**
 * An asynchronously callable which gets access to a progress monitor.
 *
 * @param <R> the return type of the callable method
 */
@FunctionalInterface
public interface DBAsyncCallable<R> {

	R call(EntityManager entityManager, ProgressMonitor monitor) throws Exception; // NOSONAR

}
