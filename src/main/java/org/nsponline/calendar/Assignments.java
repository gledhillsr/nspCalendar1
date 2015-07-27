package org.nsponline.calendar;

import java.sql.ResultSet;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;

/**
 * @author Steve Gledhill
 */
public class Assignments {
  final static int MAX = 10;          //max number of assignments
  final static String tag[] = {"Date", "StartTime", "EndTime", "EventName", "ShiftType",
      "Count", "P0", "P1", "P2", "P3", "P4", "P5", "P6", "P7", "P8", "P9"}; //string on form
  // the Date string is in this format "2001-11-03_1"  where _1 is the FIRST record
  final static int DATE_INDEX = 0;
  final static int START_INDEX = 1;
  final static int END_INDEX = 2;
  final static int EVENT_INDEX = 3;
  final static int TYPE_INDEX = 4;
  final static int COUNT_INDEX = 5;
  final static int P0_INDEX = 6;

  final static String szShiftTypes[] = {"Day Shift", "Swing Shift", "Night Shift", "Training Shift"};
  final static int DAY_TYPE = 0;
  final static int SWING_TYPE = 1;
  final static int NIGHT_TYPE = 2;
  final static int TRAINING_TYPE = 3;
  final static int MAX_SHIFT_TYPES = 4;
  // Format the current time.
//E=day of Week (ie. Tuesday), y=year, M=month, d=day, H=hour (24 hour clock), m=minute, s=second
  final static SimpleDateFormat expandedDateTimeFormatter = new SimpleDateFormat("EEEE, MMMM d yyyy");
  final static SimpleDateFormat DateFormatter = new SimpleDateFormat("yyyy-MM-dd");
  final static SimpleDateFormat normalDateFormatter = new SimpleDateFormat("MM'/'dd'/'yyyy");

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
    patrollerID = new String[MAX];  //allocate ID array
    count = 0;
    type = DAY_TYPE;
    szDate = szStartTime = szEndTime = szEventName = null;
    for (i = 0; i < MAX; ++i) {
      patrollerID[i] = "0";   //no patroller assigned
    }
  }


  private String readString(ResultSet assignmentResults, String tag) {
    String str = null;
    try {
      str = assignmentResults.getString(tag);
    }
    catch (Exception e) {
      System.out.println("exception in readString e=" + e);
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
//System.out.println("szEventName=("+szEventName+")");
    if (szEventName == null || szEventName.equals("null")) {
      szEventName = " ";
    }
    String str = readString(assignmentResults, tag[TYPE_INDEX]);
    type = Integer.parseInt(str);
    str = readString(assignmentResults, tag[COUNT_INDEX]);
    count = Integer.parseInt(str);
    for (int i = 0; i < MAX; ++i) {
      patrollerID[i] = readString(assignmentResults, ("P" + i));
    }
    existed = true;
//  System.out.println("Assignment.read="+this);
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
      System.out.println("** Assignments.getDate error, type was null");
      return DAY_TYPE;
    }
    for (int i = 0; i < MAX_SHIFT_TYPES; ++i) {
      if (szType.equals(szShiftTypes[i])) {
        return i;
      }
    }
    System.out.println("** Assignments.getDate error, invalid string");
    return DAY_TYPE;
  }

  public static String createAssignmentName(String name, int i) {
    return name + "_" + PatrolData.IndexToString(i);
  }

  public int getUseCount() {
//System.out.println("Assignments.getUseCount() for "+szDate+", fullCount="+count);
    int cnt = 0;
    int i;
    for (i = 0; i < count; ++i) {
      if (patrollerID[i] != null && !patrollerID[i].equals("0") && !patrollerID[i].equals("")) {
        ++cnt;
//System.out.println("Found "+cnt+") patroller=("+patrollerID[i]+")");
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
//    System.out.println("1 szEventName=("+szEventName+")");
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
  public int getPosIndex(String ID) {
    int i;
    for (i = 0; i < MAX; ++i) {
      if (ID.equals(patrollerID[i])) {
        return i;
      }
    }
    return -1;  //not found
  }

/*******************************************************/
/* getPosID - return the patroller ID at position nPos */

  /*******************************************************/
  public String getPosID(int nPos) {
    if (nPos >= 0 && nPos < MAX) {       //validate pos
      if (!patrollerID[nPos].equals("")) {
        return patrollerID[nPos];   //return ID
      }
    }
    return "0";                 //invalid pos
  }

/*****************************************************/
/* remove - invalidate patroller ID at position nPos */

  /*****************************************************/
  public void remove(int nPos) {
    if (nPos >= 0 && nPos < MAX) {     //validate pos
//System.out.println("Assignment: remove(nPos="+nPos+", ID was "+patrollerID[nPos]+")");
      patrollerID[nPos] = "0";    //no patroller assigned
    }
  }

/***************************************************/
/* insertAt - insert patroller ID at position nPos */

  /***************************************************/
  public void insertAt(int nPos, String ID) {
//System.out.println("Assignment: insertAt(nPos="+nPos+", ID="+ID+")");
    if (nPos >= 0 && nPos < MAX) {
      patrollerID[nPos] = ID;
    }
//System.out.println(toString());
  }

/************************/
/* getUpdateQueryString */

  /************************/
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
    for (i = 0; i < MAX; ++i) {
      if (i >= count) {
//System.out.println("setting position "+i+" to 0");
        insertAt(i, "0");
      }
      qryString += ", " + tag[P0_INDEX + i] + "=" + getPosID(i);
    }
    qryString += " WHERE Date=\'" + szDate + "\'";

// moved to callers System.out.println(qryString);
    return qryString;
  }
  /************************/
    /* getInsertQueryString */

  /************************/
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
    for (i = 0; i < MAX; ++i) {
      qryString += ", " + getPosID(i);
    }
    qryString += ")";

    System.out.println(qryString);
    return qryString;
  }

  public String getDeleteSQLString() {
    int i;
    String qryString = "DELETE FROM assignments WHERE " + tag[DATE_INDEX] + " = '" + szDate + "'";
//System.out.println(qryString);
    return qryString;
  }

  public String toString() {
    String ret = "Date=" + szDate + " StartTime=" + szStartTime + " EndTime=" + szEndTime +
        " EventName=" + szEventName + " ShiftType=" + type + " Count=" + count;
    for (int i = 0; i < MAX; ++i) {
      ret += " " + patrollerID[i];
    }
    ret += " existed=" + existed;
    return ret;

  }
}
