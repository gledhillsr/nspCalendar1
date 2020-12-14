package org.nsponline.calendar;

import org.nsponline.calendar.misc.*;
import org.nsponline.calendar.store.Roster;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Steve Gledhill
 *         <p/>
 *         1) validate user/password
 *         2) if valid, go to parent page
 *         3) else display login help screen
 */
public class LoginHelp extends nspHttpServlet {

  @SuppressWarnings("FieldCanBeLocal")
  private static boolean DEBUG = true;
  @SuppressWarnings("FieldCanBeLocal")
  private static boolean DEBUG_SENSITIVE = true;

  void servletBody(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

//      PrintWriter out;
      String szParent;

      String id = request.getParameter("ID");
      String pass = request.getParameter("Password");
  //    resort = request.getParameter("resort");
      szParent = request.getParameter("NSPgoto");
//      response.setContentType("text/html");
//      out = response.getWriter();

      sessionData = new SessionData(request, out);
      PatrolData patrolData = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData, LOG); //when reading members, read full data

      if (PatrolData.isValidLogin(out, resort, id, pass, sessionData)) {   //does password match?
        sessionData.setLoggedInUserId(id);
        sessionData.setLoggedInResort(resort);
        if (Utils.isEmpty(szParent)) {
          szParent = "MonthCalendar";
        }
        // String newLoc = BASE_URL + resort + "/index.php?resort=" + resort + "&NSPgoto=" + szParent + "&ID=" + id;
        String newLoc = PatrolData.SERVLET_URL + szParent + "?resort=" + resort + "&ID=" + id;
        debugOut("LoginHelp (valid login) sendRedirect to: " + newLoc);
        response.sendRedirect(newLoc);
        patrolData.close();
        return;
      }

      OuterPage outerPage = new OuterPage(patrolData.getResortInfo(), getJavaScriptAndStyles(), sessionData.getLoggedInUserId());
      outerPage.printResortHeader(out);

      String emailme = request.getParameter("emailme");
//      debugOut("LoginHelp emailme: " + emailme);
      if (emailme != null) {
        String ID2 = request.getParameter("ID2");
        String email = request.getParameter("email");
        if (ID2 != null || email != null) {
          int ret = sendPassword(out, ID2, email, resort, sessionData, patrolData);
          if (ret == 0) {
            out.println("Note: an email of the ID and password was just sent.<br>");
          }
          else if (ret == 1) {
            out.println("Sorry, no member was found.<br>");
          }
          else {
            out.println("Sorry, no valid email was found for this member.<br>");
          }
        }
        else {
          out.println("Error, ID or email required for search.<br>");
        }
      }

      printBody(out, resort, szParent);
      outerPage.printResortFooter(out);
      patrolData.close();
    }

    private String getJavaScriptAndStyles() {
      return "";
    }

    @SuppressWarnings("SameParameterValue")
    private boolean mailto(SessionData sessionData, MailMan mail, Roster mbr, String subject, String message) {
      if (mbr == null) {
        return false;
      }
      String recipient = mbr.getEmailAddress();
      if (recipient != null && recipient.length() > 3 && recipient.indexOf('@') > 0) {
        System.out.print("Sending password email to " + mbr.getFullName() + " at " + recipient);   //no e-mail, JUST LOG IT
//        try {
          mail.sendMessage(sessionData, subject, message, recipient);
//          Log.log("  mail was sucessfull");    //no e-mail, JUST LOG IT
          return true;
//        }
//        catch (MailManException ex) {
//          Log.log("  error " + ex);
//          Log.log("attempting to send mail to: " + recipient);   //no e-mail, JUST LOG IT
//        }
      }
      return false;
    } //end mailto


    @SuppressWarnings("UnusedParameters")
    private int sendPassword(PrintWriter out, String ID, String emailAddress, String resort, SessionData sessionData, PatrolData patrol) {
      Roster member = null;
      if (ID != null && !ID.equalsIgnoreCase(sessionData.getBackDoorUser()) && ID.length() > 4) {
        member = patrol.getMemberByID(ID); //ID from cookie
        if (member != null) {
          emailAddress = member.getEmailAddress();
//out.println("FOUND emailAddress=(" + emailAddress + ")<BR>");
        }
      }
      else if (emailAddress.trim().length() > 4) {
        member = patrol.getMemberByEmail(emailAddress); //ID from cookie
        if (member != null) {
          ID = member.getID();
//out.println("FOUND ID=(" + ID + ")<BR>");
        }
      }
      if (member != null) {
        String password = member.getPassword();
        String fullName = member.getFullName();
        String message = fullName + "\n\nYou have requested your login information for your resort " + resort + " on the online scheduling web site.\n\n" +
            "Your patrol ID is: " + ID + "\n" +
            "Your password is: " + password + "\n\n" +
            "Please change this password ASAP (since it was sent in an email).\n\n" +
            "If you continue to have problems, please contact your director.\n\nThanks.";
        MailMan mail = new MailMan(sessionData.getSmtpHost(), emailAddress, fullName, sessionData);
        if (mailto(sessionData, mail, member, "Here is your password you requested", message)) {
          return 0; //mail was sent
        }
        else {
          return -1; //invalid emailAddress
        }
      }
      return 1; //member not found
    }

    public void printTop(PrintWriter out) {
      out.println("<html>");
      out.println("<head>");
      out.println("<meta http-equiv=\"Content-Language\" content=\"en-us\">");
      out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=windows-1252\">");
      out.println("<title>Login Help</title>");
      out.println("<META HTTP-EQUIV=\"Pragma\" CONTENT=\"no-cache\">");
      out.println("<META HTTP-EQUIV=\"Expires\" CONTENT=\"-1\">");
      out.println("</head>");
      out.println("<body>");
    }

    private void printBody(PrintWriter out, String resort, String szParent) {

      out.println("<p>");
      out.println("&nbsp;");
      out.println("<font face=verdana, arial size=2 color=black>");
      out.println("<b>" + PatrolData.getResortFullName(resort) + " Login Help</b><p>");
      out.println("Please enter the following information.  If we find match in the member database, you'll get instant access.<br><b>If you have assigned yourself a password, your Last name will no longer work.</b><p>");
      out.println("<form target='_self' action=" + PatrolData.SERVLET_URL + "LoginHelp method=post>");
      out.println("<INPUT TYPE=\"HIDDEN\" NAME='NSPgoto' VALUE=\"" + szParent + "\">\n");
      out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"resort\" VALUE=\"" + resort + "\">\n");

      out.println("<table width=349 style=\"border-collapse: collapse\"  bgcolor=\"#EFEFEF\">\n");
      out.println("  <tr>\n");
      out.println("   <td align=right width=210><font face=verdana, arial size=2 color=black>Member ID: </font></td>\n");
      out.println("   <td width=229 ><INPUT type=\"text\" id=ID name=ID size=20></td>\n");
      out.println("  </tr>\n");
      out.println("  <tr>\n");
      out.println("   <td align=right width=210><font face=verdana, arial size=2 color=black>Last&nbsp;Name&nbsp;or&nbsp;password:</font></td>\n");
      out.println("   <td width=229>\n");
      out.println("     <font face=verdana, arial size=2 color=black>\n");
      out.println("     <INPUT type=\"password\" name=Password size=20></font>\n");
      out.println("  </td>\n");
      out.println("  </tr>\n");
      out.println("  <tr>\n");
      out.println("   <td align=center width=439 colspan=2>\n");
      out.println("       <font face=verdana, arial size=2 color=black>&nbsp;\n");
      out.println("       <INPUT type=\"submit\" value=\"Login\" name=login></a></font></td>\n");
      out.println("  </tr>\n");
      out.println("</table>\n");
      out.println("<p>&nbsp;</p>\n");

      out.println("<table border=0 cellpadding=2 style=\"border-collapse: collapse\" bordercolor=\"#111111\" width=450  bgcolor=\"#EFEFEF\">\n");
      out.println("  <tr>\n");
      out.println("     <td align=left width=120 rowspan=3 bgcolor=\"#FFFFFF\">\n");
      out.println("<font face=verdana, arial size=2 color=black>\n");
      out.println("  <b>If you forgot your password:</b></font></td>\n");
      out.println("     <td align=right width=153 bgcolor=\"#C0F0B5\"><font face=verdana, arial size=2 color=black>Member ID:  </font> </td>\n");
      out.println("  <td width=167 bgcolor=\"#C0F0B5\" ><INPUT type=\"text\" name=ID2 size=20></td>\n");
      out.println("     </tr>\n");
      out.println("     <tr>\n");
      out.println("  <td align=right width=153 bgcolor=\"#C0F0B5\"><font size=2>-or-&nbsp; e-mail address</font><font face=verdana, arial size=2 color=black>:</font></td>\n");
      out.println("   <td width=167 bgcolor=\"#C0F0B5\"><INPUT type=\"text\" name=email size=20> </a></td>\n");
      out.println("  </tr>\n");
      out.println("  <tr>\n");
      out.println("   <td width=293 colspan=2 align=\"center\" bgcolor=\"#C0F0B5\">\n");
      out.println("     <font face=verdana, arial size=2 color=black>\n");
      out.println("     <INPUT type=\"submit\" value=\"email me my password\" name=emailme></font></td>\n");
      out.println("     </tr>\n");
      out.println("</table>\n");

      out.println("</form>");
      out.println("<font face=verdana, arial size=2 color=black>");
      out.println("<p><b>If you did have a password, but forgot it</b>, Oops. I have not yet implemented the e-mail you your password function.  ");
      out.println("Please try to get if from your Director (then can look it up).  Otherwise email me at Steve@Gledhills.com, and tell me your resort and your patroller ID, ");
      out.println("then I will email you back your password (Sorry for the inconvience).</p>");
      out.println("For help on locating your NSP Member ID Number, check the examples below of your NSP Membership Card, your OEC Card and the mailing label on your issues of <i>Ski Patrol Magazine</i>");
      out.println("</font>");
      out.println("<table>");
      out.println("<tr>");
      out.println("<td align=center>");
      out.println("<img src=http://nsponline.org/images/MembershipCard.jpg alt=\"NSP Membership Card\" border=0 width=\"210\" height=\"138\">");
      out.println("</td>");
      out.println("<td align=center>");
      out.println("<img src=http://nsponline.org/images/OECCard.jpg alt=\"NSP Membership Card.\" width=\"210\" height=\"148\">");
      out.println("</td>");
      out.println("</tr>");
      out.println("<tr>");
      out.println("<td align=center>");
      out.println("<img src=http://nsponline.org/images/label.jpg alt=\"Mailing Label From SPM\" width=\"210\" height=\"93\"><br>");
      out.println("</td>");
      out.println("</tr>");
      out.println("</table>");
      out.println("</font>");
    }

    private void debugOut(String str) {
      if (DEBUG) {
        Logger.printToLogFileStatic(sessionData.getRequest(), sessionData.getLoggedInResort(), str);
      }
    }

    @SuppressWarnings("unused")
    private void debugSensitiveOut(String str) {
      if (DEBUG_SENSITIVE) {
        Logger.printToLogFileStatic(sessionData.getRequest(), sessionData.getLoggedInResort(), str);
      }
    }

  @Override
  Class<?> getServletClass() {
    return this.getClass();
  }

  String getParentIfBadCredentials() {
    return null;
  }
}
