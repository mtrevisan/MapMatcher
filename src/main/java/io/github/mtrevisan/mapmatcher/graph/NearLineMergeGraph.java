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
package io.github.mtrevisan.mapmatcher.graph;

import io.github.mtrevisan.mapmatcher.helpers.hprtree.HPRtree;
import io.github.mtrevisan.mapmatcher.spatial.Envelope;
import io.github.mtrevisan.mapmatcher.spatial.GPSPoint;
import io.github.mtrevisan.mapmatcher.spatial.GeodeticHelper;
import io.github.mtrevisan.mapmatcher.spatial.GeometryFactory;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeMap;


public class NearLineMergeGraph implements Graph{

	private static final Logger LOGGER = LoggerFactory.getLogger(NearLineMergeGraph.class);

	private static final String EMPTY = "";


	private final Set<Edge> edges = new HashSet<>(0);
	private final Map<Point, Node> nodeMap = new TreeMap<>();

	private final double threshold;

	private HPRtree<Polyline> tree;


	public NearLineMergeGraph(final double threshold){
		this.threshold = threshold;
	}

	public NearLineMergeGraph withTree(){
		tree = new HPRtree<>();

		return this;
	}

	public Collection<Edge> addApproximateDirectEdge(final Point from, final Point to){
		return addApproximateDirectEdge(null, from, to);
	}

	public Collection<Edge> addApproximateDirectEdge(final String id, final Point from, final Point to){
		if(from == null || to == null)
			return Collections.emptyList();

		final Collection<Edge> addedEdges = new HashSet<>(0);
		final Collection<Node> startNodes = connectNodes(from);
		final Collection<Node> endNodes = connectNodes(to);
		final Set<Node> intersectionNodes = new HashSet<>(startNodes);
		intersectionNodes.retainAll(endNodes);

		for(final Node fromNode : startNodes)
			for(final Node toNode : endNodes){
				final Edge edge = Edge.createDirectEdge(fromNode, toNode);
				if(id != null){
					edge.setID(id);

					String nodeID = (fromNode.getID() != null && fromNode.getID().length() > 0? fromNode.getID() + ",": EMPTY);
					fromNode.setID(nodeID + edge.getID() + "/from");
					nodeID = (toNode.getID() != null && toNode.getID().length() > 0? toNode.getID() + ",": EMPTY);
					toNode.setID(nodeID + edge.getID() + "/to");

					LOGGER.debug("Create edge '{}' from '{}' to '{}'", edge.getID(), fromNode.getPoint(), toNode.getPoint());
				}
				if(!edges.contains(edge)){
					fromNode.addOutEdge(edge);
					edges.add(edge);

					addedEdges.add(edge);

					LOGGER.debug("Connect edge '{}' to node '{}' and '{}'", edge.getID(), fromNode.getID(), toNode.getID());
				}
			}
		for(final Node intersectionNode1 : intersectionNodes)
			for(final Node intersectionNode2 : intersectionNodes)
				if(!intersectionNode1.equals(intersectionNode2)){
					final Edge edge = Edge.createDirectEdge(intersectionNode1, intersectionNode2);
					if(id != null){
						edge.setID(id);

						final String nodeID = (edge.getID() != null && edge.getID().length() > 0? edge.getID() + ",": EMPTY);
						edge.setID(nodeID + edge.getID() + "/from-to");

						LOGGER.debug("Create self edge '{}': point {}", edge.getID(), edge.getFrom().getPoint());
					}
					if(!edges.contains(edge)){
						intersectionNode1.addOutEdge(edge);
						intersectionNode2.addOutEdge(edge);
						edges.add(edge);

						addedEdges.add(edge);

						LOGGER.debug("Connect edge '{}' to intersection node '{}' and '{}'", edge.getID(), intersectionNode1.getID(), intersectionNode2.getID());
					}
				}

		if(tree != null){
			final Polyline polyline = from.getFactory().createPolyline(from, to);
			final Envelope geoBoundingBox = polyline.getBoundingBox();
			tree.insert(geoBoundingBox, polyline);
		}

		return addedEdges;
	}

	private Collection<Node> connectNodes(final Point newPoint){
		final Collection<Node> nodes = getApproximateNode(newPoint);
		final Point virtualStartPoint = calculateVirtualPoint(nodes, newPoint);
		for(final Node node : nodes)
			node.setPoint(virtualStartPoint);
		return nodes;
	}

	//NOTE: not the true average, but close enough
	private static Point calculateVirtualPoint(final Collection<Node> nodes, final Point newPoint){
		final Node node = nodes.iterator()
			.next();
		//connect the new node to the middle point already calculated (get this point from the first node)
		Point point = node.getPoint();
		double latitude = point.getY();
		double longitude = point.getX();
		if(nodes.size() == 1){
			//calculate the middle point between the (only) found node, plus the new one
			//NOTE: the points are close enough that simply a Euclidean mean is valid
			latitude = (newPoint.getY() + point.getY()) / 2.;
			longitude = (newPoint.getX() + point.getX()) / 2.;
		}
		final GeometryFactory factory = point.getFactory();
		return factory.createPoint(longitude, latitude);
	}

	private Collection<Node> getApproximateNode(final Point point){
		final Collection<Node> closest = getNodesNear(point);
		if(closest.isEmpty()){
			final Node node = new Node(EMPTY, point);
			nodeMap.put(point, node);
			closest.add(node);
		}
		return closest;
	}

	public Collection<Node> getNodesNear(final Point point){
		final Set<Node> closest = new HashSet<>(0);
		for(final Map.Entry<Point, Node> entry : nodeMap.entrySet())
			if(point.distance(entry.getKey()) <= threshold)
				closest.add(entry.getValue());
		return closest;
	}

	@Override
	public boolean isEmpty(){
		return nodeMap.isEmpty();
	}

	@Override
	public Collection<Node> nodes(){
		return nodeMap.values();
	}

	@Override
	public Collection<Edge> edges(){
		return edges;
	}


	@Override
	public boolean canHaveEdgesNear(){
		return (tree != null);
	}

	@Override
	public Collection<Edge> getEdgesNear(final Point point, final double threshold){
		if(tree == null)
			throw new IllegalArgumentException("Tree is not defined, call .withTree() while constructing the graph");

		final Point northEast = GeodeticHelper.destination(GeodeticHelper.destination(point, 0., threshold), 90., threshold);
		final Point southWest = GeodeticHelper.destination(GeodeticHelper.destination(point, 180., threshold), 270., threshold);
		final Envelope envelope = Envelope.of(northEast, southWest);
		final List<Polyline> polylines = tree.query(envelope);

		final List<Edge> edges = new ArrayList<>(0);
		for(final Edge edge : this.edges)
			if(polylines.contains(edge.getPolyline()))
				edges.add(edge);
		return edges;
	}

	@Override
	public Node getClosestNode(final Point point){
		if(tree == null)
			throw new IllegalArgumentException("Tree is not defined, call .withTree() while constructing the graph");

		Node closestNode = null;
		double minimumDistance = Double.POSITIVE_INFINITY;
		for(final Map.Entry<Point, Node> entry : nodeMap.entrySet()){
			final double distance = point.distance(entry.getKey());
			if(distance <= minimumDistance){
				minimumDistance = distance;
				closestNode = entry.getValue();
			}
		}
		return closestNode;
	}


	@Override
	public String toString(){
		return graphAsString()
			.toString();
	}

	@Override
	public String toStringWithObservations(final GPSPoint[] observations){
		final StringJoiner sj = graphAsString();
		for(final GPSPoint observation : observations)
			if(observation != null)
				sj.add(observation.toString());
		return sj.toString();
	}

	private StringJoiner graphAsString(){
		final StringJoiner sj = new StringJoiner(", ", "GEOMETRYCOLLECTION (", ")");
		for(final Edge edge : edges){
			final Point from = edge.getFrom().getPoint();
			final Point to = edge.getTo().getPoint();
			sj.add(from.getFactory().createPolyline(from, to).toString());
		}
		return sj;
	}

}
