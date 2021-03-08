package org.nsponline.calendar.resources;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.nsponline.calendar.store.NspSession;
import org.nsponline.calendar.utils.*;

import static org.nsponline.calendar.utils.StaticUtils.buildAndLogErrorResponse;

@SuppressWarnings({"DuplicatedCode", "BooleanMethodIsAlwaysInverted"})
public class ResourceBase {
  final PrintWriter out;
  final String resort;
  String sessionId;
  final Logger LOG;
  final HttpServletRequest request;
  SessionData sessionData;
  PatrolData patrolData;
  Connection connection;  //only used locally and ApiBase.  PatrolData has it's own copy of connection
  NspSession nspSession;
  private OuterPage outerPage; //for commonHeader & commonFooter

  ResourceBase(HttpServletRequest request, HttpServletResponse response, Logger LOG) throws IOException {
//    response.setContentType("application/json"); //for ApiResources
    this.LOG = LOG;
    this.request = request;
    response.setContentType("text/html");
    out = response.getWriter();
    resort = request.getParameter("resort");
    sessionId = request.getHeader("Authorization");
    LOG.logRequestParameters();
  }

  protected boolean initBase(HttpServletResponse response) {
    String userAgent = request.getHeader("user-agent");
    if (StaticUtils.isRequestFromBot(userAgent)) {
      StaticUtils.buildAndLogErrorResponse(response, 401, "Unauthorized agent (" + userAgent + "). Class=" + LOG.getClassName(), LOG);
      return false; //parent should stop further page display
    }
    if (!PatrolData.isValidResort(resort)) {
      buildAndLogErrorResponse(response, 400, "Resort not found: (" + resort + ")", LOG);
      return false; //parent should stop further page display
    }
    sessionData = new SessionData(request, out, LOG);
    patrolData = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData, LOG);
    connection = patrolData.getConnection();
    return true;
  }

    protected boolean initBaseAndRequireValidSession(HttpServletResponse response) {
    /*
     * TODO, soon this is to be used for all inner methods.  Right now, only called from ApiResource
     */
    if (!initBase(response)) {
      return false; //'resort' etc, MUST be valid (response was setup),  parent should stop further page display
    }
    if (StaticUtils.isEmpty(sessionId)) {
      buildAndLogErrorResponse(response, 401, "Authorization header not found", LOG);
      return false; //parent should stop further page display
    }
    nspSession = NspSession.read(connection, sessionId);
    if (nspSession == null) {
      buildAndLogErrorResponse(response, 401, "Invalid Authorization: (" + sessionId + ")", LOG);
      patrolData.close();
      return false; //parent should stop further page display
    }
    return true;
  }

  protected boolean initBaseAndAskForValidCredentials(HttpServletResponse response, String parent) {
    if (!initBase(response)) {
      return false; //'resort' etc, MUST be valid (response was setup),  parent should stop further page display
    }
    //redirects to MemberLogin if credentials are not valid
    ValidateCredentialsRedirectIfNeeded credentials = new ValidateCredentialsRedirectIfNeeded(sessionData, request, response, parent, LOG);
    return !credentials.hasInvalidCredentials(); //if 'false' parent should stop further page display
  }

  protected void printCommonHeader() {
    outerPage = new OuterPage(patrolData.getResortInfo(), "", sessionData.getLoggedInUserId(), LOG);
    outerPage.printResortHeader(out);
  }

  protected void printCommonFooter() {
    outerPage.printResortFooter(out);
  }

}
