package de.regatta_hd.commons.core.concurrent;

import java.util.concurrent.CancellationException;

/**
 * Asynchronously running tasks can update their progress information through this interface.
 */
public interface ProgressMonitor {

	/**
	 * @param workDone A value from Double.MIN_VALUE up to max. If the value is greater than max, then it will be
	 *                 clamped at max. If the value passed is negative, or Infinity, or NaN, then the resulting
	 *                 percentDone will be -1 (thus, indeterminate).
	 * @param max      A value from Double.MIN_VALUE to Double.MAX_VALUE. Infinity and NaN are treated as -1.
	 * @param message  a message about the current work to be done.
	 * @throws CancellationException if an asynchronous task was cancelled.
	 */
	void update(double workDone, double max, String message);

	/**
	 * Indicates that the task was cancelled.
	 */
	boolean isCancelled();

	/**
	 * Checks if asynchronous task was cancelled and throws a {@link CancellationException} if in this case.
	 *
	 * @throws CancellationException if an asynchronous task was cancelled.
	 */
	void checkCancelled();

}
