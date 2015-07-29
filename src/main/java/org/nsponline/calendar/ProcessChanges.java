package org.nsponline.calendar;

/**
 * @author Steve Gledhill
 */

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
//import java.sql.Date;


public class ProcessChanges extends HttpServlet {

  PrintWriter out;
  int date1, month1, year1, dayOfWeek1;
  int date2, month2, year2, dayOfWeek2;
  PatrolData patrolData;
  String strChange3 = "";

  Assignments night1, night2;
  String szSubmitterName;
  java.util.Date currTime;
  int nIndex1AsNum;
  String szMyID;
  HashMap<String, NewIndividualAssignment> monthNewIndividualAssignments = new HashMap<String, NewIndividualAssignment>();

  final boolean DEBUG = false;

  boolean sentToFirst;
  boolean sentToSecond;
  String submitterID;
  String transaction;
  String selectedID;
  String szdate1;
  String pos1;
  String szPos2;
  String index1AsString;
  String listName;
  String newID;
  String newName;
  String secondID;
  String secondName;
  String szDays[] = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
  String szTrans[] = {"err", "Insert", "Insert", "remove", "trade days", "Missed Shift"};
  String szMonths[] = {
      "January", "February", "March", "April", "May", "June",
      "July", "August", "September", "October", "November", "December"
  };
  MemberData submitter;
  MemberData member1;
  MemberData member2;     //replaced member
  Calendar calendar1;

  boolean dupError;
  int nPos1;
  boolean err;
  private String resort;

  int transNumber;
  final int ERROR = 0;
  final int INSERT = 1;
  final int REPLACE = 2;
  final int REMOVE = 3;
  final int TRADE = 4;
  final int MISSED_SHIFT = 5;
  final int NEEDS_REPLACEMENT = 6;
  final int NO_REPLACEMENT_NEEDED = 7;

  String trans[] = {
      "Error",
      "Insert Patroller",
      "Replace Patroller",
      "Remove Patroller",
      "Trade Days with Patroller",
      "Missed Shift",
      "Needs a Replacement",
      "No Replacement Needed"
  };

//-------------------------
// doGet

  //-------------------------
  public void doGet(HttpServletRequest request,
                    HttpServletResponse response)
      throws IOException, ServletException {
    synchronized (this) {
      SessionData sessionData = new SessionData(request.getSession(), out);
      CookieID cookie = new CookieID(sessionData, request, response, "MonthCalendar", "ProcessChanges");
      szMyID = cookie.getID();
      resort = request.getParameter("resort");

      response.setContentType("text/html");
      out = response.getWriter();

      if (PatrolData.validResort(resort)) {
        patrolData = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData);
        readParameters(request);
        printTop();
        printBody(sessionData);
      }
      else {
        out.println("Invalid host resort (" + resort + ")");
      }
      patrolData.close();
      printBottom();
//            if (submitter.isDirector() && !dupError) {
      response.sendRedirect(PatrolData.SERVLET_URL + "MonthCalendar?resort=" + resort + "&month=" + (month1 - 1) + "&year=" + year1 + "&resort=" + resort + "&ID=" + szMyID);
//            }

    } //end Syncronized
  }

  /**
   * @param request  http request
   * @param response http response
   * @throws IOException
   * @throws ServletException
   */
  public void doPost(HttpServletRequest request,
                     HttpServletResponse response)
      throws IOException, ServletException {
    doGet(request, response);
  }

  /**
   * @param request http request
   */
  private void readParameters(HttpServletRequest request) {

    // create a GregorianCalendar with the Pacific Daylight time zone
    // and the current date and time
    calendar1 = new GregorianCalendar(PatrolData.MDT);
    currTime = new java.util.Date();

    submitterID = request.getParameter("submitterID");  //required
    transaction = request.getParameter("transaction");  //required
    selectedID = request.getParameter("selectedID");    //required
    szdate1 = request.getParameter("date1");            //required
    pos1 = request.getParameter("pos1");                //required
    index1AsString = request.getParameter("index1");            //required
    listName = request.getParameter("listName");        //name 'selected' by radio button (can be null if 'remove' existing name)
    //noinspection ConstantConditions
    if (DEBUG) {
      System.out.println("submitterID=" + submitterID);
      System.out.println("transaction=" + transaction);
      System.out.println("selectedID=" + selectedID);
      System.out.println("szdate1=" + szdate1);
      System.out.println("pos1=" + pos1);
      System.out.println("index1=" + index1AsString);
      System.out.println("listName=" + listName);
    }
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
    System.out.println("submitter: " + submitter.getFullName() + " (" + resort + ") trans=" + transaction +
        " date1=" + szdate1 + " selectedID=" + selectedID +
        " date1=" + szdate1 + " pos1=" + pos1 + " index1=" + index1AsString + " old name(" + listName +
        ") recorded at time " + PatrolData.getCurrentDateTimeString());
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
    calendar1.set(year1, month1 - 1, date1);
    dayOfWeek1 = calendar1.get(Calendar.DAY_OF_WEEK) - 1;
//read assignments
    night1 = patrolData.readAssignment(szdate1);
    if (night1 == null) {
      //read and insert assignments from shift table
      patrolData.resetShifts();
      Shifts shift;
      int cnt = 1;
      String today = szdate1.substring(0, szdate1.indexOf("_") + 1);
      while ((shift = patrolData.readNextShift()) != null) {
        if (shift.parsedEventName().equals(szDays[dayOfWeek1])) {
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
//-------------------------
// DisplayParameters

  //-------------------------
  private void DisplayParameters() {
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
      if (setupForNeedsReplement()) {
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
    out.println("<h1>" + trans[transNumber] + "</h1><br>");
//Insert Steve Gledhill (192443)

    if (transNumber == MISSED_SHIFT) {
      out.println("<B>" + newName + "</B> Missed a Shift Assignment (" + newID + ")<br><br>");
    }
    else if (transNumber == REMOVE) {
      out.println("REMOVE <B>" + newName + "</B> (" + newID + ")<br><br>");
    }
    else if (transNumber == NEEDS_REPLACEMENT) {
      out.println("Hilight  <B>\" + newName + \"</B> as needing a replacement.   Under Construction<br><br>");
    }
    else if (transNumber == NO_REPLACEMENT_NEEDED) {
      out.println("NO replacement needed for <B>" + newName + "</B> Under Construction<br><br>");
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
        oldName = member2.getFullName2();
      }
      out.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Replacing <B>" + oldName + "</B>");
    }
    out.println("<br><br>");
//on February 1, 2001 as Auxiliary Patroller
    out.println("on: " + szDays[dayOfWeek1] + " " + szMonths[month1 - 1] + " " + date1 + ", " + year1 + "<br>");
//at position: Auxiliary Patroller"
    out.println("at shift: " + szPos + "<br>");

    if (transNumber == TRADE && szPos2 != null) {
      out.println("<br><br>INSERT <B>" + secondName + "</B> (" + secondID + ")<br><br>");
      out.println("on: " + szDays[dayOfWeek2] + " " + szMonths[month2 - 1] + " " + date2 + ", " + year2 + "<br>");
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
//        secondName = member1.getFullName2();
//        //get info on traded day
//        Integer tmp = new Integer(transaction.substring(14, 16));
//        date2 = tmp;
//        tmp = new Integer(transaction.substring(11, 13));
//        month2 = tmp;
//        tmp = new Integer(transaction.substring(6, 10));
//        year2 = tmp;
//        String nsDate = transaction.substring(6, 16);
//        calendar1.set(year2, month2 - 1, date2);
//        dayOfWeek2 = calendar1.get(Calendar.DAY_OF_WEEK) - 1;
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
    newName = member1.getFullName2();
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
    newID = member1.idNum + "";
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

  private boolean setupForNeedsReplement() {
    transNumber = NEEDS_REPLACEMENT;
    newID = selectedID;
    newName = szSubmitterName;
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
    newName = szSubmitterName;
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

    newName = member1.getFullName2();
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

//-------------------------
// printTop

  //-------------------------
  public void printTop() {
    out.println("<html>");
    out.println("<body bgcolor=\"white\">");
    out.println("<head>");

    out.println("<title>Process Changes</title>");
    out.println("<META HTTP-EQUIV=\"Pragma\" CONTENT=\"no-cache\">");
    out.println("<META HTTP-EQUIV=\"Expires\" CONTENT=\"-1\">");
    out.println("</head>");
    out.println("<body>");
  }

//-------------------------
// printBottom

  //-------------------------
  public void printBottom() {
    out.println("</body>");
    out.println("</html>");
  }

  private boolean noReplacementNeeded() {
    //todo
    String key = szdate1 + "_" + index1AsString;
    NewIndividualAssignment newIndividualAssignment = null;
    if (monthNewIndividualAssignments != null) {
      newIndividualAssignment = monthNewIndividualAssignments.get(key);
    }
    if (newIndividualAssignment == null) {
      System.out.println("ERROR, newIndividualAssignment not found in noReplacementNeeded");
    }
    else {
      //UPDATE
      newIndividualAssignment.setNeedsReplacement(false);
//            patrolData.updateNewIndividualAssignment(newIndividualAssignment);
//for now, delete the entry (this is temporary)
      patrolData.deleteNewIndividualAssignment(newIndividualAssignment);
    }
    return true;    //error occured
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
      int shiftType = NewIndividualAssignment.DAY_TYPE;  //todo HACK
      System.out.println("HACK in needsReplacement, shiftType forced to DAY SHIFT");
      newIndividualAssignment = new NewIndividualAssignment(calendar1.getTime(), nPos1, nIndex1AsNum, shiftType,
          NewIndividualAssignment.FLAG_BIT_NEEDS_REPLACEMENT, newID, submitterID);

      patrolData.insertNewIndividualAssignment(newIndividualAssignment);
    }
    else {
      //UPDATE
      newIndividualAssignment.setNeedsReplacement(true);
      patrolData.updateNewIndividualAssignment(newIndividualAssignment);
    }
    return true;    //error occured
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
  public void printBody(SessionData sessionData) {

    DisplayParameters();
    DirectorSettings ds = patrolData.readDirectorSettings();
    boolean notifyPatrollers = ds.getNotifyChanges();
    err = true;
    if (transNumber == NO_REPLACEMENT_NEEDED) {
      out.println("<h2>Submission Failed.  This feature is Under Construction</h2>");
      //todo
      err = noReplacementNeeded();
    }
    else if (transNumber == NEEDS_REPLACEMENT) {
      out.println("<h2>Submission Failed.  This feature is Under Construction</h2>");
      err = needsReplacement();
    }
    else if (night1 != null) {
      err = patrolData.writeAssignment(night1);
    }
    if (!err && night2 != null) {
      err = patrolData.writeAssignment(night2);
    }
    if (transNumber == NEEDS_REPLACEMENT || transNumber == NO_REPLACEMENT_NEEDED) {
      out.println("Try again in a few days...  Thanks for your patients");
    }
    else if (dupError) {
      out.println("<h2>ERROR! Submission Failed.</h2>");
      out.println("<h3>Cannot insert " + newName + ". He/She is <b>already</b> on this shift.</h3>");
    }
    else if (err) {
      out.println("<h2>ERROR! Submission failed</h2>");
    }
    else {

      // create a GregorianCalendar with the Pacific Daylight time zone
      // and the current date and time
//            Calendar today = new GregorianCalendar(PatrolData.MDT);

      strChange3 = "Schedule Changed:\n\n";

//          strChange3 +="On "+szMonths[month1-1]+"/"+date1+"/"+year1+" \n  "+
//          szTrans[transNumber]+" " + newName;// +" at position: "+ pos1; ???? szDays[dayOfWeek1]
      strChange3 += szTrans[transNumber] + " " + newName + " On " + szDays[dayOfWeek1] + ", " + szMonths[month1 - 1] + "/" + date1 + "/" + year1 + " (" + night1.getStartingTimeString() + " - " + night1.getEndingTimeString() + ")\n  ";// +" at position: "+ pos1;

      if (transNumber == TRADE) {
        strChange3 += "\nOn " + szMonths[month2 - 1] + "/" + date2 + "/" + year2 + "\n  " +
            szTrans[transNumber] + " " + secondName;// +" at position: "+ nPos2;
      }
      out.println("<h2>Submission Successful</h2>");
      strChange3 += "\n";
      out.println("Submission done by: " + szSubmitterName + " (" + submitterID + ")<br>");
      strChange3 += "Submission done by: " + szSubmitterName + " (" + submitterID + ")\n";
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

      sendMailNotifications(ds, notifyPatrollers, sessionData);

    } // not err in writing assignment data
//Return to Calendar
    out.println("<A HREF=\"" + PatrolData.SERVLET_URL + "MonthCalendar?resort=" + resort + "&month=" + (month1 - 1) + "&year=" + year1 + "&ID=" + szMyID + "\">Return to Calendar</A> ");
  }

  private void sendMailNotifications(DirectorSettings ds, boolean notifyPatrollers, SessionData sessionData) {
    String smtp = sessionData.getSmtpHost(); //"mail.gledhills.com";
    String from = sessionData.getEmailUser(); //"steve@gledhills.com";
//System.out.println("ds.getNotifyChanges()="+ds.getNotifyChanges());
    //noinspection StatementWithEmptyBody
    if (!ds.getNotifyChanges()) {
      //director settings say don't email on changes to calendar
    }
    else //noinspection StatementWithEmptyBody
      if (submitterID.equals(PatrolData.backDoorUser)) { //using back door login, don't send emails
//                System.out.println("hack, no mail being sent by the System Administrator");
      }
      else //noinspection StatementWithEmptyBody
        if (resort.equalsIgnoreCase("Sample")) {
//                System.out.println("hack, no mail being sent for Sample resort");
        }
        else {    //hack to stop email
//              if(submitter.isDirector())
//                  System.out.println("=== via director ===");
          MailMan mail = new MailMan(smtp, from, "Automated Ski Patrol Reminder", sessionData);
          patrolData.resetRoster();
          MemberData mbr;
          sentToFirst = false;
          sentToSecond = false;
          //email all directors in the yesEmail list
//    System.out.println("----------------");
          while ((mbr = patrolData.nextMember("")) != null) {
            if (mbr.isDirector() && mbr.getDirector().equalsIgnoreCase("yesEmail")) {
              mailto(mail, mbr, strChange3, true, sessionData);
            }

          } //end while
          //send e-mail to 1st patroller
          if (notifyPatrollers) {
            if (!sentToFirst && member1 != null) {
              mailto(mail, member1, strChange3, false, sessionData);
            }
            if (!sentToSecond && member2 != null) {
              mailto(mail, member2, strChange3, false, sessionData);
            }
          }
// System.out.println(strChange3); //LOG message
          mail.close();
        }
  }

  /**
   * @param mail       xx
   * @param mbr        yy
   * @param strChange3 zz
   * @param director   not used
   */
  private void mailto(MailMan mail, MemberData mbr, String strChange3, boolean director, SessionData sessionData) {
    //noinspection StatementWithEmptyBody
    if (director) {
      //todo send director notifications here???
    }

//if(mbr != null) return;   //hack
    if (mbr == null) {
      return;
    }
    String recipient = mbr.getEmail();
    if (recipient != null && recipient.length() > 3 && recipient.indexOf('@') > 0) {
      try {
        if (member1 != null && member1.getID().equals(mbr.getID())) {
          sentToFirst = true;
        }
        else if (member2 != null && member2.getID().equals(mbr.getID())) {
          sentToSecond = true;
        }
//System.out.println("sending to: "+recipient);   //no e-mail, JUST LOG IT
        mail.sendMessage("Patrol Roster Changed (" + resort + ")", strChange3, recipient, sessionData);
//System.out.println("mail was sucessfull");  //no e-mail, JUST LOG IT
      }
      catch (MailManException ex) {
        System.out.println("error " + ex);
        System.out.println("attempting to send mail to: " + recipient);   //no e-mail, JUST LOG IT
      }
    }
  } //end mailto
}


