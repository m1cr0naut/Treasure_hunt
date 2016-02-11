/* Leighton, Kyle & Plotkin, Benjamin
 * 12/8/2013
 * LeightonPlotkinBuildTables class -- builds initial data tables ("iTables")
 * for program
 *****************************************************************************/

import java.io.*;
import java.util.*;
import java.sql.*;

public class LeightonPlotkinBuildTables {
   // JDBC driver name and database URL
   static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
   static final String DB_URL = "jdbc:mysql://vrlab.ecs.csun.edu/plotkin63db";

   //  database account info
   static final String USER = "REDACTED";
   static final String PASS = "REDACTED";

   public static void main(String[] args) {
   Connection conn = null;
   Statement stmt = null;
   try{
      // register the JDBC driver
      Class.forName("com.mysql.jdbc.Driver");

      // open our connection
      System.out.println("Connecting to \"" + USER + "db\" database...");
      conn = DriverManager.getConnection(DB_URL, USER, PASS);
      System.out.println("Connected to \"" + USER + "db\" database!");

      // create our initial data tables
      System.out.println("Creating initial tables in \"" + USER + "db\"...");
      stmt = conn.createStatement();

      // drop initial tables if they exist so we can recreate
      String sql = "DROP TABLE IF EXISTS iPlayer, iMap, iCity, iTreasure";
      stmt.executeUpdate(sql);

      sql = "CREATE TABLE iMap " +
            "(place POINT PRIMARY KEY, " +
            "treasurePlace POINT NOT NULL)";
      stmt.executeUpdate(sql);

      System.out.println("Created table \"iMap\" in \"" + USER + "db\"!");

      sql = "CREATE TABLE iCity " +
            "(place POINT PRIMARY KEY, " +
            "cost SMALLINT NOT NULL)";
      stmt.executeUpdate(sql);

      System.out.println("Created table \"iCity\" in \"" + USER + "db\"!");

      sql = "CREATE TABLE iTreasure " +
            "(place POINT PRIMARY KEY, " +
            "gold SMALLINT NOT NULL)";
      stmt.executeUpdate(sql);

      System.out.println("Created table \"iTreasure\" in \"" + USER + "db\"!");

      sql = "CREATE TABLE iPlayer " +
            "(id SMALLINT PRIMARY KEY, " +
            "place POINT NOT NULL, " +
            "wealth DOUBLE NOT NULL)";
      stmt.executeUpdate(sql);

      System.out.println("Created table \"iPlayer\" in \"" + USER + "db\"!");
      System.out.println("Initial tables created in \"" + USER + "db\"!\n");

      // load iPlayer table
      System.out.println("Loading iPlayer table in \"" + USER + "db\"...");

      sql = "INSERT IGNORE INTO iPlayer " +
      "VALUES (1, POINT(20,20), 1000)";
      stmt.executeUpdate(sql);

      sql = "INSERT IGNORE INTO iPlayer " +
            "VALUES (2, POINT(500,20), 1000)";
      stmt.executeUpdate(sql);

      sql = "INSERT IGNORE INTO iPlayer " +
            "VALUES (3, POINT(20,500), 1000)";
      stmt.executeUpdate(sql);

      sql = "INSERT IGNORE INTO iPlayer " +
            "VALUES (4, POINT(500,440), 1000)";
      stmt.executeUpdate(sql);

      System.out.println("iPlayer table loaded in \"" + USER + "db\"!\n");

      // load waypoint tables
      Scanner scanIn = getTableFile();

      // parse Scanner's lines to strings; tokenize and extract data elements
      System.out.println("Loading initial waypoint data tables...\n");
      String lineIn = "";
      String[] tmp;
      // parse each datafile line
      while (scanIn.hasNextLine()) {
         // trim leading whitespace, grep out attributes into string array
         lineIn = scanIn.nextLine().trim();
         tmp = lineIn.split(" +", 24);

         // add row to relevant initial tables based on array values
         // iCity row
         if (Integer.parseInt(tmp[3]) > 0) {
            sql = "INSERT IGNORE INTO iCity " +
                   "VALUES (POINT(" +
                   Integer.parseInt(tmp[0]) +
                   "," + Integer.parseInt(tmp[1]) +
                   "), " + Integer.parseInt(tmp[3]) +
                   ")";
            stmt.executeUpdate(sql);
            System.out.println("Loaded City Row");
         }

        // iTreasure row
        if (Integer.parseInt(tmp[4]) > 0) {
            sql = "INSERT IGNORE INTO iTreasure " +
                   "VALUES (POINT(" +
                   Integer.parseInt(tmp[0]) +
                   "," + Integer.parseInt(tmp[1]) +
                   "), " + Integer.parseInt(tmp[4]) +
                   ")";
            stmt.executeUpdate(sql);
            System.out.println("Loaded Treasure Row");
         }

        // iMap row
        if (Integer.parseInt(tmp[5]) > 0) {
            sql = "INSERT IGNORE INTO iMap " +
                   "VALUES (POINT(" +
                   Integer.parseInt(tmp[0]) +
                   "," + Integer.parseInt(tmp[1]) +
                   "), " + "POINT(" +
                   Integer.parseInt(tmp[5]) +
                   "," + Integer.parseInt(tmp[6]) +
                   "))";
            stmt.executeUpdate(sql);
            System.out.println("Loaded Map Row");
         }
      }
   } catch(SQLException se){
      // SQL error handling
      se.printStackTrace();
   } catch(Exception e){
      // JDBC error handling
      e.printStackTrace();
   } finally {
      // close resources
      try {
         if(stmt!=null)
            conn.close();
      } catch(SQLException se){
      }// do nothing
      try{
         if(conn!=null)
            conn.close();
      }catch(SQLException se){
         se.printStackTrace();
      }
      }
   System.out.println("Waypoint tables loaded loaded in \"" + USER + "db\"!\n");
   }

   public static Scanner getTableFile() {
      // Create scanner to read in the waypointNeighbor.txt file, which must
      // be in the same directory as this java file.
      Scanner myFile = null;
      System.out.println("Reading file \"waypointNeighbor.txt\"...\n");
      // try-catch block throws error to console if file not found
      try {
         myFile = new Scanner(new File("waypointNeighbor.txt"));
      } catch (FileNotFoundException e) {
         System.out.println("File not found -- please make sure " +
         "\"waypointNeighbor.txt\" file is in the same directory as the file" +
         "LeightonPlotkinBuildTables.java\".\n");
         e.printStackTrace();
         System.exit(1);
      }
      return myFile;
   }
}
