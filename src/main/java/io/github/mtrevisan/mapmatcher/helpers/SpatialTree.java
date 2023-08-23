package io.github.mtrevisan.mapmatcher.helpers;

import io.github.mtrevisan.mapmatcher.spatial.Envelope;

import java.util.List;


public interface SpatialTree extends Tree{

	List<Envelope> query(Envelope envelope);

	void insert(Envelope envelope);

	boolean delete(Envelope envelope);

}
