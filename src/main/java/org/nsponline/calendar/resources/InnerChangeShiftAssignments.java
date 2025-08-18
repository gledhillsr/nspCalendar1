package org.nsponline.calendar.resources;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.nsponline.calendar.store.*;
import org.nsponline.calendar.utils.*;

import static com.amazonaws.util.StringUtils.isNullOrEmpty;

public class InnerChangeShiftAssignments extends ResourceBase  {
  final private HttpServletResponse response;

  private final static String LIMIT_DAY_SHIFTS_FOR_RESORT = "Brightonzz";  //todo Brighton or Sample
  private final static int MAX_DAY_SHIFTS_PER_MONTH = 30;
  private final static List<Integer> LIMITED_MONTH_MAX = new ArrayList<>(Arrays.asList(
      MAX_DAY_SHIFTS_PER_MONTH,  //Jan (0 based months)
      MAX_DAY_SHIFTS_PER_MONTH,  //Feb
      MAX_DAY_SHIFTS_PER_MONTH,  //Mar
      MAX_DAY_SHIFTS_PER_MONTH,  //Apr
      MAX_DAY_SHIFTS_PER_MONTH,  //May
      MAX_DAY_SHIFTS_PER_MONTH,  //Jun
      MAX_DAY_SHIFTS_PER_MONTH,  //Jul
      MAX_DAY_SHIFTS_PER_MONTH,  //Aug
      MAX_DAY_SHIFTS_PER_MONTH,  //Sep
      MAX_DAY_SHIFTS_PER_MONTH,  //Oct
      MAX_DAY_SHIFTS_PER_MONTH,  //Nov
      MAX_DAY_SHIFTS_PER_MONTH));  //Dec

  /**
   * From the calendar, clicked on a specific shift
   * Modify a single specific shift yyyy/mm/dd/shiftIndex/positionIndex to insert/modify/delete the assignment for that shift
   * GET resort=Brighton,  dayOfWeek=4,  date=4,  month=1,  year=2021,  ID=192443,  pos=1 (1 based),  index=0
   * then POST's updates to ProcessChangeShiftAssignments, or return to MonthCalendar
   *
   * @author Steve Gledhill
   */
  public InnerChangeShiftAssignments(HttpServletRequest request, HttpServletResponse response, Logger LOG) throws IOException {
    super(request, response, LOG);
    this.response = response;
  }

  public void runner(String parentClassName) {
    if (!initBaseAndAskForValidCredentials(response, parentClassName)) {
      return;
    }

    Parameters parameters = new Parameters(request);

    ShiftInfo shiftInfo = readData(sessionData, patrolData, parameters);

    printCommonHeader();
    printTop(out, parameters, resort);
    int visibleButtons = printMiddle(out, patrolData, shiftInfo.loggedInUserId, parameters, shiftInfo);
    printBottom(out, shiftInfo.loggedInUserId, parameters, shiftInfo, visibleButtons);
    printCommonFooter();
    patrolData.close();
  }

  public void printTop(PrintWriter out, Parameters parameters, String resort) {
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

    out.println("</SCRIPT>");
    out.println("<A NAME=\"TOP\"></A>");
    out.println("<table border=\"0\" CELLSPACING=\"0\" CELLPADDING=\"0\" WIDTH=\"100%\"><tr><td>");
    out.println("<font size=\"6\" COLOR=\"000000\" face=\"arial,helvetica\"><b>" + StaticUtils.szDays[parameters.dayOfWeek] + "</b></font><BR>");
    out.println("<font face=\"arial,helvetica\" COLOR=\"000000\" size=\"4\"><B>" + StaticUtils.szMonthsFull[parameters.month] + " " + parameters.dayOfMonth + ", " + parameters.year + "</B>");
    out.println("</font></TD>");
    out.println("<td VALIGN=\"MIDDLE\" ALIGN=\"RIGHT\" NOWRAP><FONT SIZE=\"2\" FACE=\"Arial, Helvetica\">");
    out.println("<a target='_self' href=\"MonthCalendar?resort=" + resort + "&month=" + parameters.month + "&year=" + parameters.year + "\"><IMG SRC=\"/images/ncgohome.gif\" BORDER=\"0\" ALT=\"Return to Volunteer Roster\" ALIGN=\"BOTTOM\" width=\"32\" height=\"32\"></a>");
    out.println("</font>");
    out.println("</td></tr>");
    out.println("</table>");
  }

  private boolean isThisPositionEmpty(String id, PatrolData patrol, ShiftInfo shiftInfo) {
    String name = shiftInfo.numToName.get(id); //format ,"Steve Gledhill"
    shiftInfo.newName = name;
    shiftInfo.newIdNumber = id;
    if ("0".equals(id)) {
      shiftInfo.newName1 = shiftInfo.newIdNumber;  //could not find name, so use number as name
      return true;
    }
    Roster md = patrol.getMemberByID(id);
    if (md != null) {
      shiftInfo.newName1 = md.getFullName();
    } else {
      shiftInfo.newName1 = shiftInfo.newIdNumber;  //could not find name, so use number as name
    }
    return isNullOrEmpty(name);
  }

  private void addNames(PrintWriter out, String name, ShiftInfo shiftInfo) {
    int i;
    for (i = 0; i < shiftInfo.rosterSize; ++i) {
      if (name != null && name.equals(shiftInfo.sortedRoster[i])) {
        out.println("<option selected>" + name);
      } else {
        out.println("<option>" + shiftInfo.sortedRoster[i]);
      }
    }
  }

  private boolean findIfPositionWasEmpty(Parameters parameters, PatrolData patrol, ShiftInfo shiftInfo) {
    boolean posWasEmpty = true;
    for (int assignmentGroup = 0; assignmentGroup < shiftInfo.totalAssignmentGroupsForToday; ++assignmentGroup) {
      for (int offsetWithinGroup = 0; offsetWithinGroup < shiftInfo.assignmentGroups[assignmentGroup].getCount(); ++offsetWithinGroup) {
        String id = shiftInfo.assignmentGroups[assignmentGroup].getPosID(offsetWithinGroup);
        if (id.charAt(0) == '-') { // missedShift
          id = id.substring(1);
        }
        if ((assignmentGroup + 1) == parameters.pos && offsetWithinGroup == parameters.index) {
          LOG.debug("look for patroller at assignment group: " + (assignmentGroup + 1) + ", at offset: " + assignmentGroup);
          posWasEmpty = isThisPositionEmpty(id, patrol, shiftInfo);
        }
      }
    }
    return posWasEmpty;
  }

  private int printMiddle(PrintWriter out, PatrolData patrol, String loggedInUserId, Parameters parameters, ShiftInfo shiftInfo) {
    int visibleRadioButtons = 0;

    boolean posWasEmpty = findIfPositionWasEmpty(parameters, patrol, shiftInfo);

    String nextURL = "ProcessChangeShiftAssignments";
    out.print("<form target='_self' action=" + nextURL + " method=POST id=form02 name=form02>\n");
    out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"resort\" VALUE=\"" + resort + "\">\n");
    out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"ID\" VALUE=\"" + loggedInUserId + "\">\n");

    //start of selection table
    out.println("<table border=\"1\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">");

//note newName=Shorts, Jim, newName1=Jim Shorts, myName=Jane Doe,
    boolean editingMyself = shiftInfo.newName1 != null && shiftInfo.newName1.equals(shiftInfo.myName);
    AtomicInteger myShiftsThisMonth = new AtomicInteger();
    boolean outsideEarlyLimit = shouldLimitAndInsertIsMaxedOut(loggedInUserId, parameters, patrol, myShiftsThisMonth);
    LOG.info("CALLING ProcessChangeShiftAssignments with: loggedInUserId=" + loggedInUserId
                  + ", newName=" + shiftInfo.newName
                  + ", newName1=" + shiftInfo.newName1
                  + ", myName=" + shiftInfo.myName
                  + ", posWasEmpty=" + posWasEmpty
                  + ", allowEditing=" + shiftInfo.allowEditing
                  + ", editingMyself=" + editingMyself
                  + ", outsideEarlyLimit=" + outsideEarlyLimit);
    if (shiftInfo.allowEditing && shiftInfo.myName != null) {
      //==INSERT== only used if position is empty
      if (outsideEarlyLimit && posWasEmpty && !isNightShift(patrol, parameters)) { //should be Brighton ONLY
        int maxLimitedShiftsThisMonth = LIMITED_MONTH_MAX.get(parameters.month);
        out.println("  <tr>");
        out.println("    <td width=\"100%\" colspan=\"2\">Sorry, outside <b>early</b> limit of " + maxLimitedShiftsThisMonth + " day/swing shifts per month");
        out.println("    </td>");
        out.println("  </tr>");
      }
      else if (posWasEmpty) {
        out.println("  <tr>");
        visibleRadioButtons++;
        out.println("    <td width=\"100%\" colspan=\"2\"><INPUT TYPE=RADIO NAME=\"transaction\" VALUE=\"insertMyName\" CHECKED>");
        out.println("      <b>Insert</b> myself (" + shiftInfo.myName + ").&nbsp; ");
        out.println("    </td>");
        out.println("  </tr>");
      }
      //==REPLACE== used if position is NOT empty
      else if (!editingMyself && !outsideEarlyLimit) {
        out.println("  <tr>");
        visibleRadioButtons++;
        out.println("    <td width=\"100%\" colspan=\"2\"><INPUT TYPE=RADIO NAME=\"transaction\" VALUE=\"ReplaceWithMyName\" CHECKED>");
        out.println("      <b>Replace</b> &quot;" + shiftInfo.newName1 + "&quot; with myself (" + shiftInfo.myName + ").&nbsp; No");
        out.println("      exchange to be done</td>");
        out.println("  </tr>");
      }
    }
    //==REPLACE/INSERT== some one else
    if (!outsideEarlyLimit) {
      out.println("  <tr>");
      String isChecked = "";
      if (!shiftInfo.allowEditing) {
        isChecked = " CHECKED ";
      }
      visibleRadioButtons++;
      out.println("    <td width=\"50%\"><INPUT TYPE=RADIO NAME=\"transaction\" VALUE=\"replaceWithSomeoneElse\" " + isChecked + ">");
      if (!posWasEmpty) {
        out.println("      <b>Replace</b> (" + shiftInfo.newName1 + ") with someone else&nbsp;</td>");
      }
      else {
        out.println("      <b>Insert</b> someone else&nbsp;</td>");
      }
      out.println("    <td width=\"50%\"><SELECT NAME=\"listName\" SIZE=10 onclick=autoSelectRadioBtn(" + (visibleRadioButtons - 1) + ")>");
      addNames(out, shiftInfo.myName, shiftInfo);
      out.println("</SELECT>");
      out.println("    </td>");
      out.println("  </tr>");
    }
    //==REMOVE== only is position is NOT empty
    //get today's date in days (Julian date)
    long calendarDay = (new java.util.Date(parameters.year - 1900, parameters.month, parameters.dayOfMonth)).getTime() / 1000 / 3600 / 24;    //get time in days
    java.util.Date removeDate = new java.util.Date();
    long firstRemoveDay = (new java.util.Date(removeDate.getYear(), removeDate.getMonth(), removeDate.getDate())).getTime() / 1000 / 3600 / 24;
    firstRemoveDay += shiftInfo.removeAccess;
    boolean validRemoveRange = (firstRemoveDay < calendarDay) || shiftInfo.removeAccess == 0;

    if (!posWasEmpty && ((shiftInfo.allowEditing && validRemoveRange) || shiftInfo.isDirector)) {
      out.println("  <tr>");
      out.println("    <td width=\"100%\" colspan=\"2\">");
      visibleRadioButtons++;
      out.println("<INPUT TYPE=RADIO NAME=\"transaction\" VALUE=\"removeName\" CHECKED>");
      out.println("      <b>Remove</b> name (" + shiftInfo.newName1 + ")</td>");
      out.println("  </tr>");
    }

    //==MISSED SHIFT== used if position is NOT empty
    if (!posWasEmpty && shiftInfo.isDirector) {
      out.println("  <tr> <td width=\"100%\" colspan=\"2\">");
      visibleRadioButtons++;
      out.println("<INPUT TYPE=RADIO NAME=\"transaction\" VALUE=\"missedShift\">");
      out.println("      <b>Missed Shift</b> (" + shiftInfo.newName1 + ")");
      out.println("  </td> </tr>");
    }
    boolean wasMarkedAsNeedingReplacement = false;
    HashMap<String, NewIndividualAssignment> monthNewIndividualAssignments = patrol.readNewIndividualAssignments(parameters.year, parameters.month + 1, parameters.dayOfMonth); //entire day
    String key = NewIndividualAssignment.buildKey(parameters.year, parameters.month + 1, parameters.dayOfMonth, parameters.pos, parameters.index);
    //key in format yyyy-mm-dd_idx_p
    if (monthNewIndividualAssignments.containsKey(key)) {
      //key found
      NewIndividualAssignment newIndividualAssignment = monthNewIndividualAssignments.get(key);
      wasMarkedAsNeedingReplacement = newIndividualAssignment.getNeedsReplacement();
    }
    if (wasMarkedAsNeedingReplacement) {
      out.println("  <tr> <td width=\"100%\" colspan=\"2\">");
      visibleRadioButtons++;
      out.println("<INPUT TYPE=RADIO NAME=\"transaction\" VALUE=\"noReplacementNeeded\">");
      out.println("      <b>Remove highlight</b> from -Needs a Replacement-  (" + shiftInfo.newName1 + ") ");
      out.println("  </td> </tr>");
    } else if (!posWasEmpty) {
      out.println("  <tr> <td width=\"100%\" colspan=\"2\">");
      visibleRadioButtons++;
      out.println("<INPUT TYPE=RADIO NAME=\"transaction\" VALUE=\"needsReplacement\">");
      out.println("      <b>Highlight</b> as -Needs a Replacement-");
      out.println("  </td> </tr>");
    }
    return visibleRadioButtons;
  }

  private boolean isNightShift(PatrolData patrol, Parameters parameters) {
/*
Parameters - args passed in
    int dayOfWeek;  //0=Sunday
    int dayOfMonth; //1 based
    int month;      //0 based
    int year;       //duh
    int pos;        //1 based line defining each shift definition withing the day
    int index;      //0 based offset for each shift's patrollerId array
Assignments - DB row represents each unique shift on each unique day, and will have 1-10 patrollers assigned to it.
 *  Assignments     Date         StartTime EndTime EventName      ShiftType Count P0  P1  P2 P3 P4 P5 P6 P7 P8 P9
 *                  2025-01-01_1  6         21:30  New Year's Day   2        3    123 456 789  (FIRST ROW)
 *                  2025-01-01_2  6:30      21:30  New Year's Day   2        1    543          (SECOND ROW)
ShiftDefinitions use if there is no assignment on that date
 * ShiftDefinitions    EventName     StartTime    EndTime    Count    ShiftType
 *                     Saturday_0      08:00        16:30      8        0  (day)
 *                     Saturday_1      14:00        21:30      3        2  (night)
 *                     OEC Final_0     18:00        20:00      10       3  (training)
*/
    //note the values passes in are used in a SQL query
    ArrayList<Assignments> shiftAssignments = patrol.readSortedAssignments(parameters.year, parameters.month + 1, parameters.dayOfMonth);
    if (shiftAssignments.isEmpty()) {
      //eventName values like: "Saturday_0" or "Saturday_1"
      String eventName =  StaticUtils.szDays[parameters.dayOfWeek] + "_" + (parameters.pos - 1);
      for (ShiftDefinitions shiftDefinition : patrol.readShiftDefinitions()) {
        String parsedName = shiftDefinition.getEventName();
        if (eventName.equals(parsedName)) {
          return shiftDefinition.getType() == 2; //night shift
        }
      }

        return false;
    }
    if (shiftAssignments.size() < parameters.pos || parameters.pos  < 1) {
      LOG.error("isNightShift should NEVER happen.. shiftAssignments.size=" + shiftAssignments.size() + ", parameters.pos=" + parameters.pos);
      return false;
    }
    Assignments assignment = shiftAssignments.get(parameters.pos - 1);
    return assignment.isNightShift();
  }

  private boolean shouldLimitAndInsertIsMaxedOut(String loggedInUserId, Parameters parameters, PatrolData patrol, AtomicInteger myShiftsThisMonth) {
    //only limit for Brighton resort
    if (!shouldLimitDays()) {
      return false;
    }

   //git shift assignments this month
    myShiftsThisMonth.set(getDaySwingShiftCount(loggedInUserId, parameters, patrol));
    Integer maxDaysForTheMonth = LIMITED_MONTH_MAX.get(parameters.month);
    //compute isMaxedOut
    return myShiftsThisMonth.get() >= maxDaysForTheMonth;
  }

  private boolean shouldLimitDays() {
    if (LIMIT_DAY_SHIFTS_FOR_RESORT.equals(resort)) {
      return true;
    }
    return false;
  }

  private int getDaySwingShiftCount(String loggedInUserId, Parameters parameters, PatrolData patrol) {
    int foundCount = 0;
    // The "monthAssignments" represents each unique shift on each unique day, and will have 1-10 patrollers assigned to it.
    ArrayList<Assignments> monthAssignments = patrol.readSortedAssignments(parameters.year, parameters.month + 1);
    for(Assignments assignment : monthAssignments) {
      //within each shift, loop through each patroller assigned to that shift
      boolean dayOrSwingShift = assignment.isDayShift() || assignment.isSwingShift();
      if (assignment.getPosIndex(loggedInUserId) >= 0 && dayOrSwingShift) { // was this patroller assigned to this shift?
//        LOG.info("ZZZ getDaySwingShiftCount, shift's found this month for user=" + loggedInUserId + ", foundCount=" + foundCount + ", dayOrSwingShift=" + dayOrSwingShift);
        ++foundCount;
      }
    }
    return foundCount;
  }

  private void printBottom(PrintWriter out, String loggedInUserId, Parameters parameters, ShiftInfo shiftInfo, int visibleButtons) {
    out.println("</table>");
    out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"submitterID\" VALUE=\"" + loggedInUserId + "\">");
    out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"pos1\" VALUE=\"" + parameters.pos + "\">");
    out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"index1\" VALUE=\"" + parameters.index + "\">");
    out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"selectedID\" VALUE=\"" + shiftInfo.newIdNumber + "\">");
    LOG.debug("printBottom, submitterID=");
    String strDate = parameters.buildDateAndPosString();   //in the data base, pos is 1 based

    out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"date1\" VALUE=\"" + strDate + "\">");

    out.println("<p align=\"center\">");
    if (visibleButtons > 0) {
      out.println("<INPUT TYPE=SUBMIT VALUE=\"Submit\">");
    }
    out.println("<INPUT TYPE=\"button\" VALUE=\"Cancel\" onClick=\"goHome()\">");
    out.println("</FORM>");
    out.println("<HR>");    //Horizontal Rule
    out.println("<H5>" + PatrolData.getResortFullName(resort, LOG) + " Ski Resort</H5>");
  } //end printBottom()

  public ShiftInfo readData(SessionData sessionData, PatrolData patrol, Parameters parameters) {
    String loggedInUserId = sessionData.getLoggedInUserId();

    return new ShiftInfo(loggedInUserId, patrol, parameters);
  }

  private class Parameters {
    int dayOfWeek;  //0=Sunday
    int dayOfMonth; //1 based
    int month;      //0 based
    int year;       //duh
    int pos;        //1 based line defining each shift definition withing the day
    int index;      //0 based offset for each shift's patrollerId array

    Parameters(HttpServletRequest request) {
      String szDayOfWeek = request.getParameter("dayOfWeek"); //Sunday (=0), Monday, Tuesday, etc.
      String szDate = request.getParameter("date");
      String szMonth = request.getParameter("month"); //0 based
      String szYear = request.getParameter("year");
      String szPos = request.getParameter("pos");
      String szIndex = request.getParameter("index");

      try {
        dayOfWeek = Integer.parseInt(szDayOfWeek);// throws NumberFormatException
        dayOfMonth = Integer.parseInt(szDate);
        month = Integer.parseInt(szMonth);
        year = Integer.parseInt(szYear);
        pos = Integer.parseInt(szPos);     //1 based line defining each shift definition withing the day
        index = Integer.parseInt(szIndex); //0 based offset for each shift's row
        LOG.debug("dayOfWeek=" + dayOfWeek + ", year=" + year + ", month(0-based)=" + month + ", date=" + dayOfMonth + ",  pos=" + pos + ", index=" + index);
      } catch (NumberFormatException ex) {
        dayOfWeek = 7;
        dayOfMonth = 1;
        month = 1;
        year = 2024;
        pos = 1;
        LOG.debug("ERROR, numeric processing exception, using default values");
      }   //err
    } //end readParameterDate

    String buildDateAndPosString() {
      String strDate = year + "-";
      if (month + 1 < 10) {
        strDate += "0";
      }
      strDate += (month + 1) + "-";
      if (dayOfMonth < 10) {
        strDate += "0";
      }
      strDate += dayOfMonth;
      strDate += "_" + PatrolData.IndexToString(pos);   //in the data base, pos is 1 based
      return strDate;
    }
  } //end Parameters inner class

  private class ShiftInfo {
    String newName1;
    String newName;
    String newIdNumber;
    String loggedInUserId;
    Roster loggedInMember;
    String myName;
    boolean allowEditing;
    boolean isDirector;
    int removeAccess;
    String[] sortedRoster;
    int rosterSize;
    int totalAssignmentGroupsForToday;
    Assignments[] assignmentGroups;
    Hashtable<String, String> numToName;


    ShiftInfo(String loggedInUserId, PatrolData patrol, Parameters parameters) {
      this.loggedInUserId = loggedInUserId;
      loggedInMember = patrol.getMemberByID(loggedInUserId);
      newName = "";
      newName1 = "";
      newIdNumber = "0";

      myName = loggedInMember.getFullName();
      DirectorSettings ds = patrol.readDirectorSettings();
      removeAccess = ds.getRemoveAccess();
      allowEditing = !ds.getDirectorsOnlyChange() || loggedInMember.isDirector();
      sortedRoster = new String[300];
      rosterSize = 0;
      isDirector = loggedInUserId.equalsIgnoreCase(sessionData.getBackDoorUser());

      numToName = new Hashtable<>();
      ResultSet rosterResults = patrol.resetRoster();
      Roster nextMember;
      while ((nextMember = patrol.nextMember("", rosterResults)) != null) {
        if (nextMember.getID().equals(loggedInUserId)) {
          isDirector = nextMember.isDirector();
        }
        String idNum = nextMember.getID();
        sortedRoster[rosterSize] = nextMember.getFullName_lastNameFirst();
        numToName.put(idNum, sortedRoster[rosterSize]);
        rosterSize++;
      }

      // create a GregorianCalendar with the Pacific Daylight time zone
      // and the current date and time
      final Calendar calendarToday = new GregorianCalendar(TimeZone.getDefault());
      final Date currTime = new Date();

      calendarToday.setTime(currTime);

      totalAssignmentGroupsForToday = 0;
      assignmentGroups = new Assignments[200];
      for (Assignments shiftAssignments : patrol.readSortedAssignments(parameters.year, parameters.month + 1, parameters.dayOfMonth)) {
        LOG.debug("readData-assignmentGroups[" + totalAssignmentGroupsForToday + "]=" + shiftAssignments);
        assignmentGroups[totalAssignmentGroupsForToday++] = shiftAssignments;
      } //end while Shift ski assignments

    }
  } //end ShiftInfo inner class

} //end ChangeShiftAssignments class