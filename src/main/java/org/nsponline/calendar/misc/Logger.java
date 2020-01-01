package org.nsponline.calendar.misc;

import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.http.HttpServletRequest;

public class Logger {
  private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");

  private String className;
  @SuppressWarnings("unused")
  private String ip;
  private String agent;
  private String methodType;
  private String resort;
  private HttpServletRequest request; //temporary, just save elements to display

  public Logger(Class<?> clazz) {  //todo get rid of this
    this.className = clazz.getSimpleName();
    ip = "";
    agent = "";
  }

  public Logger(final Class<?> aClass, Logger parentLogger, String resort) {
    this(aClass,
         parentLogger != null ? parentLogger.getRequest() : null,
         "",
         (resort == null && parentLogger != null) ? parentLogger.getResort() : resort);
  }

  public Logger(final Class<?> aClass, final HttpServletRequest request, final String methodType, String resort) {
    this.request = request;
    this.methodType = methodType;
    this.className = aClass != null ? aClass.getSimpleName() : "WIP - ERROR: Class was null in Logger constructor";

    this.resort = resort == null ? request.getParameter("resort") : resort;
    if (request == null) {
      if (!"DailyReminder".equals(methodType)) {
        System.out.println("Null request in Logger, parent NotYetImplemented. calling dumpStack. methodType=" + methodType);
        Thread.dumpStack();
      }
      ip = "<<null request>>";
      agent = "";
    } else {
      ip = request.getHeader("x-forwarded-for"); //x-forwarded-for: 216.49.181.51
      agent = request.getHeader("user-agent");//user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_1)
      //get 'resort' the hard way
//      String referer = request.getHeader("referer");
//todo, fails of logout
//      if (referer != null) {
//        int startIndex = referer.indexOf("resort=");
//        if (startIndex >= 0) {
//          startIndex += 7;
//          int posNextField = referer.substring(startIndex).indexOf("&");
//          int endIndex = posNextField == -1 ? referer.length() : posNextField;
//          if (endIndex > referer.length()) {
//            endIndex = referer.length();
//          }
//          resort = " resort=" + referer.substring(startIndex, endIndex) + " ";
//        }
//      }
    }
    String[] split = agent.split(" ");
    if (split.length > 1) {
      agent = split[0];
    }
  }

  public String getResort() {
    return resort == null ? "undefined" : resort;
  }

  public void logRequestParameters() {
    StringBuilder uriRequestMsg = new StringBuilder("logLevel=" + LogLevel.INFO + " class=" + className + " method=" + methodType + " ");
    try {
      String uri = request.getHeader("referer"); //referer: http://52.3.5.75/calendar-1/MemberLogin?resort=Afton
      if (uri != null) {
        //noinspection StringConcatenationInsideStringBufferAppend
        uriRequestMsg.append(" path=\"" + uri + "\" (");
      } else {
        uriRequestMsg.append(" missingPath (");
      }
      @SuppressWarnings("unchecked")
      Enumeration<String> parameterNames = request.getParameterNames();
      while (parameterNames.hasMoreElements()) {
        Set<String> foundParam = new HashSet<String>();
        String paramName = parameterNames.nextElement();
        //noinspection StringConcatenationInsideStringBufferAppend
        uriRequestMsg.append(" " + paramName + "=");
        String[] paramValues = request.getParameterValues(paramName);
        String delimiter = paramValues.length > 1 ? ":" : "";
        for (String paramValue : paramValues) {
          if (!foundParam.contains(paramName)) {
            uriRequestMsg.append("Password".equals(paramName) ? "ZZZZ" : (paramValue + delimiter));
          }
          foundParam.add(paramValue);
        }
        uriRequestMsg.append(parameterNames.hasMoreElements() ? ", " : " ");
      }
//      System.out.print(" --Headers:");
//      @SuppressWarnings("unchecked")
//      Enumeration<String> headerNames = request.getHeaderNames();
//      while (headerNames.hasMoreElements()) {
//        String headerName = headerNames.nextElement();
//        System.out.print(" " + headerName + ": " + request.getHeader(headerName));
//      }
      uriRequestMsg.append(") ");
//      System.out.flush();
    } catch (Exception e) {
      //noinspection StringConcatenationInsideStringBufferAppend
      uriRequestMsg.append(" exception=\"" + e.getMessage() + "\"");
    }
    writeToLogFile(request, uriRequestMsg.toString());
  }

  public HttpServletRequest getRequest() {
    return request;
  }

  public void debug(String msg) {
    writeToLogFile(request, "DEBUG: " + msg);
  }

  @SuppressWarnings("unused")
  public void info(String msg) {
    writeToLogFile(request, "INFO: " + msg);
  }

  @SuppressWarnings("unused")
  public void warn(String msg) {
    writeToLogFile(request, "WARN: " + msg);
  }

  public void error(String msg) {
    writeToLogFile(request, "ERROR: " + msg);
  }

  public void logSqlStatement(String qryString) {
    printCommonHeader(ip);
    System.out.println(" sql=\"" + qryString + "\"");
  }

  private void writeToLogFile(HttpServletRequest request, String msg) {
    printCommonHeader(request);
    System.out.println(msg);
  }

  private void printCommonHeader(String ip) {
    System.out.print("[" + getCurrentDateTimeString() + "] " + " ip=" + ip + ", resort=" + resort + " ");
  }

  private void printCommonHeader(HttpServletRequest request) {
    String fromIp = "";
    if (request != null) {
      fromIp = request.getHeader("x-forwarded-for"); //x-forwarded-for: 216.49.181.51
    }
    System.out.print("[" + getCurrentDateTimeString() + "] " + " ip=" + fromIp + ", resort=" + resort + " ");
  }

  public void logRequestParameters(String method, HttpServletRequest request) { //todo hack get rid of this
    this.methodType = method;
    this.request = request;
    logRequestParameters();
  }

  public void logException(String msg, Exception e) {
    printCommonHeader("ipAddress");
    System.out.println(msg + "exceptionCause=\"" + e.getCause() + "\", exceptionMessage=\"" + e.getMessage() + "\" " + e.toString());
  }

  //todo ---------- make these NOT static -----------------
  public static void printToLogFileStatic(HttpServletRequest request, String resort, String msg) {
    writeToLogFileStatic(request, resort, " - " + msg);
  }

  public static void printToLogFileStatic(HttpServletRequest request, String resort, String user, String msg) {
    writeToLogFileStatic(request, resort, ", user=" + user + ", message=\"" + msg + "\"");
  }

  private static void writeToLogFileStatic(HttpServletRequest request, String resort, String msg) {
    printCommonHeaderStatic(request, resort);
    System.out.println(msg);
  }

  public static void logStatic(String msg) { //todo hack, use printToLogFile(request, msg)
    System.out.println(msg);
  }

  private static void printCommonHeaderStatic(HttpServletRequest request, String resort) {
    String fromIp = "undef";
    if (request != null) {
      fromIp = request.getHeader("x-forwarded-for"); //x-forwarded-for: 216.49.181.51
    }
    System.out.print("[" + getCurrentDateTimeString() + "] " + " ip=" + fromIp + ", resort=" + resort + " ");
  }

  private static String getCurrentDateTimeString() {
    //format: 2018-02-18 20:35:41,712
    // java.util.Date currentTime = new java.util.Date(year,month,date);
    Calendar gregorianCalendar = new GregorianCalendar(TimeZone.getDefault());
    return formatter.format(gregorianCalendar.getTime());
  }
}
