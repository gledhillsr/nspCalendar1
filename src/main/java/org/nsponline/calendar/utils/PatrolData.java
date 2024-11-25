package org.nsponline.calendar.utils;

import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import org.nsponline.calendar.store.*;

/**
 * Main method to track general info about a resort (patrol)  Not backed by a table.  But stores
 * general display info about all patrols
 * @author Steve Gledhill
 */
@SuppressWarnings({"SqlNoDataSourceInspection", "AccessStaticViaInstance", "SqlDialectInspection",
  "SpellCheckingInspection", "UnusedReturnValue"})
public class PatrolData {

/* ------------ DEFINE ADDRESS OF MYSQL (for Amazon instances, user PRIVATE address --------- */
  private final static String MYSQL_ADDRESS = "127.0.0.1"; // was "ip-172-31-59-191.ec2.internal";  //private ip PRODUCTION.  must match /etc/my.cnf

  public final static Boolean USING_TESTING_ADDRESS = false;   //used in MonthlyCalendar will add a "TESTING" to the calendar page

  /* ------------------------------------------------------------------------------------------ */

  // ***** start back door login stuff (works with ANY resort, and does NOT send any email confirmations)*****
  private static final String backDoorFakeFirstName = "System";
  private static final String backDoorFakeLastName = "Administrator";
  private static final String backDoorEmail = "Steve@Gledhills.com";

  public static final HashMap<String, ResortData> resortMap = new HashMap<>();
  static private final int IMG_HEIGHT = 80;
  static {
    resortMap.put("Afton",          new ResortData("Afton", "Afton Alps", null, "http://www.aftonalpsskipatrol.org", "/images/AftonLogo.jpg", IMG_HEIGHT, 90));
//    resortMap.put("AlpineMt",       new ResortData("AlpineMt", "Alpine Mt", null, "http://www.alpinemtskipatrol.org", "/images/AlpineMt.jpg", IMG_HEIGHT, 80));
    resortMap.put("Andes",          new ResortData("Andes", "Andes Tower Hills", null, "http://www.andestowerhills.com", "/images/andes_logo.jpg", IMG_HEIGHT, 80));
    resortMap.put("AntelopeButte",  new ResortData("AntelopeButte", "Antelope Butte", null, "http://www.antelopebuttefoundation.org", "/images/AntelopeButte.png", IMG_HEIGHT, 80));
//BigHorn, and changed to Meadowlark (for now, use the BigHorn DB)
    resortMap.put("BigHorn",        new ResortData("BigHorn", "Meadowlark", "Meadowlarkskipatrol@gmail.com", "https://www.lodgesofthebighorns.com/", "/images/Meadowlark.png", IMG_HEIGHT, 80));
    resortMap.put("BigRock",        new ResortData("BigRock", "BigRock", "null", "https://www.bigrockmaine.com", "/images/BigRock.jpeg", IMG_HEIGHT, 80));
    //note Black River Basin was BlackjackMountain
    resortMap.put("BlackjackMountain", new ResortData("BlackjackMountain", "Black River Basin", "Teagan.knudson@snowriver.com", "http://www.snowriver.com", "/images/SnowRiver.png", IMG_HEIGHT, 80));
    resortMap.put("Brighton",       new ResortData("Brighton", "Brighton", "brightonskipatrol@gmail.com", "http://www.brightonresort.com", "/images/Brighton.gif", 60, 261));
    resortMap.put("BuenaVista",     new ResortData("BuenaVista", "Buena Vista", null, "http://www.bvskiarea.com", "/images/BuenaVista.gif", 75, 300));

    resortMap.put("CasperMountain", new ResortData("CasperMountain", "Casper Mountain", "caspermtnskipatrol@gmail.com", "https://www.caspermountainskipatrol.org/", "/images/CasperMountain.png", IMG_HEIGHT, 87));
    resortMap.put("CoffeeMill",     new ResortData("CoffeeMill", "CoffeeMill", null, "https://cm-skipatrol.org/", "/images/CoffeeMillLogo.png", IMG_HEIGHT, 87));
    resortMap.put("DetroitMountain",new ResortData("DetroitMountain", "Detroit Mountain", null, "http://detroitmountain.com/", "/images/DetroitMountain.png", 73, 121));
    resortMap.put("DevilsHead",     new ResortData("DevilsHead", "Devil's Head", "tim.theisen@ieee.org", "https://www.devilsheadresort.com/", "/images/DevilsHeadSkiPatrol.png", IMG_HEIGHT, 160));
    //todo waiting 1/13/21                                                                                             "cushman_dave@yahoo.com"
//    resortMap.put("ElmCreek",       new ResortData("ElmCreek", "Elm Creek Park", null, "https://www.threeriversparks.org/parks/elm-creek-park.aspx", "/images/ThreeRivers.jpg", IMG_HEIGHT, 80));
    resortMap.put("GrandTarghee",   new ResortData("GrandTarghee", "Grand Targhee", null, "http://www.GrandTarghee.com", "/images/GrandTarghee.jpg", IMG_HEIGHT, 80));
    resortMap.put("GrandTargheeHosts", new ResortData("GrandTargheeHosts", "Grand Targhee (Mountain Hosts)", null, "http://www.GrandTarghee.com", "/images/GrandTarghee.jpg", IMG_HEIGHT, 80));
    resortMap.put("GreatDivide",    new ResortData("GreatDivide", "Great Divide", null, "http://www.skigd.com", "/images/GreatDivide.jpg", IMG_HEIGHT, 274));

    resortMap.put("HermonMountain", new ResortData("HermonMountain", "Hermon Mountain", null, "http://www.skihermonmountain.com", "/images/HermonMountain.jpg", IMG_HEIGHT, 80));
    resortMap.put("Hesperus",       new ResortData("Hesperus", "Hesperus", null, "http://www.ski-hesperus.com/", "/images/Hesperus.jpg", 84, 192));
    resortMap.put("HolidayMountain",       new ResortData("HolidayMountain", "Holiday Mountain", null, "http://www.skiholidaymtn.com/", "/images/SkiHolidayMtn.jpg", IMG_HEIGHT, 185));
//    resortMap.put("HiddenValley",   new ResortData("HiddenValley", "Hidden Valley", null, "http://www.hvsp.org/", "/images/HiddenValley.jpg", IMG_HEIGHT, 80));
    resortMap.put("HylandHills",    new ResortData("HylandHills", "Hyland Hills Park", null, " https://threeriversparks.org/parks/hyland-lake-park/hyland-hills-ski-area.aspx", "/images/ThreeRivers.jpg", IMG_HEIGHT, 80));
    resortMap.put("IFNordic",       new ResortData("IFNordic", "IF Nordic", null, "", "/images/IFNordic.gif", IMG_HEIGHT, 80));
//Jackson Creek Summit email was "Skipatrol@bigsnow.com"
//Jackson Creek Summit was IndianHeadMountain
    resortMap.put("IndianHeadMountain", new ResortData("IndianHeadMountain", "Jackson Creek Summit", "Teagan.knudson@snowriver.com", "http://www.snowriver.com", "/images/SnowRiver.png", IMG_HEIGHT, 80));
    resortMap.put("KellyCanyon",    new ResortData("KellyCanyon", "Kelly Canyon", null, "http://www.SkiKelly.com", "/images/KellyCanyon.jpg", IMG_HEIGHT, 80));
    resortMap.put("LeeCanyon",      new ResortData("LeeCanyon", "Lee Canyon", "schedule@leecanyonskipatrol.org", "http://www.leecanyonskipatrol.org", "/images/LeeCanyon.png", IMG_HEIGHT, 80));
    resortMap.put("LonesomePine",   new ResortData("LonesomePine", "Lonesome Pine Trails", null, "http://www.lonesomepines.org", "/images/lonesomepines.gif", IMG_HEIGHT, 80));
    resortMap.put("MagicMountain",  new ResortData("MagicMountain", "Magic Mountain", null, "http://www.magicmountainresort.com/", "/images/MagicMountain.jpg", IMG_HEIGHT, 145));
    resortMap.put("psiaMagicMountain",  new ResortData("psiaMagicMountain", "Magic Mountain Snowsports School", "jwkluth@gmail.com", "http://www.magicmountainresort.com/", "/images/psia.png", IMG_HEIGHT, 80));
    resortMap.put("MountKato",      new ResortData("MountKato", "Mount Kato", null, "http://www.mtkatoskipatrol.com", "/images/MountKato.jpg", IMG_HEIGHT, 80));
    resortMap.put("MountPleasant",  new ResortData("MountPleasant", "Mount Pleasant", "dfarbotnik@gmail.com", "http://www.skimountpleasant.com ", "/images/MountPleasant.png", IMG_HEIGHT, 80));

//    resortMap.put("NorwayMountain", new ResortData("NorwayMountain", "Norway Mountain", null, "http://www.NorwayMountain.com", "/images/NorwayMountain.jpg", IMG_HEIGHT, 80));
    resortMap.put("NorwayMountain", new ResortData("NorwayMountain", "Norway Mountain", null, "http://www.norwaymt.com", "/images/Norway.gif", IMG_HEIGHT, 80));
    resortMap.put("PaidSnowCreek",  new ResortData("PaidSnowCreek", "Paid SnowCreek", null, "http://www.skisnowcreek.com", "/images/SnowCreek.jpg", IMG_HEIGHT, 80));
    resortMap.put("PaulBunyan",  new ResortData("PaulBunyan", "Paul Bunyan", null, "http://www.nspOnline.org", "/images/PaulBunyan.jpg", IMG_HEIGHT, 80));
//    resortMap.put("PebbleCreek",    new ResortData("PebbleCreek", "Pebble Creek", null, "http://www.pebblecreekskiarea.com", "/images/PebbleCreek.gif", IMG_HEIGHT, 80));
    resortMap.put("PineCreek",      new ResortData("PineCreek", "Pine Creek", null, "http://www.pinecreekskiresort.com", "/images/pinecreek.gif", IMG_HEIGHT, 80));
    resortMap.put("PineMountain",   new ResortData("PineMountain", "Pine Mountain", "schedule@PineMountainSkiPatrol.com", "http://www.PineMountainResort.com", "/images/PineMtnLogo.png", IMG_HEIGHT, 80));
    resortMap.put("psiaPineMountain",new ResortData("psiaPineMountain", "Pine Mountain (Ski Instructors)", "jwkluth@gmail.com", "http://www.PineMountainResort.com", "/images/psiaPineMountain.png", IMG_HEIGHT, 80));
    resortMap.put("Plattekill",     new ResortData("Plattekill", "Plattekill Mountain", null, "http://plattekill.com/", "/images/PlattekillLogo.png", IMG_HEIGHT, 147));
    resortMap.put("Pomerelle",      new ResortData("Pomerelle", "Pomerelle", null, "http://www.pomerelle-mtn.com", "/images/PomerelleLogo.gif", IMG_HEIGHT, 80));
    resortMap.put("RMSP",           new ResortData("RMSP", "Ragged Mountain", null, "http://www.rmskipatrol.com", "/images/RMSP_logo.JPG", IMG_HEIGHT, 80));
    //todo fix passwords in Sample
    resortMap.put("Sample",         new ResortData("Sample", "Sample Resort", null, "http://www.nspOnline.org", "/images/NSP_logo.gif", IMG_HEIGHT, 80));
    resortMap.put("Snowbowl",      new ResortData("Snowbowl", "Snowbowl", null, "http://www.snowbowlskipatrol.org", "/images/SnowBowlLogo.jpg", 80, 80));
    resortMap.put("SnowCreek",      new ResortData("SnowCreek", "SnowCreek", null, "http://www.skisnowcreek.com", "/images/SnowCreek.jpg", IMG_HEIGHT, 80));
    resortMap.put("SnowKing",       new ResortData("SnowKing", "SnowKing", null, "http://www.SnowKing.com", "/images/SnowKing.jpg", IMG_HEIGHT, 80));
//    resortMap.put("SoldierHollow", new ResortData("SoldierHollow", "Soldier Hollow", null, "http://utaholympiclegacy.org/soldier-hollow/", "/images/SOHO_II.jpg", IMG_HEIGHT, 60));
    resortMap.put("SoldierMountain", new ResortData("SoldierMountain", "Soldier Mountain", null, "http://www.soldiermountain.com", "/images/SoldierMountain.gif", IMG_HEIGHT, 80));
    resortMap.put("Steeplechase", new ResortData("Steeplechase", "Steeplechase", null, "http://www.steeplechaseevents.com", "/images/Steeplechase.png", IMG_HEIGHT, 80));

//    resortMap.put("WelchVillage",   new ResortData("WelchVillage", "Welch Village", null, "http://www.welchvillage.com", "/images/WelchVillage.jpg", IMG_HEIGHT, 80));
    resortMap.put("WhitePine",      new ResortData("WhitePine", "White Pine", "gawilson@wyoming.com", "http://www.WhitePineSki.com", "/images/WhitePine.jpg", IMG_HEIGHT, 80));
//    resortMap.put("WildMountain",     new ResortData("WildMountain", "Wild Mountain", null, "http://www.wildmountain.com/", "/images/WildMountain.jpeg", IMG_HEIGHT, 169));
    resortMap.put("Willamette",     new ResortData("Willamette", "Willamette Backcountry", null, "http://www.deetour.net/wbsp", "/images/Willamette.jpeg", IMG_HEIGHT, 80));
  }

  private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
  public static final String newShiftStyle = "--New Shift Style--";

  // create a Mountain Standard Time time zone
  public static final String NEW_SHIFT_STYLE = "--New Shift Style--";

  public static final int MAX_PATROLLERS = 400; //todo hack fix me
  public static final String SERVLET_URL = "/calendar-1/";

  //all the folowing instance variables must be initialized in the constructor
  //  private Connection connection;
  private Connection connection;
  private PreparedStatement shiftStatement;
  private final String localResort;
  private final SessionData sessionData;
  private final Logger LOG;

  public PatrolData(String myResort, SessionData sessionData, final Logger parentLogger) {
//    LOG = new Logger(PatrolData.class, parentLogger, myResort, MIN_LOG_LEVEL);
    LOG = parentLogger;
    this.sessionData = sessionData;
    shiftStatement = null;
    localResort = myResort;

    try {
      Class.forName(JDBC_DRIVER).newInstance();
    }
    catch (Exception e) {
      logError("Cannot load the driver, reason:" + e.toString());
      logError("Most likely the Java class path is incorrect.");
      //todo do something here. besides throw a NPE later
      return;
    }
    connection = getConnection(localResort, sessionData, LOG);

    if (connection == null) //error was already displayed, if null
    {
      logError("getConnection(" + localResort + ", sessionData) failed.");
      //todo do something here. besides throw a NPE later
    }
  } //end PatrolData constructor

  public Connection getConnection() {
    return connection;
  }

  public static Connection getConnection(String resort, SessionData sessionData, Logger LOG) {    //todo get rid of static !!!!!!!!!!
    Connection conn = null;
    try {
      conn = java.sql.DriverManager.getConnection(getJDBC_URL(resort, LOG), sessionData.getDbUser(), sessionData.getDbPassword());
//      debugOutStatic(sessionData, "PatrolData.connection " + ((conn == null) ? "FAILED" : "SUCCEEDED") + " for " + getJDBC_URL(resort));
    }
    catch (Exception e) {
      LOG.error("Error: " + e.getMessage() + " connecting to table:" + resort);
      java.lang.Thread.currentThread().dumpStack();
    }
    return conn;
  }

  public ResultSet resetAssignments() {
    try {
      String selectAllAssignmentsByDateSQLString = Assignments.getSelectAllAssignmentsByDateSQLString(LOG);
      LOG.logSqlStatement( selectAllAssignmentsByDateSQLString + " (resetAssignments)");
      PreparedStatement assignmentsStatement = connection.prepareStatement(selectAllAssignmentsByDateSQLString);
      return assignmentsStatement.executeQuery();
    }
    catch (Exception e) {
      logError(" Error (line 167) resetting Assignments table query:" + e.getMessage());
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
      if (assignmentResults.next()) {
        ns = new Assignments(LOG);
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
      LOG.logSqlStatement(sqlQuery + " (resetRoster) ");
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
      String sqlQuery = "SELECT * FROM roster ORDER BY " + sort;
      LOG.logSqlStatement(sqlQuery + " (resetRoster(sort)) ");
      PreparedStatement rosterStatement = connection.prepareStatement(sqlQuery);
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

  final static String READ_SHIFT_DEFINITIONS_QUERY = "SELECT * FROM `shiftdefinitions` ORDER BY `shiftdefinitions`.`EventName` ASC";

  public ArrayList<ShiftDefinitions> readShiftDefinitions() {
    ArrayList<ShiftDefinitions> shiftDefinitions = new ArrayList<>();
    StringBuilder strBuilder = new StringBuilder();
    try {
      strBuilder.append(".");
      LOG.logSqlStatement(READ_SHIFT_DEFINITIONS_QUERY);
      strBuilder.append("a");
      PreparedStatement assignmentsStatement = connection.prepareStatement(READ_SHIFT_DEFINITIONS_QUERY);
      strBuilder.append("b");
      ResultSet assignmentResults = assignmentsStatement.executeQuery();
      strBuilder.append("c");

      while (assignmentResults.next()) {
        strBuilder.append("e");
        ShiftDefinitions ns = new ShiftDefinitions(LOG);
        strBuilder.append("f");
        ns.read(assignmentResults);
        strBuilder.append("g");
        shiftDefinitions.add(ns);
      }
    }
    catch (Exception e) {
      LOG.logException(" Error (PatrolData line 283 strBuilder=" + strBuilder.toString() + ") resetting Assignments table ", e);
    } //end try
    return shiftDefinitions;
  }

  public void decrementShift(ShiftDefinitions ns) {
    LOG.debug("decrement shift:" + ns);
    int i = ns.getEventIndex();
    LOG.debug("event index =" + i);

    if (i == 0) {
      return;
    }
    deleteShift(ns);
    ns.setEventName(Assignments.createAssignmentName(ns.parsedEventName(), i - 1));
    writeShift(ns);
  }

  public void deleteShift(ShiftDefinitions ns) {
    String qryString = ns.getDeleteSQLString(localResort);
    LOG.logSqlStatement(qryString + "  deleteShift");
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
    LOG.logSqlStatement(qryString + "  writeShift");
    try {
      PreparedStatement sAssign = connection.prepareStatement(qryString);
      sAssign.executeUpdate();
      ns.setExists(true);
    }
    catch (SQLException e) {
      logError( " failed writeShift, exception=" + e.getMessage());
      return true;
    }
    return false;
  }

  public void close() {
    try {
//      LOG.debug("-- close connection");
      if (connection != null) {
        connection.close(); //let it close in finalizer ??
      }
    }
    catch (Exception e) {
      LOG.logException(" Error closing connection:", e);
      Thread.currentThread().dumpStack();
    } //end try
  } // end close

  @SuppressWarnings("ConstantConditions")
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
    if (szMemberID == null || !szMemberID.matches("[0-9a-z]{5,7}")) {
      LOG.error("called getMemberByID with bogus memberID=[" + szMemberID + "]");
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
    if (szMemberID.matches("[0-9]{5,6}")) {
      try (PreparedStatement rosterStatement = connection.prepareStatement(str)) {
        ResultSet rosterResults = rosterStatement.executeQuery();
        if (rosterResults.next()) {
          member = new Roster(LOG);  //"&nbsp;" is the default
          member.readFullFromRoster(rosterResults, "");
          return member;
        } //end while
      }
      catch (Exception e) {
        LOG.logException("caught Error in getMemberByID memberID=[" + szMemberID + "] ", e);
        //noinspection AccessStaticViaInstance
        Thread.currentThread().dumpStack();
      } //end try
    }
    LOG.error("called getMemberByID with invalid memberID=[" + szMemberID + "]");
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
        member.readFullFromRoster(rosterResults, "");
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
        str1 = str1.replaceAll("''", "'"); //IMPORTANT, UX displays ', but DB stores as ''.  So map DB to UX
//Log.log("getMemberByLastNameFirstName: (" + szFullName + ") (" + str1 + ") cmp=" + str1.equals(szFullName));
        //noinspection Duplicates
        if (str1.equals(szFullName)) {
          member = new Roster(LOG);  //"&nbsp;" is the default
          member.readFullFromRoster(rosterResults, "");
          return member;
        }
      } //end while
    }
    catch (SQLException e) {
      logError("(" + localResort + ") failed getMemberByLastNameFirstName(" + szFullName + "):" + e.getMessage());
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
      String queryString = "SELECT * FROM assignments WHERE Date='" + myDate + "'";
      LOG.logSqlStatement(queryString);
      PreparedStatement assignmentsStatementLocal = connection.prepareStatement(queryString);
      ResultSet assignmentResultsLocal = assignmentsStatementLocal.executeQuery();

        if (assignmentResultsLocal.next()) {
          ns = new Assignments(LOG);
          ns.read(sessionData, assignmentResultsLocal);
          LOG.logSqlStatement(queryString);
          return ns;
        }
    }
    catch (SQLException e) {
      logError("failed readAssignment, reason:" + e.getMessage());
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
      qryString = ns.getInsertQueryString();
    }
    try {
      PreparedStatement sAssign = connection.prepareStatement(qryString);
      sAssign.executeUpdate();
    }
    catch (SQLException e) {
      logError("(" + localResort + ") failed writeAssignment, reason:" + e.getMessage());
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
//    LOG.info("deleteAssignment" + qryString);
    try {
      PreparedStatement sAssign = connection.prepareStatement(qryString);
      sAssign.executeUpdate();
      ns.setExisted(false);
    }
    catch (SQLException e) {
      logError("(" + localResort + ") failed deleteAssignment, reason:" + e.getMessage());
    }
  }

//--------------------
// AddShiftsToDropDown
//--------------------
  static public void AddShiftsToDropDown(PrintWriter out, ArrayList<ShiftDefinitions> shifts, String selectedShift) {
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
  static public void AddShiftsToTable(PrintWriter out, ArrayList<ShiftDefinitions> shifts, String selectedShift) {
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
        out.println("<td>Start: <input type='text' onKeyDown='javascript:return captureEnter(event.keyCode)' maxlength='11' name='startTime_" + validShifts + "' size='7' value='" + data.getStartString() + "'></td>");
        out.println("<td>End: <input type='text' onKeyDown='javascript:return captureEnter(event.keyCode)' maxlength='11' name='endTime_" + validShifts + "' size='7' value='" + data.getEndString() + "'></td>");
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
  static public void AddAssignmentsToTable(PrintWriter out, ArrayList<Assignments> parameterAssignments) {
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
      out.println("<td>Start: <input type=\"text\" maxlength=\"11\" name=\"startTime_" + validShifts + "\" onKeyDown=\"javascript:return captureEnter(event.keyCode)\" size=\"7\" value=\"" + data.getStartingTimeString() + "\"></td>");
      out.println("<td>End: <input type=\"text\" maxlength=\"11\" name=\"endTime_" + validShifts + "\" onKeyDown=\"javascript:return captureEnter(event.keyCode)\" size=\"7\" value=\"" + data.getEndingTimeString() + "\"></td>");
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
    out.println("<INPUT TYPE='HIDDEN' NAME='shiftCount' VALUE='" + validShifts + "'>");
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

  public static String getResortFullName(String resort, Logger LOG) {
    if (resortMap.containsKey(resort)) {    //resort string is same as db name, value is full resort string
      return resortMap.get(resort).getResortFullName();
    }
    LOG.error("**** Error, unknown resort (" + resort + ")");
    Thread.currentThread().dumpStack();
    return "Error, invalid resort (" + resort + ")";
  }

  private static String getJDBC_URL(String resort, Logger LOG) {
    String jdbcLoc = "jdbc:mysql://" + MYSQL_ADDRESS + "/";
    if (isValidResort(resort)) {
      return jdbcLoc + resort;
    }

    LOG.error("****** Error, unknown resort (" + resort + ")");
    Thread.currentThread().dumpStack();
    return "invalidResort";
  }

  public boolean insertNewIndividualAssignment( NewIndividualAssignment newIndividualAssignment) {
    String qryString = newIndividualAssignment.getInsertSQLString();
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
    String qryString = newIndividualAssignment.getUpdateSQLString();
    LOG.info("updateNewIndividualAssignment: " + qryString);
    try {
      PreparedStatement sAssign = connection.prepareStatement(qryString);
      sAssign.executeUpdate();
    }
    catch (SQLException e) {
      logError("(" + localResort + ") Cannot update newIndividualAssignment, reason:" + e.getMessage());
      return false;
    }
    return true;
  }

  public HashMap<String, NewIndividualAssignment> readNewIndividualAssignments(int year, int month, int day) { //formmat yyyy-mm-dd_i_n
    HashMap<String, NewIndividualAssignment> results = new HashMap<>();
    String key = NewIndividualAssignment.buildKey(year, month, day, -1, -1);
    key += "%";
    //SELECT * FROM `newindividualassignment` WHERE `date_shift_pos` LIKE "2009-02-07%"
    try {
      String queryString = "SELECT * FROM `newindividualassignment` WHERE `date_shift_pos` LIKE '" + key + "'";
      LOG.logSqlStatement(queryString);
      PreparedStatement assignmentsStatement = connection.prepareStatement(queryString);
      ResultSet assignmentResults = assignmentsStatement.executeQuery();
      while (assignmentResults.next()) {
        NewIndividualAssignment newIndividualAssignment = new NewIndividualAssignment();
        newIndividualAssignment.read(assignmentResults);
//        debugOut(newIndividualAssignment.toString());
        results.put(newIndividualAssignment.getDateShiftPos(), newIndividualAssignment);
      }
    }
    catch (SQLException e) {
      logError("(" + localResort + ") readNewIndividualAssignments" + e.getMessage());
      return null;
    }
    return results;
  }

  public void deleteNewIndividualAssignment(NewIndividualAssignment newIndividualAssignment) {
    String qryString = newIndividualAssignment.getDeleteSQLString();
    try {
      PreparedStatement sAssign = connection.prepareStatement(qryString);
      sAssign.executeUpdate();  //can throw exception
      newIndividualAssignment.setExisted(false);
    }
    catch (Exception e) {
      LOG.error("(" + localResort + ") Cannot delete Shift, reason:" + e.toString());
    }
  }

  public ArrayList<Assignments> readAllSortedAssignments(String patrollerId) {
    String dateMask = "20%"; //WHERE `date_shift_pos` LIKE '2015-10-%'
//    logger("readSortedAssignments(" + dateMask + ")");
    ArrayList<Assignments> monthAssignments = new ArrayList<>();
    try {
      String qryString = "SELECT * FROM `assignments` WHERE Date like '" + dateMask + "' ORDER BY Date";
      LOG.logSqlStatement(qryString);
      PreparedStatement assignmentsStatement = connection.prepareStatement(qryString);
      ResultSet assignmentResults = assignmentsStatement.executeQuery();
//      int cnt = 1;
      while (assignmentResults.next()) {
        Assignments assignments = new Assignments(LOG);
        assignments.read(sessionData, assignmentResults);
        if (assignments.includesPatroller(patrollerId)) {
//          logger("(" + (cnt++) + ") NextAssignment-" + ns.toString());
          monthAssignments.add(assignments);
        }
      }
    }
    catch (Exception e) {
      LOG.logException(" Error (PatrolData line 821) resetting Assignments table", e);
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
    ArrayList<Assignments> filteredAssignments = new ArrayList<>();
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
    ArrayList<Assignments> monthAssignments = new ArrayList<>();
    try {
      readSortedAssignments(dateMask, monthAssignments);
    }
    catch (Exception e) {
      LOG.logException("Error (line 864) resetting Assignments table", e);
    } //end try
    return monthAssignments;
  }

  public ArrayList<Assignments> readSortedAssignments(int year, int month, int day) {
    String dateMask = String.format("%4d-%02d-%02d_", year, month, day) + "%";
//    logger("  readSortedAssignments(" + dateMask + ")");
    ArrayList<Assignments> monthAssignments = new ArrayList<>();
    try {
      readSortedAssignments(dateMask, monthAssignments);
    }
    catch (Exception e) {
      LOG.logException("Error (line 887) resetting Assignments table", e);
    } //end try
    return monthAssignments;
  }

  private void readSortedAssignments(String dateMask, ArrayList<Assignments> monthAssignments) throws SQLException {
    String qryString = "SELECT * FROM `assignments` WHERE Date like '" + dateMask + "' ORDER BY Date";
    LOG.logSqlStatement(qryString);
    PreparedStatement assignmentsStatement = connection.prepareStatement(qryString);
    ResultSet assignmentResults = assignmentsStatement.executeQuery();

    while (assignmentResults.next()) {
      Assignments ns = new Assignments(LOG);
      ns.read(sessionData, assignmentResults);
//        logger(".. NextAssignment-" + ns.toString());
      monthAssignments.add(ns);
    }
  }

  private void logError(String msg) {
    LOG.error(msg);
  }

  public boolean isValidLogin(PrintWriter out, String resort, String ID, String incomingPass, SessionData sessionData) {
    boolean validLogin = false;
    ResultSet rs;
    if (ID == null || incomingPass == null) {
      LOG.error("Login Failed: either ID (" + ID + ") or Password not supplied");
      return false;
    }
    LOG.debug("LoginHelp: isValidLogin("+resort + ", " + ID + ")");

    try {
      //noinspection unused
      Driver drv = (Driver) Class.forName(PatrolData.JDBC_DRIVER).newInstance();
    }
    catch (Exception e) {
      LOG.logException("LoginHelp: Cannot find mysql driver:", e);
      return false;
    }

    if (ID.equalsIgnoreCase(sessionData.getBackDoorUser()) && incomingPass.equalsIgnoreCase(sessionData.getBackDoorPassword())) {
      return true;
    }    // Try to connect to the database
    try {
      // Change MyDSN, myUsername and myPassword to your specific DSN
      Connection c = PatrolData.getConnection(resort, sessionData, LOG);
      @SuppressWarnings("SqlNoDataSourceInspection")
      String szQuery = "SELECT * FROM roster WHERE IDNumber = \"" + ID + "\"";
      LOG.info("(login) " + szQuery);
      PreparedStatement sRost = c.prepareStatement(szQuery);
      if (sRost == null) {
        return false;
      }

      rs = sRost.executeQuery();

      if (rs != null && rs.next()) {      //will only loop 1 time

        String originalPassword = rs.getString("password").trim();
        String hashedPassword = rs.getString("newPassword");
        String lastName = rs.getString("LastName");
        String firstName = rs.getString("FirstName");
        String emailAddress = rs.getString("email");
        originalPassword = originalPassword.trim();
        lastName = lastName.trim();
        incomingPass = incomingPass.trim();

        boolean hasPassword = (originalPassword.length() > 0);
        if (hashedPassword != null && hashedPassword.length() > 120) {
          SaltUtils saltShaker = new SaltUtils();
          String newHash = saltShaker.hashPassword(sessionData, incomingPass);
          validLogin = hashedPassword.equals(newHash);
//          LOG.warn("DEBUG newHash=" + newHash + ", hashedPassword=" + hashedPassword + (hasPassword ? " OLD PASSWORD STILL EXISTS" : " NO old password exists"));
        }
        else if (hasPassword) {
          if (originalPassword.equalsIgnoreCase(incomingPass)) {
            validLogin = true;
            LOG.warn("DEBUG using old password");
          }
        }
        else {
          if (lastName.equalsIgnoreCase(incomingPass)) {
            validLogin = true;
            LOG.warn("DEBUG using Last name as password");
          }
        }

        if (validLogin) {
          LOG.info("Login Sucessful: " + firstName + " " + lastName + ", " + ID + " (" + resort + ") " + emailAddress);
        }
        else {
          LOG.error( "sleep(500).  Login Failed: ID=[" + ID + "] LastName=[" + lastName + "] suppliedPass=[" + incomingPass + "]");
          Thread.sleep(500);
        }
      }
      else {
        LOG.error("sleep(500).  Login Failed: memberId not found [" + ID + "]");
        Thread.sleep(500);
      }

      c.close();
    }
    catch (Exception e) {
      if (e instanceof NoSuchAlgorithmException) {
        LOG.error("LoginHelp. Error initalizing salt:" + e.getMessage());
      } else {
        LOG.error("LoginHelp. Error connecting or reading table:" + e.getMessage());
      }
    } //end try

    return validLogin;
  }
}
