package org.nsponline.calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Look at QueryParameters 'ID', and 'resort'
 * if ID is empty, goto login in page!
 * if resort is empty, goto login in page!
 * if resort is different that logged in resort, goto login in page!
 *
 * @author Steve Gledhill
 */
public class ValidateCredentials {
  final static boolean DEBUG = true;
  String resortParameter;

  private boolean hasInvalidCredentials;
  @SuppressWarnings("UnusedParameters")
  public ValidateCredentials(SessionData sessionData, HttpServletRequest request, HttpServletResponse response, String parent) {
    this.resortParameter = request.getParameter("resort");
    String idParameter = request.getParameter("ID"); //NOT REQUIRED (keep it that way)
    String idLoggedIn = sessionData.getLoggedInUserId();
    debugOut("ValidateCredentialsExist: parameters  ID=" + idParameter + ", resort=" + resortParameter + ", NSPgoto=" + parent);
    debugOut("ValidateCredentialsExist: sessionData ID=" + idLoggedIn + ", resort=" + sessionData.getLoggedInResort() + ", NSPgoto=" + parent);

    hasInvalidCredentials = sessionData.isLoggedIntoAnotherResort(resortParameter) || Utils.isEmpty(sessionData.getLoggedInUserId());
    if (hasInvalidCredentials) {
      try {
        sessionData.setLoggedInResort(null);
        sessionData.setLoggedInUserId(null);
        debugOut("ValidateCredentialsExist: RESETTING logged in resort/userId to null");
        String newLoc = PatrolData.SERVLET_URL + "MemberLogin?resort=" + resortParameter + "&NSPgoto=" + parent;
        debugOut("ValidateCredentialsExist is calling sendRedirect(" + newLoc + ")");
        if (Utils.isNotEmpty(parent)) {
          response.sendRedirect(newLoc);
        }
      }
      catch (Exception e) {
        System.out.println(e.getMessage());
      }
    }
    else {
      debugOut("ValidateCredentialsExist was OK.  id=" + idLoggedIn + ", parent=" + parent + ", resort=" + resortParameter);
    }
  }

  private void debugOut(String msg) {
    if (DEBUG) {
      System.out.println("DEBUG-ValidateCredentials(" + resortParameter + "): " + msg);
    }
  }

  public boolean hasInvalidCredentials() {
    return hasInvalidCredentials;
  }
}