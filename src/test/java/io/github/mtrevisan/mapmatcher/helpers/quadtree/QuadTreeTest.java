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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;


class QuadTreeTest{

	@Test
	void contains_all_max_regions(){
		QuadTreeOptions options = new QuadTreeOptions()
			.withMaxRegionsPerNode(10);
		QuadTree tree = QuadTree.create(Region.of(2., 2., 33., 33.));
		List<Region> regions = Arrays.asList(
			Region.of(5., 5., 10., 10.),
			Region.of(25., 25., 10., 10.),
			Region.of(5., 5., 12., 10.),
			Region.of(25., 25., 10., 10.),
			Region.of(5., 25., 20., 10.),
			Region.of(25., 5., 10., 10.),
			Region.of(2., 2., 2., 2.)
		);
		for(Region region : regions)
			tree.insert(region, options);

		for(Region region : regions)
			Assertions.assertTrue(tree.intersects(region));
		Assertions.assertFalse(tree.intersects(Region.of(100., 100., 1., 1.)));
	}

	@Test
	void delete_max_regions(){
		QuadTreeOptions options = new QuadTreeOptions()
			.withMaxRegionsPerNode(10);
		QuadTree tree = QuadTree.create(Region.of(2., 2., 33., 33.));
		List<Region> regions = Arrays.asList(
			Region.of(5., 5., 10., 10.),
			Region.of(25., 25., 10., 10.),
			Region.of(5., 5., 12., 10.),
			Region.of(25., 25., 10., 10.),
			Region.of(5., 25., 20., 10.),
			Region.of(25., 5., 10., 10.),
			Region.of(2., 2., 2., 2.)
		);
		for(Region region : regions)
			tree.insert(region, options);

		int deleteIndex = 6;
		Assertions.assertTrue(tree.delete(regions.get(deleteIndex), options));
		Assertions.assertFalse(tree.delete(Region.of(25., 25., 10., 12.), options));
		for(Region region : regions){
			if(!region.equals(regions.get(deleteIndex)))
				Assertions.assertTrue(tree.intersects(region));
			else
				Assertions.assertFalse(tree.intersects(region));
		}
	}


	@Test
	void contains_all(){
		QuadTreeOptions options = new QuadTreeOptions()
			.withMaxRegionsPerNode(1);
		QuadTree tree = QuadTree.create(Region.of(2., 2., 33., 33.));
		List<Region> regions = Arrays.asList(
			Region.of(5., 5., 10., 10.),
			Region.of(25., 25., 10., 10.),
			Region.of(5., 5., 12., 10.),
			Region.of(25., 25., 10., 10.),
			Region.of(5., 25., 20., 10.),
			Region.of(25., 5., 10., 10.),
			Region.of(2., 2., 2., 2.)
		);
		for(Region region : regions)
			tree.insert(region, options);

		for(Region region : regions)
			Assertions.assertTrue(tree.intersects(region));
		Assertions.assertFalse(tree.intersects(Region.of(100., 100., 1., 1.)));
	}

	@Test
	void query(){
		QuadTreeOptions options = new QuadTreeOptions()
			.withMaxRegionsPerNode(1);
		QuadTree tree = QuadTree.create(Region.of(2., 2., 33., 33.));
		List<Region> regions = Arrays.asList(
			Region.of(5., 5., 10., 10.),
			Region.of(25., 25., 10., 10.),
			Region.of(5., 5., 12., 10.),
			Region.of(25., 25., 10., 10.),
			Region.of(5., 25., 20., 10.),
			Region.of(25., 5., 10., 10.),
			Region.of(2., 2., 2., 2.)
		);
		for(Region region : regions)
			tree.insert(region, options);

		final Region queriedRegion = Region.of(3., 3., 3., 3.);
		Assertions.assertEquals(3, tree.query(queriedRegion).size());
	}

	@Test
	void delete(){
		QuadTreeOptions options = new QuadTreeOptions()
			.withMaxRegionsPerNode(1);
		QuadTree tree = QuadTree.create(Region.of(2., 2., 33., 33.));
		List<Region> regions = Arrays.asList(
			Region.of(5., 5., 10., 10.),
			Region.of(25., 25., 10., 10.),
			Region.of(5., 5., 12., 10.),
			Region.of(25., 25., 10., 10.),
			Region.of(5., 25., 20., 10.),
			Region.of(25., 5., 10., 10.),
			Region.of(2., 2., 2., 2.)
		);
		for(Region region : regions)
			tree.insert(region, options);

		int deleteIndex = 6;
		Assertions.assertTrue(tree.delete(regions.get(deleteIndex), options));
		Assertions.assertFalse(tree.delete(Region.of(25., 25., 10., 12.), options));
		for(Region region : regions){
			if(!region.equals(regions.get(deleteIndex)))
				Assertions.assertTrue(tree.intersects(region));
			else
				Assertions.assertFalse(tree.intersects(region));
		}
	}

}
