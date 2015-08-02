package org.nsponline.calendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


public class MemberList extends HttpServlet {

  public void doGet(HttpServletRequest request,
                    HttpServletResponse response)
      throws IOException, ServletException {

    response.setContentType("text/html");
    new LocalMemberList(request, response);
  }

  public void doPost(HttpServletRequest request,
                     HttpServletResponse response)
      throws IOException, ServletException {
    doGet(request, response);
  }

  private class LocalMemberList {
    PrintWriter out;
    String szMyID;
    boolean isDirector = false;
    String ePatrollerList = "";
    private String resort;
    private DirectorSettings ds;

    private LocalMemberList(HttpServletRequest request,
                            HttpServletResponse response)
        throws IOException, ServletException {

      response.setContentType("text/html");
      out = response.getWriter();
      SessionData sessionData = new SessionData(request.getSession(), out);
      ds = null;
      CookieID cookie = new CookieID(sessionData, request, response, "MemberList", null);
      if (cookie.error) {
        return;
      }
      resort = request.getParameter("resort");
      szMyID = cookie.getID();
      if (szMyID != null) {
        readData(sessionData, szMyID);
      }

      printTop();
      int count = 0;
      if (PatrolData.validResort(resort)) {
        count = printBody(sessionData);
      }
      else {
        out.println("Invalid host resort.");
      }
      printBottom(count);
    }

    public void readData(SessionData sessionData, String IDOfEditor) {
      PatrolData patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData);
      ds = patrol.readDirectorSettings();

      MemberData member = patrol.nextMember("");
      while (member != null) {
        String em = member.getEmail();
        if (em != null && em.length() > 6 && em.indexOf('@') > 0 && em.indexOf('.') > 0) {
          if (ePatrollerList.length() > 2) {
            ePatrollerList += ",";
          }
          ePatrollerList += member.getEmail();
        }
        member = patrol.nextMember("");
      }

      MemberData editor = patrol.getMemberByID(IDOfEditor); //ID from cookie
      patrol.close(); //must close connection!
      isDirector = editor != null && editor.isDirector();
    }

    public void printTop() {
      out.println("<html><head>");
      out.println("<meta http-equiv=\"Content-Language\" content=\"en-us\">");
      out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=windows-1252\">");
      out.println("<title>" + PatrolData.getResortFullName(resort) + " Ski Patrollers</title>");
      out.println("<META HTTP-EQUIV=\"Pragma\" CONTENT=\"no-cache\">");
      out.println("<META HTTP-EQUIV=\"Expires\" CONTENT=\"-1\">");
      out.println("</head><body>");
      out.println("<script>");
      out.println("function printWindow(){");
      out.println("   bV = parseInt(navigator.appVersion)");
      out.println("   if (bV >= 4) window.print()");
      out.println("}");
      out.println("</script>");

      out.println("<p><Center><h2>List of Members of " + PatrolData.getResortFullName(resort) + " Ski Patrollers</h2></Center></p>");

      if (isDirector || (ds != null && ds.getEmailAll())) {
        //getEmail()
        out.println("<p><Bold>");
        String options = "&BAS=1&INA=1&SR=1&SRA=1&ALM=1&PRO=1&AUX=1&TRA=1&CAN=1&FullTime=1&PartTime=1&Inactive=1&ALL=1"; //default
        String loc = "EmailForm?resort=" + resort + "&ID=" + szMyID + options;
        out.println("<INPUT TYPE=\"button\" VALUE=\"e-mail THESE patrollers\" onClick=window.location=\"" + loc + "\">");
        out.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");

        out.println("<a href=\"javascript:printWindow()\">Print This Page</a></p>");
      }
      out.println("<table  style=\"font-size: 10pt; face=\'Verdana, Arial, Helvetica\' \" border=\"1\" width=\"99%\" bordercolordark=\"#003366\" bordercolorlight=\"#C0C0C0\">");
      out.println(" <tr>");
      out.println("  <td width=\"160\" bgcolor=\"#C0C0C0\"><font face=\"Verdana, Arial, Helvetica\"><font size=\"2\">Name</font></font></td>");
      out.println("  <td width=\"90\" bgcolor=\"#C0C0C0\"><font face=\"Verdana, Arial, Helvetica\"><font size=\"2\">Home</font></font></td>");
      out.println("  <td width=\"83\" bgcolor=\"#C0C0C0\"><font face=\"Verdana, Arial, Helvetica\"><font size=\"2\">Work</font></font></td>");
      out.println("  <td width=\"70\" bgcolor=\"#C0C0C0\"><font face=\"Verdana, Arial, Helvetica\"><font size=\"2\">Cell</font></font></td>");
      out.println("  <td width=\"73\" bgcolor=\"#C0C0C0\"><font face=\"Verdana, Arial, Helvetica\"><font size=\"2\">Pager</font></font></td>");
      out.println("  <td width=\"136\" bgcolor=\"#C0C0C0\"><font face=\"Verdana, Arial, Helvetica\"><font size=\"2\">Email</font></font></td>");
      out.println(" </tr>");
    }

    private void printBottom(int count) {
      out.println("</table>");
      out.println("<br>As of: " + new java.util.Date() + ",  <b>" + count + " members listed.</b>");
      out.println("</body></html>");
    }

    public int printBody(SessionData sessionData) {
      PatrolData patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData);
      MemberData member = patrol.nextMember("&nbsp;");
      int count = 0;
      while (member != null) {
        if (!member.getCommitment().equals("0")) {  //only display if NOT "Inactive"
          ++count;
          member.printMemberListRow(out);
        }
        member = patrol.nextMember("&nbsp;");   // "&nbsp;" is the default string field
      }
      patrol.close(); //must close connection!
      return count;
    }
  }
}
