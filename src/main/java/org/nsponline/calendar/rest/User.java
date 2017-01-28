package org.nsponline.calendar.rest;

import org.nsponline.calendar.misc.PatrolData;
import org.nsponline.calendar.misc.SessionData;
import org.nsponline.calendar.misc.Utils;
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
 *     http:/nsponline.org/user?resort=Sample
 * @Header Authorization: [authToken]
 *
 * @Response 200 - OK
 * @Header Content-Type - application/json
 * @Body
 *     {
 *       "IDNumber": "123456",
 *       "ClassificationCode": "SR",
 *       "LastName": "Director",
 *       "FirstName": "Joe",
 *       "email": "JCool@gledhills.com",
 *       "Commitment": "2",
 *       "Instructor": "0",
 *       "Director": "yes",
 *       "teamLead": "0",
 *       "mentoring": "0",
 *       "lastUpdated": "2015-10-04"
 *     }
 * @Response 400 - Bad Request
 *     X-Reason: "Resort not found"
 *     X-Reason: "Authorization header not found"
 * @Response 401 - Unauthorized
 *     X-Reason: "Invalid Authorization"
 *
 * @author Steve Gledhill
 */
public class User extends HttpServlet {

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    Utils.printRequestParameters(this.getClass().getSimpleName(), request);
    new InnerUser(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    Utils.printRequestParameters(this.getClass().getSimpleName(), request);
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
        Utils.buildErrorResponse(response, 400, "Authorization header not found");
        return;
      }
      if (!PatrolData.isValidResort(resort)) {
        Utils.buildErrorResponse(response, 400, "Resort not found: (" + resort + ")");
        return;
      }
      SessionData sessionData = new SessionData(request, out);
      PatrolData patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData);
      Connection connection = patrol.getConnection();
      NspSession nspSession = NspSession.read(connection, sessionId);
      if (nspSession == null) {
        Utils.buildErrorResponse(response, 401, "Invalid Authorization: (" + sessionId + ")");
        return;
      }
      Roster patroller = patrol.getMemberByID(nspSession.getAuthenticatedUser());
      if (patroller == null) {
        Utils.buildErrorResponse(response, 400, "user not found (" + nspSession.getAuthenticatedUser() + ")");
        return;
      }

      Utils.buildOkResponse(response, patroller.toNode());

      patrol.close();
    }
  }
}

