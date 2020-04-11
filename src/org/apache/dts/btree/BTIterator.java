package org.apache.dts.btree;

/**
 * Interface BTIteratorIF.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public interface BTIterator<K extends Comparable, V> {
	
	/**
	 * Item.
	 *
	 * @param key the key
	 * @param value the value
	 * @return true, if successful
	 */
	public boolean item(K key, V value);
}
