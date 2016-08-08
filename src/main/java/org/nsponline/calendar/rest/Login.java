package org.nsponline.calendar.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.nsponline.calendar.misc.PatrolData;
import org.nsponline.calendar.misc.SessionData;
import org.nsponline.calendar.misc.Utils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.nsponline.calendar.store.NspSession;
import org.nsponline.calendar.store.Roster;

import static org.nsponline.calendar.misc.Utils.buildErrorResponse;

/**
 * @author Steve Gledhill
 *
 * http:/nsponline.org/login
 * @Header
 *   Content-Type: application/json
 * @Body
 *     {
 *     "id": "123456",
 *     "resort": "Brighton",
 *     "password": "myPassword"
 *     }
 * @Response 200 - OK
 *     Content-Type: application/json
 *     {
 *     "id": "123456",
 *     "resort": "Brighton",
 *     "authToken":"368c9f15-01b4-4a49-9b8d-989f4b2d30ed"
 *     }
 * @Response 400 - Bad Request
 *   X-Reason: "Resort not found"
 *   X-Reason: "Invalid payload"
 * @Response 401 - Unauthorized
 *   X-Reason: "no matching id/password in this resort"
 */

//@Path("/")
//@Produces(MediaType.APPLICATION_JSON)
//@Consumes(MediaType.APPLICATION_JSON)
@SuppressWarnings("JavaDoc")
public class Login extends HttpServlet {

  static final boolean DEBUG = true;
  private static JsonNodeFactory nodeFactory = JsonNodeFactory.instance;


  //  @GET
//  @Timed(name = "PersonResource.getCurrentPerson", absolute = true)
//  @Path("CURRENT")
//  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
//    Utils.printRequestParameters(this.getClass().getSimpleName(), request);
//    new InnerLogin(request, response);
//  }
//
//  @GET
//  @Timed(name = "PersonResource.getCurrentPerson", absolute = true)
//  @Path("CURRENT")
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
System.out.println("doPost");
    Utils.printRequestParameters(this.getClass().getSimpleName(), request);
    new InnerLogin(request, response);
  }

  private final class InnerLogin {

    InnerLogin(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      PrintWriter out = response.getWriter();
      SessionData sessionData = new SessionData(request, out);

      ObjectMapper objectMapper = new ObjectMapper();
      StringBuilder jsonBuffer = new StringBuilder();
      String line;
      try {
        BufferedReader reader = request.getReader();
        while ((line = reader.readLine()) != null) {
          jsonBuffer.append(line);
        }
      } catch (Exception e) {
        /*report an error*/
      }
      logger(sessionData, "jsonBuffer:" + jsonBuffer.toString());

      LoginPayload payload = objectMapper.readValue(jsonBuffer.toString(), LoginPayload.class);
      logger(sessionData, "payload: " + payload.toString());

      String resort = payload.getResort();
      String password = payload.getPassword();
      String patrollerId = payload.getPatrollerId();

      if (Utils.isEmpty(resort) || !PatrolData.isValidResort(resort)) {
        buildErrorResponse(response, "Resort not found (" + resort + ")");
        return;
      }
      if (Utils.isEmpty(patrollerId) || Utils.isEmpty(password)) {
        buildErrorResponse(response, "Missing patrollerId or password");
        return;
      }

//      ValidateCredentials credentials = new ValidateCredentials(sessionData, resort, patrollerId, password);
      if (PatrolData.isValidLogin(out, resort, patrollerId, password, sessionData)) {   //does password match?
        sessionData.setLoggedInUserId(patrollerId);
        sessionData.setLoggedInResort(resort);
      }
      else {
        buildErrorResponse(response, "Unknown patrollerId or password");
        return;
      }
//      resort = sessionData.getLoggedInResort();
//      patrollerId = sessionData.getLoggedInUserId();    //editor's ID
      PatrolData patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData);
      Roster patroller = patrol.getMemberByID(patrollerId); //ID from cookie
      boolean isDirector = patroller.isDirector();



      String sessionId = java.util.UUID.randomUUID().toString();
//NspSession(String sessionId, String authenticatedUser, String resort, Date sessionCreateTime, Date lastSessionAccessTime, String sessionIpAddress, boolean isDirector) {
      java.util.Calendar calendar = java.util.Calendar.getInstance();

      Date sessionCreateTime = new Date(calendar.getTimeInMillis());
      String fromIp = request.getHeader("x-forwarded-for"); //x-forwarded-for: 216.49.181.51

      NspSession nspSession = new NspSession(sessionId, patrollerId, resort, sessionCreateTime, sessionCreateTime, fromIp, isDirector);
      Connection connection = patrol.getConnection();
      if (connection != null && nspSession.insertRow(connection)) {
        ObjectNode returnNode = nodeFactory.objectNode();
        returnNode.put("nspSessionId", sessionId);

        Utils.buildOkResponse(response, returnNode);
      }
      else {
        Utils.buildErrorResponse(response, "Internal error");
      }
      patrol.close();
    }

    private void logger(SessionData sessionData, String str) {
      Utils.printToLogFile(sessionData.getRequest(), str);
    }
  }
}

