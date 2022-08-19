package de.regatta_hd.commons.core.concurrent;

/**
 * This interface provides access to the result of an asynchronous task.
 *
 * @param <R> the type of the result
 */
@FunctionalInterface
public interface AsyncResult<R> {

	/**
	 * Returns the result of the asynchronously executed task.
	 *
	 * @return the result of the asynchronously executed task
	 * @throws Exception if an error occurred during the execution of the asynchronously executed task
	 */
	R getResult() throws Exception; // NOSONAR

}