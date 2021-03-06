package org.nsponline.calendar.resources;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.nsponline.calendar.utils.Logger;
import org.nsponline.calendar.utils.PatrolData;
import org.nsponline.calendar.utils.StaticUtils;

/**
 * Display screen to input the user id & password
 * POST the userId/password to LoginHelp (resort is HIDDEN)
 *      password is not encrypted yet
 */
public class OuterMemberLogin extends ResourceBase {

  public OuterMemberLogin(final HttpServletRequest request, final HttpServletResponse response, Logger LOG) throws IOException {
    super(request, response, LOG);
    if (!initBase(response)) {
      return; //"resort" not found, or requestFromBot
    }

    String szParent = request.getParameter("NSPgoto");
    debugOut("resort=" + resort + ", szParent=" + szParent);
    if (StaticUtils.isEmpty(szParent)) {
      debugOut("ERROR, szParent was not specified");
      szParent = "MonthCalendar";
    }
    if (StaticUtils.isEmpty(resort) || !PatrolData.isValidResort(resort)) {
      out.println("ERROR, unknown resort (" + resort + "), go back to www.nsponline.org, and click on your resort<br/>");
      out.println("If you see this problem again, please email me at steve@gledhills.com with a quick " +
                    "description of what you did to see this error. and I will fix it ASAP!!!");
      return;
    }
//    PatrolData patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData, LOG); //when reading members, read full data

    printCommonHeader();
    // printJavaScript(out, resort, szParent);
    printTop(out, resort);
    printMiddle(out, szParent, resort);
    printCommonFooter();
  }

//  private String getJavaScriptAndStyles() {
//    return "";
//  }

  @SuppressWarnings("squid:S2068") // ignore hard coded password violation - needed for Sample resort
  private void printTop(PrintWriter out, String resort) {
    out.println("<p><H2>" + PatrolData.getResortFullName(resort, LOG) + " Ski Patrol Login</H2></p>");
    out.println("<p>");
    out.println("<p>&nbsp;<table border='0' width='500' cellspacing='4' cellpadding='0'>");
    out.println("  <tr>");
    if (resort.equalsIgnoreCase("Sample")) {
      out.println("    <td width='75%'>To try <b>DEMO</b> mode, login as a Director, or a Normal Patroller<br>");
      out.println("&nbsp;&nbsp;&nbsp;&nbsp;Director  id='<b>123456</b>' password='<b>password</b>'<br>");
      out.println("&nbsp;&nbsp;&nbsp;&nbsp;Patroller id='<b>111111</b>' password='<b>password</b>'<br><br>");
      out.println("Everything is enabled, but no email notifications will be sent.");
    }
    //      else {
    //        out.println("    <td width='75%'>If you have not yet logged in, your <b>last name</b> is your password");
    //      }
    out.println("    </td>");
    out.println("  </tr>");
    out.println("  <tr>");
    out.println("    <td width='75%' valign=top>");
    out.println("<p>");

  }

  private void printMiddle(PrintWriter out, String szParent, String resort) {
    out.println("&nbsp;");
    out.println("<form target='_self' method=post action='" + PatrolData.SERVLET_URL + "LoginHelp'>");
    out.println("<INPUT TYPE='HIDDEN' NAME='NSPgoto' VALUE='" + szParent + "'>");
    out.println("<INPUT TYPE='HIDDEN' NAME='resort' VALUE='" + resort + "'>");

    out.println("  <TABLE WIDTH=450 BGCOLOR=#ABABAB ALIGN=center BORDER=0 CELLSPACING=1 CELLPADDING=5>");
    out.println("   <TR>");
    out.println("       <TD>");
    out.println("       <P>&nbsp;<TABLE WIDTH='75%' BGCOLOR=#ABABAB align=center BORDER=0 CELLSPACING=1 CELLPADDING=1>");
    out.println("   <TR>");
    out.println("       <TD><B><font face=verdana, size =2 color=white arial>Member ID Number:</font></B></TD>");
    out.println("       <TD>");
    out.println("       <INPUT id=ID name=ID> ");
    out.println("        </TD>");
    out.println("   </TR>");
    out.println("   <TR>");
    out.println("       <TD><B><font face=verdana, size =2 color=white arial>Password:</font></B></TD>");
    out.println("       <TD>");
    out.println("       <INPUT type='password' id=Password name=Password>");
    out.println("       <INPUT type='submit' value='Login' id=submit1 name=submit1>");
    out.println("          </a></TD>");
    out.println("   </TR>");
    out.println("   <TR>");
    out.println("       <TD colspan=2 align=middle>");
    out.println("       <font face=verdana, size =2 arial >");
    out.println("        <A href='" + PatrolData.SERVLET_URL + "LoginHelp?resort=" + resort + "&NSPgoto=" + szParent + "' target='_self'>Login Help</a></font>");
    out.println("        </TD>");
    out.println("   </TR>");
    out.println("   <TR>");
    out.println("       <TD colspan=2 align=middle>");
    out.println("   </TR>");
    out.println("  </TABLE>");
    out.println("</FORM>");
    out.println("</P></TD>");
    out.println("   </TR>");
    out.println("</TABLE>");
    out.println("</P></td>");
    out.println("   </tr>");
    out.println("</table>");
  }

  private void debugOut(String str) {
    LOG.debug(str);
  }
}
