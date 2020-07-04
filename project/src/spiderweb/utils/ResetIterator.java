package spiderweb.utils;

import java.util.Iterator;

/**
 * リセット可能なIterator.
 */
public interface ResetIterator extends Iterator<String> {
	public void reset();
}
