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

  /**
   * Respond to a GET request for the content produced by
   * this servlet.
   *
   * @param request  The servlet request we are processing
   * @param response The servlet response we are producing
   * @throws IOException      if an input/output error occurs
   * @throws ServletException if a servlet error occurs
   */
  public void doGet(HttpServletRequest request,
                    HttpServletResponse response)
      throws IOException, ServletException {
    new MemberLoginInternal(request, response);
  }

  public void doPost(HttpServletRequest request,
                     HttpServletResponse response)
      throws IOException, ServletException {
    doGet(request, response);
  }


  private class MemberLoginInternal {
    //no instance data..

    public MemberLoginInternal(HttpServletRequest request,
                               HttpServletResponse response)
        throws IOException, ServletException {
      PrintWriter out;
      String szParent;
      String resort;

      response.setContentType("text/html");
      out = response.getWriter();

      SessionData sessionData = new SessionData(getServletContext(), out);

//no default cookie, since other login attempts start here
//        Cookie cookie = new Cookie("NSPgoto", "UpdateInfo");
//      cookie.setMaxAge(60*30); //default is -1, indicating cookie is for current session only
//        response.addCookie(cookie);

      resort = request.getParameter("resort");
//System.out.println("memberLogin:resort="+resort);
      szParent = request.getParameter(CookieID.NSP_goto);
//System.out.println("memberLogin:szParent="+szParent);
      printTop(out, resort);
      if (PatrolData.validResort(resort)) {
        printMiddle(out, szParent, resort);
      }
      else {
        out.println("Invalid host resort.");
      }
      printBottom(out);
    }

    public void printTop(PrintWriter out, String resort) {
      out.println("<html>");
      out.println("<head>");
      out.println("<meta http-equiv=\"Content-Language\" content=\"en-us\">");
      out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=windows-1252\">");
      out.println("<title>Member Login</title>");
      out.println("<META HTTP-EQUIV=\"Pragma\" CONTENT=\"no-cache\">");
      out.println("<META HTTP-EQUIV=\"Expires\" CONTENT=\"-1\">");
      out.println("</head>");
      out.println("<body>");
      out.println("<p><H2>" + PatrolData.getResortFullName(resort) + " Ski Patrol Login</H2></p>");
      out.println("<p>");
      out.println("<p>&nbsp;<table border=\"0\" width=\"500\" cellspacing=\"4\" cellpadding=\"0\">");
      out.println("  <tr>");
      if (resort.equalsIgnoreCase("Sample")) {
        out.println("    <td width=\"75%\">To try <b>DEMO</b> mode, login as a Director, or a Normal Patroller<br>");
        out.println("&nbsp;&nbsp;&nbsp;&nbsp;Director  id='<b>123456</b>' password='<b>password</b>'<br>");
        out.println("&nbsp;&nbsp;&nbsp;&nbsp;Patroller id='<b>111111</b>' password='<b>password</b>'<br><br>");
        out.println("Everything is enabled, but no email notifications will be sent.");
      }
      else {
        out.println("    <td width=\"75%\">If you have not yet logged in, your <b>last name</b> is your password");
      }
      out.println("    </td>");
      out.println("  </tr>");
      out.println("  <tr>");
      out.println("    <td width=\"75%\" valign=top>");
      out.println("<p>");

    }

    private void printMiddle(PrintWriter out, String szParent, String resort) {
      out.println("&nbsp;");
      out.println("<form method=post action=\"" + PatrolData.SERVLET_URL + "loginHelp\">");
      out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"" + CookieID.NSP_goto + "\" VALUE=\"" + szParent + "\">");
      out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"resort\" VALUE=\"" + resort + "\">");

      out.println("  <TABLE WIDTH=450 BGCOLOR=#ABABAB ALIGN=center BORDER=0 CELLSPACING=1 CELLPADDING=5>");
      out.println("   <TR>");
      out.println("       <TD>");
      out.println("       <P>&nbsp;<TABLE WIDTH=\"75%\" BGCOLOR=#ABABAB align=center BORDER=0 CELLSPACING=1 CELLPADDING=1>");
      out.println("   <TR>");
      out.println("       <TD><B><font face=verdana, size =2 color=white arial>Member ID Number:</font></B></TD>");
      out.println("       <TD>");
      out.println("       <INPUT id=ID name=ID> ");
      out.println("        </TD>");
      out.println("   </TR>");
      out.println("   <TR>");
      out.println("       <TD><B><font face=verdana, size =2 color=white arial>Password:</font></B></TD>");
      out.println("       <TD>");
      out.println("       <INPUT type=\"password\" id=Password name=Password>");
      out.println("       <INPUT type=\"submit\" value=\"Login\" id=submit1 name=submit1>");
      out.println("          </a></TD>");
      out.println("   </TR>");
      out.println("   <TR>");
      out.println("       <TD colspan=2 align=middle>");
      out.println("       <font face=verdana, size =2 arial >");
      out.println("        <A href=\"" + PatrolData.SERVLET_URL + "loginHelp?resort=" + resort + "\">Login Help</a></font>");
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

    private void printBottom(PrintWriter out) {
      out.println("</body>");
      out.println("</html>");
    }
  }
}
