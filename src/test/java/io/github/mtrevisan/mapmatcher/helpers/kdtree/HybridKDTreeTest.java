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
package io.github.mtrevisan.mapmatcher.helpers.kdtree;

import io.github.mtrevisan.mapmatcher.helpers.quadtree.Region;
import io.github.mtrevisan.mapmatcher.helpers.quadtree.RegionQuadTree;
import io.github.mtrevisan.mapmatcher.spatial.GeometryFactory;
import io.github.mtrevisan.mapmatcher.spatial.topologies.EuclideanCalculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class HybridKDTreeTest{

	private static final GeometryFactory FACTORY_EUCLIDEAN = new GeometryFactory(new EuclideanCalculator());


	@Test
	void simple(){
		int minObjects = 1;
		int maxObjects = 10;
		RegionQuadTree quadTree0 = RegionQuadTree.create(Region.of(2., 2., 35., 35.));
		quadTree0.insert(Region.of(10., 10., 10., 10.), minObjects, maxObjects);
		quadTree0.insert(Region.of(5., 5., 10., 10.), minObjects, maxObjects);
		quadTree0.insert(Region.of(25., 25., 10., 10.), minObjects, maxObjects);
		quadTree0.insert(Region.of(5., 5., 12., 10.), minObjects, maxObjects);
		quadTree0.insert(Region.of(5., 25., 20., 10.), minObjects, maxObjects);
		quadTree0.insert(Region.of(25., 5., 10., 10.), minObjects, maxObjects);
		quadTree0.insert(Region.of(2., 2., 2., 2.), minObjects, maxObjects);

		RegionQuadTree quadTree = RegionQuadTree.create(Region.of(2., 2., 35., 35.));
		HybridKDTree.insert(quadTree, Region.of(10., 10., 10., 10.), minObjects, maxObjects);
		HybridKDTree.insert(quadTree, Region.of(5., 5., 10., 10.), minObjects, maxObjects);
		HybridKDTree.insert(quadTree, Region.of(25., 25., 10., 10.), minObjects, maxObjects);
		HybridKDTree.insert(quadTree, Region.of(5., 5., 12., 10.), minObjects, maxObjects);
		HybridKDTree.insert(quadTree, Region.of(5., 25., 20., 10.), minObjects, maxObjects);
		HybridKDTree.insert(quadTree, Region.of(25., 5., 10., 10.), minObjects, maxObjects);
		HybridKDTree.insert(quadTree, Region.of(2., 2., 2., 2.), minObjects, maxObjects);
		Region region = Region.of(5., 5., 5., 5.);
		HybridKDTree.insert(quadTree, region, FACTORY_EUCLIDEAN.createPoint(1., 1.), minObjects, maxObjects);
		HybridKDTree.insert(quadTree, region, FACTORY_EUCLIDEAN.createPoint(2., 2.), minObjects, maxObjects);
		HybridKDTree.insert(quadTree, region, FACTORY_EUCLIDEAN.createPoint(1., 2.), minObjects, maxObjects);

		Assertions.assertTrue(HybridKDTree.contains(quadTree, region, FACTORY_EUCLIDEAN.createPoint(1., 1.)));
		Assertions.assertFalse(HybridKDTree.contains(quadTree, region, FACTORY_EUCLIDEAN.createPoint(10., 10.)));
	}

}
