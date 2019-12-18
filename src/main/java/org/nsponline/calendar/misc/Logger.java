package org.nsponline.calendar.misc;

import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.http.HttpServletRequest;

public class Logger {
  private String className;
  @SuppressWarnings("unused")
  private String ip;
  private String agent;
  private String methodType;
  //  private String resort = "und";
  private HttpServletRequest tempRequest; //temporary, just save elements to display

  public Logger() {
    //don't ever use this
  }

  public Logger(Class<?> clazz) {  //todo get rid of this
    this.className = clazz.getName();
    ip = "";
    agent = "";
  }

  public Logger(final Class aClass, Logger parentLogger) {
    this(aClass, parentLogger != null ? parentLogger.getRequest() : null, "");
  }

  public Logger(final Class aClass, final HttpServletRequest request, final String methodType) {
    this.tempRequest = request;
    this.methodType = methodType;
    this.className = aClass != null ? aClass.getName() : "WIP - ERROR: Class was null in Logger constructor";
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
      String referer = request.getHeader("referer");
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

  public void logRequestParameters() {
    StringBuilder uriRequestMsg = new StringBuilder("logLevel=" + LogLevel.INFO + " class=" + className + " method=" + methodType + " ");
    try {
      String uri = tempRequest.getHeader("referer"); //referer: http://52.3.5.75/calendar-1/MemberLogin?resort=Afton
      if (uri != null) {
        //noinspection StringConcatenationInsideStringBufferAppend
        uriRequestMsg.append(" path=\"" + uri + "\" (");
      } else {
        uriRequestMsg.append(" missingPath (");
      }
      @SuppressWarnings("unchecked")
      Enumeration<String> parameterNames = tempRequest.getParameterNames();
      while (parameterNames.hasMoreElements()) {
        Set<String> foundParam = new HashSet<String>();
        String paramName = parameterNames.nextElement();
        //noinspection StringConcatenationInsideStringBufferAppend
        uriRequestMsg.append(" " + paramName + "=");
        String[] paramValues = tempRequest.getParameterValues(paramName);
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
    writeToLogFile(tempRequest, "undefined", uriRequestMsg.toString());
  }

  public HttpServletRequest getRequest() {
    return tempRequest;
  }

  public void debug(String resort, String msg) {
    writeToLogFile(tempRequest, resort, "DEBUG: " + msg);
  }

  @SuppressWarnings("unused")
  public void info(String resort, String msg) {
    writeToLogFile(tempRequest, resort, "INFO: " + msg);
  }

  @SuppressWarnings("unused")
  public void warn(String resort, String msg) {
    writeToLogFile(tempRequest, resort, "WARN: " + msg);
  }

  public void error(String resort, String msg) {
    writeToLogFile(tempRequest, resort, "ERROR: " + msg);
  }

  public void logSqlStatement(String resort, String qryString) {
    printCommonHeader(resort, ip);
    System.out.println(" sql=\"" + qryString + "\"");
  }


  //todo ---------- make these NOT static -----------------
  public static void logSqlStatementStatic(String resort, String qryString) { //todo hack
    printCommonHeader(resort, "ipString");
    System.out.println(" sql=\"" + qryString + "\"");
  }

  private static void printCommonHeader(String resort, String ip) {
    System.out.print("[" + getCurrentDateTimeString() + "] " + " ip=" + ip + ", resort=" + resort + " ");
  }

  private static void writeToLogFile(HttpServletRequest request, String resort, String msg) {
    printCommonHeader(request, resort);
    System.out.println(msg);
  }


  public static void log(String msg) { //todo hack, use printToLogFile(request, msg)
    System.out.println(msg);
  }

  public static void printToLogFile(HttpServletRequest request, String resort, String msg) {
    writeToLogFile(request, resort, " - " + msg);
  }

  public static void printToLogFile(HttpServletRequest request, String resort, String user, String msg) {
    writeToLogFile(request, resort, ", user=" + user + ", message=\"" + msg + "\"");
  }

  public void logRequestParameters(String method, HttpServletRequest request) { //todo hack get rid of this
    this.methodType = method;
    this.tempRequest = request;
    logRequestParameters();
  }

  //------ these are OK for static (used by exception processing)
  public static void logException(String resort, String msg, Exception e) {
    printCommonHeader((HttpServletRequest)null, resort);
    System.out.println(msg + "exceptionCause=\"" + e.getCause() + "\", exceptionMessage=\"" + e.getMessage() + "\" " + e.toString());
  }

  private static void printCommonHeader(HttpServletRequest request, String resort) {
    String fromIp = "";
    if (request != null) {
      fromIp = request.getHeader("x-forwarded-for"); //x-forwarded-for: 216.49.181.51
    }
    System.out.print("[" + getCurrentDateTimeString() + "] " + " ip=" + fromIp + ", resort=" + resort + " ");
  }

  private static String getCurrentDateTimeString() {
    //format: 2018-02-18 20:35:41,712
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
    // java.util.Date currentTime = new java.util.Date(year,month,date);
    Calendar cal = new GregorianCalendar(TimeZone.getDefault());
    return formatter.format(cal.getTime());
  }
}
