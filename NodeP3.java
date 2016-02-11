/* Leighton, Kyle & Plotkin, Benjamin
 * 12/8/2013
 * NodeP3 class -- holds fields and methods to store and manipulate WayPoint
 * data, assess distance between WayPoints
 *****************************************************************************/

import SimulationFramework.*;
import java.awt.*;
import java.util.*;

public class NodeP3 implements Comparable<NodeP3>{
   // data elements of a Node
   private Point self, prev;
   private double cost;
   private double wtdCost;
 
   // default constructor
   public NodeP3(Point selfIn, Point prevIn, double costIn, 
      double wtdIn) {
      self = selfIn;
      prev = prevIn;
      cost = costIn;
      wtdCost = wtdIn;
   }
   
   // compareTo method
   public int compareTo(NodeP3 thatNode) {
      final int BEFORE = -1;
      final int EQUAL = 0;
      final int AFTER = 1;
      
      // if we're comparing a Node to itself
      if (this == thatNode) return EQUAL;

      // check costs
      if (this.wtdCost < thatNode.wtdCost) return BEFORE;
      if (this.wtdCost > thatNode.wtdCost) return AFTER;
      else return EQUAL;
   }
   
   // toString method
   public String toString() {
      return "Node Point: " + self + "| Prev Point: " + prev + "| Cost: "
         + cost + "| Weighted Cost: " + wtdCost;
   }
   
   // accessors
   public Point getSelf() {
      return self;
   }
   
   public Point getPrev() {
      return prev;
   }
   
   public double getCost() {
      return cost;
   }

   public double getWtdCost() {
      return wtdCost;
   }

   // mutators
   public void setPrev(Point inPoint) {
      prev = inPoint;
   }
   
   public void setCost(double inCost) {
      cost = inCost;
   }
   
   public void setWtdCost(double inCost) {
      wtdCost = inCost;
   }
}