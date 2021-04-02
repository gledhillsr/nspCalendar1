package org.nsponline.calendar.utils;

import javax.servlet.http.Cookie;
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
public class ValidateCredentialsRedirectIfNeeded {

  public final static String NSP_TOKEN_NAME = "nspToken";
  private final static int DAYS_IN_SECONDS = 60*60*24;
  private final static int COOKIE_MAX_AGE_IN_SECONDS = DAYS_IN_SECONDS * 14;

  @SuppressWarnings("FieldCanBeLocal")
  private String resort;
  private String token;  //new todo implement for long lived credentials
  private Logger LOG;

  private boolean hasInvalidCredentials;

  @SuppressWarnings("UnusedParameters")
  public ValidateCredentialsRedirectIfNeeded(SessionData sessionData, HttpServletRequest request, HttpServletResponse response, String parent, PatrolData patrolData, final Logger parentLogger) {
    this.resort = request.getParameter("resort");
    this.token = request.getParameter("token");
//    LOG = new Logger(this.getClass(), parentLogger, resortParameter, LOG_LEVEL);
    LOG = parentLogger;
    //  public Logger(final Class<?> aClass, final HttpServletRequest request, final String methodType, String resort, int minLogLevel) //todo 1/1/2020 srg, consider using something like
    String idParameter = request.getParameter("ID"); //NOT REQUIRED (keep it that way)
    String idLoggedIn = sessionData.getLoggedInUserId();
    initAndRedirectIfNeeded(sessionData, request, response, parent, patrolData, idParameter, idLoggedIn);
  }

//  public ValidateCredentials(SessionData sessionData, String resort, String id) {
//    this.resortParameter = resort;
//    init(sessionData, null, null, id, null);
//  }

  private void initAndRedirectIfNeeded(SessionData sessionData, HttpServletRequest request, HttpServletResponse response, String parent, PatrolData patrolData, String idParameter, String idLoggedIn) {
    init(sessionData, parent, idParameter);
    attemptLoginViaCookie(sessionData, request, response, patrolData, idLoggedIn);

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
          String newLoc = PatrolData.SERVLET_URL + "MemberLogin?resort=" + resort + "&NSPgoto=" + parent;
          debugOut("calling sendRedirect(" + newLoc + ")");
          response.sendRedirect(newLoc);
        }
      }
      catch (Exception e) {
        errorOut(e.getMessage());
      }
    }
    else {
      debugOut("validCredentials.  id=" + idLoggedIn + ", parent=" + parent + ", resort=" + resort);
    }
  }

  private void attemptLoginViaCookie(SessionData sessionData, HttpServletRequest request, HttpServletResponse response, PatrolData patrolData, String idLoggedIn) {
    String cookieValue = null;
    String cookieResort = null;
    String cookiePatrollerId = null;
    Cookie[] cookies = request.getCookies();
    if (cookies != null) { //no cookies exist
      for(Cookie cookie : request.getCookies()) {
        if (NSP_TOKEN_NAME.equals(cookie.getName())) {
          cookieValue = cookie.getValue();
          String[] values = cookieValue.split(",");
          if (PatrolData.isValidResort(values[0]) && patrolData.getMemberByID(values[1]) != null) {
            cookieResort = values[0];
            cookiePatrollerId = values[1];
          }
          LOG.debug("cookie FOUND, value=" + cookieValue);
        }
      }

      if (!hasInvalidCredentials) {
        //user had good credentials, so just update the cookie TTL
        cookieValue = resort + "," + idLoggedIn;
        Cookie uiColorCookie = new Cookie(NSP_TOKEN_NAME, cookieValue);
        uiColorCookie.setMaxAge(COOKIE_MAX_AGE_IN_SECONDS);
        response.addCookie(uiColorCookie);
        uiColorCookie.setMaxAge(COOKIE_MAX_AGE_IN_SECONDS);
        LOG.debug("update/add cookie TTL) " + cookieValue);
      }
      else if (cookiePatrollerId != null) {
        //not logged in, but good cookie, so use VALID cookie to login
        sessionData.setLoggedInResort(cookieResort);
        sessionData.setLoggedInUserId(cookiePatrollerId);
        hasInvalidCredentials = false;
        Cookie uiColorCookie = new Cookie(NSP_TOKEN_NAME, cookieValue);
        uiColorCookie.setMaxAge(COOKIE_MAX_AGE_IN_SECONDS);
        response.addCookie(uiColorCookie);
        uiColorCookie.setMaxAge(COOKIE_MAX_AGE_IN_SECONDS);
        LOG.warn("using cookie for login to resort=" + cookieResort + ", as user=" + cookiePatrollerId);
      }
      else if (cookieValue != null) { //to make this true, did not find either resort or patrollerId within resort
        //not logged in, but bad cookie, so delete cookie
        Cookie uiColorCookie = new Cookie(NSP_TOKEN_NAME, cookieValue);
        uiColorCookie.setMaxAge(0);
        response.addCookie(uiColorCookie);
        LOG.warn("deleting cookie because resort or patrollerId was invalid.  newResort=" + resort + ", cookieResort=" + cookieValue.split(",")[0]);
      }
      else {
        LOG.warn("no credentials, and no 'nspToken' cookie");
      }
    }
    else {
      LOG.warn("NO cookies exist");
    }
  }

  private void init(SessionData sessionData, String parent, String idParameter) {
    debugOut("parameters  idParameter=" + idParameter + ", resort=" + resort + ", NSPgoto=" + parent);
//todo this is a HACK that bypasses the login.  remove it 3/4/21
//    if (StaticUtils.isEmpty(sessionData.getLoggedInUserId()) && doParametersRepresentValidLogin(resort, idParameter, sessionData, LOG)) {
//      sessionData.setLoggedInUserId(idParameter);
//      sessionData.setLoggedInResort(resort);
//    }

    hasInvalidCredentials = sessionData.isLoggedIntoAnotherResort(resort) || StaticUtils.isEmpty(sessionData.getLoggedInUserId());
  }

//  private boolean doParametersRepresentValidLogin(String resortParameter, String idParameter, SessionData sessionData, Logger parentLog) {
//    if (isNullOrEmpty(resortParameter) ||
//        isNullOrEmpty(idParameter) ||
//        !PatrolData.isValidResort(resortParameter)) {
//      return false;
//    }
//    PatrolData patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resortParameter, sessionData, parentLog); //todo srg, 2/27/21 pass in, usually already available
//    boolean validId = patrol.getMemberByID(idParameter) != null;
//    //todo 1/1/2020, put try/catch around getMemberByID, and dump all parameters on error
//    //seen this error when idParameter was not a number
//    patrol.close();
//    debugOut("cheated login, validId (" + idParameter + ") = " + validId);
//    return validId;
//  }

  private void errorOut(String msg) {
    // NOSONAR
    LOG.error("ERROR-ValidateCredentials(" + resort + "): " + msg);
  }

  private void debugOut(String msg) {
    // NOSONAR
    LOG.debug("DEBUG-ValidateCredentials(" + resort + "): " + msg);
  }

  public boolean hasInvalidCredentials() {
    return hasInvalidCredentials;
  }
}