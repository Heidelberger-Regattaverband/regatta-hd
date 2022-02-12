package de.regatta_hd.ui.util;

/**
 * This interface provides access to the result of a {@link DBTask}.
 *
 * @param <R> the type of the result
 */
@FunctionalInterface
public interface DBResult<R> {
	/**
	 * Returns the result of the executed {@link DBTask}.
	 *
	 * @return the result of the executed {@link DBTask}
	 * @throws Exception if an error occurred during the execution of the {@link DBTask}
	 */
	R getResult() throws Exception; // NOSONAR
}