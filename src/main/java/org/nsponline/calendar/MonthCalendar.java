package org.nsponline.calendar;


import org.nsponline.calendar.misc.*;
import org.nsponline.calendar.store.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.util.*;

/**
 * @author Steve Gledhill
 *         <p/>
 *         display a 1 month calendar
 */
public class MonthCalendar extends nspHttpServlet {
  private static final boolean DEBUG = false;
  private static final int[] iDaysInMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

  @Override
  Class<?> getServletClass() {
    return this.getClass();
  }

  String getParentIfBadCredentials() {
    return null; //do not redirect
  }

  @Override
  void servletBody(final HttpServletRequest request, final HttpServletResponse response) {
    new InnerMonthCalendar().runner(request, response);
    }

  private class InnerMonthCalendar {
    private Calendar calendar;
    private java.util.Date trialTime;
    private Assignments[][] monthData;
    private HashMap<String, NewIndividualAssignment> monthNewIndividualAssignments = new HashMap<String, NewIndividualAssignment>();
    private ShiftDefinitions[][] PredefinedDayShifts;
    private int currYear = 0;   //not initialized
    private int currMonth = 0; //0 based month
    private int realCurrMonth = 0;
    private int realCurrYear = 0;
    private int realCurrDate = 0;
    private int seasonStartDay;
    private int seasonStartMonth;
    private int seasonEndDay;
    private int seasonEndMonth;
    private int textFontSize;

    private int nextMonth, nextYear;
    private int prevMonth, prevYear;

    private String patrollerId;
    private Hashtable<Integer, String> names;
    private boolean isDirector;
    private DirectorSettings directorSettings;
    private boolean denseFormat;
    private int maxAssignmentCnt;
    private int maxNameLen;
    private int textLen;
    private int dayWidth;   //normal is 14 and 14 (100/7 = 14) ps need to FIX is other spots also
    private int wkEndWidth; //big weekend is 11 and 22 (100/9)
    private boolean notLoggedIn;
    private String patrollerIdTag;


    public void runner(final HttpServletRequest request, final HttpServletResponse response) {

      String szMonth;
      String szYear;
      textFontSize = 10;
      currYear = 0;   //not initialized
      currMonth = 0; //0 based month
      realCurrMonth = 0;
      realCurrYear = 0;
      realCurrDate = 0;
      names = new Hashtable<Integer, String>(Roster.MAX_MEMBERS);

      //has the user set an "override" for the length of each text row
      if (Utils.isNotEmpty(request.getParameter("textLen"))) {
        textLen = new Integer(request.getParameter("textLen"));
      }
      else {
        textLen = 0;  //undefined
      }

//      new ValidateCredentials(sessionData, request, response, null);  //do not redirect
      patrollerId = sessionData.getLoggedInUserId();
      if (Utils.isNotEmpty(patrollerId)) {
        patrollerIdTag = "&ID=" + patrollerId;
        notLoggedIn = false;
      }
      else {
        patrollerIdTag = "";
        notLoggedIn = true;
      }

      isDirector = false;
      directorSettings = null;
      szMonth = request.getParameter("month");
      szYear = request.getParameter("year");

      calendar = new GregorianCalendar(TimeZone.getDefault());
//todo the next 2 line are scary.  Why are they here.  I remember something where the current day was weird back east
      trialTime = new java.util.Date();
      calendar.setTime(trialTime);

      realCurrDate = calendar.get(Calendar.DATE);
      realCurrMonth = calendar.get(Calendar.MONTH);
      realCurrYear = calendar.get(Calendar.YEAR);

      if (!isDateInvalidOrMissing(szMonth, szYear)) {
        currMonth = new Integer(szMonth);
        currYear = new Integer(szYear);
      }
      else {
        if (szYear != null && szMonth != null) {
//          out.println("invalid calendar date specified, starting calendar at: " + szMonths[realCurrMonth] + " " + realCurrYear);
          debugOut("invalid calendar date, starting calendar at: (0 based month) " + realCurrMonth + "/1/" + realCurrYear);
        }
        currMonth = realCurrMonth ;
        currYear = realCurrYear;
      }
      calendar.set(currYear, currMonth, 1);

      if (!PatrolData.isValidResort(resort)) {
        out.println("ERROR.  Invalid resort=" +resort);
        return;
      }

      PatrolData patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData, LOG); //when reading members, read full data

      OuterPage outerPage = new OuterPage(patrol.getResortInfo(), getJavaScriptAndStyles(), sessionData.getLoggedInUserId());

      outerPage.printResortHeader(out);

      monthData = new Assignments[32][ShiftDefinitions.MAX + 5]; //all shifts for all days in 1 month
      getPrevNextDateInfo(); //reset calendar to 1st of month
      readData(out, resort, sessionData);

      printTopOfPage(out, resort);
      printCalendarDays(out, resort);
      printEndOfPage(out, resort);
      outerPage.printResortFooter(out);
    }

    private boolean isDateInvalidOrMissing(String szMonth, String szYear) {
      if (szMonth == null || szYear == null) {
        return true;
      }
      int paramMonth = new Integer(szMonth);
      int paramYear = new Integer(szYear);

      if (paramMonth < 0 || paramMonth > 12) {
        return true;
      }
      //noinspection RedundantIfStatement
      if (paramYear < (realCurrYear - 2) || paramYear > (realCurrYear + 1)) {
        return true;
      }
      return false;
    }

    private void getPrevNextDateInfo() {
      // create a GregorianCalendar with the Mountain Daylight time zone
      // and the current date and time

//      if (currYear == 0) { //values NOT passed in
//        currMonth = realCurrMonth;
//        currYear = realCurrYear;
//      }
      //noinspection MagicConstant
      if (currMonth == 0) { //beginning of year?
        prevMonth = 11;
        prevYear = currYear - 1;
      }
      else {
        prevMonth = currMonth - 1;
        prevYear = currYear;
      }
//get #'s for next month
      if (currMonth == 11) { //end of year?
        nextMonth = 0;
        nextYear = currYear + 1;
      }
      else {
        nextMonth = currMonth + 1;
        nextYear = currYear;
      }
    }

    @SuppressWarnings("ConstantConditions")
    private void readData(PrintWriter out, String resort, SessionData sessionData) {
      Roster member;
      PatrolData patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData, LOG); //when reading members, read full data
      ResultSet rosterResults = patrol.resetRoster();
      directorSettings = patrol.readDirectorSettings();
      while ((member = patrol.nextMember("", rosterResults)) != null) {
        names.put(member.getIdNum(), member.getFullName() + ", " + member.getHomePhone());
        if (member.getID().equals(patrollerId) || sessionData.getBackDoorUser().equals(patrollerId)) {
          isDirector = member.isDirector();
        }
      }
      monthNewIndividualAssignments = patrol.readNewIndividualAssignments(currYear, currMonth + 1, 0); //entire month

      maxAssignmentCnt = 0;

      populateMonthDataArray(patrol);

      //  decide if I will make weekend shifts in two columns
      if (resort.contains("Jackson")) {
        maxNameLen = 30;
        denseFormat = false;
        dayWidth = 14;
        wkEndWidth = 14;
      }
      else if (textLen > 0) {
        //user set an "override"
        maxNameLen = textLen;
        denseFormat = true;
        dayWidth = 11;
        wkEndWidth = 22;
      }
      else if (maxAssignmentCnt > 12 || resort.equals("Brighton")) {
        maxNameLen = 20;
        denseFormat = true;
        dayWidth = 11;
        wkEndWidth = 22;
      }
      else {
        maxNameLen = 30;
        denseFormat = false;
        dayWidth = 14;
        wkEndWidth = 14;
      }
      PredefinedDayShifts = new ShiftDefinitions[8][ShiftDefinitions.MAX]; //all daily shifts 1-7, 0 is not used in Calendar.Sunday
//      patrol.resetShiftDefinitions();
      int lastDayOfWeekIndex = -1;
      int pos = 0;
      for (ShiftDefinitions shift : patrol.readShiftDefinitions()) {
        String name = shift.parsedEventName();
        int currDayOfWeekIndex = getDayOfWeekIndexOrNegativeOne(name);

        if (currDayOfWeekIndex == lastDayOfWeekIndex) {
          ++pos;
        }
        else {
          pos = 0;
        }
        lastDayOfWeekIndex = currDayOfWeekIndex;

        if (lastDayOfWeekIndex >= 0) {
          PredefinedDayShifts[lastDayOfWeekIndex][pos] = shift;
        }
      }

      seasonStartDay = directorSettings.getStartDay();
      seasonStartMonth = directorSettings.getStartMonth();
      seasonEndDay = directorSettings.getEndDay();
      seasonEndMonth = directorSettings.getEndMonth();

      patrol.close();
    } //end of readdata

    private int getDayOfWeekIndexOrNegativeOne(String name) {
      int newDay;
      if ("Sunday".equals(name)) {
        newDay = Calendar.SUNDAY;
      }
      else if ("Monday".equals(name)) {
        newDay = Calendar.MONDAY;
      }
      else if ("Tuesday".equals(name)) {
        newDay = Calendar.TUESDAY;
      }
      else if ("Wednesday".equals(name)) {
        newDay = Calendar.WEDNESDAY;
      }
      else if ("Thursday".equals(name)) {
        newDay = Calendar.THURSDAY;
      }
      else if ("Friday".equals(name)) {
        newDay = Calendar.FRIDAY;
      }
      else if ("Saturday".equals(name)) {
        newDay = Calendar.SATURDAY;
      }
      else {
        newDay = -1;
      }
      return newDay;
    }

    private void populateMonthDataArray(PatrolData patrol) {
      int oldYear, oldMonth, oldDay;
      oldYear = oldMonth = oldDay = 0;
      int pos = 0;
//      Assignments assignments;
      int year;
      int month;
      int day;
      int curPos = 0;

      ArrayList<Assignments> monthOfAssignments = patrol.readSortedAssignments(currYear, currMonth + 1);

//      patrol.resetAssignments();
//      while ((assignments = patrol.readNextAssignment()) != null) {
      for (Assignments assignments : monthOfAssignments) {
        year = assignments.getYear();
        month = assignments.getMonth();
        day = assignments.getDay();
//        pos = assignments.getDatePos();
        if (year == oldYear && month == oldMonth && day == oldDay) {
          ++pos;
        }
        else {
          pos = 0;
          curPos = 0;
        }
        curPos += assignments.getCount();
        //is the the month we are printing ?
        if (year == currYear && month == (currMonth + 1)) {
          monthData[day][pos] = assignments;
          debugOut("monthData[" + day + "][" + pos + "] = " + assignments.toString());
          if (curPos > maxAssignmentCnt) {
            maxAssignmentCnt = curPos;
          }
        }

        oldYear = year;
        oldMonth = month;
        oldDay = day;
      }
    }

    public String getJavaScriptAndStyles() {
      return "<style type='text/css'>\n" +
        ".calendar td    {font-size:" + textFontSize + "pt; font-family:arial,helvetica; padding:1px}\n" +
        ".calendar a    {target:_self}\n" +
        "</style>";
    }

    public void printTopOfPage(PrintWriter out, String resort) {


      out.println("<html>");
      out.println("<head><title>" + PatrolData.getResortFullName(resort) + " Schedule</title>");

      out.println("<SCRIPT LANGUAGE = 'JavaScript'>");

//cancel button pressed
      out.println("function goLogin() {");
      String szLogin = "MemberLogin?resort=" + resort;
      out.println("window.location='" + szLogin + "'");
      out.println("}");


      out.println("function dispEvent(url,wname) {");
      out.println("    window.open(url, wname, 'scrollbars=yes,toolbar=no,status=no,location=no,menubar=no,resizable=yes,height=450,width=620,left=10,top=10')");
      out.println("}");

      out.println("function printWindow(){");
      out.println("   bV = parseInt(navigator.appVersion)");
      out.println("   if (bV >= 4) window.print()");
      out.println("}");

      out.println(
        "function goDayShifts(dayOfWeek, date) {\n" +
          //resort, ID, month and year are constant
          "window.location('/calendar-1/DayShifts" +
          "?resort=" + resort +
          "&ID=" + patrollerId +
          "&month=" + currMonth +
          "&year=" + currYear +
          "&dayOfWeek=' + dayOfWeek + '" +
          "&date=' + date );\n" +
          "}\n");
//      out.println("function dispEvent(url,wname) {");
//      out.println("    window.open(url, wname, 'scrollbars=yes,toolbar=no,status=no,location=no,menubar=no,resizable=yes,height=450,width=620,left=10,top=10')");
//      out.println("}");
//
//      out.println("function printWindow(){");
//      out.println("   bV = parseInt(navigator.appVersion)");
//      out.println("   if (bV >= 4) window.print()");
//      out.println("}");
//
//      out.println("function printHelp(){");
//      out.println("   alert('To make your print out fit nicely on one page, you can.\\n1) Change your 'Page Setup' from the File Menu: Set all margins to 0.25, and remove the Header and Footer data.\\n2) Change the Font Size (using the drop down menu at the bottom)\\n3) The 'Name Len' field allows you to select a total display length of a single line to:\\n    15 characters (Condensed), 20 chars (Medium), 30 chars (Full)')");
//      out.println("}");
//
//      out.println("function resizeMe(goStr){");
////NameLen 15,20,30 == 	Condensed, Medium, Full
//      out.println("  idx = document.myForm.textLen.options.selectedIndex");
//      out.println("  if(idx == 0)      val2='&textLen=15'");
//      out.println("  else if(idx == 1) val2='&textLen=20'");
//      out.println("  else              val2='&textLen=30'");
////Font Size
//      out.println("  idx = document.myForm.FontSize.options.selectedIndex");
//      out.println("  val3 = '&FontSize=' + document.myForm.FontSize.options[idx].text");
//      out.println("  val4=goStr+val2+val3");
//
////out.println("  alert(val4)");
//      out.println("  window.location=val4");
//      out.println("}");

      out.println("</SCRIPT>");

      out.println("<FORM target='_self' name='myForm'>");
      out.println("<TABLE BORDER='0' CELLSPACING='0' CELLPADDING='0' WIDTH='100%'>");
      out.println("<TR><TD ALIGN='LEFT' VALIGN='Bottom'><BR>");
      String testingMessage = "";
      if (PatrolData.USING_TESTING_ADDRESS) {
        testingMessage = "ERROR!!! Using to older database!!! Contact Steve Gledhill (801)209-5974 ASAP";
      }
      out.println("<FONT FACE='Arial, Helvetica' COLOR='000000' SIZE='4'><B>" + testingMessage + PatrolData.getResortFullName(resort) + " - Shift Schedule for " + Utils.szMonthsFull[calendar.get(Calendar.MONTH)] + " " + calendar.get(Calendar.YEAR) + "</B></FONT>");
      out.println("<font size=3>");
      out.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
      if (notLoggedIn) {
        out.println("<INPUT TYPE='button' VALUE='Login for complete details' onClick='goLogin()'>");
      }
      out.println("</font>");

      out.println("</TD><TD VALIGN='Bottom' ALIGN='RIGHT'>");
      out.println("");
      if (!notLoggedIn) {
        showPrevNextAndHomeButtons(out, resort);
      }
      out.println("</TD></TR></table>");
      out.println("");
      out.println("<TABLE BORDER='0' CELLSPACING='0' CELLPADDING='0' WIDTH='100%'><TR><TD><img src='/images/ncclear.gif' width='3' height='3'></TD></TR></table>");
      out.println("");
//style='font-size: 10pt; face='Verdana, Arial, Helvetica'
      //make default font small for calendar shifts
      out.println("<style type='text/css'>\n");
      out.println("  .calendar td    {font-size:10pt; face:Verdana,Arial,Helvetica; padding:1px}\n");
//      out.println("  a    {target:_self}\n"); //todo srg WIP
      out.println("</style>\n");

      out.println("<TABLE class='calendar' BORDER='3' CELLSPACING='0' CELLPADDING='1' WIDTH='100%'><TR>");
      out.println("<TD WIDTH='" + wkEndWidth + "%' VALIGN='TOP' HEIGHT='15' BGCOLOR='#800000'>");
      out.println("<CENTER><B><FONT face='arial,helvetica' SIZE='2' COLOR='#FFFFFF'>Sunday</FONT></B></CENTER></TD>");
      out.println("<TD WIDTH='" + dayWidth + "%' VALIGN='TOP' HEIGHT='15' BGCOLOR='#000080'>");
      out.println("<CENTER><B><FONT  face='arial,helvetica' SIZE='2' COLOR='#FFFFFF'>Monday</FONT></B></CENTER></TD>");
      out.println("<TD WIDTH='" + dayWidth + "%' VALIGN='TOP' HEIGHT='15' BGCOLOR='#000080'>");
      out.println("<CENTER><B><FONT  face='arial,helvetica' SIZE='2' COLOR='#FFFFFF'>Tuesday</FONT></B></CENTER></TD>");
      out.println("<TD WIDTH='" + dayWidth + "%' VALIGN='TOP' HEIGHT='15' BGCOLOR='#000080'>");
      out.println("<CENTER><B><FONT  face='arial,helvetica' SIZE='2' COLOR='#FFFFFF'>Wednesday</FONT></B></CENTER></TD>");
      out.println("<TD WIDTH='" + dayWidth + "%' VALIGN='TOP' HEIGHT='15' BGCOLOR='#000080'>");
      out.println("<CENTER><B><FONT  face='arial,helvetica' SIZE='2' COLOR='#FFFFFF'>Thursday</FONT></B></CENTER></TD>");
      out.println("<TD WIDTH='" + dayWidth + "%' VALIGN='TOP' HEIGHT='15' BGCOLOR='#000080'>");
      out.println("<CENTER><B><FONT  face='arial,helvetica' SIZE='2' COLOR='#FFFFFF'>Friday</FONT></B></CENTER></TD>");
      out.println("<TD WIDTH='" + wkEndWidth + "%' VALIGN='TOP' HEIGHT='15' BGCOLOR='#800000'>");
      out.println("<CENTER><B><FONT face='arial,helvetica' SIZE='2' COLOR='#FFFFFF'>Saturday</FONT></B></CENTER></TD>");
      out.println("</TR>");
    }

    private void showPrevNextAndHomeButtons(PrintWriter out, String resort) {
      out.println("<TABLE BORDER='0' CELLSPACING='0' CELLPADDING='1' WIDTH='100%' height='38'><TR><TD VALIGN='Bottom' ALIGN='RIGHT' NOWRAP height='34'><FONT SIZE='2' FACE='Arial, Helvetica'>");
//insert page for previous button
      String szPrevHTML = "MonthCalendar?resort=" + resort + patrollerIdTag + "&month=" + prevMonth + "&year=" + prevYear;
      if (notLoggedIn) {
        szPrevHTML += "&noLogin=1";
      }
      out.println("<a target='_self' href='" + szPrevHTML + "'><IMG SRC='/images/ncvwprev.gif' BORDER='0' ALT='Previous month' ALIGN='MIDDLE' width='32' height='23'></a>");
//insert page for next button
      String szNextHTML = "MonthCalendar?resort=" + resort + patrollerIdTag + "&month=" + nextMonth + "&year=" + nextYear;
      if (notLoggedIn) {
        szNextHTML += "&noLogin=1";
      }
//    szNextHTML += idParameter;
      out.println("<a target='_self' href='" + szNextHTML + "'><IMG SRC='/images/ncvwnext.gif' BORDER='0' ALT='Next month' ALIGN='MIDDLE' width='32' height='23'></a>");
//home month button
      String szCurrHTML = "MonthCalendar?resort=" + resort + patrollerIdTag + "&month=" + realCurrMonth + "&year=" + realCurrYear;
      if (notLoggedIn) {
        szCurrHTML += "&noLogin=1";
      }
      out.println("<a href='" + szCurrHTML + "' Target='_self'><IMG SRC='/images/ncgohome.gif' BORDER='0' ALT='Return to " + Utils.szMonthsFull[realCurrMonth] + " " + realCurrYear + "' ALIGN='MIDDLE' width='32' height='32'></a>");
      out.println(" </FONT></TD></TR>");
      out.println("<TR><TD VALIGN='Bottom' ALIGN='RIGHT' height='21'>");
      out.println("");
      out.println("</TD></TR></table>");
      out.println("");
    }

    public void printEndOfPage(PrintWriter out, String resort) {
      out.println("</TABLE>");
      out.println("<TABLE BORDER='0' CELLSPACING='0' CELLPADDING='0' WIDTH='100%'><TR><TD><img src='/images/ncclear.gif' width='3' height='4'></TD></TR></table>");
      out.println("<font size=1>As of: " + trialTime);
//removed Set 13, 2015
//      out.println("&nbsp;&nbsp;");
//      out.println("<a href='javascript:printWindow()'>Print page</a>");
//
//      out.println("&nbsp;&nbsp;");

      String go = "MonthCalendar?resort=" + resort + patrollerIdTag + "&month=" + currMonth + "&year=" + currYear;

//      out.println("Font Size:<SELECT NAME='FontSize' SIZE=1  onChange='resizeMe(\"" + go + "\")' >");
//      out.println("<OPTION " + ((textFontSize == 12) ? "SELECTED" : "") + ">12");
//      out.println("<OPTION " + ((textFontSize == 11) ? "SELECTED" : "") + ">11");
//      out.println("<OPTION " + ((textFontSize == 10) ? "SELECTED" : "") + ">10");
//      out.println("<OPTION " + ((textFontSize == 9) ? "SELECTED" : "") + ">9");
//      out.println("<OPTION " + ((textFontSize == 8) ? "SELECTED" : "") + ">8");
//      out.println("</SELECT>");

//      if (denseFormat) {
//removed Set 13, 2015
//        out.println("&nbsp;&nbsp;Name Len:<SELECT NAME='textLen' SIZE=1  onChange='resizeMe('" + go + "')' >");
//        out.println("<OPTION " + ((maxNameLen == 15) ? "SELECTED" : "") + ">Condensed");
//        out.println("<OPTION " + ((maxNameLen == 20) ? "SELECTED" : "") + ">Medium");
//        out.println("<OPTION " + ((maxNameLen == 30) ? "SELECTED" : "") + ">Full");
//        out.println("</SELECT>");
//      }
//removed Set 13, 2015
//      out.println("&nbsp;&nbsp;");
//      out.println("<a href='javascript:printHelp()'>Help</a>");

//      out.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
//      out.println("<!--WEBBOT bot='HTMLMarkup' startspan ALT='Site Meter' -->");
//      out.println("<script type='text/javascript' language='JavaScript'>var site='s20SkiPatrol'</script>");
//      out.println("<script type='text/javascript' language='JavaScript1.2' src='http://s20.sitemeter.com/js/counter.js?site=s20SkiPatrol'>");
//      out.println("</script>");
//      out.println("<noscript>");
//      out.println("<a href='http://s20.sitemeter.com/stats.asp?site=s20SkiPatrol' target='_top'>");
//      out.println("<img src='http://s20.sitemeter.com/meter.asp?site=s20SkiPatrol' alt='Site Meter' border='0'/></a>");
//      out.println("</noscript>");
//      out.println("<!-- Copyright (c)2005 Site Meter -->");
//      out.println("<!--WEBBOT bot='HTMLMarkup' Endspan -->");

      out.println("</font>");
      out.println("</FORM>");
    }

    private void printCalendarDays(PrintWriter out, String resort) {
      int currDay = 2 - calendar.get(Calendar.DAY_OF_WEEK);
      int month = calendar.get(Calendar.MONTH); //0 based
      int daysInMonth = iDaysInMonth[month];
      if (month == 1) {
        //is this Februrary a leap year?
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getDefault());
        if (cal.isLeapYear(currYear)) {
          ++daysInMonth;
        }
      }
      while (currDay <= daysInMonth) {
        //start of week
        out.println("<TR>");
        for (int dayOfWeek = 0; dayOfWeek <= 6; ++dayOfWeek) {
          int wid;
          if (dayOfWeek == 0 || dayOfWeek == 6) {
            wid = wkEndWidth;
          }
          else {
            wid = dayWidth;
          }
          if (currDay > 0 && currDay <= daysInMonth) {
            printCell(out, resort, monthData, currDay, dayOfWeek, wid, month + 1);
          }
          else {
            blankCell(out, wid);
          }
          ++currDay;
        }
        out.println("</TR>");
      }
    }

    private void blankCell(PrintWriter out, int wid) {
      out.println("<TD WIDTH='" + wid + "%' BGCOLOR='#e1e1e1' VALIGN='TOP' HEIGHT='64'>&nbsp;</TD>");
    }

    @SuppressWarnings({"deprecation", "Since15"})
    private void printCell(PrintWriter out, String resort, Assignments[][] data, int day, int dayOfWeek, int wid, int currMon) {
      //output DATE
      dayOfWeek++; //convert to 1 based
      String eventName = null;
      int assignmentCount = getAssignmentCountFotTheDay(data, day);
      if (assignmentCount > 0 && data[day][0] != null) {
        eventName = data[day][0].getEventName();
      }
// System.out.println("debug: resort=" + resort + ", day=" + day + ", dayOfWeek=" + dayOfWeek + ", eventName=[" + (eventName == null ? "" : eventName) + "], assignmentCount=" + assignmentCount);
//define cell properties
      if (eventName != null && eventName.length() > 1 && !"Closed".equalsIgnoreCase(eventName)) {
        out.println("<TD WIDTH='" + wid + "%' VALIGN='TOP' HEIGHT='64' BGCOLOR='#00e1e1'>");
      }
      else {
        out.println("<TD WIDTH='" + wid + "%' VALIGN='TOP' HEIGHT='64' BGCOLOR='#FFFFFF'>");
      }

//    int indx = 0;
//build hyperlink tags
      String htmData = "ChangeShift?resort=" + resort + patrollerIdTag + "&dayOfWeek=" + (dayOfWeek - 1) + "&date=" + day +
        "&month=" + currMonth + "&year=" + currYear + "&pos=";
      String htmDisplayData = "<a target='_self' href='DayShifts?resort=" + resort + "&dayOfWeek=" + (dayOfWeek - 1) +
        "&date=" + day + "&month=" + currMonth + "&year=" + currYear + patrollerIdTag +
        "' Title='Shift Details..'><B><font face='arial,helvetica' color='#0000FF' size=4>" +
        Integer.toString(day) + "</font></B></a>";
      if (notLoggedIn) {
        htmDisplayData = "<B><font face='arial,helvetica' color='#0000FF' size=4>" + Integer.toString(day) + "</font></B>";
      }
      boolean inSeason = true;
      boolean blackoutDate = false;

      if (directorSettings.getUseBlackOut() && (dayOfWeek == 1 || dayOfWeek == 7)) {
        java.util.Date startDate = new java.util.Date(directorSettings.getBlackOutStartYear() - 1900, directorSettings.getBlackOutStartMonth() - 1, directorSettings.getBlackOutStartDay());
        java.util.Date endDate = new java.util.Date(directorSettings.getBlackOutEndYear() - 1900, directorSettings.getBlackOutEndMonth() - 1, directorSettings.getBlackOutEndDay());
        java.util.Date today = new java.util.Date(currYear - 1900, currMon - 1, day);
        if (today.getTime() >= startDate.getTime() && today.getTime() <= endDate.getTime()) {
          blackoutDate = true;
        }
      }
      if ((currMon < seasonStartMonth || (currMon == seasonStartMonth && day < seasonStartDay)) &&
        (currMon > seasonEndMonth || (currMon == seasonEndMonth && day > seasonEndDay))) {
        inSeason = false;
      }
//System.out.println("  data[day][0]=" + data[day][0] + "\n  PredefinedDayShifts[dayOfWeek][0]=" + PredefinedDayShifts[dayOfWeek][0]);
      if (data[day][0] == null) {
//no data for this day, put in a blank cells
//System.err.println("day="+day+" htmDisplayData="+htmDisplayData);
        out.println(htmDisplayData);
        if (PredefinedDayShifts[dayOfWeek][0] != null) {
          //does a shift exist on the calendar for today?
          if (!inSeason) {
            out.println("<font color='#FF0000' size='4'>&nbsp;&nbsp;CLOSED</font>");
          }
          else if ("closed".equalsIgnoreCase(eventName)) {
//System.out.println("***** Closed ***");
            out.println("<font color='#FF0000' size='4'>&nbsp;&nbsp;Closed</font>");
          }
          else if (blackoutDate) {
            out.println("<font color='#FF0000' size='4'>&nbsp;&nbsp;Not Yet Available</font>");
          }
          else {
            //
            //no data for this day, FILL OUT BLANK FORM
            //
            out.println("<TABLE WIDTH='100%' Border='0' CELLPADDING='0' CellSpacing='0'>");
            int count = 0;
            for (int i = 0; i < ShiftDefinitions.MAX; ++i) {
              ShiftDefinitions shift = PredefinedDayShifts[dayOfWeek][i];
              if (shift == null) {
                break;
              }
              for (int j = 0; j < shift.getCount() && !notLoggedIn; ++j) {
                printName(out, htmData + ((i + 1) + "&index=" + j), 0, shift.getStartString(), day, dayOfWeek, count++, i + 1, j, resort);
              }
            }
            out.println("</table>");
          }
        }
        else if (!inSeason) {
          out.println("<font color='#FF0000' size='4'>&nbsp;&nbsp;CLOSED</font>");
        }

      }
      else if (data[day][0].getPosID(0).equals("1") || "Closed".equalsIgnoreCase(eventName)) {
//special flag, we are closed this day (patroller[0] ID = 1)
        out.println(htmDisplayData);
        out.println("<font color='#FF0000' size='4'>&nbsp;&nbsp;CLOSED</font>");
        if (eventName != null && !eventName.trim().isEmpty() && !"Closed".equalsIgnoreCase(eventName)) {
          out.println("<BR><font size='2'>" + eventName + "</font>");
        }

      }
      else {
//Normal day
        if (eventName != null && !eventName.trim().isEmpty()) {
          out.println(htmDisplayData);
          out.println("<BR><font size='2'>" + eventName + "</font>");
//                out.println(eventName);
        }
        else {
          out.println(htmDisplayData);
        }
        out.println("<TABLE WIDTH='100%' Border='0' CELLPADDING='0' CellSpacing='0'>");
        int posCount = 0;
        htmData = "ChangeShift?resort=" + resort + "&dayOfWeek=" + (dayOfWeek - 1) + "&date=" + day + "&month=" + currMonth + "&year=" + currYear + patrollerIdTag + "&pos=";
        for (int i = 0; i < assignmentCount && !notLoggedIn; ++i) {
          int count = data[day][i].getCount();
          for (int j = 0; j < count; ++j) {
//                    String time = "("+data[day][i].getStartingTimeString()+") ";
            String time = data[day][i].getStartingTimeString() + " ";
            String szTmp = data[day][i].getPosID(j);   //get Assignement positions still limit 0-9
            try {
              int id = Integer.parseInt(szTmp);
              printName(out, htmData + ((i + 1) + "&index=" + j), id, time, day, dayOfWeek, posCount++, i + 1, j, resort);
            }
            catch (Exception e) {
              LOG.logException("Error, bad data for data[" + day + "][" + i + "].getPosID(" + j + ") = (" + szTmp + ")", e);
            }
          }
        }
        out.println("</table>");
      }
      out.println("</TD>");
    } //end printCell

    private int getAssignmentCountFotTheDay(Assignments[][] data, int day) {
      int assignmentCount = 0;
      for (int posIndex = 0; posIndex < ShiftDefinitions.MAX; ++posIndex) {
        if (data[day][posIndex] == null) {
          break;
        }
        ++assignmentCount;
      }
      return assignmentCount;
    }

//------------
// printName
//------------
    public void printName(PrintWriter out, String html, int id, String time, int day,
                          int dayOfWeek, int posIndex, int pos, int idx, String resort) {
      String QuickTip;
      String szName;
      boolean missed = false;
      if (id < 0) {
        missed = true;
        id = -id;
      }
      if (id != 0) {
//this removes the time prefix from the name in dense format
//          if(denseFormat)
//              time = "";
        QuickTip =names.get(new Integer(id)); //format ,"Steve Gledhill,(801) 571-7716"
        if (QuickTip == null) {
          szName = "Err id (" + id + ")";
        }
        else if (missed) {
          /*full name*/
          szName = time + "<strike>" + QuickTip.substring(0, QuickTip.lastIndexOf(',')) + "</strike>";
// first name     szName = time+"<strike>" + QuickTip.substring(0, QuickTip.indexOf(' ')) + "</strike>";
        }
        else {
          /*full name*/
          szName = time + QuickTip.substring(0, QuickTip.lastIndexOf(','));
// first name szName = time+QuickTip.substring(0, QuickTip.indexOf(' '));
        }

      }
      else {
        QuickTip = "";
        szName = time;
      }
      int testLen = maxNameLen;
      if (missed) {
        testLen += 8;
      }

      if (szName.length() > testLen) {
        szName = szName.substring(0, testLen);
      }
//this works
      String szRowStart;
      String szRowEnd;
      if (denseFormat && (dayOfWeek == 1 || dayOfWeek == 7)) {
        if (((posIndex % 2) == 0)) {
          //left side
          szRowStart = "<TR><TD>";
          szRowEnd = "</TD>";
        }
        else {
          //right side
          szRowStart = "<TD>";
          szRowEnd = "</TD></TR>";
        }
      }
      else {
//          szRowStart = "<TR><TD NOWRAP>";
        szRowStart = "<TR><TD>";
        szRowEnd = "</TD></TR>";
      }
      //check if modifying the calendar is valid
      boolean validDateToEdit = true; //hack
      //todo fix this crap
      if (resort.equals("KellyCanyon") || resort.equals("Brighton")) { //<-- this part is a hack.  put info in directors page
        if (currYear < realCurrYear) {
          validDateToEdit = false;
        }
        else if (currYear == realCurrYear && currMonth < realCurrMonth) {
          validDateToEdit = false;
        }
        else if (currYear == realCurrYear && currMonth == realCurrMonth && day < realCurrDate) {
          validDateToEdit = false;
        }
      }
      if (isDirector || (!directorSettings.getDirectorsOnlyChange() && validDateToEdit)) {
        String cellBackgroundStart = "";
        String cellBackgroundEnd = "";

//todo highlight name here ------------------------------------------
//            if (resort.equals("Brighton") && monthNewIndividualAssignments != null) {
        if (monthNewIndividualAssignments != null) {
//todo check for correct patroller. all were high lighted
          String key = NewIndividualAssignment.buildKey(currYear, currMonth + 1, day, pos, idx);
//                String key = "2009-02-07_5_0";
          NewIndividualAssignment newIndividualAssignment = monthNewIndividualAssignments.get(key);
          if (newIndividualAssignment != null) {
//System.out.print("key=" + key + ", needs replacement=" + newIndividualAssignment.getNeedsReplacement());
            int foundId = cvtToInt(newIndividualAssignment.getPatrollerId());
//Log.log(" current id=" + id + ", id from new table="+ foundId);
            if (foundId != id) {
//Log.log("ERROR with newIndividualAssignment id did not match current ID");
            }
            else if (newIndividualAssignment.getNeedsReplacement()) {
              cellBackgroundStart = "<div style='background: #FFFF00'>"; //was FFC8C8
              cellBackgroundEnd = "</div>";
            }
          }
        }
// end highlight ------------------------------------------
        out.println(szRowStart + "<a target='_self' href='" + html + "' Title='" + QuickTip + "'>" + cellBackgroundStart +
                      szName + cellBackgroundEnd + "</a>" + szRowEnd);
      }
      else {
        out.println(szRowStart + "<font face='arial,helvetica' color='#000000'>" + szName + "</font>" + szRowEnd);
      }
    }

    int cvtToInt(String strNum) {
      int num = 6; //bogus invalid font point size
      try {
        if (strNum != null) {
          num = Integer.parseInt(strNum);
        }
      }
      catch (Exception e) {
        LOG.logException("cvtToInt failed to parse: " + strNum, e);
      }
      return num;
    }

    private void debugOut(String str) {
      if (DEBUG) {
        Logger.printToLogFileStatic(sessionData.getRequest(), sessionData.getLoggedInResort(), "DEBUG-MonthCalendar(" + resort + "): " + str);
      }
    }
  }
}
