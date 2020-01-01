package org.nsponline.calendar.store;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.nsponline.calendar.misc.Logger;
import org.nsponline.calendar.misc.PatrolData;
import org.nsponline.calendar.misc.Utils;

import java.lang.*;
import java.sql.*;


/**
 * @author Steve Gledhill
 */
@SuppressWarnings("SqlDialectInspection")
public class DirectorSettings {

  /*****
   * CAUTION: THESE FIELDS MUST EXACTLY MATCH THE SQL DATABASE
   *****/
  @SuppressWarnings("SqlNoDataSourceInspection")
  final static String PATROL_NAME_FIELD = "PatrolName";
  public final static String EMAIL_REMINDER_FIELD = "emailReminder";
  public final static String REMINDER_DAYS_FIELD = "reminderDays";
  public final static String NOTIFY_CHANGES_FIELD = "emailOnChanges";
  public final static String USE_TEAMS_FIELD = "useTeams";
  public final static String DIRECTORS_CHANGE_FIELD = "directorsOnlyChange";
  public final static String EMAIL_ALL_FIELD = "emailAll";
  public final static String NAME_FORMAT_FIELD = "nameFormat";
  public final static String START_DATE_FIELD = "startDate";
  public final static String END_DATE_FIELD = "endDate";
  public final static String USE_BLACKOUT_FIELD = "useBlackOut";
  public final static String START_BLACKOUT_FIELD = "startBlackOut";
  public final static String END_BLACKOUT_FIELD = "endBlackOut";
  public final static String REMOVE_ACCESS_FIELD = "removeAccess";

  //instance data
  private String szPatrolName;            //0
  private String szEmailReminder;         //1
  private int nReminderDays;              //2
  private String szNotifyChanges;         //3
  private String szUseTeams;              //4
  private String szDirectorsOnlyChange;   //5
  private String szEMailAll;              //6
  private int nNameFormat;                //7
  private String szStartDate;             //8
  private String szEndDate;               //9
  private int nUseBlackout;               //10
  private String szStartBlackout;         //11
  private String szEndBlackout;           //12
  private int nRemoveAccess;              //13
  private String resort;                  //variable
  private Logger LOG;

  //-----------------------------------------------------
// constructor - store bogus patroller ID in each field
//-----------------------------------------------------
  public DirectorSettings(String myResort, final Logger parentLogger) {
    LOG = new Logger(PatrolData.class, parentLogger, myResort, Logger.INFO);
    szPatrolName = null;
    szEmailReminder = null;
    nReminderDays = 0;
    szNotifyChanges = null;
    szUseTeams = null;
    szDirectorsOnlyChange = null;
    szEMailAll = null;
    nNameFormat = 0;
    szStartDate = null;
    szEndDate = null;
    nUseBlackout = 0;
    szStartBlackout = null;
    szEndBlackout = null;
    nRemoveAccess = 0;
    resort = myResort;
  }

  public boolean read(ResultSet resultSet) {
    try {
      szPatrolName = resultSet.getString(PATROL_NAME_FIELD);
      szEmailReminder = resultSet.getString(EMAIL_REMINDER_FIELD);
      nReminderDays = resultSet.getInt(REMINDER_DAYS_FIELD);
      szNotifyChanges = resultSet.getString(NOTIFY_CHANGES_FIELD);
      szUseTeams = resultSet.getString(USE_TEAMS_FIELD);
      szDirectorsOnlyChange = resultSet.getString(DIRECTORS_CHANGE_FIELD);
      szEMailAll = resultSet.getString(EMAIL_ALL_FIELD);
      nNameFormat = resultSet.getInt(NAME_FORMAT_FIELD);
      szStartDate = resultSet.getString(START_DATE_FIELD);
      szEndDate = resultSet.getString(END_DATE_FIELD);
      nUseBlackout = resultSet.getInt(USE_BLACKOUT_FIELD);
      szStartBlackout = resultSet.getString(START_BLACKOUT_FIELD);
      szEndBlackout = resultSet.getString(END_BLACKOUT_FIELD);
//ignore skihistory update, voucher history update, and signinLogin
      nRemoveAccess = resultSet.getInt(REMOVE_ACCESS_FIELD);
    }
    catch (Exception e) {
      LOG.error("exception in Shifts:read e=" + e);
      return false;
    } //end try
    return true;
  }


  private int getYear(String szDate) {
    int year = 0;   //error
    if (szDate.length() >= 8) { //dd-mm-yy
      try {
        year = Integer.parseInt(szDate.substring(6, 8));
        year += 2000;
      }
      catch (Exception e) {
        Thread.dumpStack();
      }
    }
    return year;
  }

  private int getMonth(String szDate) {
    int mon = 0;    //error
    if (szDate.length() >= 5) { //dd-mm
      try {
        mon = Integer.parseInt(szDate.substring(3, 5));
      }
      catch (Exception e) {
        Thread.dumpStack();
      }
    }
    return mon;
  }

  private int getDay(String szDate) {
    int day = 0;    //error
    if (szDate.length() >= 5) { //dd-mm
      try {
        day = Integer.parseInt(szDate.substring(0, 2));
      }
      catch (Exception e) {
        Thread.dumpStack();
      }
    }
    return day;
  }

  private String setYear(String szDate, int yr) {
    if (yr < 2002 || yr > 2099) {
      return szDate;
    }
    String szYr = "";
    yr -= 2000;
    if (yr <= 9) {
      szYr += "0";
    }
    szYr += yr;
    //szDate dd-mm-yy
    szDate = szDate.substring(0, 6) + szYr;
//Log.log("in setYear("+yr+") new ="+szDate);
    return szDate;
  }

  private String setMonth(String szDate, int mon) {
    if (mon < 1 || mon > 12) {
      return szDate;
    }
    String szMon = "";
    if (mon <= 9) {
      szMon += "0";
    }
    szMon += mon;
    //szDate dd-mm-yy
    if (szDate.length() > 5) {
      szDate = szDate.substring(0, 3) + szMon + szDate.substring(5);
    }
    else {
      szDate = szDate.substring(0, 3) + szMon;
    }
//Log.log("in setMonth("+mon+") new ="+szDate);
    return szDate;
  }

  public String setDay(String szDate, int day) {  //format dd/mm/yy
//Log.log("in setDay("+day+") original date="+szDate);
    if (day < 1 || day > 31) {
      return szDate;
    }
    String str = "";
    if (day <= 9) {
      str += "0";
    }
    szDate = str + day + szDate.substring(2);
//Log.log("   setDay("+day+") new ="+szDate);
    return szDate;
  }


  public boolean getSendReminder() {
    return szEmailReminder.equals("1");
  }

  public int getReminderDays() {
    return nReminderDays;
  }

  public boolean getNotifyChanges() {
    return szNotifyChanges.equals("1");
  }

  public boolean getDirectorsOnlyChange() {
    return szDirectorsOnlyChange.equals("1");
  }

  public boolean getEmailAll() {
    return szEMailAll.equals("1");
  }

  public int getNameFormat() {
    return nNameFormat;
  }

  public int getStartMonth() {
    return getMonth(szStartDate);
  }

  public int getStartDay() {
    return getDay(szStartDate);
  }

  public int getEndMonth() {
    return getMonth(szEndDate);
  }

  public int getEndDay() {
    return getDay(szEndDate);
  }

  public int getBlackOutStartDay() {
    return getDay(szStartBlackout);
  }

  public int getBlackOutStartMonth() {
    return getMonth(szStartBlackout);
  }

  public int getBlackOutStartYear() {
    return getYear(szStartBlackout);
  }

  public int getBlackOutEndDay() {
    return getDay(szEndBlackout);
  }

  public int getBlackOutEndMonth() {
    return getMonth(szEndBlackout);
  }

  public int getBlackOutEndYear() {
    return getYear(szEndBlackout);
  }

  public boolean getUseBlackOut() {
    return (nUseBlackout > 0);
  }

  public int getRemoveAccess() {
    return nRemoveAccess;
  }

  public String getResort() {
    return resort;
  }

  @SuppressWarnings("unused")
  public String secondsToTime(int sec) {
    int min = ((sec % 3600) / 60);
    return (sec / 3600) + ":" + ((min < 10) ? "0" : "") + min;
  }

//-----------------------------------------------------
// Setter methods
//-----------------------------------------------------
  public void setSendReminder(boolean flag) {
    szEmailReminder = (flag ? "1" : "0");
  }

  public void setReminderDays(int days) {
    nReminderDays = days;
  }

  public void setNotifyChanges(boolean flag) {
    szNotifyChanges = (flag ? "1" : "0");
  }

  public void setDirectorsOnlyChange(boolean flag) {
    szDirectorsOnlyChange = (flag ? "1" : "0");
  }

  public void setEmailAll(boolean flag) {
    szEMailAll = (flag ? "1" : "0");
  }

  public void setNameFormat(int fmt) {
    nNameFormat = fmt;
  }

  public void setStartDay(int day) {
    szStartDate = setDay(szStartDate, day);
  }

  public void setStartMonth(int mon) {
    szStartDate = setMonth(szStartDate, mon);
  }

  public void setEndDay(int day) {
    szEndDate = setDay(szEndDate, day);
  }

  public void setEndMonth(int mon) {
    szEndDate = setMonth(szEndDate, mon);
  }

  public void setUseBlackOut(boolean bo) {
    nUseBlackout = bo ? 1 : 0;
  }

  public void setBlackStartDay(int day) {
    szStartBlackout = setDay(szStartBlackout, day);
  }

  public void setBlackStartMonth(int mon) {
    szStartBlackout = setMonth(szStartBlackout, mon);
  }

  public void setBlackStartYear(int yr) {
    szStartBlackout = setYear(szStartBlackout, yr);
  }

  public void setBlackEndDay(int day) {
    szEndBlackout = setDay(szEndBlackout, day);
  }

  public void setBlackEndMonth(int mon) {
    szEndBlackout = setMonth(szEndBlackout, mon);
  }

  public void setBlackEndYear(int yr) {
    szEndBlackout = setYear(szEndBlackout, yr);
  }

  public void setRemoveAccess(int access) {
    nRemoveAccess = access;
  }

  @SuppressWarnings("unused")
  public int timeToSeconds(String szTime) {
    int pos;
    int seconds = 28800; //default of 8:00
    String tmp;
    if (szTime == null) {
      return seconds;
    }

    if ((pos = szTime.indexOf(':')) == -1) //-1 is NOT found
    {
      return seconds;
    }
    try {
      tmp = szTime.substring(0, pos);
      seconds = Integer.parseInt(tmp) * 3600;
//Log.log("---h="+tmp);
      tmp = szTime.substring(pos + 1);
//Log.log("---m="+tmp);
      seconds += Integer.parseInt(tmp) * 60;
    }
    catch (Exception e) {
      Thread.dumpStack();
    }
    return seconds; //hack
  }

  public String getUpdateQueryString() {
    String qryString = "UPDATE directorsettings SET " +
        " " + EMAIL_REMINDER_FIELD + "='" + szEmailReminder +
        "', " + REMINDER_DAYS_FIELD + "='" + nReminderDays +
        "', " + NOTIFY_CHANGES_FIELD + "='" + szNotifyChanges +
        "', " + USE_TEAMS_FIELD + "='" + szUseTeams +
        "', " + DIRECTORS_CHANGE_FIELD + "='" + szDirectorsOnlyChange +
        "', " + EMAIL_ALL_FIELD + "='" + szEMailAll +
        "', " + NAME_FORMAT_FIELD + "='" + nNameFormat +
        "', " + START_DATE_FIELD + "='" + szStartDate +
        "', " + END_DATE_FIELD + "='" + szEndDate +
        "', " + USE_BLACKOUT_FIELD + "='" + nUseBlackout +
        "', " + START_BLACKOUT_FIELD + "='" + szStartBlackout +
        "', " + END_BLACKOUT_FIELD + "='" + szEndBlackout +
        "', " + REMOVE_ACCESS_FIELD + "='" + nRemoveAccess +
        "' WHERE " + PATROL_NAME_FIELD + "= '" + szPatrolName + "'";
    LOG.info(qryString);
    return qryString;
  }

  public boolean write(Connection connection) {
    String qryString;
    qryString = getUpdateQueryString();
    LOG.info(qryString);
    try {
      PreparedStatement sAssign = connection.prepareStatement(qryString);
      sAssign.executeUpdate();
    }
    catch (Exception e) {
      LOG.logException("Cannot load the driver: ", e);
//      Logger.logStatic("Most likely the Java class path is incorrect.");
      return true;
    }
    return false;
  }

  public ResultSet reset(Connection connection) {
    PreparedStatement directorStatement;
    try {
      @SuppressWarnings("SqlNoDataSourceInspection")
      String sqlQuery = "SELECT * FROM directorsettings ORDER BY \"" + PATROL_NAME_FIELD + "\"";
      LOG.logSqlStatement(sqlQuery);
      directorStatement = connection.prepareStatement(sqlQuery); //sort by default key
      return directorStatement.executeQuery();
    }
    catch (Exception e) {
      LOG.logException("Error resetting DirectorSettings table reason ", e);
    } //end try
    return null;
  }

//-----------------------------------------------------
// toString
//-----------------------------------------------------
  public String toString() {
    return szPatrolName +
        " " + EMAIL_REMINDER_FIELD + "=" + szEmailReminder +
        " " + REMINDER_DAYS_FIELD + "=" + nReminderDays +
        " " + NOTIFY_CHANGES_FIELD + "=" + szNotifyChanges +
        " " + USE_TEAMS_FIELD + "=" + szUseTeams +
        " " + DIRECTORS_CHANGE_FIELD + "=" + szDirectorsOnlyChange +
        " " + EMAIL_ALL_FIELD + "=" + szEMailAll +
        " " + NAME_FORMAT_FIELD + "=" + nNameFormat +
        " " + START_DATE_FIELD + "=" + szStartDate +
        " " + END_DATE_FIELD + "=" + szEndDate +
        " " + REMOVE_ACCESS_FIELD + "=" + nRemoveAccess;
  }

  public ObjectNode toNode() {
    ObjectNode returnNode = Utils.nodeFactory.objectNode();

    returnNode.put("formalName", szPatrolName);
    returnNode.put("changesByDirectorsOnly", "1".equals(szDirectorsOnlyChange));
    returnNode.put("whenCanUserDelete", (nRemoveAccess == 127) ? (-1) : nRemoveAccess);
    returnNode.put("patrollersCanSendMail", "1".equals(szEMailAll));
    return returnNode;
  }
}
