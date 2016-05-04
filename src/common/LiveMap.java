package common;
import enums.Science;
import enums.Terrain;
import enums.RoverToolType;
public class LiveMap extends PlanetMap {
    protected boolean[][][] explored;
    public LiveMap() {
        this(1000,1000);//this is a risky assumption, we should be cautious about it.
    }
    public LiveMap(int width, int height) {
        super(width, height);
        explored = new boolean[width][height][5];
    }
    public LiveMap(int width, int height, Coord startPos, Coord targetPos) {
        super(width, height, startPos, targetPos); 
        explored = new boolean[width][height][5];
    }
    //adds a scanmap to the livemap.
    public void addScanMap(ScanMap scan, Coord centerpos, RoverToolType tool1, RoverToolType tool2) {
        MapTile[][] mapArray = scan.getScanMap();
        boolean[] mask = new boolean[5];
        mask[0] = true;
        mask[1] = tool1 == RoverToolType.RADIATION_SENSOR || tool2 == RoverToolType.RADIATION_SENSOR ?  true : false;
        mask[2] = tool1 == RoverToolType.CHEMICAL_SENSOR || tool2 == RoverToolType.CHEMICAL_SENSOR ? true : false;
        mask[3] = tool1 == RoverToolType.SPECTRAL_SENSOR || tool2 == RoverToolType.SPECTRAL_SENSOR ? true : false;
        mask[4] = tool1 == RoverToolType.RADAR_SENSOR || tool2 == RoverToolType.RADAR_SENSOR ? true : false;
        for(int i = 0; i < mapArray.length; i++) {
            for(int j = 0; j < mapArray[i].length; j++) {
                //If we're inbounds
                if(i-(scan.getEdgeSize()/2)+centerpos.xpos >= 0 && j-(scan.getEdgeSize()/2)+centerpos.ypos) {
                    //assumption: there is only ever one type of science on a tile. If not, this can overwrite stuff.
                    for(int m = 0; m < 5; m++) { //mask the explored array with our mask, to show we've covered such and such sensors.
                        explored[i-(scan.getEdgeSize()/2)+centerpos.xpos][j-(scan.getEdgeSize()/2)+centerpos.ypos][m] = mask[m] || explored[i-(scan.getEdgeSize()/2)+centerpos.xpos][j-(scan.getEdgeSize()/2)+centerpos.ypos][m];
                    }
                    //this.planetMap[i-(scan.getEdgeSize()/2)+centerpos.xpos][j-(scan.getEdgeSize()/2)+centerpos.ypos] : the tile at the appropriate position on the LiveMap
                    //if the tile has science on the scanmap, or the tile has no science on the world map
                    if(mapArray[i][j].getScience() != Science.NONE || this.getTile(i-(scan.getEdgeSize()/2)+centerpos.xpos, j-(scan.getEdgeSize()/2)+centerpos.ypos).getScience() == Science.NONE) {
                        this.setTile(mapArray[i][j].getCopyOfMapTile(), i-(scan.getEdgeSize()/2)+centerpos.xpos, j-(scan.getEdgeSize()/2)+centerpos.ypos);
                    }
                }
            }
        }
    }
    //Counts how many squares will be revealed by a rover at the given post with the given tools.
    public int revealCount(Coord pos, RoverToolType tool1, RoverToolType tool2) {
        int result = 0;
        int range = 7;
        if(tool1 == RoverToolType.RANGE_BOOTER || tool2 == RoverToolType.RANGE_BOOTER) { range = 11; }
        boolean[] mask = new boolean[5];
        mask[0] = true;
        mask[1] = tool1 == RoverToolType.RADIATION_SENSOR || tool2 == RoverToolType.RADIATION_SENSOR ?  true : false;
        mask[2] = tool1 == RoverToolType.CHEMICAL_SENSOR || tool2 == RoverToolType.CHEMICAL_SENSOR ? true : false;
        mask[3] = tool1 == RoverToolType.SPECTRAL_SENSOR || tool2 == RoverToolType.SPECTRAL_SENSOR ? true : false;
        mask[4] = tool1 == RoverToolType.RADAR_SENSOR || tool2 == RoverToolType.RADAR_SENSOR ? true : false;
        for(int i = 0; i < range; i++) {
            for(int j = 0; j < range; j++) {
                for(int k = 0; k < 5; k++) {
                    if(mask[k] && !explored[i-(range/2)+pos.xpos][j-(range/2)+pos.ypos][k]) {
                        result++;
                    }
                }
            }
        }
        return result;
    }
}