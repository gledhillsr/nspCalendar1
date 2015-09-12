package org.nsponline.calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Steve Gledhill
 */
public class CookieID {
  final static boolean trace = true;

  //Instance data
  String szID = null;
  boolean error;
  String resort;

  @SuppressWarnings("UnusedParameters")
  public CookieID(SessionData sessionData, HttpServletRequest request, HttpServletResponse response, String parent, String owner) {
    error = false;
    szID = request.getParameter("ID");
    resort = request.getParameter("resort");
    if (szID == null || szID.isEmpty()) {
      szID = sessionData.getLoggedInUserId();
      debugOut("get user id from session(" + szID + ")");
    }
    else {
      debugOut("ID=(" + szID + ")");
    }
    debugOut("NSPgoto=(" + parent + ", resort=(" + resort + ")");

    if (szID == null || szID.equals("") || resort == null || resort.equals("")) {
      try {
        error = true;
        debugOut("error, resort=(" + resort + ")");
        String newLoc = PatrolData.SERVLET_URL + "MemberLogin?resort=" + resort + "&NSPgoto=" + parent;
        debugOut("THIS IS A BUG, SHOULD USE VALIDATECREDENTIALS calling sendRedirect(" + newLoc + ")");
        response.sendRedirect(newLoc);

      }
      catch (Exception e) {
        System.out.println(e.getMessage());
      }
    }
    else {
      debugOut("Cookie was OK.  id=" + szID + ", parent=" + parent + ", resort=" + resort);
  }

}

  public String getID() {
    return szID;
  }

  private void debugOut(String msg) {
    if (trace) {
      System.out.println("DEBUG-CookieID(" + resort + "): " + msg);
    }
  }
}