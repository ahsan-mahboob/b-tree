package org.apache.dts.btree;

/**
 * Class BTNode.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class BTNode<K extends Comparable, V> {
	
	/** The Constant MIN_DEGREE. */
	public final static int MIN_DEGREE = 5;
	
	/** The Constant LOWER_BOUND_KEYNUM. */
	public final static int LOWER_BOUND_KEYNUM = MIN_DEGREE - 1;
	
	/** The Constant UPPER_BOUND_KEYNUM. */
	public final static int UPPER_BOUND_KEYNUM = (MIN_DEGREE * 2) - 1;

	/** The leaf. */
	public boolean mIsLeaf;
	
	/** The current key. */
	public int mCurrentKeyNum;
	
	/** The keys. */
	public BTKeyValue<K, V> mKeys[];
	
	/** The children. */
	public BTNode mChildren[];

	/**
	 * Instantiates a new BT node.
	 */
	public BTNode() {
		mIsLeaf = true;
		mCurrentKeyNum = 0;
		mKeys = new BTKeyValue[UPPER_BOUND_KEYNUM];
		mChildren = new BTNode[UPPER_BOUND_KEYNUM + 1];
	}

	/**
	 * Gets the child node at index.
	 *
	 * @param btNode the bt node
	 * @param keyIdx the key idx
	 * @param nDirection the n direction
	 * @return the child node at index
	 */
	protected static BTNode getChildNodeAtIndex(BTNode btNode, int keyIdx, int nDirection) {
		if (btNode.mIsLeaf) {
			return null;
		}

		keyIdx += nDirection;
		if ((keyIdx < 0) || (keyIdx > btNode.mCurrentKeyNum)) {
			return null;
		}

		return btNode.mChildren[keyIdx];
	}

	/**
	 * Gets the left child at index.
	 *
	 * @param btNode the bt node
	 * @param keyIdx the key idx
	 * @return the left child at index
	 */
	protected static BTNode getLeftChildAtIndex(BTNode btNode, int keyIdx) {
		return getChildNodeAtIndex(btNode, keyIdx, 0);
	}

	/**
	 * Gets the right child at index.
	 *
	 * @param btNode the bt node
	 * @param keyIdx the key idx
	 * @return the right child at index
	 */
	protected static BTNode getRightChildAtIndex(BTNode btNode, int keyIdx) {
		return getChildNodeAtIndex(btNode, keyIdx, 1);
	}

	/**
	 * Gets the left sibling at index.
	 *
	 * @param parentNode the parent node
	 * @param keyIdx the key idx
	 * @return the left sibling at index
	 */
	protected static BTNode getLeftSiblingAtIndex(BTNode parentNode, int keyIdx) {
		return getChildNodeAtIndex(parentNode, keyIdx, -1);
	}

	/**
	 * Gets the right sibling at index.
	 *
	 * @param parentNode the parent node
	 * @param keyIdx the key idx
	 * @return the right sibling at index
	 */
	protected static BTNode getRightSiblingAtIndex(BTNode parentNode, int keyIdx) {
		return getChildNodeAtIndex(parentNode, keyIdx, 1);
	}
}
