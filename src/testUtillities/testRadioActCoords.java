package testUtillities;

import org.junit.Test;

import common.Coord;

import org.junit.Assert;

public class testRadioActCoords {
		
	@Test
	public void testCoords() {
		String coordStr = "2,3";		
		String[] coords = coordStr.split(",");
		
		String coord1 = coords[0];
		String coord2 = coords[1];
		Assert.assertEquals("2", coord1);
		Assert.assertEquals("3", coord2);
		
		int coord1Int = Integer.valueOf(coord1);
		int coord2Int = Integer.valueOf(coord2);
		Assert.assertEquals(2, coord1Int);
		Assert.assertEquals(3, coord2Int);
		
		Coord coord = new Coord(coord1Int, coord2Int);
		
	}
}
