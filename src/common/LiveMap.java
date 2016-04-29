package common;
import enums.Science;
import enums.Terrain;
import enums.RoverToolType;
LiveMap extends PlanetMap {
    protected boolean[][][] explored;
    public LiveMap() {
        this.LiveMap(1000,1000);//this is a risky assumption, we should be cautious about it.
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
    public void addScanMap(ScanMap scan, Coord centerpos, String tool1, String tool2) {
        MapTile[][] mapArray = scan.getScanMap();
        boolean[] mask = new boolean[5];
        mask[0] = true;
        tool1 == RoverToolType.RADIATION_SENSOR || tool2 == RoverToolType.RADIATION_SENSOR ? mask[1] = true : mask[1] = false;
        tool1 == RoverToolType.CHEMICAL_SENSOR || tool2 == RoverToolType.CHEMICAL_SENSOR ? mask[2] = true : mask[2] = false;
        tool1 == RoverToolType.SPECTRAL_SENSOR || tool2 == RoverToolType.SPECTRAL_SENSOR ? mask[3] = true : mask[3] = false;
        tool1 == RoverToolType.RADAR_SENSOR || tool2 == RoverToolType.RADAR_SENSOR ? mask[4] = true : mask[4] = false;
        for(int i = 0; i < mapArray.length; i++) {
            for(int j = 0; j < mapArray[i].length; j++) {
                //assumption: there is only ever one type of science on a tile. If not, this can overwrite stuff.
                for(int m = 0; m < 5; m++) { //mask the explored array with our mask, to show we've covered such and such sensors.
                    explored[i-(scan.getEdgeSize/2)+centerpos.xpos][j-(scan.getEdgeSize/2)+centerpos.ypos][m] = mask || explored[i-(scan.getEdgeSize/2)+centerpos.xpos][j-(scan.getEdgeSize/2)+centerpos.ypos][m];
                }
                //this.planetMap[i-(scan.getEdgeSize/2)+centerpos.xpos][j-(scan.getEdgeSize/2)+centerpos.ypos] : the tile at the appropriate position on the LiveMap
                if(this.getTile(i,j).getScience() != Science.none || this.getTile(i-(scan.getEdgeSize/2)+centerpos.xpos, j-(scan.getEdgeSize/2)+centerpos.ypos).getScience() == Science.none) {
                    this.setTile(mapArray[i][j].getCopyOfMapTile(), i-(scan.getEdgeSize/2)+centerpos.xpos, j-(scan.getEdgeSize/2)+centerpos.ypos);
            }
        }
    }
    //Counts how many squares will be revealed by a rover at the given post with the given tools.
    public int revealCount(Coord pos, String tool1, String tool2) {
        int result = 0;
        int range = 7;
        if(tool1 == RoverToolType.RANGE_BOOTER || tool2 == RoverToolType.RANGE_BOOTER) { range = 11; }
        boolean[] mask = new boolean[5];
        mask[0] = true;
        tool1 == RoverToolType.RADIATION_SENSOR || tool2 == RoverToolType.RADIATION_SENSOR ? mask[1] = true : mask[1] = false;
        tool1 == RoverToolType.CHEMICAL_SENSOR || tool2 == RoverToolType.CHEMICAL_SENSOR ? mask[2] = true : mask[2] = false;
        tool1 == RoverToolType.SPECTRAL_SENSOR || tool2 == RoverToolType.SPECTRAL_SENSOR ? mask[3] = true : mask[3] = false;
        tool1 == RoverToolType.RADAR_SENSOR || tool2 == RoverToolType.RADAR_SENSOR ? mask[4] = true : mask[4] = false;
        for(int i = 0; i < range; i++) {
            for(int j = 0; j < range; j++) {
                for(int k = 0; k < 5; k++) {
                    if(mask[k] && !explored[i-(range/2)+centerpos.xpos][j-(range/2)+centerpos.ypos][m]) {
                        result++;
                    }
                }
            }
        }
        return result;
    }
}