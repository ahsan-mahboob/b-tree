package org.apache.dts.btree;

/**
 * Class BTKeyValue.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class BTKeyValue<K extends Comparable, V> {
	
	/** The key. */
	public K mKey;
	
	/** The value. */
	public V mValue;

	/**
	 * Instantiates a new BT key value.
	 *
	 * @param key the key
	 * @param value the value
	 */
	public BTKeyValue(K key, V value) {
		mKey = key;
		mValue = value;
	}
}
