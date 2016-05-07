package common;
import enums.Science;
import enums.Terrain;
import enums.RoverToolType;
import enums.RoverDriveType;
import java.util.ArrayList;
public class LiveMap extends PlanetMap {
    protected boolean[][][] explored;
    public LiveMap() {
        this(1000,1000);//this is a risky assumption, we should be cautious about it.
    }
    public LiveMap(int width, int height) {
        this(width,height,null,null);
    }
    public LiveMap(int width, int height, Coord startPos, Coord targetPos) {
        super(width, height, startPos, targetPos); 
        explored = new boolean[width][height][5];
        for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                for(int k = 0; k < 5; k++) {
                    explored[i][j][k] = false;
                }
            }
        }
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
                if(i-(scan.getEdgeSize()/2)+centerpos.xpos >= 0 && j-(scan.getEdgeSize()/2)+centerpos.ypos >= 0) {
                    boolean[] tileExplored = explored[i-(scan.getEdgeSize()/2)+centerpos.xpos][j-(scan.getEdgeSize()/2)+centerpos.ypos];
                    Science oldScience = this.getTile(i-(scan.getEdgeSize()/2)+centerpos.xpos, j-(scan.getEdgeSize()/2)+centerpos.ypos).getScience();
                    if((mask[0] && !tileExplored[0]) || mapArray[i][j].getScience() != Science.NONE || (mask[1] && oldScience == Science.RADIOACTIVE) || (mask[2] && oldScience == Science.ORGANIC) || (oldScience == Science.MINERAL) || (mask[4] && oldScience == Science.CRYSTAL)) {
                        this.setTile(mapArray[i][j].getCopyOfMapTile(), i-(scan.getEdgeSize()/2)+centerpos.xpos, j-(scan.getEdgeSize()/2)+centerpos.ypos);
                    }
                    for(int m = 0; m < 5; m++) { //mask the explored array with our mask, to show we've covered such and such sensors.
                        explored[i-(scan.getEdgeSize()/2)+centerpos.xpos][j-(scan.getEdgeSize()/2)+centerpos.ypos][m] = mask[m] || explored[i-(scan.getEdgeSize()/2)+centerpos.xpos][j-(scan.getEdgeSize()/2)+centerpos.ypos][m];
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
        for(int i = pos.xpos-(range/2); i <= pos.xpos+(range/2); i++) {
            for(int j = pos.ypos-(range/2); j <= pos.ypos+(range/2); j++) {
                for(int k = 0; k < 5; k++) {
                    if(i >= 0 && j >= 0 && mask[k] && !explored[i][j][k]) {
                        result++;
                    }
                }
            }
        }
        return result;
    }
    //"tries" all four cardinal directions and prints out how much would be revealed
    public void debugPrintRevealCounts(Coord pos, RoverToolType tool1, RoverToolType tool2) {
        System.out.println("N: " + Integer.toString(this.revealCount(new Coord(pos.xpos, pos.ypos-1), tool1, tool2)));
        System.out.println("E: " + Integer.toString(this.revealCount(new Coord(pos.xpos+1, pos.ypos), tool1, tool2)));
        System.out.println("S: " + Integer.toString(this.revealCount(new Coord(pos.xpos, pos.ypos+1), tool1, tool2)));
        System.out.println("W: " + Integer.toString(this.revealCount(new Coord(pos.xpos-1, pos.ypos), tool1, tool2)));
    }
    //A* pathfinder
    public char findPath(Coord start, Coord dest, RoverDriveType drive) {
        ArrayList<Coord> openSet = new ArrayList<Coord>();
        ArrayList<Coord> closedSet = new ArrayList<Coord>();
        openSet.add(start);
        Coord[][] cameFrom = new Coord[this.getWidth()][this.getHeight()];
        int[][] gScore = new int[this.getWidth()][this.getHeight()];
        int[][] fScore = new int[this.getWidth()][this.getHeight()];
        for(int i = 0; i < this.getWidth(); i++) {
            for(int j = 0; j < this.getHeight(); j++) {
                gScore[i][j] = 1000000;
                fScore[i][j] = 1000000; //1 million is effectively infinity
            }
        }
        fScore[start.xpos][start.ypos] = Math.abs(start.xpos-dest.xpos)+Math.abs(start.ypos-dest.ypos);
        while(!openSet.isEmpty()) {
            Coord current = null;
            for(int i = 0; i < openSet.size(); i++) {
                if(current == null || fScore[openSet.get(i).xpos][openSet.get(i).ypos] < fScore[current.xpos][current.ypos]) {
                    current = openSet.get(i);
                }
            }
            if(current.equals(dest)) {
                Coord prev = cameFrom[current.xpos][current.ypos];
                while(!start.equals(prev)) {
                    current = prev;
                    prev = cameFrom[prev.xpos][prev.ypos];
                }
                if(current.ypos < start.ypos) {
                    return 'N';
                } else if(current.xpos > start.xpos) {
                    return 'E';
                } else if(current.ypos > start.ypos) {
                    return 'S';
                } else {
                    return 'W';
                }
            }
            openSet.remove(current);
            closedSet.add(current);
            Coord[] neighbors = new Coord[4];
            neighbors[0] = new Coord(current.xpos, current.ypos-1);
            neighbors[1] = new Coord(current.xpos+1, current.ypos);
            neighbors[2] = new Coord(current.xpos, current.ypos+1);
            neighbors[3] = new Coord(current.xpos-1, current.ypos);
            for(int i  = 0; i < 4; i++) {
                if(!closedSet.contains(neighbors[i])) {
                    int tentativegScore = gScore[current.xpos][current.ypos];
                    if(blocked(neighbors[i], drive)) {
                        tentativegScore += 10000;
                    } else {
                        tentativegScore += 1;
                    }
                    if(!openSet.contains(neighbors[i])) {
                        openSet.add(neighbors[i]);
                        cameFrom[neighbors[i].xpos][neighbors[i].ypos] = current;
                        gScore[neighbors[i].xpos][neighbors[i].ypos] = tentativegScore;
                        fScore[neighbors[i].xpos][neighbors[i].ypos] = tentativegScore + Math.abs(neighbors[i].xpos-dest.xpos)+Math.abs(neighbors[i].ypos-dest.ypos);
                    }
                }
            }
        }
        return 'U'; //destination is unreachable
    }
    public boolean blocked(Coord pos, RoverDriveType drive) {
        Terrain ter = this.getTile(pos).getTerrain();
        if(ter == Terrain.SAND && drive != RoverDriveType.TREADS) {
            return true;
        }
        if(ter == Terrain.ROCK && drive != RoverDriveType.WALKER) {
            return true;
        }
        return false;
    }
}