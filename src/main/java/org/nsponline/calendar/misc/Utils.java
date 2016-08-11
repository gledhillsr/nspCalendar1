package org.nsponline.calendar.misc;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Steve Gledhill
 */
public final class Utils {

  private static final String EMAIL_PATTERN =
      "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
          + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

  public static String szMonthsFull[] = {
      "January", "February", "March", "April", "May", "June",
      "July", "August", "September", "October", "November", "December"
  };
  public static String szDays[] = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "error"};
  public static JsonNodeFactory nodeFactory = JsonNodeFactory.instance;


  private Utils() {
    //nothing to do
  }

  public static boolean isEmpty(String str) {
    //noinspection Since15
    return str == null || str.isEmpty();
  }

  public static boolean isNotEmpty(String str) {
    //noinspection Since15
    return str != null && !str.isEmpty();
  }

  public static boolean isValidEmailAddress(String emailAddress) {
    Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    Matcher matcher = pattern.matcher(emailAddress);
    return matcher.matches();
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
          uriRequestMsg += "Password".equals(paramName) ? "ZZZZ" : (paramValue + " ");
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
      uriRequestMsg += e.getMessage() + " exception";
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
    log("[" + getCurrentDateTimeString() + "] [" + fromIp + "] ");
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

  public static void build204Response(HttpServletResponse response) throws IOException {
    response.setStatus(204);
  }

  public static void buildOkResponse(HttpServletResponse response, ObjectNode returnNode) throws IOException {
    log("Response OK: " + returnNode.toString());
    response.setStatus(200);
    response.setContentType("application/json");
    log(returnNode.toString());
    response.getWriter().write(returnNode.toString());
  }

  public static void buildErrorResponse(HttpServletResponse response, int status, String errString) throws IOException {
    log("Response Error: " + status + ": " + errString);
    response.setStatus(status);
    response.addHeader("X-Reason", errString);
  }

  @SuppressWarnings("WeakerAccess")
  public static void log(String msg) {
    System.out.println(msg);
  }

  public static int convertToInt(String szNumber) {
    if (isEmpty(szNumber)) {
      return 0;
    }
    try {
      return Integer.valueOf(szNumber);
    }
    catch (NumberFormatException e) {
      return 0;
    }
  }
}
