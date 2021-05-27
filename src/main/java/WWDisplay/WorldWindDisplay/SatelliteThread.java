package WWDisplay.WorldWindDisplay;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;

import org.hipparchus.geometry.euclidean.threed.Vector3D;
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
import gov.nasa.worldwind.view.orbit.OrbitViewInputHandler;

public class SatelliteThread implements Runnable {

	private WorldWindow wwd;
	private App.AppFrame frame;
	
	private SphereAirspace sphereAirspaceReference;
	private SphereAirspace sphereAirspaceActual;
	Ellipsoid sphere;
	private boolean running;
	private boolean follow;
	View view;	 
	int i = 0;
	int posFlag;
	private ArrayList<GeodeticPoint> positionsReference;
	private ArrayList<GeodeticPoint> positionsActual;
	private int speed = 2000;
	
	public SatelliteThread() {
		super();
		
		positionsReference = ExtractData("TC_WP3_005_Orekit.out");
		positionsActual = ExtractData("TC_WP3_005_Orekit2.out");
		System.out.println("This is the size man " + positionsReference.size());
		running = true;
		follow = false;
	}


	//initialize the thread,like the constructor
	public synchronized void init(WorldWindow wwd, SphereAirspace airspaceReference, SphereAirspace airspaceActual, App.AppFrame frame) {
		this.sphereAirspaceReference = airspaceReference;
		this.sphereAirspaceActual = airspaceActual;
		this.wwd = wwd;
		view = this.wwd.getView();
		this.frame = frame;
        //wwd.redraw();
	}

	private ArrayList<GeodeticPoint> ExtractData(String dataFileName)
	{		
        String referenceFolder = "./input";
        DataParser parser = new DataParser();
        final File referenceFile = new File(referenceFolder, dataFileName);
        ArrayList<GeodeticPoint> vals = parser.ParseFile(referenceFile);
        
        return vals;
	}
	
	public void setSpeed(int speed)
	{
		System.out.println(speed);
		this.speed = speed;
	}
	
	public void addSize(double size)
	{
		if(this.sphereAirspaceActual.getRadius() + size <= 0)
		{
			return;
		}
		
		this.sphereAirspaceActual.setRadius(this.sphereAirspaceActual.getRadius() + size);
		this.sphereAirspaceReference.setRadius(this.sphereAirspaceReference.getRadius() + size);
		System.out.println(size);
		wwd.redraw();
	}
	
	public void jumpTo(String days)
	{
		if(this.i + Integer.parseInt(days) * 1440 > positionsActual.size())
		{
			return;
		}
		
		this.i = this.i + Integer.parseInt(days) * 1440;
		System.out.println(days);
		
		double lonDegrees = FastMath.toDegrees(positionsActual.get(i).getLongitude());
        double latDegrees = FastMath.toDegrees(positionsActual.get(i).getLatitude());
        double alt = positionsActual.get(i).getAltitude();

        Position pos = new Position(LatLon.fromDegrees(latDegrees, lonDegrees), alt);
		sphereAirspaceActual.setLocation(pos);
		sphereAirspaceActual.setAltitude(alt);

		this.frame.UpdatePosValuesActual(pos.getLatitude(), pos.getLongitude(), pos.getAltitude());
		
        double lonDegrees2 = FastMath.toDegrees(positionsReference.get(i).getLongitude());
        double latDegrees2 = FastMath.toDegrees(positionsReference.get(i).getLatitude());
        double alt2 = positionsReference.get(i).getAltitude();

        Position pos2 = new Position(LatLon.fromDegrees(latDegrees2, lonDegrees2), alt2);
		sphereAirspaceReference.setLocation(pos2);
		sphereAirspaceReference.setAltitude(alt2);
		i++;
		
		
		this.frame.UpdatePosValuesReference(pos2.getLatitude(), pos2.getLongitude(), pos2.getAltitude());
		this.frame.UpdateDelta(pos2.getLatitude().subtract(pos.getLatitude()),
							   pos2.getLongitude().subtract(pos.getLongitude()), 
							   alt2 - alt);
		
		Vector3D vectorPos = new Vector3D(lonDegrees, latDegrees, alt);
		Vector3D vectorPos2 = new Vector3D(lonDegrees2, latDegrees2, alt2);
		this.frame.UpdatePos(vectorPos.distance(vectorPos2));
		
		long timeInMilliseconds = speed; // Time in milliseconds you want the animation to take.
		OrbitViewInputHandler ovih = (OrbitViewInputHandler) view.getViewInputHandler();
		ovih.addPanToAnimator(pos, view.getHeading(), view.getPitch(), alt2 + 5000000, timeInMilliseconds, true);
				
		wwd.redraw();
	}
	
	public void pause()
	{
		this.running = false;
	}
	
	public void resume()
	{
		System.out.println("hallo");
		this.running = true;
	}
	
	public void restart()
	{
		this.i = 1;
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setFollow()
	{
		if(this.follow == true)
		{
			this.follow = false;			
		}
		else
		{
			this.follow = true;
		}

	}
	
	public void begin()
	{
		sphereAirspaceActual.setVisible(true);
		sphereAirspaceReference.setVisible(true);
		
		double lonDegrees = FastMath.toDegrees(positionsActual.get(i).getLongitude());
        double latDegrees = FastMath.toDegrees(positionsActual.get(i).getLatitude());
        double alt = positionsActual.get(i).getAltitude();

        Position pos = new Position(LatLon.fromDegrees(latDegrees, lonDegrees), alt);
		sphereAirspaceActual.setLocation(pos);
		sphereAirspaceActual.setAltitude(alt);

		this.frame.UpdatePosValuesActual(pos.getLatitude(), pos.getLongitude(), pos.getAltitude());
		
        double lonDegrees2 = FastMath.toDegrees(positionsReference.get(i).getLongitude());
        double latDegrees2 = FastMath.toDegrees(positionsReference.get(i).getLatitude());
        double alt2 = positionsReference.get(i).getAltitude();

        Position pos2 = new Position(LatLon.fromDegrees(latDegrees2, lonDegrees2), alt2);
		sphereAirspaceReference.setLocation(pos2);
		sphereAirspaceReference.setAltitude(alt2);
		i++;
		
		long timeInMilliseconds = speed; // Time in milliseconds you want the animation to take.
		OrbitViewInputHandler ovih = (OrbitViewInputHandler) view.getViewInputHandler();
		ovih.addPanToAnimator(pos, view.getHeading(), view.getPitch(), alt2 + 5000000, timeInMilliseconds, true);

	}
	
	public void run() {
		
		this.begin();			
		
		while(true)
		{
			//System.out.println(i);
			if(running == true)
			{
				
				try {
					Thread.sleep(speed);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
					
	
				
	            double lonDegrees = FastMath.toDegrees(positionsActual.get(i).getLongitude());
	            double latDegrees = FastMath.toDegrees(positionsActual.get(i).getLatitude());
	            double alt = positionsActual.get(i).getAltitude();
	
	            Position pos = new Position(LatLon.fromDegrees(latDegrees, lonDegrees), alt);
				sphereAirspaceActual.setLocation(pos);
				sphereAirspaceActual.setAltitude(alt);
				
				
				this.frame.UpdatePosValuesActual(pos.getLatitude(), pos.getLongitude(), pos.getAltitude());
			
				
	            double lonDegrees2 = FastMath.toDegrees(positionsReference.get(i).getLongitude());
	            double latDegrees2 = FastMath.toDegrees(positionsReference.get(i).getLatitude());
	            double alt2 = positionsReference.get(i).getAltitude();
	
	            Position pos2 = new Position(LatLon.fromDegrees(latDegrees2, lonDegrees2), alt2);
				sphereAirspaceReference.setLocation(pos2);
				sphereAirspaceReference.setAltitude(alt2);
				
				this.frame.UpdatePosValuesReference(pos2.getLatitude(), pos2.getLongitude(), pos2.getAltitude());
				this.frame.UpdateDelta(pos2.getLatitude().subtract(pos.getLatitude()),
									   pos2.getLongitude().subtract(pos.getLongitude()), 
									   alt2 - alt);
				
				Vector3D vectorPos = new Vector3D(lonDegrees, latDegrees, alt);
				Vector3D vectorPos2 = new Vector3D(lonDegrees2, latDegrees2, alt2);
				this.frame.UpdatePos(vectorPos.distance(vectorPos2));
				
				if(follow == true)
				{
					long timeInMilliseconds = speed; // Time in milliseconds you want the animation to take.
					OrbitViewInputHandler ovih = (OrbitViewInputHandler) view.getViewInputHandler();
					ovih.addPanToAnimator(pos, view.getHeading(), view.getPitch(), alt2 + 5000000, timeInMilliseconds, true);
				}

				wwd.redraw();
				i++;
			
			}
			
		}
	
	}

}


