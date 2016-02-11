/* Leighton, Kyle & Plotkin, Benjamin
 * 12/8/2013
 * WayPointP3 class -- holds fields and methods to store and manipulate 
 * WayPoint data, assess distance between WayPoints
 *****************************************************************************/

import SimulationFramework.*;
import java.awt.*;
import java.util.*;

public class WayPointP3 {
   // data elements of a WayPoint on our map
   private int x, y, height, cost, gold, mapX, mapY, neighbors;
   private ArrayList<Point> adjPoints;
   private Marker ptMark; 
   private boolean visited;
   
   // no-arg constructor
   public WayPointP3() {
      x = y = height = cost = gold = mapX = mapY = neighbors = 0;
      adjPoints = new ArrayList<Point>();
      ptMark = new Marker(0, 0, Color.black, 2);
      visited = false;
   }
   
   // constructor with args
   public WayPointP3(int xIn, int yIn, int heightIn, int costIn, 
   int goldIn, int mapXIn, int mapYIn, int neighborsIn, ArrayList<Point>
      adjPointsIn) {
      x = xIn;
      y = yIn;
      height = heightIn;
      cost = costIn;
      gold = goldIn;
      mapX = mapXIn;
      mapY = mapYIn;
      neighbors = neighborsIn;
      adjPoints = adjPointsIn;
      visited = false;

      // set up marker values based on attributes
      if (cost > 0) // city waypoint
         ptMark = new Marker(x, y, Color.cyan, 5);
      else if (gold > 0) // gold waypoint
         ptMark = new Marker(x, y, Color.yellow, 5);
      else if ((mapX + mapY) > 0) // map waypoint
         ptMark = new Marker(x, y, Color.magenta, 5);
      else // default waypoint
         ptMark = new Marker(x, y, Color.black, 2);
   }
   
   // toString method for easy printing
   public String toString() {
      String nbPts = "";
      for(int i=0; i < adjPoints.size(); i++)
         nbPts = nbPts + adjPoints.get(i).toString() + "|";
      return "Waypoint: " + x + "|" + y + "|" + height + "|" + cost + "|" + 
      gold + "|" + mapX + "|" + mapY + "|" + neighbors + "|" + visited + "|" +
      "Waypoint Neighbors: " + nbPts;
   }

   // method to calculate 3D distance between .this WP and passed-in WP
   public double getDist3D(WayPointP3 inWP) {
      // Helper function: Takes 2 WPs, returns 3-space distance between them
      int x0,x1,y0,y1,z0,z1;
      // get our two points' components
      x0 = (int)this.getX();
      x1 = (int)inWP.getX();
      y0 = (int)this.getY();
      y1 = (int)inWP.getY();
      z0 = (int)this.getZ();
      z1 = (int)inWP.getZ();
      
      // standard 3-space distance formula
      double dist = Math.sqrt(Math.pow((x1-x0),2) 
                        + Math.pow((y1-y0),2) + Math.pow((z1-z0),2));
      return dist;
   }
   
   // accessor methods
   
   public int getX (){
      return x;
   }

   public int getY (){
      return y;
   }

   public int getZ() {
      return height;
   }
   
   public int getCost() {
      return cost;
   }
   
   /* returns WP's Gold value, sets Gold value to zero
   public int getGold() {
      int tempGold = gold;
      gold = 0;
      return tempGold;
   }*/
   
   public int getMapX() {
      return mapX;
   }

   public int getMapY() {
      return mapY;
   }
   
   public void deleteMap() {
      mapX = 0;
      mapY = 0;
   }

   public int getNeighbors() {
      return neighbors;
   }
   
   public ArrayList<Point> getAdjPts() {
      return adjPoints;
   }
   
   public Marker getMarker() {
      return ptMark;
   }
   
   public boolean getVisited() {
      return visited;
   }
   
   // getPoint function for convenience
   public Point getPoint() {
      Point thisPt = new Point(x, y);
      return thisPt;
   }

   // mutators (more to come :) )
   
   public void setVisited(boolean inBool) {
      visited = inBool;
   }
}