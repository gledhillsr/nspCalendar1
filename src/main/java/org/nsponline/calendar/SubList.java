package org.nsponline.calendar;

import org.nsponline.calendar.misc.*;
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
 * List patrollers who have selected "available as a substitute" on their preferences
 * With a button to email this entire list, or links on each patrollers email address
 */
public class SubList extends HttpServlet {
  private static Logger LOG = new Logger(SubList.class);

  static final boolean DEBUG = false;

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    LOG.logRequestParameters("GET", request);
    new InnerSubList(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    LOG.logRequestParameters("POST", request);
    doGet(request, response);
  }

  private class InnerSubList {
    private Logger LOG;
    PrintWriter out;
    private String resort;
    PatrolData patrol;
    boolean isDirector;

    InnerSubList(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      response.setContentType("text/html");
      out = response.getWriter();
      SessionData sessionData = new SessionData(request, out);
      ValidateCredentials credentials = new ValidateCredentials(sessionData, request, response, "SubList", LOG);
      if (credentials.hasInvalidCredentials()) {
        return;
      }
      //by now, sessionData.getLoggedInUserId and sessionData.getLoggedInResort are valid
      resort = sessionData.getLoggedInResort();
      String IDOfEditor = sessionData.getLoggedInUserId();
      isDirector = false;
      patrol = null;
      patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData, LOG);
      Roster editor = patrol.getMemberByID(IDOfEditor);
      if (editor != null) {
        isDirector = editor.isDirector();
      }

      OuterPage outerPage = new OuterPage(patrol.getResortInfo(), "", sessionData.getLoggedInUserId());
      outerPage.printResortHeader(out);
      printStartOfTable(IDOfEditor);
      printBody();
      printEndOfTable();
      patrol.close(); //must close connection!
      debugOut("ending SubList");
      outerPage.printResortFooter(out);
    }

    @SuppressWarnings("StringConcatenationInLoop")
    void printStartOfTable(String IDOfEditor) {
      out.println("<h2>Substitute List for " + PatrolData.getResortFullName(resort) + "</h2>");
//          if(isDirector || (ds != null && !ds.getEmailAll()))
      {
//      //getEmailAddress()
        String ePatrollerList = "";
        ResultSet rosterResults = patrol.resetRoster();
        Roster member = patrol.nextMember("", rosterResults);
        while (member != null) {
          String em = member.getEmailAddress();
          if (member.getSub() == null || (!member.getSub().startsWith("y") && !member.getSub().startsWith("Y"))) {
            em = null;
          }
          if (em != null && em.length() > 6 && em.indexOf('@') > 0 && em.indexOf('.') > 0) {
            if (ePatrollerList.length() > 2) {
              ePatrollerList += ",";
            }
            ePatrollerList += member.getEmailAddress();
          }
          member = patrol.nextMember("", rosterResults);
        }

        out.println("<p><Bold>");
        //todo srg opens a new window.location  ;-(  fix me
        String options = "&SubList=1"; //show abbreviated list
        String loc = "EmailForm?resort=" + resort + "&ID=" + IDOfEditor + options;
        out.println("<INPUT TYPE='button' VALUE='e-mail THESE patrollers' onClick=window.location='" + loc + "'>");
      }
      out.println("    <table style='font-size: 10pt; face=\'Verdana, Arial, Helvetica\' ' border='1' width='99%' bordercolordark='#003366' bordercolorlight='#C0C0C0'>");
      out.println("        <tr>");
      out.println("          <td width='148' bgcolor='#C0C0C0'><font face='Verdana, Arial, Helvetica' size='2'>Name</font></td>");
      out.println("          <td width='90'  bgcolor='#C0C0C0'><font face='Verdana, Arial, Helvetica' size='2'>Home</font></td>");
      out.println("          <td width='83'  bgcolor='#C0C0C0'><font face='Verdana, Arial, Helvetica' size='2'>Work</font></td>");
      out.println("          <td width='70'  bgcolor='#C0C0C0'><font face='Verdana, Arial, Helvetica' size='2'>Cell</font></td>");
      out.println("          <td width='73'  bgcolor='#C0C0C0'><font face='Verdana, Arial, Helvetica' size='2'>Pager</font></td>");
      out.println("          <td width='136' bgcolor='#C0C0C0'><font face='Verdana, Arial, Helvetica' size='2'>Email</font></td>");
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

    private void printEndOfTable() {
      out.println("</table>");
      out.println("<br>As of: " + new java.util.Date());
    }

    public void printBody() {
      ResultSet rosterResults = patrol.resetRoster();
      Roster member = patrol.nextMember("&nbsp;", rosterResults);

      while (member != null) {

        if (Utils.isValidEmailAddress(member.getEmailAddress())) {
          member.setEmail("<a href='mailto:" + member.getEmailAddress() + "'>" + member.getEmailAddress() + "</a>");
        }

        if (member.getSub() != null && (member.getSub().startsWith("y") || member.getSub().startsWith("Y"))) {
          printRow(member.getFullName_lastNameFirst(), member.getHomePhone(), member.getWorkPhone(), member.getCellPhone(), member.getPager(), member.getEmailAddress());
        }
        member = patrol.nextMember("&nbsp;", rosterResults);   // "&nbsp;" is the default string field
      }
    }

    @SuppressWarnings("SameParameterValue")
    private void debugOut(String str) {
      if (DEBUG) {
        Logger.log("DEBUG-SubList(" + resort + "): " + str);
      }
    }
  }
}

