package org.nsponline.calendar.rest;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.nsponline.calendar.misc.*;
import org.nsponline.calendar.store.Assignments;
import org.nsponline.calendar.store.DirectorSettings;
import org.nsponline.calendar.store.NspSession;
import org.nsponline.calendar.store.Roster;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * query the patroller's entire shift schedule for a specified year/month, and optionally a specific day
 *
 * @GET
 *     http:/nsponline.org/patrol/assignments?
 *      resort=Sample (required)
 *      year=2017     (required)
 *      month=1       (required,  1 is January)
 *      day=3         (optional)
 * @Header Authorization: [authToken]
 *
 * @Response 200 - OK
 * @Header Content-Type - application/json
 * @Body
 *    {
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
@SuppressWarnings("JavaDoc")
public class PatrolAssignments extends HttpServlet {
  private static final int MIN_LOG_LEVEL = Logger.DEBUG;

  private Logger LOG;

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    LOG = new Logger(PatrolAssignments.class, request, "GET", null, MIN_LOG_LEVEL);
    LOG.logRequestParameters();
    getPatrolAssignments(request, response);
  }

  @SuppressWarnings("Duplicates")
  private void getPatrolAssignments(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    response.setContentType("application/json");
    PrintWriter out = response.getWriter();
    String resort = request.getParameter("resort");
    String szYear = request.getParameter("year");
    String szMonth = request.getParameter("month");
    String szDay = request.getParameter("day");
    Logger.logStatic(" --patrol/assignments... resort=" + resort
        + ", year: [" + szYear
        + "], month: [" + szMonth
        + "], day: [" + szDay);
    int year = Utils.convertToInt(szYear);
    int month = Utils.convertToInt(szMonth);
    int day = Utils.convertToInt(szDay);

    String sessionId = request.getHeader("Authorization");
    if (Utils.isEmpty(sessionId)) {
      Utils.buildAndLogErrorResponse(response, 400, "Authorization header not found");
      return;
    }
    if (!PatrolData.isValidResort(resort)) {
      Utils.buildAndLogErrorResponse(response, 400, "Resort not found (" + resort + ")");
      return;
    }
    if ((year == 0) != (month == 0)) {
      Utils.buildAndLogErrorResponse(response, 400, "Invalid 'year' (" + szYear + "), 'month' (" + szMonth+ ")");
      return;
    }

    SessionData sessionData = new SessionData(request, out, LOG);
    PatrolData patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData, LOG);
    Connection connection = patrol.getConnection();
    NspSession nspSession = NspSession.read(connection, sessionId);
    if (nspSession == null) {
      Logger.logStatic("ERROR:  Invalid Authorization (" + sessionId + ")");
      Utils.buildAndLogErrorResponse(response, 401, "Invalid Authorization (" + sessionId + ")");
      return;
    }
    String authenticatedUserId = nspSession.getAuthenticatedUser();
    Roster patroller = patrol.getMemberByID(authenticatedUserId);
    if (patroller == null) {
      Logger.logStatic("ERROR:  User not found (" + authenticatedUserId + ")");
      Utils.buildAndLogErrorResponse(response, 400, "User not found (" + authenticatedUserId + ")");
      return;
    }

    //state is OK.  Do the real work
    ObjectNode returnNode = Utils.nodeFactory.objectNode();
    returnNode.put("resort", resort);

    ArrayNode assignmentsArrayNode = Utils.nodeFactory.arrayNode();
    ArrayList<Assignments> assignmentsList = new ArrayList<Assignments>();
    if (day != 0) {
      assignmentsList = patrol.readSortedAssignments(year, month, day);
    }
    else if (year != 0 ) {
      assignmentsList = patrol.readSortedAssignments(year, month);
    }
    else {
      //entire season

      DirectorSettings directorSettings = patrol.readDirectorSettings();
      int startDay = directorSettings.getStartDay();
      int startMonth = directorSettings.getStartMonth();
      int startYear;
      //calculate current year
      Calendar today = Calendar.getInstance();
      int todayYear = today.get(Calendar.YEAR);
      int todayMonth = today.get(Calendar.MONTH);
      startYear = (todayMonth > 6) ? todayYear: (todayYear - 1);
      int endDay = directorSettings.getEndDay();
      int endMonth = directorSettings.getEndMonth();
      int endYear = startYear + 1;

      Logger.logStatic("full season from: "
          + startMonth + "/" + startDay + "/" + startYear
          + " to: " + endDay + "/" + endMonth + "/" + endYear);
      for(int mon = startMonth; mon <= 12; mon++) {
        assignmentsList.addAll(patrol.readSortedAssignments(startYear, mon));
      }
      for(int mon = 1; mon <= endMonth; mon++) {
        assignmentsList.addAll(patrol.readSortedAssignments(endYear, mon));
      }
    }
    int count = 0;
    for (Assignments ns : assignmentsList) {
      count++;
      assignmentsArrayNode.add(ns.toNode());
    }
    Logger.logStatic("  -- assignments count = " + count);
    returnNode.set("assignments", assignmentsArrayNode);
    Utils.buildOkResponse(response, returnNode);

    patrol.close();
  }
}

