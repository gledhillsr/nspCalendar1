package org.nsponline.calendar;

import org.nsponline.calendar.utils.*;
import org.nsponline.calendar.store.Assignments;
import org.nsponline.calendar.store.DirectorSettings;
import org.nsponline.calendar.store.Roster;

import java.io.*;
import java.sql.ResultSet;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.lang.*;

public class DirectorDownload extends HttpServlet {
  private static final int MIN_LOG_LEVEL = Logger.DEBUG;

  private Logger LOG;

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    LOG = new Logger(DirectorDownload.class, request, "GET", null, MIN_LOG_LEVEL);
    LOG.logRequestParameters();
    new LocalDownload(request, response);
  }

  private class LocalDownload {
    boolean debug = true;	//-----------
  //    ResourceBundle rb = ResourceBundle.getBundle("LocalStrings");
  PrintWriter out;
  boolean isExcel;
  boolean isPalm;
  boolean isPalmOutput;
  private String resort;
  String szMyID;

//  boolean isDirector = false;
  String ePatrollerList = "";
  private DirectorSettings ds;
  Vector classificationsToDisplay = null;
  int commitmentToDisplay = 0;
  boolean listDirector = false;
  boolean listAll = false;
  int instructorFlags = 0;
//  PatrolData patrol = null;
  int totalCount = 0;
  int actualCount = 0;
  int textFontSize = 14;
  Hashtable hash;
  Vector members;
  int maxShiftCount;
  int StartDay;
  int StartMonth;
  int StartYear;
  int EndDay;
  int EndMonth;
  int EndYear;
  boolean useMinDays;
  int MinDays;


  boolean showClass;
  boolean showID;
  boolean showBlank;
  boolean showBlankWide;
  boolean firstNameFirst;
  boolean showSpouse;
  boolean showAddr;
  boolean showCity;
  boolean showState;
  boolean showZip;
  boolean showHome;
  boolean showWork;
  boolean showCell;
  boolean showPager;
  boolean showEmail;
  boolean showEmergency;
//  boolean showSubstitute;
  boolean showCommit;
  boolean showInstructor;
  boolean showDirector;
  boolean showLastUpdated;
  boolean showComments;
  //    boolean showOldCredits;
  boolean showCreditDate;
  boolean showNightCnt;
  boolean showDayCnt;
  boolean showSwingCnt;
  boolean showTrainingCnt;
  boolean showNightList;
  boolean showDayList;
  boolean showSwingList;
  boolean showTrainingList;
//  boolean showTeamLead;
//  boolean showMentoring;
//  boolean showCreditsEarned;
  //    boolean showCreditsUsed;
  boolean showCanEarnCredits;
  String Sort1;
  String Sort2;
  String Sort3;

    private LocalDownload(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      response.setContentType("text/html");
      out = response.getWriter();
      SessionData sessionData = new SessionData(request, out, LOG);
      ValidateCredentialsRedirectIfNeeded credentials = new ValidateCredentialsRedirectIfNeeded(sessionData, request, response, "MonthCalendar", LOG);
      if (credentials.hasInvalidCredentials()) {
        return;
      }
      resort = request.getParameter("resort");
      szMyID = sessionData.getLoggedInUserId();
      readData(request, szMyID, sessionData);

      actualCount = 0;

      String format = request.getParameter("format");
      isExcel = (format != null && format.equals("Excel"));
      isPalm = (format != null && format.equals("palm"));
      isPalmOutput = (format != null && format.equals("PalmOutput"));

//        response.setContentType("text/html");
      out = response.getWriter();

      if(isExcel) {
          response.setContentType("application/vnd.ms-excel");
//Log.log("hack hack in download");  //this makes no difference in the download file
//                response.setContentType("application/txt.ms-excel");
          excelOutput(sessionData);
      }
      else if (isPalm) {
        response.setContentType("text/html");
        printTop();
        printBody();
        printBottom();
      }
//      else if (isPalmOutput) {
//        response.setContentType("application/application.txt");
////          response.setHeader( "Content-Disposition ", "inline; filename=roster.cvs");
//        palmOutput();
//      }
  }

  public void readData(HttpServletRequest request,String IDOfEditor, SessionData sessionData) {
    firstNameFirst  = true;
    String szName = request.getParameter("NAME");
    if(szName != null && szName.equals("LAST")) {
      firstNameFirst = false;
    }
    showClass       = request.getParameter("CLASS") != null;
    showID          = request.getParameter("SHOW_ID") != null;
    showBlank       = request.getParameter("SHOW_BLANK") != null;
    showBlankWide   = request.getParameter("SHOW_BLANK2") != null;
    showSpouse      = request.getParameter("SPOUSE") != null;
    showAddr        = request.getParameter("ADDR") != null;
    showCity        = request.getParameter("CITY") != null;
    showState       = request.getParameter("STATE") != null;
    showZip         = request.getParameter("ZIP") != null;
    showHome        = request.getParameter("HOME") != null;
    showWork        = request.getParameter("WORK") != null;
    showCell        = request.getParameter("CELL") != null;
    showPager       = request.getParameter("PAGER") != null;
    showEmail       = request.getParameter("EMAIL") != null;
    showEmergency   = request.getParameter("EMERGENCY") != null;
//    showSubstitute   = request.getParameter("SUBSTITUTE") != null;
    showCommit      = request.getParameter("COMMIT") != null;
    showInstructor  = request.getParameter("INSTRUCTOR") != null;
    showDirector    = request.getParameter("DIRECTOR") != null;
    showLastUpdated = request.getParameter("LAST_UPDATED") != null;
    showComments    = request.getParameter("COMMENTS") != null;
//    showOldCredits    = request.getParameter("CARRY_OVER_CREDITS") != null;
    showCreditDate = request.getParameter("LAST_CREDIT_UPDATE") != null;
    showNightCnt    = request.getParameter("NIGHT_CNT") != null;
    showDayCnt      = request.getParameter("DAY_CNT") != null;
    showSwingCnt      = request.getParameter("SWING_CNT") != null;
    showTrainingCnt      = request.getParameter("TRAINING_CNT") != null;
    showNightList   = request.getParameter("NIGHT_DETAILS") != null;
    showSwingList   = request.getParameter("SWING_DETAILS") != null;
    showDayList     = request.getParameter("DAY_DETAILS") != null;
    showTrainingList     = request.getParameter("TRAINING_DETAILS") != null;
//    showTeamLead        = request.getParameter("TEAM_LEAD") != null;
//    showMentoring       = request.getParameter("MENTORING") != null;
//    showCreditsEarned   = request.getParameter("CREDITS_EARNED") != null;
//    showCreditsUsed     = request.getParameter("CREDITS_USED") != null;
    showCanEarnCredits  = request.getParameter("CAN_EARN_CREDITS") != null;

    StartDay    = cvtToInt(request.getParameter("StartDay"));
    StartMonth  = cvtToInt(request.getParameter("StartMonth"));
    StartYear   = cvtToInt(request.getParameter("StartYear"));
    EndDay      = cvtToInt(request.getParameter("EndDay"));
    EndMonth    = cvtToInt(request.getParameter("EndMonth"));
    EndYear     = cvtToInt(request.getParameter("EndYear"));
    useMinDays  = request.getParameter("MIN_DAYS") != null;
    MinDays     = cvtToInt(request.getParameter("MinDays"));


//    Sort1 = request.getParameter("FirstSort");
//    Sort2 = request.getParameter("SecondSort");
//    Sort3 = request.getParameter("ThirdSort");

    textFontSize = cvtToInt(request.getParameter("FontSize"));
    if(debug) {
//Log.log("Sort1="+Sort1);
//Log.log("Sort2="+Sort2);
//Log.log("Sort3="+Sort3);

      LOG.info("showClass="+showClass);
      LOG.info("showID="+showID);
      LOG.info("showBlank="+showBlank);
      LOG.info("showBlankWide="+showBlankWide);
      LOG.info("showSpouse="+showSpouse);
      LOG.info("showAddr="+showAddr);
      LOG.info("showCity="+showCity);
      LOG.info("showState="+showState);
      LOG.info("showZip="+showZip);
      LOG.info("showHome="+showHome);
      LOG.info("showWork="+showWork);
      LOG.info("showCell="+showCell);
      LOG.info("showPager="+showPager);
      LOG.info("showEmail="+showEmail);
      LOG.info("showEmergency="+showEmergency);
//Log.log("showNight="+showNight);
      LOG.info("showCommit="+showCommit);
      LOG.info("showInstructor="+showInstructor);
      LOG.info("showDirector="+showDirector);
      LOG.info("showLastUpdated="+showLastUpdated);
      LOG.info("showComments="+showComments);
//Log.log("showOldCredits="+showOldCredits);
      LOG.info("showCreditDate="+showCreditDate);
      LOG.info("showNightCnt="+showNightCnt);
      LOG.info("showDayCnt="+showDayCnt);
      LOG.info("showSwingCnt="+showSwingCnt);
      LOG.info("showTrainingCnt="+showTrainingCnt);
      LOG.info("showNightList="+showNightList);
      LOG.info("showDayList="+showDayList);
      LOG.info("showSwingList="+showSwingList);
      LOG.info("showTrainingList="+showTrainingList);
      LOG.info("useMinDays="+useMinDays);
      LOG.info("MinDays="+MinDays);
    }
    String[] incList= {"BAS","INA","SR","SRA","ALM","PRO","AUX","TRA","CAN","OTH"};
    classificationsToDisplay = new Vector();
    commitmentToDisplay = 0;
//classification
    for(int i=0; i < incList.length; ++i) {
      String str = request.getParameter(incList[i]);
      if(str != null) {
        classificationsToDisplay.add(incList[i]);
//Log.log(i+") "+incList[i]+" found");
      }
//      else {
////Log.log(i+") "+incList[i]+" skipped");
//      }
    }
//commitment
    if( request.getParameter("FullTime") != null)   commitmentToDisplay += 4;
    if( request.getParameter("PartTime") != null)   commitmentToDisplay += 2;
    if( request.getParameter("Inactive") != null)   commitmentToDisplay += 1;
//Log.log("commitment= "+commitmentToDisplay);

//instructor/director flags
    listDirector = false;
    listAll = false;
    instructorFlags = 0;
    if( request.getParameter("ALL") != null)        listAll = true;
    if( request.getParameter("ListDirector") != null)   listDirector = true;
    if( request.getParameter("OEC") != null)        instructorFlags += 1;
    if( request.getParameter("CPR") != null)        instructorFlags += 2;
    if( request.getParameter("Ski") != null)        instructorFlags += 4;
    if( request.getParameter("Toboggan") != null)   instructorFlags += 8;
//Log.log("listAll= "+listAll);
//Log.log("listDirector= "+listDirector);
//Log.log("instructorFlags= "+instructorFlags);

    PatrolData patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData, LOG); //when reading members, read full data

    //read assignments within a range and get shift count
    readAssignments(patrol); //must read for other code to work


//    ds = patrol.readDirectorSettings();

//hack 8/31/07 "sort1", etc was not set
//	String sortString = getSortString();
    String sortString = "LastName,FirstName";
//end hack

//Log.log("sortString="+sortString);
    ResultSet rosterResults = patrol.resetRoster(sortString);
    ePatrollerList = "";
    Roster member = patrol.nextMember("&nbsp;", rosterResults);
//      MemberData member = patrol.nextMember("");
//int xx=0;
    while(member != null) {
//Log.log(++xx);
      if(member.okToDisplay(false, false, listAll, classificationsToDisplay, commitmentToDisplay, listDirector, instructorFlags, 0)) {
        String em = member.getEmailAddress();
        //check for valid email
        if( em != null && em.length() > 6 && em.indexOf('@') > 0 && em.indexOf('.') > 0) {
          if(ePatrollerList.length() > 2)
            ePatrollerList += ",";
          ePatrollerList += em;
        }
      }
//else Log.log("NOT OK to display "+member);
      member = patrol.nextMember("", rosterResults);
    }
//Log.log("length of email string = "+ePatrollerList.length());
//    Roster editor = patrol.getMemberByID(IDOfEditor); //ID from cookie
//      patrol.close(); //must close connection!
//    if(editor != null)
//      isDirector=editor.isDirector();
//    else
//      isDirector = false;
  }


//  public String getSortString() {
//    String sortString = "";
//    if(Sort1.equals("Name") || Sort1.equals("shiftCnt"))
//      sortString = firstNameFirst ? "FirstName,LastName" : "LastName,FirstName";
//    else if(Sort1.equals("Class"))
//      sortString = "ClassificationCode";
//    else if(Sort1.equals("Comm"))
//      sortString = "Commitment";
//    else if(Sort1.equals("Updt"))
//      sortString = "lastUpdated";
//    else
//      sortString = "FirstName,LastName";  //should not get hit
//
//    if(Sort2.equals("Name") && !Sort1.equals("shiftCnt"))
//      sortString += firstNameFirst ? ",FirstName,LastName" : ",LastName,FirstName";
//    else if(Sort2.equals("Class"))
//      sortString += ",ClassificationCode";
//    else if(Sort2.equals("Comm"))
//      sortString += ",Commitment";
////  else if(Sort1.equals("shiftCnt"))
////      sortString += "";
////  else if(Sort1.equals("DCnt"))
////      sortString += "";
//    else if(Sort2.equals("Updt"))
//      sortString += ",lastUpdated";
//
//    if(Sort3.equals("Name"))
//      sortString += firstNameFirst ? ",FirstName,LastName" : ",LastName,FirstName";
//    else if(Sort3.equals("Class"))
//      sortString += ",ClassificationCode";
//    else if(Sort3.equals("Comm"))
//      sortString += ",Commitment";
////  else if(Sort1.equals("shiftCnt"))
////      sortString += "";
////  else if(Sort1.equals("DCnt"))
////      sortString += "";
//    else if(Sort3.equals("Updt"))
//      sortString += ",lastUpdated";
////Log.log("sortString="+sortString);
//    return sortString;
//  }


  public void excelOutput(SessionData sessionData) {
    PatrolData patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData, LOG);
//sg 8/31/07
//        MemberData member = patrol.nextMember("");
//        out.println(member.getExcelHeader());
//        while(member != null) {
//            out.println(member.getExcelString());
//            member = patrol.nextMember(""); // "&nbsp;" is the default string field
//        }
//zzzz

    totalCount = 0;
    while(totalCount < members.size()) {
      Roster member = (Roster)members.elementAt(totalCount);
      int totalAssignments = member.AssignmentCount[Assignments.DAY_TYPE] +
          member.AssignmentCount[Assignments.SWING_TYPE] +
          member.AssignmentCount[Assignments.TRAINING_TYPE] +
          member.AssignmentCount[Assignments.NIGHT_TYPE];
      if(!useMinDays || totalAssignments < MinDays) {
        if(actualCount == 0)
          out.println(member.getExcelHeader());
        out.println(member.getExcelString());
//from ListPatrollers.java
// member.printMemberListRowData(out);
//
        actualCount++;
      }
      totalCount++;
    }
    patrol.close(); //must close connection!
  } //end excelOutput


//  public void palmOutput() {
//    PatrolData patrol = new PatrolData(PatrolData.FETCH_ALL_DATA,resort);
//
//    MemberData member = patrol.nextMember("");
//    while(member != null) {
//      out.println(member.getPalmString());
//      member = patrol.nextMember(""); // "&nbsp;" is the default string field
//    }
//    patrol.close(); //must close connection!
//  } //end palmOutput

  public void printTop() {
    out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
    out.println("<html><head>");
    out.println("<meta http-equiv=\"Content-Language\" content=\"en-us\">");
    out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=windows-1252\">");
    out.println("<title>Database Maintenance</title>");
    out.println("</head><body>");
    out.println("<h1 align=\"center\">Download the Roster as a CSV file?</h1>");
  }

  public void printBody() {
    out.println("<p align=\"left\">You can download the full patrol roster in a format that can be");
    out.println("imported into other applications.</p>");
    out.println("<p align=\"left\">Instructions:&nbsp; (Yes, I know this is NOT really simple)</p>");
    out.println("<p align=\"left\">1) You MUST have the Palm Pilot desktop software</p>");
    out.println("<p align=\"left\">2) Click the download button to save the roster on your hard");
    out.println("drive (remember exactly where you put it)</p>");
    out.println("<p align=\"left\">3) Start the Palm Pilot's Desktop program</p>");
    out.println("<p align=\"left\">4) In the Desktop program select the Address tab</p>");
    out.println("<p align=\"left\">5) If you need to, create a new address group (ie. Ski Patrol),");
    out.println("THEN and go to that group<br>");
    out.println("&nbsp;&nbsp;&nbsp; If you are <i>updating</i> your group, I suggest deleting all");
    out.println("the old names BEFORE importing new names</p>");
    out.println("<p align=\"left\">6) import the file you downloaded, as a comma delimited text");
    out.println("file</p>");
    out.println("<p align=\"left\">7) then sync with your Palm Pilot</p>");
//zzz
//        out.println("<form action=\""+PatrolData.SERVLET_URL+"download?format=PalmOutput&resort="+resort+"&ID="+ szMyID +" method=POST>");

    out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"resort\" VALUE=\""+resort+"\">");
    out.println("  <p align=\"left\">");
//        out.println("  <input type=\"submit\" value=\"Download Roster\">");
    String options = "&format=PalmOutput";
    String loc = "download?resort="+resort+"&ID="+ szMyID + options;
    out.println("<INPUT TYPE=\"button\" VALUE=\"Download FULL Roster\" onClick=window.location=\""+loc+"\">");
    out.println("  </a>");
    out.println("  </p>");
//        out.println("</form>");
    out.println("<p align=\"left\">&nbsp;</p>");
  }

  public void printBottom() {
    out.println("</body></html>");
  }

  public void readAssignments(PatrolData patrol) {
    Assignments ns;
    int i;
//hack 8/31/07 "sort1", etc was not set
//	String sortString = getSortString();
    String sortString = "LastName,FirstName";
//end hack
//Log.log("readAssignments-sortString="+sortString);
    ResultSet rosterResults = patrol.resetRoster(sortString);
//      patrol.resetRoster();
    Roster member;

    maxShiftCount = 0;
    members = new Vector(PatrolData.MAX_PATROLLERS);
    hash = new Hashtable();
//int xx = 0;
    while((member = patrol.nextMember("&nbsp;", rosterResults)) != null) {
//Log.log(++xx);
      if(member.okToDisplay(false, false, listAll, classificationsToDisplay, commitmentToDisplay, listDirector, instructorFlags, 0)) {
//              ++count;
        members.addElement(member);
        hash.put(member.getID() ,member);
      }
//else Log.log("NOT ok to display "+member);
    }

    ResultSet assignmentResults = patrol.resetAssignments();
//        SimpleDateFormat normalDateFormatter = new SimpleDateFormat ("MM'/'dd'/'yyyy");
//Log.log("StartYear="+StartYear+", StartMonth="+StartMonth+", StartDay="+StartDay);
//Log.log("EndYear="+EndYear+", EndMonth="+EndMonth+", StartDay="+StartDay);
    GregorianCalendar date = new GregorianCalendar(StartYear,StartMonth,StartDay);
    long startMillis = 0;
    long endMillis = 99999999999999L;
    long currMillis;
    if(StartYear != 0)
      startMillis = date.getTimeInMillis();
    date = new GregorianCalendar(EndYear,EndMonth,EndDay);
    if(EndYear != 0)
      endMillis = date.getTimeInMillis();

    while((ns = patrol.readNextAssignment(assignmentResults)) != null) {
      date = new GregorianCalendar(ns.getYear(),ns.getMonth(),ns.getDay());
      currMillis = date.getTimeInMillis();
//System.out.print("start="+startMillis+"end="+endMillis+" curr="+currMillis+" "+ns.getYear()+" "+ns.getMonth()+" "+ns.getDay());
      if(startMillis <= currMillis && currMillis <= endMillis) {
//Log.log(" ok");
        for(i =0; i < Assignments.MAX_ASSIGNMENT_SIZE ; ++i) {
          //              member = patrol.getMemberByID(ns.getPosID(i));
          member = (Roster)hash.get(ns.getPosID(i));
          //System.out.print(ns.getPosID(i) + " ");
          if(member != null && member.okToDisplay(false, false, listAll, classificationsToDisplay,commitmentToDisplay, listDirector, instructorFlags, 0)) {
            //System.out.print("(y, ");
            String tim = ns.getStartingTimeString();
            if(showDayCnt && ns.isDayShift()) {
              ++member.AssignmentCount[Assignments.DAY_TYPE];
              if(maxShiftCount < member.AssignmentCount[Assignments.DAY_TYPE])
                maxShiftCount = member.AssignmentCount[Assignments.DAY_TYPE];
              member.szAssignments[Assignments.DAY_TYPE] += ns.getMyFormattedDate() + " ";
            }
            if(showSwingCnt && ns.isSwingShift()) {
              ++member.AssignmentCount[Assignments.SWING_TYPE];
              if(maxShiftCount < member.AssignmentCount[Assignments.SWING_TYPE])
                maxShiftCount = member.AssignmentCount[Assignments.SWING_TYPE];
              member.szAssignments[Assignments.SWING_TYPE] += ns.getMyFormattedDate() + " ";
            }
            if(showNightCnt && ns.isNightShift()) {
              ++member.AssignmentCount[Assignments.NIGHT_TYPE];
              if(maxShiftCount < member.AssignmentCount[Assignments.NIGHT_TYPE])
                maxShiftCount = member.AssignmentCount[Assignments.NIGHT_TYPE];
              member.szAssignments[Assignments.NIGHT_TYPE] += ns.getMyFormattedDate() + " ";
            }
            if(showTrainingCnt && ns.isTrainingShift()) {
              ++member.AssignmentCount[Assignments.TRAINING_TYPE];
              if(maxShiftCount < member.AssignmentCount[Assignments.TRAINING_TYPE])
                maxShiftCount = member.AssignmentCount[Assignments.TRAINING_TYPE];
              member.szAssignments[Assignments.TRAINING_TYPE] += ns.getMyFormattedDate() + " ";
            }
//zzzz
          } //end if okToDisplay
        } //end for loop for shift
      } else { //end test for date
//Log.log(" Skipped");
      }
//Log.log();
    } //end while loop (all assignments)
  }

  int cvtToInt(String strNum) {
    int num = 0;
    try {
      if(strNum != null)
        num = Integer.parseInt(strNum);
    } catch (Exception e) {
      //do nothing
    }
    return num;
  }
}
}
