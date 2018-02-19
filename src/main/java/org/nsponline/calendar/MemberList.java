package org.nsponline.calendar;

import org.nsponline.calendar.misc.*;
import org.nsponline.calendar.store.DirectorSettings;
import org.nsponline.calendar.store.Roster;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;

/**
 * @author Steve Gledhill
 *
 * List all patrollers who are not marked as "inactive"
 */
public class MemberList extends HttpServlet {
  private static Logger LOG = new Logger(MemberList.class);
  private static final boolean DEBUG = false;

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    LOG.logRequestParameters("GET", request);
    new LocalMemberList(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    doGet(request, response);
  }

  private final class LocalMemberList {
    private Logger LOG;

    private PrintWriter out;
    private String patrollerId;
    private boolean isDirector = false;
    private String ePatrollerList = "";
    private String resort;
    private DirectorSettings ds;

    private LocalMemberList(HttpServletRequest request,
                            HttpServletResponse response)
        throws IOException, ServletException {

      debugOut("inside MemberList");
      response.setContentType("text/html");
      out = response.getWriter();
      SessionData sessionData = new SessionData(request, out);
      ValidateCredentials credentials = new ValidateCredentials(sessionData, request, response, "MemberList", LOG);
      if (credentials.hasInvalidCredentials()) {
        return;
      }
      //by now, sessionData.getID and sessionData.getLoggedInResort are valid
      ds = null;
      resort = sessionData.getLoggedInResort();
      patrollerId = sessionData.getLoggedInUserId();
      PatrolData patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData, LOG);
      readData(sessionData, patrollerId);

      OuterPage outerPage = new OuterPage(patrol.getResortInfo(), "", sessionData.getLoggedInUserId());
      outerPage.printResortHeader(out);
      printTop();
      int count = printBody(sessionData);
      printBottom(count);
      outerPage.printResortFooter(out);
    }

    @SuppressWarnings("StringConcatenationInLoop")
    private void readData(SessionData sessionData, String iDOfEditor) {
      PatrolData patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData, LOG);
      ResultSet rosterResults = patrol.resetRoster();
      ds = patrol.readDirectorSettings();

      Roster member = patrol.nextMember("", rosterResults);
      while (member != null) {
        String emailAddress = member.getEmailAddress();
        if (Utils.isValidEmailAddress(emailAddress)) {
          if (ePatrollerList.length() > 2) {
            ePatrollerList += ",";
          }
          ePatrollerList += member.getEmailAddress();
        }
        member = patrol.nextMember("", rosterResults);
      }

      Roster editor = patrol.getMemberByID(iDOfEditor); //ID from cookie
      patrol.close(); //must close connection!
      isDirector = editor != null && editor.isDirector();
    }

    private void printTop() {
      out.println("<script>");
      out.println("function printWindow(){");
      out.println("   bV = parseInt(navigator.appVersion)");
      out.println("   if (bV >= 4) window.print()");
      out.println("}");
      out.println("</script>");

      out.println("<p><Center><h2>List of Members of " + PatrolData.getResortFullName(resort) + " Ski Patrollers</h2></Center></p>");

      if (isDirector || (ds != null && ds.getEmailAll())) {
        //getEmailAddress()
        out.println("<p><Bold>");
        String options = "&BAS=1&INA=1&SR=1&SRA=1&ALM=1&PRO=1&AUX=1&TRA=1&CAN=1&FullTime=1&PartTime=1&Inactive=1&ALL=1"; //default
        String loc = "EmailForm?resort=" + resort + "&ID=" + patrollerId + options;
        out.println("<INPUT TYPE=\"button\" VALUE=\"e-mail THESE patrollers\" onClick=window.location=\"" + loc + "\">");
        out.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");

//        out.println("<a href=\"javascript:printWindow()\">Print This Page</a></p>");
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
    }

    private int printBody(SessionData sessionData) {
      PatrolData patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData, LOG);
      ResultSet rosterResults = patrol.resetRoster();
      Roster member = patrol.nextMember("&nbsp;", rosterResults);
      int count = 0;
      while (member != null) {
        if (!member.getCommitment().equals("0")) {  //only display if NOT "Inactive"
          ++count;
          member.printMemberListRow(out);
        }
        member = patrol.nextMember("&nbsp;", rosterResults);   // "&nbsp;" is the default string field
      }
      patrol.close(); //must close connection!
      return count;
    }

    @SuppressWarnings("SameParameterValue")
    private void debugOut(String str) {
      if (DEBUG) {
        Logger.log("DEBUG-SubList(" + resort + "): " + str);
      }
    }
  }
}
