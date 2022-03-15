package de.regatta_hd.common.concurrent;

/**
 * Via this interface asynchronous tasks can update the current progress information.
 */
public interface ProgressMonitor {

	/**
	 * @param workDone A value from Double.MIN_VALUE up to max. If the value is greater than max, then it will be
	 *                 clamped at max. If the value passed is negative, or Infinity, or NaN, then the resulting
	 *                 percentDone will be -1 (thus, indeterminate).
	 * @param max      A value from Double.MIN_VALUE to Double.MAX_VALUE. Infinity and NaN are treated as -1.
	 * @param message  a message about the current work to be done.
	 */
	void update(double workDone, double max, String message);

	/**
	 * Indicates that the task is requested to cancel.
	 */
	boolean isCancelled();
}
