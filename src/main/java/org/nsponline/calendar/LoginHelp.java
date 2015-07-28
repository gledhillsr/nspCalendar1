package org.nsponline.calendar;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.lang.*;
import java.sql.*;

/**
 * @author Steve Gledhill
 */
public class LoginHelp extends HttpServlet {

  public void doGet(HttpServletRequest request,
                    HttpServletResponse response)
      throws IOException, ServletException {
    PrintWriter out;
    String szParent;
    String resort;

    String id = request.getParameter("ID");
    String pass = request.getParameter("Password");
    resort = request.getParameter("resort");
    szParent = request.getParameter(CookieID.NSP_goto);
    response.setContentType("text/html");
    out = response.getWriter();

    SessionData sessionData = new SessionData(getServletContext(), out);
    if (isValidLogin(out, resort, id, pass, sessionData)) {   //does password match?
      try {
        String BASE_URL = "http://nsponline.org/"; //TODO fix me to work with any url.  It was "http://localhost/" bit that does not work
        sessionData.setLoggedInUserId(id);
        String newLoc = BASE_URL + resort + "/index.php?resort=" + resort + "&NSPgoto=" + szParent + "&ID=" + id;
        response.sendRedirect(newLoc);
      }
      catch (Exception ignore) {
      }

    }
    String emailme = request.getParameter("emailme");
    if (emailme != null) {
      String ID2 = request.getParameter("ID2");
      String email = request.getParameter("email");
      if (ID2 != null || email != null) {
        int ret = sendPassword(out, ID2, email, resort, new SessionData(getServletContext(), out));
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

    printTop(out);
    if (PatrolData.validResort(resort)) {
      printMiddle(out, resort, szParent);
    }
    else {
      out.println("Invalid host resort.");
    }
    printBottom(out);
  }

  public void doPost(HttpServletRequest request,
                     HttpServletResponse response)
      throws IOException, ServletException {
    doGet(request, response);
  }

  private boolean mailto(MailMan mail, MemberData mbr, String subject, String message, SessionData sessionData) {
    if (mbr == null) {
      return false;
    }
    String recipient = mbr.getEmail();
    if (recipient != null && recipient.length() > 3 && recipient.indexOf('@') > 0) {
      System.out.print("Sending mail to " + mbr.getFullName() + " at " + recipient);   //no e-mail, JUST LOG IT
      try {
        mail.sendMessage(subject, message, recipient, sessionData);
        System.out.println("  mail was sucessfull");    //no e-mail, JUST LOG IT
        return true;
      }
      catch (MailManException ex) {
        System.out.println("  error " + ex);
        System.out.println("attempting to send mail to: " + recipient);   //no e-mail, JUST LOG IT
      }
    }
    return false;
  } //end mailto


  @SuppressWarnings("UnusedParameters")
  public int sendPassword(PrintWriter out, String ID, String email, String resort, SessionData sessionData) {
    PatrolData patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData);
    MemberData member = null;
    if (ID != null && !ID.equalsIgnoreCase(PatrolData.backDoorUser) && ID.length() > 4) {
      member = patrol.getMemberByID(ID); //ID from cookie
      if (member != null) {
        email = member.getEmail();
//out.println("FOUND email=(" + email + ")<BR>");
      }
    }
    else if (email.trim().length() > 4) {
      member = patrol.getMemberByEmail(email); //ID from cookie
      if (member != null) {
        ID = member.getID();
//out.println("FOUND ID=(" + ID + ")<BR>");
      }
    }
    if (member != null) {
      String password = member.getPassword();
      String fullName = member.getFullName();
      String message = fullName + "\n\nYou have requested your login information for your patrol web site.\n\n" +
          "Your patrol ID is: " + ID + "\n" +
          "Your password is: " + password + "\n\n" +
          "If you continue to have problems, please contact your director.\n\nThanks.";
      @SuppressWarnings("UnnecessaryLocalVariable")
      String from = email;
      MailMan mail = new MailMan(sessionData.getSmtpHost(), from, fullName, sessionData);
      if (mailto(mail, member, "Here is your password you requested", message, sessionData)) {
        return 0; //mail was sent
      }
      else {
        return -1; //invalid email
      }
    }
    return 1; //member not found
  }

  private boolean isValidLogin(PrintWriter out, String resort, String ID, String pass, SessionData sessionData) {
    boolean validLogin = false;
    ResultSet rs;
//System.out.println("LoginHelp: isValidLogin("+resort + ", "+ID+", "+pass+")");
    if (ID == null || pass == null) {
      return false;
    }

    try {
//System.out.println("LoginHelp: loading JData driver");
      //noinspection unused
      Driver drv = (Driver) Class.forName(PatrolData.JDBC_DRIVER).newInstance();
//System.out.println("LoginHelp: driver ="+drv);
//System.out.println("LoginHelp: driver loaded");
    }
    catch (Exception e) {
      out.println("LoginHelp: Cannot find mysql driver, reason:" + e.toString());
      return false;
    }

    if (ID.equalsIgnoreCase(PatrolData.backDoorUser) && pass.equalsIgnoreCase(PatrolData.backDoorPass)) {
//System.out.println("Back Door login in LoginHelp");
//        Cookie cookie = new Cookie(CookieID.NSP_fullname, "Steve Gledhill");
//        cookie.setMaxAge(-1); //default is -1, indicating cookie is for current session only
//        response.addCookie(cookie);
      return true;
    }    // Try to connect to the database
    try {
      // Change MyDSN, myUsername and myPassword to your specific DSN
      Connection c = PatrolData.getConnection(resort, sessionData);
      String szQuery = "SELECT * FROM roster WHERE IDNumber = \"" + ID + "\"";
      PreparedStatement sRost = c.prepareStatement(szQuery);
      if (sRost == null) {
        return false;
      }

      rs = sRost.executeQuery();

      if (rs != null && rs.next()) {      //will only loop 1 time

        String originalPassword = rs.getString("password");
        String lastName = rs.getString("LastName");
        String firstName = rs.getString("FirstName");

        boolean hasPassword = (originalPassword != null && originalPassword.length() > 0);
        if (hasPassword) {
          if (originalPassword.equalsIgnoreCase(pass)) {
            validLogin = true;
          }
        }
        else {
          //if there is NO password, then test if the password equals the last name
//          if(lastName == pass) //name was passed on the command line as a quick password
//              validLogin = true;
          if (lastName.equalsIgnoreCase(pass)) {
            validLogin = true;
          }
        }
//only check for last name as a login, UNTIL they get a password

        if (validLogin) {
//            Cookie cookie = new Cookie(CookieID.NSP_fullname, firstName + " " + lastName);
          java.util.Date trialTime = new java.util.Date();
          //Login: Sun Jul 26 23:31:26 UTC 2015, Steve Gledhill, 192443 (Brighton)
          System.out.println("Login: " + trialTime + ", " + firstName + " " + lastName + ", " + ID + " (" + resort + ")");
//            cookie.setMaxAge(-1); //default is -1, indicating cookie is for current session only
//            response.addCookie(cookie);

        }
      } //end if

      c.close();
//System.out.println ("loginhelp closed static connection for " + resort + " at " + PatrolData.getCurrentDateTimeString());
    }
    catch (Exception e) {
      out.println("Error connecting or reading table:" + e.getMessage()); //message on browser
      System.out.println("in LoginHelp.  ID of editor:" + ID);
    } //end try
//System.out.println("<br>valid="+valid);
//out.println("<br>");

    return validLogin;
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

  private void printMiddle(PrintWriter out, String resort, String szParent) {

    out.println("<p>");
    out.println("&nbsp;");
    out.println("<font face=verdana, arial size=2 color=black>");
    out.println("<b>" + PatrolData.getResortFullName(resort) + " Login Help</b><p>");
    out.println("Please enter the following information.  If we find match in the member database, you'll get instant access.<br><b>If you have assigned yourself a password, your Last name will no longer work.</b><p>");
    out.println("<form action=" + PatrolData.SERVLET_URL + "LoginHelp method=post>");
    out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"" + CookieID.NSP_goto + "\" VALUE=\"" + szParent + "\">\n");
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
    out.println("<img src=images/MembershipCard.jpg alt=\"NSP Membership Card\" border=0 width=\"210\" height=\"138\">");
    out.println("</td>");
    out.println("<td align=center>");
    out.println("<img src=images/OECCard.jpg alt=\"NSP Membership Card.\" width=\"210\" height=\"148\">");
    out.println("</td>");
    out.println("</tr>");
    out.println("<tr>");
    out.println("<td align=center>");
    out.println("<img src=images/label.jpg alt=\"Mailing Label From SPM\" width=\"210\" height=\"93\"><br>");
    out.println("</td>");
    out.println("</tr>");
    out.println("</table>");
    out.println("</font>");
  }

  private void printBottom(PrintWriter out) {
    out.println("</body>");
    out.println("</html>");
  }

}
