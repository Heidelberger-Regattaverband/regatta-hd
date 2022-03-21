package de.regatta_hd.commons.concurrent;

/**
 * An asynchronously callable which gets access to a progress monitor.
 *
 * @param <R> the return type of the callable method
 */
@FunctionalInterface
public interface AsyncCallable<R> {

	R call(ProgressMonitor monitor) throws Exception; // NOSONAR
}
