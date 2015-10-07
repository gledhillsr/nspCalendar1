package org.nsponline.calendar;


import com.mysql.jdbc.StringUtils;

import java.io.*;
import java.util.*;
import java.lang.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class ChangeShift extends HttpServlet {
  boolean DEBUG = true;

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    new LocalChangeShift(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    new LocalChangeShift(request, response);
  }

  private class LocalChangeShift {
    //------------
    // static data
//------------
    Calendar calendar1 = null;
    String[] sortedRoster;
    Hashtable<String, String> numToName = new Hashtable<String, String>();
    int rosterSize;
    HashMap<String, NewIndividualAssignment> monthNewIndividualAssignments = new HashMap<String, NewIndividualAssignment>();

    int myShiftCount;
    private String resort;
    Calendar calendar;
    String newName1 = "";
    String newName = "";
    String newIdNumber = "0";
    String szMyID = null;
    String myName;
    String myName1;
    boolean posWasEmpty = true;

    int dayOfWeek;  //0=Sunday
    int date;       //1 based
    int month;      //0 based
    int year;       //duh
    int pos;        //
    int index;      //
    int totalAssignmentGroupsForToday;
    int todayDate;      //1 based
    int todayMonth;     //0 based
    int todayYear;      //duh
    String szYear;
    String szDate;
    Assignments[] assignmentGroups;
    boolean allowEditing;
    boolean isDirector;
    int removeAccess;
    int visibleRadioButtons = 0;
    String szMonths[] = {
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    };
    String szDays[] = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "error"};
    java.util.Date currTime;
    PrintWriter out;

    private LocalChangeShift(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      response.setContentType("text/html");
      out = response.getWriter();

      SessionData sessionData = new SessionData(request.getSession(), out);
      ValidateCredentials credentials = new ValidateCredentials(sessionData, request, response, "MonthCalendar");
      if (credentials.hasInvalidCredentials()) {
        return;
      }
      resort = sessionData.getLoggedInResort();
      szMyID = sessionData.getLoggedInUserId();
      readParameterData(request);

      PatrolData patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData); //when reading members, read full data
      readData(sessionData, patrol);

      visibleRadioButtons = 0;
      OuterPage outerPage = new OuterPage(patrol.getResortInfo(), "", sessionData.getLoggedInUserId());
      outerPage.printResortHeader(out);
      printTop();
      printMiddle(patrol);
      printBottom();
      outerPage.printResortFooter(out);
      patrol.close();
    }

    private void readParameterData(HttpServletRequest request) {
      newName = "";
      newName1 = "";
      newIdNumber = "0";
      posWasEmpty = true;

      String szDay = request.getParameter("dayOfWeek"); //Sunday (=0), Monday, Tuesday, etc.
      szDate = request.getParameter("date");
      String szMonth = request.getParameter("month"); //0 based
      szYear = request.getParameter("year");
      String szPos = request.getParameter("pos");
      String szIndex = request.getParameter("index");
      debugOut("in ChangeShifts...(debug is on)");
      debugOut("  szDay=" + szDay);
      debugOut("  szDate=" + szDate);
      debugOut("  szMonth=" + szMonth);
      debugOut("  szYear=" + szYear);
      debugOut("  szPos=" + szPos);
      debugOut("  szIndex=" + szIndex);

      try {
        dayOfWeek = Integer.parseInt(szDay);// throws NumberFormatException
        date = Integer.parseInt(szDate);
        month = Integer.parseInt(szMonth);
        year = Integer.parseInt(szYear);
        pos = Integer.parseInt(szPos);
        index = Integer.parseInt(szIndex);
        calendar = new GregorianCalendar(TimeZone.getDefault());
        //noinspection MagicConstant
        calendar.set(year, month, date);
      }
      catch (NumberFormatException ex) {
        dayOfWeek = 7;
        date = 1;
        month = 1;
        year = 1;
        pos = 1;
        debugOut("ERROR, numeric processing exception, using default values");
      }   //err
    } //end readParameterDate

    public void printTop() {
//all JavaScript code
      out.println("<SCRIPT LANGUAGE=\"JavaScript\">");
//cancel button pressed
      out.println("function goHome() {");
      out.println("history.back()");
      out.println("}");

//<!--
      out.println("function autoSelectRadioBtn (index) {");
      // Skip for older (3.x) browsers, where the 'click' method
      // steals the focus, hence foiling the tab order.
      out.println("   if (navigator.appVersion.charAt(0) < '4')");
      out.println("       return;");
      out.println("   var nLen = document.form02.transaction.length;");
      // Check if only one option, select it
      out.println("   if ((nLen == null) || (typeof nLen == \"undefined\"))");
      out.println("       document.form02.transaction.click();");
      // Make sure specified (0-based) index is within range
      out.println("   else");
      out.println("   if (index < nLen)");
      out.println("       document.form02.transaction[index].click();");
      out.println("}");
//-->

      out.println("</SCRIPT>");
      out.println("<A NAME=\"TOP\"></A>");
      out.println("<table border=\"0\" CELLSPACING=\"0\" CELLPADDING=\"0\" WIDTH=\"100%\"><tr><td>");
      out.println("<font size=\"6\" COLOR=\"000000\" face=\"arial,helvetica\"><b>" + szDays[dayOfWeek] + "</b></font><BR>");
      out.println("<font face=\"arial,helvetica\" COLOR=\"000000\" size=\"4\"><B>" + szMonths[month] + " " + szDate + ", " + szYear + "</B>");
      out.println("</font></TD>");
      out.println("<td VALIGN=\"MIDDLE\" ALIGN=\"RIGHT\" NOWRAP><FONT SIZE=\"2\" FACE=\"Arial, Helvetica\">");
      out.println("<a target='_self' href=\"MonthCalendar?resort=" + resort + "&month=" + month + "&year=" + year + "\"><IMG SRC=\"images/ncgohome.gif\" BORDER=\"0\" ALT=\"Return to Volunteer Roster\" ALIGN=\"BOTTOM\" width=\"32\" height=\"32\"></a>");
      out.println("</font>");
      out.println("</td></tr>");
      out.println("</table>");

    }

    private boolean isThisPositionEmpty(String id, PatrolData patrol) {
      String name;
      name = numToName.get(id); //format ,"Steve Gledhill"
      newName = name;
      newIdNumber = id;
      MemberData md = patrol.getMemberByID(id);
      if (md != null) {
        newName1 = md.getFullName();
      }
      else {
        newName1 = newIdNumber;  //could not find name, so use number as name
      }
      return StringUtils.isNullOrEmpty(name);
    }

//------------------

    // tradeDate  //currently NOT called
//------------------
//    private void tradeDate(int idx) {
//
//        String strDate;
//        String beginStrike = "";
//        String endStrike = "";
//        String extraDate; //expand strDate
/*
 boolean oldDate = false;
 //?? if(idx == 0) oldDate=true;
 //      date = myShiftAssignments; //was (java.sql.Date)
 java.util.Date date2 = java.sql.Date.valueOf(myShiftAssignments[idx].toString());
 //      date = java.sql.Date.valueOf(myShiftAssignments[idx].toString());
 strDate = date2.toString();
 //  int[] myShiftPosition;

 if(oldDate) {
 beginStrike = "<font COLOR=\"#FF0000\"><strike>";
 endStrike = "</strike></font>";
 }

 Integer tmp = new Integer(strDate.substring(8,10));
 int day2 = tmp.intValue();
 tmp = new Integer(strDate.substring(5,7));
 int month2 = tmp.intValue();
 tmp = new Integer(strDate.substring(0,4));
 int year2 =  tmp.intValue();
 calendar1.set(year2,month2-1,day2);
 boolean ok = todayYear <= year2;
 if( ok ) {
 //trade must be same year or next year
 if(todayYear == year2 && todayMonth > month2)
 ok = false;
 if(ok) {
 //trade month is same or later
 if(todayMonth == month2 && todayDate > day2)
 ok = false;
 }
 }
 if(!ok) {
 //          beginStrike = "<font COLOR=\"#FF0000\"><strike>";
 //          endStrike = "</strike></font>";
 return;
 }
*/
//extraDate = myShiftAssignments[idx].getStartString() + " " + myShiftAssignments[idx].getEndString();
//strDate = "2001-01-01";
//      int dayOfWeek2 = calendar1.get(Calendar.DAY_OF_WEEK)-1;
//      extraDate = szDays[dayOfWeek2] +"  " + szMonths[month2-1] + " " + day2 + ", " + year2;
//        visibleRadioButtons++;
//        out.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<INPUT TYPE=RADIO NAME=\"transaction\" VALUE=\"trade_"+strDate+"\">");
//        out.println(beginStrike+extraDate+endStrike+"<br>");
//    } //end tradeDate

    private void addNames(String name) {
      int i;
      for (i = 0; i < rosterSize; ++i) {
        if (name != null && name.equals(sortedRoster[i])) {
          out.println("<option selected>" + name);
        }
        else {
          out.println("<option>" + sortedRoster[i]);
        }
      }
    } //addnames

    private void findIfPositionWasEmpty(PatrolData patrol) {
      for (int assignmentGroup = 0; assignmentGroup < totalAssignmentGroupsForToday; ++assignmentGroup) {
        for (int offsetWithinGroup = 0; offsetWithinGroup < assignmentGroups[assignmentGroup].getCount(); ++offsetWithinGroup) {
          String id = assignmentGroups[assignmentGroup].getPosID(offsetWithinGroup);
          if (id.charAt(0) == '-') { // missedShift
            id = id.substring(1);
          }
          if ((assignmentGroup + 1) == pos && offsetWithinGroup == index) {
            debugOut("look for patroller at assignment group: " + (assignmentGroup + 1) + ", at offset: " + assignmentGroup);
            posWasEmpty = isThisPositionEmpty(id, patrol);
          }
        }
      }
    }

    //------------
// printMiddle (submitterID, transaction, selectedID, date1, pos1, listName)
//------------
    @SuppressWarnings("deprecation")
    private void printMiddle(PatrolData patrol) {
//        int i;
// print small date view
      findIfPositionWasEmpty(patrol);
//      out.println("</table>");
//      out.println("<HR>");    //Horziontal Rule

//start of form
//      out.println("<FORM METHOD=\"POST\" action=\"../../_derived/nortbots.htm\" onSubmit=\"location.href='../../_derived/nortbots.htm';return false;\" webbot-action=\"--WEBBOT-SELF--\" WEBBOT-onSubmit>");
//      out.println("<!--webbot bot=\"SaveResults\" startspan U-File=\"../formrslt.htm\"");
//      out.println("S-Format=\"HTML/DL\" B-Label-Fields=\"TRUE\" --><input TYPE=\"hidden\" NAME=\"VTI-GROUP\" VALUE=\"0\"><!--webbot");
//      out.println("bot=\"SaveResults\" endspan i-checksum=\"43374\" -->");

      String nextURL = "ProcessChanges";
      out.print("<form target='_self' action=" + nextURL + " method=POST id=form02 name=form02>\n");
      out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"resort\" VALUE=\"" + resort + "\">\n");
      out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"ID\" VALUE=\"" + szMyID + "\">\n");

//start of selection table
      out.println("<table border=\"1\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">");
      debugOut("  newName=" + newName);
      debugOut("  myName=" + myName);
      debugOut("  posWasEmpty=" + posWasEmpty);
      debugOut("  allowEditing=" + allowEditing);
      boolean editingMyself = (newName != null && newName.equals(myName));
      debugOut("  editingMyself=" + editingMyself);
      if (allowEditing && myName != null) {
//==INSERT== only used if position is empty
//            if(editingMyself) {
//                //do nothing for insert/replace
//          } else
        if (posWasEmpty) {
          out.println("  <tr>");
          visibleRadioButtons++;
          out.println("    <td width=\"100%\" colspan=\"2\"><INPUT TYPE=RADIO NAME=\"transaction\" VALUE=\"insertMyName\" CHECKED>");
          out.println("      <b>Insert</b> myself (" + myName1 + ").&nbsp; ");
          out.println("    </td>");
          out.println("  </tr>");
        }
        //==REPLACE== used if position is NOT empty
        else if (!editingMyself) {
          out.println("  <tr>");
          visibleRadioButtons++;
          out.println("    <td width=\"100%\" colspan=\"2\"><INPUT TYPE=RADIO NAME=\"transaction\" VALUE=\"ReplaceWithMyName\" CHECKED>");
          out.println("      <b>Replace</b> &quot;" + newName1 + "&quot; with myself (" + myName1 + ").&nbsp; No");
          out.println("      exchange to be done</td>");
          out.println("  </tr>");
        }
      }
//==TRADE== used if position is NOT empty
//        if(allowEditing) {
//          if (!posWasEmpty && !editingMyself && myShiftCount > 0) {
//              out.println("  <tr>");
//              out.println("    <td width=\"100%\" colspan=\"2\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b>Trade</b> days with &quot;"+newName+"&quot; (The following days assigned to "+myName+")<br>");
//////              SortDates();
////hack
////                for(i=0; i < myShiftCount; ++i)
////                    tradeDate(i);
//out.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-----");
//out.println("One step 'Shift Trading' is temporarly disabled.  Normally all of <b>your</b> shifts are displayed here.");
//              out.println(" </td> </tr>");
//          }
//        }

//==REPLACE/INSERT== some one else
      out.println("  <tr>");
      String isChecked = "";
      if (!allowEditing) {
        isChecked = " CHECKED ";
      }
      visibleRadioButtons++;
      out.println("    <td width=\"50%\"><INPUT TYPE=RADIO NAME=\"transaction\" VALUE=\"replaceWithSomeoneElse\" " + isChecked + ">");
      if (!posWasEmpty) {
        out.println("      <b>Replace</b> (" + newName1 + ") with someone else&nbsp;</td>");
      }
      else {
        out.println("      <b>Insert</b> someone else&nbsp;</td>");
      }
      out.println("    <td width=\"50%\"><SELECT NAME=\"listName\" SIZE=10 onclick=autoSelectRadioBtn(" + (visibleRadioButtons - 1) + ")>");
      addNames(myName);
      out.println("</SELECT>");
      out.println("    </td>");
      out.println("  </tr>");
//==REMOVE== only is position is NOT empty

//get today's date in days (Julian date)
      long calendarDay = (new java.util.Date(year - 1900, month, date)).getTime() / 1000 / 3600 / 24;    //get time in days
      java.util.Date removeDate = new java.util.Date();
      long firstRemoveDay = (new java.util.Date(removeDate.getYear(), removeDate.getMonth(), removeDate.getDate())).getTime() / 1000 / 3600 / 24;
      firstRemoveDay += removeAccess;
      boolean validRemoveRange = (firstRemoveDay < calendarDay) || removeAccess == 0;

      if (!posWasEmpty && ((allowEditing && validRemoveRange) || isDirector)) {
        out.println("  <tr>");
        out.println("    <td width=\"100%\" colspan=\"2\">");
        visibleRadioButtons++;
        out.println("<INPUT TYPE=RADIO NAME=\"transaction\" VALUE=\"removeName\" CHECKED>");
        out.println("      <b>Remove</b> name (" + newName1 + ")</td>");
        out.println("  </tr>");
      }

//==MISSED SHIFT== used if position is NOT empty
      if (!posWasEmpty && isDirector) {
        out.println("  <tr> <td width=\"100%\" colspan=\"2\">");
        visibleRadioButtons++;
        out.println("<INPUT TYPE=RADIO NAME=\"transaction\" VALUE=\"missedShift\">");
        out.println("      <b>Missed Shift</b> (" + newName1 + ")");
        out.println("  </td> </tr>");
      }
      boolean wasMarkedAsNeedingReplacement = false;
      if (monthNewIndividualAssignments != null) {
        String key = NewIndividualAssignment.buildKey(year, month + 1, date, pos, index);
        if (monthNewIndividualAssignments.containsKey(key)) {
          //key found
          NewIndividualAssignment newIndividualAssignment = monthNewIndividualAssignments.get(key);
          wasMarkedAsNeedingReplacement = newIndividualAssignment.getNeedsReplacement();
        }
      }
//    if(resort.equals("Brighton")) {
      if (wasMarkedAsNeedingReplacement) {
        out.println("  <tr> <td width=\"100%\" colspan=\"2\">");
        visibleRadioButtons++;
        out.println("<INPUT TYPE=RADIO NAME=\"transaction\" VALUE=\"noReplacementNeeded\">");
        out.println("      <b>Remove highlight</b> from -Needs a Replacement-  (" + newName1 + ") ");
        out.println("  </td> </tr>");
      }
      else if (!posWasEmpty) {
        out.println("  <tr> <td width=\"100%\" colspan=\"2\">");
        visibleRadioButtons++;
        out.println("<INPUT TYPE=RADIO NAME=\"transaction\" VALUE=\"needsReplacement\">");
        out.println("      <b>Highlight</b> as -Needs a Replacement-");
        out.println("  </td> </tr>");
      }
//    }
    }

    private void printBottom() {
      out.println("</table>");
//??
      out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"submitterID\" VALUE=\"" + szMyID + "\">");
      out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"pos1\" VALUE=\"" + pos + "\">");
      out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"index1\" VALUE=\"" + index + "\">");

//Integer idNum = (Integer)NameToNum.get(newName);
//String selectedID = null;
//try {
//  selectedID = idNum.toString();
//} catch (Exception e) { }
//String    selectedID = Integer.toString(newIdNumber);
      out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"selectedID\" VALUE=\"" + newIdNumber + "\">");
      String strDate = year + "-";
      if (month + 1 < 10) {
        strDate += "0";
      }
      strDate += (month + 1) + "-";
      if (date < 10) {
        strDate += "0";
      }
      strDate += date;
      strDate += "_" + PatrolData.IndexToString(pos);   //in the data base, pos is 1 based

      out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"date1\" VALUE=\"" + strDate + "\">");

      out.println("<p align=\"center\">");
      out.println("<INPUT TYPE=SUBMIT VALUE=\"Submit\">");
      out.println("<INPUT TYPE=\"button\" VALUE=\"Cancel\" onClick=\"goHome()\">");
      out.println("</FORM>");
      out.println("<HR>");    //Horizontal Rule
      out.println("<H5>" + PatrolData.getResortFullName(resort) + " Ski Resort</H5>");
    } //end printBottom()

    /**
     * *
     * //for this date, loop through all shifts, and all assignments for this shift
     * //  int month;      //0 based
     * //  int year;       //duh
     * //  int pos;        //
     * //  int index;      //
     * //existing shift
     * Assignments[] assignments;
     * Assignments data;
     * PatrolData patrol = new PatrolData(true,resort); //when reading members, read full data
     * patrol.resetAssignments();
     * int y,m,d,pos = 0;
     * while((data = patrol.readNextAssignment()) != null) {
     * y = data.getYear();
     * m = data.getMonth();
     * d = data.getDay();
     * if(year == y && month == m && date == d)
     * assignments[pos++] = data;
     * }
     * int assignmentCount = 0;
     * for(i=0; i < Assignments.MAX_ASSIGNMENT_SIZE; ++i) {
     * if(assignments[i] != null )
     * ++assignmentCount;
     * else
     * break;
     * }
     * patrol.close();
     * <p/>
     * <p/>
     * **
     */
//------------------
// readData
//------------------
    public void readData(SessionData sessionData, PatrolData patrol) {
//        String last, first;
      String idNum;
      int y, m, d;
      DirectorSettings ds;

      MemberData member1 = patrol.getMemberByID(szMyID);
      myName = member1.getFullName();
      myName1 = member1.getFullName();

      ds = patrol.readDirectorSettings();
      removeAccess = ds.getRemoveAccess();
      allowEditing = !ds.getDirectorsOnlyChange() || member1.isDirector();
      patrol.resetAssignments();
      patrol.resetRoster();
      sortedRoster = new String[300];
      rosterSize = 0;
      MemberData member;
      isDirector = szMyID.equalsIgnoreCase(sessionData.getBackDoorUser());

      while ((member = patrol.nextMember("")) != null) {
        if (member.getID().equals(szMyID)) {
          isDirector = member.isDirector();
        }
        idNum = member.getID();
        sortedRoster[rosterSize] = member.getFullName_lastNameFirst();
        numToName.put(idNum, sortedRoster[rosterSize]);
        rosterSize++;
      }

      // create a GregorianCalendar with the Pacific Daylight time zone
      // and the current date and time
      calendar1 = new GregorianCalendar(TimeZone.getDefault());
      currTime = new java.util.Date();

      calendar1.setTime(currTime);
      todayYear = calendar1.get(Calendar.YEAR);
      todayMonth = calendar1.get(Calendar.MONTH) + 1;  //MONTH is 1 based
      todayDate = calendar1.get(Calendar.DATE);
//        if(resort.equals("Brighton")) {
      //todo working on this..  read all new assignments for this date, and put into hashmap
      monthNewIndividualAssignments = patrol.readNewIndividualAssignments(year, month + 1, date); //entire day
//        }

//------------------------------------------------
// read Shift ski assignments for the specific date
//------------------------------------------------
//    myShiftAssignments = new Shifts[300]; //assume never more than 300 per season
      //save all of MY assignments
      patrol.resetAssignments();
      totalAssignmentGroupsForToday = 0;
//        String lastPos = " ";
      assignmentGroups = new Assignments[50];
      Assignments data;
      myShiftCount = 0;
//    assignmentCount = 0;
      while ((data = patrol.readNextAssignment()) != null) {
        y = data.getYear();
        m = data.getMonth() - 1; //make it 0 based
        d = data.getDay();
        if (year == y && month == m && date == d) {
          assignmentGroups[totalAssignmentGroupsForToday++] = data;
        }
        //now loop through all assignments for that shift
        // to get all of my shifts on any date except today
//        for(int j = 0; j < data.getCount(); ++j) {
//            if(year == y && month == m && date == d)
//                continue;   //don't add today
////            assignmentCount++;
//            //keep track of ALL of my assignments (for replace)
//            if(szMyID.equals(data.getPosID(j))) {
//                String theDate="(date goes here)";
//                String theTime=(data.getStartingTimeString() + " - " + data.getEndingTimeString());
//                myShiftAssignments[myShiftCount++] = new Shifts(" ",theDate,theTime, j);
//            }
//        }
      } //end while Shift ski assignments
    } //end of readdata

    private void debugOut(String msg) {
      if (DEBUG) {
        System.out.println("DEBUG-ChangeShift(" + resort + "): " + msg);
      }
    }
  }
} //end class ChangeShift
