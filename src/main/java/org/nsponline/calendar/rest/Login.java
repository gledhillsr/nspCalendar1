package org.nsponline.calendar.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.nsponline.calendar.misc.*;
import org.nsponline.calendar.store.NspSession;
import org.nsponline.calendar.store.Roster;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;

import static org.nsponline.calendar.misc.Utils.buildErrorResponse;

/**
 * get an Authorization Token for the specified resort.  Given a valid userId/password
 *
 * @POST
 *    http:/nsponline.org/login?resort=Sample
 * @Header Content-Type: application/json
 * @Body
 *    {
 *      "id": "123456",
 *      "password": "password"
 *    }
 *
 * @Response 200 - OK
 *    body Content-Type: application/json
 *    {
 *      "authToken":"368c9f15-01b4-4a49-9b8d-989f4b2d30ed"
 *    }
 * @Response 400 - Bad Request
 *    X-Reason: "Resort not found"
 *    X-Reason: "Missing id or password"
 * @Response 401 - Unauthorized
 *    X-Reason: "no matching id/password for resort: Sample"
 *
 * @author Steve Gledhill
 */

@SuppressWarnings("JavaDoc")
public class Login extends HttpServlet {
  private static Logger LOG = new Logger(Login.class);

  private static JsonNodeFactory nodeFactory = JsonNodeFactory.instance;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    LOG.printRequestParameters(LogLevel.INFO, "POST", request);
    doLogin(request, response);
  }

  private void doLogin(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    PrintWriter out = response.getWriter();
    SessionData sessionData = new SessionData(request, out);
    response.setContentType("application/json");

    ObjectMapper objectMapper = new ObjectMapper();
    StringBuilder jsonBuffer = new StringBuilder();
    String line;
    try {
      BufferedReader reader = request.getReader();
      while ((line = reader.readLine()) != null) {
        jsonBuffer.append(line);
      }
    }
    catch (Exception e) {
        /*report an error*/
    }
//    logger(sessionData, "jsonBuffer:" + jsonBuffer.toString());

    String resort = request.getParameter("resort");
    if (Utils.isEmpty(resort) || !PatrolData.isValidResort(resort)) {
      buildErrorResponse(response, 400, "Resort not found (" + resort + ")");
      return;
    }

    LoginPayload payload = objectMapper.readValue(jsonBuffer.toString(), LoginPayload.class);
    logger(sessionData, "payload: " + payload.toString());

    String password = payload.getPassword();
    String patrollerId = payload.getId();

    if (Utils.isEmpty(patrollerId) || Utils.isEmpty(password)) {
      buildErrorResponse(response, 400, "Missing id or password");
      return;
    }

    if (PatrolData.isValidLogin(out, resort, patrollerId, password, sessionData)) {   //does password match?
      sessionData.setLoggedInUserId(patrollerId);
      sessionData.setLoggedInResort(resort);
    }
    else {
      buildErrorResponse(response, 401, "no matching id/password for resort: " + resort);
      return;
    }

    PatrolData patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData);
    Roster patroller = patrol.getMemberByID(patrollerId); //ID from cookie
    boolean isDirector = patroller.isDirector();

    String sessionId = java.util.UUID.randomUUID().toString();
    java.util.Calendar calendar = java.util.Calendar.getInstance();

    Date sessionCreateTime = new Date(calendar.getTimeInMillis());
    String fromIp = request.getHeader("x-forwarded-for"); //x-forwarded-for: 216.49.181.51

    NspSession nspSession = new NspSession(sessionId, patrollerId, resort, sessionCreateTime, sessionCreateTime, fromIp, isDirector);
    Connection connection = patrol.getConnection();
    if (connection != null && nspSession.insertRow(connection)) {
      //build return node
      ObjectNode returnNode = nodeFactory.objectNode();
      returnNode.put("authToken", sessionId);
      //return OK
      Utils.buildOkResponse(response, returnNode);
    }
    else {
      String errMsg = (connection == null) ? "Could not get DB connection" : "Row insertion failed";
      Utils.buildErrorResponse(response, 500, "Internal error: " + errMsg);
    }
    patrol.close();
  }

  private void logger(SessionData sessionData, String str) {
    Logger.printToLogFile(sessionData.getRequest(), str);
  }
}

