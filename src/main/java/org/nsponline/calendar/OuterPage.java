package org.nsponline.calendar;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

/**
 * @author Steve Gledhill
 */
public class OuterPage {
  private final static boolean DEBUG = false;

  private String javaScript;
  private ResortData resortData;

  public OuterPage(ResortData resort, String javaScriptAndStyles) {
    resortData = resort;
    this.javaScript = javaScriptAndStyles;
  }

  public void printResortHeader(PrintWriter out) {
    String header = readFile("resortHeader1.html");
    String str1 = header.
        replaceAll("__RESORT_SHORT", resortData.getResortShortName()).
        replaceAll("__RESORT_LONG", resortData.getResortFullName()).
        replaceAll("__RESORT_URL", resortData.getResortURL()).
        replaceAll("__RESORT_IMG_HEIGHT",  "" + resortData.getImageHeight()).
        replaceAll("__RESORT_IMAGE", resortData.getResortImage()).
        replaceAll("__JAVA_SCRIPT", javaScript);
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
      System.out.println("ERROR: OuterPage.readFile(" + resourceFile + ") could not find resource!");
      return "";
    }
    String fileName = fileResource.getFile();
    String result = "";
    byte[] buffer = new byte[4000];
    try {
      FileInputStream inputStream = new FileInputStream(fileName);
      while(inputStream.read(buffer) != -1) {
        result += new String(buffer);
      }

      // Always close files.
      inputStream.close();
    }
    catch (FileNotFoundException ex) {
      System.out.println("Error, unable to open file '" + fileName + "'");
    }
    catch (IOException ex) {
      System.out.println("Error reading file '" + fileName + "'");
    }
    debugOut("readFile: bytes read=" + result.length());
    return result;
  }

  private void debugOut(String msg) {
    if (DEBUG) {
      System.out.println("DEBUG: OuterPage: " + msg);
    }
  }
}
