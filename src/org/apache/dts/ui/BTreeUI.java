package org.apache.dts.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.dts.btree.BTException;
import org.apache.dts.btree.BTIterator;
import org.apache.dts.btree.BTKeyValue;
import org.apache.dts.btree.BTNode;
import org.apache.dts.btree.BTree;
import org.apache.dts.util.SimpleFileWriter;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

/**
 * Class BTreeUI.
 */
public class BTreeUI extends JFrame {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8546007417562640763L;

	/** The Constant APP_WIDTH. */
	public final static int APP_WIDTH = 1140;
	
	/** The Constant APP_HEIGHT. */
	public final static int APP_HEIGHT = 650;
	
	/** The Constant HEIGHT_STEP. */
	public final static int HEIGHT_STEP = 80;
	
	/** The Constant NODE_HEIGHT. */
	public final static int NODE_HEIGHT = 30;
	
	/** The Constant NODE_DIST. */
	public final static int NODE_DIST = 16;
	
	/** The Constant TREE_HEIGHT. */
	public final static int TREE_HEIGHT = 32;

	/** The bTree. */
	private final BTree<Integer, String> mBTree;
	
	/** The buf. */
	private final StringBuilder mBuf;
	
	/** The obj lists. */
	private final Object[] mObjLists;
	
	/** The graph. */
	private mxGraph mGraph;
	
	/** The text. */
	private final JTextField mText;
	
	/** The remove bt. */
	private final JButton mAddBt, mRemoveBt;
	
	/** The remove more bt. */
	private final JButton mAddMoreBt, mRemoveMoreBt;
	
	/** The search key bt. */
	private final JButton mClearBt, mSearchKeyBt;
	
	/** The save bt. */
	private final JButton mListBt, mSaveBt;
	
	/** The output console. */
	private final JTextArea mOutputConsole;
	
	/** The m B tree stats. */
	private final JTextArea mBTreeStats;

	/** The total nodes. */
	private long totalNodes = 0;
	
	/** The total leafs. */
	private long totalLeafs = 0;
	
	/** The max value. */
	private long maxValue  = 0;
	
	/** The min value. */
	private long minValue  = 0;
	
	/**
	 * Instantiates a new b tree renderer.
	 */
	public BTreeUI() {
		super("BTree Renderer");
		mBTree = new BTree<Integer, String>();
		mBuf = new StringBuilder();
		mObjLists = new Object[TREE_HEIGHT];
		mSearchKeyBt = new JButton("Search");
		mAddBt = new JButton("Add");
		mRemoveBt = new JButton("Del");
		mAddMoreBt = new JButton("+>");
		mRemoveMoreBt = new JButton("<-");
		mClearBt = new JButton("Clear");
		mListBt = new JButton("List");
		mSaveBt = new JButton("Save");
		mText = new JTextField("");
		mOutputConsole = new JTextArea(3, 50);
		mOutputConsole.setEditable(false);
		mBTreeStats = new JTextArea(3, 50);
		mBTreeStats.setEditable(false);

		// mText.addAncestorListener(new RequestFocusListener());

		mSearchKeyBt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				searchButtonPressed();
			}
		});

		mAddBt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addButtonPressed();
			}
		});

		mRemoveBt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeButtonPressed();
			}
		});

		mAddMoreBt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addMoreButtonPressed();
			}
		});

		mRemoveMoreBt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeMoreButtonPressed();
			}
		});

		mListBt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				listButtonPressed();
			}
		});

		mClearBt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearButtonPressed();
			}
		});

		mSaveBt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveButtonPressed();
			}
		});

	}

	/**
	 * Gets the input value.
	 *
	 * @return the input value
	 */
	private Integer getInputValue() {
		String strInput = mText.getText().trim();
		int nVal;

		try {
			nVal = Integer.parseInt(strInput);
		} catch (Exception ex) {
			return null;
		}

		mText.setFocusable(true);
		return nVal;
	}

	/**
	 * Search button pressed.
	 */
	public void searchButtonPressed() {
		Integer in = getInputValue();
		if (in == null) {
			return;
		}

		mText.setText("");
		searchKey(in);
	}

	/**
	 * Adds the button pressed.
	 */
	public void addButtonPressed() {
		Integer in = getInputValue();
		if (in == null) {
			return;
		}

		mText.setText("");
		addKey(in);
		render();
	}

	/**
	 * Removes the button pressed.
	 */
	public void removeButtonPressed() {
		Integer in = getInputValue();
		if (in == null) {
			return;
		}

		mText.setText("");
		deleteKey(in);
		render();
	}

	/**
	 * Adds the more button pressed.
	 */
	public void addMoreButtonPressed() {
		Integer in = getInputValue();
		if (in == null) {
			return;
		}

		addKey(in);
		in += 1;
		mText.setText(in + "");
		render();
	}

	/**
	 * Removes the more button pressed.
	 */
	public void removeMoreButtonPressed() {
		Integer in = getInputValue();
		if (in == null) {
			return;
		}

		deleteKey(in);
		in -= 1;
		mText.setText(in + "");
		render();
	}

	/**
	 * Clear button pressed.
	 */
	public void clearButtonPressed() {
		mBTree.clear();
		mOutputConsole.setText("");
		resetStats();
		render();
	}

	/**
	 * Save button pressed.
	 */
	public void saveButtonPressed() {
		String strSavedFile;
		String strText = mOutputConsole.getText();
		if (strText == null) {
			strText = "";
		}
		strText = strText.trim();
		if (strText.isEmpty()) {
			// Nothing to save
			return;
		}

		try {
			strSavedFile = SimpleFileWriter.saveToFile(strText);
		} catch (IOException ioex) {
			println("Error: failed to save to file msg = " + ioex.getMessage());
			ioex.printStackTrace();
			return;
		}

		mOutputConsole.setText("Successfully save console text to file = " + strSavedFile);
	}

	/** The iter. */
	private BTIterator mIter = null;

	/**
	 * List button pressed.
	 */
	public void listButtonPressed() {
		if (mIter == null) {
			mIter = new BTIteratorImpl();
		}

		mOutputConsole.setText("");
		mBTree.list(mIter);
	}

	/**
	 * Render.
	 */
	public void render() {
		mGraph = new mxGraph();
		Object parent = mGraph.getDefaultParent();
		List<Object> pObjList = new ArrayList<Object>();
		List<Object> cObjList = new ArrayList<Object>();
		List<Object> tempObjList;

		for (int i = 0; i < TREE_HEIGHT; ++i) {
			mObjLists[i] = null;
		}

		try {
			generateGraphObject(mBTree.getRootNode(), 0);
		} catch (BTException btex) {
			btex.printStackTrace();
			return;
		}

		Box hBox = Box.createHorizontalBox();
		hBox.add(new JLabel("   Value:  "));
		hBox.add(mText);
		hBox.add(mSearchKeyBt);
		hBox.add(mAddBt);
		hBox.add(mRemoveBt);
		//hBox.add(mAddMoreBt);
		//hBox.add(mRemoveMoreBt);
		hBox.add(mListBt);
		hBox.add(mSaveBt);
		hBox.add(mClearBt);

		mGraph.getModel().beginUpdate();
		try {
			int nStartXPos;
			int nStartYPos = 10;
			int cellWidth;
			for (int i = 0; i < mObjLists.length; ++i) {
				cObjList.clear();
				List<KeyData> objList = (List<KeyData>) mObjLists[i];
				if (objList == null) {
					continue;
				}

				int totalWidth = 0;
				int nCount = 0;
				for (KeyData keyData : objList) {
					totalWidth += keyData.mKeys.length() * 6;
					if (nCount > 0) {
						totalWidth += NODE_DIST;
					}
					++nCount;
				}

				nStartXPos = (APP_WIDTH - totalWidth) / 2;
				if (nStartXPos < 0) {
					nStartXPos = 0;
				}

				for (KeyData keyData : objList) {
					int len = keyData.mKeys.length();
					if (len == 1) {
						len += 2;
					}
					cellWidth = len * 6;
					Object gObj = mGraph.insertVertex(parent, null, keyData.mKeys, nStartXPos, nStartYPos, cellWidth, 24);
					cObjList.add(gObj);
					nStartXPos += (cellWidth + NODE_DIST);
				}

				if (i > 0) {
					// Connect the nodes
					List<KeyData> keyList = (List<KeyData>) mObjLists[i - 1];
					int j = 0, k = 0;
					for (Object pObj : pObjList) {
						KeyData keyData = keyList.get(j);
						for (int l = 0; l < keyData.mKeyNum + 1; ++l) {
							mGraph.insertEdge(parent, null, "", pObj, cObjList.get(k));
							++k;
						}
						++j;
					}
				}

				// Swap two object lists for next loop
				tempObjList = pObjList;
				pObjList = cObjList;
				cObjList = tempObjList;

				nStartYPos += HEIGHT_STEP;
			}
		} finally {
			mGraph.getModel().endUpdate();
		}

		mxGraphComponent graphComponent = new mxGraphComponent(mGraph);
		
		getContentPane().removeAll();
		getContentPane().add(hBox, BorderLayout.NORTH);
		getContentPane().add(graphComponent, BorderLayout.CENTER);
		
		Box oBox = Box.createHorizontalBox();
		oBox.add(new JScrollPane(mOutputConsole));
		oBox.add(new JScrollPane(mBTreeStats));
		getContentPane().add(oBox, BorderLayout.SOUTH);
		
		updateStats();
		revalidate();
	}

	/**
	 * Centre window.
	 *
	 * @param frame the frame
	 * @paraframe the frame
	 */
	public static void centreWindow(JFrame frame) {
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
		int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
		frame.setLocation(x, y);
	}

	/**
	 * Generate graph object.
	 *
	 * @param treeNode the tree node
	 * @param nLevel the n level
	 * @throws BTException the BT exception
	 * @paratreeNode the tree node
	 * @paranLevel the n level
	 */
	private void generateGraphObject(BTNode<Integer, String> treeNode, int nLevel) throws BTException {
		if ((treeNode == null) || (treeNode.mCurrentKeyNum == 0)) {
			return;
		}

		int currentKeyNum = treeNode.mCurrentKeyNum;
		BTKeyValue<Integer, String> keyVal;

		List<KeyData> keyList = (List<KeyData>) mObjLists[nLevel];
		if (keyList == null) {
			keyList = new ArrayList<KeyData>();
			mObjLists[nLevel] = keyList;
		}

		mBuf.setLength(0);
		// Render the keys in the node
		for (int i = 0; i < currentKeyNum; ++i) {
			if (i > 0) {
				mBuf.append(" | ");
			}

			keyVal = treeNode.mKeys[i];
			mBuf.append(keyVal.mKey);
		}

		keyList.add(new KeyData(mBuf.toString(), currentKeyNum));

		if (treeNode.mIsLeaf) {
			return;
		}

		++nLevel;
		for (int i = 0; i < currentKeyNum + 1; ++i) {
			generateGraphObject(treeNode.mChildren[i], nLevel);
		}
	}

	/**
	 * Println.
	 *
	 * @param strText the str text
	 * @parastrText the str text
	 */
	public void println(String strText) {
		mOutputConsole.append(strText);
		mOutputConsole.append("\n");
	}

	/**
	 * Search key.
	 *
	 * @param key the key
	 * @parakey the key
	 */
	public void searchKey(Integer key) {
		println("Search for key = " + key);
		String strVal = mBTree.search(key);
		if (strVal != null) {
			println("Key = " + key + " | Value = " + strVal);
		} else {
			println("No value found for key = " + key);
		}
	}

	/**
	 * Delete key.
	 *
	 * @param key the key
	 * @parakey the key
	 */
	public void deleteKey(Integer key) {
		String strVal = mBTree.delete(key);
		println("Delete key = " + key + " | value = " + strVal);
		
		totalNodes = mBTree.getNodesCount();
		totalLeafs = mBTree.getLeafsCount();
		maxValue   = mBTree.getMaxValue();
		minValue   = mBTree.getMinValue();
	}

	/**
	 * Adds the key.
	 *
	 * @param key the key
	 * @parakey the key
	 */
	public void addKey(Integer key) {
		println("Add key = " + key);
		mBTree.insert(key, "" + key);
		
		totalNodes = mBTree.getNodesCount();
		totalLeafs = mBTree.getLeafsCount();
		maxValue   = mBTree.getMaxValue();
		minValue   = mBTree.getMinValue();
	}
	
	/**
	 * Update stats.
	 */
	public void updateStats() {
		mBTreeStats.setText("");
		mBTreeStats.append(" Nodes | Leafs => " + totalNodes + " | " + totalLeafs);
		mBTreeStats.append("\n");
		mBTreeStats.append(" Largest | Smallest => " + maxValue + " | " + minValue);
	}
	
	/**
	 * Reset stats.
	 */
	public void resetStats() {
		totalNodes = 0;
		totalLeafs = 0;
		maxValue   = 0;
		minValue   = 0;
		
		updateStats();
	}

	/**
	 * Main Entry.
	 *
	 * @param args the arguments
	 * @paraargs the arguments
	 */
	public static void main(String[] args) {
		BTreeUI frame = new BTreeUI();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(APP_WIDTH, APP_HEIGHT);
		centreWindow(frame);
		frame.render();
		frame.setVisible(true);
	}

	/**
	 * Inner class: KeyData.
	 */
	class KeyData {
		
		/** The keys. */
		String mKeys = null;
		
		/** The key num. */
		int mKeyNum = 0;

		/**
		 * Instantiates a new key data.
		 *
		 * @param keys the keys
		 * @param keyNum the key num
		 * @parakeys the keys
		 * @parakeyNuthe key num
		 */
		KeyData(String keys, int keyNum) {
			mKeys = keys;
			mKeyNum = keyNum;
		}
	}

	/**
	 * Inner class to implement BTree iterator.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @para<K> the key type
	 * @para<V> the value type
	 */
	class BTIteratorImpl<K extends Comparable, V> implements BTIterator<K, V> {
		
		/** The buf. */
		private StringBuilder mBuf = new StringBuilder();

		/* (non-Javadoc)
		 * @see org.apache.dts.btree.BTIteratorIF#item(java.lang.Comparable, java.lang.Object)
		 */
		@Override
		public boolean item(K key, V value) {
			mBuf.setLength(0);
			mBuf.append(key).append("  |  value = ").append(value);
			println(mBuf.toString());
			return true;
		}
	}
}
