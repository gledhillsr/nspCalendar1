package org.nsponline.calendar.utils;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Steve Gledhill
 */
public final class StaticUtils {

  @SuppressWarnings("RegExpRedundantEscape")
  private static final String EMAIL_PATTERN =
      "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
          + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

  public static final String[] szMonthsFull = {
      "January", "February", "March", "April", "May", "June",
      "July", "August", "September", "October", "November", "December"
  };
  public static final String[] szDays = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "error"};
  public static JsonNodeFactory nodeFactory = JsonNodeFactory.instance;


  public static boolean isEmpty(String str) {
    return str == null || str.isEmpty();
  }

  public static boolean isNotEmpty(String str) {
    return str != null && !str.isEmpty();
  }

  public static boolean isValidEmailAddress(String emailAddress) {
    Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    Matcher matcher = pattern.matcher(emailAddress);
    return matcher.matches();
  }

  public static void build204Response(HttpServletResponse response, Logger LOG) {
    LOG.info("Response OK (204):");
    response.setStatus(204);
  }

  public static void buildOkResponse(HttpServletResponse response, ObjectNode returnNode, Logger LOG) throws IOException {
    LOG.info("Response OK: " + returnNode.toString());
    response.setStatus(200);
    response.setContentType("application/json");
    LOG.info(returnNode.toString());
    response.getWriter().write(returnNode.toString());
  }

  public static boolean isRequestFromBot(String userAgent) {
    if (!StaticUtils.isEmpty(userAgent)) {
      String upperUserAgent = userAgent.toUpperCase();
      return upperUserAgent.contains("BOT") || upperUserAgent.contains("SPIDER") || upperUserAgent.contains("CRAWLER");
    }
    return false;  //cannot tell
  }

  public static void buildAndLogErrorResponse(HttpServletResponse response, int status, String errString, Logger LOG) {
    LOG.error("Response Error: " + status + ": " + errString);
    response.setStatus(status);
    response.addHeader("X-Reason", errString);
  }

  public static int convertToInt(String szNumber) {
    if (isEmpty(szNumber)) {
      return 0;
    }
    try {
      return Integer.parseInt(szNumber);
    }
    catch (NumberFormatException e) {
      System.out.println("NumberFormatException on [" + szNumber + "]");
      return 0;
    }
  }
}
