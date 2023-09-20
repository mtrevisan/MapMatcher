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
package io.github.mtrevisan.mapmatcher.helpers.quadtree;

import io.github.mtrevisan.mapmatcher.helpers.SpatialNode;
import io.github.mtrevisan.mapmatcher.helpers.kdtree.HybridKDTree;
import io.github.mtrevisan.mapmatcher.spatial.GeometryFactory;
import io.github.mtrevisan.mapmatcher.spatial.topologies.EuclideanCalculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;


class HybridQuadKDTreeTest{

	private static final GeometryFactory FACTORY_EUCLIDEAN = new GeometryFactory(new EuclideanCalculator());


	@Test
	void simple(){
		QuadTreeOptions options = new QuadTreeOptions()
			.withMaxRegionsPerNode(1);
		QuadTree quadTree = QuadTree.create(Region.of(2., 2., 37., 37.), options);
		HybridKDTree<QuadTreeOptions> tree = HybridKDTree.create(quadTree);
		tree.insert(Region.of(10., 10., 20., 20.));
		tree.insert(Region.of(5., 5., 15., 15.));
		tree.insert(Region.of(25., 25., 35., 35.));
		tree.insert(Region.of(5., 5., 17., 15.));
		tree.insert(Region.of(5., 25., 25., 35.));
		tree.insert(Region.of(25., 5., 35., 15.));
		tree.insert(Region.of(2., 2., 4., 4.));
		Region region = Region.of(5., 5., 10., 10.);
		Map<Region, SpatialNode> nodes = new HashMap<>();
		tree.insert(nodes, region, FACTORY_EUCLIDEAN.createPoint(1., 1.));
		tree.insert(nodes, region, FACTORY_EUCLIDEAN.createPoint(2., 2.));
		tree.insert(nodes, region, FACTORY_EUCLIDEAN.createPoint(1., 2.));

		Assertions.assertTrue(tree.contains(nodes, region, FACTORY_EUCLIDEAN.createPoint(1., 1.)));
		Assertions.assertFalse(tree.contains(nodes, region, FACTORY_EUCLIDEAN.createPoint(10., 10.)));
		Assertions.assertEquals(FACTORY_EUCLIDEAN.createPoint(2., 2.),
			tree.nearestNeighbor(nodes, region, FACTORY_EUCLIDEAN.createPoint(3., 3.)));
	}

}
