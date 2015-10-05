package org.nsponline.calendar;

import java.sql.ResultSet;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;

/**
 * @author Steve Gledhill
 */
public class Assignments {
  private final static boolean DEBUG = false;
  //public fields
  final static int MAX_ASSIGNMENT_SIZE = 10;          //max number of assignments
  final static int DAY_TYPE = 0;
  final static int SWING_TYPE = 1;
  final static int NIGHT_TYPE = 2;
  final static int TRAINING_TYPE = 3;
  final static int MAX_SHIFT_TYPES = 4;

  //private fields
  final private static String szShiftTypes[] = {"Day Shift", "Swing Shift", "Night Shift", "Training Shift"};
  final private static String tag[] = {"Date", "StartTime", "EndTime", "EventName", "ShiftType",
      "Count", "P0", "P1", "P2", "P3", "P4", "P5", "P6", "P7", "P8", "P9"}; //string on form
  //NOTE: the Date string is in this format "2001-11-03_1"  where _1 is the FIRST record
  final private static int DATE_INDEX = 0;
  final private static int START_INDEX = 1;
  final private static int END_INDEX = 2;
  final private static int EVENT_INDEX = 3;
  final private static int TYPE_INDEX = 4;
  final private static int COUNT_INDEX = 5;
  final private static int P0_INDEX = 6;
  // Format the current time.
//E=day of Week (ie. Tuesday), y=year, M=month, d=day, H=hour (24 hour clock), m=minute, s=second
  final private static SimpleDateFormat expandedDateTimeFormatter = new SimpleDateFormat("EEEE, MMMM d yyyy");
  final private static SimpleDateFormat DateFormatter = new SimpleDateFormat("yyyy-MM-dd");
//  final static SimpleDateFormat normalDateFormatter = new SimpleDateFormat("MM'/'dd'/'yyyy");

  // ALL the following global data MUST be initialized in the constructor
  private String szDate;
  private String szStartTime, szEndTime, szEventName;
  private int type;
  private int count;
  private String patrollerID[];           //patroller ID for each assignment
  private boolean existed;                //did this date already exist in the database
  private boolean exceptionError;

  public Assignments() {
    initData();
  }

  public Assignments(String myDate, Shifts shift) {
    initData();
    szEventName = " ";   //shift.getEventName();
    szDate = myDate;        //date of this shift
    szStartTime = shift.getStartString();
    szEndTime = shift.getEndString();
    type = shift.getType();
    count = shift.getCount();
  }

  private void initData() {
    int i;
    exceptionError = false;
    existed = false;    //not real data
    patrollerID = new String[MAX_ASSIGNMENT_SIZE];  //allocate ID array
    count = 0;
    type = DAY_TYPE;
    szDate = szStartTime = szEndTime = szEventName = null;
    for (i = 0; i < MAX_ASSIGNMENT_SIZE; ++i) {
      patrollerID[i] = "0";   //no patroller assigned
    }
  }

  private static String getDateSqlTag() {  //used for ORDER BY
    return tag[0];
  }

  public static String getShiftName(int index) {
    return szShiftTypes[index];
  }

  private String readString(ResultSet assignmentResults, String tag) {
    String str = null;
    try {
      str = assignmentResults.getString(tag);
    }
    catch (Exception e) {
      LOG("exception in readString e=" + e);
      exceptionError = true;
    } //end try

    return str;
  }

  public boolean read(ResultSet assignmentResults) {
    exceptionError = false;
    szDate = readString(assignmentResults, tag[DATE_INDEX]);
    szStartTime = readString(assignmentResults, tag[START_INDEX]);
    szEndTime = readString(assignmentResults, tag[END_INDEX]);
    szEventName = readString(assignmentResults, tag[EVENT_INDEX]);
    debugOut("szEventName=(" + szEventName + ")");
    if (szEventName == null || szEventName.equals("null")) {
      szEventName = " ";
    }
    String str = readString(assignmentResults, tag[TYPE_INDEX]);
    type = Integer.parseInt(str);
    str = readString(assignmentResults, tag[COUNT_INDEX]);
    count = Integer.parseInt(str);
    for (int i = 0; i < MAX_ASSIGNMENT_SIZE; ++i) {
      patrollerID[i] = readString(assignmentResults, ("P" + i));
    }
    existed = true;
    debugOut("Assignment.read=" + this);
    return !exceptionError;
  }

  //szDate is in this format "yyyy-mm-dd_p"
  public int getYear() {
    return Integer.parseInt(szDate.substring(0, 4));
  }

  public int getMonth() {
    return Integer.parseInt(szDate.substring(5, 7));
  }

  public int getDay() {
    return Integer.parseInt(szDate.substring(8, 10));
  }

  public int getDatePos() {
    String pos = szDate.substring(11);
    return PatrolData.StringToIndex(pos);
  }

  public String getMyFormattedDate() { //new format is mm/dd/yyyy
    return szDate.substring(5, 7) + "/" + szDate.substring(8, 10) + "/" + szDate.substring(0, 4);
  }

  public String getStartingTimeString() {
    return szStartTime;
  }

  public String getEndingTimeString() {
    return szEndTime;
  }

  public String getEventName() {
    return szEventName;
  }

  public int getType() {
    return type;
  }

  public int getCount() {
    return count;
  }

  public boolean exists() {
    return existed;
  }

  public String getDateOnly() {
    return szDate.substring(0, 10);
  }

  public String getDate() {
    return szDate;
  }

  public static int getTypeID(String szType) {
    if (szType == null) {
      LOG("** Assignments.getDate error, type was null");
      return DAY_TYPE;
    }
    for (int shiftType = 0; shiftType < MAX_SHIFT_TYPES; ++shiftType) {
      if (szType.equals(szShiftTypes[shiftType])) {
        return shiftType;
      }
    }
    LOG("** Assignments.getDate error, invalid string");
    return DAY_TYPE;
  }

  public static String createAssignmentName(String name, int i) {
    return name + "_" + PatrolData.IndexToString(i);
  }

  public int getUseCount() {
    debugOut("Assignments.getUseCount() for " + szDate + ", fullCount=" + count);
    int cnt = 0;
    int i;
    for (i = 0; i < count; ++i) {
      if (patrollerID[i] != null && !patrollerID[i].equals("0") && !patrollerID[i].equals("")) {
        ++cnt;
        debugOut("Found " + cnt + ") patroller=(" + patrollerID[i] + ")");
      }
    }
    return cnt;
  }

  public void setDate(String newDate) {
    szDate = newDate;
  }

  public void setStartTime(String str) {
    szStartTime = str;
  }

  public void setEndTime(String str) {
    szEndTime = str;
  }

  public void setEventName(String str) {
    szEventName = str;
    debugOut("1 szEventName=(" + szEventName + ")");
  }

  public void setType(int cnt) {
    type = cnt;
  }

  public void setCount(int cnt) {
    count = cnt;
  }

  public void setExisted() {
    existed = true;
  }

  public void setExisted(boolean flag) {
    existed = flag;
  }

  public String getExpandedDateAndTimeString() {
    //convert string from file to date
    ParsePosition pos = new ParsePosition(0);
    java.util.Date date = DateFormatter.parse(szDate, pos);
    //convert date to expanded format
    return expandedDateTimeFormatter.format(date);
  }

  public boolean isDayShift() {
    return (type == DAY_TYPE);
  }

  public boolean isSwingShift() {
    return (type == SWING_TYPE);
  }

  public boolean isNightShift() {
    return (type == NIGHT_TYPE);
  }

  public boolean isTrainingShift() {
    return (type == TRAINING_TYPE);
  }

  /*************************************************************************/
  /* getPosIndex - given an ID, which position(index) is that patroller    */
  /*  ASSUMES you cannot assign a patroller to two positions on same day   */
  /*************************************************************************/
  public int getPosIndex(String id) {
    int i;
    for (i = 0; i < MAX_ASSIGNMENT_SIZE; ++i) {
      if (id.equals(patrollerID[i])) {
        return i;
      }
    }
    return -1;  //not found
  }

  /*******************************************************/
  /* getPosID - return the patroller ID at position nPos */
  /*******************************************************/
  public String getPosID(int nPos) {
    if (nPos >= 0 && nPos < MAX_ASSIGNMENT_SIZE) {       //validate pos
      if (!patrollerID[nPos].equals("")) {
        return patrollerID[nPos];   //return ID
      }
    }
    return "0";                 //invalid pos
  }

  public void remove(int nPos) {
    if (nPos >= 0 && nPos < MAX_ASSIGNMENT_SIZE) {     //validate pos
      debugOut("Assignment: remove(nPos=" + nPos + ", ID was " + patrollerID[nPos] + ")");
      patrollerID[nPos] = "0";    //no patroller assigned
    }
  }

  public void insertAt(int nPos, String ID) {
    debugOut("Assignment: insertAt(nPos=" + nPos + ", ID=" + ID + ")");
    if (nPos >= 0 && nPos < MAX_ASSIGNMENT_SIZE) {
      patrollerID[nPos] = ID;
    }
    debugOut(toString());
  }

  public String getUpdateQueryString() {
    int i;
    if (szEventName.indexOf('"') != -1) {
      szEventName = szEventName.replace('"', '\'');
    }
    if (szStartTime.indexOf('"') != -1) {
      szStartTime = szStartTime.replace('"', '\'');
    }
    if (szEndTime.indexOf('"') != -1) {
      szEndTime = szEndTime.replace('"', '\'');
    }

    String qryString = "UPDATE assignments SET StartTime =\"" + szStartTime + "\", EndTime =\"" + szEndTime +
        "\", EventName=\"" + szEventName + "\", ShiftType=" + type + ", Count=" + count;
    // +
    for (i = 0; i < MAX_ASSIGNMENT_SIZE; ++i) {
      if (i >= count) {
        debugOut("setting position " + i + " to 0");
        insertAt(i, "0");
      }
      qryString += ", " + tag[P0_INDEX + i] + "=" + getPosID(i);
    }
    qryString += " WHERE Date=\'" + szDate + "\'";
    debugOut(qryString);
    return qryString;
  }

  public String getInsertQueryString() {
    int i;
    if (szEventName.indexOf('"') != -1) {
      szEventName = szEventName.replace('"', '\'');
    }
    if (szStartTime.indexOf('"') != -1) {
      szStartTime = szStartTime.replace('"', '\'');
    }
    if (szEndTime.indexOf('"') != -1) {
      szEndTime = szEndTime.replace('"', '\'');
    }

    String qryString = "INSERT INTO assignments Values(\'" + szDate + "', \"" +
        szStartTime + "\", \"" + szEndTime + "\", \"" + szEventName + "\", \"" + type + "\", " + count;
    for (i = 0; i < MAX_ASSIGNMENT_SIZE; ++i) {
      qryString += ", " + getPosID(i);
    }
    qryString += ")";

    LOG(qryString);
    return qryString;
  }

  public String getDeleteSQLString() {
    String qryString = "DELETE FROM assignments WHERE " + tag[DATE_INDEX] + " = '" + szDate + "'";
    debugOut(qryString);
    return qryString;
  }

  public static String getSelectAllAssignmentsByDateSQLString() {
    return "SELECT * FROM assignments ORDER BY \"" + getDateSqlTag() + "\"";
  }

  public String toString() {
    String ret = "Date=" + szDate + " StartTime=" + szStartTime + " EndTime=" + szEndTime +
        " EventName=" + szEventName + " ShiftType=" + type + " Count=" + count;
    for (int i = 0; i < MAX_ASSIGNMENT_SIZE; ++i) {
      ret += " " + patrollerID[i];
    }
    ret += " existed=" + existed;
    return ret;
  }

  private void debugOut(String msg) {
    if (DEBUG) {
      System.out.println("Debug-DayShifts: " + msg);
    }
  }

  private static void LOG(String msg) {
    System.out.println(msg);
  }
}
