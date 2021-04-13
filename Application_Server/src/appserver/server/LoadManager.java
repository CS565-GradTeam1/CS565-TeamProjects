package appserver.server;

import java.util.ArrayList;

/**
 *
 * @author Dr.-Ing. Wolf-Dieter Otte
 */
public class LoadManager {

    static ArrayList satellites = null;
    static int lastSatelliteIndex = -1;

    public LoadManager() {
        satellites = new ArrayList<String>();
    }

    public void satelliteAdded(String satelliteName) {
        // add satellite
        // ...
        satellites.add(satelliteName);
    }


    public String nextSatellite() throws Exception {
        
        int numberSatellites;
        int num;
        String satelliteName;
        
        synchronized (satellites) {
            // implement policy that returns the satellite name according to a round robin methodology
            // ...
            numberSatellites = satellites.size();
            
            lastSatelliteIndex++;
            num = lastSatelliteIndex % numberSatellites;
            
            satelliteName = satellites.get(num).toString();
        }

        return  satelliteName;// ... name of satellite who is supposed to take job
        
    }
}
