package org.nsponline.calendar.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.nsponline.calendar.store.Assignments;
import org.nsponline.calendar.store.DirectorSettings;
import org.nsponline.calendar.store.NspSession;
import org.nsponline.calendar.store.Roster;
import org.nsponline.calendar.utils.Logger;
import org.nsponline.calendar.utils.PatrolData;
import org.nsponline.calendar.utils.SessionData;
import org.nsponline.calendar.utils.StaticUtils;

import static org.nsponline.calendar.utils.StaticUtils.buildAndLogErrorResponse;
import static org.nsponline.calendar.utils.StaticUtils.convertToInt;

@SuppressWarnings("InnerClassMayBeStatic")
public class ApiResources {

  private static final int MIN_LOG_LEVEL = Logger.DEBUG;
  private static final JsonNodeFactory nodeFactory = JsonNodeFactory.instance;

  /**
   * return an Authorization Token for the specified resort.  Given a valid userId/password
   *
   * @author Steve Gledhill
   * @POST http:/nsponline.org/login?resort=Sample
   * @Header Content-Type: application/json
   * @Body {
   * "id": "123456",
   * "password": "password"
   * }
   * @Response 200 - OK
   * body Content-Type: application/json
   * {
   * "authToken":"368c9f15-01b4-4a49-9b8d-989f4b2d30ed"
   * }
   * @Response 400 - Bad Request
   * X-Reason: "Resort not found"
   * X-Reason: "Missing id or password"
   * @Response 401 - Unauthorized
   * X-Reason: "no matching id/password for resort: Sample"
   */

  @SuppressWarnings("JavaDoc")
  public static class Login extends HttpServlet {

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(Login.class, request, "POST", null, MIN_LOG_LEVEL);
      doLogin(request, response, LOG);
    }

    private void doLogin(HttpServletRequest request, HttpServletResponse response, Logger LOG) throws IOException {
      LOG.logRequestParameters();
      PrintWriter out = response.getWriter();
      SessionData sessionData = new SessionData(request, out, LOG);
      response.setContentType("application/json");

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
      //    logger(sessionData, "jsonBuffer:" + jsonBuffer.toString());

      String resort = request.getParameter("resort");
      if (StaticUtils.isEmpty(resort) || !PatrolData.isValidResort(resort)) {
        buildAndLogErrorResponse(response, 400, "Resort not found (" + resort + ")", LOG);
        return;
      }

      LoginPayload payload = objectMapper.readValue(jsonBuffer.toString(), LoginPayload.class);
      logger(sessionData, "payload: " + payload.toString());

      String password = payload.getPassword();
      String patrollerId = payload.getId();

      if (StaticUtils.isEmpty(patrollerId) || StaticUtils.isEmpty(password)) {
        buildAndLogErrorResponse(response, 400, "Missing id or password", LOG);
        return;
      }

      PatrolData patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData, LOG);
      if (patrol.isValidLogin(out, resort, patrollerId, password, sessionData)) {   //does password match?
        sessionData.setLoggedInUserId(patrollerId);
        sessionData.setLoggedInResort(resort);
      } else {
        buildAndLogErrorResponse(response, 401, "no matching id/password for resort=" + resort, LOG);
        return;
      }

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
        StaticUtils.buildOkResponse(response, returnNode, LOG);
      } else {
        String errMsg = (connection == null) ? "Could not get DB connection" : "Row insertion failed";
        StaticUtils.buildAndLogErrorResponse(response, 500, "Internal error: " + errMsg, LOG);
      }
      patrol.close();
    }

    private void logger(SessionData sessionData, String str) {
      sessionData.getLOG().info(str);
    }
  }

  /**
   * discard a Authorization Token for the specified resort.
   *
   * @author Steve Gledhill
   * @DELETE http:/nsponline.org/logout?resort=Sample
   * @Header Authorization: [authToken]
   * @Response 204 - OK, No Content
   * @Response 400 - Bad Request
   * X-Reason: "Resort not found"
   * X-Reason: "Authorization header not found"
   * @Response 401 - Unauthorized
   * X-Reason: "Invalid Authorization"
   */
  @SuppressWarnings("JavaDoc")
  public static class Logout extends HttpServlet {
    private static final int MIN_LOG_LEVEL = Logger.DEBUG;

    public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(Logout.class, request, "DELETE", null, MIN_LOG_LEVEL);
      new DoLogout(request, response, LOG);
    }

    private class DoLogout extends ResourceBase {
      DoLogout(HttpServletRequest request, HttpServletResponse response, Logger LOG) throws IOException {
        super(request, response, LOG);
        if (!initBaseAndRequireValidSession(response)) {
          return;
        }
        sessionData.clearLoggedInResort();
        sessionData.clearLoggedInUserId();

        nspSession.deleteRow(connection);
        StaticUtils.build204Response(response, LOG);

        patrolData.close();
      }
    }
  }

  /**
   * query the patroller's entire shift schedule for a specified year/month, and optionally a specific day
   *
   * @author Steve Gledhill
   * @GET http:/nsponline.org/patrol/assignments?
   * resort=Sample (required)
   * year=2017     (required)
   * month=1       (required,  1 is January)
   * day=3         (optional)
   * @Header Authorization: [authToken]
   * @Response 200 - OK
   * @Header Content-Type - application/json
   * @Body {
   * "resort": "Sample",
   * "assignments": [
   * {
   * "Date": "2015-12-12_1",
   * "StartTime": "0900",
   * "EndTime": "1500",
   * "EventName": " ",
   * "ShiftType": "0",
   * "Count": "6",
   * "patrollerIds": [ "111111", "0", "222222", "0", "123456", "0" ]
   * },
   * {
   * "Date": "2015-12-25_1",
   * "StartTime": "HCD",
   * "EndTime": "end time",
   * "EventName": " ",
   * "ShiftType": "0",
   * "Count": "1",
   * "patrollerIds": [ "123456"]
   * }
   * ]
   * }
   * @Response 400 - Bad Request
   * X-Reason: "Resort not found"
   * X-Reason: "Authorization header not found"
   * X-Reason: "User not found"  (may have been deleted by an admin)
   * @Response 401 - Unauthorized
   * X-Reason: "Invalid Authorization"
   */
  @SuppressWarnings("JavaDoc")
  public static class PatrolAssignments extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      Logger LOG = new Logger(PatrolAssignments.class, request, "GET", null, MIN_LOG_LEVEL);
      new GetPatrolAssignments(request, response, LOG);
    }

    private class GetPatrolAssignments extends ResourceBase {
      GetPatrolAssignments(HttpServletRequest request, HttpServletResponse response, Logger LOG) throws IOException {
        super(request, response, LOG);
        if (!initBaseAndRequireValidSession(response)) {
          return;
        }

        String szYear = request.getParameter("year");
        String szMonth = request.getParameter("month");
        String szDay = request.getParameter("day");
        LOG.info(" --patrol/assignments... resort=" + resort
                           + ", year: [" + szYear
                           + "], month: [" + szMonth
                           + "], day: [" + szDay);
        int year = convertToInt(szYear);
        int month = convertToInt(szMonth);
        int day = convertToInt(szDay);

        if ((year == 0) != (month == 0)) {
          buildAndLogErrorResponse(response, 400, "Invalid 'year' (" + szYear + "), 'month' (" + szMonth + ")", LOG);
          return;
        }

        String authenticatedUserId = nspSession.getAuthenticatedUser();
        Roster patroller = patrolData.getMemberByID(authenticatedUserId);
        if (patroller == null) {
          LOG.info("ERROR:  User not found (" + authenticatedUserId + ")");
          buildAndLogErrorResponse(response, 400, "User not found (" + authenticatedUserId + ")", LOG);
          return;
        }

        //state is OK.  Do the real work
        ObjectNode returnNode = nodeFactory.objectNode();
        returnNode.put("resort", resort);

        ArrayNode assignmentsArrayNode = nodeFactory.arrayNode();
        ArrayList<Assignments> assignmentsList = new ArrayList<Assignments>();
        if (day != 0) {
          assignmentsList = patrolData.readSortedAssignments(year, month, day);
        } else if (year != 0) {
          assignmentsList = patrolData.readSortedAssignments(year, month);
        } else {
          //entire season

          DirectorSettings directorSettings = patrolData.readDirectorSettings();
          int startDay = directorSettings.getStartDay();
          int startMonth = directorSettings.getStartMonth();
          int startYear;
          //calculate current year
          Calendar today = Calendar.getInstance();
          int todayYear = today.get(Calendar.YEAR);
          int todayMonth = today.get(Calendar.MONTH);
          startYear = (todayMonth > 6) ? todayYear : (todayYear - 1);
          int endDay = directorSettings.getEndDay();
          int endMonth = directorSettings.getEndMonth();
          int endYear = startYear + 1;

          LOG.info("full season from: "
                             + startMonth + "/" + startDay + "/" + startYear
                             + " to: " + endDay + "/" + endMonth + "/" + endYear);
          for (int mon = startMonth; mon <= 12; mon++) {
            assignmentsList.addAll(patrolData.readSortedAssignments(startYear, mon));
          }
          for (int mon = 1; mon <= endMonth; mon++) {
            assignmentsList.addAll(patrolData.readSortedAssignments(endYear, mon));
          }
        }
        int count = 0;
        for (Assignments ns : assignmentsList) {
          count++;
          assignmentsArrayNode.add(ns.toNode());
        }
        LOG.info("  -- assignments count = " + count);
        returnNode.set("assignments", assignmentsArrayNode);
        StaticUtils.buildOkResponse(response, returnNode, LOG);

        patrolData.close();
      }
    }
  }

  /**
   * query the logged in patroller's shift schedule for a specified year/month, and optionally a specific day
   *
   * @author Steve Gledhill
   * @GET http:/nsponline.org/user/assignments?
   * resort=Sample   (required)
   * year=2017
   * month=1         (1 is January)
   * day=3
   * @Header Authorization: [authToken]
   * @Response 200 - OK
   * @Header Content-Type - application/json
   * @Body {
   * "patrollerId": "123456",
   * "resort": "Sample",
   * "assignments": [
   * {
   * "Date": "2015-12-12_1",
   * "StartTime": "0900",
   * "EndTime": "1500",
   * "EventName": " ",
   * "ShiftType": "0",
   * "Count": "6",
   * "patrollerIds": [ "111111", "0", "222222", "0", "123456", "0" ]
   * },
   * {
   * "Date": "2015-12-25_1",
   * "StartTime": "HCD",
   * "EndTime": "end time",
   * "EventName": " ",
   * "ShiftType": "0",
   * "Count": "1",
   * "patrollerIds": [ "123456"]
   * }
   * ]
   * }
   * @Response 400 - Bad Request
   * X-Reason: "Resort not found"
   * X-Reason: "Authorization header not found"
   * X-Reason: "User not found"  (may have been deleted by an admin)
   * @Response 401 - Unauthorized
   * X-Reason: "Invalid Authorization"
   */
  @SuppressWarnings("JavaDoc")
  public static class UserAssignments extends HttpServlet {
    private static final int MIN_LOG_LEVEL = Logger.DEBUG;

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      Logger LOG = new Logger(UserAssignments.class, request, "GET", null, MIN_LOG_LEVEL);
      new GetUserAssignments(request, response, LOG);
    }

    private class GetUserAssignments extends ResourceBase {

      GetUserAssignments(HttpServletRequest request, HttpServletResponse response, Logger LOG) throws IOException {
        super(request, response, LOG);
        if (!initBaseAndRequireValidSession(response)) {
          return;
        }
        String szYear = request.getParameter("year");
        String szMonth = request.getParameter("month");
        String szDay = request.getParameter("day");
        int year = convertToInt(szYear);
        int month = convertToInt(szMonth);
        int day = convertToInt(szDay);

        //if no year, do everything
        //if yea, them month must exist
        if ((year == 0 && month != 0) || (year != 0 && month == 0)) {
          buildAndLogErrorResponse(response, 400, "required 'year' (" + szYear + ") and 'month' (" + szMonth + ")", LOG);
          return;
        }

        String authenticatedUserId = nspSession.getAuthenticatedUser();
        Roster patroller = patrolData.getMemberByID(authenticatedUserId);
        if (patroller == null) {
          buildAndLogErrorResponse(response, 400, "User not found (" + authenticatedUserId + ")", LOG);
          return;
        }

        //everything is OK, do the real work
        ObjectNode returnNode = nodeFactory.objectNode();
        returnNode.put("patrollerId", authenticatedUserId);
        returnNode.put("resort", resort);

        ArrayNode assignmentsArrayNode = nodeFactory.arrayNode();
        ArrayList<Assignments> assignmentsList;
        if (year == 0) {
          assignmentsList = patrolData.readAllSortedAssignments(authenticatedUserId);
        } else if (day != 0) {
          assignmentsList = patrolData.readSortedAssignments(authenticatedUserId, year, month, day);
        } else {
          assignmentsList = patrolData.readSortedAssignments(authenticatedUserId, year, month);
        }
        for (Assignments ns : assignmentsList) {
          assignmentsArrayNode.add(ns.toNode());
        }
        returnNode.set("assignments", assignmentsArrayNode);
        StaticUtils.buildOkResponse(response, returnNode, LOG);

        patrolData.close();
      }
    }
  }

  /**
   * query the logged in patroller's information, given a resort and an Authorization Token.
   * If a field is empty, then it will not be represented in the body
   *
   * @author Steve Gledhill
   * @GET http:/nsponline.org/user?
   * resort=Sample   (required)
   * @Header Authorization: [authToken]
   * @Response 200 - OK
   * @Header Content-Type - application/json
   * @Body {
   * "IDNumber": "192443",
   * "ClassificationCode": "SR",
   * "LastName": "Gledhill",
   * "FirstName": "Steve",
   * "Spouse": "Nancy",
   * "Address": "11532 Cherry Hill Drive",
   * "City": "Sandy",
   * "State": "UT",
   * "ZipCode": "84094",
   * "HomePhone": "(801) 571-7716",
   * "CellPhone": "(801) 209-5974",
   * "Pager": "none",
   * "email": "steve@gledhills.com",
   * "EmergencyCallUp": "both",
   * "NightSubsitute": "yes",
   * "Commitment": "2",
   * "Instructor": "2",
   * "Director": "yesEmail",
   * "teamLead": "1",
   * "mentoring": "0",
   * "lastUpdated": "2016-10-16"
   * }
   * @Response 400 - Bad Request
   * X-Reason: "Resort not found"
   * X-Reason: "Authorization header not found"
   * @Response 401 - Unauthorized
   * X-Reason: "Invalid Authorization"
   */
  @SuppressWarnings("JavaDoc")
  public static class GetUser extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      Logger LOG = new Logger(GetUser.class, request, "GET", null, MIN_LOG_LEVEL);
      new User(request, response, LOG);
    }

    private class User extends ResourceBase {

      User(HttpServletRequest request, HttpServletResponse response, Logger LOG) throws IOException {
        super(request, response, LOG);
        if (!initBaseAndRequireValidSession(response)) {
          return;
        }
        Roster patroller = patrolData.getMemberByID(nspSession.getAuthenticatedUser());
        if (patroller == null) {
          buildAndLogErrorResponse(response, 400, "user not found (" + nspSession.getAuthenticatedUser() + ")", LOG);
          return;
        }
        StaticUtils.buildOkResponse(response, patroller.toNode(), LOG);
        patrolData.close();
      }
    }
  }

  /**
   * query the resort's settings, given a resort and an Authorization Token.
   *
   * @author Steve Gledhill
   * @GET http:/nsponline.org/resort?
   * resort=Sample   (required)
   * @Header Authorization: [authToken]
   * @Response 200 - OK
   * @Header Content-Type - application/json
   * @Body {
   * "formalName": "Brighton",
   * "changesByDirectorsOnly": false,
   * "whenCanUserDelete": -1,
   * "patrollersCanSendMail": true
   * }
   * @Response 400 - Bad Request
   * X-Reason: "Resort not found"
   * X-Reason: "Authorization header not found"
   * @Response 401 - Unauthorized
   * X-Reason: "Invalid Authorization"
   */
  @SuppressWarnings("JavaDoc")
  public static class GetResort extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      Logger LOG = new Logger(GetResort.class, request, "GET", null, MIN_LOG_LEVEL);
      new InnerResort(request, response, LOG);
    }

    private class InnerResort extends ResourceBase {
      InnerResort(HttpServletRequest request, HttpServletResponse response, Logger LOG) throws IOException {
        super(request, response, LOG);
        if (!initBaseAndRequireValidSession(response)) {
          return;
        }
        DirectorSettings directorSettings = patrolData.readDirectorSettings();
        if (directorSettings == null) {
          buildAndLogErrorResponse(response, 400, "resort settings not found (should never happen) (" + nspSession.getAuthenticatedUser() + ")", LOG);
          return;
        }

        StaticUtils.buildOkResponse(response, directorSettings.toNode(), LOG);

        patrolData.close();
      }
    }
  }

  /**
   * query the resort's settings, given a resort and an Authorization Token.
   *
   * @author Steve Gledhill
   * @GET http:/nsponline.org/resort/list
   * @Header Authorization: [authToken]
   * @Response 200 - OK
   * @Header Content-Type - application/json
   * @Body {
   * [ "Afton", "Brighton",...]
   * }
   * @Response 400 - Bad Request
   * X-Reason: "Authorization header not found"
   * @Response 401 - Unauthorized
   * X-Reason: "Invalid Authorization"
   */
  @SuppressWarnings("JavaDoc")
  public static class ResortList extends HttpServlet {
    private static final int MIN_LOG_LEVEL = Logger.DEBUG;

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      Logger LOG = new Logger(ResortList.class, request, "GET", null, MIN_LOG_LEVEL);
      new InnerResortList(request, response, LOG);
    }

    private class InnerResortList extends ResourceBase {

      InnerResortList(HttpServletRequest request, HttpServletResponse response, Logger LOG) throws IOException {
        super(request, response, LOG);
        if (!initBaseAndRequireValidSession(response)) {
          return;
        }
        ObjectNode returnNode = nodeFactory.objectNode();
        ArrayNode resortArrayNode = nodeFactory.arrayNode();
        for (String eachResort : getResorts(connection, LOG)) {
          resortArrayNode.add(nodeFactory.textNode(eachResort));
        }
        returnNode.set("resorts", resortArrayNode);

        StaticUtils.buildOkResponse(response, returnNode, LOG);
        patrolData.close();
      }

      private List<String> getResorts(Connection connection, Logger LOG) {
        PreparedStatement dbListStatement;
        List<String> resorts = new ArrayList<String>();
        try {
          //noinspection SqlDialectInspection,SqlNoDataSourceInspection
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
        } catch (Exception e) {
          LOG.logException("Error SHOW databases:", e);
        } //end try
        return new ArrayList<String>();
      }
    }
  }

  /**
   * query the logged in patroller's information, given a resort and an Authorization Token.
   * If a field is empty, then it will not be represented in the body
   *
   * @author Steve Gledhill
   * @GET http:/nsponline.org/user?
   * resort=Sample   (required)
   * @Header Authorization: [authToken]
   * @Response 200 - OK
   * @Header Content-Type - application/json
   * @Body {  [ {
   * "IDNumber": "192443",
   * "ClassificationCode": "SR",
   * "LastName": "Gledhill",
   * "FirstName": "Steve",
   * "Spouse": "Nancy",
   * "Address": "11532 Cherry Hill Drive",
   * "City": "Sandy",
   * "State": "UT",
   * "ZipCode": "84094",
   * "HomePhone": "(801) 571-7716",
   * "CellPhone": "(801) 209-5974",
   * "Pager": "none",
   * "email": "steve@gledhills.com",
   * "EmergencyCallUp": "both",
   * "NightSubsitute": "yes",
   * "Commitment": "2",
   * "Instructor": "2",
   * "Director": "yesEmail",
   * "teamLead": "1",
   * "mentoring": "0",
   * "lastUpdated": "2016-10-16"
   * },
   * {next ...}
   * }
   * @Response 400 - Bad Request
   * X-Reason: "Resort not found"
   * X-Reason: "Authorization header not found"
   * @Response 401 - Unauthorized
   * X-Reason: "Invalid Authorization"
   */
  @SuppressWarnings("JavaDoc")
  public static class UserList extends HttpServlet {
    private static final int MIN_LOG_LEVEL = Logger.DEBUG;

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      Logger LOG = new Logger(UserList.class, request, "GET", null, MIN_LOG_LEVEL);
      new InnerUserList(request, response, LOG);
    }

    private class InnerUserList extends ResourceBase {

      InnerUserList(HttpServletRequest request, HttpServletResponse response, Logger LOG) throws IOException {
        super(request, response, LOG);
        if (!initBaseAndRequireValidSession(response)) {
          return;
        }

        ResultSet rosterResults = patrolData.resetRoster();
        int rosterSize = 0;
        Roster patroller;
        ArrayNode rosterArrayNode = nodeFactory.arrayNode();
        while ((patroller = patrolData.nextMember("", rosterResults)) != null) {
          rosterSize++;
          rosterArrayNode.add(patroller.toNode());
        }
        ObjectNode returnNode = nodeFactory.objectNode();
        returnNode.put("memberCount", rosterSize);
        returnNode.set("roster", rosterArrayNode);

        StaticUtils.buildOkResponse(response, returnNode, LOG);

        patrolData.close();
      }
    }
  }

  /**
   * @author Steve Gledhill
   */
  @SuppressWarnings({"WeakerAccess", "unused"})
  @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
  public static class LoginPayload {
    private String id;
    private String resort;
    private String password;

    public LoginPayload() {
    }

    public String toString() {
      return "id: [" + id + "], password: [" + password + "]";
    }

    @com.fasterxml.jackson.annotation.JsonProperty("id")
    public String getId() {
      return id;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("password")
    public String getPassword() {
      return password;
    }
  }
}
