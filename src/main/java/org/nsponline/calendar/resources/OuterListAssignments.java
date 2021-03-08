package org.nsponline.calendar.resources;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.nsponline.calendar.store.Assignments;
import org.nsponline.calendar.store.Roster;
import org.nsponline.calendar.utils.Logger;
import org.nsponline.calendar.utils.PatrolData;
import org.nsponline.calendar.utils.SessionData;

public class OuterListAssignments extends ResourceBase {

  OuterListAssignments(HttpServletRequest request, HttpServletResponse response, Logger LOG) throws IOException {
    super(request, response, LOG);
    if (!initBaseAndAskForValidCredentials(response, "ListAssignments")) {
      return;
    }

    printCommonHeader();
    printTop(out);
    printMiddle(out, resort,  sessionData.getLoggedInUserId(), sessionData);
    printBottom(out);
    printCommonFooter();
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

    PatrolData patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData, LOG);
    Roster member = patrol.getMemberByID(szMyID);
    myID = Integer.parseInt(szMyID);
    myName = member.getFullName();

    Calendar calendarStart = new GregorianCalendar();
    int currentYear = calendarStart.get(Calendar.YEAR);
    int currentMonth = calendarStart.get(Calendar.MONTH);
    int startYear = 2002;
    int startMonth = Calendar.JULY;

    if (resort.equals("Brighton")) {
      if (currentMonth < Calendar.JULY){
        startYear = currentYear - 1;
      }
      out.println("Display your: <a target='main' href=\"/screenshots/history.php?ID=" + szMyID + "\"><b>check-in history</b></a>");
      out.println(" or <a target='main' href=\"screenshots/ski_credits.php?ID=" + szMyID + "\"><b>Ski Credits Earnings report</b></a>");

      out.println(" (Ski History & Credits are updated at different times, so may not appear in sync.)<br>");
    }

    calendarStart.set(startYear, startMonth, 1);  //(yyyy,mm,dd) Month is 0 based
    LOG.debug("zzzz calendar start " + calendarStart.toString());
    out.println("<p><font size=5>" + myName + "'s Assignment Schedule for " + PatrolData.getResortFullName(resort, LOG) + "</font>");
    out.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
//      out.println("<a href=\"javascript:printWindow()\">Print This Page</a>");
    out.println("</font></p>");
    out.println("<div align=\"left\">");
    out.println("  <table border=\"1\" width=\"550\" bgcolor=\"#FFFFFF\" cellpadding=\"0\" cellspacing=\"0\">");
//loop through all assignments
    for (shiftType = 0; shiftType < Assignments.MAX_SHIFT_TYPES; ++shiftType) {
      shiftCounts[shiftType] = 0;
    }
    String LT_GREEN_BG_COLOR = "bgcolor=\"#D7F5ED\"";
    String WHITE_BG_COLOR = "bgcolor=\"#FFFFFF\"";
    String bgColor = WHITE_BG_COLOR;
    int totalShifts = 0;
    //     patrol.resetAssignments();
    //     Assignments ns;
    int lastMonth = 0;
    for (Assignments ns : patrol.readAllSortedAssignments(szMyID)) {
      Calendar assignmentDate = new GregorianCalendar(ns.getYear(), ns.getMonth(), ns.getDay());
      if (assignmentDate.before(calendarStart)) {
        continue;
      }
      String szDate = ns.getExpandedDateAndTimeString();
      String szStartTime = ns.getStartingTimeString();
      String szEndTime = ns.getEndingTimeString();
      if (lastMonth != ns.getMonth()) {
        lastMonth = ns.getMonth();
        bgColor = (bgColor.equals(WHITE_BG_COLOR)) ? LT_GREEN_BG_COLOR : WHITE_BG_COLOR;
      }
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
          LOG.error("error id (" + ns.getPosID(shiftType) + ")");
          continue;
        }
        if (Math.abs(pat0) == myID) {  //check if 'myID'
          ++shiftCounts[ns.getType()];
          ++totalShifts;
          showNight(out, szDate + " (" + szStartTime + " - " + szEndTime + ")", (pat0 > 0), ns.getType(), bgColor);
        }
      }
    }
    if (totalShifts == 0)   //did I have any assignments
    {
      showNight(out, "No shifts were scheduled", true, -1, WHITE_BG_COLOR);
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

  private void showNight(PrintWriter out, String fullString, boolean ok, int shiftType, String bgColor) {
    out.println("<tr " + bgColor + "><td>");
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

  @SuppressWarnings("SameParameterValue")
  private void debugOut(String msg) {
    LOG.debug("DEBUG-ListAssignments resort=" + resort + ", " + msg);
  }
}