package org.nsponline.calendar.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.amazonaws.util.StringUtils.isNullOrEmpty;

/**
 * Look at QueryParameters 'ID', and 'resort'
 * if ID is empty, goto login in page!
 * if resort is empty, goto login in page!
 * if resort is different that logged in resort, goto login in page!
 *
 * @author Steve Gledhill
 */
public class ValidateCredentials {

  @SuppressWarnings("FieldCanBeLocal")
  private String resortParameter;
  private String token;  //new todo implement
  private Logger LOG;

  private boolean hasInvalidCredentials;

  @SuppressWarnings("UnusedParameters")
  public ValidateCredentials(SessionData sessionData, HttpServletRequest request, HttpServletResponse response, String parent, final Logger parentLogger) {
    this.resortParameter = request.getParameter("resort");
    this.token = request.getParameter("token");
//    LOG = new Logger(this.getClass(), parentLogger, resortParameter, LOG_LEVEL);
    LOG = parentLogger;
    //  public Logger(final Class<?> aClass, final HttpServletRequest request, final String methodType, String resort, int minLogLevel) //todo 1/1/2020 srg, consider using something like
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
    if (StaticUtils.isEmpty(sessionData.getLoggedInUserId()) && doParametersRepresentValidLogin(resortParameter, idParameter, sessionData, LOG)) {
      sessionData.setLoggedInUserId(idParameter);
      sessionData.setLoggedInResort(resortParameter);
    }

    hasInvalidCredentials = sessionData.isLoggedIntoAnotherResort(resortParameter) || StaticUtils.isEmpty(sessionData.getLoggedInUserId());
    if (hasInvalidCredentials) {
//      if (token != null && token.isEmpty()) {
//        LOG.error("token is empty, loggedInUserId is NOT" + parent);
//        //todo do fix this (1/1/2021)
//      }
      try {
        sessionData.setLoggedInResort(null);
        sessionData.setLoggedInUserId(null);
        debugOut("RESETTING logged in resort/userId to null");
        if (StaticUtils.isNotEmpty(parent)) {
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
    if (isNullOrEmpty(resortParameter) ||
        isNullOrEmpty(idParameter) ||
        !PatrolData.isValidResort(resortParameter)) {
      return false;
    }
    PatrolData patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resortParameter, sessionData, parentLog); //when reading members, read full data
    boolean validId = patrol.getMemberByID(idParameter) != null;
    //todo 1/1/2020, put try/catch around getMemberByID, and dump all parameters on error
    //seen this error when idParameter was not a number
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