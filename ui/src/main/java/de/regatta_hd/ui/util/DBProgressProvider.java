package de.regatta_hd.ui.util;

/**
 * Via this interface database tasks can update the current progress information.
 */
public interface DBProgressProvider {

	/**
	 * @param workDone A value from Double.MIN_VALUE up to max. If the value is greater than max, then it will be
	 *                 clamped at max. If the value passed is negative, or Infinity, or NaN, then the resulting
	 *                 percentDone will be -1 (thus, indeterminate).
	 * @param max      A value from Double.MIN_VALUE to Double.MAX_VALUE. Infinity and NaN are treated as -1.
	 * @param message  a message about the current work to be done.
	 */
	void updateProgress(double workDone, double max, String message);
}
