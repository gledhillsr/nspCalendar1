package org.nsponline.calendar;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Vector;

/**
 * @author Steve Gledhill
 */
public class EmailForm extends HttpServlet {

  //    private final static String fallback_smtp = "zimbra.xmission.com";
  private final static String fallback_from = "steve@gledhills.com";

  Vector<String> emaiPatrollerList;
  Vector<String> invalidEmailPatrollerList;

  private final static boolean DEBUG_NO_SEND = false;
  private final static boolean DEBUG = false;

  boolean isDirector = false;

  Vector<String> classificationsToDisplay = null;
  int commitmentToDisplay = 0;
  boolean EveryBody;
  boolean SubList;
  boolean listDirector = false;
  boolean listAll = false;
  int instructorFlags = 0;
  PatrolData patrol = null;
//  int totalCount = 0;
//  int textFontSize = 14;
  Hashtable<String, MemberData> mapId2MemberData;
  Vector<MemberData> members;
  String szMyID = null;
  PrintWriter out;
  String resort;
  boolean showDayCnt;
  boolean showSwingCnt;
  boolean showNightCnt;
  boolean showTrainingCnt;
  boolean showDayList;
  boolean showSwingList;
  boolean showNightList;
  boolean showTrainingList;

  int StartDay;
  int StartMonth;
  int StartYear;
  int EndDay;
  int EndMonth;
  int EndYear;
  boolean useMinDays;
  int MinDays;

  private String subject;
  private String message;
  private String fromEmail;
  private MemberData fromMember;
  boolean hasValidReturnEmailAddress;
  String smtp;
  String fromEmailAddress;
  int originalPatrollerEmailCount;
  String[] memberIds;
  boolean messageIsUnique;
  String fullPatrolName;

  /**
   * http get request
   *
   * @param request  standard request
   * @param response standard response
   * @throws IOException
   * @throws ServletException
   */
  public void doGet(HttpServletRequest request,
                    HttpServletResponse response)
      throws IOException, ServletException {

    synchronized (this) {
      response.setContentType("text/html");
      out = response.getWriter();
      SessionData sessionData = new SessionData(getServletContext(), out);

      debugOut("Entering EmailForm...");

      CookieID cookie = new CookieID(sessionData, request, response, "EmailForm", null);
//      szMyID = cookie.getID();
      szMyID = sessionData.getLoggedInUserId();
      resort = request.getParameter("resort");
      if (szMyID != null) {
        readData(request);
        BuildLists(szMyID, sessionData);
      }

      String Submit = request.getParameter("Submit");

      printTop(out, Submit);
      if (PatrolData.validResort(resort)) {
        if (Submit != null) {
          debugOut("resort " + resort + ", sending emails");
          SendEmails(request, szMyID, sessionData);
          //SLOW OPERATION HERE
        }
        else {
          printMiddle(out, resort, szMyID);
        }
      }
      else {
        out.println("Invalid host resort.");
      }
      printBottom(out);
    }
  }

  /**
   * main http entry into this module
   *
   * @param request  standard HttpServletRequest
   * @param response standard HttpServletResponse
   * @throws IOException
   * @throws ServletException
   */
  public void doPost(HttpServletRequest request,
                     HttpServletResponse response)
      throws IOException, ServletException {
    doGet(request, response);
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
    fromEmail = fromMember.getEmail();
    hasValidReturnEmailAddress = true;
    if (fromEmail == null || fromEmail.length() <= 6 || fromEmail.indexOf('@') <= 0 || fromEmail.indexOf('.') <= 0) {
      fromEmail = fallback_from;
      hasValidReturnEmailAddress = false;
    }
    return true;
  }

  private boolean getFromMember(String szMyId) {
    fromMember = patrol.getMemberByID(szMyId);
    return true;
  }

  private boolean readEmailData(HttpServletRequest request, String szMyID, SessionData sessionData) {
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
    fullPatrolName = PatrolData.getResortFullName(resort);

    if (hasValidReturnEmailAddress) {
      fromEmailAddress = fromEmail;
    }

    messageIsUnique = (message.indexOf("$pass$") != -1 ||
        message.indexOf("$last$") != -1 ||
        message.indexOf("$first$") != -1 ||
        message.indexOf("$id$") != -1 ||
        message.indexOf("$carryovercredits$") != -1 ||
        message.indexOf("$credits$") != -1);
    return true;
  }

  private void logEmailBaseInfo() {
    String str = "sending emails to " + memberIds.length + " out of " + originalPatrollerEmailCount + " patrollers who had valid email addresses.";
    GregorianCalendar calendar = new GregorianCalendar();
//output to html page
    out.println(str + "<br>");
//output to tomcat logs
    PatrolData.logger(resort, str + " resort=" + resort);
    PatrolData.logger(resort, "from=" + fromMember.getFullName() + " &lt;<b>" + fromEmail + "&gt;</b><br><br>");
    PatrolData.logger(resort, "Subject=" + subject + "<br>");
    PatrolData.logger(resort, "Message=" + message + "<br>" + "time=" + calendar.getTime().toString() + "<br>");
  }

  private int logEveryEmailSent(int currentEmailCount, MemberData member) {
    String str = (++currentEmailCount) + ") Mailing: " + member.getFullName() +
        " at: " + member.getEmail();
//output to html page
    out.println(str + "<br>");
//output to tomcat logs
    PatrolData.logger(resort, str);
    return currentEmailCount;
  }

  /**
   * build the full message string, that may be unique for each member
   *
   * @param member memberData
   * @return message string to email with footers
   */
  private String getUniqueMessage(MemberData member) {
    String newMessage;

    if (messageIsUnique) {
      String pass = member.getPassword().trim();
      //if no password, then their last name is the password
      if (pass.equals("")) {
        pass = member.getLast();
      }
      newMessage = message.replaceAll("\\$pass\\$", pass);
      newMessage = newMessage.replaceAll("\\$last\\$", member.getLast());
      newMessage = newMessage.replaceAll("\\$first\\$", member.getFirst());
      newMessage = newMessage.replaceAll("\\$id\\$", member.getID());
      newMessage = newMessage.replaceAll("\\$carryovercredits\\$", member.getCarryOverCredits());
      newMessage = newMessage.replaceAll("\\$credits\\$", member.getCreditsEarned());
      newMessage = newMessage.replaceAll("\\$credit\\$", member.getCreditsEarned());
    }
    else {
      newMessage = message;
    }
//new message footer
    newMessage += "\n\n" +
        "----------------------------------------------\n" +
        "This message sent by: " + fromMember.getFullName() + "\n" +
        "from " + fullPatrolName + "'s online scheduling web site.\n" +
        "----------------------------------------------\n";

    if (!hasValidReturnEmailAddress) {
      newMessage += "\n\n\n--------------------------------------------------------\n" +
          "Please Don't respond to this email.  SEND any responses\n" +
          "to: " + fromMember.getFullName() + ", who had an invalid email address in the system.\n\n" +
          "This was sent from the Ski Patrol Web Site Auto Mailer.\n" +
          "--------------------------------------------------------\n";
    }

    return newMessage;
  }

  /**
   * @param request servletRequest from doPost
   * @param szMyID  patroller id of patroller sending the email
   */
  private void SendEmails(HttpServletRequest request, String szMyID, SessionData sessionData) {
    String newMessage;

    if (!readEmailData(request, szMyID, sessionData)) {
      return;
    }
    logEmailBaseInfo();

    MailMan mailMan = new MailMan(smtp, fromEmailAddress, fromMember.getFullName(), sessionData);
//todo srg zzz this is where the main loop is (Oct 28, 2013)
    //loop for each patroller
    int currentEmailCount = 0;
    if (true || messageIsUnique) {
      for (String memberId : memberIds) {
        MemberData member = patrol.getMemberByID(memberId);
        currentEmailCount = logEveryEmailSent(currentEmailCount, member);
        newMessage = getUniqueMessage(member);
        if (DEBUG_NO_SEND) {
          out.println("hack, no mail being sent, message body is:<br>");
          out.println(newMessage + "<br>");
        }
        else {
//          mailTo2(fromEmailAddress, member, subject, newMessage);
          mailto(mailMan, member, subject, newMessage, sessionData);
        }
      }
    }
    else {
      //          newMessage = getUniqueMessage(null);
      //          mailToAll(mail, subject, newMessage);
    }
  }

//  private void mailTo2(String fromEmailAddr, MemberData member, String subject, String newMessage) {
//    final String username = "steve@gledhills.com";
//    final String password = "gandalf2";
//
//    Properties props = new Properties();
//    props.put("mail.smtp.auth", "true");
//    props.put("mail.smtp.starttls.enable", "true");
//    props.put("mail.smtp.host", "smtp.gmail.com");
//    props.put("mail.smtp.port", "587");
//
////todo move this out of the loop
//    String recipient = member.getEmail();
//    if (!isValidAddress(recipient)) {
//      return;
//    }
//    Session session = Session.getInstance(props,
//        new javax.mail.Authenticator() {
//          protected PasswordAuthentication getPasswordAuthentication() {
//            return new PasswordAuthentication(username, password);
//          }
//        });
//    try {
//      PatrolData.logger(resort, "session=" + session);
//      Message message = new MimeMessage(session);
//      InternetAddress fromAddress = new InternetAddress(fromEmailAddr);
//      try {
//        fromAddress = new InternetAddress(fromEmailAddr, member.getFullName());
//      } catch (Exception e) {
//        PatrolData.logger(resort, "ERROR creating (fromEmail,fullName) e=" + e);
//      }
//      System.out.print(resort + " -- from: " + fromAddress);
//      message.setFrom(fromAddress);
//      System.out.print(", to: " + recipient);
//      message.setRecipients(Message.RecipientType.TO,
//          InternetAddress.parse(recipient));
//      System.out.print(", subject: " + subject);
//      message.setSubject(subject);
//      message.setText(newMessage);
//
//      System.out.print("sending...");
//      Transport.send(message);
//
//      PatrolData.logger(resort, "Done");
//
//    } catch (MessagingException e) {
//      throw new RuntimeException(e);
//    }
//  }

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
        }
        else if (foo[j] == 147) {
          foo[j] = '"';
        }
        else if (foo[j] == 148) {
          foo[j] = '"';
        }
      }
    }
    return new String(foo);
  }

  private boolean isValidAddress(String emailAddress) {
    return (emailAddress != null && emailAddress.length() > 3 && emailAddress.indexOf('@') > 0);
  }

//  private void mailToAll(MailMan mail, String subject, String message) {
//    int lastValidIndex = 0;
//    for (String memberId : memberIds) {
//      MemberData memberData = patrol.getMemberByID(memberId);
//      if (isValidAddress(memberData.getEmail())) {
//        lastValidIndex++;
//      }
//    }
//    PatrolData.logger(resort, "mass email to:" + lastValidIndex + " patrollers.");
//
//    String[] recipients = new String[lastValidIndex];
//    lastValidIndex = 0;
//    for (String memberId : memberIds) {
//      MemberData memberData = patrol.getMemberByID(memberId);
//      String recipient = memberData.getEmail();
//      PatrolData.logger(resort, "To: " + memberData.getFullName() + ", at address: " + recipient);
//      if (isValidAddress(recipient)) {
//        recipients[lastValidIndex++] = recipient;
//      }
//    }
//
//    if (lastValidIndex > 0) {
//      try {
//        mail.sendMessage(subject, message, recipients);
////                PatrolData.logger(resort, "  mail was sucessfull");    //no e-mail, JUST LOG IT
//      } catch (MailManException ex) {
//        PatrolData.logger(resort, "  error " + ex);
////                PatrolData.logger(resort, "attempting to send mail to: " + recipient);   //no e-mail, JUST LOG IT
//      }
//    }
//  }

  /**
   * @param mail    MailMan object
   * @param mbr     Member data structure for patroller,  used here just to get email address
   * @param subject subject line of email
   * @param message body o email
   */
  private void mailto(MailMan mail, MemberData mbr, String subject, String message, SessionData sessionData) {
    String recipient = mbr.getEmail();
    if (isValidAddress(recipient)) {
//            PatrolData.logger(resort, "Sending mail to " + mbr.getFullName() + " at " + recipient);   //no e-mail, JUST LOG IT
      try {
        mail.sendMessage(subject, message, recipient, sessionData);
//                PatrolData.logger(resort, "  mail was sucessfull");    //no e-mail, JUST LOG IT
      }
      catch (MailManException ex) {
        System.out.println("  error " + ex);
        System.out.println("attempting to send mail to: " + recipient);   //no e-mail, JUST LOG IT
      }
    }
  } //end mailto

  /**
   * output <html><head> sections, and start <body> section
   *
   * @param out    output device
   * @param Submit null indicates Preparing email, any valid string=send emails
   */
  private void printTop(PrintWriter out, String Submit) {

    out.println("<html>");
    out.println("<HEAD>");
    out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=windows-1252\">");

    out.println("<title>Email Selected Patrollers</title>");
    out.println("<META HTTP-EQUIV=\"Pragma\" CONTENT=\"no-cache\">");
    out.println("<META HTTP-EQUIV=\"Expires\" CONTENT=\"-1\">");
    out.println("</HEAD>");
    out.println("<body>");
    if (Submit != null) {
      out.println("<H2>Sending Emails, this may take a while (this is currently broken!!!).</H2>");
    }
    else {
      out.println("<H2>Prepare Emails (is currently broken).</H2>");
    }
//todo srg
  }

  /**
   * output final tags to close html page
   *
   * @param out output device
   */
  public void printBottom(PrintWriter out) {
    out.println("</body>");
    out.println("</html>");
  }

//---------
// printMiddle

  //---------

  private void printMiddle(PrintWriter out, String resort, String szMyID) {
    out.println("<form name=\"form\" method=\"post\" action=\"" + PatrolData.SERVLET_URL + "EmailForm\">");
    out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"resort\" VALUE=\"" + resort + "\">");
    out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"ID\" VALUE=\"" + szMyID + "\">");
//      out.println("<form action=\""+PatrolData.SERVLET_URL+"UpdateInfo\" method=POST id=form02 name=form02>");
    MemberData currentMember = patrol.getMemberByID(szMyID);
    String szName = "Invalid";
    if (currentMember != null) {
      szName = currentMember.getFullName();
      String em = currentMember.getEmail();
      //check for valid email
      if (em != null && em.length() > 6 && em.indexOf('@') > 0 && em.indexOf('.') > 0) {
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
    MemberData member;

    for (i = 0; i < max; ++i) {
      String id = emaiPatrollerList.elementAt(i);
      member = patrol.getMemberByID(id);
      if (member != null) {
        String name = member.getFullName2();
        out.println("   <option selected value=" + id + ">" + name + "</option>");
      }
      else {
        PatrolData.logger(resort, "DATA ERROR, api failed. patrol.getMemberByID(" + id + ")");
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
      String name = member.getFullName2();
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
    }
    else {
      out.println("<input type=\"submit\" name=\"Submit\" value=\"Send Mail\">&nbsp;&nbsp;&nbsp;&nbsp;");
    }
    out.println("<input type=\"button\" value=\"Cancel\" onClick=\"history.back()\">");
    out.println("</p>");
    out.println("</form>");
    out.println("<table border=0 cellpadding=0 cellspacing=0 style=\"border-collapse: collapse\" width=600>\n");
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
    out.println("    <td  align=right>$pass$</td>\n");
    out.println("    <td >&nbsp;-- replace with patrollers password</td>\n");
    out.println("  </tr>\n");
    if (resort.equalsIgnoreCase("Brighton")) {
//            out.println("  <tr>\n");
//            out.println("    <td align=right>$carryovercredits$</td>\n");
//            out.println("    <td>&nbsp;-- replace with number of 'Carry Over' credits on file</td>\n");
//            out.println("  </tr>\n");
      out.println("  <tr>\n");
      out.println("    <td align=right>$credits$</td>\n");
      out.println("    <td>&nbsp;-- replace with number of credits available (as of last update)</td>\n");
      out.println("  </tr>\n");
    }
    out.println("</table>\n");
  }


  /**
   * read data passes in the http request
   *
   * @param request httprequest parameters are:
   *                classification: "BAS","INA","SR","SRA","ALM","PRO","AUX","TRA","CAN"
   *                commitment:     "fulltime", "PartTime", "Inactive"
   *                Instructor:     "ALL", ListDirector", "OEC" "CPR", "Ski", "Toboggan"
   */
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

//day/swing/night details are not used here
    showDayList = request.getParameter("DAY_DETAILS") != null;
    showSwingList = request.getParameter("SWING_DETAILS") != null;
    showNightList = request.getParameter("NIGHT_DETAILS") != null;
    showTrainingList = request.getParameter("TRAINING_DETAILS") != null;

    StartDay = cvtToInt(request.getParameter("StartDay"));
    StartMonth = cvtToInt(request.getParameter("StartMonth"));
    StartYear = cvtToInt(request.getParameter("StartYear"));
    EndDay = cvtToInt(request.getParameter("EndDay"));
    EndMonth = cvtToInt(request.getParameter("EndMonth"));
    EndYear = cvtToInt(request.getParameter("EndYear"));
    useMinDays = request.getParameter("MIN_DAYS") != null;
    if (useMinDays) {
      MinDays = cvtToInt(request.getParameter("MinDays"));
    }
    else {
      MinDays = 0;    //no minimum
    }

    debugOut("getParameter(MinDays)=" + request.getParameter("MinDays"));
    debugOut("EveryBody=" + EveryBody);
    debugOut("SubList=" + SubList);
    debugOut("StartDay=" + StartDay);
    debugOut("StartMonth=" + StartMonth);
    debugOut("StartYear=" + StartYear);
    debugOut("EndDay=" + EndDay);
    debugOut("EndMonth=" + EndMonth);
    debugOut("EndYear=" + EndYear);
    debugOut("useMinDays=" + useMinDays);
    debugOut("MinDays=" + MinDays);
    debugOut("showDayCnt=" + showDayCnt);
    debugOut("showSwingCnt=" + showSwingCnt);
    debugOut("showTrainingCnt=" + showTrainingCnt);
    debugOut("showNightCnt=" + showNightCnt);

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
    debugOut("commitmentToDisplay= " + commitmentToDisplay);

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
    debugOut("listAll= " + listAll);
    debugOut("ListDirector= " + listDirector);
    debugOut("instructorFlags= " + instructorFlags);

  }

  /**
   * read all the assignments from the patrol
   *
   * @param patrol patroldate for current patrol
   */
  private void readAssignments(PatrolData patrol) {
    Assignments ns;
    int i;
    patrol.resetRoster();
    MemberData member;

//        maxShiftCount = 0;
    members = new Vector<MemberData>(PatrolData.MAX_PATROLLERS);
    mapId2MemberData = new Hashtable<String, MemberData>();
    debugOut("====== entering readAssignments ===========");
    while ((member = patrol.nextMember("&nbsp;")) != null) {
//System.out.print(member);
      if (member.okToDisplay(EveryBody, SubList, listAll, classificationsToDisplay, commitmentToDisplay, listDirector, instructorFlags, 0)) {
//              ++count;
//PatrolData.logger(resort, "ok");
        members.addElement(member);
        mapId2MemberData.put(member.getID(), member);
      }
//else PatrolData.logger(resort, "NOT ok to display "+member);
    }

    patrol.resetAssignments();
//        SimpleDateFormat normalDateFormatter = new SimpleDateFormat ("MM'/'dd'/'yyyy");
//        GregorianCalendar date = ;
    long startMillis = (new GregorianCalendar(StartYear, StartMonth, StartDay)).getTimeInMillis();
    long endMillis = (new GregorianCalendar(EndYear, EndMonth, EndDay)).getTimeInMillis();
    long currMillis;

    //loop through all assignments
    while ((ns = patrol.readNextAssignment()) != null) {
      currMillis = (new GregorianCalendar(ns.getYear(), ns.getMonth(), ns.getDay())).getTimeInMillis();
//if(debug) System.out.print("start="+startMillis+"end="+endMillis+" curr="+currMillis+" "+ns.getYear()+" "+ns.getMonth()+" "+ns.getDay());
      if (startMillis <= currMillis && currMillis <= endMillis) {
        //loop thru individual assignments on this day
        for (i = 0; i < Assignments.MAX; ++i) {
          //              member = patrol.getMemberByID(ns.getPosID(i));
          member = mapId2MemberData.get(ns.getPosID(i));
//if(debug) System.out.print(ns.getPosID(i) + " ");
          if (member != null && member.okToDisplay(EveryBody, SubList, listAll, classificationsToDisplay, commitmentToDisplay, listDirector, instructorFlags, 0)) {
//if(debug) System.out.print(" ok to display");
//                        String tim = ns.getStartingTimeString();
            // count shifts
            if (showDayCnt && ns.isDayShift()) {
              ++member.AssignmentCount[Assignments.DAY_TYPE];
            }
            else if (showSwingCnt && ns.isSwingShift()) {
              ++member.AssignmentCount[Assignments.SWING_TYPE];
            }
            else if (showNightCnt && ns.isNightShift()) {
              ++member.AssignmentCount[Assignments.NIGHT_TYPE];
            }
            else if (showTrainingCnt && ns.isTrainingShift()) {
              ++member.AssignmentCount[Assignments.TRAINING_TYPE];
            }
          } //end if okToDisplay
        } //end for loop for shift
      } //end test for date
    } //end while loop (all assignments)
  } //readAssignments

  private void debugOut(String s) {
    if (DEBUG) {
      System.out.println("DEBUG: " + s);
    }
  }


  /**
   * convert integer to int
   *
   * @param strNum string (normally 6 digits, may be negative)
   * @return converted number
   */
  private int cvtToInt(String strNum) {
    int num = 0;
    try {
      if (strNum != null) {
        num = Integer.parseInt(strNum);
      }
    }
    catch (Exception e) {
      num = 0;
    }
    return num;
  }

  /**
   * build list of ???
   *
   * @param IDOfEditor patroller ID of the patroller who is doing the editing
   */
  private void BuildLists(String IDOfEditor, SessionData sessionData) {
    patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData);

    readAssignments(patrol); //must read ASSIGNMENT data for other code to work

    patrol.resetRoster();
    emaiPatrollerList = new Vector<String>();
    invalidEmailPatrollerList = new Vector<String>();
    MemberData memberx = patrol.nextMember("&nbsp;");
//	int siz = members.size();
//	int i = 0;
//int xx=0;
    MemberData member;

    while (memberx != null) {
      member = mapId2MemberData.get(memberx.getID());
      if (member != null) {
        debugOut(member.getID() + ": " + member.getEmail());
        if (member.okToDisplay(EveryBody, SubList, listAll, classificationsToDisplay, commitmentToDisplay, listDirector, instructorFlags, MinDays)) {
          String em = member.getEmail();
          //check for valid email
          if (em != null && em.length() > 6 && em.indexOf('@') > 0 && em.indexOf('.') > 0) {
            emaiPatrollerList.add(member.getID());
          }
          else {
            invalidEmailPatrollerList.add(member.getID());
          }
        }
      }
//else System.out.println("NOT OK to display "+member);
      memberx = patrol.nextMember("");
    }
//System.out.println("length of email string = "+ePatrollerList.length());
    MemberData editor = patrol.getMemberByID(IDOfEditor); //ID from cookie
//      patrol.close(); //must close connection!
    isDirector = editor != null && editor.isDirector();
  }

}
