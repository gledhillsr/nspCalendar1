package org.nsponline.calendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class ListAssignments extends HttpServlet {


  //------
// doGet
//------
  public void doGet(HttpServletRequest request,
                    HttpServletResponse response)
      throws IOException, ServletException {
    String szMyID;
    PrintWriter out;
    String resort;

    response.setContentType("text/html");
    out = response.getWriter();
    synchronized (this) {
//System.out.println("ListAssignment: resort="+request.getParameter("resort"));
//System.out.println("ListAssignment: NSPgoto="+request.getParameter("NSPgoto"));
//System.out.println("ListAssignment: ID="+request.getParameter("ID"));
      SessionData sessionData = new SessionData(request.getSession(), out);

      CookieID cookie = new CookieID(sessionData, request, response, "ListAssignments", null);
      if (cookie.error) {
        return;
      }
      resort = request.getParameter("resort");
      szMyID = sessionData.getLoggedInUserId();
      if (szMyID != null) {

        printTop(out);
        if (PatrolData.validResort(resort)) {
          printMiddle(out, resort, szMyID, sessionData);
        }
        else {
          out.println("Invalid host resort.");
        }
        printBottom(out);
      }
    }
  }

  //-------
// doPost
//-------
  public void doPost(HttpServletRequest request,
                     HttpServletResponse response)
      throws IOException, ServletException {
    doGet(request, response);
  }

  //---------
// printTop
//---------
  public void printTop(PrintWriter out) {
    out.println("<HTML>");
    out.println("<HEAD>");
    out.println("<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=windows-1252\">");
    out.println("<META HTTP-EQUIV=\"Content-Language\" CONTENT=\"en-us\">");
    out.println("<TITLE>List My Shift Assignments</TITLE>");

    out.println("<META HTTP-EQUIV=\"Pragma\" CONTENT=\"no-cache\">");
    out.println("<META HTTP-EQUIV=\"Expires\" CONTENT=\"-1\">");
    out.println("</HEAD>");

    out.println("<BODY BGCOLOR=\"#FFEEFF\">");

    out.println("<script>");
    out.println("function printWindow(){");
    out.println("   bV = parseInt(navigator.appVersion)");
    out.println("   if (bV >= 4) window.print()");
    out.println("}");
    out.println("</script>");

//      out.println("<h1 align=\"center\">Shift Summary</h1>");
//      out.println("<h3><img border=\"0\" src=\"/nspImages/ski_accident_md_wht.gif\" width=\"140\" height=\"120\"></h3>");
    out.println("<h1><img border=\"0\" src=\"http://nsponline.org/images/ski_accident_md_wht.gif\" align=top>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Shift Summary</h1>");
  }

  //------------
// printMiddle (submitterID, transaction, selectedID, date1, pos1, listName)
//------------
  private void printMiddle(PrintWriter out, String resort, String szMyID, SessionData sessionData) {
    String myName;
    int myID;
    int i;
//    java.util.Date date1;
//    int dayShiftCnt = 0;
//    int nightShiftCnt = 0;
    int[] shiftCounts = new int[Assignments.MAX_SHIFT_TYPES];
    if (szMyID.equalsIgnoreCase(PatrolData.backDoorUser)) {
      //backdoor login, don't display anything
      out.println("Have a great day<br>");
      return;
    }

//read in entire patrol
    PatrolData patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData);
//my data
    MemberData member = patrol.getMemberByID(szMyID);
    myID = Integer.parseInt(szMyID);
    myName = member.getFullName();

    patrol.resetAssignments();
    patrol.resetRoster();
//    int myNightCount = 0;
    Assignments ns;
    if (resort.equals("Brighton")) {
//      String szDays[] = {"Error", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Error"};
      Calendar calendar = new GregorianCalendar();
//====================================================================
      //noinspection MagicConstant
      calendar.set(2013, 3, 1);  //(yyyy,mm,dd) Month is 0 based
//====================================================================
//      long millis = calendar.getTimeInMillis() / 1000;

//	    out.println("Display your: <a href=\"/screenshots/history.php?ID="+szMyID+"&millis="+millis+"\"><b>check-in history</b></a>");
//	    out.println(" or <a href=\"/screenshots/ski_credits.php?ID="+szMyID+"&millis="+millis+"\"><b>Ski Credits Earnigs report</b></a>");
      out.println("Display your: <a href=\"/screenshots/history.php?ID=" + szMyID + "\"><b>check-in history</b></a>");
      out.println(" or <a href=\"/screenshots/ski_credits.php?ID=" + szMyID + "\"><b>Ski Credits Earnigs report</b></a>");
//	    out.println(" (Last updated on "+szDays[calendar.get(Calendar.DAY_OF_WEEK)]+", "+(calendar.get(Calendar.MONTH)+1)+"/"+calendar.get(Calendar.DAY_OF_MONTH)+"/"+calendar.get(Calendar.YEAR)+")<br>");
      out.println(" (Ski History & Credits are updated at different times, so may not appear in sync.)<br>");
    }
    out.println("<p><font size=5>" + myName + "'s Assignment Schedule for " + PatrolData.getResortFullName(resort) + "</font>");
    out.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
    out.println("<a href=\"javascript:printWindow()\">Print This Page</a></font></p>");
    out.println("<div align=\"left\">");
    out.println("  <table border=\"1\" width=\"550\" bgcolor=\"#FFFFFF\" cellpadding=\"0\" cellspacing=\"0\">");
//loop through all assignments
    for (i = 0; i < Assignments.MAX_SHIFT_TYPES; ++i) {
      shiftCounts[i] = 0;
    }
    int totalShifts = 0;
    while ((ns = patrol.readNextAssignment()) != null) {
      String szDate = ns.getExpandedDateAndTimeString();
      String szStartTime = ns.getStartingTimeString();
      String szEndTime = ns.getEndingTimeString();
      int pat0;
      //loop through all patrollers on this assignment
//        boolean isWeekendDay = false;
      for (i = 0; i < Assignments.MAX; ++i) {
        if (ns.getPosID(i) == null || ns.getPosID(i).equals("")) {
          continue;
        }
        try {
          pat0 = Integer.parseInt(ns.getPosID(i));
        }
        catch (Exception e) {
          System.out.println("error id (" + ns.getPosID(i) + ")");
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
    for (i = 0; i < Assignments.MAX_SHIFT_TYPES; ++i) {
      if (shiftCounts[i] > 0) {
        out.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + Assignments.szShiftTypes[i] + " = <b>" + shiftCounts[i] + "</b>");
      }
    }
    out.println("<br><b>Total shifts</b>:&nbsp;&nbsp;&nbsp;<b>" + totalShifts + "</b>"); //brighton

  } // end printMiddle


  //------------
// showNight
//------------
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
      out.println(Assignments.szShiftTypes[shiftType]);
    }

    out.println("</td></tr>");
  }

  //------------
// printBottom
//------------
  private void printBottom(PrintWriter out) {
    out.println("<br><br>As of: " + new java.util.Date());
    out.println("</body>");
    out.println("</html>");
  } //end printBottom()

} //end class ListAssignments
