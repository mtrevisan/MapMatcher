/**
 * Copyright (c) 2022 Mauro Trevisan
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
package io.github.mtrevisan.mapmatcher.helpers;

import java.lang.reflect.Array;


/**
 * This class implements a Fibonacci heap data structure. Much of the
 * code in this class is based on the algorithms in Chapter 21 of the
 * "Introduction to Algorithms" by Cormen, Leiserson, Rivest, and Stein.
 * The amortized running time of most of these methods is O(1), making
 * it a very fast data structure. Several have an actual running time
 * of O(1). removeMin() and delete() have O(log n) amortized running
 * times because they do the heap consolidation.
 *
 * <p><strong>Note that this implementation is not synchronized.</strong>
 * If multiple threads access a set concurrently, and at least one of the
 * threads modifies the set, it <em>must</em> be synchronized externally.
 * This is typically accomplished by synchronizing on some object that
 * naturally encapsulates the set.</p>
 *
 * @author Nathan Fiedler
 * @see <a href="https://github.com/nlfiedler/graphmaker/blob/master/core/src/com/bluemarsh/graphmaker/core/util/FibonacciHeap.java">FibonacciHeap.java</a>
 */
public class FibonacciHeap<T>{

	/** Points to the minimum node in the heap. */
	private Node<T> min;
	/**
	 * Number of nodes in the heap.
	 * If the type is ever widened, (e.g. changed to long) then recalculate the maximum degree value used in the {@link #consolidate()}
	 * method.
	 */
	private int n;


	/**
	 * Removes all elements from this heap.
	 *
	 * <p><em>Running time: <code>O(1)</code></em></p>
	 */
	public void clear(){
		min = null;
		n = 0;
	}

	/**
	 * Consolidates the trees in the heap by joining trees of equal degree until there are no more trees of equal degree in the root list.
	 *
	 * <p><em>Running time: <code>O(log n)</code> amortized</em></p>
	 */
	@SuppressWarnings("SuspiciousNameCombination")
	private void consolidate(){
		/**
		 * The magic number 45 comes from logarithm base Φ of {@link Integer.MAX_VALUE}, which is the most elements we will ever hold, and
		 * logarithm base Φ represents the largest degree of any root list node.
		 */
		@SuppressWarnings("unchecked")
		final Node<T>[] nodes = (Node<T>[])Array.newInstance(Node.class, 45);

		//for each root list node look for others of the same degree
		Node<T> start = min;
		Node<T> w = min;
		do{
			Node<T> x = w;
			//because `x` might be moved, save its sibling now
			Node<T> nextW = w.right;
			int d = x.degree;
			while(nodes[d] != null){
				//make one of the nodes a child of the other
				Node<T> y = nodes[d];
				if(x.key > y.key){
					final Node<T> tmp = y;
					y = x;
					x = tmp;
				}
				if(y == start)
					/**
					 * Because {@link #poll()} arbitrarily assigned the min reference, we have to ensure we do not miss the end of the
					 * root node list.
					 */
					start = start.right;
				if(y == nextW)
					//if we wrapped around we need to check for this case
					nextW = nextW.right;
				//node `y` disappears from root list
				y.link(x);
				//we've handled this degree, go to next one.
				nodes[d] = null;

				d ++;
			}

			//save this node for later when we might encounter another of the same degree
			nodes[d] = x;
			//move forward through list
			w = nextW;
		}while(w != start);

		//the node considered to be `min` may have been changed above
		min = start;
		//find the minimum key again
		for(final Node<T> a : nodes)
			if(a != null && a.key < min.key)
				min = a;
	}

	/**
	 * Decreases the key value for a heap node, given the new value to take on.
	 * <p>The structure of the heap may be changed, but will not be consolidated.</p>
	 *
	 * <p><em>Running time: <code>O(1)</code> amortized</em></p>
	 *
	 * @param x	Node to decrease the key of.
	 * @param k	New key value for node x.
	 * @throws IllegalArgumentException	If `k` is larger than `x.key` value.
	 */
	public void decreaseKey(final Node<T> x, final double k){
		decreaseKey(x, k, false);
	}

	/**
	 * Decrease the key value of a node, or simply bubble it up to the top of the heap in preparation for a delete operation.
	 *
	 * @param x	Node to decrease the key of.
	 * @param k	New key value for node `x`.
	 * @param delete	Whether to delete the node (in which case, `k` is ignored).
	 */
	private void decreaseKey(final Node<T> x, final double k, final boolean delete){
		if(!delete && k > x.key)
			throw new IllegalArgumentException("Cannot increase key value");

		x.key = k;
		final Node<T> y = x.parent;
		if(y != null && (delete || k < y.key)){
			y.cut(x, min);
			y.cascadingCut(min);
		}
		if(delete || k < min.key)
			min = x;
	}

	/**
	 * Deletes a node from the heap given the reference to the node.
	 * <p>The trees in the heap will be consolidated, if necessary.</p>
	 *
	 * <p><em>Running time: <code>O(log n)</code> amortized</em></p>
	 *
	 * @param x	Node to remove from heap.
	 */
	public void remove(final Node<T> x){
		//make `x` as small as possible, bringing it to the root
		decreaseKey(x, 0, true);
		//remove the smallest, which decreases `n` also
		poll();
	}

	/**
	 * Tests if the Fibonacci heap is empty or not.
	 *
	 * <p><em>Running time: <code>O(1)</code></em></p>
	 *
	 * @return	Whether the heap is empty.
	 */
	public boolean isEmpty(){
		return (min == null);
	}

	/**
	 * Inserts a new data element into the heap.
	 * <p>No heap consolidation is performed at this time, the new node is simply inserted into the root list of this heap.</p>
	 *
	 * <p><em>Running time: <code>O(1)</code></em></p>
	 *
	 * @param x	Data object to insert into heap.
	 * @param key	Key value associated with data object.
	 * @return	The newly created heap node.
	 */
	public Node<T> add(final T x, final double key){
		final Node<T> node = new Node<>(x, key);
		//concatenate node into min list
		if(min != null){
			node.right = min;
			node.left = min.left;
			min.left = node;
			node.left.right = node;
			if(key < min.key)
				min = node;
		}
		else
			min = node;
		n ++;
		return node;
	}

	/**
	 * Returns the smallest element in the heap.
	 * <p>This smallest element is the one with the minimum key value.</p>
	 *
	 * <p><em>Running time: <code>O(1)</code></em></p>
	 *
	 * @return	The heap node with the smallest key, or <code>null</code> if empty.
	 */
	public Node<T> peek(){
		return min;
	}

	/**
	 * Removes the smallest element from the heap.
	 * <p>This will cause the trees in the heap to be consolidated, if necessary.</p>
	 *
	 * <p><em>Running time: <code>O(log n)</code> amortized</em></p>
	 *
	 * @return	The data object with the smallest key.
	 */
	public T poll(){
		final Node<T> z = min;
		if(z == null)
			return null;

		if(z.child != null){
			z.child.parent = null;
			//for each child of `z` do...
			for(Node<T> x = z.child.right; x != z.child; x = x.right)
				//set `parent[x]` to `null`
				x.parent = null;
			//merge the children into root list
			final Node<T> minLeft = min.left;
			final Node<T> zChildLeft = z.child.left;
			min.left = zChildLeft;
			zChildLeft.right = min;
			z.child.left = minLeft;
			minLeft.right = z.child;
		}
		//remove `z` from root list of heap
		z.left.right = z.right;
		z.right.left = z.left;
		if(z == z.right)
			min = null;
		else{
			min = z.right;
			consolidate();
		}

		//decrement size of heap
		n --;

		return z.data;
	}

	/**
	 * Returns the size of the heap which is measured in the number of elements contained in the heap.
	 *
	 * <p><em>Running time: <code>O(1)</code></em></p>
	 *
	 * @return	The number of elements in the heap.
	 */
	public int size(){
		return n;
	}

	/**
	 * Joins two Fibonacci heaps into a new one.
	 * <p>No heap consolidation is performed at this time. The two root lists are simply joined together.</p>
	 *
	 * <p><em>Running time: <code>O(1)</code></em></p>
	 *
	 * @param heap1	The first heap.
	 * @param heap2	The second heap.
	 * @return	The new heap containing `heap1` and `heap2`.
	 */
	public static <T> FibonacciHeap<T> union(final FibonacciHeap<T> heap1, final FibonacciHeap<T> heap2){
		final FibonacciHeap<T> heap = new FibonacciHeap<>();
		if(heap1 != null && heap2 != null){
			heap.min = heap1.min;
			if(heap.min == null){
				heap.min = heap2.min;
			}
			else if(heap2.min != null){
				heap.min.right.left = heap2.min.left;
				heap2.min.left.right = heap.min.right;
				heap.min.right = heap2.min;
				heap2.min.left = heap.min;
				if(heap2.min.key < heap1.min.key)
					heap.min = heap2.min;
			}
			heap.n = heap1.n + heap2.n;
		}
		return heap;
	}


	/**
	 * Implements a node of the Fibonacci heap.
	 * <p>It holds the information necessary for maintaining the structure of the heap. It acts as an opaque handle for the data element,
	 * and serves as the key to retrieving the data from the heap.</p>
	 *
	 * @param <T>	The data type.
	 */
	public static class Node<T> implements Comparable<Node<T>>{
		/** Data object for this node, holds the key value. */
		private final T data;
		/** Key value for this node. */
		private double key;
		/** Parent node. */
		private Node<T> parent;
		/** First child node. */
		private Node<T> child;
		/** Right sibling node. */
		private Node<T> right;
		/** Left sibling node. */
		private Node<T> left;
		/** Number of children of this node. */
		private int degree;
		/** Whether this node has had a child removed since this node was added to its parent. */
		private boolean mark;


		/**
		 * Constructor which sets the data and key fields to the passed arguments.
		 * <p>It also initializes the right and left pointers, making this a circular doubly-linked list.</p>
		 *
		 * @param data	The data object to associate with this node.
		 * @param key	The key value for this data object.
		 */
		private Node(final T data, final double key){
			this.data = data;
			this.key = key;
			right = this;
			left = this;
		}

		public T getData(){
			return data;
		}

		public double getKey(){
			return key;
		}

		/**
		 * Performs a cascading cut operation.
		 * <p>Cuts this from its parent and then does the same for its parent, and so on up the tree.</p>
		 *
		 * <p><em>Running time: <code>O(log n)</code></em></p>
		 *
		 * @param min	The minimum heap node, to which nodes will be added.
		 */
		private void cascadingCut(final Node<T> min){
			Node<T> z = parent;
			//if there's a parent...
			if(z != null){
				if(mark){
					//it's marked, cut it from parent
					z.cut(this, min);
					//cut its parent as well
					z.cascadingCut(min);
				}
				else
					//if `y` is unmarked, set it marked
					mark = true;
			}
		}

		/**
		 * The reverse of the link operation: removes `x` from the child list of this node.
		 *
		 * <p><em>Running time: <code>O(1)</code></em></p>
		 *
		 * @param x	The child to be removed from this node's child list.
		 * @param min	The minimum heap node, to which `x` is added.
		 */
		private void cut(final Node<T> x, final Node<T> min){
			//remove `x` from child list and decrement degree
			x.left.right = x.right;
			x.right.left = x.left;
			degree --;
			//reset child if necessary
			if(degree == 0)
				child = null;
			else if(child == x)
				child = x.right;
			//add `x` to root list of heap
			x.right = min;
			x.left = min.left;
			min.left = x;
			x.left.right = x;
			//set `parent[x]` to `nil`
			x.parent = null;
			//set `mark[x]` to `false`
			x.mark = false;
		}

		/**
		 * Make this node a child of the given parent node.
		 * <p>All linkages are updated, the degree of the parent is incremented, and mark is set to `false`.</p>
		 *
		 * @param parent	The new parent node.
		 */
		private void link(final Node<T> parent){
			//NOTE: putting this code here in Node makes it 7x faster because it doesn't have to use generated accessor methods, which add
			// a lot of time when called millions of times.
			//remove this from its circular list
			left.right = right;
			right.left = left;
			//make this a child of `x`
			this.parent = parent;
			if(parent.child == null){
				parent.child = this;
				right = this;
				left = this;
			}
			else{
				left = parent.child;
				right = parent.child.right;
				parent.child.right = this;
				right.left = this;
			}
			//increase `degree[x]`
			parent.degree ++;
			//set mark `false`
			mark = false;
		}

		@Override
		public int compareTo(final Node node){
			return Double.compare(key, node.key);
		}

		@Override
		public boolean equals(final Object obj){
			if(this == obj)
				return true;
			if(obj == null || getClass() != obj.getClass())
				return false;

			final Node<?> other = (Node<?>)obj;
			return data.equals(other.data);
		}

		@Override
		public int hashCode(){
			return data.hashCode();
		}
	}

}
