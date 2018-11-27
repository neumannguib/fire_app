package br.pucrio.inf.lac.deprecated;

//Adaptado de Eric Leschinski <https://stackoverflow.com/a/7199522>

import java.util.ArrayList;
public class Runner
{
    public static double PI = 3.14159265;
    public static double TWOPI = 2*PI;
    public static ArrayList<Double> lat_array = new ArrayList<Double>();
    public static ArrayList<Double> long_array = new ArrayList<Double>();
    
    public static void main(String[] args) {
    


    ArrayList<String> polygon_lat_long_pairs = new ArrayList<String>();
    
    //lat/long of camping_area.
    polygon_lat_long_pairs.add("-8.505564,-37.351169");
    polygon_lat_long_pairs.add("-8.508228,-37.346144");
    polygon_lat_long_pairs.add("-8.514219,-37.355633");
    polygon_lat_long_pairs.add("-8.516922,-37.350403");  
    
    //Convert the strings to doubles.       
    for(String s : polygon_lat_long_pairs){
        lat_array.add(Double.parseDouble(s.split(",")[0]));
        long_array.add(Double.parseDouble(s.split(",")[1]));
    }

}
public boolean coordinate_is_inside_polygon(
    double latitude, double longitude)
{       
       int i;
       double angle=0;
       double point1_lat;
       double point1_long;
       double point2_lat;
       double point2_long;
       int n = lat_array.size();

       for (i=0;i<n;i++) {
          point1_lat = lat_array.get(i) - latitude;
          point1_long = long_array.get(i) - longitude;
          point2_lat = lat_array.get((i+1)%n) - latitude; 
          //you should have paid more attention in high school geometry.
          point2_long = long_array.get((i+1)%n) - longitude;
          angle += Angle2D(point1_lat,point1_long,point2_lat,point2_long);
       }

       if (Math.abs(angle) < PI)
          return false;
       else
          return true;
}

public static double Angle2D(double y1, double x1, double y2, double x2)
{
   double dtheta,theta1,theta2;

   theta1 = Math.atan2(y1,x1);
   theta2 = Math.atan2(y2,x2);
   dtheta = theta2 - theta1;
   while (dtheta > PI)
      dtheta -= TWOPI;
   while (dtheta < -PI)
      dtheta += TWOPI;

   return(dtheta);
}

public static boolean is_valid_gps_coordinate(double latitude, 
    double longitude)
{
    //This is a bonus function, it's unused, to reject invalid lat/longs.
    if (latitude > -90 && latitude < 90 && 
            longitude > -180 && longitude < 180)
    {
        return true;
    }
    return false;
}
}


