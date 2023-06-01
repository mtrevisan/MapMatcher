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

import io.github.mtrevisan.mapmatcher.helpers.PathHelper;
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


public class NearNodeMergeGraph implements Graph{

	private static final Logger LOGGER = LoggerFactory.getLogger(NearNodeMergeGraph.class);

	private static final String EMPTY = "";


	private final Set<Edge> edges = new HashSet<>(0);
	private final Map<Point, Node> nodeMap = new TreeMap<>();

	private final double threshold;

	private HPRtree<Polyline> tree;


	public NearNodeMergeGraph(final double threshold){
		if(threshold < 0.)
			throw new IllegalArgumentException("Threshold must be non-negative");

		this.threshold = threshold;
	}

	public NearNodeMergeGraph withTree(){
		tree = new HPRtree<>();

		return this;
	}

	@Override
	public GeometryFactory getFactory(){
		for(final Polyline polyline : tree.nodes())
			return polyline.getFactory();
		return null;
	}

	public Collection<Edge> addApproximateEdge(final Point from, final Point to){
		final Collection<Edge> addedEdges = addApproximateDirectEdge(from, to);
		addedEdges.addAll(addApproximateDirectEdge(to, from));
		return addedEdges;
	}

	public Collection<Edge> addApproximateEdge(final Polyline path){
		final Collection<Edge> addedEdges = addApproximateDirectEdge(path);
		addedEdges.addAll(addApproximateDirectEdge(path.reverse()));
		return addedEdges;
	}

	public Collection<Edge> addApproximateEdge(final String id, final Point from, final Point to){
		final Collection<Edge> addedEdges = addApproximateDirectEdge(id, from, to);
		addedEdges.addAll(addApproximateDirectEdge(id + PathHelper.REVERSED_EDGE_SUFFIX, to, from));
		return addedEdges;
	}

	public Collection<Edge> addApproximateEdge(final String id, final Polyline path){
		final Collection<Edge> addedEdges = addApproximateDirectEdge(id, path);
		addedEdges.addAll(addApproximateDirectEdge(id + PathHelper.REVERSED_EDGE_SUFFIX, path.reverse()));
		return addedEdges;
	}

	public Collection<Edge> addApproximateDirectEdge(final Point from, final Point to){
		return addApproximateDirectEdge(null, from.getFactory().createPolyline(from, to));
	}

	public Collection<Edge> addApproximateDirectEdge(final Polyline path){
		return addApproximateDirectEdge(null, path);
	}

	public Collection<Edge> addApproximateDirectEdge(final String id, final Point from, final Point to){
		return addApproximateDirectEdge(id, from.getFactory().createPolyline(from, to));
	}

	public Collection<Edge> addApproximateDirectEdge(final String id, final Polyline path){
		if(path == null || path.size() < 2)
			return Collections.emptyList();

		final Collection<Edge> addedEdges = new HashSet<>(0);
		final Collection<Node> startNodes = connectNodes(path.getStartPoint());
		final Collection<Node> endNodes = connectNodes(path.getEndPoint());
		final Set<Node> intersectionNodes = new HashSet<>(startNodes);
		intersectionNodes.retainAll(endNodes);

		for(final Node fromNode : startNodes)
			for(final Node toNode : endNodes){
				final Edge edge = Edge.createDirectEdge(fromNode, toNode, path);
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
					final Edge edge = Edge.createDirectEdge(intersectionNode1, intersectionNode2, path);
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
			final Envelope geoBoundingBox = path.getBoundingBox();
			tree.insert(geoBoundingBox, path);
		}

		return addedEdges;
	}

	private Collection<Node> connectNodes(final Point newPoint){
		final Collection<Node> nodes = getApproximateNode(newPoint);
		final Point virtualPoint = calculateVirtualPoint(nodes, newPoint);
		for(final Node node : nodes)
			node.setPoint(virtualPoint);
		return nodes;
	}

	private Collection<Node> getApproximateNode(final Point point){
		final Collection<Node> closest = getNodesNear(point);
		if(closest.isEmpty()){
			final Node node = Node.of(EMPTY, point);
			nodeMap.put(point, node);
			closest.add(node);
		}
		return closest;
	}

	public Collection<Node> getNodesNear(final Point point){
		final Set<Node> closest = new HashSet<>(0);
		if(threshold > 0.)
			for(final Map.Entry<Point, Node> entry : nodeMap.entrySet()){
				if(point.distance(entry.getKey()) <= threshold)
					closest.add(entry.getValue());
			}
		else
			for(final Map.Entry<Point, Node> entry : nodeMap.entrySet())
				if(point.equals(entry.getKey()))
					closest.add(entry.getValue());
		return closest;
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
			//NOTE: the points should be close enough that simply an Euclidean mean should be acceptable
			latitude = (newPoint.getY() + point.getY()) / 2.;
			longitude = (newPoint.getX() + point.getX()) / 2.;
		}
		final GeometryFactory factory = point.getFactory();
		return factory.createPoint(longitude, latitude);
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

		final Point northEast = GeodeticHelper.destination(point, 45., threshold);
		final Point southWest = GeodeticHelper.destination(point, 225., threshold);
		final Envelope envelope = Envelope.of(northEast, southWest);
		final List<Polyline> polylines = tree.query(envelope);

		final List<Edge> edges = new ArrayList<>(0);
		for(final Edge edge : this.edges)
			if(polylines.contains(edge.getPath()))
				edges.add(edge);
		return edges;
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
		for(final Edge edge : edges)
			sj.add(edge.getPath().toString());
		return sj;
	}

}
