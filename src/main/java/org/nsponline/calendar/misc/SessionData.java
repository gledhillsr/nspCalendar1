package org.nsponline.calendar.misc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

/**
 * @author Steve Gledhill
 */

public class SessionData {

  private final static Boolean DEBUG = false;
  private final static Boolean DEBUG_VERBOSE = false;

  private final static String PROPERTIES_FILE = "/var/lib/tomcat7/webapps/credentials.properties";
  //these fields should be defined in credentials.properties
  private final static String DB_USER_TAG = "dbUser";
  private final static String DB_PASSWORD_TAG = "dbPassword";
  private final static String SMTP_HOST_TAG = "smtpHost";
  private final static String POP_HOST_TAG = "popHost";
  private final static String EMAIL_USER_TAG = "emailUser";
  private final static String EMAIL_PASSWORD_TAG = "emailPassword";
  private final static String BACK_DOOR_USER_TAG = "backDoorUser";
  private final static String BACK_DOOR_PASSWORD_TAG = "backDoorPassword";
  private final static String LOGGED_IN_USER_ID_TAG = "loggedInUserId";
  private final static String LOGGED_IN_RESORT_TAG = "loggedInResort";
  private final static String AWS_ACCESS_KEY_ID_TAG = "AWSAccessKeyId";
  private final static String AWS_SECRET_KEY_TAG = "AWSSecretKey";

  //don't store in session.  Afraid user may be able to see session data
  private String dbUser;
  private String dbPassword;
  private String smtpHost;
  private String popHost;
  private String emailUser;
  private String emailPassword;
  private String backDoorUser;
  private String backDoorPassword;
  private String AWSAccessKeyId;
  private String AWSSecretKey;
  private HttpSession session;
  private HttpServletRequest request;


  public SessionData(HttpServletRequest request, PrintWriter out) {
    this.request = request;
    this.session = request.getSession();
    readCredentialPropertiesFile(new Properties(), out);
  }

  public SessionData(Properties properties, PrintWriter out) {  //called by DailyReminder
    this.session = null;
    this.request = null;
    readCredentialPropertiesFile(properties, out);
  }

  private void readCredentialPropertiesFile(Properties properties, PrintWriter out) {
    FileInputStream inStream;
    try {
      inStream = new FileInputStream(PROPERTIES_FILE);
    }
    catch (IOException e) {
      out.println("ERROR, could not open properties file.");
      return;
    }

    try {
      debugOut("reading SessionData properties file from disk.");
      properties.load(inStream);
    }
    catch (IOException e) {
      out.println("ERROR, could not read properties file");
      return;
    }
    dbUser = properties.getProperty(DB_USER_TAG, "");
    dbPassword = properties.getProperty(DB_PASSWORD_TAG, "");
    smtpHost = properties.getProperty(SMTP_HOST_TAG, "");
    popHost = properties.getProperty(POP_HOST_TAG, "");
    emailUser = properties.getProperty(EMAIL_USER_TAG, "");
    emailPassword = properties.getProperty(EMAIL_PASSWORD_TAG, "");
    backDoorUser = properties.getProperty(BACK_DOOR_USER_TAG, "");
    backDoorPassword = properties.getProperty(BACK_DOOR_PASSWORD_TAG, "");
    AWSAccessKeyId = properties.getProperty(AWS_ACCESS_KEY_ID_TAG, "");
    AWSSecretKey = properties.getProperty(AWS_SECRET_KEY_TAG, "");

    if (dbUser.isEmpty() || dbPassword.isEmpty()) {
      out.println("ERROR, could not read user information from properties file.");
      return;
    }

    //write to Tomcat logs, never to screen
    debugVerboseOut(DB_USER_TAG + "=" + dbUser);
    debugVerboseOut(DB_PASSWORD_TAG + "=" + dbPassword);
    debugVerboseOut(SMTP_HOST_TAG + "=" + smtpHost);
    debugVerboseOut(POP_HOST_TAG + "=" + popHost);
    debugVerboseOut(EMAIL_USER_TAG + "=" + emailUser);
    debugVerboseOut(EMAIL_PASSWORD_TAG + "=" + emailPassword);
    debugVerboseOut(BACK_DOOR_USER_TAG + "=" + backDoorUser);
    debugVerboseOut(BACK_DOOR_PASSWORD_TAG + "=" + backDoorPassword);
    debugVerboseOut(AWS_ACCESS_KEY_ID_TAG + "=" + AWSAccessKeyId.substring(0, 4) + "....");
    debugVerboseOut(AWS_SECRET_KEY_TAG + "=" + AWSSecretKey.substring(0, 4) + "....");
  }

  public String getDbUser() {
    return dbUser;
  }

  public String getDbPassword() {
    return dbPassword;
  }

  public String getSmtpHost() {
    return smtpHost;
  }

  public String getPopHost() {
    return popHost;
  }

  public String getEmailUser() {
    return emailUser;
  }

  public String getEmailPassword() {
    return emailPassword;
  }

  @SuppressWarnings("unused")
  public String getBackDoorUser() {
    return backDoorUser;
  }

  @SuppressWarnings("unused")
  public String getBackDoorPassword() {
    return backDoorPassword;
  }

  public String getAWSAccessKeyId() {
    return AWSAccessKeyId;
  }

  public String getAWSSecretKey() {
    return AWSSecretKey;
  }

  public void setLoggedInUserId(String loggedInUserId) {
    debugOut("SessionData.setLoggedInUserId=" + loggedInUserId);
    if (session != null) {
      session.setAttribute(LOGGED_IN_USER_ID_TAG, loggedInUserId);
    }
  }

  public void clearLoggedInUserId() {
    debugOut("SessionData.clearLoggedInUserId()");
    if (session != null) {
      session.removeAttribute(LOGGED_IN_USER_ID_TAG);
    }
  }

  public void clearLoggedInResort() {
    debugOut("SessionData.clearLoggedInResort()");
    if (session != null) {
      session.removeAttribute(LOGGED_IN_RESORT_TAG);
    }
  }

  public String getLoggedInUserId() {
    if (session == null) {
      return null;
    }
    String userId = (String) session.getAttribute(LOGGED_IN_USER_ID_TAG);
    debugOut("SessionData.getLoggedInUserId=" + userId);
    return userId;
  }

  public void setLoggedInResort(String loggedInResort) {
    debugOut("SessionData.setLoggedInResort=" + loggedInResort);
    if (session != null) {
      session.setAttribute(LOGGED_IN_RESORT_TAG, loggedInResort);
    }
  }

  public String getLoggedInResort() {
    if (session == null) {
      return null;
    }
    String resort = (String) session.getAttribute(LOGGED_IN_RESORT_TAG);
    debugOut("SessionData.getLoggedInResort=" + resort);
    return resort;
  }

  public boolean isLoggedIntoAnotherResort(String resortParameter) {
    if (session == null) {
      return true;
    }
    String resort = (String) session.getAttribute(LOGGED_IN_RESORT_TAG);
    boolean isLoggedIn = resort != null && !resort.equals(resortParameter);
    debugOut("isLoggedIntoAnotherResort = (" + isLoggedIn + " ), resort=(" + resort + "), resortParameter=(" + resortParameter + ")");
    return isLoggedIn;
  }

  private void debugOut(String msg) {
    if (DEBUG) {
      // NOSONAR
      Logger.logStatic("DEBUG-SessionData: " + msg);
    }
  }

  private void debugVerboseOut(String msg) {
    if (DEBUG_VERBOSE) {
      // NOSONAR
      Logger.logStatic("DEBUG_VERBOSE-SessionData: " + msg);
    }
  }

  public HttpServletRequest getRequest() {
    return request;
  }


}
