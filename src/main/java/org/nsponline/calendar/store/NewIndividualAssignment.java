package org.nsponline.calendar.store;

import org.nsponline.calendar.misc.Logger;
import org.nsponline.calendar.misc.PatrolData;
import org.nsponline.calendar.misc.SessionData;

import java.sql.SQLException;
import java.text.*;
import java.util.Date;
import java.lang.*;
import java.sql.ResultSet;

/**
 * Steve Gledhill
 */
public class NewIndividualAssignment {

  final static String tableName = "newindividualassignment";
  final static String tag[] = {"date_shift_pos", "scheduleDate", "shifttype", "flags",
      "patrollerId", "lastModifiedDate", "lastModifiedBy"}; //string on form
  // the Date string is in this format "2001-11-03_1"  where _1 is the FIRST record
  final static int DATE_SHIFT_POS_INDEX = 0;
  final static int SCHEDULE_DATE_INDEX = 1;
  final static int SHIFT_TYPE_INDEX = 2;
  final static int FLAGS_INDEX = 3;
  final static int PATROLLER_ID_INDEX = 4;
  final static int LAST_MODIFIED_DATE_INDEX = 5;
  final static int LAST_MODIFIED_BY_INDEX = 6;

  public final static int DAY_TYPE = 0;
  final static SimpleDateFormat detailedDateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  final static SimpleDateFormat DateFormatter = new SimpleDateFormat("yyyy-MM-dd");

  // ALL the following global data MUST be initialized in the constructor
  private String dateShiftPos;
  private Date scheduleDate;
  private int shiftType;
  private int flags;
  private String patrollerId;           //patroller ID for each assignment
  private Date lastModifiedDate;
  private String lastModifiedBy;
  private boolean exceptionError;
  private boolean existed;

  public final static int FLAG_BIT_NEEDS_REPLACEMENT = 2;

  public NewIndividualAssignment() {
    initData();
  }

  public NewIndividualAssignment(Date date, int shift, int pos, int shiftType, int flags,
                                 String patrollerId, String editorId) {
    initData();
    String formattedDate = DateFormatter.format(date);
    this.dateShiftPos = formattedDate + "_" + shift + "_" + pos;
    this.scheduleDate = date;
    this.shiftType = shiftType;
    this.flags = flags;
    this.patrollerId = patrollerId;
    this.lastModifiedDate = new Date();
    this.lastModifiedBy = editorId;
  }

  private void initData() {
    this.dateShiftPos = "";
    this.scheduleDate = null;
    this.shiftType = DAY_TYPE;
    this.flags = 0;
    this.patrollerId = "";           //patroller ID for each assignment
    this.lastModifiedDate = null;
    this.lastModifiedBy = "";
    existed = false;
  }

  private String readString(SessionData sessionData, ResultSet newAssignmentResults, String tag) {
    String str = null;
    try {
      str = newAssignmentResults.getString(tag);
    }
    catch (SQLException e) {
      Logger.printToLogFile(sessionData.getRequest(), "ERROR NewIndividualAssignment.readString(" + tag + "), message: " + e.getMessage());
      exceptionError = true;
    } //end try

    return str;
  }

  private int readInt(SessionData sessionData, ResultSet newAssignmentResults, String tag) {
    try {
      return newAssignmentResults.getInt(tag);
    }
    catch (SQLException e) {
      Logger.printToLogFile(sessionData.getRequest(), "ERROR NewIndividualAssignment.readInt(" + tag + "), message: " + e.getMessage());
      exceptionError = true;
    } //end try
    return 0;
  }

  private Date readDate(SessionData sessionData, ResultSet newAssignmentResults, String tag) {
    try {
      return newAssignmentResults.getDate(tag);
    }
    catch (SQLException e) {
      Logger.printToLogFile(sessionData.getRequest(), "ERROR NewIndividualAssignment.readDate(" + tag + "), message: " + e.getMessage());
      exceptionError = true;
    } //end try
    return null;
  }

  public boolean read(SessionData sessionData, ResultSet newAssignmentResults) {
    exceptionError = false;

    dateShiftPos = readString(sessionData, newAssignmentResults, tag[DATE_SHIFT_POS_INDEX]);
    scheduleDate = readDate(sessionData, newAssignmentResults, tag[SCHEDULE_DATE_INDEX]);
    shiftType = readInt(sessionData, newAssignmentResults, tag[SHIFT_TYPE_INDEX]);
    flags = readInt(sessionData, newAssignmentResults, tag[FLAGS_INDEX]);
    patrollerId = readString(sessionData, newAssignmentResults, tag[PATROLLER_ID_INDEX]);
    lastModifiedDate = readDate(sessionData, newAssignmentResults, tag[LAST_MODIFIED_DATE_INDEX]);
    lastModifiedBy = readString(sessionData, newAssignmentResults, tag[LAST_MODIFIED_BY_INDEX]);
    existed = true;
    return !exceptionError;
  }

  public String getPatrollerId() {
    return patrollerId;
  }

  public String getDateShiftPos() {
    return dateShiftPos;
  }

  public boolean getNeedsReplacement() {
    return ((flags & FLAG_BIT_NEEDS_REPLACEMENT) == FLAG_BIT_NEEDS_REPLACEMENT);
  }

  public void setNeedsReplacement(boolean replace) {
    if (replace) {
      flags |= FLAG_BIT_NEEDS_REPLACEMENT;
    }
    else {
      flags &= ~FLAG_BIT_NEEDS_REPLACEMENT;
    }
  }

  public void setExisted(boolean flag) {
    existed = flag;
  }

  public String getUpdateSQLString(SessionData sessionData) {
//UPDATE `newindividualassignment` SET `lastModifiedDate` = '2009-02-01 04:50:15' WHERE `date_shift_pos` = '2009-02-14_0_1' LIMIT 1
    lastModifiedDate = new Date();
    String szScheduleDate = DateFormatter.format(scheduleDate);
    String szLastModDate = detailedDateTimeFormatter.format(lastModifiedDate);
    String qryString = "UPDATE newindividualassignment SET " +
        tag[SCHEDULE_DATE_INDEX] + " = '" + szScheduleDate + "', " +
        tag[SHIFT_TYPE_INDEX] + " = '" + shiftType + "', " +
        tag[FLAGS_INDEX] + " = '" + flags + "', " +
        tag[PATROLLER_ID_INDEX] + " = '" + patrollerId + "', " +
        tag[LAST_MODIFIED_DATE_INDEX] + " = '" + szLastModDate + "', " +
        tag[LAST_MODIFIED_BY_INDEX] + " = '" + lastModifiedBy + "'";
    qryString += " WHERE " + tag[DATE_SHIFT_POS_INDEX] + " = '" + dateShiftPos + "'";
    Logger.logSqlStatementStatic(qryString);
    return qryString;
  }

  public String getInsertSQLString(SessionData sessionData) {
    String szScheduleDate = DateFormatter.format(scheduleDate);
    String szLastModDate = detailedDateTimeFormatter.format(lastModifiedDate);
    String qryString = "INSERT INTO newindividualassignment Values(\'" + dateShiftPos + "', \"" +
        szScheduleDate + "\", \"" + shiftType + "\", \"" + flags + "\", \"" + patrollerId + "\", \"" +
        szLastModDate + "\", \"" + lastModifiedBy + "\")";

    Logger.printToLogFile(sessionData.getRequest(), qryString);
    return qryString;
  }

  public String getDeleteSQLString(SessionData sessionData) {
    String qryString = "DELETE FROM " + tableName + " WHERE " + tag[DATE_SHIFT_POS_INDEX] + " = '" + dateShiftPos + "'";
    Logger.printToLogFile(sessionData.getRequest(), qryString);
    return qryString;
  }

  public String toString() {
    String ret = "dateShiftPos=" + dateShiftPos + " scheduleDate=" + scheduleDate +
        " shiftType=" + shiftType + " flags=" + flags +
        " patrollerId=" + patrollerId +
        " lastModifiedDate=" + lastModifiedDate + " lastModifiedBy=" + lastModifiedBy;
    ret += " existed=" + existed;
    return ret;

  }

  public static String buildKey(int year, int month, int day, int idx, int pos) {
    //build key in the form yyyy-mm-dd-p  (p is a single digit 1-9ABCDEF...)
    String key = year + "-";
    if (month < 10) {
      key += "0";
    }
    key += month + "-";
    if (day > 0) {
      if (day < 10) {
        key += "0";
      }
      key += day + "_";
    }
    if (idx >= 0) {
      key += idx + "_";
    }
    //this should be 1-9ABCDEF....
    if (pos >= 0) {
      key += PatrolData.IndexToString(pos);
    }
    return key;
  }
}
