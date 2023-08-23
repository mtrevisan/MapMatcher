package io.github.mtrevisan.mapmatcher.helpers.kdtree;

import io.github.mtrevisan.mapmatcher.helpers.SpatialTree;
import io.github.mtrevisan.mapmatcher.spatial.Envelope;
import io.github.mtrevisan.mapmatcher.spatial.Point;

import java.util.List;


public class AbstractHybridKDTree{

	public void insert(final SpatialTree tree, final Envelope envelope, final Point point){
		List<Envelope> envelopes = query(tree, envelope);

		if(!envelopes.isEmpty())
			for(final Envelope queriedEnvelope : envelopes)
				if(queriedEnvelope.isBoundary()){
					final KDTree kdTree = new KDTree();
					final KDNode kdNode = (KDNode)queriedEnvelope.getNode();
					kdTree.insert(kdNode, point);
					return;
				}

		envelope.setBoundary();
		envelope.setNode(new KDNode(point));
		tree.insert(envelope);
	}

	public void insert(final SpatialTree tree, final Envelope envelope){
		tree.insert(envelope);
	}

	public boolean query(final SpatialTree tree, final Envelope boundary, final Point point){
		final List<Envelope> envelopes = tree.query(boundary);

		for(final Envelope envelope : envelopes){
			if(envelope.isBoundary()){
				final KDTree kdTree = new KDTree();
				final KDNode kdNode = (KDNode)envelope.getNode();
				if(kdTree.query(kdNode, point)){
					return true;
				}
			}
		}
		return false;
	}

	public List<Envelope> query(final SpatialTree tree, final Envelope envelope){
		return tree.query(envelope);
	}

}
