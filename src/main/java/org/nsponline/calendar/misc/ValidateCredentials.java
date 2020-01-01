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
  private static final int LOG_LEVEL = Logger.INFO;

  @SuppressWarnings("FieldCanBeLocal")
  private String resortParameter;
  private Logger LOG;

  private boolean hasInvalidCredentials;

  @SuppressWarnings("UnusedParameters")
  public ValidateCredentials(SessionData sessionData, HttpServletRequest request, HttpServletResponse response, String parent, final Logger parentLogger) {
    this.resortParameter = request.getParameter("resort");
    LOG = new Logger(this.getClass(), parentLogger, resortParameter, LOG_LEVEL);
    String idParameter = request.getParameter("ID"); //NOT REQUIRED (keep it that way)
    String idLoggedIn = sessionData.getLoggedInUserId();
    init(sessionData, response, parent, idParameter, idLoggedIn);
  }

//  public ValidateCredentials(SessionData sessionData, String resort, String id) {
//    this.resortParameter = resort;
//    init(sessionData, null, null, id, null);
//  }

  private void init(SessionData sessionData, HttpServletResponse response, String parent, String idParameter, String idLoggedIn) {
    debugOut("parameters  idParameter=" + idParameter + ", resort=" + resortParameter + ", NSPgoto=" + parent);
//    debugOut("sessionData idLoggedIn=" + idLoggedIn + ", resort=" + sessionData.getLoggedInResort() + ", NSPgoto=" + parent);
    if (Utils.isEmpty(sessionData.getLoggedInUserId()) && doParametersRepresentValidLogin(resortParameter, idParameter, sessionData, LOG)) {
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

  private boolean doParametersRepresentValidLogin(String resortParameter, String idParameter, SessionData sessionData, Logger parentLog) {
    if (StringUtils.isNullOrEmpty(resortParameter) ||
        StringUtils.isNullOrEmpty(idParameter) ||
        !PatrolData.isValidResort(resortParameter)) {
      return false;
    }
    PatrolData patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resortParameter, sessionData, parentLog); //when reading members, read full data
    boolean validId = patrol.getMemberByID(idParameter) != null;
    patrol.close();
    debugOut("cheated login, validId (" + idParameter + ") = " + validId);
    return validId;
  }

  private void errorOut(String msg) {
    // NOSONAR
    LOG.error("ERROR-ValidateCredentials(" + resortParameter + "): " + msg);
  }

  private void debugOut(String msg) {
    // NOSONAR
    LOG.debug("DEBUG-ValidateCredentials(" + resortParameter + "): " + msg);
  }

  public boolean hasInvalidCredentials() {
    return hasInvalidCredentials;
  }
}