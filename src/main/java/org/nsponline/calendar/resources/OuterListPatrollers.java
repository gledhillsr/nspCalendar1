package org.nsponline.calendar.resources;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.nsponline.calendar.store.Assignments;
import org.nsponline.calendar.store.DirectorSettings;
import org.nsponline.calendar.store.Roster;
import org.nsponline.calendar.utils.Logger;
import org.nsponline.calendar.utils.PatrolData;
import org.nsponline.calendar.utils.SessionData;

public class OuterListPatrollers extends ResourceBase {
  private final static boolean DEBUG = false;

  private String szMyID;
  private boolean isDirector = false;
  //    String ePatrollerList = "";
  private DirectorSettings directorSettings;
  private Vector<String> classificationsToDisplay = null;
  private int commitmentToDisplay = 0;
  private boolean listDirector = false;
  private boolean listAll = false;
  private int instructorFlags = 0;
  private PatrolData patrol = null;
  //  int memberIndex = 0;
  private int patrollersListed = 0;
  private int textFontSize = 14;
  private Vector<Roster> members;
  private int maxShiftCount;
  private int StartDay;
  private int StartMonth;
  private int StartYear;
  private int EndDay;
  private int EndMonth;
  private int EndYear;
  private boolean useMinDays;
  private int minDays;

  private boolean showClass;
  private boolean showID;
  private boolean showBlank;
  private boolean showBlankWide;
  private boolean firstNameFirst;
  private boolean showSpouse;
  private boolean showAddr;
  private boolean showCity;
  private boolean showState;
  private boolean showZip;
  private boolean showHome;
  private boolean showWork;
  private boolean showCell;
  private boolean showPager;
  private boolean showEmail;
  private boolean showEmergency;
  private boolean showSubstitute;
  private boolean showCommit;
  private boolean showInstructor;
  private boolean showDirector;
  private boolean showLastUpdated;
  private boolean showComments;
  //    boolean showOldCredits;
  private boolean showCreditDate;
  private boolean showNightCnt;
  private boolean showDayCnt;
  private boolean showSwingCnt;
  private boolean showTrainingCnt;
  private boolean showOtherCnt;
  private boolean showHolidayCnt;
  private boolean showNightList;
  private boolean showDayList;
  private boolean showSwingList;
  private boolean showTrainingList;
  private boolean showOtherList;
  private boolean showHolidayList;
  private boolean showTeamLead;
  private boolean showMentoring;
  private boolean showCreditsEarned;
  //    boolean showCreditsUsed;
  private boolean showCanEarnCredits;
  private String sort1;
  private String sort2;
  private String sort3;

  final private HttpServletResponse response;

  OuterListPatrollers(HttpServletRequest request, HttpServletResponse response, Logger LOG) throws IOException {
    super(request, response, LOG);
    this.response = response;
  }

  public void runner(String parentClassName) {
    if (!initBaseAndAskForValidCredentials(response, parentClassName)) {
      return;
    }

    directorSettings = null;
    szMyID = sessionData.getLoggedInUserId();
    readData(request, szMyID, sessionData);
    patrollersListed = 0;

    printCommonHeader();
    printTop();
    printBody();
    printBottom();
    printCommonFooter();
  }


  private void dumpParameters() {
    debugOut("Sort1=" + sort1);
    debugOut("Sort2=" + sort2);
    debugOut("Sort3=" + sort3);

    debugOut("showClass=" + showClass);
    debugOut("showID=" + showID);
    debugOut("showBlank=" + showBlank);
    debugOut("showBlankWide=" + showBlankWide);
    debugOut("showSpouse=" + showSpouse);
    debugOut("showAddr=" + showAddr);
    debugOut("showCity=" + showCity);
    debugOut("showState=" + showState);
    debugOut("showZip=" + showZip);
    debugOut("showHome=" + showHome);
    debugOut("showWork=" + showWork);
    debugOut("showCell=" + showCell);
    debugOut("showPager=" + showPager);
    debugOut("showEmail=" + showEmail);
    debugOut("showEmergency=" + showEmergency);
    debugOut("showCommit=" + showCommit);
    debugOut("showInstructor=" + showInstructor);
    debugOut("showDirector=" + showDirector);
    debugOut("showLastUpdated=" + showLastUpdated);
    debugOut("showComments=" + showComments);
    debugOut("showCreditDate=" + showCreditDate);
    debugOut("showNightCnt=" + showNightCnt);
    debugOut("showDayCnt=" + showDayCnt);
    debugOut("showSwingCnt=" + showSwingCnt);
    debugOut("showTrainingCnt=" + showTrainingCnt);
    debugOut("showOtherCnt=" + showOtherCnt);
    debugOut("showHolidayCnt=" + showHolidayCnt);
    debugOut("showNightList=" + showNightList);
    debugOut("showDayList=" + showDayList);
    debugOut("showSwingList=" + showSwingList);
    debugOut("showTrainingList=" + showTrainingList);
    debugOut("showOtherList=" + showOtherList);
    debugOut("showHolidayList=" + showHolidayList);
    debugOut("useMinDays=" + useMinDays);
    debugOut("MinDays=" + minDays);
  }

  private void readData(HttpServletRequest request, String IDOfEditor, SessionData sessionData) {
    firstNameFirst = true;
    String szName = request.getParameter("NAME");
    if (szName != null && szName.equals("LAST")) {
      firstNameFirst = false;
    }
    showClass = request.getParameter("CLASS") != null;
    showID = request.getParameter("SHOW_ID") != null;
    showBlank = request.getParameter("SHOW_BLANK") != null;
    showBlankWide = request.getParameter("SHOW_BLANK2") != null;
    showSpouse = request.getParameter("SPOUSE") != null;
    showAddr = request.getParameter("ADDR") != null;
    showCity = request.getParameter("CITY") != null;
    showState = request.getParameter("STATE") != null;
    showZip = request.getParameter("ZIP") != null;
    showHome = request.getParameter("HOME") != null;
    showWork = request.getParameter("WORK") != null;
    showCell = request.getParameter("CELL") != null;
    showPager = request.getParameter("PAGER") != null;
    showEmail = request.getParameter("EMAIL") != null;
    showEmergency = request.getParameter("EMERGENCY") != null;
    showSubstitute = request.getParameter("SUBSTITUTE") != null;
    showCommit = request.getParameter("COMMIT") != null;
    showInstructor = request.getParameter("INSTRUCTOR") != null;
    showDirector = request.getParameter("DIRECTOR") != null;
    showLastUpdated = request.getParameter("LAST_UPDATED") != null;
    showComments = request.getParameter("COMMENTS") != null;
    //    showOldCredits    = request.getParameter("CARRY_OVER_CREDITS") != null;
    showCreditDate = request.getParameter("LAST_CREDIT_UPDATE") != null;
    showNightCnt = request.getParameter("NIGHT_CNT") != null;
    showDayCnt = request.getParameter("DAY_CNT") != null;
    showSwingCnt = request.getParameter("SWING_CNT") != null;
    showTrainingCnt = request.getParameter("TRAINING_CNT") != null;
    showOtherCnt = request.getParameter("OTHER_CNT") != null;
    showHolidayCnt = request.getParameter("HOLIDAY_CNT") != null;

    showNightList = request.getParameter("NIGHT_DETAILS") != null;
    showSwingList = request.getParameter("SWING_DETAILS") != null;
    showDayList = request.getParameter("DAY_DETAILS") != null;
    showTrainingList = request.getParameter("TRAINING_DETAILS") != null;
    showOtherList = request.getParameter("OTHER_DETAILS") != null;
    showHolidayList = request.getParameter("HOLIDAY_DETAILS") != null;
    showTeamLead = request.getParameter("TEAM_LEAD") != null;
    showMentoring = request.getParameter("MENTORING") != null;
    showCreditsEarned = request.getParameter("CREDITS_EARNED") != null;
    //    showCreditsUsed     = request.getParameter("CREDITS_USED") != null;
    showCanEarnCredits = request.getParameter("CAN_EARN_CREDITS") != null;

    StartDay = cvtToInt(request.getParameter("StartDay"));
    StartMonth = cvtToInt(request.getParameter("StartMonth"));
    StartYear = cvtToInt(request.getParameter("StartYear"));
    EndDay = cvtToInt(request.getParameter("EndDay"));
    EndMonth = cvtToInt(request.getParameter("EndMonth"));
    EndYear = cvtToInt(request.getParameter("EndYear"));
    useMinDays = request.getParameter("MIN_DAYS") != null;
    minDays = cvtToInt(request.getParameter("MinDays"));


    sort1 = request.getParameter("FirstSort");
    sort2 = request.getParameter("SecondSort");
    sort3 = request.getParameter("ThirdSort");

    textFontSize = cvtToInt(request.getParameter("FontSize"));
    if (DEBUG) {
      dumpParameters();
    }
    String[] incList = {"BAS", "INA", "SR", "SRA", "ALM", "PRO", "AUX", "TRA", "CAN", "OTH"};
    classificationsToDisplay = new Vector<String>();
    commitmentToDisplay = 0;
    //classification
    for (String classification : incList) {
      String str = request.getParameter(classification);
      if (str != null) {
        classificationsToDisplay.add(classification);
      }
    }
    //commitment
    if (request.getParameter("FullTime") != null) {
      commitmentToDisplay += 4;
    }
    if (request.getParameter("PartTime") != null) {
      commitmentToDisplay += 2;
    }
    if (request.getParameter("Inactive") != null) {
      commitmentToDisplay += 1;
    }

    //instructor/director flags
    listDirector = false;
    listAll = false;
    instructorFlags = 0;
    if (request.getParameter("ALL") != null) {
      listAll = true;
    }
    if (request.getParameter("ListDirector") != null) {
      listDirector = true;
    }
    if (request.getParameter("OEC") != null) {
      instructorFlags += 1;
    }
    if (request.getParameter("CPR") != null) {
      instructorFlags += 2;
    }
    if (request.getParameter("Ski") != null) {
      instructorFlags += 4;
    }
    if (request.getParameter("Toboggan") != null) {
      instructorFlags += 8;
    }

    patrol = new PatrolData(resort, sessionData, LOG);

    //read assignments within a range and get shift count
    readAssignments(patrol); //must read for other code to work


    directorSettings = patrol.readDirectorSettings();
    //zz      String sortString = getSortString();
    //zz      patrol.resetRoster(sortString);
    //        ePatrollerList = "";
    //        MemberData member = patrol.nextMember("&nbsp;");
    ////      MemberData member = patrol.nextMember("");
    ////int xx=0;
    //        while (member != null) {
    //            if (member.okToDisplay(false, false, listAll, classificationsToDisplay, commitmentToDisplay, listDirector, instructorFlags, 0)) {
    //                String em = member.getEmailAddress();
    //                //check for valid email
    //                if (em != null && em.length() > 6 && em.indexOf('@') > 0 && em.indexOf('.') > 0) {
    //                    if (ePatrollerList.length() > 2)
    //                        ePatrollerList += ",";
    //                    ePatrollerList += em;
    //                }
    //            }
    //            member = patrol.nextMember("");
    //        }
    Roster editor = patrol.getMemberByID(IDOfEditor); //ID from cookie
    //      patrol.close(); //must close connection!
    isDirector = editor != null && editor.isDirector();
  }

  /**
   * showButtonsAtTop
   */
  private void showButtonsAtTop(String resort) {
    out.println("<p><font size=\"3\"><Bold>");
    String options = "";
    //classification
    for (int j = 0; j < classificationsToDisplay.size(); ++j) {
      //noinspection StringConcatenationInLoop
      options += "&" + classificationsToDisplay.elementAt(j) + "=1";
    }

    //commitment
    if ((commitmentToDisplay & 4) == 4) {
      options += "&FullTime=1";
    }
    if ((commitmentToDisplay & 2) == 2) {
      options += "&PartTime=1";
    }
    if ((commitmentToDisplay & 1) == 1) {
      options += "&Inactive=1";
    }
    //Instructor
    if (listAll) {
      options += "&ALL=1";
    }
    if (listDirector) {
      options += "&ListDirector=1";
    }
    if ((instructorFlags & 1) == 1) {
      options += "&OEC=1";
    }
    if ((instructorFlags & 2) == 2) {
      options += "&CPR=1";
    }
    if ((instructorFlags & 4) == 4) {
      options += "&Ski=1";
    }
    if ((instructorFlags & 8) == 8) {
      options += "&Toboggan=1";
    }
    // day count (1/0), swing count (1/0), night count (1/0), Minimum Shifts (#)
    //fix me ????
    if (useMinDays && (showDayCnt || showSwingCnt || showNightCnt)) {
      options += "&MIN_DAYS=1&MinDays=" + minDays;
      if (showDayCnt) {
        options += "&DAY_CNT=1";
      }
      if (showNightCnt) {
        options += "&NIGHT_CNT=1";
      }
      if (showSwingCnt) {
        options += "&SWING_CNT=1";
      }
      if (showTrainingCnt) {
        options += "&TRAINING_CNT=1";
      }
      if (showOtherCnt) {
        options += "&OTHER_CNT=1";
      }
      if (showHolidayCnt) {
        options += "&HOLIDAY_CNT=1";
      }
      options += "&StartDay=" + StartDay;
      options += "&StartMonth=" + StartMonth;
      options += "&StartYear=" + StartYear;
      options += "&EndDay=" + EndDay;
      options += "&EndMonth=" + EndMonth;
      options += "&EndYear=" + EndYear;

    }
    String loc = "EmailForm?resort=" + resort + "&ID=" + szMyID + options;
    if (isDirector || directorSettings.getEmailAll()) {
      out.println("<INPUT TYPE=\"button\" VALUE=\"e-mail THESE patrollers\" onClick=window.location=\"" + loc + "\">");
      out.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
    }
    //      out.println("<a href=\"javascript:printWindow()\">Print This Page</a></font>");

    //DownloadThisTable
    //todo mostly removed button 2/3/2021 (
    if (isDirector && "192443".equals(szMyID)) {
      out.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
      //options += "&format=palm";
      options += "&format=Excel";
      loc = "DownloadRoster?resort=" + resort + "&ID=" + szMyID + options;
      out.println("<INPUT  TYPE=\"button\" VALUE=\"Steve, Download table (under construction)\" onClick=window.location=\"" + loc + "\">");
    }
    out.println("</p>");
  }

  public void printTop() {

    //        int headerFontSize = textFontSize+2; //adjust me ?
//todo removed 2/27/21
//    out.println("<style type=\"text/css\">");
//    out.println("<!-- ");
//    out.println("body  {font-size:10pt; color: #000000; background-color: #ffffff}");
//    out.println(".list table {border-width:1px; border-color:#000000; border-style:solid; border-collapse:collapse; border-spacing:0}");
//    out.println(".list th    {font-size:" + textFontSize + "pt; font-weight: bold; color: #000000; background-color: #ffffff; border-width:1px; border-color:#000000; border-style:solid; padding:2px}");
//    out.println(".list td    {font-size:" + textFontSize + "pt; color: #000000; background-color: #ffffff; border-width:1px; border-color:#000000; border-style:solid; padding:1px}");
//    out.println("//-->");
//    out.println("</style>");

    out.println("<script>");
    out.println("function printWindow(){");
    out.println("   bV = parseInt(navigator.appVersion)");
    out.println("   if (bV >= 4) window.print()");
    out.println("}");
    out.println("</script>");

    out.println("<p><Center><h2>Members of the " + PatrolData.getResortFullName(resort, LOG) + " Ski Patrol</h2></Center></p>");

    LOG.info("ListPatrollers, isDirector=" + isDirector + " ID: " + szMyID + " directorSettings=" + directorSettings);
    if (isDirector || (directorSettings != null && directorSettings.getEmailAll())) {
      showButtonsAtTop(resort);
    } //end email patrollers...
    out.println("<table class='list' style=\"font-size: 10pt; face='Verdana, Arial, Helvetica' \" border=\"1\" width=\"99%\" bordercolordark=\"#003366\" bordercolorlight=\"#C0C0C0\">");
    out.println(" <tr>");

    Roster.addColumn(-1);
    Roster.addColumn(firstNameFirst ? Roster.FIRST : Roster.LAST);
    if (showBlank) {
      Roster.addColumn(Roster.BLANK);
    }
    if (showBlankWide) {
      Roster.addColumn(Roster.BLANK_WIDE);
    }
    if (showClass) {
      Roster.addColumn(Roster.CLASSIFICATION);
    }
    if (showID) {
      Roster.addColumn(Roster.ID_NUM);
    }
    if (showSpouse) {
      Roster.addColumn(Roster.SPOUSE);
    }
    if (showAddr) {
      Roster.addColumn(Roster.ADDRESS);
    }
    if (showCity) {
      Roster.addColumn(Roster.CITY);
    }
    if (showState) {
      Roster.addColumn(Roster.STATE);
    }
    if (showZip) {
      Roster.addColumn(Roster.ZIPCODE);
    }
    if (showHome) {
      Roster.addColumn(Roster.HOMEPHONE);
    }
    if (showWork) {
      Roster.addColumn(Roster.WORKPHONE);
    }
    if (showCell) {
      Roster.addColumn(Roster.CELLPHONE);
    }
    if (showPager) {
      Roster.addColumn(Roster.PAGER);
    }
    if (showEmail) {
      Roster.addColumn(Roster.EMAIL);
    }
    if (showEmergency) {
      Roster.addColumn(Roster.EMERGENCY);
    }
    if (showSubstitute) {
      Roster.addColumn(Roster.SUB);
    }
    if (showCommit) {
      Roster.addColumn(Roster.COMMITMENT);
    }
    if (showInstructor) {
      Roster.addColumn(Roster.INSTRUCTOR);
    }
    if (showDirector) {
      Roster.addColumn(Roster.DIRECTOR);
    }
    if (showLastUpdated) {
      Roster.addColumn(Roster.LAST_UPDATED);
    }
    if (showComments) {
      Roster.addColumn(Roster.COMMENTS);
    }
    if (showCanEarnCredits) {
      Roster.addColumn(Roster.CAN_EARN_CREDITS);
    }
    //        if(showOldCredits)  MemberData.addColumn(MemberData.CARRY_OVER_CREDITS);
    if (showCreditsEarned) {
      Roster.addColumn(Roster.CREDITS_EARNED);
    }
    //        if(showCreditsUsed)     MemberData.addColumn(MemberData.CREDITS_USED);
    if (showCreditDate) {
      Roster.addColumn(Roster.LAST_CREDIT_UPDATE);
    }
    if (showTeamLead) {
      Roster.addColumn(Roster.TEAM_LEAD);
    }
    if (showMentoring) {
      Roster.addColumn(Roster.MENTORING);
    }

    if (showDayCnt) {
      Roster.addColumn(Roster.SHOW_DAY_CNT);
    }
    if (showDayList) {
      Roster.addColumn(Roster.SHOW_DAY_LIST);
    }
    if (showSwingCnt) {
      Roster.addColumn(Roster.SHOW_SWING_CNT);
    }
    if (showSwingList) {
      Roster.addColumn(Roster.SHOW_SWING_LIST);
    }
    if (showNightCnt) {
      Roster.addColumn(Roster.SHOW_NIGHT_CNT);
    }
    if (showNightList) {
      Roster.addColumn(Roster.SHOW_NIGHT_LIST);
    }
    if (showTrainingCnt) {
      Roster.addColumn(Roster.SHOW_TRAINING_CNT);
    }
    if (showTrainingList) {
      Roster.addColumn(Roster.SHOW_TRAINING_LIST);
    }
    if (showOtherCnt) {
      Roster.addColumn(Roster.SHOW_OTHER_CNT);
    }
    if (showOtherList) {
      Roster.addColumn(Roster.SHOW_OTHER_LIST);
    }
    if (showHolidayCnt) {
      Roster.addColumn(Roster.SHOW_HOLIDAY_CNT);
    }
    if (showHolidayList) {
      Roster.addColumn(Roster.SHOW_HOLIDAY_LIST);
    }

    Roster.printMemberListRowHeading(out, resort);
    out.println(" </tr>");
  }

  /**
   * printBottom
   */
  private void printBottom() {
    out.println("</table>");
    out.println("Total Patrollers Listed=" + patrollersListed);

    out.println("<br>As of: " + new java.util.Date());
  }

  String getSortString() {
    String sortString;
    if (sort1 == null || sort1.equals("Name") || sort1.equals("shiftCnt")) {
      sortString = firstNameFirst ? "FirstName,LastName" : "LastName,FirstName";
    }
    else if (sort1.equals("Class")) {
      sortString = "ClassificationCode";
    }
    else if (sort1.equals("Comm")) {
      sortString = "Commitment";
    }
    else if (sort1.equals("Updt")) {
      sortString = "lastUpdated";
    }
    else {
      sortString = "FirstName,LastName";
    }  //should not get hit

    //noinspection StatementWithEmptyBody
    if (sort2 == null) {
      //dont add anything
    }
    else if ("Name".equals(sort2) && !"shiftCnt".equals(sort1)) {
      sortString += firstNameFirst ? ",FirstName,LastName" : ",LastName,FirstName";
    }
    else if (sort2.equals("Class")) {
      sortString += ",ClassificationCode";
    }
    else if (sort2.equals("Comm")) {
      sortString += ",Commitment";
    }
    //  else if(Sort1.equals("shiftCnt"))
    //      sortString += "";
    //  else if(Sort1.equals("DCnt"))
    //      sortString += "";
    else if (sort2.equals("Updt")) {
      sortString += ",lastUpdated";
    }

    //noinspection StatementWithEmptyBody
    if (sort3 == null) {
      //dont add anything
    }
    else if (sort3.equals("Name")) {
      sortString += firstNameFirst ? ",FirstName,LastName" : ",LastName,FirstName";
    }
    else if (sort3.equals("Class")) {
      sortString += ",ClassificationCode";
    }
    else if (sort3.equals("Comm")) {
      sortString += ",Commitment";
    }
    //  else if(Sort1.equals("shiftCnt"))
    //      sortString += "";
    //  else if(Sort1.equals("DCnt"))
    //      sortString += "";
    else if (sort3.equals("Updt")) {
      sortString += ",lastUpdated";
    }
    return sortString;
  }

  /**
   * printBody
   */
  public void printBody() {
    //    String sortString = getSortString();
    //  patrol.resetRoster(sortString);

    //  MemberData member = patrol.nextMember("&nbsp;");
    Roster member;
    if ("shiftCnt".equals(sort1)) {
      if (useMinDays) {
        maxShiftCount = minDays - 1;
      }
      else {
        maxShiftCount = 40; //todo this is a HACK
      }
      TreeMap<String, Roster> treeMap = new TreeMap<String, Roster>(Collections.reverseOrder());
      for (int memberIndex = 0; memberIndex < members.size(); memberIndex++) { //loop through all members
        member = members.elementAt(memberIndex);
        Integer totalAssignments = member.AssignmentCount[Assignments.DAY_TYPE] +
          member.AssignmentCount[Assignments.SWING_TYPE] +
          member.AssignmentCount[Assignments.TRAINING_TYPE] +
          member.AssignmentCount[Assignments.NIGHT_TYPE] +
          member.AssignmentCount[Assignments.OTHER_TYPE] +
          member.AssignmentCount[Assignments.HOLIDAY_TYPE];
        String key = String.format("%04d-%d", totalAssignments, memberIndex);
        treeMap.put(key, member);
      }
      for (String key : treeMap.keySet()) {
        String cnt = key.substring(0, 4);
        String idx = key.substring(5);
        String cnt1 = cnt.replaceFirst("^0+(?!$)", "");
        int currentAssignmentCount = new Integer(cnt1);
        int memberIndex = new Integer(idx);
        member = members.elementAt(memberIndex);
        if (!useMinDays || currentAssignmentCount <= maxShiftCount) {
          member.printMemberListRowData(out);
          patrollersListed++;
        }
      }

      //      for (int currentAssignmentCount = maxShiftCount; currentAssignmentCount >= 0; --currentAssignmentCount) {    //loop from highest shift total back to 0
      //        for (int memberIndex = 0; memberIndex < members.size(); memberIndex++) { //loop through all members
      //          member = members.elementAt(memberIndex);
      //          int totalAssignments = member.AssignmentCount[Assignments.DAY_TYPE] +
      //              member.AssignmentCount[Assignments.SWING_TYPE] +
      //              member.AssignmentCount[Assignments.TRAINING_TYPE] +
      //              member.AssignmentCount[Assignments.NIGHT_TYPE];
      //          if (totalAssignments == currentAssignmentCount) {
      //            member.printMemberListRowData(out);
      //            patrollersListed++;
      //          }
      //        }
      //      }
    }
    else {
      int memberIndex = 0;
      while (memberIndex < members.size()) {
        member = members.elementAt(memberIndex);
        int totalAssignments = member.AssignmentCount[Assignments.DAY_TYPE] +
          member.AssignmentCount[Assignments.SWING_TYPE] +
          member.AssignmentCount[Assignments.TRAINING_TYPE] +
          member.AssignmentCount[Assignments.NIGHT_TYPE] +
          member.AssignmentCount[Assignments.OTHER_TYPE] +
          member.AssignmentCount[Assignments.HOLIDAY_TYPE];
        if (!useMinDays || totalAssignments < minDays) {
          member.printMemberListRowData(out);
          patrollersListed++;
        }
        memberIndex++;
      }
    }
    patrol.close(); //must close connection!
  } //end printBody


  /**
   * readAssignments
   *
   * @param patrol patrolData
   */
  void readAssignments(PatrolData patrol) {
    Assignments ns;
    int i;
    String sortString = getSortString();
    ResultSet rosterResults = patrol.resetRoster(sortString);
    //      patrol.resetRoster();
    Roster member;

    maxShiftCount = 0;
    members = new Vector<Roster>(PatrolData.MAX_PATROLLERS);
    final Hashtable<String, Roster> hash = new Hashtable<String, Roster>();
    //int xx = 0;
    while ((member = patrol.nextMember("&nbsp;", rosterResults)) != null) {
      if (member.okToDisplay(false, false, listAll, classificationsToDisplay, commitmentToDisplay, listDirector, instructorFlags, 0)) {
        //              ++count;
        members.addElement(member);
        hash.put(member.getID(), member);
      }
    }

    ResultSet assignmentResults = patrol.resetAssignments();
    //        SimpleDateFormat normalDateFormatter = new SimpleDateFormat ("MM'/'dd'/'yyyy");
    GregorianCalendar date = new GregorianCalendar(StartYear, StartMonth, StartDay);
    long startMillis = 0;
    long endMillis = 99999999999999L;
    long currMillis;
    if (StartYear != 0) {
      startMillis = date.getTimeInMillis();
    }
    date = new GregorianCalendar(EndYear, EndMonth, EndDay);
    if (EndYear != 0) {
      endMillis = date.getTimeInMillis();
    }

    while ((ns = patrol.readNextAssignment(assignmentResults)) != null) {
      //noinspection MagicConstant
      date = new GregorianCalendar(ns.getYear(), ns.getMonth(), ns.getDay());
      currMillis = date.getTimeInMillis();
      if (startMillis <= currMillis && currMillis <= endMillis) {
        for (i = 0; i < Assignments.MAX_ASSIGNMENT_SIZE; ++i) {
          //              member = patrol.getMemberByID(ns.getPosID(i));
          member = hash.get(ns.getPosID(i));
          if (member != null && member.okToDisplay(false, false, listAll, classificationsToDisplay, commitmentToDisplay, listDirector, instructorFlags, 0)) {
            //                        String tim = ns.getStartingTimeString();
            if (showDayCnt && ns.isDayShift()) {
              ++member.AssignmentCount[Assignments.DAY_TYPE];
              if (maxShiftCount < member.AssignmentCount[Assignments.DAY_TYPE]) {
                maxShiftCount = member.AssignmentCount[Assignments.DAY_TYPE];
              }
              member.szAssignments[Assignments.DAY_TYPE] += ns.getMyFormattedDate() + " ";
            }
            if (showSwingCnt && ns.isSwingShift()) {
              ++member.AssignmentCount[Assignments.SWING_TYPE];
              if (maxShiftCount < member.AssignmentCount[Assignments.SWING_TYPE]) {
                maxShiftCount = member.AssignmentCount[Assignments.SWING_TYPE];
              }
              member.szAssignments[Assignments.SWING_TYPE] += ns.getMyFormattedDate() + " ";
            }
            if (showNightCnt && ns.isNightShift()) {
              ++member.AssignmentCount[Assignments.NIGHT_TYPE];
              if (maxShiftCount < member.AssignmentCount[Assignments.NIGHT_TYPE]) {
                maxShiftCount = member.AssignmentCount[Assignments.NIGHT_TYPE];
              }
              member.szAssignments[Assignments.NIGHT_TYPE] += ns.getMyFormattedDate() + " ";
            }
            if (showTrainingCnt && ns.isTrainingShift()) {
              ++member.AssignmentCount[Assignments.TRAINING_TYPE];
              if (maxShiftCount < member.AssignmentCount[Assignments.TRAINING_TYPE]) {
                maxShiftCount = member.AssignmentCount[Assignments.TRAINING_TYPE];
              }
              member.szAssignments[Assignments.TRAINING_TYPE] += ns.getMyFormattedDate() + " ";
            }
            if (showOtherCnt && ns.isOtherShift()) {
              ++member.AssignmentCount[Assignments.OTHER_TYPE];
              if (maxShiftCount < member.AssignmentCount[Assignments.OTHER_TYPE]) {
                maxShiftCount = member.AssignmentCount[Assignments.OTHER_TYPE];
              }
              member.szAssignments[Assignments.OTHER_TYPE] += ns.getMyFormattedDate() + " ";
            }
            if (showHolidayCnt && ns.isHolidayShift()) {
              ++member.AssignmentCount[Assignments.HOLIDAY_TYPE];
              if (maxShiftCount < member.AssignmentCount[Assignments.HOLIDAY_TYPE]) {
                maxShiftCount = member.AssignmentCount[Assignments.HOLIDAY_TYPE];
              }
              member.szAssignments[Assignments.HOLIDAY_TYPE] += ns.getMyFormattedDate() + " ";
            }
          } //end if okToDisplay
        } //end for loop for shift
      }
    } //end while loop (all assignments)
  }

  int cvtToInt(String strNum) {
    int num = 0;
    try {
      if (strNum != null) {
        num = Integer.parseInt(strNum);
      }
    }
    catch (Exception e) {
      //don't know what to do..
    }
    return num;
  }

  private void debugOut(String msg) {
    if (DEBUG) {
      LOG.info( "DEBUG-ListPatrollers(): " + msg);
    }
  }
}