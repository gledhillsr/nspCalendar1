package org.nsponline.calendar.rest;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.nsponline.calendar.misc.PatrolData;
import org.nsponline.calendar.misc.SessionData;
import org.nsponline.calendar.misc.Utils;
import org.nsponline.calendar.store.Assignments;
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
 *     http:/nsponline.org/assignments?resort=Sample
 * @Header Authorization: [authToken]
 *
 * @Response 200 - OK
 * @Header Content-Type - application/json
 * @Body
 *    {
 *      "patrollerId": "123456",
 *      "resort": "Sample",
 *      "assignments": [
 *        {
 *          "Date": "2015-12-12_1",
 *          "StartTime": "0900",
 *          "EndTime": "1500",
 *          "EventName": " ",
 *          "ShiftType": "0",
 *          "Count": "6",
 *          "patrollerIds": [ "111111", "0", "222222", "0", "123456", "0" ]
 *        },
 *        {
 *          "Date": "2015-12-25_1",
 *          "StartTime": "HCD",
 *          "EndTime": "end time",
 *          "EventName": " ",
 *          "ShiftType": "0",
 *          "Count": "1",
 *          "patrollerIds": [ "123456"]
 *        }
 *       ]
 *      }
 * @Response 400 - Bad Request
 *     X-Reason: "Resort not found"
 *     X-Reason: "Authorization header not found"
 *     X-Reason: "User not found"  (may have been deleted by an admin)
 * @Response 401 - Unauthorized
 *     X-Reason: "Invalid Authorization"
 *
 * @author Steve Gledhill
 */
public class UserAssignments extends HttpServlet {

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    Utils.printRequestParameters(this.getClass().getSimpleName(), request);
    getUserAssignments(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    Utils.printRequestParameters(this.getClass().getSimpleName(), request);
    getUserAssignments(request, response);
  }


  private void getUserAssignments(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    response.setContentType("application/json");
    PrintWriter out = response.getWriter();
    String resort = request.getParameter("resort");
    String sessionId = request.getHeader("Authorization");
    if (Utils.isEmpty(sessionId)) {
      Utils.buildErrorResponse(response, 400, "Authorization header not found");
      return;
    }
    if (!PatrolData.isValidResort(resort)) {
      Utils.buildErrorResponse(response, 400, "Resort not found (" + resort + ")");
      return;
    }
    SessionData sessionData = new SessionData(request, out);
    PatrolData patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData);
    Connection connection = patrol.getConnection();
    NspSession nspSession = NspSession.read(connection, sessionId);
    if (nspSession == null) {
      Utils.buildErrorResponse(response, 401, "Invalid Authorization (" + sessionId + ")");
      return;
    }
    String authenticatedUserId = nspSession.getAuthenticatedUser();
    Roster patroller = patrol.getMemberByID(authenticatedUserId);
    if (patroller == null) {
      Utils.buildErrorResponse(response, 400, "User not found (" + authenticatedUserId + ")");
      return;
    }
    //everything is OK
    ObjectNode returnNode = Utils.nodeFactory.objectNode();
    returnNode.put("patrollerId", authenticatedUserId);
    returnNode.put("resort", resort);

    ArrayNode assignmentsArrayNode = Utils.nodeFactory.arrayNode();
    for (Assignments ns : patrol.readAllSortedAssignments(authenticatedUserId)) {
      assignmentsArrayNode.add(ns.toNode());
    }
    returnNode.set("assignments", assignmentsArrayNode);
    Utils.buildOkResponse(response, returnNode);

    patrol.close();
  }
}

