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
  Connection connection;
  NspSession nspSession;
  private OuterPage outerPage; //for commonHeader & commonFooter

  ResourceBase(HttpServletRequest request, HttpServletResponse response, Logger LOG) throws IOException {
//    response.setContentType("application/json");
    this.LOG = LOG;
    this.request = request;
    out = response.getWriter();
    response.setContentType("text/html");
    resort = request.getParameter("resort");
    sessionId = request.getHeader("Authorization");
    LOG.logRequestParameters();
  }

  protected boolean initBaseAndRequireValidSession(HttpServletResponse response) {
    if (StaticUtils.isEmpty(sessionId)) {
      buildAndLogErrorResponse(response, 401, "Authorization header not found");
      return false;
    }
    String userAgent = request.getHeader("user-agent");
    if (StaticUtils.isRequestFromBot(userAgent)) {
      StaticUtils.buildAndLogErrorResponse(response, 401, "Unauthorized agent (" + userAgent + "). Class=" + LOG.getClassName());
      return false;
    }
    if (!PatrolData.isValidResort(resort)) {
      buildAndLogErrorResponse(response, 400, "Resort not found: (" + resort + ")");
      return false;
    }
    sessionData = new SessionData(request, out, LOG);
    patrolData = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData, LOG);
    connection = patrolData.getConnection();
    nspSession = NspSession.read(connection, sessionId);
    if (nspSession == null) {
      buildAndLogErrorResponse(response, 401, "Invalid Authorization: (" + sessionId + ")");
      patrolData.close();
      return false;
    }
    return true;
  }

  protected boolean initBaseAndAskForValidCredentials(HttpServletResponse response, String parent) {
    String userAgent = request.getHeader("user-agent");
    if (StaticUtils.isRequestFromBot(userAgent)) {
      StaticUtils.buildAndLogErrorResponse(response, 401, "Unauthorized agent (" + userAgent + "). Class=" + LOG.getClassName());
      return false;
    }
    if (!PatrolData.isValidResort(resort)) {
      buildAndLogErrorResponse(response, 400, "Resort not found: (" + resort + ")");
      return false;
    }
    sessionData = new SessionData(request, out, LOG);
    patrolData = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData, LOG);
    connection = patrolData.getConnection();

    ValidateCredentialsRedirectIfNeeded credentials = new ValidateCredentialsRedirectIfNeeded(sessionData, request, response, parent, LOG);
    return !credentials.hasInvalidCredentials(); //stop further page display
//continue displaying page
  }

  protected void printCommonHeader() {
    outerPage = new OuterPage(patrolData.getResortInfo(), "", sessionData.getLoggedInUserId());
    outerPage.printResortHeader(out);
  }

  protected void printCommonFooter() {
    outerPage.printResortFooter(out);
  }

}
