/* Leighton, Kyle & Plotkin, Benjamin
 * 12/8/2013
 * LeightonPlotkinP3 class -- our simulation's main class (a subclass
 * of SimFrame)
 *
 * DATABASE INFORMATION:
 * DB Name is plotkin63db
 * APPLICATION REQUIRES THAT LOADER PROGRAM 'LeightonPlotkinBuildTables.java' 
 * BE RUN FIRST (ONE-TIME EXECUTION), TO CREATE AND LOAD "iTables"
 *****************************************************************************/

import java.awt.*;
import java.awt.event.*;  
import javax.swing.*;
import java.io.*;
import java.util.*;
import java.sql.*;

import SimulationFramework.*;
// CLASSPATH = ... /282projects/SimulationFrameworkV3
// PATH = ... /282projects/SimulationFrameworkV3/SimulationFramework
// The simulated algorithm is defined in simulateAlgorithm()

public class LeightonPlotkinP3 extends SimFrame   {
	// eliminate warning @ serialVersionUID
	private static final long serialVersionUID = 42L;
   
      
   // global application variables
   private static HashMap<Point, WayPointP3> myHashMap;
   private static PriorityQueue<NodeP3> myOpenSet;
   private static HashMap<Point, NodeP3> myClosedSet;
   private Stack<NodeP3> myPath;
   private static ArrayList<Point> tempNeighbors;
   // mysql globals
   private static Connection conn = null;
   private static Statement stmt = null;
   private static String sql = null;
   // reference Points
   private static Point startPt;
   // holds our Player Bots
   private static ArrayList <PlayerP3> player;
   // our (single) Player for this simulation
   private static PlayerP3 myPlayer;
   private static WayPointP3 currWP, nextWP, endWP, tempWP;
   private static NodeP3 tempNode;
   private static boolean pathFound, pathWalked, hasMap = false;
   private static String msgStr;

   // GUI components for application's men
   /** the simulation application */
   private LeightonPlotkinP3 app;
         
   // application constants
   // magic numbers :)   
   private final int numPlayers = 4; // set test values here, final value should be 4
   //  database access info
   static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
   static final String SERVER_URL = "jdbc:mysql://vrlab.ecs.csun.edu/";
   static final String DB_NAME = "plotkin63db";
   static final String DB_URL = SERVER_URL + DB_NAME;
   static final String USER = "plotkin63";
   static final String PASS = "benjamin718";
   
   // MAIN METHOD
   public static void main(String args[]) {      
      // instantiate and launch simulation application
      LeightonPlotkinP3 app = 
      new LeightonPlotkinP3("Treasure Hunt (Project 3) by " +
      "Kyle Leighton & Benjamin Plotkin", "terrain282.png");
            
      app.start();  // start is inherited from SimFrame      
   }

   //Make the application: Create the MenuBar, "help" dialogs
   public LeightonPlotkinP3(String frameTitle, String imageFile) {
      super(frameTitle, imageFile);
      // create menus
      JMenuBar menuBar = new JMenuBar();
      // set About and Usage menu items and listeners.
      aboutMenu = new JMenu("About");
      aboutMenu.setMnemonic('A');
      aboutMenu.setToolTipText(
        "Display information about this program");
      // create a menu item and the dialog it invoke 
      usageItem = new JMenuItem("usage");
      authorItem = new JMenuItem("author");
      usageItem.addActionListener( // anonymous inner class event handler
         new ActionListener() {        
         public void actionPerformed(ActionEvent event) {
            JOptionPane.showMessageDialog(LeightonPlotkinP3.this, 
               "Simulation of a multiplayer online gaming environment. \n" +
               "Application uses a mySQL database to store shared \n" +
               "information, such as player positions, and city, \n" +
               "map and treasure values. \n\n" +
               "Simulation creates four Player bots that follow paths made \n" +
               "for them by a variant of the A* pathfinding algorithm. \n" +
               "A* creates open and closed points while pathfinding. \n" +
               "If Players encounter Map points, they will temporarily \n" +
               "abandon their journeys to find paths to the points the \n" +
               "Maps hold (ignoring any other Maps while holding Maps). \n" + 
               "Upon reaching Map points, the Players will repath to their \n" +
               "original goal points. If a Player moves onto a space \n" + 
               "occupied by one or more other Players, the newcomer will \n" +
               "contest the other Players for their wealth. \n\n"+
               "Speed of the simulation is set by the slider. \n" +
               "Player status updates are displayed in the status line.\n\n" +
               "1.  \"Stop\" button will pause the simulation. \n" +
               "2.  \"Start\" button will resume the simulation. \n" +
               "3.  \"Clear\" button will erase temporary drawables.\n",
               "Usage",   // dialog window's title
               JOptionPane.PLAIN_MESSAGE);
               }}
         );
      // create a menu item and the dialog it invokes
      authorItem.addActionListener(
         new ActionListener() {          
            public void actionPerformed(ActionEvent event) {
               JOptionPane.showMessageDialog( LeightonPlotkinP3.this, 
               "Kyle Leighton & Benjamin Plotkin \n" +
               "kyle.leighton.41@my.csun.edu \n" +
					"benjamin.plotkin.323@my.csun.edu \n" +
					"Comp 282",
               "author",  // dialog window's title
               JOptionPane.INFORMATION_MESSAGE,
					//  author's picture 
               new ImageIcon("author.png"));
               }}
         );
      // add menu items to menu 
      aboutMenu.add(usageItem);
      aboutMenu.add(authorItem);
      menuBar.add(aboutMenu);
      setJMenuBar(menuBar);
      validate();  // resize layout managers
      // construct the application specific variables
   }

   /* this method is for instantiating our player Bots and adding them
    * to the application's ArrayList and AnimatePanel's ArrayList.  */
   private void makePlayers() {
   // instantiate our four Player Bots
      PlayerP3 b = new PlayerP3(1, 20, 20, 500, 440, Color.red, 1000.0, 2000.0);
      player.add(b); 
      animatePanel.addBot(b);
      
      b = new PlayerP3(2, 500, 20, 20, 500, Color.pink, 1000.0, 2000.0);
      player.add(b); 
      animatePanel.addBot(b);
      
      b = new PlayerP3(3, 20, 500, 500, 20, Color.orange, 1000.0, 2000.0);
      player.add(b); 
      animatePanel.addBot(b);
      
      b = new PlayerP3(4, 500, 440, 20, 20, Color.blue, 1000.0, 2000.0);
      player.add(b); 
      animatePanel.addBot(b);
      
      /* TEST VALUES
      PlayerP3 b = new PlayerP3(1, 300, 20, 500, 20, Color.red, 1000.0, 2000.0);
      player.add(b); 
      animatePanel.addBot(b);
      b = new PlayerP3(2, 380, 20, 500, 20, Color.pink, 1000.0, 2000.0);
      player.add(b);
      animatePanel.addBot(b);
      b = new PlayerP3(3, 380, 20, 500, 20, Color.orange, 1000.0, 2000.0);
      player.add(b); 
      animatePanel.addBot(b);
      b = new PlayerP3(4, 380, 20, 500, 20, Color.blue, 1000.0, 2000.0);
      player.add(b); 
      animatePanel.addBot(b);    */
      
   }
   
   /* Set up our simulation model; Init global variables, load WayPoints
    * from file in same directory as extending .java files, instantiate
    * our Player Bots */

   public void setSimModel() {
      // load the waypoint.txt datafile into HashMap
      loadFile(getWayPointFile());
		// set any initial visual Markers or Connectors
      drawPoints();
      //  create Bot collection
      player = new ArrayList<PlayerP3>();
      // initialize simulation Point globals
      startPt = null;     
      // update screen with graph lines at start
      checkStateToWait();
      // load database tables from initial tables ("iName" tables)
      // LOADER PROGRAM 'LeightonPlotkinBuildTables.java' MUST BE RUN FIRST
      loadLiveTables();
      // make our four players - done after tables are loaded so that position
      // and wealth will be updated on player creation
      makePlayers();
   }

   /* Set up our initial algorithm state and while(runnable()) execute
    * pathfinding and Player moving functions */

   public synchronized void simulateAlgorithm() {
      // our main moves loop   
      while (runnable()) {
         String tmp = null;         
         // start pathfinding for each player
         for(int i=0; i < numPlayers; i++){
            myPlayer = player.get(i);
            findPath();
            // required initial trace statement
            tmp = "Player " + myPlayer.getId() + " path = (" +
               (int)myPlayer.getPlace().getX() + "," + 
               (int)myPlayer.getPlace().getY() + ") to (" + 
               (int)myPlayer.getGoal().getX() + "," + 
               (int)myPlayer.getGoal().getY() + ") wealth = " + 
               (int)myPlayer.getWealth() + " strength = " + 
               (int)myPlayer.getStrength() + "\n";
            setStatus(tmp); 
            System.out.println(tmp);        
         }
         
         // randomly select player to move first
         nextPlayer();
          
         // while at least one of our players has moves to make
         while(myPlayer.isPlaying()) {            
            // get Point to move to, WPs for calcs
            Point movePt = myPlayer.getMyPath().pop().getSelf();
            nextWP = myHashMap.get(movePt);
            
            // ensure Player has most current wealth info from DB
            myPlayer.updateWealth();
            
            // move Player
            myPlayer.move(movePt);
            
            // update Player's Stats using WP and distance args
            // function returns String for setStatus() and also
            // updates console for debugging/grading
            tmp = myPlayer.calcStats(nextWP, currWP.getDist3D(nextWP));
            
            // don't set null status
            if(tmp != null)
               setStatus(tmp);
            
            // update currWP
            currWP = nextWP;
            checkStateToWait();
            
            // if current WP has a map and Player doesn't have a treasure map
            // pick up map and repath
            if(!myPlayer.hasMap()) {
               if(myPlayer.checkTreasureMap()){
                  // required trace statement for map pickup
                  tmp = "Player " + myPlayer.getId() + " at map = (" +
                     (int)myPlayer.getPlace().getX() + "," + 
                     (int)myPlayer.getPlace().getY() + ") treasure at = (" +
                     (int)myPlayer.getMap().getX() + "," +
                     (int)myPlayer.getMap().getY() + ")\n";
                  System.out.println(tmp);
                  setStatus(tmp);
                  
                  //findPath to the treasure coordinates
                  findPath();
               }
               
            } 
            
            // if Player has reached their treasure goal
            if(myPlayer.hasMap() && myPlayer.getMap().equals(myPlayer.getPlace())){
               myPlayer.removeMap();
               // repath to original goal
               findPath();
            }
            
            //if Player has reached their final goal
            if(myPlayer.getGoal().equals(myPlayer.getPlace())){
               // move Player strength to wealth and update DB
               myPlayer.strToWel();
               // required final trace statement
               tmp ="Player " + myPlayer.getId() +
                  " is done goal = (" +(int)myPlayer.getGoal().getX() + "," + 
                  (int)myPlayer.getGoal().getY() + ") wealth = " + 
                  (int)myPlayer.getWealth() + "\n";
               setStatus(tmp);
               System.out.println(tmp);
               
               // decommission Player and their DB connection
               myPlayer.setPlaying(false);
               myPlayer.closeDB();
            }
            
            // check if any players are still playing before getting next
            for (int i = 0; i < numPlayers; i++) {
               if (player.get(i).isPlaying()) {             
                  nextPlayer();
                  break;
               }
            }
         }
         
         // clear final open/closed set temp Markers (no more paths to init)
         animatePanel.clearTemporaryDrawables();
         
      // set our app to simulation end-state
      setSimRunning(false);
      animatePanel.setComponentState(false, false, false, false, true);
         
      // The following statement must be at end of any
      // overridden abstract simulateAlgorithm() method   
      checkStateToWait();
      } 
   }

   private void initPathSets() {

      // clear out any temporary Markers
      animatePanel.clearTemporaryDrawables();
      
      // init open and closed sets of Nodes
      myOpenSet = new PriorityQueue<NodeP3>();
      myClosedSet = new HashMap<Point, NodeP3>();
      // init Stack of path Nodes
      myPath = new Stack<NodeP3>();
            
      // initial step of pathfinding
      // get start, end WPs, init temp WP 
      startPt = myPlayer.getPlace();
      currWP = myHashMap.get(startPt);
      
      // if we have a map go to treasure, else go to goal
      if (myPlayer.hasMap())
         endWP = myHashMap.get(myPlayer.getMap());
      else
         endWP = myHashMap.get(myPlayer.getGoal());

      tempWP = null;
      tempNode = null;
      
      // put starting WP Node into closed set
      tempNode = new NodeP3(startPt, null, 0.0, 0.0);
      myClosedSet.put(tempNode.getSelf(), tempNode);
                  
      // mark Node's WP as closed on map
      animatePanel.addTemporaryDrawable(new Marker(tempNode.getSelf(), 
         Color.gray, 2));
      checkStateToWait();
      
      // get starting WP's neighbor WPs
      tempNeighbors = currWP.getAdjPts();
      
      // for each starting WP neighbor, calc cost, create Node in Open Set
      for(int i = 0; i < tempNeighbors.size(); i++) {
         // get neighbor WP
         tempWP = myHashMap.get(tempNeighbors.get(i));
         // calc cost & wtdCost to neighbor: currWP to tempWP + tempWP to endWP
         double tempCost = 0.0 + currWP.getDist3D(tempWP);
         double tempWtdCost = tempCost + tempWP.getDist3D(endWP);
         // create Node
         tempNode = new NodeP3(tempNeighbors.get(i), startPt, tempCost, 
            tempWtdCost);
         // add start Node to player
         myOpenSet.add(tempNode);
         // mark Node's WP as open on map
         animatePanel.addTemporaryDrawable(new Marker(tempNode.getSelf(), 
            Color.white, 3));
      }
   }
   
   private void findPath() {
      
      System.out.println("Pathfinding for Player " + myPlayer.getId() + "...\n");
      initPathSets();
      
      // while open set is not empty
      while (myOpenSet.peek() != null) {
         // get lowest cost Node
         tempNode = myOpenSet.poll();;
         // get Node's WayPoint
         currWP = myHashMap.get(tempNode.getSelf());
         
         // check if Node is our goal
         if(currWP == endWP) {
            pathFound = true;
            break;
         }
         // put lowest cost Node into closed set
         myClosedSet.put(tempNode.getSelf(), tempNode);            
         // mark Node's WP as closed on map
         animatePanel.addTemporaryDrawable(new Marker(tempNode.getSelf(), 
            Color.gray, 2));
            
         checkStateToWait(); //comment out to hide pathfinding draws
         
         // get WayPoint's neighbors
         tempNeighbors = currWP.getAdjPts();
         for(int i = 0; i < tempNeighbors.size(); i++) {
            // get neighbor WP
            tempWP = myHashMap.get(tempNeighbors.get(i));
            // check if in open or closed set
            if(!myClosedSet.containsKey(tempNeighbors.get(i)) && 
               !openSetContains(myOpenSet, tempNeighbors.get(i))) {
               // calc cost & wtdCost to neighbor: 
               // currWP to tempWP + tempWP to endWP
               double tempCost = tempNode.getCost() + currWP.getDist3D(tempWP);
               double tempWtdCost = tempCost + tempWP.getDist3D(endWP); 
               // create Node
               tempNode = new NodeP3(tempNeighbors.get(i), currWP.getPoint(), 
                  tempCost, tempWtdCost);
               // add Node to open set
               myOpenSet.add(tempNode);
               // mark Node's WP as open on map
               animatePanel.addTemporaryDrawable(new Marker(tempNode.getSelf(),
                  Color.white, 3));                  
            }
         }
      }
      
      // build path Stack of moves
      if(pathFound) {
         NodeP3 stackNode = tempNode;
         while(stackNode.getPrev() != null) {
            myPath.push(stackNode);
            stackNode = myClosedSet.get(stackNode.getPrev());
         }
         // pass path to player
         myPlayer.setMyPath(myPath);
      }
      animatePanel.clearTemporaryDrawables();
   }
   
   private boolean openSetContains(PriorityQueue<NodeP3> openSet, Point p){
      Iterator<NodeP3> iter = openSet.iterator();
      while(iter.hasNext()){
         if (p == iter.next().getSelf())
            return true;
      }
      
      return false;
   }
   
   public static Scanner getWayPointFile() {
      /* Create scanner to read in the waypoint.txt file, which must
       * be in the same directory as this java file. */
      Scanner myFile = null;
      System.out.println("Attempting to read file \"waypointNeighbor.txt\"...\n");
      // try-catch block throws error to console if file not found
      try {
         myFile = new Scanner(new File("waypointNeighbor.txt"));
      } catch (FileNotFoundException e) {
         System.out.println("File not found -- please make sure " +
         "\"waypointNeighbor.txt\" file is in the same directory as the file" +
         "LeightonPlotkinP3.java\".\n");
         e.printStackTrace();
         System.exit(1);
      }
      return myFile;
   }

   public static void loadFile(Scanner scanIn) {
      /* parse Scanner's lines to strings; tokenize and extract data elements
       * into integer values -- each datafile line becomes a new WayPoint */
      System.out.println("Attempting to load WayPoint data...\n");
      String lineIn = "";
      String[] tmp;
      
      int neighborCount = 0;
      Point tmpPoint = null;
      ArrayList<Point> tmp2 = null;
      
      // instantiate HashMap for WayPoints
      myHashMap = new HashMap<Point, WayPointP3>();
                  
      // parse each datafile line into a WayPoint
      while (scanIn.hasNextLine()) {
         // trim leading whitespace, grep out attributes into string array
         lineIn = scanIn.nextLine().trim();
         tmp = lineIn.split(" +", 24);
         
         // check neighbors attribute to determine how many neighbors to store
         neighborCount = Integer.parseInt(tmp[7]);
         
         // if we have neighbors, create temp ArrayList containing their Points
         if (neighborCount > 0) {
            tmp2 = new ArrayList<Point>();
            for (int i=8; i<((neighborCount*2)+7); i+=2) {
               tmpPoint = new Point(Integer.parseInt(tmp[i]), 
                  Integer.parseInt(tmp[i+1]));
               tmp2.add(tmpPoint);
            }
         }
         
         // instantiate WayPoint using array values
         WayPointP3 myWayPoint = 
         new WayPointP3(Integer.parseInt(tmp[0]), 
         Integer.parseInt(tmp[1]), Integer.parseInt(tmp[2]),
         Integer.parseInt(tmp[3]), Integer.parseInt(tmp[4]),
         Integer.parseInt(tmp[5]), Integer.parseInt(tmp[6]), 
         Integer.parseInt(tmp[7]), tmp2);         
         
         // instantiate Point key using WayPoint's coordinates
         Point keyPt = new Point(myWayPoint.getX(), myWayPoint.getY());
         // put WayPoint into HashMap using Point as key
         myHashMap.put(keyPt, myWayPoint);
      }
      
      System.out.println("Loaded WayPoint data!\n");
   }

   private void drawPoints() {
      // Helper function: iterate through HashMap, draw permanent points/edges
      Iterator iter = myHashMap.keySet().iterator();
      while(iter.hasNext()) {
         // get WP
         Point myKey = (Point)iter.next();
         WayPointP3 myWP = myHashMap.get(myKey);
         // draw WP
         animatePanel.addPermanentDrawable(new Marker(myWP.getMarker()));
         // if WP has neighbors, draw edges
         int nbrCt = myWP.getNeighbors();
         if(nbrCt > 0) {
            ArrayList<Point> edgePts = myWP.getAdjPts();
            Iterator iter2 = edgePts.iterator();
            while(iter2.hasNext()) {
               Point endEdge = (Point)iter2.next();
               animatePanel.addPermanentDrawable(new Connector(myKey, endEdge, 
               Color.black));
            }
         }
      }
   }
   
   //picks a random player to go next
   private void nextPlayer() {
      
      Random r = new Random();
      
      // randomly select the next player -- use do/while loop
      // to ensure randomly selected player is still playing
      do {
         myPlayer = player.get(r.nextInt(numPlayers));
      } while (!myPlayer.isPlaying());
         
      //update currentWP to new current player's position
      currWP = myHashMap.get(myPlayer.getPlace());  
          
      //update myPath
      myPath = myPlayer.getMyPath();
   }

   private void loadLiveTables() {
      setStatus("Creating live database tables...");
      System.out.println("Attempting to populate database tables...\n");
      try{
         // register the JDBC driver
         Class.forName("com.mysql.jdbc.Driver");
   
         // open our connection
         System.out.println("Connecting to \"" + DB_NAME + "\" database...\n");
         conn = DriverManager.getConnection(DB_URL, USER, PASS);
         System.out.println("Connected to \"" + DB_NAME + "\"!\n");
         
         // create/recreate our working tables using iTables
         System.out.println("Creating database tables in \"" + DB_NAME + "\"...\n");
                  
         stmt = conn.createStatement();
         
         // drop live tables if they exist so we can recreate
         sql = "DROP TABLE IF EXISTS Player, Map, City, Treasure"; 
         stmt.executeUpdate(sql);
         
         // create/load live tables
         sql = "CREATE TABLE Player LIKE iPlayer";
         stmt.executeUpdate(sql);         
         sql = "INSERT Player SELECT * FROM iPlayer";
         stmt.executeUpdate(sql);         
         System.out.println("Created table \"Player\" in \"" + DB_NAME + "\"!\n");
         sql = "CREATE TABLE Map LIKE iMap";
         stmt.executeUpdate(sql);
         sql = "INSERT Map SELECT * FROM iMap";
         stmt.executeUpdate(sql);
         System.out.println("Created table \"Map\" in \"" + DB_NAME + "\"!\n"); 
         sql = "CREATE TABLE City LIKE iCity";
         stmt.executeUpdate(sql);
         sql = "INSERT City SELECT * FROM iCity";
         stmt.executeUpdate(sql);         
         System.out.println("Created table \"City\" in \"" + DB_NAME + "\"!\n");
         sql = "CREATE TABLE Treasure LIKE iTreasure";
         stmt.executeUpdate(sql);
         sql = "INSERT Treasure SELECT * FROM iTreasure";
         stmt.executeUpdate(sql);   
         System.out.println("Created table \"Treasure\" in \"" + DB_NAME + "\"!\n");
               
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
      System.out.println("Database tables populated!\n");
   }
   
}