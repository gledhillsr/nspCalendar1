package org.nsponline.calendar.misc;

import org.nsponline.calendar.store.*;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Main method to track general info about a resort (patrol)  Not backed by a table.  But stores
 * general display info about all patrols
 * @author Steve Gledhill
 */
@SuppressWarnings({"SqlNoDataSourceInspection", "AccessStaticViaInstance", "SqlDialectInspection"})
public class PatrolData {
  private final static boolean DEBUG = false;

/* ------------ DEFINE ADDRESS OF MYSQL (for Amazon instances, user PRIVATE address --------- */
private static String MYSQL_ADDRESS = "ip-172-31-53-48.ec2.internal";  //private ip PRODUCTION.  must match /etc/my.cnf

  public final static Boolean USING_TESTING_ADDRESS = false;   //used in MonthlyCalendar will add a "TESTING" to the calendar page

  static {
    //noinspection ConstantConditions
    if (USING_TESTING_ADDRESS) {
      MYSQL_ADDRESS = "ip-172-31-62-143.ec2.internal";  //private ip TESTING.  must match /etc/my.cnf
    }
  }

//  private final static String MYSQL_ADDRESS = "127.0.0.1";  //local laptop.  must match /etc/my.cnf
/* ------------------------------------------------------------------------------------------ */

  // ***** start back door login stuff (works with ANY resort, and does NOT send any email confirmations)*****
  private final static String backDoorFakeFirstName = "System";
  private final static String backDoorFakeLastName = "Administrator";
  private final static String backDoorEmail = "Steve@Gledhills.com";

  static public HashMap<String, ResortData> resortMap = new HashMap<String, ResortData>();
  static private final int IMG_HEIGHT = 80;
  static {
    resortMap.put("Afton",          new ResortData("Afton", "Afton Alps", null, "http://www.aftonalpsskipatrol.org", "/images/AftonLogo.jpg", IMG_HEIGHT, 90));
    resortMap.put("AlpineMt",       new ResortData("AlpineMt", "Alpine Mt", null, "http://www.alpinemtskipatrol.org", "/images/AlpineMt.jpg", IMG_HEIGHT, 80));
    resortMap.put("Andes",          new ResortData("Andes", "Andes Tower Hills", null, "http://www.andestowerhills.com", "/images/andes_logo.jpg", IMG_HEIGHT, 80));
    resortMap.put("BigHorn",        new ResortData("BigHorn", "Big Horn", "BigHornSkiPatrolDirector@gmail.com", "https://www.lodgesofthebighorns.com/", "/images/NSP_logo.gif", IMG_HEIGHT, 80));
    resortMap.put("Brighton",       new ResortData("Brighton", "Brighton", "brightonskipatrol@gmail.com", "http://www.brightonresort.com", "/images/Brighton.gif", 60, 261));
    resortMap.put("BuenaVista",     new ResortData("BuenaVista", "Buena Vista", null, "http://www.bvskiarea.com", "/images/BuenaVista.gif", 75, 300));
    resortMap.put("CoffeeMill",     new ResortData("CoffeeMill", "CoffeeMill", null, "https://cm-skipatrol.org/", "/images/CoffeeMillLogo.png", IMG_HEIGHT, 87));
    resortMap.put("DetroitMountain",new ResortData("DetroitMountain", "Detroit Mountain", null, "http://detroitmountain.com/", "/images/DetroitMountain.png", 73, 121));
    resortMap.put("DevilsHead",     new ResortData("DevilsHead", "Devil's Head", "tim.theisen@ieee.org", "https://www.devilsheadresort.com/", "/images/DevilsHeadSkiPatrol.png", IMG_HEIGHT, 160));
    resortMap.put("ElmCreek",       new ResortData("ElmCreek", "Elm Creek Park", null, "https://www.threeriversparks.org/parks/elm-creek-park.aspx", "/images/ThreeRivers.jpg", IMG_HEIGHT, 80));
    resortMap.put("GrandTarghee",   new ResortData("GrandTarghee", "Grand Targhee", null, "http://www.GrandTarghee.com", "/images/GrandTarghee.jpg", IMG_HEIGHT, 80));
    resortMap.put("GreatDivide",    new ResortData("GreatDivide", "Great Divide", null, "http://www.skigd.com", "/images/GreatDivide.jpg", IMG_HEIGHT, 274));
    resortMap.put("HermonMountain", new ResortData("HermonMountain", "Hermon Mountain", null, "http://www.skihermonmountain.com", "/images/HermonMountain.jpg", IMG_HEIGHT, 80));
    resortMap.put("Hesperus",       new ResortData("Hesperus", "Hesperus", null, "http://www.ski-hesperus.com/", "/images/Hesperus.jpg", 84, 192));
    resortMap.put("HiddenValley",   new ResortData("HiddenValley", "Hidden Valley", null, "http://www.hvsp.org/", "/images/HiddenValley.jpg", IMG_HEIGHT, 80));
    resortMap.put("HylandHills",    new ResortData("HylandHills", "Hyland Hills Park", null, " https://threeriversparks.org/parks/hyland-lake-park/hyland-hills-ski-area.aspx", "/images/ThreeRivers.jpg", IMG_HEIGHT, 80));
    resortMap.put("IFNordic",       new ResortData("IFNordic", "IF Nordic", null, "", "/images/IFNordic.gif", IMG_HEIGHT, 80));
// EMS   resortMap.put("JacksonHole",    new ResortData("JacksonHole", "Jackson Hole Fire/EMS", null, "http://tetonwyo.org/AgencyHome.asp?dept_id=fire", "/images/JacksonHole.jpg", IMG_HEIGHT, 80));
// EMS   resortMap.put("JacksonSpecialEvents", new ResortData("JacksonSpecialEvents", "Jackson Hole Fire/EMS Special Events", null, "http://tetonwyo.org/AgencyHome.asp?dept_id=fire", "/images/JacksonHole.jpg", IMG_HEIGHT, 80));
    resortMap.put("KellyCanyon",    new ResortData("KellyCanyon", "Kelly Canyon", null, "http://www.SkiKelly.com", "/images/KellyCanyon.jpg", IMG_HEIGHT, 80));
    resortMap.put("LonesomePine",   new ResortData("LonesomePine", "Lonesome Pine Trails", null, "http://www.lonesomepines.org", "/images/lonesomepines.gif", IMG_HEIGHT, 80));
    resortMap.put("LeeCanyon",      new ResortData("LeeCanyon", "Lee Canyon", "schedule@leecanyonskipatrol.org", "http://www.leecanyonskipatrol.org", "/images/LeeCanyon.png", IMG_HEIGHT, 80));
    resortMap.put("MagicMountain",  new ResortData("MagicMountain", "Magic Mountain", null, "http://www.magicmountainresort.com/", "/images/MagicMountain.jpg", IMG_HEIGHT, 145));
    resortMap.put("MountKato",      new ResortData("MountKato", "Mount Kato", null, "http://www.mtkatoskipatrol.com", "/images/MountKato.jpg", IMG_HEIGHT, 80));
    resortMap.put("MountPleasant",  new ResortData("MountPleasant", "Mount Pleasant", "dfarbotnik@gmail.com", "http://www.skimountpleasant.com ", "/images/MountPleasant.png", IMG_HEIGHT, 80));
    resortMap.put("NorwayMountain", new ResortData("NorwayMountain", "Norway Mountain", null, "http://www.NorwayMountain.com", "/images/NorwayMountain.jpg", IMG_HEIGHT, 80));
    resortMap.put("PaidSnowCreek",  new ResortData("PaidSnowCreek", "Paid SnowCreek", null, "http://www.skisnowcreek.com", "/images/SnowCreek.jpg", IMG_HEIGHT, 80));
    resortMap.put("ParkCity",       new ResortData("ParkCity", "PCM-Canyons", "dukespeer@gmail.com", "http://www.parkcitymountain.com", "/images/ParkCity.png", 30, 30));
    resortMap.put("PebbleCreek",    new ResortData("PebbleCreek", "Pebble Creek", null, "http://www.pebblecreekskiarea.com", "/images/PebbleCreek.gif", IMG_HEIGHT, 80));
    resortMap.put("PineCreek",      new ResortData("PineCreek", "Pine Creek", null, "http://www.pinecreekskiresort.com", "/images/pinecreek.gif", IMG_HEIGHT, 80));
    resortMap.put("PineMountain",   new ResortData("PineMountain", "Pine Mountain", "schedule@PineMountainSkiPatrol.com", "http://www.PineMountainResort.com", "/images/PineMtnLogo.png", IMG_HEIGHT, 80));
    resortMap.put("Plattekill",     new ResortData("Plattekill", "Plattekill Mountain", null, "http://plattekill.com/", "/images/PlattekillLogo.png", IMG_HEIGHT, 147));
    resortMap.put("Pomerelle",      new ResortData("Pomerelle", "Pomerelle", null, "http://www.pomerelle-mtn.com", "/images/PomerelleLogo.gif", IMG_HEIGHT, 80));
//got new software    resortMap.put("PowderRidge",    new ResortData("PowderRidge", "Powder Ridge", null, "http://www.powderridgeskipatrol.com", "/images/PowderRidge.png", IMG_HEIGHT, 80));
    resortMap.put("RMSP",           new ResortData("RMSP", "Ragged Mountain", null, "http://www.rmskipatrol.com", "/images/RMSP_logo.JPG", IMG_HEIGHT, 80));
    resortMap.put("Sample",         new ResortData("Sample", "Sample Resort", null, "http://www.nspOnline.org", "/images/NSP_logo.gif", IMG_HEIGHT, 80));
//snobowl
//snowbird (hosts)
    resortMap.put("Snowbowl",      new ResortData("Snowbowl", "Snowbowl", null, "http://www.snowbowlskipatrol.org", "/images/SnowBowlLogo.jpg", 80, 80));
    resortMap.put("SnowCreek",      new ResortData("SnowCreek", "SnowCreek", null, "http://www.skisnowcreek.com", "/images/SnowCreek.jpg", IMG_HEIGHT, 80));
    resortMap.put("SnowKing",       new ResortData("SnowKing", "SnowKing", null, "http://www.SnowKing.com", "/images/SnowKing.jpg", IMG_HEIGHT, 80));
    resortMap.put("SoldierHollow", new ResortData("SoldierHollow", "Soldier Hollow", null, "http://utaholympiclegacy.org/soldier-hollow/", "/images/SOHO_II.jpg", IMG_HEIGHT, 60));
    resortMap.put("SoldierMountain", new ResortData("SoldierMountain", "Soldier Mountain", null, "http://www.soldiermountain.com", "/images/SoldierMountain.gif", IMG_HEIGHT, 80));
//replaced by Hyland hills    resortMap.put("ThreeRivers",    new ResortData("ThreeRivers", "Three Rivers Park", null, "http://www.threeriverspark.com", "/images/ThreeRivers.jpg", IMG_HEIGHT, 80));
//uop
    resortMap.put("WelchVillage",   new ResortData("WelchVillage", "Welch Village", null, "http://www.welchvillage.com", "/images/WelchVillage.jpg", IMG_HEIGHT, 80));
    resortMap.put("WhitePine",      new ResortData("WhitePine", "White Pine", "gawilson@wyoming.com", "http://www.WhitePineSki.com", "/images/WhitePine.jpg", IMG_HEIGHT, 80));
    resortMap.put("WildMountain",     new ResortData("WildMountain", "Wild Mountain", null, "http://www.wildmountain.com/", "/images/WildMountain.jpeg", IMG_HEIGHT, 169));
    resortMap.put("Willamette",     new ResortData("Willamette", "Willamette Backcountry", null, "http://www.deetour.net/wbsp", "/images/Willamette.jpeg", IMG_HEIGHT, 80));
  }

  //  final static String JDBC_DRIVER = "org.gjt.mm.mysql.Driver"; //todo change July 32 2015
  private final static String JDBC_DRIVER = "com.mysql.jdbc.Driver";
  public final static String newShiftStyle = "--New Shift Style--";

  // create a Mountain Standard Time time zone
  public final static String NEW_SHIFT_STYLE = "--New Shift Style--";

  public final static int MAX_PATROLLERS = 400; //todo hack fix me
  public final static String SERVLET_URL = "/calendar-1/";

  public final static boolean FETCH_MIN_DATA = false;
  public final static boolean FETCH_ALL_DATA = true;

  //all the folowing instance variables must be initialized in the constructor
  //  private Connection connection;
  private Connection connection;
//  private ResultSet rosterResults;
//  private ResultSet assignmentResults;  //todo get rid of this global !!!
  private PreparedStatement shiftStatement;
  private boolean fetchFullData;
  private String localResort;
  private SessionData sessionData;
  private Logger LOG;

  public PatrolData(boolean readAllData, String myResort, SessionData sessionData, final Logger parentLogger) {
    LOG = new Logger(PatrolData.class, parentLogger, myResort);
    this.sessionData = sessionData;
//    rosterResults = null;
//    assignmentResults = null;
    shiftStatement = null;
    localResort = myResort;
    fetchFullData = readAllData;

    try {
      Class.forName(JDBC_DRIVER).newInstance();
    }
    catch (Exception e) {
      logError(sessionData, "Cannot load the driver, reason:" + e.toString());
      logError(sessionData, "Most likely the Java class path is incorrect.");
      //todo do something here. besides throw a NPE later
      return;
    }
    connection = getConnection(localResort, sessionData);

    if (connection == null) //error was already displayed, if null
    {
      logError(sessionData, "getConnection(" + localResort + ", sessionData) failed.");
      //todo do something here. besides throw a NPE later
    }
  } //end PatrolData constructor

  public Connection getConnection() {
    return connection;
  }

  public static Connection getConnection(String resort, SessionData sessionData) {    //todo get rid of static !!!!!!!!!!
    Connection conn = null;
    try {
      debugOut(sessionData, "-----getJDBC_URL(" + resort + ")=" + getJDBC_URL(resort));
//      debugOut(sessionData, "-----" + sessionData.getDbUser() + ", " + sessionData.getDbPassword());
      conn = java.sql.DriverManager.getConnection(getJDBC_URL(resort), sessionData.getDbUser(), sessionData.getDbPassword());
      debugOut(sessionData, "PatrolData.connection " + ((conn == null) ? "FAILED" : "SUCCEEDED") + " for " + getJDBC_URL(resort));
    }
    catch (Exception e) {
      logError(sessionData, "Error: " + e.getMessage() + " connecting to table:" + resort);
      java.lang.Thread.currentThread().dumpStack();
    }
    return conn;
  }

  public ResultSet resetAssignments() {
    try {
      String selectAllAssignmentsByDateSQLString = Assignments.getSelectAllAssignmentsByDateSQLString(localResort);
      LOG.info("resetAssignments: " + selectAllAssignmentsByDateSQLString);
      PreparedStatement assignmentsStatement = connection.prepareStatement(selectAllAssignmentsByDateSQLString);
      return assignmentsStatement.executeQuery();
    }
    catch (Exception e) {
      logError(sessionData, " Error (line 167) resetting Assignments table query:" + e.getMessage());
      return null;
    } //end try
  }

  public Assignments readNextAssignment(ResultSet assignmentResults) {
    //todo srg fix all callers to this.  it is returning things out of order.  use readSortedAssignments
//    logger("\n**********\nfix all callers of this API (except PurgeAssignments, ?)");
//    Map<Thread, StackTraceElement[]> threadMap = Thread.getAllStackTraces();
//    Thread currentThread = Thread.currentThread();
//    if (threadMap.get(currentThread) != null) {
//      logger("[1]" + threadMap.get(currentThread)[1].toString());
//      logger("[2]" + threadMap.get(currentThread)[2].toString());
//      logger("[3]" + threadMap.get(currentThread)[3].toString());
//      logger("[4]" + threadMap.get(currentThread)[4].toString());
//      logger("[5]" + threadMap.get(currentThread)[5].toString());
//      logger("[6]" + threadMap.get(currentThread)[6].toString());
//    }
//    logger("*******");
    Assignments ns = null;
    try {
      if (assignmentResults.next()) {    //todo uses global :-(
        ns = new Assignments();
        ns.read(sessionData, assignmentResults);
      }
    }
    catch (SQLException e) {
      LOG.logException("Cannot read Assignment:", e);
      return null;
    }
    return ns;
  }

  public ResultSet resetRoster() {
    try {
      String sqlQuery = "SELECT * FROM roster ORDER BY LastName, FirstName";
      LOG.logSqlStatement(sqlQuery);
      PreparedStatement rosterStatement = connection.prepareStatement(sqlQuery);
      return rosterStatement.executeQuery();
    }
    catch (Exception e) {
      LOG.logException("Error reseting roster table ", e);
      return null;
    } //end try
  }

  public ResultSet resetRoster(String sort) {
    try {
      PreparedStatement rosterStatement = connection.prepareStatement("SELECT * FROM roster ORDER BY " + sort);
      return rosterStatement.executeQuery();
    }
    catch (Exception e) {
      LOG.logException("Error resetting roster table ", e);
      return null;
    } //end try
  }

  public void resetShiftDefinitions() {
    try {
      shiftStatement = connection.prepareStatement("SELECT * FROM shiftdefinitions ORDER BY \"" + ShiftDefinitions.tags[0] + "\""); //sort by default key
      shiftStatement.executeQuery();  //todo ignore return ???
    }
    catch (Exception e) {
      LOG.logException("Error resetting Shifts table exception=", e);
    } //end try
  }

  public boolean writeDirectorSettings(DirectorSettings ds) {
    return ds.write(connection);
  }

  public DirectorSettings readDirectorSettings() {
    DirectorSettings ds = new DirectorSettings(localResort, LOG);
    final ResultSet directorResults = ds.reset(connection);
    try {
      if (directorResults.next()) {
        ds.read(directorResults);
      }
      else {
        LOG.error("ERROR: directorResults.next() failed:");
      }
    }
    catch (Exception e) {
        LOG.logException("Cannot read DirectorSettings:", e);
      return null;
    }
    return ds;
  }

  public ArrayList<ShiftDefinitions> readShiftDefinitions() {
    ArrayList<ShiftDefinitions> shiftDefinitions = new ArrayList<ShiftDefinitions>();
    try {
      String qryString = "SELECT * FROM `shiftdefinitions` ORDER BY `shiftdefinitions`.`EventName` ASC";
      LOG.logSqlStatement(qryString);
      PreparedStatement assignmentsStatement = connection.prepareStatement(qryString);
      ResultSet assignmentResults = assignmentsStatement.executeQuery();

      while (assignmentResults.next()) {
        ShiftDefinitions ns = new ShiftDefinitions(LOG);
        ns.read(assignmentResults);
//        logger(".. NextShifts-" + ns.toString());
        shiftDefinitions.add(ns);
      }
    }
    catch (Exception e) {
      LOG.logException(" Error (line 279) resetting Assignments table ", e);
    } //end try
    return shiftDefinitions;
  }

  public void decrementShift(ShiftDefinitions ns) {
    if (DEBUG) {
      LOG.debug("decrement shift:" + ns);
    }
    int i = ns.getEventIndex();
    if (DEBUG) {
      LOG.debug("event index =" + i);
    }

    if (i == 0) {
      return;
    }
    deleteShift(ns);
    ns.setEventName(Assignments.createAssignmentName(ns.parsedEventName(), i - 1));
    writeShift(ns);
  }

  public void deleteShift(ShiftDefinitions ns) {
    String qryString = ns.getDeleteSQLString(localResort);
    LOG.info("deleteShift: " + qryString);
    try {
      PreparedStatement sAssign = connection.prepareStatement(qryString);
      sAssign.executeUpdate();
      ns.setExists(false);
    }
    catch (Exception e) {
      LOG.logException("Cannot delete Shift, reason: ",e);
    }
  }

  public boolean writeShift(ShiftDefinitions ns) {
    String qryString;
    if (ns.exists()) {
      qryString = ns.getUpdateShiftDefinitionsQueryString(localResort);
    }
    else {
      qryString = ns.getInsertShiftDefinitionsQueryString(localResort);
    }
    LOG.info("writeShift: " + qryString);
    try {
      PreparedStatement sAssign = connection.prepareStatement(qryString);
      sAssign.executeUpdate();
      ns.setExists(true);
    }
    catch (SQLException e) {
      logError(sessionData, " failed writeShift, exception=" + e.getMessage());
      return true;
    }
    return false;
  }

  public void close() {
    try {
      if (DEBUG) {
        LOG.debug("-- close connection");
      }
      if (connection != null) {
        connection.close(); //let it close in finalizer ??
      }
    }
    catch (Exception e) {
      LOG.logException(" Error closing connection:", e);
      Thread.currentThread().dumpStack();
    } //end try
  } // end close

  public Roster nextMember(String defaultString, ResultSet rosterResults) {
    Roster member = null;
    try {
      if (rosterResults.next()) {
        member = new Roster(LOG);  //"&nbsp;" is the default
        member.readFullFromRoster(rosterResults, defaultString);
      } //end if
    }
    catch (SQLException e) {
      member = null;
      LOG.logException("Failed nextMember, reason:", e);
      Thread.currentThread().dumpStack();
    } //end try

    return member;
  } //end nextMember

  public Roster getMemberByID(String szMemberID) {
    Roster member;
    String str = "SELECT * FROM roster WHERE IDNumber =" + szMemberID;
    LOG.logSqlStatement(str);
    if (szMemberID == null || szMemberID.length() <= 3) {
      return null;
    }
    else if (szMemberID.equals(sessionData.getBackDoorUser())) {
      member = new Roster(LOG);  //"&nbsp;" is the default
      member.setLast(backDoorFakeLastName);
      member.setFirst(backDoorFakeFirstName);
      member.setEmail(backDoorEmail);
      member.setID("000000");
      member.setDirector("yes");
      return member;
    }
    try {
      PreparedStatement rosterStatement = connection.prepareStatement(str);
      ResultSet rosterResults = rosterStatement.executeQuery();
      while (rosterResults.next()) {
        int id = rosterResults.getInt("IDNumber");
        String str1 = id + "";
        //noinspection Duplicates
        if (str1.equals(szMemberID)) {
          member = new Roster(LOG);  //"&nbsp;" is the default
          if (fetchFullData) {
            member.readFullFromRoster(rosterResults, "");
          }
          else {
            member.readPartialFromRoster(rosterResults, "");
          }
          return member;
        }
      } //end while
    }
    catch (Exception e) {
      LOG.logException("Error in getMemberByID(" + szMemberID + "): ", e);
      //noinspection AccessStaticViaInstance
      Thread.currentThread().dumpStack();
    } //end try
    return null; //failed
  } //end getMemberByID

  public Roster getMemberByEmail(String szEmail) {
    Roster member;
    String str = "SELECT * FROM roster WHERE email =\"" + szEmail + "\"";
    LOG.logSqlStatement(str);
    try {
      PreparedStatement rosterStatement = connection.prepareStatement(str);
      ResultSet rosterResults = rosterStatement.executeQuery();
      if (rosterResults.next()) {
        member = new Roster(LOG);  //"&nbsp;" is the default
        if (fetchFullData) {
          member.readFullFromRoster(rosterResults, "");
        }
        else {
          member.readPartialFromRoster(rosterResults, "");
        }
        return member;
      } //end while
    }
    catch (Exception e) {
      LOG.logException("Error in getMemberByEmail(" + szEmail + "): ", e);
    } //end try
    return null;  //failure
  } //end getMemberByID

  public Roster getMemberByName2(String szFullName) {
    return getMemberByLastNameFirstName(szFullName);
  } //end getMemberByName

  public Roster getMemberByLastNameFirstName(String szFullName) {
    Roster member;
    String str = "SELECT * FROM roster";
    LOG.logSqlStatement(str);
    try {
      PreparedStatement rosterStatement = connection.prepareStatement(str);
      ResultSet rosterResults = rosterStatement.executeQuery();
      while (rosterResults.next()) {
//                int id = rosterResults.getInt("IDNumber");
        String str1 = rosterResults.getString("LastName").trim() + ", " +
            rosterResults.getString("FirstName").trim();
//Log.log("getMemberByLastNameFirstName: (" + szFullName + ") (" + str1 + ") cmp=" + str1.equals(szFullName));
        //noinspection Duplicates
        if (str1.equals(szFullName)) {
          member = new Roster(LOG);  //"&nbsp;" is the default
          if (fetchFullData) {
            member.readFullFromRoster(rosterResults, "");
          }
          else {
            member.readPartialFromRoster(rosterResults, "");
          }
          return member;
        }
      } //end while
    }
    catch (SQLException e) {
      logError(sessionData, "(" + localResort + ") failed getMemberByLastNameFirstName(" + szFullName + "):" + e.getMessage());
      Thread.currentThread().dumpStack();
    }
    return null;  //not found (or error)
  }

  public static int StringToIndex(String temp) {
    int i;
    try {
      i = Integer.parseInt(temp);
    }
    catch (Exception e) {
      char ch = temp.charAt(0);
      i = ch - 'A' + 10;
    }
    return i;
  }

  public static String IndexToString(int i) {
    String val;
    if (i < 10) {
      val = i + "";     //force automatic conversion of integer to string
    }
    else {
      val = String.valueOf((char) ('A' + i - 10));
    }
    return val;
  }

  public Assignments readAssignment(String myDate) { //formmat yyyy-mm-dd_p
    Assignments ns;
    try {
      String queryString = "SELECT * FROM assignments WHERE Date=\'" + myDate + "\'";
      LOG.logSqlStatement(queryString);
      PreparedStatement assignmentsStatementLocal = connection.prepareStatement(queryString);
      ResultSet assignmentResultsLocal = assignmentsStatementLocal.executeQuery();

        if (assignmentResultsLocal.next()) {
          ns = new Assignments();
          ns.read(sessionData, assignmentResultsLocal);
          LOG.logSqlStatement(queryString);
          return ns;
        }
    }
    catch (SQLException e) {
      logError(sessionData, "failed readAssignment, reason:" + e.getMessage());
      return null;
    }
    return null;
  }


  public boolean writeAssignment(Assignments ns) {
    String qryString;
    if (ns.exists()) {
      qryString = ns.getUpdateQueryString(sessionData);
    }
    else {
      qryString = ns.getInsertQueryString(sessionData);
    }
    try {
      PreparedStatement sAssign = connection.prepareStatement(qryString);
      sAssign.executeUpdate();
    }
    catch (SQLException e) {
      logError(sessionData, "(" + localResort + ") failed writeAssignment, reason:" + e.getMessage());
      return true;
    }
    return false;
  }

//---------------------------------------------------------------------
//     decrementAssignment -  change 2015-10-06_2 to 2015-10-06_1  (delete _2 and write _1)
//                                   2015-10-06_0  is ignored
//---------------------------------------------------------------------
  public void decrementAssignment(Assignments ns) {
    LOG.info("decrement Assignment:" + ns);
    int i = ns.getDatePos(); //1 based

    if (i < 1)    //#'s are 0 based, can't decrement pos 0
    {
      return;
    }
    deleteAssignment(ns);

    String qry2String = ShiftDefinitions.createShiftName(ns.getDateOnly(), i - 1);
    ns.setDate(qry2String);
    writeAssignment(ns);
  }

//---------------------------------------------------------------------
//     deleteShift - DELETE Shift assignment for a specified date and index
//---------------------------------------------------------------------
  public void deleteAssignment(Assignments ns) {
    String qryString = ns.getDeleteSQLString(sessionData);
    LOG.info("deleteAssignment" + qryString);
    try {
      PreparedStatement sAssign = connection.prepareStatement(qryString);
      sAssign.executeUpdate();
      ns.setExisted(false);
    }
    catch (SQLException e) {
      logError(sessionData, "(" + localResort + ") failed deleteAssignment, reason:" + e.getMessage());
    }
  }

//--------------------
// AddShiftsToDropDown
//--------------------
  static public void AddShiftsToDropDown(PrintWriter out, ArrayList shifts, String selectedShift) {
    String lastName = "";
    String parsedName;
    String selected = "";

    if (selectedShift == null) {
      selected = " selected";
    }
    out.println("                    <option" + selected + ">" + NEW_SHIFT_STYLE + "</option>");
    for (Object shift : shifts) {
      ShiftDefinitions data = (ShiftDefinitions) shift;
      parsedName = data.parsedEventName();
      if (parsedName.equals(selectedShift)) {
        selected = " selected";
      }
      else {
        selected = "";
      }
      if (!parsedName.equals(lastName)) {
        out.println("<option" + selected + ">" + parsedName + "</option>");
        lastName = parsedName;
      }
    }
  }
//--------------------
// countDropDown
//--------------------
  static private void countDropDown(PrintWriter out, String szName, int value) {
    out.println("<select size=\"1\" name=\"" + szName + "\">");
    for (int i = Math.min(1, value); i <= Assignments.MAX_ASSIGNMENT_SIZE; ++i) {
      if (i == value) {
        out.println("<option selected>" + i + "</option>");
      }
      else {
        out.println("<option>" + i + "</option>");
      }
    }
    out.println("                  </select>");
  }
//--------------------
// AddShiftsToTable
//--------------------
  static public void AddShiftsToTable(PrintWriter out, ArrayList shifts, String selectedShift) {
    int validShifts = 0;
    for (Object shift : shifts) {
      ShiftDefinitions data = (ShiftDefinitions) shift;
      String parsedName = data.parsedEventName();
      if (parsedName.equals(selectedShift)) {
//name is if the format of startTime_0, endTime_0, count_0, startTime_1, endTime_1, count_1, etc
// delete_0, delete_1
//shiftCount
        out.println("<tr>");
//delete button
//              out.println("<td width=\"103\"><input onClick=\"DeleteBtn()\" type=\"button\" value=\"Delete\" name=\"delete_"+validShifts+"\"></td>");
        out.println("<td><input type='submit' value='Delete' name='delete_" + validShifts + "'></td>");
        out.println("<td>Start: <input type='text' onKeyDown='javascript:return captureEnter(event.keyCode)' name='startTime_" + validShifts + "' size='7' value='" + data.getStartString() + "'></td>");
        out.println("<td>End: <input type='text' onKeyDown='javascript:return captureEnter(event.keyCode)' name='endTime_" + validShifts + "' size='7' value='" + data.getEndString() + "'></td>");
        out.println("<td>Patroller&nbsp;Count:&nbsp;");
//                out.println("<input type=\"text\" name=\"count_"+validShifts+"\" size=\"4\" value=\""+data.getCount()+"\">");
        countDropDown(out, "count_" + validShifts, data.getCount());
        out.println("</td>");
//add Day/Seing/Night shift
        out.println("<td>&nbsp;");
        out.println("<select size=1 name='shift_" + validShifts + "'>");
//Log.log("in AddShiftsToTable, data.getType()="+data.getType());
        for (int shiftType = 0; shiftType < Assignments.MAX_SHIFT_TYPES; ++shiftType) {
          String sel = (data.getType() == shiftType) ? "selected" : "";
          out.println("<option " + sel + ">" + Assignments.getShiftName(shiftType) + "</option>");
        }
        out.println("</select>");
        out.println("</td>");
        out.println("</tr>");
        ++validShifts;
      }
    }
    out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"shiftCount\" VALUE=\"" + validShifts + "\">");
  }

//--------------------
// AddAssignmentsToTable
//--------------------
  static public void AddAssignmentsToTable(PrintWriter out, ArrayList parameterAssignments) {
    int validShifts = 0;
    for (Object parameterAssignment : parameterAssignments) {
      Assignments data = (Assignments) parameterAssignment;
//      String parsedName = data.getEventName();
      int useCount = data.getUseCount("???");    //get # of patrollers actually assigned to this shift (warn if deleteing!)
//            if(parsedName.equals(selectedShift)) {
//name is if the format of startTime_0, endTime_0, count_0, startTime_1, endTime_1, count_1, etc
// delete_0, delete_1
//shiftCount
      out.println("<tr>");
//delete button
//              out.println("<td width=\"103\"><input onClick=\"DeleteBtn()\" type=\"button\" value=\"Delete\" name=\"delete_"+validShifts+"\"></td>");
      out.println("<td><input type=\"submit\" value=\"Delete\" onclick=\"return confirmShiftDelete(" + useCount + ")\" name=\"delete_" + validShifts + "\"></td>");
      out.println("<td>Start: <input type=\"text\" name=\"startTime_" + validShifts + "\" onKeyDown=\"javascript:return captureEnter(event.keyCode)\" size=\"7\" value=\"" + data.getStartingTimeString() + "\"></td>");
      out.println("<td>End: <input type=\"text\" name=\"endTime_" + validShifts + "\" onKeyDown=\"javascript:return captureEnter(event.keyCode)\" size=\"7\" value=\"" + data.getEndingTimeString() + "\"></td>");
      out.println("<td>Patroller&nbsp;Count:&nbsp;");
//                out.println("<input type=\"text\" name=\"count_"+validShifts+"\" size=\"4\" value=\""+data.getCount()+"\">");
      countDropDown(out, "count_" + validShifts, data.getCount());
      out.println("</td>");
//add Day/Seing/Night shift
      out.println("<td>&nbsp;&nbsp; ");
      out.println("<select size=1 name='shift_" + validShifts + "'>");
//Log.log("in AddAssignmentsToTable, data.getType()="+data.getType());
      for (int shiftType = 0; shiftType < Assignments.MAX_SHIFT_TYPES; ++shiftType) {
        String sel = (data.getType() == shiftType) ? "selected" : "";
        out.println("<option " + sel + ">" + Assignments.getShiftName(shiftType) + "</option>");
      }
      out.println("</select>");
      out.println("</td>");

      out.println("</tr>");
      ++validShifts;
//            }
    }
    out.println("<INPUT TYPE='HIDDEN\' NAME='shiftCount' VALUE='" + validShifts + "'>");
  }

  static public boolean isValidResort(String resort) {
    return resortMap.containsKey(resort);
  }

  static public ResortData getResortInfo(String theResort) {
    return resortMap.get(theResort);
  }

  public ResortData getResortInfo() {
    return resortMap.get(localResort);
  }

  public static String getResortFullName(String resort) {
    if (resortMap.containsKey(resort)) {    //resort string is same as db name, value is full resort string
      return resortMap.get(resort).getResortFullName();
    }
    Logger.logStatic("**** Error, unknown resort (" + resort + ")");
    Thread.currentThread().dumpStack();
    return "Error, invalid resort (" + resort + ")";
  }

  private static String getJDBC_URL(String resort) {
    String jdbcLoc = "jdbc:mysql://" + MYSQL_ADDRESS + "/";
    if (isValidResort(resort)) {
      return jdbcLoc + resort;
    }

    logger(resort, "****** Error, unknown resort (" + resort + ")");
    Thread.currentThread().dumpStack();
    return "invalidResort";
  }

  public boolean insertNewIndividualAssignment( NewIndividualAssignment newIndividualAssignment) {
    String qryString = newIndividualAssignment.getInsertSQLString(sessionData);
    LOG.info("insertNewIndividualAssignment" + qryString);
    try {
      PreparedStatement sAssign = connection.prepareStatement(qryString);
      sAssign.executeUpdate();
    }
    catch (SQLException e) {
      LOG.logException("Cannot insert newIndividualAssignment", e);
      return false;
    }
    return true;
  }

  public boolean updateNewIndividualAssignment(NewIndividualAssignment newIndividualAssignment) {
    String qryString = newIndividualAssignment.getUpdateSQLString(sessionData);
    LOG.info("updateNewIndividualAssignment: " + qryString);
    try {
      PreparedStatement sAssign = connection.prepareStatement(qryString);
      sAssign.executeUpdate();
    }
    catch (SQLException e) {
      logError(sessionData, "(" + localResort + ") Cannot update newIndividualAssignment, reason:" + e.getMessage());
      return false;
    }
    return true;
  }

  public HashMap<String, NewIndividualAssignment> readNewIndividualAssignments(int year, int month, int day) { //formmat yyyy-mm-dd_i_n
    HashMap<String, NewIndividualAssignment> results = new HashMap<String, NewIndividualAssignment>();
    String key = NewIndividualAssignment.buildKey(year, month, day, -1, -1);
    key += "%";
    //SELECT * FROM `newindividualassignment` WHERE `date_shift_pos` LIKE "2009-02-07%"
    try {
      String queryString = "SELECT * FROM `newindividualassignment` WHERE `date_shift_pos` LIKE \'" + key + "\'";
      debugOut(null, "readNewIndividualAssignments: " + queryString);
      PreparedStatement assignmentsStatement = connection.prepareStatement(queryString);
      ResultSet assignmentResults = assignmentsStatement.executeQuery();
      while (assignmentResults.next()) {
        NewIndividualAssignment newIndividualAssignment = new NewIndividualAssignment();
        newIndividualAssignment.read(sessionData, assignmentResults);
        debugOut(null, newIndividualAssignment.toString());
        results.put(newIndividualAssignment.getDateShiftPos(), newIndividualAssignment);
      }
    }
    catch (SQLException e) {
      logError(sessionData, "(" + localResort + ") readNewIndividualAssignments" + e.getMessage());
      return null;
    }
    return results;
  }

//  private void logger(String message) {
//    if (sessionData != null && sessionData.getRequest() != null) {
//      Logger.printToLogFileStatic(sessionData.getRequest(), localResort, sessionData.getLoggedInUserId(), message);
//    }
//    else {
//      logger(localResort, "THIS SHOULD NEVER HAPPEN. PatrolData@782, " + message);
//    }
//  }
//
  public static void logger(String myResort, String message) {
    Logger.printToLogFileStatic(null, myResort, message);
  }

  public void deleteNewIndividualAssignment(NewIndividualAssignment newIndividualAssignment) {
    String qryString = newIndividualAssignment.getDeleteSQLString(sessionData);
    LOG.logSqlStatement(qryString);
    try {
      PreparedStatement sAssign = connection.prepareStatement(qryString);
      sAssign.executeUpdate();  //can throw exception
      newIndividualAssignment.setExisted(false);
    }
    catch (Exception e) {
      Logger.logStatic("(" + localResort + ") Cannot delete Shift, reason:" + e.toString());
    }
  }

  public ArrayList<Assignments> readAllSortedAssignments(String patrollerId) {
    String dateMask = "20%"; //WHERE `date_shift_pos` LIKE '2015-10-%'
//    logger("readSortedAssignments(" + dateMask + ")");
    ArrayList<Assignments> monthAssignments = new ArrayList<Assignments>();
    try {
      String qryString = "SELECT * FROM `assignments` WHERE Date like '" + dateMask + "' ORDER BY Date";
      LOG.logSqlStatement(qryString);
      PreparedStatement assignmentsStatement = connection.prepareStatement(qryString);
      ResultSet assignmentResults = assignmentsStatement.executeQuery();
//      int cnt = 1;
      while (assignmentResults.next()) {
        Assignments assignments = new Assignments();
        assignments.read(sessionData, assignmentResults);
        if (assignments.includesPatroller(patrollerId)) {
//          logger("(" + (cnt++) + ") NextAssignment-" + ns.toString());
          monthAssignments.add(assignments);
        }
      }
    }
    catch (Exception e) {
      LOG.logException(" Error (line 821) resetting Assignments table", e);
    } //end try
    return monthAssignments;
  }

  public ArrayList<Assignments> readSortedAssignments(String patrollerId, int year, int month) {
    ArrayList<Assignments> unFilteredAssignments = readSortedAssignments(year, month);
    return filterAssignments(patrollerId, unFilteredAssignments);
  }

  public ArrayList<Assignments> readSortedAssignments(String patrollerId, int year, int month, int day) {
    ArrayList<Assignments> unFilteredAssignments = readSortedAssignments(year, month, day);
    return filterAssignments(patrollerId, unFilteredAssignments);
  }

  private ArrayList<Assignments> filterAssignments(String patrollerId, ArrayList<Assignments> unFilteredAssignments) {
    ArrayList<Assignments> filteredAssignments = new ArrayList<Assignments>();
    for (Assignments eachAssignment : unFilteredAssignments) {
      if (eachAssignment.includesPatroller(patrollerId)) {
        filteredAssignments.add(eachAssignment);
      }
    }
    return filteredAssignments;
  }

  public ArrayList<Assignments> readSortedAssignments(int year, int month) {
    String dateMask = String.format("%4d-%02d-", year, month) + "%";
//    logger("  readSortedAssignments(" + dateMask + ")");
    ArrayList<Assignments> monthAssignments = new ArrayList<Assignments>();
    try {
      String qryString = "SELECT * FROM `assignments` WHERE Date like '" + dateMask + "' ORDER BY Date";
      LOG.logSqlStatement(qryString);
      PreparedStatement assignmentsStatement = connection.prepareStatement(qryString);
      ResultSet assignmentResults = assignmentsStatement.executeQuery();

      while (assignmentResults.next()) {
        Assignments ns = new Assignments();
        ns.read(sessionData, assignmentResults);
//        logger(".. NextAssignment-" + ns.toString());
        monthAssignments.add(ns);
      }
    }
    catch (Exception e) {
      LOG.logException("Error (line 864) resetting Assignments table", e);
    } //end try
    return monthAssignments;
  }

  public ArrayList<Assignments> readSortedAssignments(int year, int month, int day) {
    String dateMask = String.format("%4d-%02d-%02d_", year, month, day) + "%";
//    logger("  readSortedAssignments(" + dateMask + ")");
    ArrayList<Assignments> monthAssignments = new ArrayList<Assignments>();
    try {
      String qryString = "SELECT * FROM `assignments` WHERE Date like '" + dateMask + "' ORDER BY Date";
      LOG.logSqlStatement(qryString);
      PreparedStatement assignmentsStatement = connection.prepareStatement(qryString);
      ResultSet assignmentResults = assignmentsStatement.executeQuery();

      while (assignmentResults.next()) {
        Assignments ns = new Assignments();
        ns.read(sessionData, assignmentResults);
//        logger(".. NextAssignment-" + ns.toString());
        monthAssignments.add(ns);
      }
    }
    catch (Exception e) {
      LOG.logException("Error (line 887) resetting Assignments table", e);
    } //end try
    return monthAssignments;
  }

  private static void debugOut(SessionData sessionData, String msg) {
    if (DEBUG) {
      String localResort = sessionData == null ? "noLoggedInResort" : sessionData.getLoggedInResort();
      HttpServletRequest request = sessionData == null ? null : sessionData.getRequest();
      Logger.printToLogFileStatic(request , localResort, msg);
    }
  }

  private static void logError(SessionData sessionData, String msg) {
    String localResort = sessionData == null ? "noLoggedInResort" : sessionData.getLoggedInResort();
    HttpServletRequest request = sessionData == null ? null : sessionData.getRequest();
    Logger.printToLogFileStatic(request , localResort, msg);
  }

  public static boolean isValidLogin(PrintWriter out, String resort, String ID, String pass, SessionData sessionData) {
    Logger logErr = new Logger(PatrolData.class, sessionData.getRequest(), "???", resort);
    boolean validLogin = false;
    ResultSet rs;
//Log.log("LoginHelp: isValidLogin("+resort + ", "+ID+", "+pass+")");
    if (ID == null || pass == null) {
      logErr.error("Login Failed: either ID (" + ID + ") or Password not supplied");
      return false;
    }

    try {
      //noinspection unused
      Driver drv = (Driver) Class.forName(PatrolData.JDBC_DRIVER).newInstance();
    }
    catch (Exception e) {
      logErr.logException("LoginHelp: Cannot find mysql driver:", e);
      return false;
    }

    if (ID.equalsIgnoreCase(sessionData.getBackDoorUser()) && pass.equalsIgnoreCase(sessionData.getBackDoorPassword())) {
      return true;
    }    // Try to connect to the database
    try {
      // Change MyDSN, myUsername and myPassword to your specific DSN
      Connection c = PatrolData.getConnection(resort, sessionData);
      @SuppressWarnings("SqlNoDataSourceInspection")
      String szQuery = "SELECT * FROM roster WHERE IDNumber = \"" + ID + "\"";
      logErr.info(szQuery);
      @SuppressWarnings("SpellCheckingInspection")
      PreparedStatement sRost = c.prepareStatement(szQuery);
      if (sRost == null) {
        return false;
      }

      rs = sRost.executeQuery();

      if (rs != null && rs.next()) {      //will only loop 1 time

        String originalPassword = rs.getString("password");
        String lastName = rs.getString("LastName");
        String firstName = rs.getString("FirstName");
        String emailAddress = rs.getString("email");
        originalPassword = originalPassword.trim();
        lastName = lastName.trim();
        pass = pass.trim();

        boolean hasPassword = (originalPassword.length() > 0);
        if (hasPassword) {
          if (originalPassword.equalsIgnoreCase(pass)) {
            validLogin = true;
          }
        }
        else {
          if (lastName.equalsIgnoreCase(pass)) {
            validLogin = true;
          }
        }

        if (validLogin) {
          Logger.printToLogFileStatic(sessionData.getRequest(), sessionData.getLoggedInResort(), "Login Sucessful: " + firstName + " " + lastName + ", " + ID + " (" + resort + ") " + emailAddress);
        }
        else {
          Logger.printToLogFileStatic(sessionData.getRequest(), sessionData.getLoggedInResort(), "Login Failed: ID=[" + ID + "] LastName=[" + lastName + "] suppliedPass=[" + pass + "] dbPass[" + originalPassword + "]");
        }
      }
      else {
        Logger.printToLogFileStatic(sessionData.getRequest(), sessionData.getLoggedInResort(), "Login Failed: memberId not found [" + ID + "]");
      }

      c.close();
    }
    catch (Exception e) {
      out.println("Error connecting or reading table:" + e.getMessage()); //message on browser
      Logger.printToLogFileStatic(sessionData.getRequest(), sessionData.getLoggedInResort(), "LoginHelp. Error connecting or reading table:" + e.getMessage());
    } //end try

    return validLogin;
  }
}
