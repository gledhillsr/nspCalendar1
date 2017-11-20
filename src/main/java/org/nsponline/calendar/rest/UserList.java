package org.nsponline.calendar.rest;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.sql.ResultSet;

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
 *   {  [ {
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
 *     },
 *     {next ...}
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
public class UserList extends HttpServlet {

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    System.out.println("ZZZ new Rest API GET: /user/list?resort=" + request.getParameter("resort"));
    Utils.printRequestParameters(this.getClass().getSimpleName(), request);
    new InnerUserList(request, response);
  }

  private class InnerUserList {
    private String resort;

    InnerUserList(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
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

      ResultSet rosterResults = patrol.resetRoster();
      int rosterSize = 0;
      Roster patroller;
      ArrayNode rosterArrayNode = Utils.nodeFactory.arrayNode();
      while ((patroller = patrol.nextMember("", rosterResults)) != null) {
        rosterSize++;
        rosterArrayNode.add(patroller.toNode());
      }
      ObjectNode returnNode = Utils.nodeFactory.objectNode();
      returnNode.put("memberCount", rosterSize);
      returnNode.set("roster", rosterArrayNode);

      Utils.buildOkResponse(response, returnNode);

      patrol.close();
    }
  }
}

