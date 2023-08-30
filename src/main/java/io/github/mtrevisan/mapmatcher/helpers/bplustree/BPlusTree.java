/**
 * Copyright (c) 2023 Mauro Trevisan
 * <p>
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * <p>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.mtrevisan.mapmatcher.helpers.bplustree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;


/**
 * Memory-based B+ Tree
 *
 * @see <a href="https://github.com/search?q=language%3Ajava+b%2B+tree&type=repositories">github java projects</a>
 *
 * <p>
 * Algorithm		Average		Worst case
 * Space				O(n)			O(n)
 * Search			O(log(n))	O(log(n))
 * Range search	O(log(n))	O(log(n))
 * Insert			O(log(n))	O(log(n))
 * </p>
 *
 * @see <a href="https://en.wikipedia.org/wiki/B%2B_tree">B+ Tree</a>
 * @see <a href="https://github.com/andylamp/BPlusTree">A Purely On-Disk Implementation of a B+ Tree</a>
 * @see <a href="https://github.com/myui/btree4j">btree4j</a>
 * @see <a href="https://github.com/karanchauhan/BPlus-Tree">Memory Resident B Plus Tree</a>
 * @see <a href="https://github.com/Morgan279/MemoryBasedBPlusTree">MemoryBasedBPlusTree</a>
 *
 * @param <E>	Key type.
 * @param <K>	Entry type.
 */
public class BPlusTree<K extends Comparable<K>, E>{

	private final int overflowBound;
	private final int underflowBound;


	private BPlusTreeNode root;


	/** Construct a B+ tree specifying the maximum number of elements that can contains. */
	public static <K extends Comparable<K>, E> BPlusTree<K, E> ofOrder(final int order){
		return new BPlusTree<>(order);
	}


	private BPlusTree(final int order){
		if(order < 3)
			throw new IllegalArgumentException("The order of B+ Tree must be greater than or equal to 3");

		overflowBound = order - 1;
		underflowBound = (overflowBound >> 1);
	}


	/**
	 * Insert a key and value pair to the B Plus Tree.
	 *
	 * @param key	the key to be inserted.
	 * @param value	the value to be inserted.
	 */
	public void insert(final K key, final E value){
		if(root != null){
			final BPlusTreeNode newChildNode = root.insert(key, value);
			if(newChildNode != null){
				//change root into non-leaf node
				final K newRootKey = (newChildNode.getClass().equals(BPlusTreeLeafNode.class)
					? newChildNode.keys.get(0)
					: ((BPlusTreeNonLeafNode)newChildNode).findLeafKey(newChildNode));
				root = new BPlusTreeNonLeafNode(asList(newRootKey), asList(root, newChildNode));
			}
		}
		else
			//insert into an empty tree
			root = new BPlusTreeLeafNode(asList(key), asList(asSet(value)));
	}


	/**
	 * Search values for a key.
	 *
	 * @param key	The key to be searched.
	 * @return	The list of values for the key.
	 */
	public List<E> query(final K key){
		return (root != null? root.query(key): Collections.emptyList());
	}

	/**
	 * Search values for a range of keys.
	 *
	 * @param keyStartInclusive	The starting key to be searched.
	 * @param keyEndExclusive	The ending key (exclusive) to be searched.
	 * @return	The list of values for the key.
	 */
	public List<E> rangeQuery(final K keyStartInclusive, final K keyEndExclusive){
		if(keyStartInclusive.compareTo(keyEndExclusive) >= 0)
			throw new IllegalArgumentException("invalid range");

		if(root == null)
			return Collections.emptyList();

		return root.rangeQuery(keyStartInclusive, keyEndExclusive);
	}


	public boolean update(final K key, final E oldValue, final E newValue){
		return (root != null && root.update(key, oldValue, newValue));
	}


	public boolean remove(final K key, final E value){
		if(root == null)
			return false;

		final RemoveResult removeResult = root.remove(key, value);
		if(!removeResult.isRemoved)
			return false;

		if(root.keys.isEmpty())
			handleRootUnderflow();

		return true;
	}

	public boolean remove(final K key){
		if(root == null)
			return false;

		final RemoveResult removeResult = root.remove(key);
		if(!removeResult.isRemoved)
			return false;

		if(root.keys.isEmpty())
			handleRootUnderflow();

		return true;
	}


	private void handleRootUnderflow(){
		root = (root.getClass().equals(BPlusTreeLeafNode.class)
			? null
			: ((BPlusTreeNonLeafNode)root).children.get(0));
	}

	@SafeVarargs
	private <T> List<T> asList(final T... e){
		final List<T> res = new ArrayList<>();
		Collections.addAll(res, e);
		return res;
	}

	@SafeVarargs
	private <T> Set<T> asSet(final T... e){
		final Set<T> res = new HashSet<>();
		Collections.addAll(res, e);
		return res;
	}


	@Override
	public String toString(){
		return (root != null? root.toString(): "");
	}


	private abstract class BPlusTreeNode{

		protected List<K> keys;


		protected boolean isUnderflow(){
			return (keys.size() < underflowBound);
		}

		protected boolean isOverflow(){
			return (keys.size() > overflowBound);
		}

		protected int getMedianIndex(){
			return (overflowBound >> 1);
		}

		protected int keyIndexUpperBound(final K key){
			int l = 0;
			int r = keys.size();
			while(l < r){
				int mid = l + ((r - l) >> 1);
				if(keys.get(mid).compareTo(key) <= 0)
					l = mid + 1;
				else
					r = mid;
			}
			return l;
		}

		protected abstract List<E> rangeQuery(K keyStartInclusive, K keyEndExclusive);

		protected abstract List<E> query(K key);

		protected abstract BPlusTreeNode insert(K key, E value);

		protected abstract boolean update(K key, E oldValue, E newValue);

		protected abstract RemoveResult remove(K key);

		protected abstract RemoveResult remove(K key, E value);

		protected abstract void combine(BPlusTreeNode neighbor, K parentKey);

		protected abstract void borrow(BPlusTreeNode neighbor, K parentKey, boolean isLeft);
	}

	private class BPlusTreeNonLeafNode extends BPlusTreeNode{

		private List<BPlusTreeNode> children;


		private BPlusTreeNonLeafNode(final List<K> keys, final List<BPlusTreeNode> children){
			this.keys = keys;
			this.children = children;
		}


		@Override
		protected List<E> rangeQuery(final K keyStartInclusive, final K keyEndExclusive){
			return children.get(keyIndexUpperBound(keyStartInclusive)).rangeQuery(keyStartInclusive, keyEndExclusive);
		}

		@Override
		protected List<E> query(final K key){
			return children.get(keyIndexUpperBound(key))
				.query(key);
		}

		@Override
		protected boolean update(final K key, final E oldValue, final E newValue){
			return children.get(keyIndexUpperBound(key))
				.update(key, oldValue, newValue);
		}

		@Override
		protected BPlusTreeNode insert(final K key, final E value){
			final BPlusTreeNode newChildNode = children.get(keyIndexUpperBound(key))
				.insert(key, value);

			if(newChildNode != null){
				final K newKey = findLeafKey(newChildNode);
				final int newKeyIndex = keyIndexUpperBound(newKey);
				keys.add(newKeyIndex, newKey);
				children.add(newKeyIndex + 1, newChildNode);
				return (isOverflow()? split(): null);
			}

			return null;
		}

		@Override
		protected RemoveResult remove(final K key){
			final int childIndex = keyIndexUpperBound(key);
			final int keyIndex = Math.max(0, childIndex - 1);
			final BPlusTreeNode childNode = children.get(childIndex);
			final RemoveResult removeResult = childNode.remove(key);
			if(!removeResult.isRemoved)
				return removeResult;

			if(removeResult.isUnderflow)
				handleUnderflow(childNode, childIndex, keyIndex);

			return new RemoveResult(true, isUnderflow());
		}

		@Override
		protected RemoveResult remove(final K key, final E value){
			final int childIndex = keyIndexUpperBound(key);
			final int keyIndex = Math.max(0, childIndex - 1);

			final BPlusTreeNode childNode = children.get(childIndex);
			final RemoveResult removeResult = childNode.remove(key, value);
			if(!removeResult.isRemoved)
				return removeResult;

			if(removeResult.isUnderflow)
				handleUnderflow(childNode, childIndex, keyIndex);

			return new RemoveResult(true, isUnderflow());
		}

		private void handleUnderflow(final BPlusTreeNode childNode, final int childIndex, final int keyIndex){
			BPlusTreeNode neighbor;
			if(childIndex > 0 && (neighbor = children.get(childIndex - 1)).keys.size() > underflowBound){
				childNode.borrow(neighbor, keys.get(keyIndex), true);
				final K boundKey = (childNode.getClass().equals(BPlusTreeNonLeafNode.class)
					? findLeafKey(childNode)
					: childNode.keys.get(0));
				keys.set(keyIndex, boundKey);

			}
			else if(childIndex < children.size() - 1 && (neighbor = children.get(childIndex + 1)).keys.size() > underflowBound){
				final int parentKeyIndex = childIndex == 0? 0: Math.min(keys.size() - 1, keyIndex + 1);
				childNode.borrow(neighbor, keys.get(parentKeyIndex), false);
				keys.set(parentKeyIndex, (childNode.getClass().equals(BPlusTreeNonLeafNode.class)
					? findLeafKey(neighbor)
					: neighbor.keys.get(0)));
			}
			else if(childIndex > 0){
				//combine current child to left child
				neighbor = children.get(childIndex - 1);
				neighbor.combine(childNode, keys.get(keyIndex));
				keys.remove(keyIndex);
				children.remove(childIndex);

			}
			else{
				//combine right child to current child (child index = 0)
				neighbor = children.get(1);
				childNode.combine(neighbor, keys.get(0));
				keys.remove(0);
				children.remove(1);
			}
		}

		private BPlusTreeNonLeafNode split(){
			final int medianIndex = getMedianIndex();
			final List<K> allKeys = keys;
			final List<BPlusTreeNode> allChildren = children;

			keys = new ArrayList<>(allKeys.subList(0, medianIndex));
			children = new ArrayList<>(allChildren.subList(0, medianIndex + 1));

			final List<K> rightKeys = new ArrayList<>(allKeys.subList(medianIndex + 1, allKeys.size()));
			final List<BPlusTreeNode> rightChildren = new ArrayList<>(allChildren.subList(medianIndex + 1, allChildren.size()));
			return new BPlusTreeNonLeafNode(rightKeys, rightChildren);
		}

		@Override
		protected void combine(final BPlusTreeNode neighbor, final K parentKey){
			final BPlusTreeNonLeafNode nonLeafNode = (BPlusTreeNonLeafNode)neighbor;
			keys.add(parentKey);
			keys.addAll(nonLeafNode.keys);
			children.addAll(nonLeafNode.children);
		}

		@Override
		protected void borrow(final BPlusTreeNode neighbor, final K parentKey, final boolean isLeft){
			final BPlusTreeNonLeafNode nonLeafNode = (BPlusTreeNonLeafNode)neighbor;
			if(isLeft){
				keys.add(0, parentKey);
				children.add(0, nonLeafNode.children.get(nonLeafNode.children.size() - 1));
				nonLeafNode.children.remove(nonLeafNode.children.size() - 1);
				nonLeafNode.keys.remove(nonLeafNode.keys.size() - 1);
			}
			else{
				keys.add(parentKey);
				children.add(nonLeafNode.children.get(0));
				nonLeafNode.keys.remove(0);
				nonLeafNode.children.remove(0);
			}
		}

		protected K findLeafKey(final BPlusTreeNode cur){
			if(cur.getClass().equals(BPlusTreeLeafNode.class))
				return cur.keys.get(0);

			return findLeafKey(((BPlusTreeNonLeafNode)cur).children.get(0));
		}

		@Override
		public String toString(){
			final StringBuilder res = new StringBuilder();
			final Queue<BPlusTreeNode> queue = new LinkedList<>();
			queue.add(this);
			while(!queue.isEmpty()){
				final int size = queue.size();
				for(int i = 0; i < size; i ++){
					final BPlusTreeNode cur = queue.poll();
					assert cur != null;

					res.append(cur.keys).append("  ");
					if(cur.getClass().equals(BPlusTreeNonLeafNode.class))
						queue.addAll(((BPlusTreeNonLeafNode)cur).children);
				}
				res.append('\n');
			}
			return res.toString();
		}
	}

	private class BPlusTreeLeafNode extends BPlusTreeNode{

		private List<Set<E>> data;
		private BPlusTreeLeafNode next;

		private BPlusTreeLeafNode(final List<K> keys, final List<Set<E>> data){
			this.keys = keys;
			this.data = data;
		}

		@Override
		protected List<E> rangeQuery(final K keyStartInclusive, final K keyEndExclusive){
			final List<E> res = new ArrayList<>();
			final int startUpperBound = Math.max(1, keyIndexUpperBound(keyStartInclusive));

			int end = keyIndexUpperBound(keyEndExclusive) - 1;
			if(end >= 0 && keys.get(end) == keyEndExclusive)
				end --;

			for(int i = startUpperBound - 1; i <= end; i ++)
				res.addAll(data.get(i));

			BPlusTreeLeafNode nextLeafNode = next;
			while(nextLeafNode != null){
				for(int i = 0; i < nextLeafNode.keys.size(); i ++){
					if(nextLeafNode.keys.get(i).compareTo(keyEndExclusive) < 0)
						res.addAll(nextLeafNode.data.get(i));
					else
						return res;
				}

				nextLeafNode = nextLeafNode.next;
			}
			return res;
		}

		@Override
		protected List<E> query(final K key){
			final int keyIndex = getEqualKeyIndex(key);
			return (keyIndex != -1? new ArrayList<>(data.get(keyIndex)): Collections.emptyList());
		}

		@Override
		protected boolean update(final K key, final E oldValue, final E newValue){
			final int keyIndex = getEqualKeyIndex(key);
			if(keyIndex == -1 || !data.get(keyIndex).contains(oldValue))
				return false;

			data.get(keyIndex).remove(oldValue);
			data.get(keyIndex).add(newValue);
			return true;
		}

		@Override
		protected RemoveResult remove(final K key){
			final int keyIndex = getEqualKeyIndex(key);
			if(keyIndex == -1)
				return new RemoveResult(false, false);

			keys.remove(keyIndex);
			data.remove(keyIndex);

			return new RemoveResult(true, isUnderflow());
		}

		@Override
		protected RemoveResult remove(final K key, final E value){
			final int keyIndex = getEqualKeyIndex(key);
			if(keyIndex == -1 || !data.get(keyIndex).contains(value))
				return new RemoveResult(false, false);

			data.get(keyIndex).remove(value);
			if(data.get(keyIndex).isEmpty()){
				keys.remove(keyIndex);
				data.remove(keyIndex);
			}

			return new RemoveResult(true, isUnderflow());
		}

		@Override
		protected void combine(final BPlusTreeNode neighbor, final K parentKey){
			final BPlusTreeLeafNode leafNode = (BPlusTreeLeafNode)neighbor;
			keys.addAll(leafNode.keys);
			data.addAll(leafNode.data);
			next = leafNode.next;
		}

		@Override
		protected void borrow(final BPlusTreeNode neighbor, final K parentKey, final boolean isLeft){
			final BPlusTreeLeafNode leafNode = (BPlusTreeLeafNode)neighbor;
			int borrowIndex;
			if(isLeft){
				borrowIndex = leafNode.keys.size() - 1;
				keys.add(0, leafNode.keys.get(borrowIndex));
				data.add(0, leafNode.data.get(borrowIndex));
			}
			else{
				borrowIndex = 0;
				keys.add(leafNode.keys.get(borrowIndex));
				data.add(leafNode.data.get(borrowIndex));
			}

			leafNode.keys.remove(borrowIndex);
			leafNode.data.remove(borrowIndex);
		}

		@Override
		protected BPlusTreeNode insert(final K key, final E value){
			final int equalKeyIndex = getEqualKeyIndex(key);
			if(equalKeyIndex != -1){
				data.get(equalKeyIndex).add(value);
				return null;
			}

			final int index = keyIndexUpperBound(key);
			keys.add(index, key);
			data.add(index, asSet(value));
			return isOverflow()? split(): null;
		}

		private BPlusTreeLeafNode split(){
			final int medianIndex = getMedianIndex();
			final List<K> allKeys = keys;
			final List<Set<E>> allData = data;

			keys = new ArrayList<>(allKeys.subList(0, medianIndex));
			data = new ArrayList<>(allData.subList(0, medianIndex));

			final List<K> rightKeys = new ArrayList<>(allKeys.subList(medianIndex, allKeys.size()));
			final List<Set<E>> rightData = new ArrayList<>(allData.subList(medianIndex, allData.size()));
			final BPlusTreeLeafNode newLeafNode = new BPlusTreeLeafNode(rightKeys, rightData);

			newLeafNode.next = next;
			next = newLeafNode;
			return newLeafNode;
		}

		private int getEqualKeyIndex(final K key){
			int l = 0;
			int r = keys.size() - 1;
			while(l <= r){
				final int mid = l + ((r - l) >> 1);
				final int compare = nullSafeCompareTo(keys.get(mid), key);
				if(compare == 0)
					return mid;

				if(compare > 0)
					r = mid - 1;
				else
					l = mid + 1;
			}
			return -1;
		}

		/** Null safe comparison for {@link Comparable}s. **/
		private static <K extends Comparable<K>> int nullSafeCompareTo(final K key1, final K key2){
			final boolean f1;
			return (f1 = (key1 == null)) ^ (key2 == null)
				? (f1? -1: 1)
				: (f1? 0: key1.compareTo(key2));
		}

		@Override
		public String toString(){
			return keys.toString();
		}
	}

	private static class RemoveResult{

		private final boolean isRemoved;
		private final boolean isUnderflow;

		private RemoveResult(final boolean isRemoved, final boolean isUnderflow){
			this.isRemoved = isRemoved;
			this.isUnderflow = isUnderflow;
		}
	}

}
