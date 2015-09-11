package org.nsponline.calendar;

/**
 * @author Steve Gledhill
 */
public class Utils {

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
}
