package testUtillities;
import supportTools.SwarmMapInit;
import common.PlanetMap;
import common.LiveMap;
import common.ScienceLocations;
import common.RoverLocations;
import common.Coord;
import enums.Terrain;
import enums.RoverToolType;
public class LiveMapTest {
    public static void main(String[] args) throws Exception {
        SwarmMapInit swarm = new SwarmMapInit();
        swarm.parseInputFromDisplayTextFile("MapDefault.txt");
        PlanetMap pmap = swarm.getPlanetMap();
        LiveMap live = new LiveMap(swarm.getMapWidth(), swarm.getMapHeight());
        ScienceLocations sloc = new ScienceLocations();
        RoverLocations rloc = new RoverLocations();
        live.addScanMap(pmap.getScanMap(new Coord(10,10), 7, rloc, sloc), new Coord(10,10), RoverToolType.NONE, RoverToolType.NONE);
        live.addScanMap(pmap.getScanMap(new Coord(14,14), 7, rloc, sloc), new Coord(14,14), RoverToolType.NONE, RoverToolType.NONE);
        live.addScanMap(pmap.getScanMap(new Coord(6,10), 7, rloc, sloc), new Coord(6,10), RoverToolType.NONE, RoverToolType.NONE);
        live.addScanMap(pmap.getScanMap(new Coord(5,7), 7, rloc, sloc), new Coord(5,7), RoverToolType.NONE, RoverToolType.NONE);
        live.addScanMap(pmap.getScanMap(new Coord(2,4), 7, rloc, sloc), new Coord(2,4), RoverToolType.NONE, RoverToolType.NONE);
        live.addScanMap(pmap.getScanMap(new Coord(6,14), 7, rloc, sloc), new Coord(6,14), RoverToolType.NONE, RoverToolType.NONE);
        live.addScanMap(pmap.getScanMap(new Coord(4,2), 7, rloc, sloc), new Coord(4,2), RoverToolType.NONE, RoverToolType.NONE);
        for(int i = 0; i < 20; i++) {
            for(int j = 0; j < 20; j++) {
                System.out.print(live.getTile(j,i).getTerrain());
            }
            System.out.print("\n");
        }
    }
}