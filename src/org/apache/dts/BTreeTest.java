package org.apache.dts;

import java.util.Map;
import java.util.TreeMap;

import org.apache.dts.btree.BTException;
import org.apache.dts.btree.BTIteratorImpl;
import org.apache.dts.btree.BTNode;
import org.apache.dts.btree.BTree;

/**
 * Class BTreeTest
 * 
 * Description: This class contains all the test cases for BTree.
 */
public class BTreeTest {

	/** The bTree. */
	private final BTree<Integer, String> mBTree;
	
	/** The map. */
	private final Map<Integer, String> mMap;
	
	/** The iter. */
	private BTIteratorImpl<Integer, String> mIter;

	/**
	 * Instantiates a new b tree test.
	 */
	public BTreeTest() {
		System.out.println("Creating BTree...");
		mBTree = new BTree<Integer, String>();
		mMap = new TreeMap<Integer, String>();
		mIter = new BTIteratorImpl<Integer, String>();
		System.out.println("BTree created successfully.");
	}

	/**
	 * Gets the b tree.
	 *
	 * @return the b tree
	 */
	public BTree<Integer, String> getBTree() {
		return mBTree;
	}

	/**
	 * Gets the root node.
	 *
	 * @return the root node
	 */
	public BTNode<Integer, String> getRootNode() {
		return mBTree.getRootNode();
	}

	/**
	 * Adds the.
	 *
	 * @param key the key
	 * @param value the value
	 */
	public void add(Integer key, String value) {
		mMap.put(key, value);
		mBTree.insert(key, value);
		System.out.println("Key: " + key + " inserted.");
	}

	/**
	 * Delete.
	 *
	 * @param key the key
	 * @throws BTException the BT exception
	 */
	public void delete(Integer key) throws BTException {
		System.out.println("Deleting Key: " + key + " from BTree...");
		String strVal1 = mMap.remove(key);
		String strVal2 = mBTree.delete(key);
		if (!isEqual(strVal1, strVal2)) {
			throw new BTException("Deleted key = " + key + " has different values: " + strVal1 + " | " + strVal2);
		} else {
			System.out.println("Key: " + key + " deleted from BTree successfully.");
		}
	}

	/**
	 * Clear data.
	 */
	private void clearData() {
		mBTree.clear();
		mMap.clear();
	}

	/**
	 * Checks if is equal.
	 *
	 * @param strVal1 the str val 1
	 * @param strVal2 the str val 2
	 * @return true, if is equal
	 */
	private boolean isEqual(String strVal1, String strVal2) {
		if ((strVal1 == null) && (strVal2 == null)) {
			return true;
		}

		if ((strVal1 == null) && (strVal2 != null)) {
			return false;
		}

		if ((strVal1 != null) && (strVal2 == null)) {
			return false;
		}

		if (!strVal1.equals(strVal2)) {
			return false;
		}

		return true;
	}

	/**
	 * Validate search.
	 *
	 * @param key the key
	 * @throws BTException the BT exception
	 */
	public void validateSearch(Integer key) throws BTException {
		System.out.println("Searching key: " + key + " in BTree...");
		String strVal1 = mMap.get(key);
		String strVal2 = mBTree.search(key);
		if (!isEqual(strVal1, strVal2)) {
			throw new BTException("Error in validateSearch(): Failed to compare value for key = " + key);
		} else {
			System.out.println("Key: " + key + " found in BTree.");
		}
	}

	/**
	 * Validate data.
	 *
	 * @throws BTException the BT exception
	 */
	public void validateData() throws BTException {
		for (Map.Entry<Integer, String> entry : mMap.entrySet()) {
			try {
				String strVal = mBTree.search(entry.getKey());
				if (!isEqual(entry.getValue(), strVal)) {
					throw new BTException(
							"Error in validateData(): Failed to compare value for key = " + entry.getKey());
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				throw new BTException("Runtime Error in validateData(): Failed to compare value for key = "
						+ entry.getKey() + " msg = " + ex.getMessage());
			}
		}
	}

	/**
	 * Validate size.
	 *
	 * @throws BTException the BT exception
	 */
	public void validateSize() throws BTException {
		if (mMap.size() != mBTree.getSize()) {
			throw new BTException(
					"Error in validateSize(): Failed to compare the size:  " + mMap.size() + " <> " + mBTree.getSize());
		}
	}

	/**
	 * Validate order.
	 *
	 * @throws BTException the BT exception
	 */
	public void validateOrder() throws BTException {
		mIter.reset();
		mBTree.list(mIter);
		if (!mIter.getStatus()) {
			throw new BTException(
					"Error in validateData(): Failed to compare value for key = " + mIter.getCurrentKey());
		}
	}

	/**
	 * Validate all.
	 *
	 * @throws BTException the BT exception
	 */
	public void validateAll() throws BTException {
		validateData();
		validateSize();
		validateOrder();
	}

	/**
	 * Adds the key.
	 *
	 * @param i the i
	 */
	public void addKey(int i) {
		add(i, "" + i);
	}
	
	/**
	 * Main Entry for the test.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {

		System.out.println("---------------------------------------------------");
		BTreeTest test = new BTreeTest();
		System.out.println("---------------------------------------------------");

		try {
			test.clearData();

			System.out.println("Inserting values in BTree...");
			for (int i = 1; i < 50; ++i) {
				test.addKey(i);
			}
			System.out.println("BTree populated successfully.");
			System.out.println("---------------------------------------------------");

			test.delete(24);
			test.delete(23);
			test.delete(27);
			System.out.println("---------------------------------------------------");

			test.validateSearch(7);
			System.out.println("---------------------------------------------------");
			
			test.validateAll();
		} catch (BTException btex) {
			System.out.println("BTException msg = " + btex.getMessage());
			btex.printStackTrace();
		} catch (Exception ex) {
			System.out.println("BTException msg = " + ex.getMessage());
			ex.printStackTrace();
		}
	}

}
