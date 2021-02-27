package org.nsponline.calendar.resources;

import java.io.IOException;
import java.sql.ResultSet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.nsponline.calendar.store.Roster;
import org.nsponline.calendar.utils.Logger;
import org.nsponline.calendar.utils.PatrolData;
import org.nsponline.calendar.utils.StaticUtils;

public class OuterSubList extends ResourceBase {
  //class globals

  OuterSubList(HttpServletRequest request, HttpServletResponse response, Logger LOG)  throws IOException {
    super(request, response, LOG);
    if (!initBaseAndAskForValidCredentials(response, "SubList")) {
      return;
    }

    printCommonHeader();
    printStartOfTable(sessionData.getLoggedInUserId());
    printBody();
    printEndOfTable();
    printCommonFooter();

    patrol.close(); //must close connection!
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

      if (StaticUtils.isValidEmailAddress(member.getEmailAddress())) {
        member.setEmail("<a href='mailto:" + member.getEmailAddress() + "'>" + member.getEmailAddress() + "</a>");
      }

      if (member.getSub() != null && (member.getSub().startsWith("y") || member.getSub().startsWith("Y"))) {
        printRow(member.getFullName_lastNameFirst(), member.getHomePhone(), member.getWorkPhone(), member.getCellPhone(), member.getPager(), member.getEmailAddress());
      }
      member = patrol.nextMember("&nbsp;", rosterResults);   // "&nbsp;" is the default string field
    }
  }

}
