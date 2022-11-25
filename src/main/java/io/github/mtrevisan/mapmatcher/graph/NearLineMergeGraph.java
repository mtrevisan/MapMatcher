/**
 * Copyright (c) 2021 Mauro Trevisan
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
package io.github.mtrevisan.mapmatcher.graph;

import io.github.mtrevisan.mapmatcher.distances.DistanceCalculator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.LineString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


public class NearLineMergeGraph implements Graph{

	private static final Logger LOGGER = LoggerFactory.getLogger(NearLineMergeGraph.class);


	private final Set<Edge> edges = new HashSet<>();
	private final Map<Coordinate, Node> nodeMap = new TreeMap<>();

	private final double radius;
	private final DistanceCalculator distanceCalculator;


	public NearLineMergeGraph(final double radius, final DistanceCalculator distanceCalculator){
		this.radius = radius;
		this.distanceCalculator = distanceCalculator;
	}

	public Collection<Edge> addApproximateEdge(final LineString lineString){
		return addApproximateEdge(null, lineString);
	}

	public Collection<Edge> addApproximateEdge(final String id, final LineString lineString){
		return addApproximateDirectEdge(id, lineString, true);
	}

	public Collection<Edge> addApproximateDirectEdge(final LineString lineString){
		return addApproximateDirectEdge(null, lineString);
	}

	public Collection<Edge> addApproximateDirectEdge(final String id, final LineString lineString){
		return addApproximateDirectEdge(id, lineString, false);
	}

	private Collection<Edge> addApproximateDirectEdge(final String id, final LineString lineString, final boolean bidirectionalEdge){
		final Collection<Edge> addedEdges = new HashSet<>(0);
		if(lineString.isEmpty())
			return addedEdges;

		final Coordinate[] coordinates = CoordinateArrays.removeRepeatedPoints(lineString.getCoordinates());
		//don't add lines with all coordinates equal
		if(coordinates.length <= 1)
			return addedEdges;

		final Coordinate startCoordinate = coordinates[0];
		final Coordinate endCoordinate = coordinates[coordinates.length - 1];
		final Collection<Node> startNodes = getApproximateNode(startCoordinate);
		final Collection<Node> endNodes = getApproximateNode(endCoordinate);
		final Set<Node> intersectionNodes = new HashSet<>(startNodes);
		intersectionNodes.retainAll(endNodes);
		for(final Node fromNode : startNodes)
			for(final Node toNode : endNodes){
				final Edge edge = Edge.createBidirectionalEdge(fromNode, toNode, lineString);
				if(bidirectionalEdge)
					edge.setBidirectional();
				if(id != null){
					edge.setID(id);

					String nodeID = (fromNode.getID() != null && fromNode.getID().length() > 0? fromNode.getID() + ",": "N:");
					fromNode.setID(nodeID + edge.getID() + "/from");
					nodeID = (toNode.getID() != null && toNode.getID().length() > 0? toNode.getID() + ",": "N:");
					toNode.setID(nodeID + edge.getID() + "/to");
				}
				if(!edges.contains(edge)){
					fromNode.addOutEdge(edge);
					if(bidirectionalEdge)
						toNode.addOutEdge(edge);
					edges.add(edge);

					addedEdges.add(edge);

					LOGGER.debug("Connect edge {} to node {} and {}", edge.getID(), fromNode.getID(), toNode.getID());
				}
			}
		for(final Node intersectionNode1 : intersectionNodes)
			for(final Node intersectionNode2 : intersectionNodes)
				if(!intersectionNode1.equals(intersectionNode2)){
					final Edge edge = Edge.createBidirectionalEdge(intersectionNode1, intersectionNode2, lineString);
					edge.setBidirectional();
					if(id != null){
						edge.setID(id);

						final String nodeID = (edge.getID() != null && edge.getID().length() > 0? edge.getID() + ",": "N:");
						edge.setID(nodeID + edge.getID() + "/from-to");
					}
					if(!edges.contains(edge)){
						intersectionNode1.addOutEdge(edge);
						intersectionNode2.addOutEdge(edge);
						edges.add(edge);

						addedEdges.add(edge);

						LOGGER.debug("Connect edge {} to intersection node {} and {}", edge.getID(), intersectionNode1.getID(), intersectionNode2.getID());
					}
				}
		return addedEdges;
	}

	private Collection<Node> getApproximateNode(final Coordinate coordinate){
		final Collection<Node> closest = getNodesNear(coordinate);
		if(closest.isEmpty()){
			final Node node = new Node("", coordinate);
			nodeMap.put(coordinate, node);
			closest.add(node);
		}
		return closest;
	}

	public Collection<Node> getNodesNear(final Coordinate coordinate){
		final Set<Node> closest = new HashSet<>();
		for(final Map.Entry<Coordinate, Node> entry : nodeMap.entrySet())
			if(distanceCalculator.distance(entry.getKey(), coordinate) < radius)
				closest.add(entry.getValue());
		return closest;
	}

	/**
	 * Returns the nodes that have been added to this graph.
	 *
	 * @return	The nodes.
	 */
	@Override
	public Collection<Node> nodes(){
		return nodeMap.values();
	}

	/**
	 * Returns an iterator over the nodes in this graph.
	 *
	 * @return	The node iterator.
	 */
	@Override
	public Iterator<Node> nodeIterator(){
		return nodeMap.values().iterator();
	}

	/**
	 * Returns the edges that have been added to this graph.
	 *
	 * @return	The edges.
	 */
	@Override
	public Collection<Edge> edges(){
		return edges;
	}

	/**
	 * Returns an iterator over the edges in this graph, in the order in which they were added.
	 *
	 * @return	The edge iterator.
	 */
	@Override
	public Iterator<Edge> edgeIterator(){
		return edges.iterator();
	}

}
