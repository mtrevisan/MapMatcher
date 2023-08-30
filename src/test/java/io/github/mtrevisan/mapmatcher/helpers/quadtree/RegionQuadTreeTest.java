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

import io.github.mtrevisan.mapmatcher.helpers.bplustree.BPlusTree;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;


class RegionQuadTreeTest{

	@Test
	void contains_all_max_envelopes(){
		QuadTreeOptions options = QuadTreeOptions.withDefault()
			.withMaxRegionsPerNode(10);
		RegionQuadTree tree = RegionQuadTree.create(options, Region.of(2., 2., 33., 33.));
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
			tree.insert(region);

		for(Region region : regions)
			Assertions.assertTrue(tree.contains(region));
		Assertions.assertFalse(tree.contains(Region.of(100., 100., 1., 1.)));
	}

	@Test
	void delete_max_envelopes(){
		QuadTreeOptions options = QuadTreeOptions.withDefault()
			.withMaxRegionsPerNode(10);
		RegionQuadTree tree = RegionQuadTree.create(options, Region.of(2., 2., 33., 33.));
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
			tree.insert(region);

		int deleteIndex = 6;
		Assertions.assertTrue(tree.delete(regions.get(deleteIndex)));
		Assertions.assertFalse(tree.delete(Region.of(25., 25., 10., 12.)));
		for(Region region : regions){
			if(!region.equals(regions.get(deleteIndex)))
				Assertions.assertTrue(tree.contains(region));
			else
				Assertions.assertFalse(tree.contains(region));
		}
	}


	@Test
	void contains_all(){
		QuadTreeOptions options = QuadTreeOptions.withDefault()
			.withMaxRegionsPerNode(1);
		RegionQuadTree tree = RegionQuadTree.create(options, Region.of(2., 2., 33., 33.));
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
			tree.insert(region);
		final BPlusTree<BitCode, Region> bptree = BPlusTree.ofOrder(regions.size());
		for(Region region : regions)
			bptree.insert(region.getCode(), region);

		for(Region region : regions){
			Assertions.assertTrue(tree.contains(region));
			Assertions.assertFalse(bptree.query(region.getCode()).isEmpty());
		}
		Assertions.assertFalse(tree.contains(Region.of(100., 100., 1., 1.)));
	}

	@Test
	void query(){
		QuadTreeOptions options = QuadTreeOptions.withDefault()
			.withMaxRegionsPerNode(1);
		RegionQuadTree tree = RegionQuadTree.create(options, Region.of(2., 2., 33., 33.));
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
			tree.insert(region);
		final BPlusTree<BitCode, Region> bptree = BPlusTree.ofOrder(regions.size());
		for(Region region : regions)
			bptree.insert(region.getCode(), region);

		final Region queriedRegion = Region.of(3., 3., 3., 3.);
		Assertions.assertEquals(3, tree.query(queriedRegion).size());
		BitCode key = BitCode.ofEmpty();
		//TODO extract key from queried region
		Assertions.assertEquals(3, bptree.query(key).size());
	}

	@Test
	void delete(){
		QuadTreeOptions options = QuadTreeOptions.withDefault()
			.withMaxRegionsPerNode(1);
		RegionQuadTree tree = RegionQuadTree.create(options, Region.of(2., 2., 33., 33.));
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
			tree.insert(region);

		int deleteIndex = 6;
		Assertions.assertTrue(tree.delete(regions.get(deleteIndex)));
		Assertions.assertFalse(tree.delete(Region.of(25., 25., 10., 12.)));
		for(Region region : regions){
			if(!region.equals(regions.get(deleteIndex)))
				Assertions.assertTrue(tree.contains(region));
			else
				Assertions.assertFalse(tree.contains(region));
		}
	}

}
