package br.pucrio.inf.lac;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import bin.com.esri.shp.*;
import com.esri.core.geometry.*;

public class shape_collection {

	final File file = new File("testpolygon.shp");
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
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
 }
	
public VertexDescription area(double latitude, double longitude) {
	final Point ponto = new Point();
	ponto.setX(latitude);
	ponto.setY(longitude);
	for (int i=0;i<coord.size();i++) {
	
		if(coord.get(i).contains(ponto))
			return coord.get(i).getDescription();
	}
	return null;	
}
}

