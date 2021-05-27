package WWDisplay.WorldWindDisplay;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
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
import javax.swing.JTextField;
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
    public static final String SPEEDX1_BUTTON = "ActionCommand_SpeedX1Button";
    public static final String SPEEDX2_BUTTON = "ActionCommand_SpeedX2Button";
    public static final String SPEEDX4_BUTTON = "ActionCommand_SpeedX4Button";
    public static final String SPEEDX8_BUTTON = "ActionCommand_SpeedX8Button";
    public static final String SPEEDX16_BUTTON = "ActionCommand_SpeedX16Button";
    public static final String SPEEDX32_BUTTON = "ActionCommand_SpeedX32Button";
    public static final String SPEEDX64_BUTTON = "ActionCommand_SpeedX64Button";
    public static final String PAUSE_BUTTON = "ActionCommand_PauseButton";
    public static final String RESTART_BUTTON = "ActionCommand_RestartButton";
    public static final String RESUME_BUTTON = "ActionCommand_ResumeButton";
    public static final String FOLLOW_BUTTON = "ActionCommand_FollowButton";
    public static final String JUMP_BUTTON = "ActionCommand_JumpButton";
    public static final String SIZE_BUTTON = "ActionCommand_SizeButton";
    public static final String SIZE_BUTTON2 = "ActionCommand_SizeButton2";
    public static boolean threadRunning;
    public static SatelliteThread satThread;
    public static JTextField textFieldDays;


    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
    	protected InterfaceController controller;
        
		JLabel labelLatActual;
		JLabel labelLonActual;
		JLabel labelAltActual;
		
		JLabel labelLatReference;
		JLabel labelLonReference;
		JLabel labelAltReference;
		
		JLabel labelDeltaLat;
		JLabel labelDeltaLon;
		JLabel labelDeltaAlt;
		JLabel labelDeltaPos;

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
            airspace.setRadius(100000.0);
            //airspace.setAltitude(100);
            airspace.setVisible(false);
            shapesLayer.addRenderable((Renderable) airspace);
           
            
            SphereAirspace airspace2 = new SphereAirspace();
            airspace2.setTerrainConforming(true);
            airspace2.setAttributes(new BasicAirspaceAttributes(Material.RED, 1d));
            airspace2.setRadius(100000.0);
            //airspace2.setAltitude(100);
            airspace2.setVisible(false);
            shapesLayer.addRenderable((Renderable) airspace2);
              
    		satThread = new SatelliteThread();
    		satThread.init(getWwd(), airspace, airspace2, this); 		
    		threadRunning = false;
    		//new Thread(satThread).start(); 

        }
        
        protected void makeComponents()
        {
			 JPanel btnPanel = new JPanel(new GridLayout(4,1,5,5));
            {
            		            	
            	btnPanel.setBorder(new EmptyBorder(5,5,0,5));
            	
            	JPanel mainBtnPanel = new JPanel(new GridLayout(1,4,5,5));
            	{
            		
            		  JPanel mainBtnInnerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 2));
    				  {
            			  JButton startBtn = new JButton("Start");
                          startBtn.setActionCommand(START_BUTTON);
                          startBtn.setPreferredSize(new Dimension(120, 60));
                          startBtn.addActionListener(this.controller);
                          mainBtnInnerPanel.add(startBtn);                
                          
                          JButton pauseBtn;
                          pauseBtn = new JButton("Pause");
                          pauseBtn.setActionCommand(PAUSE_BUTTON);
                          pauseBtn.setPreferredSize(new Dimension(120, 60));
                          pauseBtn.addActionListener(this.controller);
                          mainBtnInnerPanel.add(pauseBtn);
                          
                          JButton restartBtn;
                          restartBtn = new JButton("Restart");
                          restartBtn.setActionCommand(RESTART_BUTTON);
                          restartBtn.setPreferredSize(new Dimension(120, 60));
                          restartBtn.addActionListener(this.controller);
                          mainBtnInnerPanel.add(restartBtn);
                          
                          JButton followBtn;
                          followBtn = new JButton("Lock");
                          followBtn.setActionCommand(FOLLOW_BUTTON);
                          followBtn.setPreferredSize(new Dimension(120, 60));
                          followBtn.addActionListener(this.controller);
                          mainBtnInnerPanel.add(followBtn);
    				  }
            		 
                      mainBtnPanel.add(mainBtnInnerPanel);
            	}     
            
            	
            	JPanel speedBtnPanel = new JPanel(new GridLayout(1,9,10,10));
            	{
            		
            		JPanel speedBtnInnerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    				{
                		JButton speedBtnx1 = new JButton("x1");
                		speedBtnx1.setFont(new Font("Time News Roman", Font.BOLD, 10));
                        speedBtnx1.setActionCommand(SPEEDX1_BUTTON);
                        speedBtnx1.setPreferredSize(new Dimension(60, 60));
                        speedBtnx1.addActionListener(this.controller);
                        speedBtnInnerPanel.add(speedBtnx1);
                        
                        JButton speedBtnx2 = new JButton("x2");
                        speedBtnx2.setFont(new Font("Time News Roman", Font.BOLD, 10));
                        speedBtnx2.setActionCommand(SPEEDX2_BUTTON);
                        speedBtnx2.setPreferredSize(new Dimension(60, 60));
                        speedBtnx2.addActionListener(this.controller);
                        speedBtnInnerPanel.add(speedBtnx2);
                        
                        JButton speedBtnx4 = new JButton("x4");
                        speedBtnx4.setFont(new Font("Time News Roman", Font.BOLD, 10));
                        speedBtnx4.setActionCommand(SPEEDX4_BUTTON);
                        speedBtnx4.setPreferredSize(new Dimension(60, 60));
                        speedBtnx4.addActionListener(this.controller);
                        speedBtnInnerPanel.add(speedBtnx4);
                        
                        JButton speedBtnx8 = new JButton("x8");
                        speedBtnx8.setFont(new Font("Time News Roman", Font.BOLD, 10));
                        speedBtnx8.setActionCommand(SPEEDX8_BUTTON);
                        speedBtnx8.setPreferredSize(new Dimension(60, 60));
                        speedBtnx8.addActionListener(this.controller);
                        speedBtnInnerPanel.add(speedBtnx8);
                        
                        JButton speedBtnx16 = new JButton("x16");
                        speedBtnx16.setFont(new Font("Time News Roman", Font.BOLD, 10));
                        speedBtnx16.setActionCommand(SPEEDX16_BUTTON);
                        speedBtnx16.setPreferredSize(new Dimension(60, 60));
                        speedBtnx16.addActionListener(this.controller);
                        speedBtnInnerPanel.add(speedBtnx16);
                        
                        JButton speedBtnx32 = new JButton("x32");
                        speedBtnx32.setFont(new Font("Time News Roman", Font.BOLD, 10));
                        speedBtnx32.setActionCommand(SPEEDX32_BUTTON);
                        speedBtnx32.setPreferredSize(new Dimension(60, 60));
                        speedBtnx32.addActionListener(this.controller);
                        speedBtnInnerPanel.add(speedBtnx32);
                        
                        JButton speedBtnx64 = new JButton("x64");
                        speedBtnx64.setFont(new Font("Time News Roman", Font.BOLD, 10));
                        speedBtnx64.setActionCommand(SPEEDX64_BUTTON);
                        speedBtnx64.setPreferredSize(new Dimension(60, 60));
                        speedBtnx64.addActionListener(this.controller);
                        speedBtnInnerPanel.add(speedBtnx64);
    				}
            		
    				speedBtnPanel.add(speedBtnInnerPanel);

            	}
            	

            	JPanel jumpButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            	{
            		jumpButtonPanel.setPreferredSize(new Dimension(20, 10));
            		
            		JLabel lblDays = new JLabel("Jump days");
                    textFieldDays = new JTextField(20);
                    //textFieldDays.setPreferredSize(new Dimension(40, 20));
                    lblDays.setLabelFor(textFieldDays);
                    
                    JButton jumpBtn;
                    jumpBtn = new JButton("Jump");
                    jumpBtn.setActionCommand(JUMP_BUTTON);
                    jumpBtn.setPreferredSize(new Dimension(100, 20));
                    jumpBtn.addActionListener(this.controller);
                    mainBtnPanel.add(jumpBtn);
                    
                    jumpButtonPanel.add(lblDays);
                    jumpButtonPanel.add(textFieldDays);
                    jumpButtonPanel.add(jumpBtn);
            	}
            	
            	
            	JPanel sizePanel = new JPanel(new FlowLayout());
            	{       		
            		//sizePanel.setPreferredSize(new Dimension(50, 50));
            		
	                JButton sizeButton;
	                sizeButton = new JButton("-");
	                sizeButton.setActionCommand(SIZE_BUTTON);
	                sizeButton.setPreferredSize(new Dimension(45, 45));
	                sizeButton.addActionListener(this.controller);
	                sizePanel.add(sizeButton);
	                
	                JButton sizeButton2;
	                sizeButton2 = new JButton("+");
	                sizeButton2.setActionCommand(SIZE_BUTTON2);
	                sizeButton2.setPreferredSize(new Dimension(45, 45));
	                sizeButton2.addActionListener(this.controller);
	                sizePanel.add(sizeButton2);                   
            	}
            	
            	//mainBtnPanel.setPreferredSize(new Dimension(30, 30));
            	
            	btnPanel.add(mainBtnPanel);
                btnPanel.add(speedBtnPanel);
                btnPanel.add(jumpButtonPanel);
                btnPanel.add(sizePanel);

            }
            

            
            JPanel detailsPanel = new JPanel(new FlowLayout());
            {
            	detailsPanel.setPreferredSize(new Dimension(800, 0));
             
            	JPanel referencePanel = new JPanel(new GridLayout(5, 3, 10, 10));
            	{
            		
            		JLabel actualLabel = new JLabel("Actual satellite", JLabel.CENTER);
            		JLabel referenceLabel = new JLabel("Reference satellite", JLabel.CENTER);
            		JLabel deltaLabel = new JLabel("Delta (Reference - Actual)", JLabel.CENTER);            		
            		
            		labelLatActual = new JLabel("Latitude = 0.0", JLabel.CENTER);
            		labelLonActual = new JLabel("Longitude = 0.0", JLabel.CENTER);
            		labelAltActual = new JLabel("Altitude = 0.0Km", JLabel.CENTER);
            		
            		labelLatReference = new JLabel("Latitude = 0.0", JLabel.CENTER);
            		labelLonReference = new JLabel("Longitude = 0.0", JLabel.CENTER);
            		labelAltReference = new JLabel("Altitude = 0.0 Km", JLabel.CENTER);
            		
            		labelDeltaLat = new JLabel("ΔLatitude = 0.0", JLabel.CENTER);
            		labelDeltaLon = new JLabel("ΔLongitude = 0.0", JLabel.CENTER);
            		labelDeltaAlt = new JLabel("ΔAltitude = 0.0 Km", JLabel.CENTER);
            		
            		labelDeltaPos = new JLabel("ΔPosition = 0.0 Km", JLabel.CENTER);
            		
            		referencePanel.add(actualLabel);
            		referencePanel.add(referenceLabel);
            		referencePanel.add(deltaLabel);
            		
            		referencePanel.add(labelLatReference);
            		referencePanel.add(labelLatActual);
            		referencePanel.add(labelDeltaLat);
            		
            		referencePanel.add(labelLonReference);
            		referencePanel.add(labelLonActual);
            		referencePanel.add(labelDeltaLon);
            		
            		referencePanel.add(labelAltReference);
            		referencePanel.add(labelAltActual);  
            		referencePanel.add(labelDeltaAlt);
            		
            		referencePanel.add(new JLabel());
            		referencePanel.add(labelDeltaPos);
            	}
            	
            	
                
            	detailsPanel.add(referencePanel);
                 
            }
            
			JPanel controlPanel = new JPanel(new GridLayout(2,1,5,15));
			{
				controlPanel.setBorder(new EmptyBorder(10,10,0,10));
				//controlPanel.setPreferredSize(new Dimension(800, 0));
				
				controlPanel.add(btnPanel);
				controlPanel.add(detailsPanel);
				
			}
			
			//this.pack();
			//this.setLocationRelativeTo(null);
			getContentPane().add(controlPanel, BorderLayout.WEST);
        	
        	
        }
    
        public void UpdatePosValuesActual(Angle lat, Angle lon, double alt)
        {
        	
        	this.labelLatActual.setText("Latitude = " + lat.toString());
        	this.labelLonActual.setText("Longitude = " + lon.toString());
        	this.labelAltActual.setText("Altitude = " + String.valueOf(alt) + " Km");
        }
        
        public void UpdatePosValuesReference(Angle lat, Angle lon, double alt)
        {
        	this.labelLatReference.setText("Latitude = " + lat.toString());
        	this.labelLonReference.setText("Longitude = " + lon.toString());
        	this.labelAltReference.setText("Altitude = " + String.valueOf(alt) + " Km");
        }
        
        public void UpdateDelta(Angle lat, Angle lon, double alt)
        {
        	this.labelDeltaLat.setText("ΔLatitude = " + lat.toString());
        	this.labelDeltaLon.setText("ΔLongitude = " + lon.toString());
        	this.labelDeltaAlt.setText("ΔAltitude = " + String.valueOf(alt) + " Km");
        }
        
        public void UpdatePos(double posDiff)
        {
        	this.labelDeltaPos.setText("ΔPosition = " + posDiff);
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
            else if(PAUSE_BUTTON.equalsIgnoreCase(e.getActionCommand()))
            {            	            	
            	if(((JButton)e.getSource()).getText().equals("Pause")){
            		((JButton)e.getSource()).setText("Resume");
            		this.doActionOnButton2();
                
            	}
            	else{
            		((JButton)e.getSource()).setText("Pause");
            		this.doActionOnButton3();
            	}
            }
            else if(JUMP_BUTTON.contentEquals(e.getActionCommand()))
            {
            	this.jumpAction();
            }
            else if(SIZE_BUTTON.contentEquals(e.getActionCommand()))
            {
            	this.addSize(true);
            }
            else if(SIZE_BUTTON2.contentEquals(e.getActionCommand()))
            {
            	this.addSize(false);
            }
            else if (RESTART_BUTTON.equalsIgnoreCase(e.getActionCommand()))
            {
                this.doActionOnButton5();
            } 
            else if (SPEEDX1_BUTTON.equalsIgnoreCase(e.getActionCommand()))
            {
                this.doActionOnButton4(1000);
            }  
            else if (SPEEDX2_BUTTON.equalsIgnoreCase(e.getActionCommand()))
            {
                this.doActionOnButton4(500);
            }  
            else if (SPEEDX4_BUTTON.equalsIgnoreCase(e.getActionCommand()))
            {
                this.doActionOnButton4(250);
            } 
            else if (SPEEDX8_BUTTON.equalsIgnoreCase(e.getActionCommand()))
            {
                this.doActionOnButton4(125);
            } 
            else if (SPEEDX16_BUTTON.equalsIgnoreCase(e.getActionCommand()))
            {
                this.doActionOnButton4(62);
            }
            else if (SPEEDX32_BUTTON.equalsIgnoreCase(e.getActionCommand()))
            {
                this.doActionOnButton4(31);
            } 
            else if (SPEEDX64_BUTTON.equalsIgnoreCase(e.getActionCommand()))
            {
                this.doActionOnButton4(16);
            } 
            else if(FOLLOW_BUTTON.equalsIgnoreCase(e.getActionCommand()))
            {
            	if(((JButton)e.getSource()).getText().equals("Lock")){
            		((JButton)e.getSource()).setText("Unlock");
            		this.doActionOnButton6();
                
            	}
            	else{
            		((JButton)e.getSource()).setText("Lock");
            		this.doActionOnButton6();
            	}            	
            	
            }
        }

        public void jumpAction()
        {
        	System.out.println(textFieldDays.getText());
        	satThread.jumpTo(textFieldDays.getText().toString());
        }
        
        public void addSize(boolean val)
        {
        	if(val == true)
        	{
        		satThread.addSize(-50000.0);
        	}
        	else
        	{
        		satThread.addSize(50000.0);
        	}
        }
        
        public void doActionOnButton1()
        {       
        	System.out.println("Start thread");
        	if(threadRunning == false)
        	{
        		System.out.println("Thread started");
    			new Thread(satThread).start();
    			threadRunning = true;
        	}
        }
        
        public void doActionOnButton2()
        {       
        	System.out.println("Pause thread");
        	satThread.pause();        	

        }
        
        public void doActionOnButton3()
        {       
        	System.out.println("Resume thread");
        	satThread.resume();

        }
        
        public void doActionOnButton4(int speed)
        {       
        	System.out.println("Speed " + speed);
        	satThread.setSpeed(speed);
        }
        
        public void doActionOnButton5()
        {       
        	System.out.println("Restart thread");
        	satThread.restart();

        }
        
        public void doActionOnButton6()
        {       
        	System.out.println("Follow satt");
        	satThread.setFollow();

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

  