package org.nsponline.calendar.resources;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.nsponline.calendar.store.NspSession;
import org.nsponline.calendar.utils.Logger;
import org.nsponline.calendar.utils.PatrolData;
import org.nsponline.calendar.utils.SessionData;
import org.nsponline.calendar.utils.StaticUtils;

import static org.nsponline.calendar.utils.StaticUtils.buildAndLogErrorResponse;

public class ApiBase {
  protected PrintWriter out;
  String resort;
  String sessionId;
  Logger LOG;
  HttpServletRequest request;
  SessionData sessionData;
  PatrolData patrol;
  Connection connection;
  NspSession nspSession;

  ApiBase(HttpServletRequest request, HttpServletResponse response, Logger LOG) throws IOException {
    response.setContentType("application/json");
    this.LOG = LOG;
    this.request = request;
    out = response.getWriter();
    resort = request.getParameter("resort");
    sessionId = request.getHeader("Authorization");
    LOG.logRequestParameters();
  }

  protected boolean initBaseAndValidSession(HttpServletResponse response) {
    if (StaticUtils.isEmpty(sessionId)) {
      buildAndLogErrorResponse(response, 401, "Authorization header not found");
      return false;
    }
    if (!PatrolData.isValidResort(resort)) {
      buildAndLogErrorResponse(response, 400, "Resort not found: (" + resort + ")");
      return false;
    }
    sessionData = new SessionData(request, out, LOG);
    patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData, LOG);
    connection = patrol.getConnection();
    nspSession = NspSession.read(connection, sessionId);
    if (nspSession == null) {
      buildAndLogErrorResponse(response, 401, "Invalid Authorization: (" + sessionId + ")");
      patrol.close();
      return false;
    }
    return true;
  }
}
