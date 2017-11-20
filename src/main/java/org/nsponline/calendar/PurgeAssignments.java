package org.nsponline.calendar;

import org.nsponline.calendar.misc.PatrolData;
import org.nsponline.calendar.misc.SessionData;
import org.nsponline.calendar.misc.Utils;
import org.nsponline.calendar.misc.ValidateCredentials;
import org.nsponline.calendar.store.Assignments;
import org.nsponline.calendar.store.DirectorSettings;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.util.Calendar;

/**
 * @author Steve Gledhill
 */
public class PurgeAssignments extends HttpServlet {

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    Utils.printRequestParameters(this.getClass().getSimpleName(), request);
    new LocalPurgeAssignments(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    Utils.printRequestParameters(this.getClass().getSimpleName(), request);
    new LocalPurgeAssignments(request, response);
  }

  private class LocalPurgeAssignments {

    String szMyID;
    PrintWriter out;
    boolean purgeData;
    int startDay;
    int startMonth;
    int startYear;
    private String resort;

    private LocalPurgeAssignments(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      response.setContentType("text/html");
      out = response.getWriter();
      SessionData sessionData = new SessionData(request, out);
      ValidateCredentials credentials = new ValidateCredentials(sessionData, request, response, "MonthCalendar");
      if (credentials.hasInvalidCredentials()) {
        return;
      }
      resort = request.getParameter("resort");
      szMyID = sessionData.getLoggedInUserId();
      readParameters(request, sessionData);
      if (purgeData) {
        deleteAssignments(sessionData);
        response.sendRedirect(PatrolData.SERVLET_URL + "Directors?resort=" + resort + "&ID=" + szMyID);
      }
      else {
        PatrolData patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData); //when reading members, read full data

        OuterPage outerPage = new OuterPage(patrol.getResortInfo(), "", sessionData.getLoggedInUserId());
        outerPage.printResortHeader(out);

        printTop();
        printBody();
        printBottom();
        outerPage.printResortFooter(out);
      }
    }

    public void printTop() {
      out.println("<SCRIPT LANGUAGE=\"JavaScript\">");
//cancel button pressed
      out.println("function goHome() {");
      out.println("   location.href = \"" + PatrolData.SERVLET_URL + "Directors?resort=" + resort + "&ID=" + szMyID + "\"");
      out.println("}");
//confirmation dialog
      out.println("function validate() {");
      out.println("   if(confirm(\"delete shift assignments?\"))");
//      out.println("   alert('delete shift assignments?')");
      out.println("       return true");
      out.println("   return false");
      out.println("}");

      out.println("</SCRIPT>");
//end JavaScript code
    }

    //------------
// readData
//------------
    private void readParameters(HttpServletRequest request, SessionData sessionData) {
      PatrolData patrol = new PatrolData(PatrolData.FETCH_MIN_DATA, resort, sessionData); //when reading members, read minimal data
      DirectorSettings ds = patrol.readDirectorSettings();
//System.out.println("Original settings: "+ds.toString());
      String temp = request.getParameter("XYZ"); //allways a non-null value when returning
      purgeData = (temp != null);
      if (purgeData) {
        //read data from command line
        try {
          startDay = Integer.parseInt(request.getParameter("startDay"));
          startMonth = Integer.parseInt(request.getParameter("startMonth"));
          startYear = Integer.parseInt(request.getParameter("startYear"));
        }
        catch (Exception e) {
          return; //really should never happen
        }
      }
      else {
        //read data from database
        startDay = ds.getStartDay();
        startMonth = ds.getStartMonth();
        //calculate current year
        Calendar today = Calendar.getInstance();
        startYear = today.get(Calendar.YEAR);
      }
      patrol.close();
    }

    //------------
// deleteAssignments
//------------
    //write data to database
    private void deleteAssignments(SessionData sessionData) {

      PatrolData patrol = new PatrolData(PatrolData.FETCH_MIN_DATA, resort, sessionData); //when reading members, read minimal data
      Assignments assignment;
      int year, month, day;

      System.out.println("Purgins ALL shifts on and before " + startDay + "/" + startMonth + "/" + startYear);

      ResultSet assignmentResults= patrol.resetAssignments();
      while ((assignment = patrol.readNextAssignment(assignmentResults)) != null) {
        year = assignment.getYear();
        month = assignment.getMonth();
        day = assignment.getDay();
        if ((year < startYear) ||
            (year == startYear && month < startMonth) ||
            (year == startYear && month == startMonth && day < startDay)) {
          patrol.deleteAssignment(assignment);
        }
      }
      patrol.close();
    }

    private void printBottom() {
      out.println("<p><br></p>");
      out.println("As of: " + new java.util.Date());
    }

    public void printBody() {
      out.println("<h1 align=\"center\">Delete Old Assignments for " + PatrolData.getResortFullName(resort) + "</h1>");
      out.println("<form target='_self' onSubmit=\"return validate()\" action=\"" + PatrolData.SERVLET_URL + "PurgeAssignments\" method=POST>");
      out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"resort\" VALUE=\"" + resort + "\">");
      out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"ID\" VALUE=\"" + szMyID + "\">");
      out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"XYZ\" VALUE=\"XYZ\">");

//Directors ONLY can Purge
      out.println("DANGER! Delete all assignments dated before <select size=\"1\" name=\"startDay\">");
      for (int i = 1; i <= 31; ++i) {
        out.println("    <option " + ((i == startDay) ? "selected" : "") + ">" + i + "</option>");
      }
      out.println("  </select>&nbsp;&nbsp;<select size=\"1\" name=\"startMonth\">");
      for (int i = 1; i <= 12; ++i) {
        out.println("&nbsp;&nbsp;<option value=\"" + i + "\" " + ((i == startMonth) ? "selected" : "") + ">" + Utils.szMonthsFull[i - 1] + "</option>");
      }
      out.println("  </select>&nbsp;&nbsp;<select size=\"1\" name=\"startYear\">");
      for (int i = 0; i < 4; ++i) {
        out.println("&nbsp;&nbsp;<option " + ((i == 0) ? "selected" : "") + ">" + (startYear - i) + "</option>");
      }
      out.println("  </select>&nbsp;&nbsp;<br>");

      out.println("  <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
      out.println("<input type=\"submit\" value=\"Delete Old Assignments...\" name=\"btnDelete\">");
      out.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
      out.println("  <input type=\"button\" value=\"Cancel\" name=\"B3\" onClick=\"goHome()\"></p>");

      out.println("</form>");
    } //end printBody
  }
}