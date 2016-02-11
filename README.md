# Treasure Hunt
Java-based path finding simulation which utilizes a MySQL database backend; the final phase of a three-phased project for my advanced data structures course, it was developed as a team with classmate Kyle Leighton, with each of us contributing equally to design, development and testing, with the majority of database functionality being implemented by me (Benjamin).

The general application design consists of the following Java classes:

* `LeightonPlotkinBuildTables.java`:
A standalone program used to create “initial” database tables (“iTables”), which are not accessed by the simulation Players, but only used to reload the “live” database tables (which the Players do read to and write from).  It must be run once before the simulation 
application, to create the initial tables and populate them with data (which is programmatically loaded from the “waypointNeighbor.txt” 
file).  The iTables are named “iMap,” “iCity,” “iTreasure” and “iPlayer,” to distinguish them from the “live” tables.  Note that we use 
the MySQL POINT spatial data type for the “place” key value in all tables, as it is very convenient for our existing approach with 
Waypoints and Points.

* `LeightonPlotkinP3.java`:
This is our main class; it extends SimFrame and holds our main method.  In this class we instantiate a HashMap of our terrain and populate it with WayPoints (using Point values for the keys).  At the start of each simulation, we DROP all live database tables (if extant) and CREATE them as copies of the “iTables” (initial tables) using the LIKE operator, and load the iTable data into the live tables using commands of the form `INSERT <table> SELECT * FROM <iTable>`.  We retained the implementation of our A* path finding algorithm, which uses a PriorityQueue of Nodes for the Open Set (least cost Node at the head of the queue) and a HashMap of Nodes for the Closed Set, and which builds a Stack of Nodes for each Player’s path by traversing back through the Closed Set of Nodes, from the goal Node to the start Node, using each Node’s predecessor Point as a key to retrieve the previous Node.  However, all Treasure, Map and City info, as well as competing Player info, is pulled from the database via MySQL queries (see PlayerP3 section for details).

* `PlayerP3.java`:
This class represents our game’s Players; it extends Bot (an instructor-provided class).  Our Players have id, place, goal, strength, 
wealth, path and isPlaying attributes, as well as a method to move the Player on the map, and methods to query and update the database 
(for Map, Treasure, City and competing Player data), and to update Player statistics and return a status String to the calling method for application display and console log updates.  Each Player opens its own dedicated database connection, which is closed after the Player 
reaches its goal.

* `WayPointP3.java`:
This class represents our terrain WayPoints; each instance of this class represents a point on the terrain map, and holds attributes for 
said point, including three-space coordinate position values, city cost (if any), gold value (if any), treasure map coordinates (if any),
Marker attribute values, and an ArrayList of adjacent (neighbor) points for graph drawing and path finding.  It has a distance-finding 
method to calculate the three-space distance between itself and a passed-in WayPoint.  Note that we only use the City, Map and Gold data 
for display purposes; our program does not use this data for Player logic.

* `NodeP3.java`:
This class represents a graph node; each instance of this class represents a candidate point for path finding on the graph of adjacent 
points on our map.  Each Node has two Point attributes – one for its own position and one for its predecessor’s position.  The predecessor Point acts like a pointer to the Node’s predecessor on the graph of the map.  Each Node also has cost and weighted cost attributes for path finding.  This class implements the Comparable interface via a compareTo() method, which is predicated on the weighted cost attribute value, to ensure that the lowest-cost Node is always at the head of the Closed Set’s PriorityQueue.

In addition to the JDBC API and a MySQL database built to the project specifications, we used the following Java Collections Framework 
classes and interface to implement this final project phase:
`ArrayList`, `Comparable`, `HashMap`, `Iterator`, `PriorityQueue`, `Stack`.
