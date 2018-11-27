package br.pucrio.inf.lac;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import com.esri.shp.*;
import com.esri.core.geometry.*;

/** Shape_collection is a class that reads a shape file (park area), converts it to coordinates and then is used to 
 * analyze whether a fire event occurs inside the park.
 * @author Guilherme Neumann 
 */

public class shape_collection {

	final File file = new File("tests/metrople_rio.shp");
	ArrayList<Envelope> coord = new ArrayList<Envelope>();{
	try(final FileInputStream fileInputStream = new FileInputStream(file);)
	{
	    final Envelope envelope = new Envelope();
	    final Polygon polygon = new Polygon();
	    final ShpReader shpReader = new ShpReader(new DataInputStream(new BufferedInputStream(fileInputStream)));
	    while (shpReader.hasMore())
	    {
	        shpReader.queryPolygon(polygon);
	        polygon.queryEnvelope(envelope);
	        coord.add(envelope);
	    }
	    fileInputStream.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
 }
	
public Point area(double latitude, double longitude) {
	Point ponto = new Point();
	if(is_valid_gps_coordinate(latitude,longitude)) {
	ponto.setX(longitude);
	ponto.setY(latitude);

	for (int i=0;i<coord.size();i++) {

		if(coord.get(i).contains(ponto)) {
			return ponto;
		}
		
	}
	}
	ponto.setEmpty();	
	return ponto;
}

public static boolean is_valid_gps_coordinate(double latitude, 
	    double longitude)
	{
	    if (latitude > -90 && latitude < 90 && 
	            longitude > -180 && longitude < 180)
	    {
	        return true;
	    }
	    return false;
	}
}

