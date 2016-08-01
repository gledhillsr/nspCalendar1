package org.nsponline.calendar;

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

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
        buildErrorResponse(response, sessionData, "Resort not found (" + resort + ")");
        return;
      }
      if (Utils.isEmpty(patrollerId) || Utils.isEmpty(password)) {
        buildErrorResponse(response, sessionData, "Missing patrollerId or password");
        return;
      }

      String sessionId = java.util.UUID.randomUUID().toString();
      String sessionIdKey = "nspSessionId";
      buildOkResponse(response, sessionData, sessionIdKey, sessionId);
    }

    private void buildOkResponse(HttpServletResponse response, SessionData sessionData, String sessionIdKey, String sessionId) throws IOException {
      response.setStatus(200);
      response.setContentType("text/json");
      ObjectNode returnNode = nodeFactory.objectNode();
      returnNode.put(sessionIdKey, sessionId);
      logger(sessionData, returnNode.toString());
      response.getWriter().write(returnNode.toString());
    }

    private void buildErrorResponse(HttpServletResponse response, SessionData sessionData, String errString) throws IOException {
      response.setStatus(400);
      response.setContentType("text/json");
      ObjectNode errorNode = nodeFactory.objectNode();
      errorNode.put("errorMsg", errString);
      logger(sessionData, errorNode.toString());
      response.getWriter().write(errorNode.toString());
    }

    private void logger(SessionData sessionData, String str) {
      Utils.printToLogFile(sessionData.getRequest(), str);
    }
  }
}

