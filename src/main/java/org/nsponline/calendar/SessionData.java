package org.nsponline.calendar;

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

  public SessionData(HttpSession session, PrintWriter out) {
    this.session = session;
    readCredentials(new Properties(), out);
  }

  public SessionData(Properties properties, PrintWriter out) {
    readCredentials(properties, out);
  }

  private void readCredentials(Properties properties, PrintWriter out) {
    FileInputStream inStream;
    try {
      inStream = new FileInputStream(PROPERTIES_FILE);
    }
    catch (IOException e) {
      out.println("ERROR, could not open properties file.");
      return;
    }

    try {
      if (DEBUG) {
        System.out.println("reading SessionData properties file from disk.");
      }
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

    if (DEBUG_VERBOSE) {
      //write to Tomcat logs, never to screen
      System.out.println(DB_USER_TAG + "=" + dbUser);
      System.out.println(DB_PASSWORD_TAG + "=" + dbPassword);
      System.out.println(SMTP_HOST_TAG + "=" + smtpHost);
      System.out.println(POP_HOST_TAG + "=" + popHost);
      System.out.println(EMAIL_USER_TAG + "=" + emailUser);
      System.out.println(EMAIL_PASSWORD_TAG + "=" + emailPassword);
      System.out.println(BACK_DOOR_USER_TAG + "=" + backDoorUser);
      System.out.println(BACK_DOOR_PASSWORD_TAG + "=" + backDoorPassword);
      System.out.println(AWS_ACCESS_KEY_ID_TAG + "=" + AWSAccessKeyId.substring(0,2) + "....");
      System.out.println(AWS_SECRET_KEY_TAG + "=" + AWSSecretKey.substring(0,2) + "....");
    }
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
    if (DEBUG) {
      System.out.println("SessionData.setLoggedInUserId=" + loggedInUserId);
    }
    session.setAttribute(LOGGED_IN_USER_ID_TAG, loggedInUserId);
  }

  public String getLoggedInUserId() {
    String userId = (String) session.getAttribute(LOGGED_IN_USER_ID_TAG);
    if (DEBUG) {
      System.out.println("SessionData.getLoggedInUserId=" + userId);
    }
    return userId;
  }

  public void setLoggedInResort(String loggedInResort) {
    if (DEBUG) {
      System.out.println("SessionData.setLoggedInResort=" + loggedInResort);
    }
    session.setAttribute(LOGGED_IN_RESORT_TAG, loggedInResort);
  }

  public String getLoggedInResort() {
    String resort = (String) session.getAttribute(LOGGED_IN_RESORT_TAG);
    if (DEBUG) {
      System.out.println("SessionData.getLoggedInResort=" + resort);
    }
    return resort;
  }

  public boolean isLoggedIntoAnotherResort(String resortParameter) {
    String resort = (String) session.getAttribute(LOGGED_IN_RESORT_TAG);
    if (DEBUG) {
      System.out.println("SessionData.isLoggedIntoAnotherResort resort=" + resort + ", resortParameter=" + resortParameter);
    }
    return resort != null && !resort.equals(resortParameter);
  }
}
