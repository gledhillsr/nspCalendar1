package org.nsponline.calendar.misc;

import com.mysql.jdbc.StringUtils;

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
  private static final boolean DEBUG = false;
  @SuppressWarnings("FieldCanBeLocal")
  private String resortParameter;

  private boolean hasInvalidCredentials;

  @SuppressWarnings("UnusedParameters")
  public ValidateCredentials(SessionData sessionData, HttpServletRequest request, HttpServletResponse response, String parent) {
    this.resortParameter = request.getParameter("resort");
    String idParameter = request.getParameter("ID"); //NOT REQUIRED (keep it that way)
    String idLoggedIn = sessionData.getLoggedInUserId();
    xyzzy(sessionData, response, parent, idParameter, idLoggedIn);
  }

  public ValidateCredentials(SessionData sessionData, String resort, String id, String password) {
    this.resortParameter = resort;
    String idParameter = id; //NOT REQUIRED (keep it that way)
    xyzzy(sessionData, null, null, idParameter, null);
  }

  private void xyzzy(SessionData sessionData, HttpServletResponse response, String parent, String idParameter, String idLoggedIn) {
    debugOut("parameters  idParameter=" + idParameter + ", resort=" + resortParameter + ", NSPgoto=" + parent);
//    debugOut("sessionData idLoggedIn=" + idLoggedIn + ", resort=" + sessionData.getLoggedInResort() + ", NSPgoto=" + parent);
    if (Utils.isEmpty(sessionData.getLoggedInUserId()) && doParametersRepresentValidLogin(resortParameter, idParameter, sessionData)) {
      sessionData.setLoggedInUserId(idParameter);
      sessionData.setLoggedInResort(resortParameter);
    }

    hasInvalidCredentials = sessionData.isLoggedIntoAnotherResort(resortParameter) || Utils.isEmpty(sessionData.getLoggedInUserId());
    if (hasInvalidCredentials) {
      try {
        sessionData.setLoggedInResort(null);
        sessionData.setLoggedInUserId(null);
        debugOut("RESETTING logged in resort/userId to null");
        if (Utils.isNotEmpty(parent)) {
          String newLoc = PatrolData.SERVLET_URL + "MemberLogin?resort=" + resortParameter + "&NSPgoto=" + parent;
          debugOut("calling sendRedirect(" + newLoc + ")");
          response.sendRedirect(newLoc);
        }
      }
      catch (Exception e) {
        errorOut(e.getMessage());
      }
    }
    else {
      debugOut("OK.  id=" + idLoggedIn + ", parent=" + parent + ", resort=" + resortParameter);
    }
  }

  private boolean doParametersRepresentValidLogin(String resortParameter, String idParameter, SessionData sessionData) {
    if (StringUtils.isNullOrEmpty(resortParameter) ||
        StringUtils.isNullOrEmpty(idParameter) ||
        !PatrolData.isValidResort(resortParameter)) {
      return false;
    }
    PatrolData patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resortParameter, sessionData); //when reading members, read full data
    boolean validId = patrol.getMemberByID(idParameter) != null;
    patrol.close();
    debugOut("cheated login, validId (" + idParameter + ") = " + validId);
    return validId;
  }

  private void errorOut(String msg) {
    // NOSONAR
    Logger.log("ERROR-ValidateCredentials(" + resortParameter + "): " + msg);
  }

  private void debugOut(String msg) {
    if (DEBUG) {
      // NOSONAR
      Logger.log("DEBUG-ValidateCredentials(" + resortParameter + "): " + msg);
    }
  }

  public boolean hasInvalidCredentials() {
    return hasInvalidCredentials;
  }
}