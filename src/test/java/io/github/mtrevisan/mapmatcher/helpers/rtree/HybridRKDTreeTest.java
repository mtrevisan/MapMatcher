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
package io.github.mtrevisan.mapmatcher.helpers.rtree;

import io.github.mtrevisan.mapmatcher.helpers.kdtree.HybridKDTree;
import io.github.mtrevisan.mapmatcher.helpers.quadtree.Region;
import io.github.mtrevisan.mapmatcher.spatial.GeometryFactory;
import io.github.mtrevisan.mapmatcher.spatial.topologies.EuclideanCalculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class HybridRKDTreeTest{

	private static final GeometryFactory FACTORY_EUCLIDEAN = new GeometryFactory(new EuclideanCalculator());


	@Test
	void simple(){
		RTreeOptions options = new RTreeOptions()
			.withMinObjects(1)
			.withMaxObjects(10);
		RTree quadTree = RTree.create();
		HybridKDTree.insert(quadTree, Region.of(10., 10., 20., 20.), options);
		HybridKDTree.insert(quadTree, Region.of(5., 5., 15., 15.), options);
		HybridKDTree.insert(quadTree, Region.of(25., 25., 35., 35.), options);
		HybridKDTree.insert(quadTree, Region.of(5., 5., 17., 15.), options);
		HybridKDTree.insert(quadTree, Region.of(5., 25., 25., 35.), options);
		HybridKDTree.insert(quadTree, Region.of(25., 5., 35., 15.), options);
		HybridKDTree.insert(quadTree, Region.of(2., 2., 4., 4.), options);
		Region region = Region.of(5., 5., 10., 10.);
		HybridKDTree.insert(quadTree, region, FACTORY_EUCLIDEAN.createPoint(1., 1.), options);
		HybridKDTree.insert(quadTree, region, FACTORY_EUCLIDEAN.createPoint(2., 2.), options);
		HybridKDTree.insert(quadTree, region, FACTORY_EUCLIDEAN.createPoint(1., 2.), options);

		Assertions.assertTrue(HybridKDTree.contains(quadTree, region, FACTORY_EUCLIDEAN.createPoint(1., 1.)));
		Assertions.assertFalse(HybridKDTree.contains(quadTree, region, FACTORY_EUCLIDEAN.createPoint(10., 10.)));
		Assertions.assertEquals(FACTORY_EUCLIDEAN.createPoint(2., 2.),
			HybridKDTree.nearestNeighbor(quadTree, region, FACTORY_EUCLIDEAN.createPoint(3., 3.)));
	}

	@Test
	void italy(){
		RTreeOptions options = new RTreeOptions()
			.withMinObjects(2)
			.withMaxObjects(3);
		RTree quadTree = RTree.create();
		//italy (IT)
//		HybridKDTree.insert(quadTree, Region.of(6.6272658, 35.4929521, 18.5205121, 47.0921462).withID("IT"), options);
		//sicilia (IT-82)
//		HybridKDTree.insert(quadTree, Region.of(11.9258571, 35.4929521, 15.6530796, 38.8176638).withID("IT-82"), options);
		//sicilia (IT-82), agrigento (IT-AG)
		HybridKDTree.insert(quadTree, Region.of(12.3201246, 35.4929521, 14.0386072, 37.7428334).withID("IT-82-AG"), options);
		//sicilia (IT-82), siracusa (IT-ST)
		HybridKDTree.insert(quadTree, Region.of(14.7729261, 36.6441671, 15.3367251, 37.4128826).withID("IT-82-ST"), options);
		//sicilia (IT-82), ragusa (IT-RG)
		HybridKDTree.insert(quadTree, Region.of(14.3372325, 36.6858792, 15.0078667, 37.1393668).withID("IT-82-RG"), options);
		//sicilia (IT-82), trapani (IT-TP)
		HybridKDTree.insert(quadTree, Region.of(11.9258571, 36.7343812, 13.1001376, 38.1896376).withID("IT-82-TP"), options);
		//sicilia (IT-82), caltanissetta (IT-CL)
		HybridKDTree.insert(quadTree, Region.of(13.6507368, 37.0020778, 14.4768137, 37.7450177).withID("IT-82-CL"), options);
		//sicilia (IT-82), catania (IT-CT)
		HybridKDTree.insert(quadTree, Region.of(14.3517735, 37.050297, 15.2580859, 37.961106).withID("IT-82-CT"), options);
		//sicilia (IT-82), enna (IT-EN)
		HybridKDTree.insert(quadTree, Region.of(14.0562834, 37.2907801, 14.8298134, 37.8800862).withID("IT-82-EN"), options);
		//sicilia (IT-82), palermo (IT-PA)
		HybridKDTree.insert(quadTree, Region.of(12.9287426, 37.5393034, 14.2943904, 38.7206781).withID("IT-82-PA"), options);
		//sicilia (IT-82), messina (IT-ME)
		HybridKDTree.insert(quadTree, Region.of(14.1834248, 37.7950379, 15.6530796, 38.8176638).withID("IT-82-ME"), options);
		//puglia (IT-75)
//		HybridKDTree.insert(quadTree, Region.of(14.9341858, 39.7895944, 18.5205121, 42.2264805).withID("IT-75"), options);
		//puglia (IT-75), lecce (IT-LE)
		HybridKDTree.insert(quadTree, Region.of(17.762464, 39.7895944,18.5205121, 40.5148674).withID("IT-75-LE"), options);
		//puglia (IT-75), taranto (IT-TA)
		HybridKDTree.insert(quadTree, Region.of(16.6973101, 40.2944732, 17.8004172, 40.803251).withID("IT-75-TA"), options);
		//puglia (IT-75), brindisi (IT-BR)
		HybridKDTree.insert(quadTree, Region.of(17.295343, 40.3795066, 18.098336, 40.8922178).withID("IT-75-BR"), options);
		//puglia (IT-75), bari (IT-BA)
		HybridKDTree.insert(quadTree, Region.of(16.203292, 40.6929782, 17.399673, 41.2295907).withID("IT-75-BA"), options);
		//puglia (IT-75), foggia (IT-FG)
		HybridKDTree.insert(quadTree, Region.of(14.9341858, 41.0557147, 16.2014289, 42.2264805).withID("IT-75-FG"), options);
		//puglia (IT-75), barletta-andria-trani (IT-BT)
		HybridKDTree.insert(quadTree, Region.of(15.8710923, 40.8987028, 16.5419992, 41.4426848).withID("IT-75-BT"), options);
		//puglia (IT-75), arcidiocesi di manfredonia-vieste-san giovanni rotondo
		HybridKDTree.insert(quadTree, Region.of(15.4750386, 41.3894884, 16.1999353, 42.2264805).withID("IT-75-null"), options);
		//basilicata (IT-77)
//		HybridKDTree.insert(quadTree, Region.of(15.3349712, 39.8958079, 16.8670356, 41.1409017).withID("IT-77"), options);
		//basilicata (IT-77), potenza (IT-PZ)
		HybridKDTree.insert(quadTree, Region.of(15.3349712, 39.8958079, 16.3995736, 41.1409017).withID("IT-77-PZ"), options);
		//basilicata (IT-77), matera (IT-MT)
		HybridKDTree.insert(quadTree, Region.of(15.9422533, 40.0566506, 16.8670356, 40.838581).withID("IT-77-MT"), options);
		//campania (IT-72)
//		HybridKDTree.insert(quadTree, Region.of(13.7623799, 39.9905158, 15.8064468, 41.5080786).withID("IT-72"), options);
		//campania (IT-72), salerno (IT-SA)
		HybridKDTree.insert(quadTree, Region.of(14.4282965, 39.9905158, 15.8064468, 40.8487482).withID("IT-72-SA"), options);
		//campania (IT-72), napoli (IT-NA)
		HybridKDTree.insert(quadTree, Region.of(13.8508534, 40.5358786, 14.6702478, 41.019847).withID("IT-72-NA"), options);
		//campania (IT-72), avellino (IT-AV)
		HybridKDTree.insert(quadTree, Region.of(14.5594641, 40.706645, 15.5723064, 41.2864497).withID("IT-72-AV"), options);
		//campania (IT-72), caserta (IT-CE)
		HybridKDTree.insert(quadTree, Region.of(13.7623799, 40.898783, 14.5353095, 41.5080786).withID("IT-72-CE"), options);
		//campania (IT-72), benevento (IT-BN)
		HybridKDTree.insert(quadTree, Region.of(14.3520045, 40.9750316, 15.1491557, 41.4863874).withID("IT-72-BN"), options);
		//lazio (IT-62)
//		HybridKDTree.insert(quadTree, Region.of(11.4491695, 40.7849283, 14.0276445, 42.840269).withID("IT-62"), options);
		//lazio (IT-62), latina (IT-LT)
		HybridKDTree.insert(quadTree, Region.of(12.530159, 40.7849283, 13.8948148, 41.7148329).withID("IT-62-LT"), options);
		//lazio (IT-62), frosinone (IT-FR)
		HybridKDTree.insert(quadTree, Region.of(12.9918722, 41.301351, 14.0276445, 41.9554478).withID("IT-62-FR"), options);
		//lazio (IT-62), roma capitale (IT-RM)
		HybridKDTree.insert(quadTree, Region.of(11.7337425, 41.4076117, 13.2962768, 42.2965732).withID("IT-62-RM"), options);
		//lazio (IT-62), rieti (IT-RI)
		HybridKDTree.insert(quadTree, Region.of(12.4372963, 42.0889167, 13.4099792, 42.7409686).withID("IT-62-RI"), options);
		//lazio (IT-62), viterbo (IT-VT)
		HybridKDTree.insert(quadTree, Region.of(11.4491695, 42.1471742, 12.5198374, 42.840269).withID("IT-62-VT"), options);
		//molise (IT-67)
//		HybridKDTree.insert(quadTree, Region.of(13.9410911, 41.3640631, 15.1616821, 42.0702848).withID("IT-67"), options);
		//molise (IT-67), campobasso (IT-CB)
		HybridKDTree.insert(quadTree, Region.of(14.3777332, 41.3640631, 15.1616821, 42.0702848).withID("IT-67-CB"), options);
		//molise (IT-67), isernia (IT-IS)
		HybridKDTree.insert(quadTree, Region.of(13.9410911, 41.3876719, 14.5222524, 41.9117554).withID("IT-67-IS"), options);
		//toscana (IT-52)
//		HybridKDTree.insert(quadTree, Region.of(9.6867692, 42.237615, 12.3713544, 44.4725419).withID("IT-52"), options);
		//toscana (IT-52), grosseto (IT-GR)
		HybridKDTree.insert(quadTree, Region.of(10.7062668, 42.237615, 11.820158, 43.1891437).withID("IT-52-GR"), options);
		//toscana (IT-52), livorno (IT-LI)
		HybridKDTree.insert(quadTree, Region.of(9.7912939, 42.3133103, 10.7962196, 43.6346331).withID("IT-52-LI"), options);
		//toscana (IT-52), siena (IT-SI)
		HybridKDTree.insert(quadTree, Region.of(10.9083129, 42.7857809, 11.9827957, 43.5485568).withID("IT-52-SI"), options);
		//toscana (IT-52), pisa (IT-PI)
		HybridKDTree.insert(quadTree, Region.of(10.2574977, 43.1084854, 11.0133182, 43.834394).withID("IT-52-PI"), options);
		//toscana (IT-52), arezzo (IT-AR)
		HybridKDTree.insert(quadTree, Region.of(11.3976024, 43.1563413, 12.3713544, 43.8779929).withID("IT-52-AR"), options);
		//toscana (IT-52), firenze (IT-FI)
		HybridKDTree.insert(quadTree, Region.of(10.7111211, 43.4515008, 11.7529365, 44.2398961).withID("IT-52-FI"), options);
		//toscana (IT-52), lucca (IT-LU)
		HybridKDTree.insert(quadTree, Region.of(10.1433617, 43.7493355, 10.7375468, 44.2861712).withID("IT-52-LU"), options);
		//toscana (IT-52), prato (IT-PO)
		HybridKDTree.insert(quadTree, Region.of(10.9639122, 43.7586956, 11.2128196, 44.1123682).withID("IT-52-PO"), options);
		//toscana (IT-52), pistoia (IT-PT)
		HybridKDTree.insert(quadTree, Region.of(10.6209226, 43.7880964, 11.0714805, 44.1601774).withID("IT-52-PT"), options);
		//toscana (IT-52), massa-carrara (IT-MS)
		HybridKDTree.insert(quadTree, Region.of(9.6867692, 43.9753156, 10.2557058, 44.4725419).withID("IT-52-MS"), options);
		//umbria (IT-55)
//		HybridKDTree.insert(quadTree, Region.of(11.8920337, 42.3647769, 13.2641895, 43.6173443).withID("IT-55"), options);
		//umbria (IT-55), terni (IT-TR)
		HybridKDTree.insert(quadTree, Region.of(11.8920337, 42.3647769, 12.8961982, 42.9431276).withID("IT-55-TR"), options);
		//umbria (IT-55), perugia (IT-PG)
		HybridKDTree.insert(quadTree, Region.of(11.9123518, 42.5968383, 13.2641895, 43.6173443).withID("IT-55-PG"), options);
		//emilia-romagna (IT-45)
//		HybridKDTree.insert(quadTree, Region.of(9.1979117, 43.7318997, 12.7559006, 45.1395001).withID("IT-45"), options);
		//emilia-romagna (IT-45), forlì-cesena (IT-FC)
		HybridKDTree.insert(quadTree, Region.of(11.645549, 43.7405381, 12.4577806, 44.3306793).withID("IT-45-FC"), options);
		//emilia-romagna (IT-45), rimini (IT-RN)
		HybridKDTree.insert(quadTree, Region.of(12.0961656, 43.7318997, 12.7559006, 44.1622868).withID("IT-45-RN"), options);
		//emilia-romagna (IT-45), bologna (IT-BO)
		HybridKDTree.insert(quadTree, Region.of(10.8066008, 44.0622266, 11.8392441, 44.8053346).withID("IT-45-BO"), options);
		//emilia-romagna (IT-45), modena (IT-MO)
		HybridKDTree.insert(quadTree, Region.of(10.4698307, 44.114152, 11.3689308, 44.9629506).withID("IT-45-MO"), options);
		//emilia-romagna (IT-45), ravenna (IT-RA)
		HybridKDTree.insert(quadTree, Region.of(11.5249381, 44.0990967, 12.3839715, 44.6283408).withID("IT-45-RA"), options);
		//emilia-romagna (IT-45), reggio nell'emilia (IT-RE)
		HybridKDTree.insert(quadTree, Region.of(10.1423815, 44.225466, 10.8971498, 44.9920133).withID("IT-45-RE"), options);
		//emilia-romagna (IT-45), parma (IT-PR)
		HybridKDTree.insert(quadTree, Region.of(9.4381716, 44.3460313, 10.5085422, 45.0444369).withID("IT-45-PR"), options);
		//emilia-romagna (IT-45), ferrara (IT-FE)
		HybridKDTree.insert(quadTree, Region.of(11.235746, 44.5466121, 12.3987088, 44.986943).withID("IT-45-FE"), options);
		//emilia-romagna (IT-45), piacenza (IT-PC)
		HybridKDTree.insert(quadTree, Region.of(9.1979117, 44.5558766, 10.0831378, 45.1395001).withID("IT-45-PC"), options);
		//emilia-romagna (IT-45), prefettura
		HybridKDTree.insert(quadTree, Region.of(10.9286173, 44.6423889, 10.9295371, 44.642949).withID("IT-45-null"), options);
		//veneto (IT-34)
//		HybridKDTree.insert(quadTree, Region.of(10.6231032, 44.7924256, 13.1021703, 46.6806162).withID("IT-34"), options);
		//veneto (IT-34), rovigo (IT-RO)
		HybridKDTree.insert(quadTree, Region.of(11.173546, 44.7924256, 12.5534386, 45.161954).withID("IT-34-RO"), options);
		//veneto (IT-34), verona (IT-VR)
		HybridKDTree.insert(quadTree, Region.of(10.6231032, 45.0530672, 11.491357, 45.8318248).withID("IT-34-VR"), options);
		//veneto (IT-34), padova (IT-PD)
		HybridKDTree.insert(quadTree, Region.of(11.3909429, 45.09558, 12.2064075, 45.6872283).withID("IT-34-PD"), options);
		//veneto (IT-34), venezia (IT-VE)
		HybridKDTree.insert(quadTree, Region.of(11.9619782, 45.0556953, 13.1021703, 45.8546002).withID("IT-34-VE"), options);
		//veneto (IT-34), vicenza (IT-VI)
		HybridKDTree.insert(quadTree, Region.of(11.1361942, 45.2551958, 11.8362709, 46.0144637).withID("IT-34-VI"), options);
		//veneto (IT-34), treviso (IT-TV)
		HybridKDTree.insert(quadTree, Region.of(11.7430292, 45.5304763, 12.6853155, 46.0828957).withID("IT-34-TV"), options);
		//veneto (IT-34), belluno (IT-BL)
		HybridKDTree.insert(quadTree, Region.of(11.6659608, 45.8804429, 12.7322625, 46.6806162).withID("IT-34-BL"), options);
		//piemonte (IT-21)
//		HybridKDTree.insert(quadTree, Region.of(6.6272658, 44.0597158, 9.2142404, 46.4642213).withID("IT-21"), options);
		//piemonte (IT-21), cuneo (IT-CN)
		HybridKDTree.insert(quadTree, Region.of(6.8545067, 44.0597158, 8.2692128, 44.8566056).withID("IT-21-CN"), options);
		//piemonte (IT-21), alessandria (IT-AL)
		HybridKDTree.insert(quadTree, Region.of(8.1093301, 44.4647191, 9.2142404, 45.2054572).withID("IT-21-AL"), options);
		//piemonte (IT-21), asti (IT-AT)
		HybridKDTree.insert(quadTree, Region.of(7.8834321, 44.5189941, 8.513625, 45.1330048).withID("IT-21-AT"), options);
		//piemonte (IT-21), torino (IT-TO)
		HybridKDTree.insert(quadTree, Region.of(6.6272658, 44.713755, 8.1518662, 45.6023219).withID("IT-21-TO"), options);
		//piemonte (IT-21), biella (IT-BI)
		HybridKDTree.insert(quadTree, Region.of(7.8814575, 45.3760887, 8.3279084, 45.7576414).withID("IT-21-BI"), options);
		//piemonte (IT-21), verbano-cusio-ossola (IT-VB)
		HybridKDTree.insert(quadTree, Region.of(7.8684964, 45.7669327, 8.7289666, 46.4642213).withID("IT-21-VB"), options);
		//piemonte (IT-21), novara (IT-NO)
		HybridKDTree.insert(quadTree, Region.of(8.3105682, 45.2916099, 8.8428962, 45.8767896).withID("IT-21-NO"), options);
		//piemonte (IT-21), vercelli (IT-VC)
		HybridKDTree.insert(quadTree, Region.of(7.8613734, 45.1595211, 8.5811897, 45.9507585).withID("IT-21-VC"), options);
		//lombardia (IT-25)
//		HybridKDTree.insert(quadTree, Region.of(8.4978518, 44.6799091, 11.4276477, 46.6353523).withID("IT-25"), options);
		//lombardia (IT-25), pavia (IT-PV)
		HybridKDTree.insert(quadTree, Region.of(8.4978518, 44.6799091, 9.5505475, 45.393838).withID("IT-25-PV"), options);
		//lombardia (IT-25), mantova (IT-MN)
		HybridKDTree.insert(quadTree, Region.of(10.3090587, 44.9102742, 11.4276477, 45.4283223).withID("IT-25-MN"), options);
		//lombardia (IT-25), cremona (IT-CR)
		HybridKDTree.insert(quadTree, Region.of(9.4528532, 44.937668, 10.5967139, 45.5041724).withID("IT-25-CR"), options);
		//lombardia (IT-25), lodi (IT-LO)
		HybridKDTree.insert(quadTree, Region.of(9.3099901, 45.0526084, 9.900973, 45.4700243).withID("IT-25-LO"), options);
		//lombardia (IT-25), milano (IT-MI)
		HybridKDTree.insert(quadTree, Region.of(8.7060961, 45.1614258, 9.551457, 45.6434829).withID("IT-25-MI"), options);
		//lombardia (IT-25), brescia (IT-BS)
		HybridKDTree.insert(quadTree, Region.of(9.8362928, 45.2042549, 10.842675, 46.3548531).withID("IT-25-BS"), options);
		//lombardia (IT-25), bergamo (IT-BG)
		HybridKDTree.insert(quadTree, Region.of(9.4450549, 45.4221614, 10.2627486, 46.0912018).withID("IT-25-BG"), options);
		//lombardia (IT-25), varese (IT-VA)
		HybridKDTree.insert(quadTree, Region.of(8.552246, 45.5583143, 9.0662956, 46.1222569).withID("IT-25-VA"), options);
		//lombardia (IT-25), como (IT-CO)
		HybridKDTree.insert(quadTree, Region.of(8.8942419, 45.6393854, 9.4406278, 46.2395703).withID("IT-25-CO"), options);
		//lombardia (IT-25), lecco (IT-LC)
		HybridKDTree.insert(quadTree, Region.of(9.2437341, 45.6493752, 9.5411056, 46.151716).withID("IT-25-LC"), options);
		//lombardia (IT-25), sondrio (IT-SO)
		HybridKDTree.insert(quadTree, Region.of(9.2469833, 46.0113575, 10.6327598, 46.6353523).withID("IT-25-SO"), options);
		//lombardia (IT-25), monza-brianza (IT-MB)
		HybridKDTree.insert(quadTree, Region.of(9.0505698, 45.5367099, 9.4974636, 45.7424391).withID("IT-25-MB"), options);
		//valle d'aosta (IT-23)
//		HybridKDTree.insert(quadTree, Region.of(6.8015322, 45.4669522, 7.939504, 45.9876787).withID("IT-23"), options);
		//valle d'aosta (IT-23), haute-savoie (FR-74)
		HybridKDTree.insert(quadTree, Region.of(5.8051345, 45.6817078, 7.0452884, 46.4563726).withID("IT-23-FR-74"), options);
		//valle d'aosta (IT-23), diocèse d'annecy
		HybridKDTree.insert(quadTree, Region.of(5.7207927, 45.6817078, 7.0452884, 46.4082363).withID("IT-23-null"), options);
		//trentino-südtirol (IT-32)
//		HybridKDTree.insert(quadTree, Region.of(10.3817965, 45.6728669, 12.4779676, 47.0921462).withID("IT-32"), options);
		//trentino-südtirol (IT-32), provincia di trento (IT-TN)
		HybridKDTree.insert(quadTree, Region.of(10.4522048, 45.6728669, 11.9628023, 46.5330375).withID("IT-32-TN"), options);
		//trentino-südtirol (IT-32), bozen (IT-BZ)
		HybridKDTree.insert(quadTree, Region.of(10.3817965, 46.2197712, 12.4779676, 47.0921462).withID("IT-32-BZ"), options);
		//marche (IT-57)
//		HybridKDTree.insert(quadTree, Region.of(12.1854536, 42.6871561, 13.9163435, 43.9715999).withID("IT-57"), options);
		//marche (IT-57), ascoli piceno (IT-AP)
		HybridKDTree.insert(quadTree, Region.of(13.1875521, 42.6871561, 13.9163435, 43.0797782).withID("IT-57-AP"), options);
		//marche (IT-57), macerata (IT-MC)
		HybridKDTree.insert(quadTree, Region.of(12.8296453, 42.8318542, 13.7432808, 43.4739419).withID("IT-57-MC"), options);
		//marche (IT-57), pesaro-urbino (IT-PU)
		HybridKDTree.insert(quadTree, Region.of(12.1854536, 43.4165862, 13.1723132, 43.9715999).withID("IT-57-PU"), options);
		//marche (IT-57), ancona (IT-AN)
		HybridKDTree.insert(quadTree, Region.of(12.7461961, 43.2099152, 13.6586187, 43.7505106).withID("IT-57-AN"), options);
		//marche (IT-57), fermo (IT-FM)
		HybridKDTree.insert(quadTree, Region.of(13.2160385, 42.8901647, 13.8496502, 43.2942629).withID("IT-57-FM"), options);
		//marche (IT-57), diocesi di macerata-tolentino-recanati-cingoli-treia
		HybridKDTree.insert(quadTree, Region.of(13.1218581, 43.1497529, 13.6792134, 43.4739419).withID("IT-57-null"), options);
		//abruzzo (IT-65)
//		HybridKDTree.insert(quadTree, Region.of(13.0189718, 41.6820382, 14.7838895, 42.8950814).withID("IT-65"), options);
		//abruzzo (IT-65), chieti (IT-CH)
		HybridKDTree.insert(quadTree, Region.of(14.0756603, 41.7596519, 14.7838895, 42.4450747).withID("IT-65-CH"), options);
		//abruzzo (IT-65), pescara (IT-PE)
		HybridKDTree.insert(quadTree, Region.of(13.7661594, 42.0740441, 14.2538298, 42.5454921).withID("IT-65-PE"), options);
		//abruzzo (IT-65), teramo (IT-TE)
		HybridKDTree.insert(quadTree, Region.of(13.3469322, 42.4211162, 14.1463154, 42.8950814).withID("IT-65-TE"), options);
		//abruzzo (IT-65), l'acquila (IT-AQ)
		HybridKDTree.insert(quadTree, Region.of(13.0189718, 41.6820382, 14.2297358, 42.5912825).withID("IT-65-AQ"), options);
		//friuli-venezia giulia (IT-36)
//		HybridKDTree.insert(quadTree, Region.of(12.3213811, 45.5808987, 13.9186553, 46.6479539).withID("IT-36"), options);
		//friuli-venezia giulia (IT-36), trieste (IT-TS)
		HybridKDTree.insert(quadTree, Region.of(13.5757991, 45.5808987, 13.9186553, 45.8081828).withID("IT-36-TS"), options);
		//friuli-venezia giulia (IT-36), pordenone (IT-PN)
		HybridKDTree.insert(quadTree, Region.of(12.3213811, 45.7906045, 12.9906991, 46.412508).withID("IT-36-PN"), options);
		//friuli-venezia giulia (IT-36), gorizia (IT-GO)
		HybridKDTree.insert(quadTree, Region.of(13.2398255, 45.6739839, 13.6430755, 46.0513351).withID("IT-36-GO"), options);
		//friuli-venezia giulia (IT-36), udine (IT-UD)
		HybridKDTree.insert(quadTree, Region.of(12.4930436, 45.644003, 13.7172972, 46.6479539).withID("IT-36-UD"), options);
		//liguria (IT-42)
//		HybridKDTree.insert(quadTree, Region.of(7.4951614, 43.7756906, 10.0715114, 44.6764424).withID("IT-42"), options);
		//liguria (IT-42), imperia (IT-IM)
		HybridKDTree.insert(quadTree, Region.of(7.4951614, 43.7756906, 8.1353954, 44.1410201).withID("IT-42-IM"), options);
		//liguria (IT-42), savona (IT-SV)
		HybridKDTree.insert(quadTree, Region.of(7.9772768, 43.9382354, 8.6709233, 44.528545).withID("IT-42-SV"), options);
		//liguria (IT-42), la spezia (IT-SP)
		HybridKDTree.insert(quadTree, Region.of(9.4657374, 44.02324, 10.0715114, 44.441566).withID("IT-42-SP"), options);
		//liguria (IT-42), genova (IT-GE)
		HybridKDTree.insert(quadTree, Region.of(8.5715542, 44.2163249, 9.574846, 44.6764424).withID("IT-42-GE"), options);
		//calabria (IT-78)
//		HybridKDTree.insert(quadTree, Region.of(15.6298657, 37.9157947, 17.2064253, 40.1449242).withID("IT-78"), options);
		//calabria (IT-78), catanzaro (IT-CZ)
		HybridKDTree.insert(quadTree, Region.of(16.0954859, 38.4633398, 16.9205972, 39.1942391).withID("IT-78-CZ"), options);
		//calabria (IT-78), crotone (IT-KR)
		HybridKDTree.insert(quadTree, Region.of(16.6130213, 38.8924655, 17.2064253, 39.4824414).withID("IT-78-KR"), options);
		//calabria (IT-78), cosenza (IT-CS)
		HybridKDTree.insert(quadTree, Region.of(15.7564433, 39.048701, 17.0236705, 40.1449242).withID("IT-78-CS"), options);
		//calabria (IT-78), reggio calabria (IT-RC)
		HybridKDTree.insert(quadTree, Region.of(15.6298657, 37.9157947, 16.5834766, 38.5738018).withID("IT-78-RC"), options);
		//calabria (IT-78), vibo valentia (IT-VV)
		HybridKDTree.insert(quadTree, Region.of(15.82343, 38.428875, 16.4347613, 38.8244201).withID("IT-78-VV"), options);
		//sardigna (IT-88)
//		HybridKDTree.insert(quadTree, Region.of(8.1308446, 38.8590846, 9.8293196, 41.3131122).withID("IT-88"), options);
		//sardigna (IT-88), nuoro (IT-NU)
		HybridKDTree.insert(quadTree, Region.of(8.6111616, 39.8222077, 9.8293196, 40.7060029).withID("IT-88-NU"), options);
		//sardigna (IT-88), aristanis (IT-OR)
		HybridKDTree.insert(quadTree, Region.of(8.2749259, 39.6270116, 9.1729365, 40.4261074).withID("IT-88-OR"), options);
		//sardigna (IT-88), sassari (IT-SS)
		HybridKDTree.insert(quadTree, Region.of(8.1308446, 40.2965476, 9.2849064, 41.1207294).withID("IT-88-SS"), options);
		//sardigna (IT-88), cagliari (IT-CA)
		HybridKDTree.insert(quadTree, Region.of(8.6053505, 38.8640117, 9.6597945, 39.9183299).withID("IT-88-CA"), options);
		//sardigna (IT-88), medio campidano (IT-VS)
		HybridKDTree.insert(quadTree, Region.of(8.3792773, 39.374733, 9.0736886, 39.7829214).withID("IT-88-VS"), options);
		//sardigna (IT-88), sulcis iglesiente
		HybridKDTree.insert(quadTree, Region.of(8.207469, 38.8590846, 8.8531525, 39.4829916).withID("IT-88-sulcis"), options);
		//sardigna (IT-88), nord-est sardegna
		HybridKDTree.insert(quadTree, Region.of(8.8025523, 40.5258328, 9.7793564, 41.3131122).withID("IT-88-ne"), options);
		//sardigna (IT-88), ogliastra (IT-OG)
		HybridKDTree.insert(quadTree, Region.of(9.2783708, 9.5492929, 9.7355697, 40.2249519).withID("IT-88-OG"), options);
		Region region = Region.of(5., 5., 10., 10.);
		HybridKDTree.insert(quadTree, region, FACTORY_EUCLIDEAN.createPoint(1., 1.), options);
		HybridKDTree.insert(quadTree, region, FACTORY_EUCLIDEAN.createPoint(2., 2.), options);
		HybridKDTree.insert(quadTree, region, FACTORY_EUCLIDEAN.createPoint(1., 2.), options);

		Assertions.assertTrue(HybridKDTree.contains(quadTree, region, FACTORY_EUCLIDEAN.createPoint(1., 1.)));
		Assertions.assertFalse(HybridKDTree.contains(quadTree, region, FACTORY_EUCLIDEAN.createPoint(10., 10.)));
		Assertions.assertEquals(FACTORY_EUCLIDEAN.createPoint(2., 2.),
			HybridKDTree.nearestNeighbor(quadTree, region, FACTORY_EUCLIDEAN.createPoint(3., 3.)));
	}

}
