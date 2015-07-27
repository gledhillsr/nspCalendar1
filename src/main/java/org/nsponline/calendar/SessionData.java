package org.nsponline.calendar;

import javax.servlet.ServletContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

/**
 * @author Steve Gledhill
 */
public class SessionData {

  private final static Boolean DEBUG = true;

  private final static String PROPERTIES_FILE = "/var/lib/tomcat7/webapps/credentials.properties";
  //these fields should be defined in credentials.properties
  private final static String DB_USER = "dbUser";
  private final static String DB_PASSWORD = "dbPassword";
  private final static String SMTP_HOST = "smtpHost";
  private final static String POP_HOST = "popHost";
  private final static String EMAIL_USER = "emailUser";
  private final static String EMAIL_PASSWORD = "emailPassword";
  private final static String BACK_DOOR_USER = "backDoorUser";
  private final static String BACK_DOOR_PASSWORD = "backDoorPassword";
  private final static String LOGGED_IN_USER_ID = "loggedInUserId";

  private String dbUser;
  private String dbPassword;
  private String smtpHost;
  private String popHost;
  private String emailUser;
  private String emailPassword;
  private String backDoorUser;
  private String backDoorPassword;
  private ServletContext servletContext;

  public SessionData(ServletContext servletContext, PrintWriter out) {
    this.servletContext = servletContext;
    readCredentials(out);
  }

  public SessionData() {
    throw new IllegalStateException("SessionData not yet implemented");
  }

  private void readCredentials(PrintWriter out) {
    Properties properties = new Properties();
    FileInputStream inStream;
    try {
      inStream = new FileInputStream(PROPERTIES_FILE);
    }
    catch (IOException e) {
      out.println("ERROR, could not open properties file.");
      return;
    }

    try {
      properties.load(inStream);
    }
    catch (IOException e) {
      out.println("ERROR, could not read properties file");
      return;
    }
    dbUser = properties.getProperty(DB_USER, "");
    dbPassword = properties.getProperty(DB_PASSWORD, "");
    smtpHost = properties.getProperty(SMTP_HOST, "");
    popHost = properties.getProperty(POP_HOST, "");
    emailUser = properties.getProperty(EMAIL_USER, "");
    emailPassword = properties.getProperty(EMAIL_PASSWORD, "");
    backDoorUser = properties.getProperty(BACK_DOOR_USER, "");
    backDoorPassword = properties.getProperty(BACK_DOOR_PASSWORD, "");

    if (dbUser.isEmpty() || dbPassword.isEmpty()) {
      out.println("ERROR, could not read user information from properties file.");
      return;
    }
    servletContext.setAttribute(DB_USER, dbUser);
    servletContext.setAttribute(DB_PASSWORD, dbPassword);
    servletContext.setAttribute(SMTP_HOST, smtpHost);
    servletContext.setAttribute(POP_HOST, popHost);
    servletContext.setAttribute(EMAIL_USER, emailUser);
    servletContext.setAttribute(EMAIL_PASSWORD, emailPassword);
    servletContext.setAttribute(BACK_DOOR_USER, backDoorUser);
    servletContext.setAttribute(BACK_DOOR_PASSWORD, backDoorPassword);
    if (DEBUG) {
      //write to Tomcat logs, never to screen
      System.out.println(DB_USER + "=" + dbUser);
      System.out.println(DB_PASSWORD + "=" + dbPassword);
      System.out.println(SMTP_HOST + "=" + smtpHost);
      System.out.println(POP_HOST + "=" + popHost);
      System.out.println(EMAIL_USER + "=" + emailUser);
      System.out.println(EMAIL_PASSWORD + "=" + emailPassword);
      System.out.println(BACK_DOOR_USER + "=" + backDoorUser);
      System.out.println(BACK_DOOR_PASSWORD + "=" + backDoorPassword);
    }
  }

  public String getDbUser() {
    if (DEBUG) {
      System.out.println("dbUser=" + dbUser);
    }
    return dbUser;
  }

  public String getDbPassword() {
    if (DEBUG) {
      System.out.println("dbPassword=" + dbPassword);
    }
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

  public String getBackDoorUser() {
    return backDoorUser;
  }

  public String getBackDoorPassword() {
    return backDoorPassword;
  }

  public void setLoggedInUserId(String loggedInUserId) {
    if (DEBUG) {
      System.out.println("setLoggedInUserId=" + loggedInUserId);
    }
    servletContext.setAttribute(LOGGED_IN_USER_ID, loggedInUserId);
  }

  public String getLoggedInUserId() {
    String userId = (String) servletContext.getAttribute(LOGGED_IN_USER_ID);
    if (DEBUG) {
      System.out.println("getLoggedInUserId=" + userId);
    }
    return userId;
  }
}
