package org.nsponline.calendar;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * @author Steve Gledhill
 */
public final class Utils {

  private Utils() {
    //nothing to do
  }

  public static boolean isEmpty(String str) {
    return str == null || str.isEmpty();
  }

  public static boolean isNotEmpty(String str) {
    return str != null && !str.isEmpty();
  }

  public static boolean isValidEmailAddress(String emailAddress) {
    //todo someday add better email address checking
    return isNotEmpty(emailAddress) && !"&nbsp;".equals(emailAddress);
  }

  public static void dumpRequestParameters(String className, HttpServletRequest request) {
    try {
      System.out.print(className + " (" + getCurrentDateTimeString() + ") PARAMETERS [");
      @SuppressWarnings("unchecked")
      Enumeration<String> parameterNames = request.getParameterNames();
      while (parameterNames.hasMoreElements()) {
        String paramName = parameterNames.nextElement();
        System.out.print(" " + paramName + ":");
        String[] paramValues = request.getParameterValues(paramName);
        for (String paramValue : paramValues) {
          System.out.print(paramValue + " ");
        }
      }
      System.out.println("]");
    }
    catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  public static String getCurrentDateTimeString() {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    // java.util.Date currentTime = new java.util.Date(year,month,date);
    Calendar cal = new GregorianCalendar(TimeZone.getDefault());
    return formatter.format(cal.getTime());
  }
}
