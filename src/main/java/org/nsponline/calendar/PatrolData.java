package org.nsponline.calendar;

import java.io.*;
import java.util.*;
import java.lang.*;
import java.sql.*;
import java.text.SimpleDateFormat;

/**
 * @author Steve Gledhill
 */
public class PatrolData {
  final static boolean DEBUG = false;

  //  final static String JDBC_DRIVER = "org.gjt.mm.mysql.Driver"; //todo change July 32 2015
  final static String JDBC_DRIVER = "com.mysql.jdbc.Driver";

  // create a Mountain Standard Time time zone
  final static String[] ids = TimeZone.getAvailableIDs(-7 * 60 * 60 * 1000);
  final static SimpleTimeZone MDT = new SimpleTimeZone(-7 * 60 * 60 * 1000, ids[0]);
  final static String NEW_SHIFT_STYLE = "--New Shift Style--";

  // set up rules for daylight savings time
  static {
    MDT.setStartRule(Calendar.APRIL, 1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);
    MDT.setEndRule(Calendar.OCTOBER, -1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);
  }

  static public HashMap<String, String> resortMap = new HashMap<String, String>();

  static {
    resortMap.put("Afton", "Afton Alps");
    resortMap.put("AlpineMt", "Alpine Mt");
    resortMap.put("Andes", "Andes Tower Hills");
    resortMap.put("Brighton", "Brighton");
    resortMap.put("GrandTarghee", "Grand Targhee");
    resortMap.put("HermonMountain", "Hermon Mountain");
    resortMap.put("KellyCanyon", "Kelly Canyon");
    resortMap.put("LonesomePine", "Lonesome Pine Trails");
    resortMap.put("IFNordic", "IF Nordic");
    resortMap.put("JacksonHole", "Jackson Hole Fire/EMS");
    resortMap.put("JacksonSpecialEvents", "Jackson Hole Fire/EMS Special Events");
    resortMap.put("MountKato", "Mount Kato");
    resortMap.put("PebbleCreek", "Pebble Creek");
    resortMap.put("PineCreek", "Pine Creek");
    resortMap.put("PineMountain", "Pine Mountain");
    resortMap.put("Pomerelle", "Pomerelle");
    resortMap.put("RMSP", "Ragged Mountain");
    resortMap.put("Sample", "Sample Resort");
    resortMap.put("scouts", "Troop 316 Calendar");
    resortMap.put("SoldierHollow", "Soldier Hollow");
    resortMap.put("SoldierMountain", "Soldier Mountain");
    resortMap.put("SnowBird", "SnowBird");
    resortMap.put("SnowCreek", "SnowCreek");
    resortMap.put("SnowCreekPaid", "SnowCreekPaid");
    resortMap.put("SnowKing", "Snow King");
    resortMap.put("ThreeRivers", "Three Rivers Park");
    resortMap.put("UOP", "The Utah Olympic Park");
    resortMap.put("WhitePine", "White Pine");
    resortMap.put("PowderRidge", "Powder Ridge");
    resortMap.put("Willamette", "Willamette Backcountry");
  }

  /* ----- uncomment the following to run from the Internet ------ */
//  final static String IP_ADDRESS = "166.70.236.73";
/* ----- uncomment the following to run local ------*/
//    final static String IP_ADDRESS = "127.0.0.1";   /* used in LoginHelp */
/*----- end local declarations ------*/

  // ***** start back door login stuff (works with ANY resort, and does NOT send any email confermations)*****
  final static String backDoorUser = "sgled57"; //todo use from Properties file
  final static String backDoorPass = "gandalf"; //todo use from Properties file
  final static String backDoorFakeFirstName = "System";
  final static String backDoorFakeLastName = "Administrator";
  final static String backDoorEmail = "Steve@Gledhills.com";  //todo do I really need these??

  final static int MAX_PATROLLERS = 400;
  final static String SERVLET_URL = "/calendar-1/";

/*----- end local declarations ------*/

/* ----- uncomment the following to run local ------ */

  final static int iDaysInMonth[] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
  static final boolean FETCH_MIN_DATA = false;
  static final boolean FETCH_ALL_DATA = true;
/* -----end local declaration-----s */

  //all the folowing instance variables must be initialized in the constructor
  //  private Connection connection;
  Connection connection;
  private ResultSet rosterResults;
  private PreparedStatement assignmentsStatement;
  private ResultSet assignmentResults;
  private PreparedStatement shiftStatement;
  private ResultSet shiftResults;
  private boolean fetchFullData;
  private String localResort;

  public PatrolData(boolean readAllData, String myResort, SessionData sessionData) {
//todo add a user ID in constructor for debug tracking
//System.out.println("**11** database--jdbcURL (" + jdbcURL + ") myResort="+myResort);
    rosterResults = null;
    assignmentsStatement = null;
    assignmentResults = null;
    shiftStatement = null;
    shiftResults = null;
    localResort = myResort;

    // create a Mountain Standard Time time zone
    fetchFullData = readAllData;
    // create a GregorianCalendar with the Pacific Daylight time zone
    // and the current date and time
//      calendar = new GregorianCalendar(MDT);

    try {
////------- the following line works for an applet, but not for a servlet -----
      Driver drv;
      drv = (Driver) Class.forName(JDBC_DRIVER).newInstance();
    }
    catch (Exception e) {
      System.out.println("Cannot load the driver, reason:" + e.toString());
      System.out.println("Most likely the Java class path is incorrect.");
      return;
    }
// Try to connect to the database
    try {
      // Change MyDSN, myUsername and myPassword to your specific DSN
      connection = getConnection(localResort, sessionData);
//..      connection =java.sql.DriverManager.getConnection(getJDBC_URL(localResort));

//prepare SQL for roster
      if (connection != null) //error was already displayed, if null
      {
        resetRoster();
      }

//    connection.close(); // close MUST ba called explicity
    }
    catch (Exception e) {
      System.out.println("(" + localResort + ") Error connecting or reading table on open:" + e.getMessage());
      java.lang.Thread.currentThread().dumpStack();
    } //end try
  } //end PatrolData constructor

  public static String getCurrentDateTimeString() {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    // java.util.Date currentTime = new java.util.Date(year,month,date);
    Calendar cal = new GregorianCalendar(PatrolData.MDT);
    return formatter.format(cal.getTime());
  }

  //--------------
// getConnection - to JDBC connector
// -------------

  static Connection getConnection(String resort, SessionData sessionData) {    //todo get rid of static !!!!!!!!!!
//HACK HACK  HAVE THIS RETURN THE EXCEPTION TO THE PARENT, DON'T THROW THE EXCEPTION
    Connection conn = null;
    try {
//            logger(resort, " -- PatrolData.getConnection");
      if (DEBUG) {
        //write to Tomcat logs, never to screen
        System.out.println("-----getJDBC_URL(" + resort + ")=" + getJDBC_URL(resort));
        System.out.println("-----" + sessionData.getDbUser() + ", " + sessionData.getDbPassword());

      }
      conn = java.sql.DriverManager.getConnection(getJDBC_URL(resort), sessionData.getDbUser(), sessionData.getDbPassword());
      if (DEBUG) {
        System.out.println("PatrolData.connection for " + resort);
      }
    }
    catch (Exception e) {
      System.out.println("Error: " + e.getMessage() + " connecting to table:" + resort);
      java.lang.Thread.currentThread().dumpStack();
    }
    return conn;
  }

  public void resetAssignments() {
    try {
      assignmentsStatement = connection.prepareStatement("SELECT * FROM assignments ORDER BY \"" + Assignments.tag[0] + "\"");   //sort by default key
      assignmentResults = assignmentsStatement.executeQuery();
    }
    catch (Exception e) {
      System.out.println("(" + localResort + ") Error resetting Assignments table query:" + e.getMessage());
    } //end try
  }

  public void resetAssignmentsForDateSortedByStartTime(String yyyy_mm_ddValue) {
    //yyyy_mm_ddValue must have the form 2010-03-01
    try {
      assignmentsStatement = connection.prepareStatement(
          "SELECT * FROM `assignments` WHERE `date` LIKE '" + yyyy_mm_ddValue + "_%' ORDER BY `StartTime`");
      assignmentResults = assignmentsStatement.executeQuery();
    }
    catch (Exception e) {
      System.out.println("(" + localResort + ") Error selecting sorted assignments by date table query:" + e.getMessage());
    } //end try
  }
//---------------

  // readNextAssignment
//---------------
  public Assignments readNextAssignment() {
    Assignments ns = null;
    try {
      if (assignmentResults.next()) {
        ns = new Assignments();
        ns.read(assignmentResults);
      }
    }
    catch (Exception e) {
      System.out.println("(" + localResort + ") Cannot read Assignment, reason:" + e.toString());
      return null;
    }
    return ns;
  }

//---------------

  // resetRoster
//---------------
  public void resetRoster() {
    try {
      PreparedStatement rosterStatement = connection.prepareStatement("SELECT * FROM roster ORDER BY LastName, FirstName");
      rosterResults = rosterStatement.executeQuery();
    }
    catch (Exception e) {
      System.out.println("(" + localResort + ") Error reseting roster table query:" + e.getMessage());
    } //end try
  }

//---------------

  // resetRoster
//---------------
  public void resetRoster(String sort) {
    try {
      PreparedStatement rosterStatement = connection.prepareStatement("SELECT * FROM roster ORDER BY " + sort);
      rosterResults = rosterStatement.executeQuery();
    }
    catch (Exception e) {
      System.out.println("(" + localResort + ") Error reseting roster table query:" + e.getMessage());
    } //end try
  }

//---------------

  // resetShifts
//---------------
  public void resetShifts() {
    try {
      shiftStatement = connection.prepareStatement("SELECT * FROM shiftdefinitions ORDER BY \"" + Shifts.tags[0] + "\""); //sort by default key
      shiftResults = shiftStatement.executeQuery();
    }
    catch (Exception e) {
      System.out.println("(" + localResort + ") Error reseting Shifts table query:" + e.getMessage());
    } //end try
  }

//---------------
// resetDirectorSettings
//---------------
//  public void resetDirectorSettings() {
//      directorResults = DirectorSettings.reset(connection);
//  }

//---------------------------------------------------------------------

  //     writeDirectorSettings - WRITE director settings
//---------------------------------------------------------------------
  public boolean writeDirectorSettings(DirectorSettings ds) {
    return ds.write(connection);
  }

  public DirectorSettings readDirectorSettings() {
    ResultSet directorResults;

//System.out.println("HACK: directorResults.calling reset");
    directorResults = DirectorSettings.reset(connection);
    DirectorSettings ds = null;
//System.out.println("HACK: directorResults starting try");
    try {
//System.out.println("ERROR: directorResults inside try");
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

  public Shifts readNextShift() {
    Shifts ns = null;
    try {
      if (shiftResults.next()) {
        ns = new Shifts();
        ns.read(shiftResults);
      }
    }
    catch (Exception e) {
      System.out.println("(" + localResort + ") Cannot read Shift, reason:" + e.toString());
      return null;
    }
    return ns;
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
    String qry2String = Assignments.createAssignmentName(ns.parsedEventName(), i - 1);
    ns.eventName = qry2String;
    writeShift(ns);
  }

  public void deleteShift(Shifts ns) {
//System.out.println("delete shift:"+ns);
    String qryString = ns.getDeleteSQLString();
    logger(qryString);
    try {
      PreparedStatement sAssign = connection.prepareStatement(qryString);
      int row = sAssign.executeUpdate();
      ns.existed = false;
    }
    catch (Exception e) {
      System.out.println("(" + localResort + ") Cannot delete Shift, reason:" + e.toString());
    }
  }

  public boolean writeShift(Shifts ns) {
    String qryString;
//System.out.println("write shift:"+ns);
    if (ns.exists()) {
      qryString = ns.getUpdateQueryString();
    }
    else {
      qryString = ns.getInsertQueryString();
    }
    logger(qryString);
    try {
      PreparedStatement sAssign = connection.prepareStatement(qryString);
      int row = sAssign.executeUpdate();
    }
    catch (Exception e) {
      System.out.println("(" + localResort + ") Cannot load the driver, reason:" + e.toString());
      System.out.println("Most likely the Java class path is incorrect.");
      return true;
    }
    return false;
  }

  public void close() {
    try {
      if (DEBUG) {
        System.out.println("-- close connection (" + localResort + "): " + getCurrentDateTimeString());
      }
      connection.close(); //let it close in finalizer ??
    }
    catch (Exception e) {
      System.out.println("(" + localResort + ") Error connecting or reading table on close:" + e.getMessage());
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
    catch (Exception e) {
      member = null;
      System.out.println("(" + localResort + ") Error connecting or reading table on nextMember:" + e.getMessage());
      Thread.currentThread().dumpStack();
    } //end try

    return member;
  } //end nextMember

  public MemberData getMemberByID(String szMemberID) {
    MemberData member = null;
//  String str="SELECT * FROM roster WHERE \"IDNumber\" = '"+szMemberID+"'";
    String str = "SELECT * FROM roster WHERE IDNumber =" + szMemberID;
//          String str="SELECT * FROM roster";
    if (szMemberID == null || szMemberID.length() <= 3) {
      return null;
    }
    else if (szMemberID.equals(backDoorUser)) {
      member = new MemberData();  //"&nbsp;" is the default
      if (member != null) {
        member.setLast(backDoorFakeLastName);
        member.setFirst(backDoorFakeFirstName);
        member.setEmail(backDoorEmail);
        member.setID("000000");
        member.setDirector("yes");
      }
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
      member = null;
      System.out.println("(" + localResort + ") Error in getMemberByID(" + szMemberID + "): " + e.getMessage());
      System.out.println("(" + localResort + ") ERROR in PatrolData:getMemberByID(" + szMemberID + ") maybe a close was already done?");
      Thread.currentThread().dumpStack();
    } //end try
    return member;
  } //end getMemberByID

  public MemberData getMemberByEmail(String szEmail) {
    MemberData member = null;
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
      member = null;
      System.out.println("(" + localResort + ") Error in getMemberByEmail(" + szEmail + "): " + e.getMessage());
    } //end try
    return member;
  } //end getMemberByID

  public MemberData getMemberByName(String szFullName) {
    MemberData member = null;
    String str = "SELECT * FROM roster";
    try {
      PreparedStatement rosterStatement = connection.prepareStatement(str);
      rosterResults = rosterStatement.executeQuery();
      while (rosterResults.next()) {
        int id = rosterResults.getInt("IDNumber");
        String str1 = rosterResults.getString("FirstName").trim() + " " +
            rosterResults.getString("LastName").trim();
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
    catch (Exception e) {
      member = null;
      System.out.println("(" + localResort + ") Error reading table in getMemberByName(" + szFullName + "):" + e.getMessage());
      System.out.println("(" + localResort + ") ERROR in PatrolData:getMemberByName(" + szFullName + ") maybe a close was already done?");
      Thread.currentThread().dumpStack();
    } //end try
    return member;
  } //end getMemberByName

  public MemberData getMemberByName2(String szFullName) {
    return getMemberByLastNameFirstName(szFullName);
  } //end getMemberByName

  public MemberData getMemberByLastNameFirstName(String szFullName) {
    MemberData member = null;
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
    catch (Exception e) {
      member = null;
      System.out.println("(" + localResort + ") Error reading table in getMemberByName(" + szFullName + "):" + e.getMessage());
      System.out.println("(" + localResort + ") ERROR in PatrolData:getMemberByName(" + szFullName + ") maybe a close was already done?");
      Thread.currentThread().dumpStack();
    } //end try
//System.out.println("getMemberByLastNameFirstName: returning" + member);
    return member;
  } //end getMemberByName

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
    if (DEBUG) {
      System.out.println("  value= " + val);
    }
    return val;
  }

  public Assignments readAssignment(String myDate) { //formmat yyyy-mm-dd_p
    Assignments ns;
    try {
      assignmentsStatement = connection.prepareStatement("SELECT * FROM assignments WHERE Date=\'" + myDate + "\'");
      assignmentResults = assignmentsStatement.executeQuery();
      ns = readNextAssignment();
    }
    catch (Exception e) {
      System.out.println("Cannot load the driver, reason:" + e.toString());
      System.out.println("Most likely the Java class path is incorrect.");
      return null;
    }
    return ns;
  }

  //---------------------------------------------------------------------
  //  setValidDate - convert yyyy/mm/dd to string format in database
//---------------------------------------------------------------------
  String setValidDate(int currYear, int currMonth, int currDay) {
    String lastValidDate = currYear + "-";
    if (currMonth + 1 < 10) {
      lastValidDate += "0";
    }
    lastValidDate += (currMonth + 1) + "-";
    if (currDay < 10) {
      lastValidDate += "0";
    }
    lastValidDate += currDay;
    return lastValidDate;
  }

  public Assignments readAssignment(int year, int month, int date) { //was readNightSki
    String szDate = setValidDate(year, month - 1, date); //month should be 0 based
    return readAssignment(szDate);
  }

//---------------------------------------------------------------------

  //     writeAssignment - WRITE all night ski assignments for a specified date
//---------------------------------------------------------------------
  public boolean writeAssignment(Assignments ns) { //was writeNightSki
    String qryString;
//System.out.println("in writeAssignment:"+ns.toString());
    if (ns.exists()) {
      qryString = ns.getUpdateQueryString();
    }
    else {
      qryString = ns.getInsertQueryString();
    }
    logger(" writeAssignment: " + qryString);
    try {
      PreparedStatement sAssign = connection.prepareStatement(qryString);
      int row = sAssign.executeUpdate();
    }
    catch (Exception e) {
      System.out.println("(" + localResort + ") Cannot load the driver, reason:" + e.toString());
      System.out.println("(" + localResort + ") Most likely the Java class path is incorrect.");
      return true;
    }
    return false;
  }
//---------------------------------------------------------------------

  //     decrementAssignment -
//---------------------------------------------------------------------
  public void decrementAssignment(Assignments ns) {
    logger("decrement Assignment:" + ns);
//    int i = Integer.parseInt(ns.getDatePos()); //1 based
    int i = ns.getDatePos(); //1 based

    if (i <= 1)    //#'s are 1 based, can't decrement pos 1
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
    logger(" delete Assignment:" + ns);
    String qryString = ns.getDeleteSQLString();
    try {
      PreparedStatement sAssign = connection.prepareStatement(qryString);
      sAssign.executeUpdate();
      ns.setExisted(false);
    }
    catch (Exception e) {
      System.out.println("(" + localResort + ") Cannot delete Shift, reason:" + e.toString());
    }
  }

//--------------------

  // AddShiftsToDropDown
//--------------------
  static public void AddShiftsToDropDown(PrintWriter out, Vector shifts, String selectedShift) {
    String lastName = "";
    String parsedName;
    String selected = "";

    if (selectedShift == null) {
      selected = " selected";
    }
    out.println("                    <option" + selected + ">" + NEW_SHIFT_STYLE + "</option>");
    for (int i = 0; i < shifts.size(); ++i) {
      Shifts data = (Shifts) shifts.get(i);
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
    for (int i = Math.min(1, value); i <= Assignments.MAX; ++i) {
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
  static public void AddShiftsToTable(PrintWriter out, Vector shifts, String selectedShift) {
    int validShifts = 0;
    for (int i = 0; i < shifts.size(); ++i) {
      Shifts data = (Shifts) shifts.get(i);
      String parsedName = data.parsedEventName();
      if (parsedName.equals(selectedShift)) {
//name is if the format of startTime_0, endTime_0, count_0, startTime_1, endTime_1, count_1, etc
// delete_0, delete_1
//shiftCount
        out.println("<tr>");
//delete button
//              out.println("<td width=\"103\"><input onClick=\"DeleteBtn()\" type=\"button\" value=\"Delete\" name=\"delete_"+validShifts+"\"></td>");
        out.println("<td><input type=\"submit\" value=\"Delete\" name=\"delete_" + validShifts + "\"></td>");
        out.println("<td>Start: <input type=\"text\" onKeyDown=\"javascript:return captureEnter(event.keyCode)\" name=\"startTime_" + validShifts + "\" size=\"7\" value=\"" + data.getStartString() + "\"></td>");
        out.println("<td>End: <input type=\"text\" onKeyDown=\"javascript:return captureEnter(event.keyCode)\" name=\"endTime_" + validShifts + "\" size=\"7\" value=\"" + data.getEndString() + "\"></td>");
        out.println("<td>Patroller&nbsp;Count:&nbsp;");
//                out.println("<input type=\"text\" name=\"count_"+validShifts+"\" size=\"4\" value=\""+data.getCount()+"\">");
        countDropDown(out, "count_" + validShifts, data.getCount());
        out.println("</td>");
//add Day/Seing/Night shift
        out.println("<td>&nbsp;");
        out.println("<select size=1 name=\"shift_" + validShifts + "\">");
//System.out.println("in AddShiftsToTable, data.getType()="+data.getType());
        for (int j = 0; j < Assignments.MAX_SHIFT_TYPES; ++j) {
          String sel = (data.getType() == j) ? "selected" : "";
          out.println("<option " + sel + ">" + Assignments.szShiftTypes[j] + "</option>");
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
  static public void AddAssignmentsToTable(PrintWriter out, Vector assignments) {
    int validShifts = 0;
    for (int i = 0; i < assignments.size(); ++i) {
      Assignments data = (Assignments) assignments.elementAt(i);
      String parsedName = data.getEventName();
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
      out.println("<select size=1 name=\"shift_" + validShifts + "\">");
//System.out.println("in AddAssignmentsToTable, data.getType()="+data.getType());
      for (int j = 0; j < Assignments.MAX_SHIFT_TYPES; ++j) {
        String sel = (data.getType() == j) ? "selected" : "";
        out.println("<option " + sel + ">" + Assignments.szShiftTypes[j] + "</option>");
      }
      out.println("</select>");
      out.println("</td>");

      out.println("</tr>");
      ++validShifts;
//            }
    }
    out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"shiftCount\" VALUE=\"" + validShifts + "\">");
  }

  static public boolean validResort(String resort) {
    return resortMap.containsKey(resort);
  }

  static public String getResortFullName(String resort) {
    if (resortMap.containsKey(resort)) {    //resort string is same as db name, value is full resort string
      return resortMap.get(resort);
    }
    System.out.println("**** Error, unknown resort (" + resort + ")");
    Thread.currentThread().dumpStack();
    return "Error, invalid resort (" + resort + ")";
  }

  static public String getJDBC_URL(String resort) {
//    String jdbcLoc = "jdbc:mysql://" + IP_ADDRESS + "/";
    String jdbcLoc = "jdbc:mysql://" + "127.0.0.1" + "/";
    if (validResort(resort)) {
      return jdbcLoc + resort;
    }

    System.out.println("****** Error, unknown resort (" + resort + ")");
    Thread.currentThread().dumpStack();
    return "invalidResort";
  }

  public boolean insertNewIndividualAssignment(NewIndividualAssignment newIndividualAssignment) {
    try {
      System.out.println("ERROR: PatrolData Under Construction need to insert " + newIndividualAssignment);
      String qryString = newIndividualAssignment.getInsertSQLString();
      logger("insert newIndividualAssignment- qryString=" + qryString);
      try {
        PreparedStatement sAssign = connection.prepareStatement(qryString);
        int row = sAssign.executeUpdate();
      }
      catch (Exception e) {
        System.out.println("Cannot insert newIndividualAssignment, reason:" + e.toString());
      }
    }
    catch (Exception e) {
      System.out.println("Error inserting  NewIndividualAssignment, reason:" + e.toString());
      return false;
    }
    return true;
  }

  public boolean updateNewIndividualAssignment(NewIndividualAssignment newIndividualAssignment) {
    try {
      System.out.println("ERROR: PatrolData Under Construction need to update " + newIndividualAssignment);
      String qryString = newIndividualAssignment.getUpdateSQLString();
      logger("update newIndividualAssignment- qryString=" + qryString);
      try {
        PreparedStatement sAssign = connection.prepareStatement(qryString);
        int row = sAssign.executeUpdate();
      }
      catch (Exception e) {
        System.out.println("(" + localResort + ") Cannot update newIndividualAssignment, reason:" + e.toString());
      }
    }
    catch (Exception e) {
      System.out.println("(" + localResort + ") Error updating  NewIndividualAssignment, reason:" + e.toString());
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
      logger(queryString);
      assignmentsStatement = connection.prepareStatement(queryString);
      assignmentResults = assignmentsStatement.executeQuery();
      while (assignmentResults.next()) {
        NewIndividualAssignment newIndividualAssignment = new NewIndividualAssignment();
        newIndividualAssignment.read(assignmentResults);
//System.out.println(newIndividualAssignment);
//System.out.println(newIndividualAssignment.getDateShiftPos());
        results.put(newIndividualAssignment.getDateShiftPos(), newIndividualAssignment);
      }
    }
    catch (Exception e) {
      System.out.println("(" + localResort + ") Cannot load the driver, reason:" + e.toString());
      System.out.println("(" + localResort + ") Most likely the Java class path is incorrect.");
      return null;
    }
    return results;
  }

  private void logger(String message) {
    logger(localResort, message);
  }

  public static void logger(String myResort, String message) {
    System.out.println("(" + myResort + ": " + getCurrentDateTimeString() + ") " + message);
  }

  public void deleteNewIndividualAssignment(NewIndividualAssignment newIndividualAssignment) {
    System.out.println("(" + localResort + ") delete Assignment:" + newIndividualAssignment);
    String qryString = newIndividualAssignment.getDeleteSQLString();
    try {
      PreparedStatement sAssign = connection.prepareStatement(qryString);
      int row = sAssign.executeUpdate();
      newIndividualAssignment.setExisted(false);
    }
    catch (Exception e) {
      System.out.println("(" + localResort + ") Cannot delete Shift, reason:" + e.toString());
    }
  }
}
