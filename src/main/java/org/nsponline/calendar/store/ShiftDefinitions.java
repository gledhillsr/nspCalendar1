package org.nsponline.calendar.store;

import org.nsponline.calendar.misc.Logger;
import org.nsponline.calendar.misc.PatrolData;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;

/**
 * @author Steve Gledhill
 */
@SuppressWarnings("unused")
public class ShiftDefinitions {
  private Logger LOG;

  //static data
  //format EventName Saturday_0, Saturday_1, World Cup_0 etc
  //format StartTime 08:00 (text format)
  //format EndTime 08:00 (text format)
  //format Count (int)
  public final static String tags[] = {"EventName", "StartTime", "EndTime", "Count", "ShiftType"};  //string on form
  private final static int EVENT_NAME_INDEX = 0;
  private final static int START_TIME_INDEX = 1;
  private final static int END_TIME_INDEX = 2;
  private final static int COUNT_INDEX = 3;
  private final static int TYPE_INDEX = 4;

  private final static SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
  public final static int MAX = 35;  //maximum # of different shifts on any single day

  //instance data
  private String eventName;
  private String startTime;
  private String endTime;
  private int count;
  private int type;
  private boolean existed;

  public ShiftDefinitions(Logger parentLogger) {
    LOG = new Logger(this.getClass(), parentLogger);
    eventName = null;
    startTime = null;
    endTime = null;
    count = 0;
    type = 0;  //day shift
    existed = false;
  }

  public ShiftDefinitions(String name, String start, String end, int cnt, int typ, Logger parentLogger) {
    LOG = new Logger(this.getClass(), parentLogger);
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
      Logger.log("exception in Shifts:read exception=" + e);
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

  public String getUpdateShiftDefinitionsQueryString() {
    String qryString = "UPDATE shiftdefinitions SET " +
        " " + tags[START_TIME_INDEX] + "='" + startTime +
        "', " + tags[END_TIME_INDEX] + "='" + endTime +
        "', " + tags[COUNT_INDEX] + "='" + count +
        "', " + tags[TYPE_INDEX] + "='" + type +
        "' WHERE " + tags[EVENT_NAME_INDEX] + "= '" + eventName + "'";
    LOG.logSqlStatement(qryString);
    return qryString;
  }

  public String getInsertShiftDefinitionsQueryString() {
    String qryString = "INSERT INTO shiftdefinitions " +
        " Values('" + eventName + "','" + startTime + "','" + endTime + "','" + count + "'," + type + ")";
    LOG.logSqlStatement(qryString);
    return qryString;
  }

  public String getDeleteSQLString() {
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