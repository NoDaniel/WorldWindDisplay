package WWDisplay.WorldWindDisplay;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.NumberFormatter;

import org.hipparchus.util.FastMath;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.TopocentricFrame;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.Ephemeris;
import org.orekit.propagation.events.ElevationDetector;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.AirspaceLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Box;
import gov.nasa.worldwind.render.Cone;
import gov.nasa.worldwind.render.Cylinder;
import gov.nasa.worldwind.render.Ellipsoid;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Pyramid;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.Wedge;
import gov.nasa.worldwind.render.airspaces.Airspace;
import gov.nasa.worldwind.render.airspaces.AirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.Cake;
import gov.nasa.worldwind.render.airspaces.CappedCylinder;
import gov.nasa.worldwind.render.airspaces.Curtain;
import gov.nasa.worldwind.render.airspaces.Orbit;
import gov.nasa.worldwind.render.airspaces.PartialCappedCylinder;
import gov.nasa.worldwind.render.airspaces.Polygon;
import gov.nasa.worldwind.render.airspaces.SphereAirspace;
import gov.nasa.worldwind.render.airspaces.TrackAirspace;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import gov.nasa.worldwindx.examples.util.RandomShapeAttributes;


/**
 * This is the most basic WorldWind program.
 *
 * @version $Id: HelloWorldWind.java 1971 2014-04-29 21:31:28Z dcollins $
 */
public class App extends ApplicationTemplate
{
    // An inner class is used rather than directly subclassing JFrame in the main class so
    // that the main can configure system properties prior to invoking Swing. This is
    // necessary for instance on OS X (Macs) so that the application name can be specified.
    public static final String START_BUTTON = "ActionCommand_StartButton";
    public static final String PAUSE_BUTTON = "ActionCommand_PauseButton";
    public static final String RESUME_BUTTON = "ActionCommand_ResumeButton";
    public static final String STOP_BUTTON = "ActionCommand_StopButton";
    //public static SatelliteThread satThread;
    public static boolean threadRunning;
    

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
    	protected InterfaceController controller;
    	
        public AppFrame()
        {
        	super(true, false, false);
            //this.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
            //this.getContentPane().add(wwd, java.awt.BorderLayout.CENTER);
            this.controller = new InterfaceController(this.getWwd());
            this.controller.frame = this;
            this.makeShapes();
        	makeComponents();
            this.pack();

            //wwd.setModel(new BasicModel());
        }
       
        
        protected void makeShapes()
        {
    		
    		RenderableLayer shapesLayer = new RenderableLayer();	
    		insertBeforePlacenames(this.getWwd(), shapesLayer);
    		
            SphereAirspace airspace = new SphereAirspace();
            airspace.setTerrainConforming(true);
            airspace.setAttributes(new BasicAirspaceAttributes(Material.GREEN, 1d));
            airspace.setRadius(200000.0);
            airspace.setAltitude(100);
            shapesLayer.addRenderable((Renderable) airspace);
            // Add the layer to the model and update the layer panel.
            System.out.println(airspace.getLocation().latitude + " " + airspace.getLocation().longitude + " " + airspace.getAltitudes()[0] + " " + airspace.getAltitudes()[1]);
            
            SphereAirspace airspace2 = new SphereAirspace();
            airspace2.setTerrainConforming(true);
            airspace2.setAttributes(new BasicAirspaceAttributes(Material.RED, 1d));
            airspace.setRadius(200000.0);
            shapesLayer.addRenderable((Renderable) airspace2);
              
    		SatelliteThread satThread = new SatelliteThread();
    		satThread.init(getWwd(), airspace, airspace2);
    		threadRunning = false;
    		new Thread(satThread).start();

        }
        
        protected void makeComponents()
        {
			 JPanel btnPanel = new JPanel();
            {
            		            	
            	btnPanel.setBorder(new EmptyBorder(5,5,5,5));
            	
                JButton startBtn = new JButton("Start");
                startBtn.setActionCommand(START_BUTTON);
                startBtn.setPreferredSize(new Dimension(100, 40));
                startBtn.addActionListener(this.controller);
                btnPanel.add(startBtn);
                
                JButton rewindBtn;
                rewindBtn = new JButton("Rewind");
                rewindBtn.setActionCommand(PAUSE_BUTTON);
                rewindBtn.setPreferredSize(new Dimension(100, 40));
                //btn.addActionListener(this.controller);
                btnPanel.add(rewindBtn);
                
                JButton pauseBtn;
                pauseBtn = new JButton("Pause");
                pauseBtn.setActionCommand(RESUME_BUTTON);
                pauseBtn.setPreferredSize(new Dimension(100, 40));
                //btn.addActionListener(this.controller);
                btnPanel.add(pauseBtn);
                
                JButton resumeBtn;
                resumeBtn = new JButton("Resume");
                resumeBtn.setActionCommand(STOP_BUTTON);
                resumeBtn.setPreferredSize(new Dimension(100, 40));
                //btn.addActionListener(this.controller);
                btnPanel.add(resumeBtn);
            }
            
            JPanel detailsPanel = new JPanel(new GridLayout(6,2,0,1));
            {
            	detailsPanel.setBorder(new EmptyBorder(10,10,10,10));
            	JLabel curDate = new JLabel();
            	 
            	DateFormat dateFormat = new SimpleDateFormat("dd MMM YYYY");
                JFormattedTextField date = new JFormattedTextField(dateFormat);
                date.setEditable(false);
                date.setValue(new Date());
                date.setBorder(null);
                
               
            	
            	detailsPanel.add(date);
                 
            }
            
			JPanel controlPanel = new JPanel(new GridLayout(3, 1));
			{
				controlPanel.add(btnPanel);
				controlPanel.add(detailsPanel);
				
			}

			getContentPane().add(controlPanel, BorderLayout.WEST);
        	
        	
        }
    }

    public static class InterfaceController implements ActionListener
    {
        protected AppFrame frame;
        // World Wind stuff.
        protected WorldWindow wwd;

        public InterfaceController(WorldWindow wwd)
        {
            this.wwd = wwd;
        }

        public void actionPerformed(ActionEvent e)
        {
            if (START_BUTTON.equalsIgnoreCase(e.getActionCommand()))
            {
                this.doActionOnButton1();
            }           
        }

        public void doActionOnButton1()
        {       
        	System.out.println("Start thread");
        	if(threadRunning == false)
        	{
        		System.out.println("Thread started");
    			//new Thread(satThread).start();
    			threadRunning = true;
        	}
        }

    }
    
    public static void main(String[] args)
    {

    	File orekitData = new File("data/orekit-data");
    	DataProvidersManager manager = DataProvidersManager.getInstance();
    	manager.addProvider(new DirectoryCrawler(orekitData));
    	
    	new AppFrame().setVisible(true);
    	
        
        //start("World Wind Get Elevations Demo", AppFrame.class);
    }
}

  