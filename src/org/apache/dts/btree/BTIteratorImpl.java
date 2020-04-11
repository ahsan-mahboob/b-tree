package org.apache.dts.btree;

/**
 * The Class BTIteratorImpl.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class BTIteratorImpl<K extends Comparable, V> implements BTIterator<K, V> {

	/** The current key. */
	private K mCurrentKey;

	/** The previous key. */
	private K mPreviousKey;

	/** The status. */
	private boolean mStatus;

	/**
	 * Instantiates a new BT test iterator impl.
	 */
	public BTIteratorImpl() {
		reset();
	}

	/* (non-Javadoc)
	 * @see org.apache.dts.btree.BTIterator#item(java.lang.Comparable, java.lang.Object)
	 */
	@Override
	public boolean item(K key, V value) {
		mCurrentKey = key;
		if ((mPreviousKey != null) && (mPreviousKey.compareTo(key) > 0)) {
			mStatus = false;
			return false;
		}
		mPreviousKey = key;
		return true;
	}

	/**
	 * Gets the status.
	 *
	 * @return the status
	 */
	public boolean getStatus() {
		return mStatus;
	}

	/**
	 * Gets the current key.
	 *
	 * @return the current key
	 */
	public K getCurrentKey() {
		return mCurrentKey;
	}

	/**
	 * Gets the previous key.
	 *
	 * @return the previous key
	 */
	public K getPreviousKey() {
		return mPreviousKey;
	}

	/**
	 * Reset.
	 */
	public final void reset() {
		mPreviousKey = null;
		mStatus = true;
	}
}
