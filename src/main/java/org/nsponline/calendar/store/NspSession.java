package org.nsponline.calendar.store;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import org.nsponline.calendar.misc.Logger;

/**
 * @author Steve Gledhill
 *
 * Store all session data required to support the REST API.
 */
public class NspSession {

  private static final String TABLE_NAME = "session";

  static final String SESSION_ID = "sessionId";
  static final String AUTHENTICATED_USER_ID = "authenticatedUserId";
  static final String RESORT = "resort";
  static final String SESSION_CREATE_TIME = "sessionCreateTime";
  static final String SESSION_LAST_ACCESS_TIME = "sessionLastAccessTime";
  static final String SESSION_IP_ADDRESS = "sessionIpAddress";
  static final String IS_DIRECTOR = "isDirector";

  String sessionId;   //36 character guid
  String authenticatedUser;
  String resort;
  Date sessionCreateTime;
  Date lastSessionAccessTime;
  String sessionIpAddress;
  boolean isDirector;

  public NspSession(String sessionId, String authenticatedUser, String resort, Date sessionCreateTime, Date lastSessionAccessTime, String sessionIpAddress, boolean isDirector) {
    this.sessionId = sessionId;
    this.authenticatedUser = authenticatedUser;
    this.resort = resort;
    this.sessionCreateTime = sessionCreateTime;
    this.lastSessionAccessTime = lastSessionAccessTime;
    this.sessionIpAddress = sessionIpAddress;
    this.isDirector = isDirector;
  }

  public static NspSession read(Connection connection, String sessionId) {
    if (sessionId == null || sessionId.length() != 36) {
      return null;
    }
    PreparedStatement sessionStatement = null;
    @SuppressWarnings("SqlNoDataSourceInspection")
    String str = "SELECT * FROM " + TABLE_NAME + " WHERE " + SESSION_ID + " = ?" ;
    try {
      sessionStatement = connection.prepareStatement(str);
      sessionStatement.setString(1, sessionId);
      ResultSet sessionResults = sessionStatement.executeQuery();
      if (sessionResults.next()) {
        String authenticatedUser = sessionResults.getString(AUTHENTICATED_USER_ID);
        String resort = sessionResults.getString(RESORT);
        Date sessionCreateTime = sessionResults.getDate(SESSION_CREATE_TIME);
        Date lastSessionAccessTime = sessionResults.getDate(SESSION_LAST_ACCESS_TIME);
        String sessionIpAddress = sessionResults.getString(SESSION_IP_ADDRESS);
        boolean isDirector = sessionResults.getBoolean(IS_DIRECTOR);
        NspSession nspSession = new NspSession(sessionId, authenticatedUser, resort, sessionCreateTime, lastSessionAccessTime, sessionIpAddress, isDirector);
        //todo log something
        return nspSession;
      }
    }
    catch (SQLException e) {
      e.printStackTrace();
      //todo ...
    }
    finally {
      if (sessionStatement != null) {
        try {
          sessionStatement.close();
        }
        catch (SQLException e) {
          //todo ...
        }
      }
    }
    return null;
  }

  public boolean insertRow(Connection connection) {
    @SuppressWarnings("SqlNoDataSourceInspection")
    String qryString = "INSERT INTO session Values(\'" + sessionId + "', \"" +
        authenticatedUser + "\", \"" + resort + "\", \"" + sessionCreateTime + "\", \"" + lastSessionAccessTime + "\", \"" +
        sessionIpAddress + "\", \"" + (isDirector? 1:0) + "\")";

//    String qryString = newIndividualAssignment.getInsertSQLString(sessionData);
    Logger.logSqlStatement(qryString);
    try {
      PreparedStatement sAssign = connection.prepareStatement(qryString);
      sAssign.executeUpdate();
      return true;
    }
    catch (SQLException e) {
      Logger.logException("Cannot insert session", e);
      return false;
    }
//    String str = "insert into session (sessionId, authenticatedUserId, resort, sessionCreateTime, lastSessionAccessTime, sessionIpAddress, isDirector) values (?, ?, ?, ?, ?, ?, ?)";
//    PreparedStatement insertStatement = null;
//    try {
//      insertStatement = connection.prepareStatement(str);
//
//      insertStatement.setString(1, sessionId);
//      insertStatement.setString(2, authenticatedUser);
//      insertStatement.setString(3, resort);
//      insertStatement.setDate(4, sessionCreateTime);
//      insertStatement.setDate(5, lastSessionAccessTime);
//      insertStatement.setString(6, sessionIpAddress);
//      insertStatement.setBoolean(7, isDirector);
//      Boolean isResultSet = insertStatement.execute();
//      Log.log("isResultSet = " + isResultSet);
//      if (isResultSet) {
//        Log.log("resultset=" + insertStatement.getResultSet().toString());
//      }
//      //todo log something
//      connection.commit();
//      return true;
//    }
//    catch (SQLException e) {
//      e.printStackTrace();
//      //todo ...
//    }
//    finally {
//      if (insertStatement != null) {
//        try {
//          insertStatement.close();
//        }
//        catch (SQLException e) {
//          e.printStackTrace();
//          //todo ...
//        }
//      }
//    }
//    return false;
  }

  public boolean deleteRow(Connection connection) {
    @SuppressWarnings("SqlNoDataSourceInspection")
    String qryString = "DELETE FROM session WHERE " + SESSION_ID + " = '" + sessionId + "'";
    Logger.logSqlStatement(qryString);
    try {
      PreparedStatement sAssign = connection.prepareStatement(qryString);
      sAssign.executeUpdate();
      return true;
    }
    catch (SQLException e) {
      Logger.logException("Cannot insert session, reason ", e);
      return false;
    }
  }

  public String getAuthenticatedUser() {
    return authenticatedUser;
  }

  public Date getSessionCreateTime() {
    return sessionCreateTime;
  }

  public Date getLastSessionAccessTime() {
    return lastSessionAccessTime;
  }

  public void setLastSessionAccessTime(Date lastSessionAccessTime) {
    this.lastSessionAccessTime = lastSessionAccessTime;
  }

  public String getSessionIpAddress() {
    return sessionIpAddress;
  }

  public boolean isDirector() {
    return isDirector;
  }

  public String getResort() {
    return resort;
  }

  //--------------- SQL support --------------------
//  String getDeleteSessionSQLString() {
//    int i;
//    String qryString = "DELETE FROM " + TABLE_NAME + " WHERE " + SESSION_ID + " = '" + sessionId + "'";
//    PatrolData.logger(resort, qryString);
//    return qryString;
//  }
//
//  String getLastSessionAccessTimeUpdateSQLString() {
//    String qryString = "UPDATE " + TABLE_NAME + " SET " +
//        " " + SESSION_LAST_ACCESS_TIME + "='" + lastSessionAccessTime +
//        "' WHERE " + SESSION_ID + "= '" + sessionId + "'";
//    PatrolData.logger(resort, qryString);
//    return qryString;
//  }
//
//  public boolean writeLastSessionAccessTime(Connection connection) {
//    String qryString = getLastSessionAccessTimeUpdateSQLString();
//    PatrolData.logger(resort, "writeLastSessionAccessTime: " + qryString);
//    try {
//      PreparedStatement sAssign = connection.prepareStatement(qryString);
//      sAssign.executeUpdate();
//    }
//    catch (SQLException e) {
//      PatrolData.logger(resort, "Failed writeShift, reason:" + e.getMessage());
//      return true;
//    }
//    return false;
//  }
}
