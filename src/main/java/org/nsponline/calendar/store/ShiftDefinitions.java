package org.nsponline.calendar.store;

import org.nsponline.calendar.utils.Logger;
import org.nsponline.calendar.utils.PatrolData;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;

/**
 * @author Steve Gledhill
 *
 * Example    EventName     StartTime    EndTime    Count    ShiftType
 *          Saturday_0      08:00        16:30      8        0  (day)
 *          Saturday_1      14:00        21:30      3        2  (night)
 *          OEC Final_0     18:00        20:00      10       3  (training)
 */
@SuppressWarnings("unused")
public class ShiftDefinitions {
  private Logger LOG;

  public static final String[] tags = {"EventName", "StartTime", "EndTime", "Count", "ShiftType"};  //string on form
  private static final int EVENT_NAME_INDEX = 0;
  private static final int START_TIME_INDEX = 1;
  private static final int END_TIME_INDEX = 2;
  private static final int COUNT_INDEX = 3;
  private static final int TYPE_INDEX = 4;

  private static final SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
  public final static int MAX = 35;  //maximum # of different shifts on any single day

  //instance data
  private String eventName;
  private String startTime;
  private String endTime;
  private int count;
  private int type;
  private boolean existed;

  public ShiftDefinitions(Logger parentLogger) {
    LOG = parentLogger; //new Logger(this.getClass(), parentLogger, null, Logger.INFO);
    eventName = null;
    startTime = null;
    endTime = null;
    count = 0;
    type = 0;  //day shift
    existed = false;
  }

  public ShiftDefinitions(String name, String start, String end, int cnt, int typ, Logger parentLogger) {
    LOG = parentLogger; //new Logger(this.getClass(), parentLogger, null, Logger.INFO);
    eventName = name;
    startTime = start;
    endTime = end;
    count = cnt;
    type = typ;
    if (type < 0 || type >= Assignments.MAX_SHIFT_TYPES) {
      type = 0;  //reset to default
    }
    existed = false;
  }

  public boolean equals(ShiftDefinitions other) {
    return other.eventName.equals(eventName) &&
        other.startTime.equals(startTime) &&
        other.endTime.equals(endTime) &&
        (other.count == count) &&
        (other.type == type);
  }

  public boolean read(ResultSet shiftResults) {
    int Start, End;
    try {
      eventName = shiftResults.getString(tags[EVENT_NAME_INDEX]);
      startTime = shiftResults.getString(tags[START_TIME_INDEX]);
      endTime = shiftResults.getString(tags[END_TIME_INDEX]);
      count = shiftResults.getInt(tags[COUNT_INDEX]);
      type = shiftResults.getInt(tags[TYPE_INDEX]);
      if (type < 0 || type >= Assignments.MAX_SHIFT_TYPES) {
        type = 0;  //reset to default, if invalid
      }
      existed = true;
    }
    catch (Exception e) {
      LOG.logException("exception in Shifts:read", e);
      return false;
    } //end try
    return true;
  }

  public boolean exists() {
    return existed;
  }

  public static String createShiftName(String name, int i) {
    return name + "_" + PatrolData.IndexToString(i);
  }

  public int getEventIndex() {
    String temp = eventName.substring(eventName.lastIndexOf("_") + 1);
    return PatrolData.StringToIndex(temp);
  }

  public String parsedEventName() {
    return eventName.substring(0, eventName.lastIndexOf("_"));
  }

  public String getEventName() {
    return eventName;
  }

  public void setEventName(String eventName) {
    this.eventName = eventName;
  }

  public String getStartString() {
    return startTime;
  }

  public String getEndString() {
    return endTime;
  }

  public int getCount() {
    return count;
  }

  public int getType() {
    return type;
  }

  public String getUpdateShiftDefinitionsQueryString(String resort) {
    String qryString = "UPDATE shiftdefinitions SET " +
        " " + tags[START_TIME_INDEX] + "='" + startTime +
        "', " + tags[END_TIME_INDEX] + "='" + endTime +
        "', " + tags[COUNT_INDEX] + "='" + count +
        "', " + tags[TYPE_INDEX] + "='" + type +
        "' WHERE " + tags[EVENT_NAME_INDEX] + "= '" + eventName + "'";
    LOG.logSqlStatement(qryString);
    return qryString;
  }

  public String getInsertShiftDefinitionsQueryString(String resort) {
    String qryString = "INSERT INTO shiftdefinitions " +
        " Values('" + eventName + "','" + startTime + "','" + endTime + "','" + count + "'," + type + ")";
    LOG.logSqlStatement(qryString);
    return qryString;
  }

  public String getDeleteSQLString(String resort) {
    int i;
    String qryString = "DELETE FROM shiftdefinitions WHERE " + tags[EVENT_NAME_INDEX] + " = '" + eventName + "'";
    LOG.logSqlStatement(qryString);
    return qryString;
  }

  public String toString() {
//        int start = startTime.get(Calendar.HOUR)*startTime.get(Calendar.MINUTE);
//        int end = endTime.get(Calendar.HOUR)*endTime.get(Calendar.MINUTE);
    return eventName + " starts: " + startTime + " ends: " + endTime + " count=" + count + " type=" + Assignments.getShiftName(type) + " existed=" + existed;
  }

  public void setExists(boolean exists) {
    this.existed = exists;
  }
}