package org.nsponline.calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Steve Gledhill
 */
public class CookieID {
  final static String NSP_goto = "NSPgoto";
  final static boolean trace = true;

  //Instance data
  String szID = null;
  boolean error;

  @SuppressWarnings("UnusedParameters")
  public CookieID(SessionData sessionData, HttpServletRequest request, HttpServletResponse response, String parent, String owner) {
    error = false;
    szID = request.getParameter("ID");
    if (szID == null || szID.isEmpty()) {
      szID = sessionData.getLoggedInUserId();
      System.out.println("get user id from session(" + szID + ")");
    }
    else if (trace) {
      System.out.println("CookieID: ID=(" + szID + ")");
    }
    String resort = request.getParameter("resort");
    if (trace) {
      System.out.println("CookieID: NSPgoto=(" + parent + ")");
    }
    if (trace) {
      System.out.println("CookieID: resort=(" + resort + ")");
    }

    if (szID == null || szID.equals("") || resort == null || resort.equals("")) {
      try {
        error = true;
        if (trace) {
          System.out.println("error, resort=(" + resort + ")");
        }
        String newLoc = PatrolData.SERVLET_URL + "MemberLogin?resort=" + resort + "&" + NSP_goto + "=" + parent;
        if (trace) {
          System.out.println(",,calling sendRedirect(" + newLoc + ")");
        }
        response.sendRedirect(newLoc);

      }
      catch (Exception e) {
        System.out.println(e.getMessage());
      }
    }
    else {
      if (trace) {
        System.out.println("Cookie was OK.  id=" + szID + ", parent=" + parent + ", resort=" + resort);
      }
    }

  }

  public String getID() {
    return szID;
  }
}