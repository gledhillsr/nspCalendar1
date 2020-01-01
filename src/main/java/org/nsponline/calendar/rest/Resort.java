package org.nsponline.calendar.rest;

import org.nsponline.calendar.misc.*;
import org.nsponline.calendar.store.DirectorSettings;
import org.nsponline.calendar.store.NspSession;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

/**
 * query the resort's settings, given a resort and an Authorization Token.
 *
 * @GET
 *     http:/nsponline.org/resort?
 *      resort=Sample   (required)
 * @Header Authorization: [authToken]
 *
 * @Response 200 - OK
 * @Header Content-Type - application/json
 * @Body
 *   {
 *     "formalName": "Brighton",
 *     "changesByDirectorsOnly": false,
 *     "whenCanUserDelete": -1,
 *     "patrollersCanSendMail": true
 *   }
 * @Response 400 - Bad Request
 *     X-Reason: "Resort not found"
 *     X-Reason: "Authorization header not found"
 * @Response 401 - Unauthorized
 *     X-Reason: "Invalid Authorization"
 *
 * @author Steve Gledhill
 */
@SuppressWarnings("JavaDoc")
public class Resort extends HttpServlet {
  private final static int MIN_LOG_LEVEL = Logger.DEBUG;

  private Logger LOG;

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    LOG = new Logger(Resort.class, request, "GET", null, MIN_LOG_LEVEL);
    LOG.logRequestParameters();
    new InnerResort(request, response);
  }

  private class InnerResort {
    private String resort;

    InnerResort(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      response.setContentType("application/json");
      PrintWriter out = response.getWriter();
      resort = request.getParameter("resort");
      String sessionId = request.getHeader("Authorization");
      if(Utils.isEmpty(sessionId)) {
        Utils.buildErrorResponse(response, 400, "Authorization header not found");
        return;
      }
      if (!PatrolData.isValidResort(resort)) {
        Utils.buildErrorResponse(response, 400, "Resort not found: (" + resort + ")");
        return;
      }
      SessionData sessionData = new SessionData(request, out);
      PatrolData patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData, LOG);
      Connection connection = patrol.getConnection();
      NspSession nspSession = NspSession.read(connection, sessionId);
      if (nspSession == null) {
        Utils.buildErrorResponse(response, 401, "Invalid Authorization: (" + sessionId + ")");
        return;
      }
      DirectorSettings directorSettings = patrol.readDirectorSettings();
      if (directorSettings == null) {
        Utils.buildErrorResponse(response, 400, "resort settings not found (should never happen) (" + nspSession.getAuthenticatedUser() + ")");
        return;
      }

      Utils.buildOkResponse(response, directorSettings.toNode());

      patrol.close();
    }
  }
}

