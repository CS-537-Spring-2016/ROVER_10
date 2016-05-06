package a_star;

import common.MapTile;
import enums.Terrain;

public class AStarMap implements TileBasedMap {
	MapTile[][] scanMapTiles;
	
	public AStarMap(MapTile[][] scanMap) {
		this.scanMapTiles = scanMap;		
	}
	
	@Override
	public int getWidthInTiles() {
		return scanMapTiles[0].length; //return the size of the first element; that is the width essentially
	}

	@Override
	public int getHeightInTiles() {
		return scanMapTiles.length;
	}

	@Override
	public void pathFinderVisited(int x, int y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean blocked(Mover mover, int x, int y) {
		return (scanMapTiles[x][y].getHasRover() || scanMapTiles[x][y].getTerrain() == Terrain.ROCK
				|| scanMapTiles[x][y].getTerrain() == Terrain.NONE
				|| scanMapTiles[x][y].getTerrain() == Terrain.SAND);
	}
	

	@Override
	public float getCost(Mover mover, int sx, int sy, int tx, int ty) {
		return 0;
	}

}
