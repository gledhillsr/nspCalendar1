package org.nsponline.calendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Steve Gledhill
 */
public class MemberLogin extends HttpServlet {

  private static final boolean DEBUG = true;

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    Utils.dumpRequestParameters(this.getClass().getSimpleName(), request);
    new MemberLoginInternal(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    Utils.dumpRequestParameters(this.getClass().getSimpleName(), request);
    new MemberLoginInternal(request, response);
  }

  private final class MemberLoginInternal {
    private String resort;

    public MemberLoginInternal(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      PrintWriter out;
      String szParent;

      response.setContentType("text/html");
      out = response.getWriter();

      SessionData sessionData = new SessionData(request.getSession(), out);
      resort = request.getParameter("resort");
      szParent = request.getParameter("NSPgoto");
      debugOut("resort=" + resort + ", szParent=" + szParent);
      if (Utils.isEmpty(szParent)) {
        debugOut("ERROR, szParent was not specified");
        szParent = "MonthCalendar";
      }
      if (Utils.isEmpty(resort) || !PatrolData.validResort(resort)) {
        out.println("ERROR, unknown resort (" + resort + "), go back to www.nsponline.org, and click on your resort<br/>");
        out.println("If you see this problem again, please email me at steve@gledhills.com with a quick " +
            "description of what you did to see this error. and I will fix it ASAP!!!");
        return;
      }
      PatrolData patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData); //when reading members, read full data

      OuterPage outerPage = new OuterPage(patrol.getResortInfo(), getJavaScriptAndStyles(), sessionData.getLoggedInUserId());

      outerPage.printResortHeader(out);
      // printJavaScript(out, resort, szParent);
      printTop(out, resort);
      printMiddle(out, szParent, resort);
      outerPage.printResortFooter(out);
    }

    private String getJavaScriptAndStyles() {
      return "";
    }

    private void printTop(PrintWriter out, String resort) {
      out.println("<p><H2>" + PatrolData.getResortFullName(resort) + " Ski Patrol Login</H2></p>");
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
      if (DEBUG) {
        System.out.println("DEBUG-MemberLogin(" + resort + "): " + str);
      }
    }
  }
}
