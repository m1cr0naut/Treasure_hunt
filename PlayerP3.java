/* Leighton, Kyle & Plotkin, Benjamin
 * 12/8/2013
 * PlayerP3 class: Extends Bot, implements move() function for our Player Bot
 * Adds variables to store Player's move count, strength and wealth
 *****************************************************************************/

import java.awt.*;
import java.util.*;
import java.sql.*;
import SimulationFramework.*;

public class PlayerP3 extends Bot {
   
   // database access constants
   static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
   static final String DB_URL = "jdbc:mysql://vrlab.ecs.csun.edu/plotkin63db";
   static final String USER = "plotkin63";
   static final String PASS = "benjamin718";
   
   // mysql globals
   private Connection conn;
   private Statement stmt;
   private ResultSet res;
   
   // Player attributes
   private double strength, wealth;
   private int moves;
   private Stack<NodeP3> myPath = new Stack<NodeP3>();
   private Point treasure, place, goal;
   private boolean isPlaying = true, hasMap = false;
   private int id;
   private boolean plundered = false;

   // Player constructor
   public PlayerP3 (int playerID, int startX, int startY, int goalX, int goalY,
         Color colorValue, double strengthIn, double wealthIn) {
		super("" + playerID, startX, startY, colorValue);
      id = playerID;
      strength = strengthIn;
      wealth = wealthIn;
      moves = 0;
      place = new Point(startX, startY);
      goal = new Point(goalX, goalY);
      
      // open Player's database connection
      try{
         conn = DriverManager.getConnection(DB_URL, USER, PASS);
      }
      catch (Exception e) {
         System.out.println("Error:  " + e);
      }
      
      updatePlayer();
   }

   // Clear temporary drawables
	public void reset() {
		super.reset();
   }

   // our move function which calls move() with a Point argument
   public void move (Point inPoint) {
      place = inPoint;
      move();
   }

   // here we define parent class Bot's abstract move() method
	public void move() {
		moveTo(place);
      moves+=1; // increment move count
      
      //check for other players for contest
      checkContest();
   }
   
   // getter for Bot path ArrayList
   public ArrayList <Point> getPath() {
      return path;
   }
   
   // getter for Player strength
   public double getStrength() {
      return strength;
   }

   // setter for Player strength
   public void setStrength(double inStrength) {
      strength = inStrength;
   }
   
   // getter for Player id
   public String getId() {
      return "" + id;
   }

   // getter for Player wealth
   public double getWealth() {
      return wealth;
   }

   // setter for Player wealth
   public void setWealth(double inWealth) {
      wealth = inWealth;
   }
   
   // getter for Player moves
   public int getMoves() {
      return moves;
   } 
   
   // setter for Player moves
   public void setMoves(int movesIn) {
      moves = movesIn;
   }

   public boolean hasMap(){
      return hasMap;
   }
   
   public void setMap(Point p){
      hasMap = true;
      treasure = p;
   }
   
   public Point getMap(){
      return treasure;
   }
   
   public void removeMap(){
      hasMap = false;
      treasure = null;
   }
   
   public void setGoal(Point p){
      goal = p;
   }
   
   public void setPlace(Point p){
      place = p;
   }
   
   public Point getPlace(){
      return place;
   }
   
   public Point getGoal(){
      return goal;
   }
   
   public void setMyPath(Stack<NodeP3> thisPath){
      myPath = thisPath;
   }
   
   public Stack<NodeP3> getMyPath(){
      return myPath;
   }   
   
   public void setPlaying(boolean bool){
      isPlaying = bool;
   }
   
   public boolean isPlaying(){
      return isPlaying;
   }
   
   // method to update Player Stats based on dest WP and distance -
   // returns a String for system status updates and pushes that
   // String to console out for debugging/grading
   public String calcStats(WayPointP3 WP, double distance) {
      String statStr = "";
      strength = strength - distance;
            
      // create String "POINT" for SQL queries 
      String dbPoint = "POINT(" + WP.getX() + "," + WP.getY() + ")";

      // query DB - returns array of ints: [ cost | gold ]
      // also zeros out gold
      int[] info = checkDB(dbPoint);
            
      // temp vars from array for subsequent calcs
      int tempGold = info[1];
      int tempCost = info[0];

      // get Player's wealth from DB

      // calc Player's wealth locally
      wealth = wealth + tempGold;
      wealth = wealth - tempCost;
      // UPDATE Player's wealth in DB
      updatePlayer();
      
      // check if we have paid the full toll for city cost
      if(tempCost > 0 && wealth >= 0) // we had enough!
         strength = strength + tempCost;      

      // if City WayPoint
      if(tempCost > 0) {
         // required trace statement
         statStr = "Player " + this.id + " at city = (" +
            WP.getX() + "," + WP.getY() + ") cost = " +
            tempCost + " wealth = " + (int)this.wealth +
            " strength = " + (int)this.strength;
      }
      
      // if Treasure WayPoint
      else if(tempGold > 0 || plundered) {
         // required trace statement
         statStr = "Player " + this.id + " at treasure = (" +
            WP.getX() + "," + WP.getY() + ") wealth = " +
            (int)this.wealth + " gold = " + tempGold;
      }
      
      // if default WayPoint (aka "Place")  
      else statStr = null;
      
      // don't print if null
      if(statStr != null)
         System.out.println(statStr + "\n");
      return statStr;    
   }
   
   public void strToWel() {
      // move Player's strength points to wealth and update DB
      wealth = (int)(wealth + strength);
      strength = 0;
      updatePlayer();
   }
   
   public boolean checkTreasureMap(){
   // check for TreasureMap; return true if there is one and
   // update player's treasure Point and hasMap boolean, remove Map record
      
      // create String "POINT" for SQL queries 
      String myPlace = "POINT(" + place.getX() + "," + place.getY() + ")";
      int mapX, mapY;
      
      try {
         // set up SELECT query
         stmt = conn.createStatement();
         res = stmt.executeQuery(
            "SELECT X(treasurePlace), Y(treasurePlace) FROM Map WHERE place = " + myPlace);
         // process results
         if (res.next()) {
            mapX = res.getInt("X(treasurePlace)");
            mapY = res.getInt("Y(treasurePlace)");
            // delete map row from DB
            stmt.executeUpdate(
               "DELETE FROM Map WHERE place = " + myPlace);
            //set player to have map
            hasMap = true;
            treasure = new Point(mapX, mapY);
            return true;
         }

      }
      catch (Exception e) {
         System.out.println("Error:  " + e);
      }
      return false;
   }
   
   public void updateWealth() {
   // make sure Player's wealth attribute has the most current DB info
   // (in event it was modified by another Player in a contest)
      try {
         stmt = conn.createStatement();
         res = stmt.executeQuery(
            "SELECT wealth FROM Player WHERE id = " + this.id);
         // process results
         if (res.next())
            wealth = res.getDouble("wealth");
      }
      catch (Exception e) {
         System.out.println("Error:  " + e);
      }
   }
   
   public void checkContest() {
   // check for Player contests and resolve them
      // if we have negative wealth, don't need to check
      if(this.wealth < 0){
         return;
      }
      
      // create String "POINT" for SQL queries 
      String myPlace = "POINT(" + place.getX() + "," + place.getY() + ")";
      
      //variables to hold potential opponent's ID and wealth
      int tempID;
      double tempWealth;
      
      try {
         // set up SELECT query for any other Players in the same place
         stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                   ResultSet.CONCUR_UPDATABLE);
         res = stmt.executeQuery(
               "SELECT id, wealth FROM Player WHERE place = " + myPlace
                + "AND id <> " + this.id);
               
         // iterate through any opponents found
         while(res.next()){
            tempID = res.getInt("id");
            tempWealth = res.getDouble("wealth");

            // check both Players have positive wealth
            if(tempWealth > 0 && this.wealth > 0){
               /* test trace
               System.out.println("Contest initiated by Player " 
                  + this.id + " at place " + place.getX() + "," + place.getY() + "...");
               System.out.println("Player " + this.id + " has wealth: " 
                  + (int)this.wealth + ", Player " + tempID + " has wealth: "
                  + (int)tempWealth); */

               // if opponent has more wealth
               if(tempWealth > this.wealth){
                  tempWealth = tempWealth + (int)(this.wealth/3);
                  this.wealth = this.wealth - (int)(this.wealth/3);
                  System.out.println("Contest! Player " + tempID +
                     " with " + (int)tempWealth + " wins against Player " +
                     id + " with " + (int)wealth + "\n");
               }
               // if we have more wealth
               else if(this.wealth > tempWealth){
                  this.wealth = this.wealth + (int)(tempWealth/3);
                  tempWealth = tempWealth - (int)(tempWealth/3);
                  System.out.println("Contest! Player " + id +
                     " with " + (int)wealth + " wins against Player " +
                     tempID + " with " + (int)tempWealth + "\n");
               }
               // if there's a tie
               else
                  System.out.println("Contest! Player " + id +
                     " with " + (int)wealth + " ties against Player " +
                     tempID + " with " + (int)tempWealth + "\n");
               
               // update Player wealth entry for other Player
               res.updateDouble("wealth", tempWealth);
               res.updateRow();
            }
         } //end while
      } //end try
      catch (Exception e) {
         System.out.println("Error:  " + e);
      }
      return;
   }
      
   private int[] checkDB(String qPoint) {
      // updates Player place in DB
      // checks DB tables for City and Treasure entries
      // zeros out gold values
      
      // array to hold DB values:
      // cost | gold
      int[] tempVals = new int[]{0, 0};
      
      // reset plundered variable
      plundered = false;
   
      try {
         // set up SELECT query
         stmt = conn.createStatement();
         // update Player place entry
         stmt.executeUpdate(
               "UPDATE Player SET place = " + qPoint + "WHERE id = " + this.id);
         // City query
         res = stmt.executeQuery(
            "SELECT cost FROM City WHERE place = " + qPoint);
         // process results
         if (res.next())
            tempVals[0] = res.getInt("cost");
         // Treasure query
         res = stmt.executeQuery(
            "SELECT gold FROM Treasure WHERE place = " + qPoint);
         // process results
         if (res.next()) {
            tempVals[1] = res.getInt("gold");
            // check if plundered
            if(tempVals[1] == 0)
               plundered = true;
            else { // zero out DB gold
               stmt.executeUpdate(
                  "UPDATE Treasure SET gold = 0 WHERE place = " + qPoint);
            }
         }

      }
      catch (Exception e) {
         System.out.println("Error:  " + e);
      }
   return tempVals; 
   }

   private void updatePlayer() {
   // writes Player's wealth and place to database
   // create String "POINT" for SQL queries 
      String myPlace = "POINT(" + place.getX() + "," + place.getY() + ")";
   
   try {
      // set up UPDATE statement
      stmt = conn.createStatement();
      // Player query
      stmt.executeUpdate(
         "UPDATE Player SET wealth = " + wealth +" WHERE id = " + this.id);
      stmt.executeUpdate(
         "UPDATE Player SET place = " + myPlace +" WHERE id = " + this.id);
   }
   catch (Exception e) {
      System.out.println("Error:  " + e);
   }
   }
   
   public void closeDB() {
   // close Player's DB resources
      try {
         if(stmt!=null)
            conn.close();
      } catch(SQLException se){
      } // do nothing
      try{
         if(conn!=null)
            conn.close();
      }catch(SQLException se){
         se.printStackTrace();
      }
   }
}