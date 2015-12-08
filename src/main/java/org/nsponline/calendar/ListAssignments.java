package org.nsponline.calendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * @author Steve Gledhill
 *         <p/>
 *         List calendar assignments for the patroller.
 *         Brighton patrollers also get a view of the locker room assignments
 */
public class ListAssignments extends HttpServlet {
  private static final boolean DEBUG = false;

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    Utils.printRequestParameters(this.getClass().getSimpleName(), request);
    new LocalListAssignments(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    Utils.printRequestParameters(this.getClass().getSimpleName(), request);
    new LocalListAssignments(request, response);
  }

  private final class LocalListAssignments {

    private String resort;

    private LocalListAssignments(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

      String patrollerId;
      PrintWriter out;

      response.setContentType("text/html");
      out = response.getWriter();
      SessionData sessionData = new SessionData(request, out);
      ValidateCredentials credentials = new ValidateCredentials(sessionData, request, response, "ListAssignments");
      if (credentials.hasInvalidCredentials()) {
        return;
      }
      //by now, sessionData.getLoggedInUserId and sessionData.getLoggedInResort are valid
      resort = sessionData.getLoggedInResort();
      patrollerId = sessionData.getLoggedInUserId();

      PatrolData patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData); //when reading members, read full data
      OuterPage outerPage = new OuterPage(patrol.getResortInfo(), "", sessionData.getLoggedInUserId());
      outerPage.printResortHeader(out);
      printTop(out);
      printMiddle(out, resort, patrollerId, sessionData);
      printBottom(out);
      outerPage.printResortFooter(out);
      debugOut("leaving ListAssignments");
    }

    public void printTop(PrintWriter out) {

      out.println("<script>");
      out.println("function printWindow(){");
      out.println("   bV = parseInt(navigator.appVersion)");
      out.println("   if (bV >= 4) window.print()");
      out.println("}");
      out.println("</script>");

      out.println("<h1><img border=\"0\" src=\"/images/ski_accident_md_wht.gif\" align=top>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Shift Summary</h1>");
    }

    private void printMiddle(PrintWriter out, String resort, String szMyID, SessionData sessionData) {
      String myName;
      int myID;
      int shiftType;
      int[] shiftCounts = new int[Assignments.MAX_SHIFT_TYPES];
      if (szMyID.equalsIgnoreCase(sessionData.getBackDoorUser())) {
        //backdoor login, don't display anything
        out.println("Have a great day<br>");
        return;
      }

      PatrolData patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData);
      MemberData member = patrol.getMemberByID(szMyID);
      myID = Integer.parseInt(szMyID);
      myName = member.getFullName();

      patrol.resetRoster();
      if (resort.equals("Brighton")) {
        Calendar calendar = new GregorianCalendar();

        //noinspection MagicConstant
        calendar.set(2013, 3, 1);  //(yyyy,mm,dd) Month is 0 based

        out.println("Display your: <a target='main' href=\"/screenshots/history.php?ID=" + szMyID + "\"><b>check-in history</b></a>");
        out.println(" or <a target='main' href=\"screenshots/ski_credits.php?ID=" + szMyID + "\"><b>Ski Credits Earnigs report</b></a>");

        out.println(" (Ski History & Credits are updated at different times, so may not appear in sync.)<br>");
      }
      out.println("<p><font size=5>" + myName + "'s Assignment Schedule for " + PatrolData.getResortFullName(resort) + "</font>");
      out.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
//      out.println("<a href=\"javascript:printWindow()\">Print This Page</a>");
      out.println("</font></p>");
      out.println("<div align=\"left\">");
      out.println("  <table border=\"1\" width=\"550\" bgcolor=\"#FFFFFF\" cellpadding=\"0\" cellspacing=\"0\">");
//loop through all assignments
      for (shiftType = 0; shiftType < Assignments.MAX_SHIFT_TYPES; ++shiftType) {
        shiftCounts[shiftType] = 0;
      }
      int totalShifts = 0;
 //     patrol.resetAssignments();
 //     Assignments ns;
      for (Assignments ns : patrol.readAllSortedAssignments(szMyID)) {
        String szDate = ns.getExpandedDateAndTimeString();
        String szStartTime = ns.getStartingTimeString();
        String szEndTime = ns.getEndingTimeString();
        int pat0;
        //loop through all patrollers on this assignment
//        boolean isWeekendDay = false;
        for (shiftType = 0; shiftType < Assignments.MAX_ASSIGNMENT_SIZE; ++shiftType) {
          if (ns.getPosID(shiftType) == null || ns.getPosID(shiftType).equals("")) {
            continue;
          }
          try {
            pat0 = Integer.parseInt(ns.getPosID(shiftType));
          }
          catch (Exception e) {
            System.out.println("error id (" + ns.getPosID(shiftType) + ")");
            continue;
          }
          if (Math.abs(pat0) == myID) {  //check if 'myID'
            ++shiftCounts[ns.getType()];
            ++totalShifts;
            showNight(out, szDate + " (" + szStartTime + " - " + szEndTime + ")", (pat0 > 0), ns.getType());
          }
        }
      }
      if (totalShifts == 0)   //did I have any assignments
      {
        showNight(out, "No shifts were scheduled", true, -1);
      }

      patrol.close();
      out.println("  </table>");
      out.println("</div>");
//only display sub totals if non zero
      out.println("<br><b>Totals</b>");
      for (shiftType = 0; shiftType < Assignments.MAX_SHIFT_TYPES; ++shiftType) {
        if (shiftCounts[shiftType] > 0) {
          out.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + Assignments.getShiftName(shiftType) + " = <b>" + shiftCounts[shiftType] + "</b>");
        }
      }
      out.println("<br><b>Total shifts</b>:&nbsp;&nbsp;&nbsp;<b>" + totalShifts + "</b>"); //brighton

    }

    private void showNight(PrintWriter out, String fullString, boolean ok, int shiftType) {
      out.println("<tr><td>");
      if (!ok) {
        fullString += " &lt;Shift Missed&gt;";
        out.println("  <td valign=\"middle\" BGCOLOR=\"#80ffff\" align=\"left\">");
      }
      else {
        out.println("  <td valign=\"middle\" align=\"left\">");
      }
      out.println("&nbsp;" + fullString + "</td><td>&nbsp;");
      if (shiftType >= 0 && shiftType <= Assignments.MAX_SHIFT_TYPES) {
        out.println(Assignments.getShiftName(shiftType));
      }

      out.println("</td></tr>");
    }

    private void printBottom(PrintWriter out) {
      out.println("<br><br>As of: " + new java.util.Date());
    }

    private void debugOut(String msg) {
      if (DEBUG) {
        System.out.println("DEBUG-ListAssignments(" + resort + "): " + msg);
      }
    }
  }
}
