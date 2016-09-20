import java.util.LinkedList;

public class AVLTree<E extends Comparable<? super E>> {
	private class AVLNode {
		E value;              //Value to be stored
		AVLNode left = null,  //Left subtree
				right = null; //Right subtree
		int height = 0;       //The height of a subtree with this node as its root := the greatest depth of a node in this subtree.
		
		/**Constructor, produces child-less node**/
		public AVLNode(E value) {
			this.value = value;
		}
		
		/**Recalculates the height of a node as 1 + the greater of the height of the left or right subtree. 
		 * Recommended to be used whenever a change in height (by adding or removing a node) 
		 * occurs to either the left or right subtree of the node.**/
		public void resetHeight() {
			//Note: -1 is the height of an empty subtree (== null).
			height = 1 + Math.max(left != null ? left.height : -1, right != null ? right.height : -1);
		}
	}
	
	private class AddResult {
		AVLNode aTree;
		boolean added;
		public AddResult(AVLNode sTree, boolean added) {
			this.aTree = sTree;
			this.added = added;
		}
	}
	
	private class RemoveResult {
		AVLNode remTree,
				removed;
		public RemoveResult(AVLNode remTree, AVLNode removed) {
			this.remTree = remTree;
			this.removed = removed;
		}
	}
	
	//The root node.
	private AVLNode root = null;
	
	/**Returns the height of a tree. An empty tree has a height of -1.**/
	private int getHeight(AVLNode tree) {
		return tree != null ? tree.height : -1;
	}
	
	public int getHeight() {
		return root.height;
	}
	
	public boolean addAll(E[] values) {
		boolean success = true;
		for(E value : values)
			success |= add(value);
		return success;
	}
	
	/**Calls its recursive private counterpart to add to the AVL tree.
	 * @param value The value to add.
	 * @return true if added (the value was not previously in tree), else false
	 */
	public boolean add(E value) {
		AddResult addResult = add(root, value);
		root = addResult.aTree;
		return addResult.added;
	}
	
	public E get(E value) {
		return get(value, root);
	}
	
	public boolean remove(E value) {
		RemoveResult remResult = remove(root, value);
		root = remResult.remTree;
		return remResult.removed != null;
	}
	
	private AddResult add(AVLNode sTree, E value) {
		if(sTree == null)
			return new AddResult(new AVLNode(value), true);
		
		AddResult addResult;
		if(sTree.value.compareTo(value) < 0) {
			addResult = add(sTree.right, value);
			sTree.right = addResult.aTree;
		}
		else if(sTree.value.compareTo(value) > 0) {
			addResult = add(sTree.left, value);
			sTree.left = addResult.aTree;
		}
		else {
			return new AddResult(sTree, false);
		}
		
		addResult.aTree = balance(sTree);
		
		return addResult;
	}
	
	private E get(E value, AVLNode sTree) {
		if(sTree == null)
			return null;
		if(sTree.value.compareTo(value) == 0)
			return sTree.value;
		if(sTree.value.compareTo(value) < 0)
			return get(value, sTree.right);
			
		return get(value, sTree.left);
	}
	
	public boolean set(E value) {
		return set(value, root);
	}
	
	private boolean set(E value, AVLNode sTree) {
		if(sTree == null)
			return false;
		if(sTree.value.compareTo(value) == 0) {
			if(sTree.value.compareTo(value) == 0) {
				sTree.value = value;
				return true;
			}
			else 
				return false;
		}
		if(sTree.value.compareTo(value) < 0)
			return set(value, sTree.right);
			
		return set(value, sTree.left);
	}
	
	private RemoveResult remove(AVLNode sTree, E value) {
		if(sTree == null)
			return new RemoveResult(sTree, null);
		
		RemoveResult remResult;
		if(sTree.value.compareTo(value) < 0) {
			remResult = remove(sTree.right, value);
			sTree.right = remResult.remTree;
			remResult.remTree = balance(sTree);
			return remResult;
		}
		if(sTree.value.compareTo(value) > 0) {
			remResult = remove(sTree.left, value);
			sTree.left = remResult.remTree;
			remResult.remTree = balance(sTree);
			return remResult;
		}
		
		if(sTree.left == null && sTree.right == null)
			return new RemoveResult(null, sTree);
			
		if(sTree.left != null && sTree.right != null) {
			remResult = removeLargest(sTree.left);
			AVLNode newRoot = remResult.removed;
			newRoot.left = remResult.remTree;
			newRoot.right = sTree.right;
			return new RemoveResult(balance(newRoot), sTree);
		}
		
		return new RemoveResult(sTree.left != null ? sTree.left : sTree.right, sTree);
	}
	
	private RemoveResult removeLargest(AVLNode sTree) {
		RemoveResult remResult;
		if(sTree.right == null) 
			return new RemoveResult(sTree.left, sTree);
		
		remResult = removeLargest(sTree.right);
		sTree.right = remResult.remTree;
		remResult.remTree = balance(sTree);
		return remResult;
	}
	
	private AVLNode balance(AVLNode sTree) {
		if (Math.abs(getHeight(sTree.right) - getHeight(sTree.left)) == 2) {
			if(getHeight(sTree.right) >= getHeight(sTree.left)) {
				AVLNode rightChild = sTree.right;
				if(getHeight(rightChild.right) > getHeight(rightChild.left))
					sTree = leftRotate(sTree);
				else {
					sTree.right = rightRotate(rightChild);
					sTree = leftRotate(sTree);
				}
			}
			else {
				AVLNode leftChild = sTree.left;
				if(getHeight(leftChild.left) >= getHeight(leftChild.right))
					sTree = rightRotate(sTree);
				else {
					sTree.left = leftRotate(leftChild);
					sTree = rightRotate(sTree);
				}
			}
		}
		else
			sTree.resetHeight();
		
		return sTree;
	}
	
	private AVLNode rightRotate(AVLNode sTree) {
		AVLNode lNode = sTree.left,
				lrNode = lNode.right;
		
		lNode.right = sTree;
		sTree.left = lrNode;
		
		sTree.resetHeight();
		lNode.resetHeight();
		
		return lNode;
	}
	
	private AVLNode leftRotate(AVLNode sTree) {
		AVLNode rNode = sTree.right,
				rlNode = rNode.left;
		
		rNode.left = sTree;
		sTree.right = rlNode;
		
		sTree.resetHeight();
		rNode.resetHeight();
		
		return rNode;
	}
	
	public String toString() {
		LinkedList<AVLNode> queue = new LinkedList<AVLNode>();
		LinkedList<Integer> childrenOfNulls = new LinkedList<>();
		AVLNode front;
		int height = getHeight(),
			counter = 0,
			level = 0,
			levelSize = 1;
		String strTree = "Height: " + height + "\n";
		queue.add(root);
		while(!queue.isEmpty() && level <= height) {
			front = queue.remove(0);
			if(front != null) {
				strTree += front.value;
				queue.addLast(front.left);
				queue.addLast(front.right);
			}
			else {
				strTree += "E";
				childrenOfNulls.addLast(2);
			}
			
			if(++counter == levelSize) {
				levelSize = (int) Math.pow(2, ++level);
				counter = 0;
				int numChild;
				for(int nullChild = 0; nullChild < childrenOfNulls.size(); nullChild++) {
					numChild = childrenOfNulls.get(nullChild);
					counter += numChild;
					childrenOfNulls.set(nullChild, numChild * 2);
				}
				strTree += "\n";
			}
			else
				strTree += " ";
		}
		return strTree;
	}
}
