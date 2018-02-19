package org.nsponline.calendar;

import com.mysql.jdbc.StringUtils;
import org.nsponline.calendar.misc.Logger;
import org.nsponline.calendar.misc.ResortData;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

/**
 * @author Steve Gledhill
 */
public class OuterPage {
  private static final boolean DEBUG = false;

  private String javaScript;
  private String loggedInUserId;
  private ResortData resortData;
  private static final int MAX_BUFFER = 4000;


  public OuterPage(ResortData resort, String javaScriptAndStyles, String loggedInUserId) {
    resortData = resort;
    this.javaScript = javaScriptAndStyles;
    this.loggedInUserId = loggedInUserId;
  }

  public void printResortHeader(PrintWriter out) {
    String header;
    if ("Brighton".equals(resortData.getResortShortName())) {
      header = readFile("brightonHeader1.html");
    }
    else {
      header = readFile("resortHeader1.html");
    }
    String userIdTag = StringUtils.isNullOrEmpty(loggedInUserId) ? "" : "&ID=" + loggedInUserId;
    String str1 = header.
        replaceAll("__RESORT_SHORT", resortData.getResortShortName()).
        replaceAll("__RESORT_LONG", resortData.getResortFullName()).
        replaceAll("__USER_ID_TAG", userIdTag).
        replaceAll("__RESORT_URL", resortData.getResortURL()).
        replaceAll("__RESORT_IMG_HEIGHT", "" + resortData.getImageHeight()).
        replaceAll("__RESORT_IMAGE", resortData.getResortImage()).
        replaceAll("__JAVA_SCRIPT", javaScript);
    if (!StringUtils.isNullOrEmpty(loggedInUserId)) {
      String buttonClass = "Brighton".equals(resortData.getResortShortName()) ? "class='button'" : "";
      String logout = "<a " + buttonClass + " href='/calendar-1/Logout?resort=" + resortData.getResortShortName() + "' "
          + "target='_self'>Logout</a>";
      str1 = str1.replace("__LOGOUT", logout);
    }
    else {
      str1 = str1.replace("__LOGOUT", "");
    }
    debugOut("processed header=" + str1);
    out.println(str1);
  }

  public void printResortFooter(PrintWriter out) {
    String footer = readFile("resortFooter1.html");
    debugOut("processed footer=" + footer);
    out.println(footer);
  }

  public String readFile(String resourceFile) {
    ClassLoader classLoader = getClass().getClassLoader();
    URL fileResource = classLoader.getResource(resourceFile);
    debugOut("readFile: fileResource=" + fileResource);
    if (fileResource == null) {
      errorOut("ERROR: OuterPage.readFile(" + resourceFile + ") could not find resource!");
      return "";
    }
    String fileName = fileResource.getFile();
    //noinspection StringBufferMayBeStringBuilder
    StringBuffer result = new StringBuffer();
    byte[] buffer = new byte[MAX_BUFFER];
    try {
      FileInputStream inputStream = new FileInputStream(fileName);
      while (inputStream.read(buffer) != -1) {
        result.append(new String(buffer));
      }

      // Always close files.
      inputStream.close();
    }
    catch (FileNotFoundException ex) {
      errorOut("Error, unable to open file '" + fileName + "'");
    }
    catch (IOException ex) {
      errorOut("Error reading file '" + fileName + "'");
    }
    debugOut("readFile: bytes read=" + result.length());
    return result.toString();
  }

  private void errorOut(String msg) {
    // nosonar
    Logger.log("ERROR: OuterPage: " + msg);
  }

  private void debugOut(String msg) {
    if (DEBUG) {
      // nosonar
      Logger.log("DEBUG: OuterPage: " + msg);
    }
  }
}
