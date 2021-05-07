package WWDisplay.WorldWindDisplay;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.util.FastMath;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

public class DataParser {
		
	GeodeticPoint positions[];
	double posX;
	double posY;
	double posZ;
	Frame itrf;
	AbsoluteDate date;
	
	public DataParser()
	{
		positions = new GeodeticPoint[4500];
		date = new AbsoluteDate("2013-07-23T03:03:05.970", TimeScalesFactory.getUTC());
	}

	public GeodeticPoint[] ParseFile(final File dataFile) {
		
		try (final BufferedReader br = new BufferedReader(new FileReader(dataFile))) {
			
			String line;
			int positionInVector = 0;

			// Read the entire file
			while ((line = br.readLine()) != null) {
				line = br.readLine();

				line = line.trim();
				if (line.startsWith("#")) {
					// skip comments
					continue;
				}

				final StringTokenizer st = new StringTokenizer(line);
				//System.out.println(st.countTokens());
				
				 for (int cnt = 0; cnt < 12; cnt++) { 
					 String garbageToken = st.nextToken();
				 }
				 
				 posX = Double.parseDouble(st.nextToken()) * 1000;
				 posY = Double.parseDouble(st.nextToken()) * 1000;
				 posZ = Double.parseDouble(st.nextToken()) * 1000;
				 
			    // get the ITRF frame
			    itrf = FramesFactory.getITRF(IERSConventions.IERS_2010, false);
			    // create the Earth
		        OneAxisEllipsoid earth = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
		                Constants.WGS84_EARTH_FLATTENING, itrf);
		        
		        Frame frame = FramesFactory.getEME2000();
		        Vector3D position = new Vector3D(posX, posY, posZ);
		        date = date.shiftedBy(6.0000000000000000e+02);
		        GeodeticPoint geoPoint = earth.transform(position, frame, date);
		        
		        positions[positionInVector] = geoPoint;
		        positionInVector++;
				 
				 
			}

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		
		return positions;
	}

}
