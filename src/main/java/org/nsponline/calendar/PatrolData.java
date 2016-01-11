package org.nsponline.calendar;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Steve Gledhill
 */
@SuppressWarnings({"SqlNoDataSourceInspection", "AccessStaticViaInstance"})
public class PatrolData {
  final static boolean DEBUG = false;

  //  final static String JDBC_DRIVER = "org.gjt.mm.mysql.Driver"; //todo change July 32 2015
  final static String JDBC_DRIVER = "com.mysql.jdbc.Driver";
  final static String newShiftStyle = "--New Shift Style--";

  // create a Mountain Standard Time time zone
//  final static String[] ids = TimeZone.getAvailableIDs(-7 * 60 * 60 * 1000);
//  final static SimpleTimeZone MDT = new SimpleTimeZone(-7 * 60 * 60 * 1000, ids[0]);
  final static String NEW_SHIFT_STYLE = "--New Shift Style--";
  // set up rules for daylight savings time
//  static {
//    MDT.setStartRule(Calendar.APRIL, 1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);
//    MDT.setEndRule(Calendar.OCTOBER, -1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);
//  }

  static public HashMap<String, ResortData> resortMap = new HashMap<String, ResortData>();
  static private final int IMG_HEIGHT = 80;
  static {
    resortMap.put("Afton",          new ResortData("Afton", "Afton Alps", "http://www.aftonalpsskipatrol.org", "/images/AftonLogo.jpg", IMG_HEIGHT, 90));
    resortMap.put("AlpineMt",       new ResortData("AlpineMt", "Alpine Mt", "http://www.alpinemtskipatrol.org", "/images/AlpineMt.jpg", IMG_HEIGHT, 80));
    resortMap.put("Andes",          new ResortData("Andes", "Andes Tower Hills", "http://www.andestowerhills.com", "/images/andes_logo.jpg", IMG_HEIGHT, 80));
    resortMap.put("Brighton",       new ResortData("Brighton", "Brighton", "http://www.brightonresort.com", "/images/Brighton.gif", 60, 261));
    resortMap.put("BuenaVista",     new ResortData("BuenaVista", "Buena Vista", "http://www.bvskiarea.com", "/images/BuenaVista.gif", 75, 300));
    resortMap.put("DetroitMountain",new ResortData("DetroitMountain", "Detroit Mountain", "http://detroitmountain.com/", "/images/DetroitMountain.png", 73, 121));
    resortMap.put("ElmCreek",       new ResortData("ElmCreek", "Elm Creek Park", "https://www.threeriversparks.org/parks/elm-creek-park.aspx", "/images/ThreeRivers.jpg", IMG_HEIGHT, 80));
    resortMap.put("GrandTarghee",   new ResortData("GrandTarghee", "Grand Targhee", "http://www.GrandTarghee.com", "/images/GrandTarghee.jpg", IMG_HEIGHT, 80));
    resortMap.put("HermonMountain", new ResortData("HermonMountain", "Hermon Mountain", "http://www.skihermonmountain.com", "/images/HermonMountain.jpg", IMG_HEIGHT, 80));
    resortMap.put("Hesperus",       new ResortData("Hesperus", "Hesperus", "http://www.ski-hesperus.com/", "/images/Hesperus.jpg", 84, 192));
    resortMap.put("HylandHills",    new ResortData("HylandHills", "Hyland Hills Park", " https://threeriversparks.org/parks/hyland-lake-park/hyland-hills-ski-area.aspx", "/images/ThreeRivers.jpg", IMG_HEIGHT, 80));
    resortMap.put("IFNordic",       new ResortData("IFNordic", "IF Nordic", "", "/images/IFNordic.gif", IMG_HEIGHT, 80));
    resortMap.put("JacksonHole",    new ResortData("JacksonHole", "Jackson Hole Fire/EMS", "http://tetonwyo.org/AgencyHome.asp?dept_id=fire", "/images/JacksonHole.jpg", IMG_HEIGHT, 80));
    resortMap.put("JacksonSpecialEvents", new ResortData("JacksonSpecialEvents", "Jackson Hole Fire/EMS Special Events", "http://tetonwyo.org/AgencyHome.asp?dept_id=fire", "/images/JacksonHole.jpg", IMG_HEIGHT, 80));
    resortMap.put("KellyCanyon",    new ResortData("KellyCanyon", "Kelly Canyon", "http://www.SkiKelly.com", "/images/KellyCanyon.jpg", IMG_HEIGHT, 80));
    resortMap.put("LonesomePine",   new ResortData("LonesomePine", "Lonesome Pine Trails", "http://www.lonesomepines.org", "/images/lonesomepines.gif", IMG_HEIGHT, 80));
    resortMap.put("MountKato",      new ResortData("MountKato", "Mount Kato", "http://www.mtkatoskipatrol.com", "/images/MountKato.jpg", IMG_HEIGHT, 80));
    resortMap.put("NorwayMountain", new ResortData("NorwayMountain", "Norway Mountain", "http://www.NorwayMountain.com", "/images/NorwayMountain.jpg", IMG_HEIGHT, 80));
    resortMap.put("PaidSnowCreek",  new ResortData("PaidSnowCreek", "Paid SnowCreek", "http://www.skisnowcreek.com", "/images/SnowCreek.jpg", IMG_HEIGHT, 80));
    resortMap.put("PebbleCreek",    new ResortData("PebbleCreek", "Pebble Creek", "http://www.pebblecreekskiarea.com", "/images/PebbleCreek.gif", IMG_HEIGHT, 80));
    resortMap.put("PineCreek",      new ResortData("PineCreek", "Pine Creek", "http://www.pinecreekskiresort.com", "/images/pinecreek.gif", IMG_HEIGHT, 80));
    resortMap.put("PineMountain",   new ResortData("PineMountain", "Pine Mountain", "http://www.PineMountainResort.com", "/images/PineMtnLogo.png", IMG_HEIGHT, 80));
    resortMap.put("Pomerelle",      new ResortData("Pomerelle", "Pomerelle", "http://www.pomerelle-mtn.com", "/images/PomerelleLogo.gif", IMG_HEIGHT, 80));
    resortMap.put("PowderRidge",    new ResortData("PowderRidge", "Powder Ridge", "http://www.powderridgeskipatrol.com", "/images/PowderRidge.png", IMG_HEIGHT, 80));
    resortMap.put("RMSP",           new ResortData("RMSP", "Ragged Mountain", "http://www.rmskipatrol.com", "/images/RMSP_logo.JPG", IMG_HEIGHT, 80));
    resortMap.put("Sample",         new ResortData("Sample", "Sample Resort", "http://www.nspOnline.org", "/images/NSP_logo.gif", IMG_HEIGHT, 80));
    resortMap.put("SnowCreek",      new ResortData("SnowCreek", "SnowCreek", "http://www.skisnowcreek.com", "/images/SnowCreek.jpg", IMG_HEIGHT, 80));
    resortMap.put("SnowKing",       new ResortData("SnowKing", "SnowKing", "http://www.SnowKing.com", "/images/SnowKing.jpg", IMG_HEIGHT, 80));
    resortMap.put("SoldierHollow", new ResortData("SoldierHollow", "Soldier Hollow", "http://www.soldierhollow.com", "/images/SoldierHollow.jpg", IMG_HEIGHT, 60));
    resortMap.put("SoldierMountain", new ResortData("SoldierMountain", "Soldier Mountain", "http://www.soldiermountain.com", "/images/SoldierMountain.gif", IMG_HEIGHT, 80));
    resortMap.put("ThreeRivers",    new ResortData("ThreeRivers", "Three Rivers Park", "http://www.threeriverspark.com", "/images/ThreeRivers.jpg", IMG_HEIGHT, 80));
//    resortMap.put("uop",            new ResortData("uop", "The Utah Olympic Park", "http://www.imd.org/uop.html", "/images/uop.jpg", IMG_HEIGHT, 80));
    resortMap.put("WelchVillage",   new ResortData("WelchVillage", "Welch Village", "http://www.welchvillage.com", "/images/WelchVillage.jpg", IMG_HEIGHT, 80));
    resortMap.put("WhitePine",      new ResortData("WhitePine", "White Pine", "http://www.WhitePineSki.com", "/images/WhitePine.jpg", IMG_HEIGHT, 80));
    resortMap.put("Willamette",     new ResortData("Willamette", "Willamette Backcountry", "http://www.deetour.net/wbsp", "/images/Willamette.jpeg", IMG_HEIGHT, 80));
  }

/* - - - - - uncomment the following to run from the Internet - - - - - - */
//  final static String MYSQL_ADDRESS = "172.31.50.20";  //private ip address.  must match /etc/my.cnf
/* - - - - - uncomment the following to run local - - - - - -*/
    final static String MYSQL_ADDRESS = "127.0.0.1";     //must match /etc/my.cnf
/*- - - - - end local declarations - - - - - -*/

  // ***** start back door login stuff (works with ANY resort, and does NOT send any email confirmations)*****
  final static String backDoorFakeFirstName = "System";
  final static String backDoorFakeLastName = "Administrator";
  final static String backDoorEmail = "Steve@Gledhills.com";

  final static int MAX_PATROLLERS = 400;
  final static String SERVLET_URL = "/calendar-1/";

  static final boolean FETCH_MIN_DATA = false;
  static final boolean FETCH_ALL_DATA = true;

  //all the folowing instance variables must be initialized in the constructor
  //  private Connection connection;
  Connection connection;
  private ResultSet rosterResults;
  private ResultSet assignmentResults;
  private PreparedStatement shiftStatement;
  private ResultSet shiftResults;
  private boolean fetchFullData;
  private String localResort;
  private SessionData sessionData;

  public PatrolData(boolean readAllData, String myResort, SessionData sessionData) {
    this.sessionData = sessionData;
    rosterResults = null;
    assignmentResults = null;
    shiftStatement = null;
    shiftResults = null;
    localResort = myResort;
    fetchFullData = readAllData;

    try {
      Class.forName(JDBC_DRIVER).newInstance();
    }
    catch (Exception e) {
      errorOut(sessionData, "Cannot load the driver, reason:" + e.toString());
      errorOut(sessionData, "Most likely the Java class path is incorrect.");
      //todo do something here. besides throw a NPE later
      return;
    }
    connection = getConnection(localResort, sessionData);

    if (connection != null) //error was already displayed, if null
    {
      resetRoster();
    }
    else {
      errorOut(sessionData,"getConnection(" + localResort + ", sessionData) failed.");
      //todo do something here. besides throw a NPE later
    }
  } //end PatrolData constructor

  static Connection getConnection(String resort, SessionData sessionData) {    //todo get rid of static !!!!!!!!!!
    Connection conn = null;
    try {
      debugOut(sessionData, "-----getJDBC_URL(" + resort + ")=" + getJDBC_URL(resort));
//      debugOut(sessionData, "-----" + sessionData.getDbUser() + ", " + sessionData.getDbPassword());
      conn = java.sql.DriverManager.getConnection(getJDBC_URL(resort), sessionData.getDbUser(), sessionData.getDbPassword());
      debugOut(sessionData, "PatrolData.connection " + ((conn == null) ? "FAILED" : "SUCCEEDED") + " for " + getJDBC_URL(resort));
    }
    catch (Exception e) {
      errorOut(sessionData,"Error: " + e.getMessage() + " connecting to table:" + resort);
      java.lang.Thread.currentThread().dumpStack();
    }
    return conn;
  }

  public void resetAssignments() {
    //todo srg, get rid of this method and make assignmentResults method local
    try {
      String selectAllAssignmentsByDateSQLString = Assignments.getSelectAllAssignmentsByDateSQLString();
      logger("resetAssignments: " + selectAllAssignmentsByDateSQLString);
      PreparedStatement assignmentsStatement = connection.prepareStatement(selectAllAssignmentsByDateSQLString);
      assignmentResults = assignmentsStatement.executeQuery(); //todo uses global :-(
    }
    catch (Exception e) {
      errorOut(sessionData,"(" + localResort + ") Error resetting Assignments table query:" + e.getMessage());
    } //end try
  }

  public Assignments readNextAssignment() {
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
      logger("(" + localResort + ") Cannot read Assignment, reason:" + e.toString());
      return null;
    }
    return ns;
  }

  public void resetRoster() {
    try {
      PreparedStatement rosterStatement = connection.prepareStatement("SELECT * FROM roster ORDER BY LastName, FirstName");
      rosterResults = rosterStatement.executeQuery();
    }
    catch (Exception e) {
      System.out.println("(" + localResort + ") Error reseting roster table query:" + e.getMessage());
    } //end try
  }

  public void resetRoster(String sort) {
    try {
      PreparedStatement rosterStatement = connection.prepareStatement("SELECT * FROM roster ORDER BY " + sort);
      rosterResults = rosterStatement.executeQuery();
    }
    catch (Exception e) {
      System.out.println("(" + localResort + ") Error reseting roster table query:" + e.getMessage());
    } //end try
  }

  public void resetShiftDefinitions() {
    try {
      shiftStatement = connection.prepareStatement("SELECT * FROM shiftdefinitions ORDER BY \"" + Shifts.tags[0] + "\""); //sort by default key
      shiftResults = shiftStatement.executeQuery();
    }
    catch (Exception e) {
      System.out.println("(" + localResort + ") Error reseting Shifts table query:" + e.getMessage());
    } //end try
  }

  public boolean writeDirectorSettings(DirectorSettings ds) {
    return ds.write(connection);
  }

  public DirectorSettings readDirectorSettings() {
    ResultSet directorResults = DirectorSettings.reset(connection);
    DirectorSettings ds = null;
//System.out.println("HACK: directorResults starting try");
    try {
//System.out.println("ERROR: directorResults inside try");
      //noinspection ConstantConditions
      if (directorResults.next()) {
        ds = new DirectorSettings(localResort);
        ds.read(directorResults);
      }
      else {
        System.out.println("ERROR: directorResults.next() failed for resort: ");
      }
    }
    catch (Exception e) {
      if (ds == null) {
        System.out.println("Cannot read DirectorSettings for resort (ds=null), reason:" + e.toString());
      }
      else {
        System.out.println("Cannot read DirectorSettings for resort " + ds.getResort() + ", reason:" + e.toString());
      }
      return null;
    }
    return ds;
  }

  public ArrayList<Shifts> readShiftDefinitions() {
    ArrayList<Shifts> shiftDefinitions = new ArrayList<Shifts>();
    try {
      String qryString = "SELECT * FROM `shiftdefinitions` ORDER BY `shiftdefinitions`.`EventName` ASC";
//      logger("readShiftDefinitions: " + qryString);
      PreparedStatement assignmentsStatement = connection.prepareStatement(qryString);
      ResultSet assignmentResults = assignmentsStatement.executeQuery();

      while (assignmentResults.next()) {
        Shifts ns = new Shifts();
        ns.read(assignmentResults);
//        logger(".. NextShifts-" + ns.toString());
        shiftDefinitions.add(ns);
      }
    }
    catch (Exception e) {
      System.out.println("(" + localResort + ") Error resetting Assignments table query:" + e.getMessage());
    } //end try
    return shiftDefinitions;
  }

  public void decrementShift(Shifts ns) {
    if (DEBUG) {
      System.out.println("decrement shift:" + ns);
    }
    int i = ns.getEventIndex();
    if (DEBUG) {
      System.out.println("event index =" + i);
    }

    if (i == 0) {
      return;
    }
    deleteShift(ns);
    ns.eventName = Assignments.createAssignmentName(ns.parsedEventName(), i - 1);
    writeShift(ns);
  }

  public void deleteShift(Shifts ns) {
    String qryString = ns.getDeleteSQLString();
    logger("deleteShift: " + qryString);
    try {
      PreparedStatement sAssign = connection.prepareStatement(qryString);
      sAssign.executeUpdate();
      ns.setExists(false);
    }
    catch (Exception e) {
      System.out.println("(" + localResort + ") Cannot delete Shift, reason:" + e.toString());
    }
  }

  public boolean writeShift(Shifts ns) {
    String qryString;
    if (ns.exists()) {
      qryString = ns.getUpdateShiftDefinitionsQueryString();
    }
    else {
      qryString = ns.getInsertShiftDefinitionsQueryString();
    }
    logger("writeShift: " + qryString);
    try {
      PreparedStatement sAssign = connection.prepareStatement(qryString);
      sAssign.executeUpdate();
      ns.setExists(true);
    }
    catch (SQLException e) {
      errorOut(sessionData, "(" + localResort + ") failed writeShift, reason:" + e.getMessage());
      return true;
    }
    return false;
  }

  public void close() {
    try {
      if (DEBUG) {
        System.out.println("-- close connection (" + localResort + "): " + Utils.getCurrentDateTimeString());
      }
      connection.close(); //let it close in finalizer ??
    }
    catch (Exception e) {
      System.out.println("(" + localResort + ") Error closing connection:" + e.getMessage());
      Thread.currentThread().dumpStack();
    } //end try
  } // end close

  public MemberData nextMember(String defaultString) {
    MemberData member = null;
    try {
      if (rosterResults.next()) {
        member = new MemberData();  //"&nbsp;" is the default
        member.readFullFromRoster(rosterResults, defaultString);
      } //end if
    }
    catch (SQLException e) {
      member = null;
      System.out.println("(" + localResort + ") Failed nextMember, reason:" + e.getMessage());
      Thread.currentThread().dumpStack();
    } //end try

    return member;
  } //end nextMember

  public MemberData getMemberByID(String szMemberID) {
    MemberData member;
    String str = "SELECT * FROM roster WHERE IDNumber =" + szMemberID;
    if (szMemberID == null || szMemberID.length() <= 3) {
      return null;
    }
    else if (szMemberID.equals(sessionData.getBackDoorUser())) {
      member = new MemberData();  //"&nbsp;" is the default
      member.setLast(backDoorFakeLastName);
      member.setFirst(backDoorFakeFirstName);
      member.setEmail(backDoorEmail);
      member.setID("000000");
      member.setDirector("yes");
      return member;
    }
    try {
      PreparedStatement rosterStatement = connection.prepareStatement(str);
      rosterResults = rosterStatement.executeQuery();
      while (rosterResults.next()) {
        int id = rosterResults.getInt("IDNumber");
        String str1 = id + "";
        if (str1.equals(szMemberID)) {
          member = new MemberData();  //"&nbsp;" is the default
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
      System.out.println("(" + localResort + ") Error in getMemberByID(" + szMemberID + "): " + e.getMessage());
      System.out.println("(" + localResort + ") ERROR in PatrolData:getMemberByID(" + szMemberID + ") maybe a close was already done?");
      //noinspection AccessStaticViaInstance
      Thread.currentThread().dumpStack();
    } //end try
    return null; //failed
  } //end getMemberByID

  public MemberData getMemberByEmail(String szEmail) {
    MemberData member;
    String str = "SELECT * FROM roster WHERE email =\"" + szEmail + "\"";
//System.out.println(str);
    try {
      PreparedStatement rosterStatement = connection.prepareStatement(str);
      rosterResults = rosterStatement.executeQuery();
      if (rosterResults.next()) {
        member = new MemberData();  //"&nbsp;" is the default
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
      System.out.println("(" + localResort + ") Error in getMemberByEmail(" + szEmail + "): " + e.getMessage());
    } //end try
    return null;  //failure
  } //end getMemberByID

  public MemberData getMemberByName2(String szFullName) {
    return getMemberByLastNameFirstName(szFullName);
  } //end getMemberByName

  public MemberData getMemberByLastNameFirstName(String szFullName) {
    MemberData member;
    String str = "SELECT * FROM roster";
    try {
      PreparedStatement rosterStatement = connection.prepareStatement(str);
      rosterResults = rosterStatement.executeQuery();
      while (rosterResults.next()) {
//                int id = rosterResults.getInt("IDNumber");
        String str1 = rosterResults.getString("LastName").trim() + ", " +
            rosterResults.getString("FirstName").trim();
//System.out.println("getMemberByLastNameFirstName: (" + szFullName + ") (" + str1 + ") cmp=" + str1.equals(szFullName));
        if (str1.equals(szFullName)) {
          member = new MemberData();  //"&nbsp;" is the default
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
      errorOut(sessionData, "(" + localResort + ") failed getMemberByLastNameFirstName(" + szFullName + "):" + e.getMessage());
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
      PreparedStatement assignmentsStatementLocal = connection.prepareStatement(queryString);
      ResultSet assignmentResultsLocal = assignmentsStatementLocal.executeQuery();

        if (assignmentResultsLocal.next()) {
          ns = new Assignments();
          ns.read(sessionData, assignmentResultsLocal);
          logger("readAssignment(" + queryString + ")= " + ns.toString());
          return ns;
        }
    }
    catch (SQLException e) {
      errorOut(sessionData, "failed readAssignment, reason:" + e.getMessage());
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
    logger("writeAssignment: " + qryString);
    try {
      PreparedStatement sAssign = connection.prepareStatement(qryString);
      sAssign.executeUpdate();
    }
    catch (SQLException e) {
      errorOut(sessionData, "(" + localResort + ") failed writeAssignment, reason:" + e.getMessage());
      return true;
    }
    return false;
  }

//---------------------------------------------------------------------
//     decrementAssignment -  change 2015-10-06_2 to 2015-10-06_1  (delete _2 and write _1)
//                                   2015-10-06_0  is ignored
//---------------------------------------------------------------------
  public void decrementAssignment(Assignments ns) {
    logger("decrement Assignment:" + ns);
    int i = ns.getDatePos(); //1 based

    if (i < 1)    //#'s are 0 based, can't decrement pos 0
    {
      return;
    }
    deleteAssignment(ns);

    String qry2String = Shifts.createShiftName(ns.getDateOnly(), i - 1);
    ns.setDate(qry2String);
    writeAssignment(ns);
  }

//---------------------------------------------------------------------
//     deleteShift - DELETE Shift assignment for a specified date and index
//---------------------------------------------------------------------
  public void deleteAssignment(Assignments ns) {
    String qryString = ns.getDeleteSQLString(sessionData);
    logger("deleteAssignment" + qryString);
    try {
      PreparedStatement sAssign = connection.prepareStatement(qryString);
      sAssign.executeUpdate();
      ns.setExisted(false);
    }
    catch (SQLException e) {
      errorOut(sessionData, "(" + localResort + ") failed deleteAssignment, reason:" + e.getMessage());
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
      Shifts data = (Shifts) shift;
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
      Shifts data = (Shifts) shift;
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
//System.out.println("in AddShiftsToTable, data.getType()="+data.getType());
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
      int useCount = data.getUseCount();    //get # of patrollers actually assigned to this shift (warn if deleteing!)
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
//System.out.println("in AddAssignmentsToTable, data.getType()="+data.getType());
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

  public ResortData getResortInfo() {
    return resortMap.get(localResort);
  }

  public static String getResortFullName(String resort) {
    if (resortMap.containsKey(resort)) {    //resort string is same as db name, value is full resort string
      return resortMap.get(resort).getResortFullName();
    }
    System.out.println("**** Error, unknown resort (" + resort + ")");
    Thread.currentThread().dumpStack();
    return "Error, invalid resort (" + resort + ")";
  }

  static public String getJDBC_URL(String resort) {
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
    logger("insertNewIndividualAssignment" + qryString);
    try {
      PreparedStatement sAssign = connection.prepareStatement(qryString);
      sAssign.executeUpdate();
    }
    catch (SQLException e) {
      System.out.println("Cannot insert newIndividualAssignment, reason:" + e.getMessage());
      return false;
    }
    return true;
  }

  public boolean updateNewIndividualAssignment(NewIndividualAssignment newIndividualAssignment) {
    String qryString = newIndividualAssignment.getUpdateSQLString(sessionData);
    logger("updateNewIndividualAssignment: " + qryString);
    try {
      PreparedStatement sAssign = connection.prepareStatement(qryString);
      sAssign.executeUpdate();
    }
    catch (SQLException e) {
      errorOut(sessionData, "(" + localResort + ") Cannot update newIndividualAssignment, reason:" + e.getMessage());
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
      errorOut(sessionData, "(" + localResort + ") readNewIndividualAssignments" + e.getMessage());
      return null;
    }
    return results;
  }

  private void logger(String message) {
    if (sessionData != null && sessionData.getRequest() != null) {
      Utils.printToLogFile(sessionData.getRequest(), message);
    }
    else {
      logger(localResort, message);
    }
  }

  public static void logger(String myResort, String message) {
    Utils.printToLogFile(null, "(" + myResort + ") " + message);
  }

  public void deleteNewIndividualAssignment(NewIndividualAssignment newIndividualAssignment) {
    System.out.println("(" + localResort + ") delete Assignment:" + newIndividualAssignment);
    String qryString = newIndividualAssignment.getDeleteSQLString(sessionData);
    try {
      PreparedStatement sAssign = connection.prepareStatement(qryString);
      sAssign.executeUpdate();  //can throw exception
      newIndividualAssignment.setExisted(false);
    }
    catch (Exception e) {
      System.out.println("(" + localResort + ") Cannot delete Shift, reason:" + e.toString());
    }
  }

  public ArrayList<Assignments> readAllSortedAssignments(String patrollerId) {
    String dateMask = "20%"; //WHERE `date_shift_pos` LIKE '2015-10-%'
//    logger("readSortedAssignments(" + dateMask + ")");
    ArrayList<Assignments> monthAssignments = new ArrayList<Assignments>();
    try {
      String qryString = "SELECT * FROM `assignments` WHERE Date like '" + dateMask + "' ORDER BY Date";
      logger("readAllSortedAssignments(" + patrollerId + "): " + qryString);
      PreparedStatement assignmentsStatement = connection.prepareStatement(qryString);
      ResultSet assignmentResults = assignmentsStatement.executeQuery();
//      int cnt = 1;
      while (assignmentResults.next()) {
        Assignments ns = new Assignments();
        ns.read(sessionData, assignmentResults);
        if (ns.includesPatroller(patrollerId)) {
//          logger("(" + (cnt++) + ") NextAssignment-" + ns.toString());
          monthAssignments.add(ns);
        }
      }
    }
    catch (Exception e) {
      System.out.println("(" + localResort + ") Error resetting Assignments table query:" + e.getMessage());
    } //end try
    return monthAssignments;
  }

  public ArrayList<Assignments> readSortedAssignments(int year, int month) {
    String dateMask = String.format("%4d-%02d-", year, month) + "%";
//    logger("  readSortedAssignments(" + dateMask + ")");
    ArrayList<Assignments> monthAssignments = new ArrayList<Assignments>();
    try {
      String qryString = "SELECT * FROM `assignments` WHERE Date like '" + dateMask + "' ORDER BY Date";
      debugOut(null, "readSortedAssignments: " + qryString);
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
      System.out.println("(" + localResort + ") Error resetting Assignments table query:" + e.getMessage());
    } //end try
    return monthAssignments;
  }

  public ArrayList<Assignments> readSortedAssignments(int year, int month, int day) {
    String dateMask = String.format("%4d-%02d-%02d_", year, month, day) + "%";
//    logger("  readSortedAssignments(" + dateMask + ")");
    ArrayList<Assignments> monthAssignments = new ArrayList<Assignments>();
    try {
      String qryString = "SELECT * FROM `assignments` WHERE Date like '" + dateMask + "' ORDER BY Date";
      logger("readSortedAssignments:" + qryString);
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
      System.out.println("(" + localResort + ") Error resetting Assignments table query:" + e.getMessage());
    } //end try
    return monthAssignments;
  }

  private static void debugOut(SessionData sessionData, String msg) {
    if (DEBUG) {
      String localResort = sessionData == null ? "noLoggedInResort" : sessionData.getLoggedInResort();
      HttpServletRequest request = sessionData == null ? null : sessionData.getRequest();
      Utils.printToLogFile(request ,"DEBUG-PatrolData(" + localResort + "): " + msg);
    }
  }

  private static void errorOut(SessionData sessionData, String msg) {
    String localResort = sessionData == null ? "noLoggedInResort" : sessionData.getLoggedInResort();
    HttpServletRequest request = sessionData == null ? null : sessionData.getRequest();
    Utils.printToLogFile(request ,"ERROR-PatrolData(" + localResort + "): " + msg);
  }

}
