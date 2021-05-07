package WWDisplay.WorldWindDisplay;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hipparchus.util.FastMath;
import org.orekit.bodies.GeodeticPoint;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.animation.DoubleAnimator;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.AirspaceLayer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Ellipsoid;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.airspaces.Airspace;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.SphereAirspace;

public class SatelliteThread implements Runnable {

	private WorldWindow wwd;
	
	private SphereAirspace sphereAirspaceReference;
	private SphereAirspace sphereAirspaceActual;
	Ellipsoid sphere;
	private boolean pause;
	private boolean restart;
	private boolean stop;
	View view;	 
	int i = 0;
	private GeodeticPoint[] positionsReference;
	private GeodeticPoint[] positionsActual;
	
	public SatelliteThread() {
		super();
		
		positionsReference = ExtractData("TC_WP3_005_Orekit.out");
		positionsActual = ExtractData("TC_WP3_005_Orekit.out");
	}

	public synchronized void pause() {
		this.pause = true;
	}
	public synchronized void restart() {
		this.restart=true;
	}
	
	public synchronized void stop() {
		this.stop=true;
	}
	
	public synchronized void resume() {
		this.pause = false;
		this.restart = false;
	}
	
	public synchronized void delete() {
	}
	//initialize the thread,like the constructor
	public synchronized void init(WorldWindow wwd, SphereAirspace airspaceReference, SphereAirspace airspaceActual) {
		this.sphereAirspaceReference = airspaceReference;
		this.sphereAirspaceActual = airspaceActual;
		this.wwd = wwd;
		view = this.wwd.getView();
        //wwd.redraw();
	}

	private GeodeticPoint[] ExtractData(String dataFileName)
	{		
        String referenceFolder = "./input";
        DataParser parser = new DataParser();
        final File referenceFile = new File(referenceFolder, dataFileName);
        GeodeticPoint vals[] = parser.ParseFile(referenceFile);
        
        return vals;
	}
	
	public void run() {
		while(true)
		{
			i++;
			
			try {
				Thread.sleep(2500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
					
			
            double lonDegrees = FastMath.toDegrees(positionsActual[i].getLongitude());
            double latDegrees = FastMath.toDegrees(positionsActual[i].getLatitude());
            double alt = positionsActual[i].getAltitude();

            Position pos = new Position(LatLon.fromDegrees(latDegrees, lonDegrees), alt);
            //System.out.println(sphereAirspaceActual.getLocation());
			sphereAirspaceReference.move(Position.fromDegrees(latDegrees, lonDegrees));
			sphereAirspaceReference.setAltitude(alt);
			System.out.println(lonDegrees + " " + latDegrees);
			
            //view.goTo(pos,50000000);
			
			wwd.redraw();
			

			
		}
	
	}

}


