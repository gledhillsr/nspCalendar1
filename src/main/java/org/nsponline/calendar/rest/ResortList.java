package org.nsponline.calendar.rest;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.nsponline.calendar.misc.*;
import org.nsponline.calendar.store.NspSession;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * query the resort's settings, given a resort and an Authorization Token.
 *
 * @GET
 *     http:/nsponline.org/resort/list
 * @Header Authorization: [authToken]
 *
 * @Response 200 - OK
 * @Header Content-Type - application/json
 * @Body
 *   {
 *    [ "Afton", "Brighton",...]
 *   }
 * @Response 400 - Bad Request
 *     X-Reason: "Authorization header not found"
 * @Response 401 - Unauthorized
 *     X-Reason: "Invalid Authorization"
 *
 * @author Steve Gledhill
 */
@SuppressWarnings("JavaDoc")
public class ResortList extends HttpServlet {
  private static Logger LOG = new Logger(ResortList.class);

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    LOG.logRequestParameters("GET", request);
    new InnerResortList(request, response);
  }

  private class InnerResortList {
    private String resort;

    InnerResortList(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
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
      ObjectNode returnNode = Utils.nodeFactory.objectNode();
      ArrayNode resortArrayNode = Utils.nodeFactory.arrayNode();
      for (String eachResort : getResorts(connection)) {
        resortArrayNode.add(Utils.nodeFactory.textNode(eachResort));
      }
      returnNode.set("resorts", resortArrayNode);

      Utils.buildOkResponse(response, returnNode);
          patrol.close();
    }

    private List<String> getResorts(Connection connection) {
      PreparedStatement dbListStatement;
      List<String> resorts = new ArrayList<String>();
      try {
        dbListStatement = connection.prepareStatement("SHOW databases"); //sort by default key
        ResultSet results = dbListStatement.executeQuery();
        while (results.next()) {
          String resort = results.getString("Database");
          if ("mysql".equals(resort)
              || "information_schema".equals(resort)
              || "performance_schema".equals(resort)) {
            continue;
          }
          resorts.add(resort);
        }
        return resorts;
      }
      catch (Exception e) {
        LOG.logException("Error SHOW databases:", e);
      } //end try
      return new ArrayList<String>();
    }
  }
}

