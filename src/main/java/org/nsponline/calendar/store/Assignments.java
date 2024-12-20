package org.nsponline.calendar.store;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.nsponline.calendar.utils.Logger;
import org.nsponline.calendar.utils.PatrolData;
import org.nsponline.calendar.utils.SessionData;
import org.nsponline.calendar.utils.StaticUtils;

import static org.nsponline.calendar.utils.StaticUtils.sqlEncode;
import static org.nsponline.calendar.utils.StaticUtils.sqlStrip;
import static org.nsponline.calendar.utils.StaticUtils.sqlDecode;

import java.sql.ResultSet;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;

/**
 * The "assignments" represents each unique shift on each unique day, and will have 1-10 patrollers assigned to it.
 *  Example Date         StartTime EndTime EventName      ShiftType Count P0  P1  P2 P3 P4 P5 P6 P7 P8 P9
 *          2025-01-01_1  6         21:30  New Year's Day   2        3    123 456 789
 *          2025-01-01_2  6:30      21:30  New Year's Day   2        1    543
 *
 * @author Steve Gledhill
 */
public class Assignments {
  //public fields
  public final static int MAX_ASSIGNMENT_SIZE = 10;          //max number of assignments
  public final static int DAY_TYPE = 0;
  public final static int SWING_TYPE = 1;
  public final static int NIGHT_TYPE = 2;
  public final static int TRAINING_TYPE = 3;
  public final static int OTHER_TYPE = 4;
  public final static int HOLIDAY_TYPE = 5;
  public final static int MAX_SHIFT_TYPES = 6;

  //private fields
  final private static String[] szShiftTypes = {"Day Shift", "Swing Shift", "Night Shift", "Training Shift", "Other Shift", "Holiday Shift"};
  final private static String[] tag = {"Date", "StartTime", "EndTime", "EventName", "ShiftType",
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
  private Logger LOG;


  public Assignments(Logger parentLogger) {
    initData(parentLogger);
  }

  public Assignments(String myDate, ShiftDefinitions shift, Logger parentLogger) {
    initData(parentLogger);
    szEventName = " ";   //shift.getEventName();
    szDate = myDate;        //date of this shift
    szStartTime = shift.getStartString();
    szEndTime = shift.getEndString();
    type = shift.getType();
    count = shift.getCount();
  }

  private void initData(Logger parentLogger) {
    LOG = parentLogger; //new Logger(Assignments.class, parentLogger, null, MIN_LOG_LEVEL);

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

  private String readString(SessionData sessionData, ResultSet assignmentResults, String tag) {
    String str = null;
    try {
      str = assignmentResults.getString(tag);
      LOG.debug(str); //todo remove me
    }
    catch (Exception e) {
      LOG.error("exception in readString e=" + e);
      exceptionError = true;
    } //end try

    return str;
  }

  public boolean read(SessionData sessionData, ResultSet assignmentResults) {
    exceptionError = false;
    szDate = readString(sessionData, assignmentResults, tag[DATE_INDEX]);
    szStartTime = readString(sessionData, assignmentResults, tag[START_INDEX]);
    szEndTime = readString(sessionData, assignmentResults, tag[END_INDEX]);
    szEventName = sqlDecode(readString(sessionData, assignmentResults, tag[EVENT_INDEX]));
    LOG.debug("szEventName=(" + szEventName + ")");
    if (szEventName == null || szEventName.equals("null")) {
      szEventName = " ";
    }
    String str = readString(sessionData, assignmentResults, tag[TYPE_INDEX]);
    type = Integer.parseInt(str);
    str = readString(sessionData, assignmentResults, tag[COUNT_INDEX]);
    count = Integer.parseInt(str);
    for (int i = 0; i < MAX_ASSIGNMENT_SIZE; ++i) {
      patrollerID[i] = readString(sessionData, assignmentResults, ("P" + i));
    }
    existed = true;
    LOG.debug("Assignment.read=" + this);
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
    return szDate.substring(0, 10);   //YYYY-MM-DD
  }

  public String getDate() {
    return szDate;                    //YYYY-MM-DD_n
  }

  public static int getTypeID(SessionData sessionData, String szType) {
    if (szType == null) {
      sessionData.getLOG().error("** Assignments.getDate error, type was null");
      return DAY_TYPE;
    }
    for (int shiftType = 0; shiftType < MAX_SHIFT_TYPES; ++shiftType) {
      if (szType.equals(szShiftTypes[shiftType])) {
        return shiftType;
      }
    }
    sessionData.getLOG().error("** Assignments.getDate error, invalid string");
    return DAY_TYPE;
  }

  public static String createAssignmentName(String name, int i) {
    return name + "_" + PatrolData.IndexToString(i);
  }

  public int getUseCount(String resort) {
    LOG.debug("Assignments.getUseCount() for " + szDate + ", fullCount=" + count);
    int cnt = 0;
    int i;
    for (i = 0; i < count; ++i) {
      if (patrollerID[i] != null && !patrollerID[i].equals("0") && !patrollerID[i].equals("")) {
        ++cnt;
        LOG.debug("Found " + cnt + ") patroller=(" + patrollerID[i] + ")");
      }
    }
    return cnt;
  }

  public void setDate(String newDate) {
    szDate = newDate;
  }

  public void setEventName(String str) {
    szEventName = str;
    LOG.debug("1 szEventName=(" + szEventName + ")");
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

  public boolean isOtherShift() {
    return (type == OTHER_TYPE);
  }

  public boolean isHolidayShift() {
    return (type == HOLIDAY_TYPE);
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

  public void remove(String resort, int nPos) {
    if (nPos >= 0 && nPos < MAX_ASSIGNMENT_SIZE) {     //validate pos
      LOG.debug("Assignment: remove(nPos=" + nPos + ", ID was " + patrollerID[nPos] + ")");
      patrollerID[nPos] = "0";    //no patroller assigned
    }
  }

  public void insertAt(String resort, int nPos, String ID) {
    LOG.debug("Assignment: insertAt(nPos=" + nPos + ", ID=" + ID + ")");
    if (nPos >= 0 && nPos < MAX_ASSIGNMENT_SIZE) {
      patrollerID[nPos] = ID;
    }
    LOG.debug(toString());
  }

  public String getUpdateQueryString(SessionData sessionData) {
    int i;
    //encode fields that can be set by the user
    String encodedEventName = sqlEncode(szEventName);
    String encodedStartTime = sqlStrip(szStartTime);
    String encodedEndTime = sqlStrip(szEndTime);

    String qryString = "UPDATE assignments SET StartTime =\"" + encodedStartTime + "\", EndTime =\"" + encodedEndTime +
        "\", EventName=\"" + encodedEventName + "\", ShiftType=" + type + ", Count=" + count;
    // +
    for (i = 0; i < MAX_ASSIGNMENT_SIZE; ++i) {
      if (i >= count) {
        LOG.debug("setting position " + i + " to 0");
        insertAt(sessionData.getLoggedInResort(), i, "0");
      }
      qryString += ", " + tag[P0_INDEX + i] + "='" + getPosID(i) + "'"; //fixes bug where string starts with 0 zero
    }
    qryString += " WHERE Date=\'" + szDate + "\'";
    LOG.logSqlStatement(qryString);
    return qryString;
  }

  public String getInsertQueryString() {
    int i;
    String encodedEventName = sqlEncode(szEventName);
    String encodedStartTime = sqlStrip(szStartTime);
    String encodedEndTime = sqlStrip(szEndTime);

    String qryString = "INSERT INTO assignments Values(\'" + szDate + "', \"" +
      encodedStartTime + "\", \"" + encodedEndTime + "\", \"" + encodedEventName + "\", \"" + type + "\", " + count;
    for (i = 0; i < MAX_ASSIGNMENT_SIZE; ++i) {
      qryString += ", '" + getPosID(i) + "'";
    }
    qryString += ")";

    LOG.logSqlStatement(qryString);
    return qryString;
  }

  public String getDeleteSQLString(SessionData sessionData) {
    String qryString = "DELETE FROM assignments WHERE " + tag[DATE_INDEX] + " ='" + szDate + "'";
    LOG.logSqlStatement(qryString);
    return qryString;
  }

  public static String getSelectAllAssignmentsByDateSQLString(Logger LOG) {
    String sqlQuery = "SELECT * FROM assignments ORDER BY \"" + getDateSqlTag() + "\"";
    LOG.logSqlStatement(sqlQuery);
    return sqlQuery;
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

  public boolean includesPatroller(String patrollerId) {
    if (patrollerId == null) {
      return false;
    }
    for (int i = 0; i < MAX_ASSIGNMENT_SIZE; ++i) {
      if (patrollerId.equals(patrollerID[i])) {
        return true;
      }
    }
    return false;
  }

  public void copyAssignedPatrollers(Assignments prevAssignment) {
    for (int i = 0; i < MAX_ASSIGNMENT_SIZE; ++i) {
      patrollerID[i] = (prevAssignment.getPosID(i));
    }
  }

  public ObjectNode toNode() {
    ObjectNode returnNode = StaticUtils.nodeFactory.objectNode();

    returnNode.put("Date", szDate);
    returnNode.put("StartTime", szStartTime);
    returnNode.put("EndTime", szEndTime);
    setIfNotEmpty(returnNode, "EventName", szEventName);
    returnNode.put("ShiftType", String.valueOf(type));
    returnNode.put("Count", String.valueOf(count));
    ArrayNode patrollerIds = StaticUtils.nodeFactory.arrayNode();
    for (int i=0; i < count; ++i) {
      patrollerIds.add(patrollerID[i]);
    }
    returnNode.set("patrollerIds", patrollerIds);
    return returnNode;
  }

  private void setIfNotEmpty(ObjectNode returnNode, String key, String value) {
    if (StaticUtils.isNotEmpty(value)) {
      returnNode.put(key, value);
    }
  }
}
