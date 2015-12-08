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

  private static String getAsString(String str) {
    return (str == null) ? "" : str;
  }

  public static boolean isValidEmailAddress(String emailAddress) {
    //todo someday add better email address checking
    return isNotEmpty(emailAddress) && !"&nbsp;".equals(emailAddress);
  }

  public static void printRequestParameters(String className, HttpServletRequest request) {
    String uriRequestMsg = "";
    try {
      String uri = request.getHeader("referer"); //referer: http://52.3.5.75/calendar-1/MemberLogin?resort=Afton
      if (uri != null) {
        uriRequestMsg += uri + "(";
      }
      else {
        uriRequestMsg +=className + "(";
      }
      @SuppressWarnings("unchecked")
      Enumeration<String> parameterNames = request.getParameterNames();
      while (parameterNames.hasMoreElements()) {
        String paramName = parameterNames.nextElement();
        uriRequestMsg += (" " + paramName + ":");
        String[] paramValues = request.getParameterValues(paramName);
        for (String paramValue : paramValues) {
          uriRequestMsg += (paramValue + " ");
        }
      }
//      System.out.print(" --Headers:");
//      @SuppressWarnings("unchecked")
//      Enumeration<String> headerNames = request.getHeaderNames();
//      while (headerNames.hasMoreElements()) {
//        String headerName = headerNames.nextElement();
//        System.out.print(" " + headerName + ": " + request.getHeader(headerName));
//      }
      uriRequestMsg += ")";
//      System.out.flush();
    }
    catch (Exception e) {
      uriRequestMsg += e.getMessage();
    }
    localPrintToLogFile(request, uriRequestMsg);
  }

  private static void printCommonHeader(HttpServletRequest request) {
    String fromIp = "";
//    String agent = "";
    if (request != null) {
      fromIp = request.getHeader("x-forwarded-for"); //x-forwarded-for: 216.49.181.51
//      agent = request.getHeader("user-agent");//user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_1)
//      String[] split = agent.split(" ");
//      if (split.length > 1) {
//        agent = split[0];
//      }
    }
    System.out.print("[" + getCurrentDateTimeString() + "] [" + fromIp + "] ");
  }

  public static void printToLogFile(HttpServletRequest request, String msg) {
    localPrintToLogFile(request, " - " + msg);
  }

  private static void localPrintToLogFile(HttpServletRequest request, String msg) {
    printCommonHeader(request);
    System.out.println(msg);
  }

  public static String getCurrentDateTimeString() {
    SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss SSS z");
    // java.util.Date currentTime = new java.util.Date(year,month,date);
    Calendar cal = new GregorianCalendar(TimeZone.getDefault());
    return formatter.format(cal.getTime());
  }
}
