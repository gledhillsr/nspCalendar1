package org.nsponline.calendar.rest;

import org.nsponline.calendar.misc.*;
import org.nsponline.calendar.store.NspSession;
import org.nsponline.calendar.store.Roster;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

/**
 * query the logged in patroller's information, given a resort and an Authorization Token.
 * If a field is empty, then it will not be represented in the body
 *
 * @GET
 *     http:/nsponline.org/user?
 *      resort=Sample   (required)
 * @Header Authorization: [authToken]
 *
 * @Response 200 - OK
 * @Header Content-Type - application/json
 * @Body
 *   {
 *     "IDNumber": "192443",
 *     "ClassificationCode": "SR",
 *     "LastName": "Gledhill",
 *     "FirstName": "Steve",
 *     "Spouse": "Nancy",
 *     "Address": "11532 Cherry Hill Drive",
 *     "City": "Sandy",
 *     "State": "UT",
 *     "ZipCode": "84094",
 *     "HomePhone": "(801) 571-7716",
 *     "CellPhone": "(801) 209-5974",
 *     "Pager": "none",
 *     "email": "steve@gledhills.com",
 *     "EmergencyCallUp": "both",
 *     "NightSubsitute": "yes",
 *     "Commitment": "2",
 *     "Instructor": "2",
 *     "Director": "yesEmail",
 *     "teamLead": "1",
 *     "mentoring": "0",
 *     "lastUpdated": "2016-10-16"
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
public class User extends HttpServlet {
  private static final int MIN_LOG_LEVEL = Logger.DEBUG;

  private Logger LOG;

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    LOG = new Logger(User.class, request, "GET", null, MIN_LOG_LEVEL);
    LOG.logRequestParameters();
    new InnerUser(request, response);
  }

  private class InnerUser {
    private String resort;

    InnerUser(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      response.setContentType("application/json");
      PrintWriter out = response.getWriter();
      resort = request.getParameter("resort");
      String sessionId = request.getHeader("Authorization");
      if(Utils.isEmpty(sessionId)) {
        Utils.buildAndLogErrorResponse(response, 400, "Authorization header not found");
        return;
      }
      if (!PatrolData.isValidResort(resort)) {
        Utils.buildAndLogErrorResponse(response, 400, "Resort not found: (" + resort + ")");
        return;
      }
      SessionData sessionData = new SessionData(request, out, LOG);
      PatrolData patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData, LOG);
      Connection connection = patrol.getConnection();
      NspSession nspSession = NspSession.read(connection, sessionId);
      if (nspSession == null) {
        Utils.buildAndLogErrorResponse(response, 401, "Invalid Authorization: (" + sessionId + ")");
        return;
      }
      Roster patroller = patrol.getMemberByID(nspSession.getAuthenticatedUser());
      if (patroller == null) {
        Utils.buildAndLogErrorResponse(response, 400, "user not found (" + nspSession.getAuthenticatedUser() + ")");
        return;
      }

      Utils.buildOkResponse(response, patroller.toNode());

      patrol.close();
    }
  }
}

