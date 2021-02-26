package org.nsponline.calendar.rest;

import org.nsponline.calendar.utils.*;
import org.nsponline.calendar.store.NspSession;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

/**
 * discard a Authorization Token for the specified resort.
 *
 * @DELETE
 *     http:/nsponline.org/logout?resort=Sample
 * @Header Authorization: [authToken]
 *
 * @Response 204 - OK, No Content
 * @Response 400 - Bad Request
 *     X-Reason: "Resort not found"
 *     X-Reason: "Authorization header not found"
 * @Response 401 - Unauthorized
 *     X-Reason: "Invalid Authorization"
 *
 * @author Steve Gledhill
 */
@SuppressWarnings("JavaDoc")
public class Logout extends HttpServlet {
  private static final int MIN_LOG_LEVEL = Logger.DEBUG;
  private Logger LOG;


  public void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    LOG = new Logger(Logout.class, request, "DELETE", null, MIN_LOG_LEVEL);
    LOG.logRequestParameters();
    doLogout(request, response);
  }

  private void doLogout(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    String resort;
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    resort = request.getParameter("resort");
    String sessionId = request.getHeader("Authorization");
    if (StaticUtils.isEmpty(sessionId)) {
      StaticUtils.buildAndLogErrorResponse(response, 400, "Authorization header not found");
      return;
    }
    if (!PatrolData.isValidResort(resort)) {
      StaticUtils.buildAndLogErrorResponse(response, 400, "Resort not found (" + resort + ")");
      return;
    }
    SessionData sessionData = new SessionData(request, out, LOG);
    sessionData.clearLoggedInResort();
    sessionData.clearLoggedInUserId();

    PatrolData patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData, LOG);
    Connection connection = patrol.getConnection();
    NspSession nspSession = NspSession.read(connection, sessionId);
    if (nspSession == null) {
      StaticUtils.buildAndLogErrorResponse(response, 401, "Invalid Authorization: (" + sessionId + ")");
      return;
    }
    nspSession.deleteRow(connection);
    StaticUtils.build204Response(response);

    patrol.close();
  }
}

