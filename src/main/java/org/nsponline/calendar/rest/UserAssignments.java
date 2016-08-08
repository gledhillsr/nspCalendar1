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
 * @author Steve Gledhill
 *
 * clear cookies, and push to MonthCalendar (no longer logged in)
 */
public class UserAssignments extends HttpServlet {

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    Utils.printRequestParameters(this.getClass().getSimpleName(), request);
    new InnerUserAssignments(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    Utils.printRequestParameters(this.getClass().getSimpleName(), request);
    new InnerUserAssignments(request, response);
  }

  private class InnerUserAssignments {
    private String resort;

    InnerUserAssignments(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();
      resort = request.getParameter("resort");
      String sessionId = request.getHeader("Authorization");
      if(Utils.isEmpty(sessionId)) {
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
        Utils.buildErrorResponse(response, 400, "Authorization not found (" + sessionId + ")");
        return;
      }
      String authenticatedUserId = nspSession.getAuthenticatedUser();
      Roster patroller = patrol.getMemberByID(authenticatedUserId);
      if (patroller == null) {
        Utils.buildErrorResponse(response, 400, "user not found (" + authenticatedUserId + ")");
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
}

