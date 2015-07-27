package org.nsponline.calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Steve Gledhill
 */
public class CookieID {
  final static String NSP_goto = "NSPgoto";
  final static boolean trace = false;

  //Instance data
  String szID = null;
  boolean error;

  @SuppressWarnings("UnusedParameters")
  public CookieID(HttpServletRequest request, HttpServletResponse response, String parent, String owner) {
    String szParent = null;
    String lastResort = null;
    error = false;
    szID = request.getParameter("ID");
    String resort = request.getParameter("resort");
    if (trace) {
      System.out.println("CookieID: ID=(" + szID + ")");
    }
    if (trace) {
      System.out.println("CookieID: NSPgoto=(" + szParent + ")");
    }
    if (trace) {
      System.out.println("CookieID: resort=(" + resort + ")");
    }

    if (szID == null || szID.equals("") || resort == null || resort.equals("")) {
      try {
        error = true;
        if (trace) {
          System.out.println("error, lastResort=(" + lastResort + "), resort=(" + resort + ")");
        }
        String newLoc = PatrolData.SERVLET_URL + "MemberLogin?resort=" + resort + "&" + NSP_goto + "=" + parent;
        if (trace) {
          System.out.println(",,calling sendRedirect(" + newLoc + ")");
        }
        response.sendRedirect(newLoc);

      }
      catch (Exception ignored) {
      }
    }
    else {
      if (trace) {
        System.out.println("Cookie was OK.  id=" + szID + ", parent=" + szParent + ", resort=" + resort);
      }
    }

  } //end CookieID()

  @SuppressWarnings("unused")
  public String getID() {
    return szID;
  }
}