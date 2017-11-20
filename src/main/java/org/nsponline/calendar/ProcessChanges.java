package org.nsponline.calendar;

import org.nsponline.calendar.misc.*;
import org.nsponline.calendar.store.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TimeZone;

public class ProcessChanges extends HttpServlet {

  @SuppressWarnings("FieldCanBeLocal")
  private static boolean PAUSE_ON_THIS_SCREEN = false;

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    Utils.printRequestParameters(this.getClass().getSimpleName(), request);
    new LocalProcessChanges(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    Utils.printRequestParameters(this.getClass().getSimpleName(), request);
    new LocalProcessChanges(request, response);
  }

  private class LocalProcessChanges {

    private PrintWriter out;
    private int date1, month1, year1, dayOfWeek0based;

    @SuppressWarnings("unused")
    private int date2, month2, year2, dayOfWeek2; //date2 etc all have to do with "trade" which is currently disabled
    private PatrolData patrolData;
    private String strChange3 = "";

    private Assignments night1, night2;
    private String szSubmitterName;
    private java.util.Date currTime;
    private int nIndex1AsNum;
    private String szMyID;
    private HashMap<String, NewIndividualAssignment> monthNewIndividualAssignments = new HashMap<String, NewIndividualAssignment>();

    private boolean sentToFirst;
    private boolean sentToSecond;
    private String submitterID;
    private String transaction;
    private String selectedID;
    private String szdate1;
    private String pos1;
    private String szPos2;
    private String index1AsString;
    private String listName;
    private String newID;
    private String newName;
    private String secondID;

    @SuppressWarnings("unused")
    private String secondName;  //secondName etc all have to do with "trade" which is currently disabled
    private Roster submitter;
    private Roster member1;
    private Roster member2;     //replaced member
    private Calendar calendarToday;

    private boolean dupError;
    private int nPos1;
    private boolean err;
    private String resort;

    private int transNumber;
    private final int ERROR = 0;
    private final int INSERT = 1;
    private final int REPLACE = 2;
    private final int REMOVE = 3;
    private final int TRADE = 4;
    private final int MISSED_SHIFT = 5;
    private final int NEEDS_REPLACEMENT = 6;
    private final int NO_REPLACEMENT_NEEDED = 7;

    private String trans[] = {
        "Error",
        "Insert Patroller",
        "Replace Patroller",
        "Remove Patroller",
        "Trade Days with Patroller",
        "Missed Shift",
        "Needs a Replacement",
        "No Replacement Needed"
    };

    private LocalProcessChanges(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      SessionData sessionData = new SessionData(request, out);
      ValidateCredentials credentials = new ValidateCredentials(sessionData, request, response, "MonthCalendar"); //this class has no display
      if (credentials.hasInvalidCredentials()) {
        return;
      }
      szMyID = sessionData.getLoggedInUserId();
      resort = request.getParameter("resort");

      response.setContentType("text/html");
      out = response.getWriter();

      patrolData = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData);
      readParameters(request);
      OuterPage outerPage = new OuterPage(patrolData.getResortInfo(), "", sessionData.getLoggedInUserId());
      outerPage.printResortHeader(out);
      printBody(request, sessionData);
      outerPage.printResortFooter(out);
      if (!PAUSE_ON_THIS_SCREEN) {
        response.sendRedirect(PatrolData.SERVLET_URL + "MonthCalendar?resort=" + resort + "&month=" + (month1 - 1) + "&year=" + year1 + "&resort=" + resort + "&ID=" + szMyID);
      }
      patrolData.close();
    }

    private void readParameters(HttpServletRequest request) {

      // create a GregorianCalendar with the Pacific Daylight time zone
      // and the current date and time
      calendarToday = new GregorianCalendar(TimeZone.getDefault());
      currTime = new java.util.Date();

      submitterID = request.getParameter("submitterID");  //required
      transaction = request.getParameter("transaction");  //required
      selectedID = request.getParameter("selectedID");    //required
      szdate1 = request.getParameter("date1");            //required
      pos1 = request.getParameter("pos1");                //required 1-based
      index1AsString = request.getParameter("index1");    //required 0-based
      listName = request.getParameter("listName");        //name 'selected' by radio button (can be null if 'remove' existing name)

//      log(request, "ProcessChanges: submitterID=" + submitterID
//          + ", transaction=" + transaction
//          + ", selectedID=" + selectedID
//          + ", szdate1=" + szdate1
//          + ", pos1=" + pos1
//          + ", index1=" + index1AsString
//          + ", listName=" + listName);

      member1 = null;
      member2 = null;
      night1 = null;
      night2 = null;
      secondID = null;
      submitter = patrolData.getMemberByID(submitterID);
      if (submitter == null) {
        out.println("<h1>Error: member " + submitterID + " not found!</h1><br>");
        return;
      }
      log(request, "submitter=" + submitter.getFullName() + " selectedID=" + selectedID  + " resort=" + resort + " trans=" + transaction +
          " date1=" + szdate1 + " pos1=" + pos1 + " index1=" + index1AsString + " old_name=" + listName);
      szSubmitterName = submitter.getFullName();

      nPos1 = Integer.parseInt(pos1);
      nIndex1AsNum = PatrolData.StringToIndex(index1AsString);
//srg 10/9/11        nIndex1 = Integer.parseInt(index1);

      Integer tmp = new Integer(szdate1.substring(8, 10));
      date1 = tmp;
      tmp = new Integer(szdate1.substring(5, 7));
      month1 = tmp;
      tmp = new Integer(szdate1.substring(0, 4));
      year1 = tmp;

      //noinspection MagicConstant
      calendarToday.set(year1, month1 - 1, date1);
      dayOfWeek0based = calendarToday.get(Calendar.DAY_OF_WEEK) - 1;  //calendarToday.get(..) returns a 1 based DOW index Sunday=1
//read assignments
      night1 = patrolData.readAssignment(szdate1); //
      if (night1 == null) {
        //read and insert assignments from shift table
//        patrolData.resetShiftDefinitions();
//        Shifts shift;
        int cnt = 1;
        String today = szdate1.substring(0, szdate1.indexOf("_") + 1);
        for (ShiftDefinitions shift : patrolData.readShiftDefinitions()) {
          if (shift.parsedEventName().equals(Utils.szDays[dayOfWeek0based])) {
            Assignments assign = new Assignments((today + cnt), shift);
            patrolData.writeAssignment(assign);
            if (cnt == nPos1) {
              night1 = assign;
              night1.setExisted();
            }
            ++cnt;
          }
        }
      }
    }

    private void DisplayTransactionInformation() {
      szPos2 = null;
      if (night1 == null) {
        out.println("<h1>Error: Assignment data for " + szdate1 + " not found!</h1><br>");
        return;
      }
      String szPos = night1.getStartingTimeString() + " - " + night1.getEndingTimeString();
      String oldID;
      oldID = night1.getPosID(nIndex1AsNum);
      dupError = false;

      if (transaction.equals("insertMyName")) {
        setupForInsertMyself();
      }
      else if (transaction.equals("missedShift")) {
        if (setupForMissedShift()) {
          return;
        }
      }
      else if (transaction.equals("ReplaceWithMyName")) {
        setupForReplaceWithMyself();
      }
      else if (transaction.equals("replaceWithSomeoneElse")) {
        if (setupForReplaceWithNameInList()) {
          return;  //member in list was not found
        }
      }
      else if (transaction.equals("removeName")) {
        if (setupForRemoveName()) {
          return; //member not found
        }
      }
      else if (transaction.startsWith("trade_")) {
        if (setupForTrade()) {
          return;
        }
      }
      else if (transaction.startsWith("needsReplacement")) {
        if (setupForNeedsReplacement()) {
          return; //member not found
        }
      }
      else if (transaction.startsWith("noReplacementNeeded")) {
        if (setupForNoReplementNeeded()) {
          return; //member not found
        }

      }
      else {
        transNumber = ERROR;
      }

//Title ie (Insert New Name)
      out.println("<h2>" + trans[transNumber] + " (this screen is temporary, please validate what you did.)</h2><br>");
//Insert Steve Gledhill (192443)

      if (transNumber == MISSED_SHIFT) {
        out.println("<B>" + newName + "</B> Missed a Shift Assignment (" + newID + ")<br><br>");
      }
      else if (transNumber == REMOVE) {
        out.println("REMOVE <B>" + newName + "</B> (" + newID + ")<br><br>");
      }
      else if (transNumber == NEEDS_REPLACEMENT) {
        out.println("Hilight  <B>" + newName + "</B> as needing a replacement.<br><br>");
      }
      else if (transNumber == NO_REPLACEMENT_NEEDED) {
        out.println("NO replacement needed for <B>" + newName + "</B> <br><br>");
      }
      else {
        out.println("INSERT <B>" + newName + "</B> (" + newID + ")");
      }
      if (transNumber == REPLACE && !oldID.equals("0")) {
        String oldName;
        member2 = patrolData.getMemberByID(oldID);
        if (member2 == null) {
          oldName = oldID;
        }
        else {
          oldName = member2.getFullName_lastNameFirst();
        }
        out.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Replacing <B>" + oldName + "</B>");
      }
      out.println("<br/>");
//on February 1, 2001 as Auxiliary Patroller
      out.println("on: " + Utils.szDays[dayOfWeek0based] + " " + Utils.szMonthsFull[month1 - 1] + " " + date1 + ", " + year1 + "<br>");
//at position: Auxiliary Patroller"
      out.println("at shift: " + szPos + "<br>");

      if (transNumber == TRADE && szPos2 != null) {
        out.println("<br><br>INSERT <B>" + secondName + "</B> (" + secondID + ")<br><br>");
        out.println("on: " + Utils.szDays[dayOfWeek2] + " " + Utils.szMonthsFull[month2 - 1] + " " + date2 + ", " + year2 + "<br>");
        out.println("at shift: " + szPos2 + "<br>");
//          out.println("at shift: "+szPos[nPos2]+"<br>");
      }
    }

    private boolean setupForTrade() {
//hack
      out.println("<h1>Error: Trade is broken!</h1><br>");
      return true;
//        member1 = pData.getMemberByID(selectedID);
//        if (member1 == null) {
//            out.println("<h1>Error: member " + listName + " not found!</h1><br>");
//            return true;
//        }
//        transNumber = TRADE;
//        newID = submitterID;
//        newName = szSubmitterName;
//        secondID = member1.idNum + "";  //convert to string
//        secondName = member1.getFullName_lastNameFirst();
//        //get info on traded day
//        Integer tmp = new Integer(transaction.substring(14, 16));
//        date2 = tmp;
//        tmp = new Integer(transaction.substring(11, 13));
//        month2 = tmp;
//        tmp = new Integer(transaction.substring(6, 10));
//        year2 = tmp;
//        String nsDate = transaction.substring(6, 16);
//        calendarToday.set(year2, month2 - 1, date2);
//        dayOfWeek2 = calendarToday.get(Calendar.DAY_OF_WEEK) - 1;
//        night2 = pData.readAssignment(nsDate);
//        if (night2 == null) {
//            out.println("<h1>Error: Assignment data for " + nsDate + " not found!</h1><br>");
//            return true;
//        }
//        nPos2 = night2.getPosIndex(submitterID);
//        szPos2 = night2.getStartingTimeString() + " - " + night2.getEndingTimeString();
//        if (nPos2 == -1) {
//            out.println("<h1>Error: NO Assignment data on: " + nsDate + " for patroller " + submitterID + "</h1><br>");
//            return true;
//        }
//        night1.insertAt(nIndex1, newID);
////fix me            night2.insertAt(nIndex2,secondID);
//        return false;
    }

    private boolean setupForRemoveName() {
      member1 = patrolData.getMemberByID(selectedID);
      if (member1 == null) {
        out.println("<h1>Error: member " + selectedID + " not found!</h1><br>");
        return true;
      }
      transNumber = REMOVE;
      newID = selectedID;
      newName = member1.getFullName_lastNameFirst();
      night1.remove(nIndex1AsNum);
      return false;
    }

    private boolean setupForReplaceWithNameInList() {
      member1 = patrolData.getMemberByName2(listName);
      if (member1 == null) {
        out.println("<h1>Error: member " + listName + " not found!</h1><br>");
        return true;
      }
      transNumber = REPLACE;
      newID = member1.getID() + "";
      newName = listName;
      if (night1.getPosIndex(newID) != -1) {
        dupError = true;
      }
      else {
        night1.insertAt(nIndex1AsNum, newID);
      }
      return false;
    }

    private void setupForReplaceWithMyself() {
      transNumber = REPLACE;
      newID = submitterID;
      newName = szSubmitterName;
      if (night1.getPosIndex(newID) != -1) {
        dupError = true;
      }
      else {
        night1.insertAt(nIndex1AsNum, newID);
      }
    }

    private boolean setupForNeedsReplacement() {
      transNumber = NEEDS_REPLACEMENT;
      newID = selectedID;
      member1 = patrolData.getMemberByID(selectedID);
      newName = member1.getFullName();
//        if(resort.equals("Brighton")) {
//            String key = szdate1 + "_" + index1;
      int year = Integer.parseInt(szdate1.substring(0, 4));
      int month = Integer.parseInt(szdate1.substring(5, 7));
      int date = Integer.parseInt(szdate1.substring(8, 10));
//System.out.println("NEEDS_REPLACEMENT debug = key=" + key + " year=" + year + " month=" + month + " date=" + date);
      monthNewIndividualAssignments = patrolData.readNewIndividualAssignments(year, month, date); //entire day
//System.out.println("newAssignment=" + monthNewIndividualAssignments.get(key));
//        }
      return false;
    }

    private boolean setupForNoReplementNeeded() {
      transNumber = NO_REPLACEMENT_NEEDED;
      newID = selectedID;
      member1 = patrolData.getMemberByID(selectedID);
      newName = member1.getFullName();
//        if(resort.equals("Brighton")) {
//            String key = szdate1 + "_" + index1;
      int year = Integer.parseInt(szdate1.substring(0, 4));
      int month = Integer.parseInt(szdate1.substring(5, 7));
      int date = Integer.parseInt(szdate1.substring(8, 10));
//System.out.println("NO_REPLACEMENT_NEEDED debug = key=" + key + " year=" + year + " month=" + month + " date=" + date);
      monthNewIndividualAssignments = patrolData.readNewIndividualAssignments(year, month, date); //entire day
//System.out.println("newAssignment=" + monthNewIndividualAssignments.get(key));
//        }
      return false;
    }

    private boolean setupForMissedShift() {
      transNumber = MISSED_SHIFT;
      String cleanID;
      boolean missedShift = false;
      cleanID = night1.getPosID(nIndex1AsNum);
      if (cleanID.charAt(0) == '-') {
        missedShift = true;
      }

      if (missedShift) { //already missed ?
        newID = selectedID;
        cleanID = newID;
      }
      else {
        cleanID = selectedID;
        newID = "-" + selectedID;             //add "missed"
      }
      member1 = patrolData.getMemberByID(cleanID);
      if (member1 == null) {
        out.println("<h1>Error: member " + listName + " not found!</h1><br>");
        return true;
      }

      newName = member1.getFullName_lastNameFirst();
      night1.insertAt(nIndex1AsNum, newID);
      return false;
    }

    private void setupForInsertMyself() {
      transNumber = INSERT;
      newID = submitterID;
      newName = szSubmitterName;
      if (night1.getPosIndex(newID) != -1) {
        dupError = true;
      }
      else {
        night1.insertAt(nIndex1AsNum, newID);
      }
    }

    private boolean noReplacementNeeded() {
      String key = szdate1 + "_" + index1AsString;
      NewIndividualAssignment newIndividualAssignment = null;
      if (monthNewIndividualAssignments != null) {
        newIndividualAssignment = monthNewIndividualAssignments.get(key);
      }
      if (newIndividualAssignment == null) {
        System.out.println("ERROR-ProcessChanges, newIndividualAssignment not found in noReplacementNeeded");
      }
      else {
        //UPDATE
        newIndividualAssignment.setNeedsReplacement(false);
//            patrolData.updateNewIndividualAssignment(newIndividualAssignment);
//for now, delete the entry (this is temporary)
        patrolData.deleteNewIndividualAssignment(newIndividualAssignment);
      }
      return false; //successful
    }

    /**
     * insert NewIndividualAssignment into table
     *
     * @return true if error occured
     */
    private boolean needsReplacement() {
      String key = szdate1 + "_" + index1AsString;
      NewIndividualAssignment newIndividualAssignment = null;
      if (monthNewIndividualAssignments != null) {
        newIndividualAssignment = monthNewIndividualAssignments.get(key);
      }
      if (newIndividualAssignment == null) {
        //INSERT (eventually this should go away) only used when assignments are duplicated
        int shiftType = NewIndividualAssignment.DAY_TYPE;  //todo shiftType is ignored?
        System.out.println("HACK in needsReplacement, shiftType forced to DAY SHIFT");
        newIndividualAssignment = new NewIndividualAssignment(calendarToday.getTime(), nPos1, nIndex1AsNum, shiftType,
            NewIndividualAssignment.FLAG_BIT_NEEDS_REPLACEMENT, newID, submitterID);

        patrolData.insertNewIndividualAssignment(newIndividualAssignment);
      }
      else {
        //UPDATE
        newIndividualAssignment.setNeedsReplacement(true);
        patrolData.updateNewIndividualAssignment(newIndividualAssignment);
      }
      return false; //successful
    }
//} else if (transNumber == NEEDS_REPLACEMENT) {
//    out.println("<h2>Submission Failed.  This feature is Under Construction</h2>");
//    err = needsReplacement();
//    String key = szdate1 + "_" + index1;
//    NewIndividualAssignment newIndividualAssignment = null;
//    if(monthNewIndividualAssignments != null) {
//      newIndividualAssignment  = monthNewIndividualAssignments.get(key);
//    }
//    if (transNumber == NEEDS_REPLACEMENT && newIndividualAssignment == null) {
//System.out.println("xxxx debug process changes. need to create new NewIndividualAssignment");   //todo
//    } else {
//        newIndividualAssignment.setNeedsReplacement(transNumber == NEEDS_REPLACEMENT);
//        patrolData.updateNewIndividualAssignment(newIndividualAssignment);
//    }


    /**
     * printBody
     */
    public void printBody(HttpServletRequest request, SessionData sessionData) {

      DisplayTransactionInformation();
      DirectorSettings ds = patrolData.readDirectorSettings();
      boolean notifyPatrollers = ds.getNotifyChanges();
      err = true;
      if (transNumber == NO_REPLACEMENT_NEEDED) {
//        out.println("<h2>Submission Failed.  This feature is Under Construction</h2>");
        //todo
        err = noReplacementNeeded();
      }
      else if (transNumber == NEEDS_REPLACEMENT) {
//        out.println("<h2>Submission Failed.  This feature is Under Construction</h2>");
        err = needsReplacement();
      }
      else if (night1 != null) {
        err = patrolData.writeAssignment(night1);
      }
      if (!err && night2 != null) {
        err = patrolData.writeAssignment(night2);
      }
      if (dupError) {
        out.println("<h2>ERROR! Submission Failed.</h2>");
        out.println("<h3>Cannot insert " + newName + ". He/She is <b>already</b> on this shift.</h3>");
      }
      else if (err) {
        out.println("<h2>ERROR! Submission failed</h2>");
      }
      else {

        strChange3 = "Schedule Changed:\n\n";

//          strChange3 +="On "+szMonths[month1-1]+"/"+date1+"/"+year1+" \n  "+
//          szTrans[transNumber]+" " + newName;// +" at position: "+ pos1; ???? szDays[dayOfWeek0based]
        strChange3 += trans[transNumber] + " " + newName + " On " + Utils.szDays[dayOfWeek0based] + ", " + Utils.szMonthsFull[month1 - 1] + "/" + date1 + "/" + year1 + " (" + night1.getStartingTimeString() + " - " + night1.getEndingTimeString() + ")\n  ";// +" at position: "+ pos1;

        if (transNumber == TRADE) {
          strChange3 += "\nOn " + Utils.szMonthsFull[month2 - 1] + "/" + date2 + "/" + year2 + "\n  " +
              trans[transNumber] + " " + secondName;// +" at position: "+ nPos2;
        }
        out.println("<h2>Submission Successful</h2>");
        strChange3 += "\n";
        //todo Nov 5, change (id) to (from email) for my convenience
        out.println("Submission done by: " + szSubmitterName + " (" + sessionData.getEmailUser() + ")<br>");
        strChange3 += "Submission done by: " + szSubmitterName + " (" + sessionData.getEmailUser() + ")\n";
//        out.println("Submission done by: " + szSubmitterName + " (" + submitterID + ")<br>");
//        strChange3 += "Submission done by: " + szSubmitterName + " (" + submitterID + ")\n";
        out.println("Submission time: " + currTime + "<br><br>");
        strChange3 += "Submission time: " + currTime;
        out.println("<br>At Ski Resort: " + resort + "<br>");
        strChange3 += "\nAt Ski Resort: " + resort + "\n";
        strChange3 += "\nPlease do NOT reply to this automated reminder.\n";
        strChange3 += "Unless, you are NOT a member of the National Ski Patrol, and received this email accidently.\n";
        //loop getting all email directors

        //add header & body
//          String smtp="mail.novell.com";
//          String from="SGledhill@Novell.com" ;
//          if(!submitter.isDirector()) {

        sendMailNotifications(request, ds, notifyPatrollers, sessionData);

      } // not err in writing assignment data
//Return to Calendar
      out.println("<br/>&nbsp;&nbsp;&nbsp;&nbsp;");
      String loc = PatrolData.SERVLET_URL + "MonthCalendar?resort=" + resort + "&month=" + (month1 - 1) + "&year=" + year1 + "&ID=" + szMyID;
//      out.println("<A target='_self' HREF='" + loc + "'>Return to Calendar</A> ");
      out.println("<INPUT TYPE=\"button\" VALUE='Return to Calendar' onClick=window.location='" + loc + "'>");
      out.println("<br/><br/>");
    }

    private void sendMailNotifications(HttpServletRequest request, DirectorSettings ds, boolean notifyPatrollers, SessionData sessionData) {
      String smtp = sessionData.getSmtpHost(); //"mail.gledhills.com";
      String from = sessionData.getEmailUser(); //"steve@gledhills.com";
//System.out.println("ds.getNotifyChanges()="+ds.getNotifyChanges());
      if (!ds.getNotifyChanges()) {
        //director settings say don't email on changes to calendar
        log(request,"debug, no mail sent because notify changes is 'false'");
      }
      else if (submitterID.equals(sessionData.getBackDoorUser())) { //using back door login, don't send emails
        log(request,"debug, no mail being sent by the System Administrator");
      }
      else if (resort.equalsIgnoreCase("Sample")) {
        log(request,"debug, no mail being sent for Sample resort");
      }
      else {    //hack to stop email
        MailMan mail = new MailMan(smtp, from, "Automated Ski Patrol Reminder", sessionData);
        ResultSet rosterResults = patrolData.resetRoster();
        Roster mbr;
        sentToFirst = false;
        sentToSecond = false;
        //email all directors in the yesEmail list
        while ((mbr = patrolData.nextMember("", rosterResults)) != null) {
          if (mbr.isDirector() && mbr.getDirector().equalsIgnoreCase("yesEmail")) {
            mailto(sessionData, mail, mbr, strChange3, true);
          }

        } //end while
        //send e-mail to 1st patroller
        if (notifyPatrollers) {
          if (!sentToFirst && member1 != null) {
            mailto(sessionData, mail, member1, strChange3, false);
          }
          if (!sentToSecond && member2 != null) {
            mailto(sessionData, mail, member2, strChange3, false);
          }
        }
      }
    }

    /**
     * @param mail       xx
     * @param mbr        yy
     * @param strChange3 zz
     * @param director   not used
     */
    private void mailto(SessionData sessionData, MailMan mail, Roster mbr, String strChange3, boolean director) {
      //noinspection StatementWithEmptyBody
      if (director) {
        //todo send director notifications here???
      }

      if (mbr == null) {
        return;
      }
      String recipient = mbr.getEmailAddress();
      if (recipient != null && recipient.length() > 3 && recipient.indexOf('@') > 0) {
        if (member1 != null && member1.getID().equals(mbr.getID())) {
          sentToFirst = true;
        }
        else if (member2 != null && member2.getID().equals(mbr.getID())) {
          sentToSecond = true;
        }
        mail.sendMessage(sessionData, "Patrol Roster Changed (" + resort + ")", strChange3, recipient);
      }
    }

    @SuppressWarnings("unused")
    private void log(String msg) {
      //noinspection ConstantConditions
      System.out.println("ProcessChanges: " + msg);
    }

    private void log(HttpServletRequest request, String msg) {
      //noinspection ConstantConditions
      Utils.printToLogFile(request, resort, submitterID, "ProcessChanges: " + msg);
    }
  } //end LocalProcessChanges
}

