package org.nsponline.calendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


public class SubList extends HttpServlet {

  PrintWriter out;
  private String resort;
  PatrolData patrol;
  boolean isDirector;

  //------------
// doGet
//------------
  public void doGet(HttpServletRequest request,
                    HttpServletResponse response)
      throws IOException, ServletException {
    response.setContentType("text/html");
    synchronized (this) {
      out = response.getWriter();
      SessionData sessionData = new SessionData(getServletContext(), out);
      System.out.println("starting SubList");
      out = response.getWriter();
      CookieID cookie = new CookieID(sessionData, request, response, "SubList", null);
      resort = request.getParameter("resort");
      String IDOfEditor = cookie.getID();
//      DirectorSettings ds;
      isDirector = false;
      patrol = null;
      if (PatrolData.validResort(resort)) {
        patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData);
//        ds = patrol.readDirectorSettings();
        MemberData editor = patrol.getMemberByID(IDOfEditor); //ID from cookie
        if (editor != null) {
          isDirector = editor.isDirector();
        }
      }

      printTop(IDOfEditor);
      if (patrol != null) {
        printBody();
      }
      else {
        out.println("Invalid host resort.");
      }
      printBottom();
      if (patrol != null) {
        patrol.close(); //must close connection!
      }
      System.out.println("ending SubList");
    } //end Syncronized
  }

  //------------
// doPost
//------------
  public void doPost(HttpServletRequest request,
                     HttpServletResponse response)
      throws IOException, ServletException {
    doGet(request, response);
  }

  @Override
  public String getServletInfo() {
    return "Create a Subsitute List";
  }

  public void printTop(String IDOfEditor) {
    out.println("<html><head>");
    out.println("<meta http-equiv=\"Content-Language\" content=\"en-us\">");
    out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=windows-1252\">");
    out.println("<title>Substitute List</title>");
    out.println("<META HTTP-EQUIV=\"Pragma\" CONTENT=\"no-cache\">");
    out.println("<META HTTP-EQUIV=\"Expires\" CONTENT=\"-1\">");
    out.println("</head><body>");

    out.println("<script>");
    out.println("function printWindow(){");
    out.println("   bV = parseInt(navigator.appVersion)");
    out.println("   if (bV >= 4) window.print()");
    out.println("}");
    out.println("</script>");

    out.println("<h2>Substitute List for " + PatrolData.getResortFullName(resort) + "</h2>");
//          if(isDirector || (ds != null && !ds.getEmailAll()))
    {
//      //getEmail()
      String ePatrollerList = "";
      patrol.resetRoster();
      MemberData member = patrol.nextMember("");
      while (member != null) {
        String em = member.getEmail();
        if (member.getSub() != null && (member.getSub().startsWith("y") || member.getSub().startsWith("Y"))) {
        }
        else {
          em = null;
        }
        if (em != null && em.length() > 6 && em.indexOf('@') > 0 && em.indexOf('.') > 0) {
          if (ePatrollerList.length() > 2) {
            ePatrollerList += ",";
          }
          ePatrollerList += member.getEmail();
        }
        member = patrol.nextMember("");
      }

      out.println("<p><Bold>");
//          out.println("<a href=\"mailto:"+ePatrollerList+"\">");
//          if(resort.equals("Sample")) {
//              out.println("(email THESE patrollers disabled for Demo resort)&nbsp;&nbsp;&nbsp;");
//          } else {
      String options = "&SubList=1"; //default
      String loc = "EmailForm?resort=" + resort + "&ID=" + IDOfEditor + options;
      out.println("<INPUT TYPE=\"button\" VALUE=\"e-mail THESE patrollers\" onClick=window.location=\"" + loc + "\">");
      out.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
//          }
      out.println("<a href=\"javascript:printWindow()\">Print This Page</a></font></p>");
    }
    out.println("    <table style=\"font-size: 10pt; face=\'Verdana, Arial, Helvetica\' \" border=\"1\" width=\"99%\" bordercolordark=\"#003366\" bordercolorlight=\"#C0C0C0\">");
    out.println("        <tr>");
    out.println("          <td width=\"148\" bgcolor=\"#C0C0C0\"><font face=\"Verdana, Arial, Helvetica\" size=\"2\">Name</font></td>");
    out.println("          <td width=\"90\"  bgcolor=\"#C0C0C0\"><font face=\"Verdana, Arial, Helvetica\" size=\"2\">Home</font></td>");
    out.println("          <td width=\"83\"  bgcolor=\"#C0C0C0\"><font face=\"Verdana, Arial, Helvetica\" size=\"2\">Work</font></td>");
    out.println("          <td width=\"70\"  bgcolor=\"#C0C0C0\"><font face=\"Verdana, Arial, Helvetica\" size=\"2\">Cell</font></td>");
    out.println("          <td width=\"73\"  bgcolor=\"#C0C0C0\"><font face=\"Verdana, Arial, Helvetica\" size=\"2\">Pager</font></td>");
    out.println("          <td width=\"136\" bgcolor=\"#C0C0C0\"><font face=\"Verdana, Arial, Helvetica\" size=\"2\">Email</font></td>");
    out.println("        </tr>");
  }

  private void printRow(String name, String home, String work, String cell, String pager, String email) {
    out.println("<tr>");
    out.println(" <td>" + name + "</td>");
    out.println(" <td>" + home + "</td>");
    out.println(" <td>" + work + "</td>");
    out.println(" <td>" + cell + "</td>");
    out.println(" <td>" + pager + "</td>");
    out.println(" <td>" + email + "</td>");
    out.println("</tr>\n");
  }

  private void printBottom() {
    out.println("</table>");
    out.println("<br>As of: " + new java.util.Date());
    out.println("</body></html>");
  }

  public void printBody() {
    patrol.resetRoster();
    MemberData member = patrol.nextMember("&nbsp;");

    while (member != null) {
      if (member.getEmail() != "&nbsp;") {
        member.setEmail("<a href=\"mailto:" + member.getEmail() + "\">" + member.getEmail() + "</a>");
      }

      if (member.getSub() != null && (member.getSub().startsWith("y") || member.getSub().startsWith("Y"))) {
        printRow(member.getFullName2(), member.getHomePhone(), member.getWorkPhone(), member.getCellPhone(), member.getPager(), member.getEmail());
      }
      member = patrol.nextMember("&nbsp;");   // "&nbsp;" is the default string field
    }
  }
  private class _SubList {

  }
}

