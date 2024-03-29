package org.nsponline.calendar.resources;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.nsponline.calendar.store.Assignments;
import org.nsponline.calendar.store.Roster;
import org.nsponline.calendar.utils.*;

@SuppressWarnings("CommentedOutCode")
public class InnerEmailForm extends ResourceBase {
  final private HttpServletResponse response; //todo move into ResourceBase

  private static final String fallback_from = "steve@gledhills.com";

  private Vector<String> emaiPatrollerList;
  private Vector<String> invalidEmailPatrollerList;

  private static final boolean DEBUG_NO_SEND = false;

  private Vector<String> classificationsToDisplay = null;
  private int commitmentToDisplay = 0;
  private boolean EveryBody;
  private boolean SubList;
  private boolean listDirector = false;
  private boolean listAll = false;
  private int instructorFlags = 0;
  private PatrolData patrol = null;
  //  int totalCount = 0;
  //  int textFontSize = 14;
  private Hashtable<String, Roster> mapId2MemberData;
  private boolean showDayCnt;
  private boolean showSwingCnt;
  private boolean showNightCnt;
  private boolean showTrainingCnt;
  private boolean showOtherCnt;
  private boolean showHolidayCnt;

  private int StartDay;
  private int StartMonth;
  private int StartYear;
  private int EndDay;
  private int EndMonth;
  private int EndYear;
  private int MinDays;

  private String subject;
  private String message;
  private String memberFromEmailAddress;
  private Roster fromMember;
  private boolean hasValidReturnEmailAddress;
  private String smtp;
  private String fromEmailAddress;
  private int originalPatrollerEmailCount;
  private String[] memberIds;
  private boolean messageIsUnique;
  private String fullPatrolName;

  public InnerEmailForm(HttpServletRequest request, HttpServletResponse response, Logger LOG) throws IOException {
    super(request, response, LOG);
    this.response = response;
  }

  public void runner(String parentClassName) throws IOException {
    if (!initBaseAndAskForValidCredentials(response, parentClassName)) {
      return;
    }

    final String szMyID = sessionData.getLoggedInUserId();
    if (szMyID != null) {
      readData(request);
      BuildLists(szMyID, sessionData);
    }

    String Submit = request.getParameter("Submit");

    PatrolData patrol = new PatrolData(resort, sessionData, LOG); //when reading members, read full data

    printCommonHeader();
    printTop(out, Submit);
    if (Submit != null) {
      LOG.debug("resort " + resort + ", sending emails");
      SendEmails(request, szMyID, sessionData);
    } else {
      printMiddle(out, resort, szMyID);
    }
    printCommonFooter();

  }

  private boolean readSubject(HttpServletRequest request) {
    subject = request.getParameter("subject");
    if (subject.length() < 2) {
      out.println("Error, Subject required<br>");
      out.println("<form><p>");
      out.println("<input type=\"button\" value=\"Try Again\" onClick=\"history.back()\">");
      out.println("</p>");
      out.println("</form>");
      return false;
    }
    return true;
  }

  private boolean readMessage(HttpServletRequest request) {
    message = request.getParameter("message");
    message = fixMagicCharactersFromMicrosoftWordPaste(message);

    if (message.length() < 4) {
      out.println("Error, message must be at least 4 characters long<br>");
      out.println("<form><p>");
      out.println("<input type=\"button\" value=\"Try Again\" onClick=\"history.back()\">");
      out.println("</p>");
      out.println("</form>");
      return false;
    }
    return true;
  }

  private boolean getFromEmailAddress() {
    memberFromEmailAddress = fromMember.getEmailAddress();
    hasValidReturnEmailAddress = true;
    if (!StaticUtils.isValidEmailAddress(memberFromEmailAddress)) {
      memberFromEmailAddress = fallback_from;
      hasValidReturnEmailAddress = false;
    }
    return true;
  }

  private boolean getFromMember(String szMyId) {
    fromMember = patrol.getMemberByID(szMyId);
    return true;
  }

  private boolean readEmailData(HttpServletRequest request, String szMyID, SessionData sessionData, String resort) {
    originalPatrollerEmailCount = cvtToInt(request.getParameter("patrollerCount"));
    memberIds = request.getParameterValues("Patrollers");
    if (memberIds == null) {
      out.println("Error, No names selected<br>");
      out.println("<form><p>");
      out.println("<input type=\"button\" value=\"Try Again\" onClick=\"history.back()\">");
      out.println("</p>");
      out.println("</form>");
      return false;
    }

    if (!getFromMember(szMyID) ||
      !readSubject(request) ||
      !readMessage(request) ||
      !getFromEmailAddress()) {
      return false;
    }
    smtp = sessionData.getSmtpHost(); //MailMan._smtpHost;
    fromEmailAddress = fallback_from;
    fullPatrolName = PatrolData.getResortFullName(resort, LOG);

    if (hasValidReturnEmailAddress) {
      fromEmailAddress = memberFromEmailAddress;
    }

    messageIsUnique = (
//      message.contains("$pass$") ||
      message.contains("$last$") ||
      message.contains("$first$") ||
      message.contains("$id$") ||
      message.contains("$carryovercredits$")
//    ||  message.contains("$credits$")
    );
    return true;
  }

  private void logEmailBaseInfo(String resort) {
    String str = "sending emails to " + memberIds.length + " out of " + originalPatrollerEmailCount + " patrollers who had valid email addresses.";
    GregorianCalendar calendar = new GregorianCalendar();
    //output to html page
    out.println(str + "<br>");
    //output to tomcat logs
//todo 3/4/21    LOG.info( str + "from=" + fromMember.getFullName() + " &lt;<b>" + memberFromEmailAddress + "&gt;</b><br><br>");
//    PatrolData.logger(resort, "Subject=" + subject + "<br>");
//    PatrolData.logger(resort, "Message=" + message + "<br>" + "time=" + calendar.getTime().toString() + "<br>");
  }

  private int logEveryEmailSent(int currentEmailCount, Roster member, String resort) {
    String str = (++currentEmailCount) + ") Mailing: " + member.getFullName() +
      " at: " + member.getEmailAddress();
    //output to html page
    out.println(str + "<br>");
    //output to tomcat logs
//todo 3/4/21    PatrolData.logger(resort, str);
    return currentEmailCount;
  }

  /**
   * build the full message string, that may be unique for each member
   *
   * @param member memberData
   * @return message string to email with footers
   */
  private String getUniqueMessage(Roster member, String resort) {
    String newMessage;

    if (messageIsUnique) {
//      String pass = member.getPassword().trim();
//      //if no password, then their last name is the password
//      if (pass.equals("")) {
//        pass = member.getLast();
//      }
      newMessage = message.replaceAll("\\$last\\$", member.getLast());
      newMessage = newMessage.replaceAll("\\$first\\$", member.getFirst());
      newMessage = newMessage.replaceAll("\\$id\\$", member.getID());
//      newMessage = newMessage.replaceAll("\\$pass\\$", pass);
      newMessage = newMessage.replaceAll("\\$carryovercredits\\$", member.getCarryOverCredits());
//      newMessage = newMessage.replaceAll("\\$credits\\$", member.getCreditsEarned());
      newMessage = newMessage.replaceAll("\\$credit\\$", member.getCreditsEarned());
    } else {
      newMessage = message;
    }
    //new message footer
    newMessage += "\n\n" +
      "----------------------------------------------\n" +
      "This message sent by: " + fromMember.getFullName() + " at ( " + fromMember.getEmailAddress() + " )\n" +
      "from " + fullPatrolName + "'s online scheduling web site.\n\n";
    if (!hasValidReturnEmailAddress) {
      newMessage += "Please Don't respond to this email.  SEND any responses\n" +
        "to: " + fromMember.getFullName() + ", who had an invalid email address in the system.\n\n";
    } else {
      ResortData resortInfo = PatrolData.getResortInfo(resort); //will return null if invalid resort (like in dailyReminder because no HttpSession)
      String directorsVerifiedEmail = (resortInfo != null && StaticUtils.isNotEmpty(resortInfo.getDirectorsVerifiedEmail())) ? resortInfo.getDirectorsVerifiedEmail() : null;

//      log("the fromEmailAddress=" + fromEmailAddress);
      if (directorsVerifiedEmail == null) {
        newMessage += "Please Don't respond to this email.  SEND any responses\n" +
          "to: " + fromMember.getEmailAddress() + " .  I am working on setting this 'From' address to be specific to your patrol.\n\n";
      }
    }
    newMessage += "This was sent from the Ski Patrol Web Site Auto Mailer.\n" +
      "--------------------------------------------------------\n";

    return newMessage;
  }

  private void SendEmails(HttpServletRequest request, String szMyID, SessionData sessionData) {
    String newMessage;

    if (!readEmailData(request, szMyID, sessionData, resort)) {
      return;
    }
    logEmailBaseInfo(resort);

    MailMan mailMan = new MailMan(fromEmailAddress, sessionData, LOG);
    //todo srg zzz this is where the main loop is (Oct 28, 2013)
    //loop for each patroller
//    log("44444444444444");
    int currentEmailCount = 0;
    //noinspection PointlessBooleanExpression,ConstantConditions
    if (true || messageIsUnique) {
//      log("5555555  memberIds.length=" + memberIds.length);
      for (String memberId : memberIds) {
        Roster member = patrol.getMemberByID(memberId);
//        log("666666 member=" + member);
        currentEmailCount = logEveryEmailSent(currentEmailCount, member, resort);
        newMessage = getUniqueMessage(member, resort);
        if (DEBUG_NO_SEND) {
          out.println("hack, no mail being sent, message body is:<br>");
          out.println(newMessage + "<br>");
        } else {
          //          mailTo2(fromEmailAddress, member, subject, newMessage);
//          log("77777777777");
          mailto(sessionData, mailMan, member, subject, newMessage);
        }
      }
    }
    //      else {
    //        //          newMessage = getUniqueMessage(null);
    //        //          mailToAll(mail, subject, newMessage);
    //      }
  }

  /**
   * replace of word processing characters for '(146), "(147), and "(148) to normal characters
   * for some weird reason, the String.replace() command did not work for char's > 127
   *
   * @param message content to fixup
   * @return cleaned up string
   */
  private String fixMagicCharactersFromMicrosoftWordPaste(String message) {
    char[] foo = message.toCharArray();
    int j;
    for (j = 0; j < message.length(); ++j) {
      if (foo[j] >= 128) {
        if (foo[j] == 146) {
          foo[j] = '\'';
        } else if (foo[j] == 147) {
          foo[j] = '"';
        } else if (foo[j] == 148) {
          foo[j] = '"';
        }
      }
    }
    return new String(foo);
  }

  private void mailto(SessionData sessionData, MailMan mail, Roster mbr, String subject, String message) {
    String recipient = mbr.getEmailAddress();
    if (StaticUtils.isValidEmailAddress(recipient)) {
//      LOG.debug("Sending mail to " + mbr.getFullName() + " at " + recipient);   //no e-mail, JUST LOG IT
      mail.sendMessage(subject, message, recipient);
    }
  } //end mailto

  private void printTop(PrintWriter out, String Submit) {
    if (Submit != null) {
      out.println("<H2>Emails sent.</H2>");
    } else {
      out.println("<H3>Prepare Emails.</H2>");
    }
  }

  private void printMiddle(PrintWriter out, String resort, String szMyID) {
    out.println("<form target='_self' name=\"form\" method=\"post\" action=\"" + PatrolData.SERVLET_URL + "EmailForm\">");
    out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"resort\" VALUE=\"" + resort + "\">");
    out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"ID\" VALUE=\"" + szMyID + "\">");
    //      out.println("<form action=\""+PatrolData.SERVLET_URL+"UpdateInfo\" method=POST id=form02 name=form02>");
    Roster currentMember = patrol.getMemberByID(szMyID);
    String szName = "Invalid";
    if (currentMember != null) {
      szName = currentMember.getFullName();
      String em = currentMember.getEmailAddress();
      //check for valid email
      if (StaticUtils.isValidEmailAddress(em)) {
        szName += " &lt;" + em + "&gt;";
      }
    }
    out.println("<p>From: <input type=\"text\" name=\"from\" size=\"40\" value=\"" + szName + "\" readonly></p>");

    int i;
    int max = emaiPatrollerList.size();
    out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"patrollerCount\" VALUE=\"" + max + "\">");
    out.println("<Table>");
    out.println("<td valign=center>To:</td>");
    out.println("<td width=460>");
    out.println("Send Emails to selected (hilighted) patrollers.<br>");
    out.println("Use <b>&lt;CTRL&gt; Click</b>, to UNSELECT specific members.<br>");
    out.println("   <select multiple size=\"6\" name=\"Patrollers\" readonly>");
    Roster member;

    for (i = 0; i < max; ++i) {
      String id = emaiPatrollerList.elementAt(i);
      member = patrol.getMemberByID(id);
      if (member != null) {
        String name = member.getFullName_lastNameFirst();
        out.println("   <option selected value=" + id + ">" + name + "</option>");
      } else {
        LOG.error("DATA ERROR, api failed. patrol.getMemberByID(" + id + ")");
      }

    }
    out.println("   </select>");
    out.println("<br>" + max + " Email's to send");
    out.println("</tr>  ");
    out.println("<td width=250>");
    out.println("   Patrollers with No Valid Email Address<br>");
    out.println("   <select size=\"5\" name=\"Patrollers2\" readonly>");
    max = invalidEmailPatrollerList.size();
    for (i = 0; i < max; ++i) {
      String id = invalidEmailPatrollerList.elementAt(i);
      member = patrol.getMemberByID(id);
      String name = member.getFullName_lastNameFirst();
      out.println("   <option value=" + id + ">" + name + "</option>");
    }
    out.println("   </select>");
    out.println("<br>" + max + " Email's SKIPPED");
    out.println("</tr>  ");
    out.println("</table>");
    out.println("<br>");
    out.println("Subject: <input type=\"text\" name=\"subject\" size=60\"> (Required)<br>");
    out.println("<p><table><tr>");
    out.println("  <td valign=top>Message: <br>(Required)</td>");
    out.println("  <td valign=top width=100%><textarea  style=\"width: 100%;\" name=\"message\" rows=\"10\"></textarea></td>");
    out.println("</tr></table>");

    out.println("<p>");
    out.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
    if (resort.equals("Sample")) {
      out.println("<input type=\"button\" value=\"Disabled in Demo\" onClick=\"history.back()\">");
    } else {
      out.println("<input type=\"submit\" name=\"Submit\" value=\"Send Mail\">&nbsp;(be patient, sending mail can take 30 seconds to respond)&nbsp;&nbsp;&nbsp;");
    }
    out.println("<input type=\"button\" value=\"Cancel\" onClick=\"history.back()\">");
    out.println("</p>");
    out.println("</form>");
    out.println("<table border=0 cellpadding=0 cellspacing=0 style=\"border-collapse: collapse\" width=700>\n");
    out.println("  <tr>\n");
    out.println("    <td colspan=2>\n");
    out.println("    <p align=left><b><font size=4>Special replacement codes for email Message</b> (must be lower case ONLY)</font></td>\n");
    out.println("  </tr>\n");
    out.println("  <tr>\n");
    out.println("    <td align=right>$first$</td>\n");
    out.println("    <td>&nbsp;-- replace with patrollers First Name</td>\n");
    out.println("  </tr>\n");
    out.println("  <tr>\n");
    out.println("    <td align=right>$last$</td>\n");
    out.println("    <td width=354>&nbsp;-- replace with patrollers Last Name</td>\n");
    out.println("  </tr>\n");
    out.println("  <tr>\n");
    out.println("    <td align=right>$id$</td>\n");
    out.println("    <td>&nbsp;-- replace with patrollers ID number</td>\n");
    out.println("  </tr>\n");
    out.println("  <tr>\n");
//    out.println("    <td  align=right>$pass$</td>\n");
//    out.println("    <td >&nbsp;-- replace with patrollers password</td>\n");
    out.println("  </tr>\n");
//    if (resort.equalsIgnoreCase("Brighton")) {
      //            out.println("  <tr>\n");
      //            out.println("    <td align=right>$carryovercredits$</td>\n");
      //            out.println("    <td>&nbsp;-- replace with number of 'Carry Over' credits on file</td>\n");
      //            out.println("  </tr>\n");
//      out.println("  <tr>\n");
//      out.println("    <td align=right>$credits$</td>\n");
//      out.println("    <td>&nbsp;-- replace with number of credits available (as of last update)</td>\n");
//      out.println("  </tr>\n");
//    }
    out.println("</table>\n");
    out.println("<br/><br/>");
  }

  private void readData(HttpServletRequest request) {
    String str = request.getParameter("EveryBody");
    if (str != null) {
      EveryBody = true;
      return;
    }
    EveryBody = false;

    str = request.getParameter("SubList");
    if (str != null) {
      SubList = true;
      return;
    }
    SubList = false;

    showDayCnt = request.getParameter("DAY_CNT") != null;
    showSwingCnt = request.getParameter("SWING_CNT") != null;
    showNightCnt = request.getParameter("NIGHT_CNT") != null;
    showTrainingCnt = request.getParameter("TRAINING_CNT") != null;
    showOtherCnt = request.getParameter("OTHER_CNT") != null;
    showHolidayCnt = request.getParameter("HOLIDAY_CNT") != null;

    //day/swing/night details are not used here
    //    final boolean showSwingList = request.getParameter("SWING_DETAILS") != null;
    //    final boolean showNightList = request.getParameter("NIGHT_DETAILS") != null;
    //    final boolean showTrainingList = request.getParameter("TRAINING_DETAILS") != null;

    StartDay = cvtToInt(request.getParameter("StartDay"));
    StartMonth = cvtToInt(request.getParameter("StartMonth"));
    StartYear = cvtToInt(request.getParameter("StartYear"));
    EndDay = cvtToInt(request.getParameter("EndDay"));
    EndMonth = cvtToInt(request.getParameter("EndMonth"));
    EndYear = cvtToInt(request.getParameter("EndYear"));
    final boolean useMinDays = request.getParameter("MIN_DAYS") != null;
    if (useMinDays) {
      MinDays = cvtToInt(request.getParameter("MinDays"));
    } else {
      MinDays = 0;    //no minimum
    }
    //
    //      debugOut("getParameter(MinDays)=" + request.getParameter("MinDays"));
    //      debugOut("EveryBody=" + EveryBody);
    //      debugOut("SubList=" + SubList);
    //      debugOut("StartDay=" + StartDay);
    //      debugOut("StartMonth=" + StartMonth);
    //      debugOut("StartYear=" + StartYear);
    //      debugOut("EndDay=" + EndDay);
    //      debugOut("EndMonth=" + EndMonth);
    //      debugOut("EndYear=" + EndYear);
    //      debugOut("useMinDays=" + useMinDays);
    //      debugOut("MinDays=" + MinDays);
    //      debugOut("showDayCnt=" + showDayCnt);
    //      debugOut("showSwingCnt=" + showSwingCnt);
    //      debugOut("showTrainingCnt=" + showTrainingCnt);
    //      debugOut("showNightCnt=" + showNightCnt);

    String[] incList = {"BAS", "INA", "SR", "SRA", "ALM", "PRO", "AUX", "TRA", "CAN", "OTH"};
    classificationsToDisplay = new Vector<String>();
    commitmentToDisplay = 0;
    //classification
    for (String anIncList : incList) {
      str = request.getParameter(anIncList);
      if (str != null) {
        classificationsToDisplay.add(anIncList);
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
    LOG.debug("commitmentToDisplay= " + commitmentToDisplay);

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
    LOG.debug("listAll= " + listAll + ", ListDirector= " + listDirector + ", instructorFlags= " + instructorFlags);

  }

  /**
   * read all the assignments from the patrol
   *
   * @param patrol patroldate for current patrol
   */
  private void readAssignments(PatrolData patrol) {
    Assignments ns;
    int i;
    ResultSet rosterResults = patrol.resetRoster();
    Roster member;

    //        maxShiftCount = 0;
    //    final Vector<Roster> members = new Vector<Roster>(PatrolData.MAX_PATROLLERS);
    mapId2MemberData = new Hashtable<String, Roster>();
    while ((member = patrol.nextMember("&nbsp;", rosterResults)) != null) {
      //System.out.print(member);
      if (member.okToDisplay(EveryBody, SubList, listAll, classificationsToDisplay, commitmentToDisplay, listDirector, instructorFlags, 0)) {
        //              ++count;
        //PatrolData.logger(resort, "ok");
        //        members.addElement(member);
        mapId2MemberData.put(member.getID(), member);
      }
      //else PatrolData.logger(resort, "NOT ok to display "+member);
    }

    //        SimpleDateFormat normalDateFormatter = new SimpleDateFormat ("MM'/'dd'/'yyyy");
    //        GregorianCalendar date = ;
    long startMillis = (new GregorianCalendar(StartYear, StartMonth, StartDay)).getTimeInMillis();
    long endMillis = (new GregorianCalendar(EndYear, EndMonth, EndDay)).getTimeInMillis();
    long currMillis;

    ResultSet assignmentResults = patrol.resetAssignments();
    //loop through all assignments
    while ((ns = patrol.readNextAssignment(assignmentResults)) != null) {
      currMillis = (new GregorianCalendar(ns.getYear(), ns.getMonth(), ns.getDay())).getTimeInMillis();
      //if(debug) System.out.print("start="+startMillis+"end="+endMillis+" curr="+currMillis+" "+ns.getYear()+" "+ns.getMonth()+" "+ns.getDay());
      if (startMillis <= currMillis && currMillis <= endMillis) {
        //loop thru individual assignments on this day
        for (i = 0; i < Assignments.MAX_ASSIGNMENT_SIZE; ++i) {
          //              member = patrol.getMemberByID(ns.getPosID(i));
          member = mapId2MemberData.get(ns.getPosID(i));
          //if(debug) System.out.print(ns.getPosID(i) + " ");
          if (member != null && member.okToDisplay(EveryBody, SubList, listAll, classificationsToDisplay, commitmentToDisplay, listDirector, instructorFlags, 0)) {
            //if(debug) System.out.print(" ok to display");
            //                        String tim = ns.getStartingTimeString();
            // count shifts
            if (showDayCnt && ns.isDayShift()) {
              ++member.AssignmentCount[Assignments.DAY_TYPE];
            } else if (showSwingCnt && ns.isSwingShift()) {
              ++member.AssignmentCount[Assignments.SWING_TYPE];
            } else if (showNightCnt && ns.isNightShift()) {
              ++member.AssignmentCount[Assignments.NIGHT_TYPE];
            } else if (showTrainingCnt && ns.isTrainingShift()) {
              ++member.AssignmentCount[Assignments.TRAINING_TYPE];
            } else if (showOtherCnt && ns.isOtherShift()) {
              ++member.AssignmentCount[Assignments.OTHER_TYPE];
            } else if (showHolidayCnt && ns.isHolidayShift()) {
              ++member.AssignmentCount[Assignments.HOLIDAY_TYPE];
            }
          } //end if okToDisplay
        } //end for loop for shift
      } //end test for date
    } //end while loop (all assignments)
  } //readAssignments

  private int cvtToInt(String strNum) {
    int num = 0;
    try {
      if (strNum != null) {
        num = Integer.parseInt(strNum);
      }
    } catch (Exception e) {
      //num = 0;
    }
    return num;
  }

  private void BuildLists(@SuppressWarnings("unused") String IDOfEditor, SessionData sessionData) {
    patrol = new PatrolData(resort, sessionData, LOG);

    readAssignments(patrol); //must read ASSIGNMENT data for other code to work

    ResultSet rosterResults = patrol.resetRoster();
    emaiPatrollerList = new Vector<String>();
    invalidEmailPatrollerList = new Vector<String>();
    Roster memberx = patrol.nextMember("&nbsp;", rosterResults);
    //	int siz = members.size();
    //	int i = 0;
    //int xx=0;
    Roster member;

    while (memberx != null) {
      member = mapId2MemberData.get(memberx.getID());
      if (member != null) {
        //simply building list          debugOut(member.getID() + ": " + member.getEmailAddress());
        if (member.okToDisplay(EveryBody, SubList, listAll, classificationsToDisplay, commitmentToDisplay, listDirector, instructorFlags, MinDays)) {
          String emailAddress = member.getEmailAddress();
          //check for valid email
          if (StaticUtils.isValidEmailAddress(emailAddress)) {
            emaiPatrollerList.add(member.getID());
          } else {
            invalidEmailPatrollerList.add(member.getID());
          }
        }
      }
      //else Log.log("NOT OK to display "+member);
      memberx = patrol.nextMember("", rosterResults);
    }
    //Log.log("length of email string = "+ePatrollerList.length());
    //    Roster editor = patrol.getMemberByID(IDOfEditor); //ID from cookie
    //      patrol.close(); //must close connection!
    //      final boolean isDirector = editor != null && editor.isDirector();
  }
} //end InnerEmailForm

