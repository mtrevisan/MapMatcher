package io.github.mtrevisan.mapmatcher.helpers.rtree2;

import io.github.mtrevisan.mapmatcher.helpers.quadtree.Region;
import io.github.mtrevisan.mapmatcher.helpers.rtree.RNode;
import io.github.mtrevisan.mapmatcher.spatial.Point;


public class RTree{

	private final static int DEFAULT_MIN_CHILDREN = 2;
	private final static int DEFAULT_MAX_CHILDREN = 4;

	private RNode rootNode;
	protected int min_num_records;
	protected int max_num_records;
	protected NodeSplitter<Point> splitter;

	public RTree(){
		min_num_records = DEFAULT_MIN_CHILDREN;
		max_num_records = DEFAULT_MAX_CHILDREN;
		splitter = new LinearSplitter<Point>(DEFAULT_MIN_CHILDREN);
	}

	public RTree(int min_num_records, int max_num_records, NodeSplitter<Point> splitter){
		this.min_num_records = min_num_records;
		this.max_num_records = max_num_records;
		rootNode = new RNode();
		this.splitter = splitter;
	}

	public RNode getRoot(){
		return rootNode;
	}

	public void insert(Record<Point> record){
		Region recordMbr = record.getMbr();
		// choose leaf that needs the least enlargement with mbr
		RNode leaf = chooseLeaf(recordMbr);
		RNode newNode = new RNode(record);
		// if node has enough space to insert the child
		if(leaf.numChildren() < max_num_records){
			leaf.add(newNode);
			adjustTree(leaf, null);
		}
		else{
			splitNodeAndReassign(leaf, newNode);
		}
	}

	public void insertLeaf(List<Record<Point>> records){
		for(Record<Point> record : records){
			insert(record);
		}
	}

	private RNode chooseLeaf(Region recordMbr){
		RNode current = rootNode;
		while(!current.isLeaf()){
			ArrayList<RNode> minEnlargedRecords = getMinEnlargedRecords(current, recordMbr);
			if(minEnlargedRecords.size() == 1)
				current = minEnlargedRecords.get(0);
			else{
				// resolve ties if any, by choosing the node with least mbr's area
				current = getMinAreaRecord(minEnlargedRecords);
			}
		}
		return current;
	}

	private RNode getMinAreaRecord(ArrayList<RNode> nodes){
		RNode minAreaRecord = null;
		double minArea = Float.MAX_VALUE;
		for(RNode node : nodes){
			double area = node.getMbr().getArea();
			if(area < minArea){
				minAreaRecord = node;
				minArea = area;
			}
		}
		return minAreaRecord;
	}

	private ArrayList<RNode> getMinEnlargedRecords(RNode current, Region recordMbr){
		double minEnlargement = Float.MAX_VALUE;
		ArrayList<RNode> minEnlargedRecords = new ArrayList<RNode>();
		// choose record which mbr's enlarge the less with current record's mbr
		for(RNode child : current.getChildren()){
			Rectangle childMbr = child.getMbr();

			double enlargement = childMbr.calculateEnlargement(recordMbr);
			if(enlargement == minEnlargement || minEnlargedRecords.size() == 0){
				minEnlargedRecords.add(child);
				minEnlargement = enlargement;
			}
			else if(enlargement < minEnlargement){
				minEnlargedRecords = new ArrayList<RNode>();
				minEnlargedRecords.add(child);
				minEnlargement = enlargement;
			}
		}
		return minEnlargedRecords;
	}

	private void splitNodeAndReassign(RNode nodeToSplit, RNode overflowNode){
		Pair<RNode, RNode> splittedNodes = splitter.splitNodes(nodeToSplit, overflowNode);
		RNode splittedLeft = splittedNodes.getFirst(), splittedRight = splittedNodes.getSecond();
		if(nodeToSplit == rootNode)
			assignNewRoot(splittedLeft, splittedRight);
		else{
			RNode splittedParent = nodeToSplit.getParent();
			splittedParent.remove(nodeToSplit);
			splittedParent.add(splittedLeft);
			splittedParent.add(splittedRight);
			adjustTree(splittedLeft, splittedRight);
		}
	}

	protected void assignNewRoot(RNode child1, RNode child2){
		RNode newRoot = new RNode();
		newRoot.add(child1);
		newRoot.add(child2);
		rootNode = newRoot;
	}

	private void adjustTree(RNode node, RNode createdNode){
		RNode previousNode = node;
		// node resulting from split
		RNode splittedNode = createdNode;
		// while we do not reach root
		while(!previousNode.isRoot()){
			RNode previousParent = previousNode.getParent();
			// updating parent recursively in the no-split case
			if(splittedNode == null)
				previousParent.updateMbr(previousNode.getMbr());
			// see if there is a node overflow, and update accordingly
			else if(previousParent.numChildren() > max_num_records){
				previousParent.remove(splittedNode);
				splitNodeAndReassign(previousParent, splittedNode);
			}
			previousNode = previousParent;
		}
	}

	public boolean delete(Record<Point> record){
		// choose leaf that contains record
		RNode leaf = findLeafAndRemove(record, rootNode);
		if(leaf == null)
			return false;
		condenseTree(leaf);
		// if root needs to be reassigned
		if(rootNode.numChildren() == 1 && ! rootNode.isLeaf()){
			RNode newRoot = rootNode.getChildren().get(0);
			newRoot.setParent(null);
			rootNode = newRoot;
		}
		return true;
	}

	public List<Boolean> deleteRes(List<Record<Point>> records){
		List<Boolean> deletedRecords = new ArrayList<Boolean>();
		for(Record<Point> record : records){
			deletedRecords.add(delete(record));
		}
		return deletedRecords;
	}

	private RNode findLeafAndRemove(Record<Point> record, RNode node){
		if(! node.isLeaf()){
			// perform DFS of child nodes
			for(RNode child : node.getChildren()){
				if(child.getMbr().containedBy(record.getMbr())){
					RNode foundLeaf = findLeafAndRemove(record, child);
					if(foundLeaf != null){
						return foundLeaf;
					}
				}
			}
		}
		else{
			for(RNode child : node.getChildren()){
				Record<Point> childRecord = child.getRecord();
				if(childRecord.equals(record)){
					node.remove(child);
					return node;
				}
			}
		}
		return null;
	}

	private void condenseTree(RNode leaf){
		RNode N = leaf;
		ArrayList<RNode> removedEntries = new ArrayList<RNode>();
		if(! N.isRoot()){
			RNode P = N.getParent();
			// N has underflow of childs
			if(N.numChildren() < min_num_records){
				P.remove(N);
				// we will reinsert remaining entries if they have at least one child
				if(N.numChildren() > 0)
					removedEntries.add(N);
			}
			else{
				N.updateMbr(null);
			}
			// update parent recursively
			condenseTree(P);
		}
		// reinsert temporarily deleted entries
		for(RNode deletedChild : removedEntries){
			insertFromNode(deletedChild);
		}
	}

	private void insertFromNode(RNode node){
		if(node.getChildren() != null){
			for(RNode child : node.getChildren()){
				if(child.getRecord() != null)
					insert(child.getRecord());
				else
					insertFromNode(child);
			}
		}
	}

	public Record<Point> search(Record<Point> record){
		Region recordMbr = record.getMbr();
		Record<Point> result = null;
		// init stack for dfs in valid childs
		Stack<RNode> validNodes = new Stack<RNode>();
		validNodes.push(rootNode);
		// traverse whole tree
		while(! validNodes.empty() && result == null){
			RNode currentNode = validNodes.pop();
			for(RNode child : currentNode.getChildren()){
				// record node
				Record<Point> childRecord = child.getRecord();
				if(childRecord != null){
					if(childRecord.equals(record)){
						result = childRecord;
						break;
					}
				}
				else if(child.getMbr().containedBy(recordMbr)){
					validNodes.push(child);
				}
			}
		}
		return result;
	}

	public List<Record<Point>> searchRes(List<Record<Point>> records){
		List<Record<Point>> searchResults = new ArrayList<Record<Point>>();
		for(Record<Point> searchRecord : records)
			searchResults.add(search(searchRecord));
		return searchResults;
	}

}
