package org.apache.dts.btree;

import java.util.Stack;

/**
 * The Class BTree.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class BTree<K extends Comparable, V> {

	/** The Constant REBALANCE_FOR_LEAF_NODE. */
	public final static int REBALANCE_FOR_LEAF_NODE = 1;

	/** The Constant REBALANCE_FOR_INTERNAL_NODE. */
	public final static int REBALANCE_FOR_INTERNAL_NODE = 2;

	/** The root. */
	private BTNode<K, V> mRoot = null;

	/** The intermediate internal node. */
	private BTNode<K, V> mIntermediateInternalNode = null;

	/** The node index. */
	private int mNodeIdx = 0;

	/** The tack tracer. */
	private final Stack<StackInfo> mStackTracer = new Stack<StackInfo>();
	
	/** The size. */
	private int mSize = 0;

	/** The max val. */
	private int maxVal = 0;
	
	/** The min val. */
	private int minVal = 0;
	
	/** The nodes. */
	private int nodes  = 0;
	
	/** The leafs. */
	private int leafs  = 0;

	/**
	 * Gets the root node.
	 *
	 * @return the root node
	 */
	public BTNode<K, V> getRootNode() {
		return mRoot;
	}

	/**
	 * Size.
	 *
	 * @return the long
	 */
	public int getSize() {
		return mSize;
	}
	
	/**
	 * Gets the max value.
	 *
	 * @return the max value
	 */
	public int getMaxValue() {
		return maxVal;
	}
	
	/**
	 * Gets the min value.
	 *
	 * @return the min value
	 */
	public int getMinValue() {
		return minVal;
	}
	
	/**
	 * Gets the nodes count.
	 *
	 * @return the nodes count
	 */
	public int getNodesCount() {
		nodes = 0;
		countNodes(mRoot);
		return nodes;
	}
	
	/**
	 * Gets the leafs count.
	 *
	 * @return the leafs count
	 */
	public int getLeafsCount() {
		leafs = 0;
		countLeafs(mRoot);
		return leafs;
	}

	/**
	 * Clear.
	 */
	public void clear() {
		mSize  = 0;
		maxVal = 0;
		minVal = 0;
		leafs  = 0;
		nodes  = 0;
		mRoot = null;
	}

	/**
	 * Creates the node.
	 *
	 * @return the BT node
	 */
	private BTNode<K, V> createNode() {
		BTNode<K, V> btNode;
		btNode = new BTNode();
		btNode.mIsLeaf = true;
		btNode.mCurrentKeyNum = 0;
		return btNode;
	}

	/**
	 * Search.
	 *
	 * @param key
	 *            the key
	 * @return the v
	 */
	public V search(K key) {
		BTNode<K, V> currentNode = mRoot;
		BTKeyValue<K, V> currentKey;
		int i, numberOfKeys;

		while (currentNode != null) {
			numberOfKeys = currentNode.mCurrentKeyNum;
			i = 0;
			currentKey = currentNode.mKeys[i];
			while ((i < numberOfKeys) && (key.compareTo(currentKey.mKey) > 0)) {
				++i;
				if (i < numberOfKeys) {
					currentKey = currentNode.mKeys[i];
				} else {
					--i;
					break;
				}
			}

			if ((i < numberOfKeys) && (key.compareTo(currentKey.mKey) == 0)) {
				return currentKey.mValue;
			}

			if (key.compareTo(currentKey.mKey) > 0) {
				currentNode = BTNode.getRightChildAtIndex(currentNode, i);
			} else {
				currentNode = BTNode.getLeftChildAtIndex(currentNode, i);
			}
		}

		return null;
	}

	/**
	 * Insert.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return the b tree
	 */
	public BTree insert(K key, V value) {
		if (mRoot == null) {
			mRoot = createNode();
			minVal = (int) key;
			maxVal = (int) key;
		}

		++mSize;
		if (mRoot.mCurrentKeyNum == BTNode.UPPER_BOUND_KEYNUM) {
			// The root is full, split it
			BTNode<K, V> btNode = createNode();
			btNode.mIsLeaf = false;
			btNode.mChildren[0] = mRoot;
			mRoot = btNode;
			splitNode(mRoot, 0, btNode.mChildren[0]);
		}

		insertKeyAtNode(mRoot, key, value);
		
		if(maxVal < (int) key) {
			maxVal = (int) key;
		}
		
		if(minVal > (int) key) {
			minVal = (int) key;
		}
		
		return this;
	}

	/**
	 * Insert key at node.
	 *
	 * @param rootNode
	 *            the root node
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	private void insertKeyAtNode(BTNode rootNode, K key, V value) {
		int i;
		int currentKeyNum = rootNode.mCurrentKeyNum;

		if (rootNode.mIsLeaf) {
			if (rootNode.mCurrentKeyNum == 0) {
				// Empty root
				rootNode.mKeys[0] = new BTKeyValue<K, V>(key, value);
				++(rootNode.mCurrentKeyNum);
				return;
			}

			// Verify if the specified key doesn't exist in the node
			for (i = 0; i < rootNode.mCurrentKeyNum; ++i) {
				if (key.compareTo(rootNode.mKeys[i].mKey) == 0) {
					// Find existing key, overwrite its value only
					rootNode.mKeys[i].mValue = value;
					--mSize;
					return;
				}
			}

			i = currentKeyNum - 1;
			BTKeyValue<K, V> existingKeyVal = rootNode.mKeys[i];
			while ((i > -1) && (key.compareTo(existingKeyVal.mKey) < 0)) {
				rootNode.mKeys[i + 1] = existingKeyVal;
				--i;
				if (i > -1) {
					existingKeyVal = rootNode.mKeys[i];
				}
			}

			i = i + 1;
			rootNode.mKeys[i] = new BTKeyValue<K, V>(key, value);

			++(rootNode.mCurrentKeyNum);
			return;
		}

		// This is an internal node (i.e: not a leaf node)
		// So let find the child node where the key is supposed to belong
		i = 0;
		int numberOfKeys = rootNode.mCurrentKeyNum;
		BTKeyValue<K, V> currentKey = rootNode.mKeys[i];
		while ((i < numberOfKeys) && (key.compareTo(currentKey.mKey) > 0)) {
			++i;
			if (i < numberOfKeys) {
				currentKey = rootNode.mKeys[i];
			} else {
				--i;
				break;
			}
		}

		if ((i < numberOfKeys) && (key.compareTo(currentKey.mKey) == 0)) {
			// The key already existed so replace its value and done with it
			currentKey.mValue = value;
			--mSize;
			return;
		}

		BTNode<K, V> btNode;
		if (key.compareTo(currentKey.mKey) > 0) {
			btNode = BTNode.getRightChildAtIndex(rootNode, i);
			i = i + 1;
		} else {
			if ((i - 1 >= 0) && (key.compareTo(rootNode.mKeys[i - 1].mKey) > 0)) {
				btNode = BTNode.getRightChildAtIndex(rootNode, i - 1);
			} else {
				btNode = BTNode.getLeftChildAtIndex(rootNode, i);
			}
		}

		if (btNode.mCurrentKeyNum == BTNode.UPPER_BOUND_KEYNUM) {
			// If the child node is a full node then handle it by splitting out
			// then insert key starting at the root node after splitting node
			splitNode(rootNode, i, btNode);
			insertKeyAtNode(rootNode, key, value);
			return;
		}

		insertKeyAtNode(btNode, key, value);
	}

	/**
	 * Split node.
	 *
	 * @param parentNode
	 *            the parent node
	 * @param nodeIdx
	 *            the node idx
	 * @param btNode
	 *            the bt node
	 */
	private void splitNode(BTNode parentNode, int nodeIdx, BTNode btNode) {
		int i;

		BTNode<K, V> newNode = createNode();

		newNode.mIsLeaf = btNode.mIsLeaf;

		// Since the node is full,
		// new node must share LOWER_BOUND_KEYNUM (aka t - 1) keys from the node
		newNode.mCurrentKeyNum = BTNode.LOWER_BOUND_KEYNUM;

		// Copy right half of the keys from the node to the new node
		for (i = 0; i < BTNode.LOWER_BOUND_KEYNUM; ++i) {
			newNode.mKeys[i] = btNode.mKeys[i + BTNode.MIN_DEGREE];
			btNode.mKeys[i + BTNode.MIN_DEGREE] = null;
		}

		// If the node is an internal node (not a leaf),
		// copy the its child pointers at the half right as well
		if (!btNode.mIsLeaf) {
			for (i = 0; i < BTNode.MIN_DEGREE; ++i) {
				newNode.mChildren[i] = btNode.mChildren[i + BTNode.MIN_DEGREE];
				btNode.mChildren[i + BTNode.MIN_DEGREE] = null;
			}
		}

		// The node at this point should have LOWER_BOUND_KEYNUM (aka min degree
		// - 1) keys at this point.
		// We will move its right-most key to its parent node later.
		btNode.mCurrentKeyNum = BTNode.LOWER_BOUND_KEYNUM;

		// Do the right shift for relevant child pointers of the parent node
		// so that we can put the new node as its new child pointer
		for (i = parentNode.mCurrentKeyNum; i > nodeIdx; --i) {
			parentNode.mChildren[i + 1] = parentNode.mChildren[i];
			parentNode.mChildren[i] = null;
		}
		parentNode.mChildren[nodeIdx + 1] = newNode;

		// Do the right shift all the keys of the parent node the right side of
		// the node index as well
		// so that we will have a slot for move a median key from the splitted
		// node
		for (i = parentNode.mCurrentKeyNum - 1; i >= nodeIdx; --i) {
			parentNode.mKeys[i + 1] = parentNode.mKeys[i];
			parentNode.mKeys[i] = null;
		}
		parentNode.mKeys[nodeIdx] = btNode.mKeys[BTNode.LOWER_BOUND_KEYNUM];
		btNode.mKeys[BTNode.LOWER_BOUND_KEYNUM] = null;
		++(parentNode.mCurrentKeyNum);
	}

	/**
	 * Find predecessor.
	 *
	 * @param btNode
	 *            the bt node
	 * @param nodeIdx
	 *            the node idx
	 * @return the BT node
	 */
	private BTNode<K, V> findPredecessor(BTNode<K, V> btNode, int nodeIdx) {
		if (btNode.mIsLeaf) {
			return btNode;
		}

		BTNode<K, V> predecessorNode;
		if (nodeIdx > -1) {
			predecessorNode = BTNode.getLeftChildAtIndex(btNode, nodeIdx);
			if (predecessorNode != null) {
				mIntermediateInternalNode = btNode;
				mNodeIdx = nodeIdx;
				btNode = findPredecessor(predecessorNode, -1);
			}

			return btNode;
		}

		predecessorNode = BTNode.getRightChildAtIndex(btNode, btNode.mCurrentKeyNum - 1);
		if (predecessorNode != null) {
			mIntermediateInternalNode = btNode;
			mNodeIdx = btNode.mCurrentKeyNum;
			btNode = findPredecessorForNode(predecessorNode, -1);
		}

		return btNode;
	}

	/**
	 * Find predecessor for node.
	 *
	 * @param btNode
	 *            the bt node
	 * @param keyIdx
	 *            the key idx
	 * @return the BT node
	 */
	private BTNode<K, V> findPredecessorForNode(BTNode<K, V> btNode, int keyIdx) {
		BTNode<K, V> predecessorNode;
		BTNode<K, V> originalNode = btNode;
		if (keyIdx > -1) {
			predecessorNode = BTNode.getLeftChildAtIndex(btNode, keyIdx);
			if (predecessorNode != null) {
				btNode = findPredecessorForNode(predecessorNode, -1);
				rebalanceTreeAtNode(originalNode, predecessorNode, keyIdx, REBALANCE_FOR_LEAF_NODE);
			}

			return btNode;
		}

		predecessorNode = BTNode.getRightChildAtIndex(btNode, btNode.mCurrentKeyNum - 1);
		if (predecessorNode != null) {
			btNode = findPredecessorForNode(predecessorNode, -1);
			rebalanceTreeAtNode(originalNode, predecessorNode, keyIdx, REBALANCE_FOR_LEAF_NODE);
		}

		return btNode;
	}

	/**
	 * Perform left rotation.
	 *
	 * @param btNode
	 *            the bt node
	 * @param nodeIdx
	 *            the node idx
	 * @param parentNode
	 *            the parent node
	 * @param rightSiblingNode
	 *            the right sibling node
	 */
	private void performLeftRotation(BTNode<K, V> btNode, int nodeIdx, BTNode<K, V> parentNode,
			BTNode<K, V> rightSiblingNode) {
		int parentKeyIdx = nodeIdx;

		/*
		 * if (nodeIdx >= parentNode.mCurrentKeyNum) { // This shouldn't happen
		 * parentKeyIdx = nodeIdx - 1; }
		 */

		// Move the parent key and relevant child to the deficient node
		btNode.mKeys[btNode.mCurrentKeyNum] = parentNode.mKeys[parentKeyIdx];
		btNode.mChildren[btNode.mCurrentKeyNum + 1] = rightSiblingNode.mChildren[0];
		++(btNode.mCurrentKeyNum);

		// Move the leftmost key of the right sibling and relevant child pointer
		// to the parent node
		parentNode.mKeys[parentKeyIdx] = rightSiblingNode.mKeys[0];
		--(rightSiblingNode.mCurrentKeyNum);
		// Shift all keys and children of the right sibling to its left
		for (int i = 0; i < rightSiblingNode.mCurrentKeyNum; ++i) {
			rightSiblingNode.mKeys[i] = rightSiblingNode.mKeys[i + 1];
			rightSiblingNode.mChildren[i] = rightSiblingNode.mChildren[i + 1];
		}
		rightSiblingNode.mChildren[rightSiblingNode.mCurrentKeyNum] = rightSiblingNode.mChildren[rightSiblingNode.mCurrentKeyNum
				+ 1];
		rightSiblingNode.mChildren[rightSiblingNode.mCurrentKeyNum + 1] = null;
	}

	/**
	 * Perform right rotation.
	 *
	 * @param btNode
	 *            the bt node
	 * @param nodeIdx
	 *            the node idx
	 * @param parentNode
	 *            the parent node
	 * @param leftSiblingNode
	 *            the left sibling node
	 */
	private void performRightRotation(BTNode<K, V> btNode, int nodeIdx, BTNode<K, V> parentNode,
			BTNode<K, V> leftSiblingNode) {
		int parentKeyIdx = nodeIdx;
		if (nodeIdx >= parentNode.mCurrentKeyNum) {
			// This shouldn't happen
			parentKeyIdx = nodeIdx - 1;
		}

		// Shift all keys and children of the deficient node to the right
		// So that there will be available left slot for insertion
		btNode.mChildren[btNode.mCurrentKeyNum + 1] = btNode.mChildren[btNode.mCurrentKeyNum];
		for (int i = btNode.mCurrentKeyNum - 1; i >= 0; --i) {
			btNode.mKeys[i + 1] = btNode.mKeys[i];
			btNode.mChildren[i + 1] = btNode.mChildren[i];
		}

		// Move the parent key and relevant child to the deficient node
		btNode.mKeys[0] = parentNode.mKeys[parentKeyIdx];
		btNode.mChildren[0] = leftSiblingNode.mChildren[leftSiblingNode.mCurrentKeyNum];
		++(btNode.mCurrentKeyNum);

		// Move the leftmost key of the right sibling and relevant child pointer
		// to the parent node
		parentNode.mKeys[parentKeyIdx] = leftSiblingNode.mKeys[leftSiblingNode.mCurrentKeyNum - 1];
		leftSiblingNode.mChildren[leftSiblingNode.mCurrentKeyNum] = null;
		--(leftSiblingNode.mCurrentKeyNum);
	}

	/**
	 * Perform merge with left sibling.
	 *
	 * @param btNode
	 *            the bt node
	 * @param nodeIdx
	 *            the node idx
	 * @param parentNode
	 *            the parent node
	 * @param leftSiblingNode
	 *            the left sibling node
	 * @return true, if successful
	 */
	private boolean performMergeWithLeftSibling(BTNode<K, V> btNode, int nodeIdx, BTNode<K, V> parentNode,
			BTNode<K, V> leftSiblingNode) {
		if (nodeIdx == parentNode.mCurrentKeyNum) {
			// For the case that the node index can be the right most
			nodeIdx = nodeIdx - 1;
		}

		// Here we need to determine the parent node's index based on child
		// node's index (nodeIdx)
		if (nodeIdx > 0) {
			if (leftSiblingNode.mKeys[leftSiblingNode.mCurrentKeyNum - 1].mKey
					.compareTo(parentNode.mKeys[nodeIdx - 1].mKey) < 0) {
				nodeIdx = nodeIdx - 1;
			}
		}

		// Copy the parent key to the node (on the left)
		leftSiblingNode.mKeys[leftSiblingNode.mCurrentKeyNum] = parentNode.mKeys[nodeIdx];
		++(leftSiblingNode.mCurrentKeyNum);

		// Copy keys and children of the node to the left sibling node
		for (int i = 0; i < btNode.mCurrentKeyNum; ++i) {
			leftSiblingNode.mKeys[leftSiblingNode.mCurrentKeyNum + i] = btNode.mKeys[i];
			leftSiblingNode.mChildren[leftSiblingNode.mCurrentKeyNum + i] = btNode.mChildren[i];
			btNode.mKeys[i] = null;
		}
		leftSiblingNode.mCurrentKeyNum += btNode.mCurrentKeyNum;
		leftSiblingNode.mChildren[leftSiblingNode.mCurrentKeyNum] = btNode.mChildren[btNode.mCurrentKeyNum];
		btNode.mCurrentKeyNum = 0; // Abandon the node

		// Shift all relevant keys and children of the parent node to the left
		// since it lost one of its keys and children (by moving it to the child
		// node)
		int i;
		for (i = nodeIdx; i < parentNode.mCurrentKeyNum - 1; ++i) {
			parentNode.mKeys[i] = parentNode.mKeys[i + 1];
			parentNode.mChildren[i + 1] = parentNode.mChildren[i + 2];
		}
		parentNode.mKeys[i] = null;
		parentNode.mChildren[parentNode.mCurrentKeyNum] = null;
		--(parentNode.mCurrentKeyNum);

		// Make sure the parent point to the correct child after the merge
		parentNode.mChildren[nodeIdx] = leftSiblingNode;

		if ((parentNode == mRoot) && (parentNode.mCurrentKeyNum == 0)) {
			// Root node is updated. It should be done
			mRoot = leftSiblingNode;
			return false;
		}

		return true;
	}

	/**
	 * Perform merge with right sibling.
	 *
	 * @param btNode
	 *            the bt node
	 * @param nodeIdx
	 *            the node idx
	 * @param parentNode
	 *            the parent node
	 * @param rightSiblingNode
	 *            the right sibling node
	 * @return true, if successful
	 */
	private boolean performMergeWithRightSibling(BTNode<K, V> btNode, int nodeIdx, BTNode<K, V> parentNode,
			BTNode<K, V> rightSiblingNode) {
		// Copy the parent key to right-most slot of the node
		btNode.mKeys[btNode.mCurrentKeyNum] = parentNode.mKeys[nodeIdx];
		++(btNode.mCurrentKeyNum);

		// Copy keys and children of the right sibling to the node
		for (int i = 0; i < rightSiblingNode.mCurrentKeyNum; ++i) {
			btNode.mKeys[btNode.mCurrentKeyNum + i] = rightSiblingNode.mKeys[i];
			btNode.mChildren[btNode.mCurrentKeyNum + i] = rightSiblingNode.mChildren[i];
		}
		btNode.mCurrentKeyNum += rightSiblingNode.mCurrentKeyNum;
		btNode.mChildren[btNode.mCurrentKeyNum] = rightSiblingNode.mChildren[rightSiblingNode.mCurrentKeyNum];
		rightSiblingNode.mCurrentKeyNum = 0; // Abandon the sibling node

		// Shift all relevant keys and children of the parent node to the left
		// since it lost one of its keys and children (by moving it to the child
		// node)
		int i;
		for (i = nodeIdx; i < parentNode.mCurrentKeyNum - 1; ++i) {
			parentNode.mKeys[i] = parentNode.mKeys[i + 1];
			parentNode.mChildren[i + 1] = parentNode.mChildren[i + 2];
		}
		parentNode.mKeys[i] = null;
		parentNode.mChildren[parentNode.mCurrentKeyNum] = null;
		--(parentNode.mCurrentKeyNum);

		// Make sure the parent point to the correct child after the merge
		parentNode.mChildren[nodeIdx] = btNode;

		if ((parentNode == mRoot) && (parentNode.mCurrentKeyNum == 0)) {
			// Root node is updated. It should be done
			mRoot = btNode;
			return false;
		}

		return true;
	}

	/**
	 * Search key.
	 *
	 * @param btNode
	 *            the bt node
	 * @param key
	 *            the key
	 * @return the int
	 */
	private int searchKey(BTNode<K, V> btNode, K key) {
		for (int i = 0; i < btNode.mCurrentKeyNum; ++i) {
			if (key.compareTo(btNode.mKeys[i].mKey) == 0) {
				return i;
			} else if (key.compareTo(btNode.mKeys[i].mKey) < 0) {
				return -1;
			}
		}

		return -1;
	}

	/**
	 * Delete.
	 *
	 * @param key
	 *            the key
	 * @return the v
	 */
	public V delete(K key) {
		mIntermediateInternalNode = null;
		BTKeyValue<K, V> keyVal = deleteKey(null, mRoot, key, 0);
		if (keyVal == null) {
			return null;
		}
		--mSize;
		
		if(mRoot == null || mRoot.mCurrentKeyNum == 0) {
			minVal = 0;
			maxVal = 0;
		} else {
			if(maxVal == (int) key) {
				maxVal = 0;
				findMaxValue(mRoot);
			}
			
			if(minVal == (int) key) {
				minVal = maxVal;
				findMinValue(mRoot);
			}			
		}

		return keyVal.mValue;
	}
	
	/**
	 * Delete key.
	 *
	 * @param parentNode
	 *            the parent node
	 * @param btNode
	 *            the bt node
	 * @param key
	 *            the key
	 * @param nodeIdx
	 *            the node idx
	 * @return the BT key value
	 */
	private BTKeyValue<K, V> deleteKey(BTNode<K, V> parentNode, BTNode<K, V> btNode, K key, int nodeIdx) {
		int i;
		int nIdx;
		BTKeyValue<K, V> retVal;

		if (btNode == null) {
			// The tree is empty
			return null;
		}

		if (btNode.mIsLeaf) {
			nIdx = searchKey(btNode, key);
			if (nIdx < 0) {
				// Can't find the specified key
				return null;
			}

			retVal = btNode.mKeys[nIdx];

			if ((btNode.mCurrentKeyNum > BTNode.LOWER_BOUND_KEYNUM) || (parentNode == null)) {
				// Remove it from the node
				for (i = nIdx; i < btNode.mCurrentKeyNum - 1; ++i) {
					btNode.mKeys[i] = btNode.mKeys[i + 1];
				}
				btNode.mKeys[i] = null;
				--(btNode.mCurrentKeyNum);

				if (btNode.mCurrentKeyNum == 0) {
					// btNode is actually the root node
					mRoot = null;
				}

				return retVal;
			}

			// Find the left sibling
			BTNode<K, V> rightSibling;
			BTNode<K, V> leftSibling = BTNode.getLeftSiblingAtIndex(parentNode, nodeIdx);
			if ((leftSibling != null) && (leftSibling.mCurrentKeyNum > BTNode.LOWER_BOUND_KEYNUM)) {
				// Remove the key and borrow a key from the left sibling
				moveLeftLeafSiblingKeyWithKeyRemoval(btNode, nodeIdx, nIdx, parentNode, leftSibling);
			} else {
				rightSibling = BTNode.getRightSiblingAtIndex(parentNode, nodeIdx);
				if ((rightSibling != null) && (rightSibling.mCurrentKeyNum > BTNode.LOWER_BOUND_KEYNUM)) {
					// Remove a key and borrow a key the right sibling
					moveRightLeafSiblingKeyWithKeyRemoval(btNode, nodeIdx, nIdx, parentNode, rightSibling);
				} else {
					// Merge to its sibling
					boolean isRebalanceNeeded = false;
					boolean bStatus;
					if (leftSibling != null) {
						// Merge with the left sibling
						bStatus = doLeafSiblingMergeWithKeyRemoval(btNode, nodeIdx, nIdx, parentNode, leftSibling,
								false);
						if (!bStatus) {
							isRebalanceNeeded = false;
						} else if (parentNode.mCurrentKeyNum < BTNode.LOWER_BOUND_KEYNUM) {
							// Need to rebalance the tree
							isRebalanceNeeded = true;
						}
					} else {
						// Merge with the right sibling
						bStatus = doLeafSiblingMergeWithKeyRemoval(btNode, nodeIdx, nIdx, parentNode, rightSibling,
								true);
						if (!bStatus) {
							isRebalanceNeeded = false;
						} else if (parentNode.mCurrentKeyNum < BTNode.LOWER_BOUND_KEYNUM) {
							// Need to rebalance the tree
							isRebalanceNeeded = true;
						}
					}

					if (isRebalanceNeeded && (mRoot != null)) {
						rebalanceTree(mRoot, parentNode, parentNode.mKeys[0].mKey);
					}
				}
			}

			return retVal; // Done with handling for the leaf node
		}

		//
		// At this point the node is an internal node
		//

		nIdx = searchKey(btNode, key);
		if (nIdx >= 0) {
			// We found the key in the internal node

			// Find its predecessor
			mIntermediateInternalNode = btNode;
			mNodeIdx = nIdx;
			BTNode<K, V> predecessorNode = findPredecessor(btNode, nIdx);
			BTKeyValue<K, V> predecessorKey = predecessorNode.mKeys[predecessorNode.mCurrentKeyNum - 1];

			// Swap the data of the deleted key and its predecessor (in the leaf
			// node)
			BTKeyValue<K, V> deletedKey = btNode.mKeys[nIdx];
			btNode.mKeys[nIdx] = predecessorKey;
			predecessorNode.mKeys[predecessorNode.mCurrentKeyNum - 1] = deletedKey;

			// mIntermediateNode is done in findPrecessor
			return deleteKey(mIntermediateInternalNode, predecessorNode, deletedKey.mKey, mNodeIdx);
		}

		//
		// Find the child subtree (node) that contains the key
		//
		i = 0;
		BTKeyValue<K, V> currentKey = btNode.mKeys[0];
		while ((i < btNode.mCurrentKeyNum) && (key.compareTo(currentKey.mKey) > 0)) {
			++i;
			if (i < btNode.mCurrentKeyNum) {
				currentKey = btNode.mKeys[i];
			} else {
				--i;
				break;
			}
		}

		BTNode<K, V> childNode;
		if (key.compareTo(currentKey.mKey) > 0) {
			childNode = BTNode.getRightChildAtIndex(btNode, i);
			if (childNode.mKeys[0].mKey.compareTo(btNode.mKeys[btNode.mCurrentKeyNum - 1].mKey) > 0) {
				// The right-most side of the node
				i = i + 1;
			}
		} else {
			childNode = BTNode.getLeftChildAtIndex(btNode, i);
		}

		return deleteKey(btNode, childNode, key, i);
	}

	/**
	 * Move right leaf sibling key with key removal.
	 *
	 * @param btNode
	 *            the bt node
	 * @param nodeIdx
	 *            the node idx
	 * @param keyIdx
	 *            the key idx
	 * @param parentNode
	 *            the parent node
	 * @param rightSiblingNode
	 *            the right sibling node
	 */
	private void moveRightLeafSiblingKeyWithKeyRemoval(BTNode<K, V> btNode, int nodeIdx, int keyIdx,
			BTNode<K, V> parentNode, BTNode<K, V> rightSiblingNode) {
		// Shift to the right where the key is deleted
		for (int i = keyIdx; i < btNode.mCurrentKeyNum - 1; ++i) {
			btNode.mKeys[i] = btNode.mKeys[i + 1];
		}

		btNode.mKeys[btNode.mCurrentKeyNum - 1] = parentNode.mKeys[nodeIdx];
		parentNode.mKeys[nodeIdx] = rightSiblingNode.mKeys[0];

		for (int i = 0; i < rightSiblingNode.mCurrentKeyNum - 1; ++i) {
			rightSiblingNode.mKeys[i] = rightSiblingNode.mKeys[i + 1];
		}

		--(rightSiblingNode.mCurrentKeyNum);
	}

	/**
	 * Move left leaf sibling key with key removal.
	 *
	 * @param btNode
	 *            the bt node
	 * @param nodeIdx
	 *            the node idx
	 * @param keyIdx
	 *            the key idx
	 * @param parentNode
	 *            the parent node
	 * @param leftSiblingNode
	 *            the left sibling node
	 */
	private void moveLeftLeafSiblingKeyWithKeyRemoval(BTNode<K, V> btNode, int nodeIdx, int keyIdx,
			BTNode<K, V> parentNode, BTNode<K, V> leftSiblingNode) {
		// Use the parent key on the left side of the node
		nodeIdx = nodeIdx - 1;

		// Shift to the right to where the key will be deleted
		for (int i = keyIdx; i > 0; --i) {
			btNode.mKeys[i] = btNode.mKeys[i - 1];
		}

		btNode.mKeys[0] = parentNode.mKeys[nodeIdx];
		parentNode.mKeys[nodeIdx] = leftSiblingNode.mKeys[leftSiblingNode.mCurrentKeyNum - 1];
		--(leftSiblingNode.mCurrentKeyNum);
	}

	/**
	 * Do leaf sibling merge with key removal.
	 *
	 * @param btNode
	 *            the bt node
	 * @param nodeIdx
	 *            the node idx
	 * @param keyIdx
	 *            the key idx
	 * @param parentNode
	 *            the parent node
	 * @param siblingNode
	 *            the sibling node
	 * @param isRightSibling
	 *            the is right sibling
	 * @return true, if successful
	 */
	private boolean doLeafSiblingMergeWithKeyRemoval(BTNode<K, V> btNode, int nodeIdx, int keyIdx,
			BTNode<K, V> parentNode, BTNode<K, V> siblingNode, boolean isRightSibling) {
		int i;

		if (nodeIdx == parentNode.mCurrentKeyNum) {
			// Case node index can be the right most
			nodeIdx = nodeIdx - 1;
		}

		if (isRightSibling) {
			// Shift the remained keys of the node to the left to remove the key
			for (i = keyIdx; i < btNode.mCurrentKeyNum - 1; ++i) {
				btNode.mKeys[i] = btNode.mKeys[i + 1];
			}
			btNode.mKeys[i] = parentNode.mKeys[nodeIdx];
		} else {
			// Here we need to determine the parent node id based on child node
			// id (nodeIdx)
			if (nodeIdx > 0) {
				if (siblingNode.mKeys[siblingNode.mCurrentKeyNum - 1].mKey
						.compareTo(parentNode.mKeys[nodeIdx - 1].mKey) < 0) {
					nodeIdx = nodeIdx - 1;
				}
			}

			siblingNode.mKeys[siblingNode.mCurrentKeyNum] = parentNode.mKeys[nodeIdx];
			// siblingNode.mKeys[siblingNode.mCurrentKeyNum] =
			// parentNode.mKeys[0];
			++(siblingNode.mCurrentKeyNum);

			// Shift the remained keys of the node to the left to remove the key
			for (i = keyIdx; i < btNode.mCurrentKeyNum - 1; ++i) {
				btNode.mKeys[i] = btNode.mKeys[i + 1];
			}
			btNode.mKeys[i] = null;
			--(btNode.mCurrentKeyNum);
		}

		if (isRightSibling) {
			for (i = 0; i < siblingNode.mCurrentKeyNum; ++i) {
				btNode.mKeys[btNode.mCurrentKeyNum + i] = siblingNode.mKeys[i];
				siblingNode.mKeys[i] = null;
			}
			btNode.mCurrentKeyNum += siblingNode.mCurrentKeyNum;
		} else {
			for (i = 0; i < btNode.mCurrentKeyNum; ++i) {
				siblingNode.mKeys[siblingNode.mCurrentKeyNum + i] = btNode.mKeys[i];
				btNode.mKeys[i] = null;
			}
			siblingNode.mCurrentKeyNum += btNode.mCurrentKeyNum;
			btNode.mKeys[btNode.mCurrentKeyNum] = null;
		}

		// Shift the parent keys accordingly after the merge of child nodes
		for (i = nodeIdx; i < parentNode.mCurrentKeyNum - 1; ++i) {
			parentNode.mKeys[i] = parentNode.mKeys[i + 1];
			parentNode.mChildren[i + 1] = parentNode.mChildren[i + 2];
		}
		parentNode.mKeys[i] = null;
		parentNode.mChildren[parentNode.mCurrentKeyNum] = null;
		--(parentNode.mCurrentKeyNum);

		if (isRightSibling) {
			parentNode.mChildren[nodeIdx] = btNode;
		} else {
			parentNode.mChildren[nodeIdx] = siblingNode;
		}

		if ((mRoot == parentNode) && (mRoot.mCurrentKeyNum == 0)) {
			// Only root left
			mRoot = parentNode.mChildren[nodeIdx];
			mRoot.mIsLeaf = true;
			return false; // Root has been changed, we don't need to go futher
		}

		return true;
	}

	/**
	 * Rebalance tree at node.
	 *
	 * @param parentNode
	 *            the parent node
	 * @param btNode
	 *            the bt node
	 * @param nodeIdx
	 *            the node idx
	 * @param balanceType
	 *            the balance type
	 * @return true, if successful
	 */
	private boolean rebalanceTreeAtNode(BTNode<K, V> parentNode, BTNode<K, V> btNode, int nodeIdx, int balanceType) {
		if (balanceType == REBALANCE_FOR_LEAF_NODE) {
			if ((btNode == null) || (btNode == mRoot)) {
				return false;
			}
		} else if (balanceType == REBALANCE_FOR_INTERNAL_NODE) {
			if (parentNode == null) {
				// Root node
				return false;
			}
		}

		if (btNode.mCurrentKeyNum >= BTNode.LOWER_BOUND_KEYNUM) {
			// The node doesn't need to rebalance
			return false;
		}

		BTNode<K, V> rightSiblingNode;
		BTNode<K, V> leftSiblingNode = BTNode.getLeftSiblingAtIndex(parentNode, nodeIdx);
		if ((leftSiblingNode != null) && (leftSiblingNode.mCurrentKeyNum > BTNode.LOWER_BOUND_KEYNUM)) {
			// Do right rotate
			performRightRotation(btNode, nodeIdx, parentNode, leftSiblingNode);
		} else {
			rightSiblingNode = BTNode.getRightSiblingAtIndex(parentNode, nodeIdx);
			if ((rightSiblingNode != null) && (rightSiblingNode.mCurrentKeyNum > BTNode.LOWER_BOUND_KEYNUM)) {
				// Do left rotate
				performLeftRotation(btNode, nodeIdx, parentNode, rightSiblingNode);
			} else {
				// Merge the node with one of the siblings
				boolean bStatus;
				if (leftSiblingNode != null) {
					bStatus = performMergeWithLeftSibling(btNode, nodeIdx, parentNode, leftSiblingNode);
				} else {
					bStatus = performMergeWithRightSibling(btNode, nodeIdx, parentNode, rightSiblingNode);
				}

				if (!bStatus) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Rebalance tree.
	 *
	 * @param upperNode
	 *            the upper node
	 * @param lowerNode
	 *            the lower node
	 * @param key
	 *            the key
	 */
	private void rebalanceTree(BTNode<K, V> upperNode, BTNode<K, V> lowerNode, K key) {
		mStackTracer.clear();
		mStackTracer.add(new StackInfo(null, upperNode, 0));

		//
		// Find the child subtree (node) that contains the key
		//
		BTNode<K, V> parentNode, childNode;
		BTKeyValue<K, V> currentKey;
		int i;
		parentNode = upperNode;
		while ((parentNode != lowerNode) && !parentNode.mIsLeaf) {
			currentKey = parentNode.mKeys[0];
			i = 0;
			while ((i < parentNode.mCurrentKeyNum) && (key.compareTo(currentKey.mKey) > 0)) {
				++i;
				if (i < parentNode.mCurrentKeyNum) {
					currentKey = parentNode.mKeys[i];
				} else {
					--i;
					break;
				}
			}

			if (key.compareTo(currentKey.mKey) > 0) {
				childNode = BTNode.getRightChildAtIndex(parentNode, i);
				if (childNode.mKeys[0].mKey.compareTo(parentNode.mKeys[parentNode.mCurrentKeyNum - 1].mKey) > 0) {
					// The right-most side of the node
					i = i + 1;
				}
			} else {
				childNode = BTNode.getLeftChildAtIndex(parentNode, i);
			}

			if (childNode == null) {
				break;
			}

			if (key.compareTo(currentKey.mKey) == 0) {
				break;
			}

			mStackTracer.add(new StackInfo(parentNode, childNode, i));
			parentNode = childNode;
		}

		boolean bStatus;
		StackInfo stackInfo;
		while (!mStackTracer.isEmpty()) {
			stackInfo = mStackTracer.pop();
			if ((stackInfo != null) && !stackInfo.mNode.mIsLeaf) {
				bStatus = rebalanceTreeAtNode(stackInfo.mParent, stackInfo.mNode, stackInfo.mNodeIdx,
						REBALANCE_FOR_INTERNAL_NODE);
				if (!bStatus) {
					break;
				}
			}
		}
	}
	
	/**
	 * List.
	 *
	 * @param iterImpl the iter impl
	 */
	public void list(BTIterator<K, V> iterImpl) {
		if (mSize < 1) {
			return;
		}

		if (iterImpl == null) {
			return;
		}

		listEntriesInOrder(mRoot, iterImpl);
	}

	/**
	 * List entries in order.
	 *
	 * @param treeNode the tree node
	 * @param iterImpl the iter impl
	 * @return true, if successful
	 */
	private boolean listEntriesInOrder(BTNode<K, V> treeNode, BTIterator<K, V> iterImpl) {
		if ((treeNode == null) || (treeNode.mCurrentKeyNum == 0)) {
			return false;
		}

		boolean bStatus;
		BTKeyValue<K, V> keyVal;
		int currentKeyNum = treeNode.mCurrentKeyNum;
		for (int i = 0; i < currentKeyNum; ++i) {
			listEntriesInOrder(BTNode.getLeftChildAtIndex(treeNode, i), iterImpl);

			keyVal = treeNode.mKeys[i];
			bStatus = iterImpl.item(keyVal.mKey, keyVal.mValue);
			if (!bStatus) {
				return false;
			}

			if (i == currentKeyNum - 1) {
				listEntriesInOrder(BTNode.getRightChildAtIndex(treeNode, i), iterImpl);
			}
		}

		return true;
	}

	/**
	 * Find max value.
	 *
	 * @param node the node
	 * @param maxVal the max val
	 * @return the int
	 */
	public void findMaxValue(BTNode<K, V> node) {
		if(node == null || node.mCurrentKeyNum == 0) {
			return;
		}
		
        int i = 0; 
        for (i = 0; i < node.mCurrentKeyNum; i++) { 
            if (!node.mIsLeaf) { 
                findMaxValue(node.mChildren[i]); 
            } 
            int key = (int) node.mKeys[i].mKey;
            if(maxVal < key) {
            	maxVal = key;
            }
        } 
  
        if (!node.mIsLeaf) {
            findMaxValue(node.mChildren[i]);
        }
	}
	
	/**
	 * Find min value.
	 *
	 * @param node the node
	 */
	public void findMinValue(BTNode<K, V> node) {
		if(node == null || node.mCurrentKeyNum == 0) {
			return;
		}
		
        int i = 0; 
        for (i = 0; i < node.mCurrentKeyNum; i++) { 
            if (!node.mIsLeaf) { 
            	findMinValue(node.mChildren[i]); 
            } 
            int key = (int) node.mKeys[i].mKey;
            if(minVal > key) {
            	minVal = key;
            }
        } 
  
        if (!node.mIsLeaf) {
        	findMinValue(node.mChildren[i]);
        }
	}
	
	/**
	 * Count nodes.
	 *
	 * @param node the node
	 */
	public void countNodes(BTNode<K, V> node) {
		if(node == null || node.mCurrentKeyNum == 0) {
			return;
		}
		
		if (!node.mIsLeaf) {
			int i = 0;
	        for (i = 0; i < node.mChildren.length; i++) {
	        	BTNode<K, V> cNode = node.mChildren[i];
	            if (cNode != null) {
	            	if(!cNode.mIsLeaf) { 
	            		countNodes(cNode);
	            	}
	            	nodes++;
	            }
	        } 
		}
		
        nodes++;
	}
	
	/**
	 * Count leafs.
	 *
	 * @param node the node
	 */
	public void countLeafs(BTNode<K, V> node) {
		if(node == null || node.mCurrentKeyNum == 0) {
			return;
		}
		
		if (node.mIsLeaf) {
			leafs++;
		} else {
			int i = 0;
	        for (i = 0; i < node.mChildren.length; i++) {
	        	BTNode<K, V> cNode = node.mChildren[i];
	            if (cNode != null) {
	            	if(cNode.mIsLeaf) {
	            		leafs++;
	            	} else {
	            		countLeafs(cNode);
	            	}
	            }
	        } 
		}
	}

	/**
	 * Inner class StackInfo for tracing-back purpose 
	 * Structure contains parent node and node index.
	 */
	public class StackInfo {

		/** The m parent. */
		public BTNode<K, V> mParent = null;

		/** The m node. */
		public BTNode<K, V> mNode = null;

		/** The m node idx. */
		public int mNodeIdx = -1;

		/**
		 * Instantiates a new stack info.
		 *
		 * @param parent
		 *            the parent
		 * @param node
		 *            the node
		 * @param nodeIdx
		 *            the node idx
		 */
		public StackInfo(BTNode<K, V> parent, BTNode<K, V> node, int nodeIdx) {
			mParent = parent;
			mNode = node;
			mNodeIdx = nodeIdx;
		}
	}
}
