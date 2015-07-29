package org.nsponline.calendar;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.lang.*;

/**
 * @author Steve Gledhill
 */
public class CustomizedList extends HttpServlet {

  PrintWriter out;
  String title;
  private String resort;
  String szMonths[] = {"Error", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
  boolean isDirector;
  String IDOfPatroller;


  public void doGet(HttpServletRequest request,
                    HttpServletResponse response)
      throws IOException, ServletException {
    synchronized (this) {
      resort = request.getParameter("resort");
      response.setContentType("text/html");
      out = response.getWriter();
      SessionData sessionData = new SessionData(request.getSession(), out);
      CookieID cookie = new CookieID(sessionData, request, response, "CustomizedList", null);

      isDirector = false;
      IDOfPatroller = cookie.getID(); //editor's ID
      PatrolData patrol = new PatrolData(PatrolData.FETCH_MIN_DATA, resort, sessionData); //when reading members, read full data
      if (IDOfPatroller != null) {
        MemberData patroller = patrol.getMemberByID(IDOfPatroller); //ID from cookie
        isDirector = patroller.isDirector();
        if (IDOfPatroller.equals(PatrolData.backDoorUser)) {
          isDirector = true;
        }
      }

      printTop();
      printBody();
      printBottom();
    }
  }

  /**********/
    /* doPost */

  /**
   * ******
   */
  public void doPost(HttpServletRequest request,
                     HttpServletResponse response)
      throws IOException, ServletException {
    doGet(request, response);
  }

  /************/
    /* printTop */

  /**
   * ********
   */
  public void printTop() {
    out.println("<html>");
    out.println("<body bgcolor=\"white\">");
    out.println("<head>");

    title = "Customized Patrol List";
    out.println("<title>" + title + "</title>");
    out.println("<META HTTP-EQUIV=\"Pragma\" CONTENT=\"no-cache\">");
    out.println("<META HTTP-EQUIV=\"Expires\" CONTENT=\"-1\">");

//all JavaScript code
    out.println("<SCRIPT LANGUAGE=\"JavaScript\">");
//????
    out.println("function validateBtn () {");
//        out.println("   var nLen = document.form1.transaction.length;");
//    // Check if only one option, select it
//        out.println("   if ((nLen == null) || (typeof nLen == \"undefined\"))");
//        out.println("       document.form02.transaction.click();");
//    // Make sure specified (0-based) index is within range
//        out.println("   else");
//        out.println("   if (index < nLen)");
//        out.println("       document.form1.DAY_CNT.checked=1;");
//        out.println("       document.form1.NIGHT_CNT.checked=1;");
    out.println("   if(!document.form1.DAY_CNT.checked && !document.form1.SWING_CNT.checked && !document.form1.NIGHT_CNT.checked && !document.form1.TRAINING_CNT.checked) {");
    out.println("       alert(\"Must first check any combination of \\nDay, Swing, Night, and/or Training!\");");
    out.println("       document.form1.MIN_DAYS.checked=0;");
//
    out.println("    } else {");
    out.println("       document.form1.MinDays.disabled=false;");
    out.println("    }");
    out.println("}");
//-->

    out.println("</SCRIPT>");

    out.println("</head>");
    out.println("<body>");
  }

  /***************/
    /* printBottom */

  /**
   * ***********
   */
  public void printBottom() {
    out.println("</body>");
    out.println("</html>");
  }

  private void printStep1InstructionsAsTable() {
    out.println("  <table border=\"0\" width=\"100%\">");
    out.println("    <tr>");
    out.println("      <td width=\"25%\" bgcolor=\"#E5E5E5\" bordercolor=\"#E5E5E5\">With Classification:</td>");
    out.println("      <td width=\"20%\" bgcolor=\"#E5E5E5\" bordercolor=\"#E5E5E5\">");
    out.println("        <input type=\"checkbox\" name=\"AUX\" value=\"ON\" checked>Auxiliary<br>");
    out.println("        <input type=\"checkbox\" name=\"BAS\" value=\"ON\" checked>Basic<br>");
    out.println("        <input type=\"checkbox\" name=\"SR\" value=\"ON\" checked>Senior</td>");
    out.println("      <td width=\"20%\" bgcolor=\"#E5E5E5\" bordercolor=\"#E5E5E5\">");
    out.println("        <input type=\"checkbox\" name=\"SRA\" value=\"ON\" checked>Senior Auxiliary<br>");
    out.println("        <input type=\"checkbox\" name=\"TRA\" value=\"ON\" checked>Transfer<br>");
    out.println("        <input type=\"checkbox\" name=\"CAN\" value=\"ON\" checked>Candidate</td>");
    out.println("      <td width=\"35%\" bgcolor=\"#E5E5E5\" bordercolor=\"#E5E5E5\">");
    out.println("        <input type=\"checkbox\" name=\"PRO\" value=\"ON\">Pro<br>");
    out.println("        <input type=\"checkbox\" name=\"ALM\" value=\"ON\">Alumni<br>");
    out.println("        <input type=\"checkbox\" name=\"OTH\" value=\"ON\">Other<br>");
//    out.println("        <input type=\"checkbox\" name=\"INA\" value=\"ON\" disabled>Inactive</td>");
    out.println("    </tr>");
    out.println("    <tr>");
    out.println("      <td width=\"25%\" bordercolor=\"#E5E5E5\" bgcolor=\"#E5E5E5\">AND Commitment:</td>");
    out.println("      <td width=\"20%\" bordercolor=\"#E5E5E5\" bgcolor=\"#E5E5E5\">");
    out.println("        <input type=\"checkbox\" name=\"FullTime\" value=\"ON\" checked>Full Time</td>");
    out.println("      <td width=\"20%\" bordercolor=\"#E5E5E5\" bgcolor=\"#E5E5E5\">");
    out.println("        <input type=\"checkbox\" name=\"PartTime\" value=\"ON\" checked>Part Time</td>");
    out.println("      <td width=\"35%\" bordercolor=\"#E5E5E5\" bgcolor=\"#E5E5E5\">");
    out.println("        <input type=\"checkbox\" name=\"Inactive\" value=\"ON\">Currently Inactive</td>");
    out.println("    </tr>");
    out.println("    <tr>");
    out.println("      <td width=\"25%\" bordercolor=\"#E5E5E5\" bgcolor=\"#E5E5E5\">AND:</td>");
    out.println("      <td width=\"20%\" bordercolor=\"#E5E5E5\" bgcolor=\"#E5E5E5\">");
    out.println("      <input type=\"checkbox\" name=\"ALL\" value=\"ON\" checked>Everyone<br>");
    out.println("      <input type=\"checkbox\" name=\"ListDirector\">Directors");
    out.println("      </td>");
    out.println("      <td width=\"20%\" bordercolor=\"#E5E5E5\" bgcolor=\"#E5E5E5\">");
    out.println("        <input type=\"checkbox\" name=\"OEC\">OEC Instructor<br>");
    out.println("        <input type=\"checkbox\" name=\"CPR\">CPR Instructor</td>");
    out.println("      <td width=\"35%\" bordercolor=\"#E5E5E5\" bgcolor=\"#E5E5E5\">");
    out.println("        <input type=\"checkbox\" name=\"Ski\">Ski Instructor<br>");
    out.println("        <input type=\"checkbox\" name=\"Toboggan\">Toboggan Instructor</td>");
    out.println("    </tr>");
    out.println("  </table>");
  }

  private void printStep2InstructionsAsTable() {
    out.println("    <table border=\"0\" width=\"100%\">");
    out.println("      <tr>");
    out.println("        <td width=\"20%\" bgcolor=\"#E5E5E5\">");
    out.println("          <input type=\"radio\" value=\"FIRST\" name=\"NAME\">First&nbsp;Name&nbsp;Last&nbsp;Name<br>");
    out.println("          <input type=\"radio\" value=\"LAST\" name=\"NAME\" checked>Last&nbsp;Name,&nbsp;First&nbsp;Name<br>");
    out.println("          <input type=\"checkbox\" name=\"CLASS\">Classification<br>");
    out.println("          <input type=\"checkbox\" name=\"SHOW_ID\">Patroller&nbsp;ID<br>");
    out.println("          <input type=\"checkbox\" name=\"SHOW_BLANK\">Slim&nbsp;Blank&nbsp;Column<br>");
    out.println("          <input type=\"checkbox\" name=\"SHOW_BLANK2\">Wide&nbsp;Blank&nbsp;Column<br>");
    if (isDirector) {
      out.println("<font color=RED>");
      out.println("<br>  <input type=\"checkbox\" name=\"COMMENTS\">Private&nbsp;Comments<br>");
      if (resort.equalsIgnoreCase("Brighton")) {
        out.println("      <input type=\"checkbox\" name=\"TEAM_LEAD\">Team Lead Trained<br>");
        out.println("      <input type=\"checkbox\" name=\"MENTORING\">Mentoring Status");
      }
      out.println("</font>");
//    out.println("          <input type=\"checkbox\" name=\"C20\" value=\"ON\">Password<br>");
    }
    out.println("        </td>");
    out.println("        <td width=\"15%\" bgcolor=\"#E5E5E5\">");
    out.println("          <input type=\"checkbox\" name=\"SPOUSE\">Spouse<br>");
    out.println("          <input type=\"checkbox\" name=\"ADDR\">Address<br>");
    out.println("          <input type=\"checkbox\" name=\"CITY\">City<br>");
    out.println("          <input type=\"checkbox\" name=\"STATE\">State<br>");
    out.println("          <input type=\"checkbox\" name=\"ZIP\">Zip&nbsp;Code<br>");
    if (resort.equalsIgnoreCase("Brighton") && isDirector) {
      out.println("<font color=RED>");
      out.println("<br>   <input type=\"checkbox\" name=\"CAN_EARN_CREDITS\">Can&nbsp;Earn&nbsp;Credit<br>");
//        out.println("          <input type=\"checkbox\" name=\"CARRY_OVER_CREDITS\">Old&nbsp;Credits");
      out.println(" <input type=\"checkbox\" name=\"CREDITS_EARNED\">Credits&nbsp;Available<br>");
      out.println(" <input type=\"checkbox\" name=\"LAST_CREDIT_UPDATE\">Date of Credit Update");
      out.println("</font>");
    }
    out.println("        </td>");
    out.println("        <td width=\"15%\" bgcolor=\"#E5E5E5\">");
    out.println("          <input type=\"checkbox\" name=\"HOME\" Checked >Home Phone<br>");
    out.println("          <input type=\"checkbox\" name=\"WORK\" Checked >Work Phone<br>");
    out.println("          <input type=\"checkbox\" name=\"CELL\" Checked >Cell Phone<br>");
    out.println("          <input type=\"checkbox\" name=\"PAGER\">Pager<br>");
    out.println("          <input type=\"checkbox\" name=\"EMAIL\" Checked>Email&nbsp;Address<br>");
//    if(resort.equalsIgnoreCase("Brighton") && isDirector) {
//        out.println("<font color=RED>");
//        out.println("<br>  <input type=\"checkbox\" name=\"CREDITS_EARNED\">Credits&nbsp;Available<br>");
//        out.println("      <input type=\"checkbox\" name=\"CREDITS_USED\">Credits&nbsp;Used");
//        out.println("</font>");
//    }
    out.println("        </td>");
    out.println("        <td width=\"20%\" bgcolor=\"#E5E5E5\">");
    out.println("          <input type=\"checkbox\" name=\"EMERGENCY\">Emergency Call Up<br>");
    out.println("          <input type=\"checkbox\" name=\"SUBSITUTE\">Substitute List<br>");
    out.println("          <input type=\"checkbox\" name=\"COMMIT\">Commitment Level<br>");
    out.println("          <input type=\"checkbox\" name=\"INSTRUCTOR\">Instructor&nbsp;Qualifications<br>");
    out.println("          <input type=\"checkbox\" name=\"DIRECTOR\">Director Status<br>");
    out.println("          <input type=\"checkbox\" name=\"LAST_UPDATED\">Last Updated Date");
//    if(resort.equalsIgnoreCase("Brighton") && isDirector) {
//        out.println("<font color=RED>");
//        out.println("<br><br>          <input type=\"checkbox\" name=\"LAST_CREDIT_UPDATE\">Date of Credit Update");
//        out.println("</font>");
//    }
    out.println("        </td>");
    out.println("        <td width=\"30%\" bgcolor=\"#E5E5E5\">");
    int i;

    Calendar cal = Calendar.getInstance();
    int year = cal.get(Calendar.YEAR);

    int StartDay = 1, StartMonth = 1, StartYear = year - 2;
    int EndDay = 31, EndMonth = 12, EndYear = year + 2;
    out.println("Starting:&nbsp;<select size=\"1\" name=\"StartDay\">");
    for (i = 1; i <= 31; ++i) {
      out.println("    <option " + ((i == StartDay) ? "selected" : "") + ">" + i + "</option>");
    }
    out.println("  </select>&nbsp;&nbsp;<select size=\"1\" name=\"StartMonth\">");
    for (i = 1; i <= 12; ++i) {
      out.println("    <option value=\"" + i + "\" " + ((i == StartMonth) ? "selected" : "") + ">" + szMonths[i] + "</option>");
    }
    out.println("  </select>&nbsp;&nbsp;<select size=\"1\" name=\"StartYear\">");
    for (i = year - 2; i <= year + 2; ++i) {
      out.println("    <option " + ((i == StartYear) ? "selected" : "") + ">" + i + "</option>");
    }
    out.println("  </select><br> ");

    out.println("Ending:&nbsp;&nbsp;<select size=\"1\" name=\"EndDay\">");
    for (i = 1; i <= 31; ++i) {
      out.println("    <option " + ((i == EndDay) ? "selected" : "") + ">" + i + "</option>");
    }
    out.println("  </select>&nbsp;&nbsp;<select size=\"1\" name=\"EndMonth\">");
    for (i = 1; i <= 12; ++i) {
      out.println("    <option value=\"" + i + "\" " + ((i == EndMonth) ? "selected" : "") + ">" + szMonths[i] + "</option>");
    }
    out.println("  </select>&nbsp;&nbsp;<select size=\"1\" name=\"EndYear\">");
    for (i = year - 2; i <= year + 2; ++i) {
      out.println("    <option " + ((i == EndYear) ? "selected" : "") + ">" + i + "</option>");
    }
    out.println("  </select><br>");

    out.println("---Shift&nbsp;Info-(Within&nbsp;above&nbsp;Range)---<br>");
//    if(resort.equalsIgnoreCase("Brighton")) {
    out.println("<input   type=checkbox name=DAY_CNT      >#&nbsp;Days:&nbsp;&nbsp;\n");
    out.println("&nbsp;&nbsp;<input type=checkbox name=DAY_DETAILS  >Include&nbsp;Details<br>\n");
    out.println("<input   type=checkbox name=SWING_CNT    ># Swings:\n");
    out.println("&nbsp;<input type=checkbox name=SWING_DETAILS>Include&nbsp;Details<br>\n");
    out.println("<input   type=checkbox name=NIGHT_CNT    ># Nights:\n");
    out.println("&nbsp;&nbsp;<input type=checkbox name=NIGHT_DETAILS>Include&nbsp;Details<br>\n");
    out.println("<input   type=checkbox name=TRAINING_CNT    ># Training:\n");
    out.println("&nbsp;&nbsp;<input type=checkbox name=TRAINING_DETAILS>Include&nbsp;Details<br>\n");
    out.println("---&nbsp;Limit&nbsp;names&nbsp;if&nbsp;above&nbsp;total&nbsp;is:&nbsp;----<br>\n");

//    } else {
//        out.println("          <input  type=\"checkbox\" name=\"DAY_CNT\">Count of Shift Assignments<br>");
//        out.println("          &nbsp;&nbsp;&nbsp;&nbsp;<input  type=\"checkbox\" name=\"DAY_DETAILS\">Include Details<br>");
//    }
    out.println("          <input type=\"checkbox\" name=\"MIN_DAYS\" onclick=validateBtn()>Only list if less than ");
    out.println("<select size=\"1\" name=\"MinDays\">");
    for (i = 1; i <= 20; ++i) {
      out.println("    <option " + ((i == 6) ? "selected" : "") + ">" + i + "</option>");
    }
    out.println("</select> shifts.");


    out.println("      </td>");
    out.println("      </tr>");
    out.println("    </table>");
  }

  private void printStep3InstructionsAsTable() {
    out.println("    <table border=\"0\" width=\"100%\">");
    out.println("      <tr>");
    out.println("        <td width=\"34%\" bgcolor=\"#E5E5E5\">");
    out.println("          <p align=\"center\"><b>First</b> sort by:</td>");
    out.println("        <td width=\"33%\" bgcolor=\"#E5E5E5\">");
    out.println("          <p align=\"center\"><b>Second</b> Sort by:</td>");
    out.println("        <td width=\"33%\" bgcolor=\"#E5E5E5\">");
    out.println("          <p align=\"center\"><b>Third</b> Sort by:</td>");
    out.println("      </tr>");
    out.println("      <tr>");
    out.println("        <td width=\"34%\" bgcolor=\"#E5E5E5\">");
    out.println("          &nbsp;&nbsp; <input type=\"radio\" name=\"FirstSort\" value=\"Name\" checked>Name<br>");
    out.println("          &nbsp;&nbsp; <input type=\"radio\" name=\"FirstSort\" value=\"Class\">Classification<br>");
    out.println("          &nbsp;&nbsp; <input type=\"radio\" name=\"FirstSort\" value=\"Comm\">Commitment<br>");
    out.println("          &nbsp;&nbsp; <input type=\"radio\" name=\"FirstSort\" value=\"shiftCnt\"># of Shift Assignments<br>");
    out.println("          &nbsp;&nbsp; <input type=\"radio\" name=\"FirstSort\" value=\"Updt\">Last Updated Date<br>");
    out.println("        </td>");
    out.println("        <td width=\"33%\" bgcolor=\"#E5E5E5\">");
    out.println("          &nbsp;&nbsp; <input type=\"radio\" name=\"SecondSort\" value=\"None\" checked>NO second sort field<br>");
    out.println("          &nbsp;&nbsp; <input type=\"radio\" name=\"SecondSort\" value=\"Name\">Name<br>");
    out.println("          &nbsp;&nbsp; <input type=\"radio\" name=\"SecondSort\" value=\"Class\">Classification<br>");
    out.println("          &nbsp;&nbsp; <input type=\"radio\" name=\"SecondSort\" value=\"Comm\">Commitment<br>");
//    out.println("          &nbsp;&nbsp; <input type=\"radio\" name=\"SecondSort\" disabled value=\"shiftCnt\"># of Shift Assignments<br>");
//    out.println("          &nbsp;&nbsp; <input type=\"radio\" name=\"SecondSort\" disabled value=\"Updt\">Last Updated Date</td>");
    out.println("        <td width=\"33%\" bgcolor=\"#E5E5E5\">");
    out.println("          &nbsp;&nbsp; <input type=\"radio\" name=\"ThirdSort\" value=\"None\" checked>NO third sort field<br>");
    out.println("          &nbsp;&nbsp; <input type=\"radio\" name=\"ThirdSort\" value=\"Name\">Name<br>");
    out.println("          &nbsp;&nbsp; <input type=\"radio\" name=\"ThirdSort\" value=\"Class\">Classification<br>");
    out.println("          &nbsp;&nbsp; <input type=\"radio\" name=\"ThirdSort\" value=\"Comm\">Commitment<br>");
//    out.println("          &nbsp;&nbsp; <input type=\"radio\" name=\"ThirdSort\" disabled value=\"shiftCnt\"># of Shift Assignments<br>");
//    out.println("          &nbsp;&nbsp; <input type=\"radio\" name=\"ThirdSort\" disabled value=\"Updt\">Last Updated Date</td>");
    out.println("      </tr>");
    out.println("    </table>");
  }
  /*************/
    /* printBody */

  /**
   * *********
   */
  public void printBody() {
    out.println("<h1 align=\"center\">Customized Patrol List</h1>");

    out.println("<form action=\"" + PatrolData.SERVLET_URL + "CustomizedList2\" method=POST>");
//        out.println("<form name=form1 action=\"CustomizedList2\" method=PUT>");

    out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"resort\" VALUE=\"" + resort + "\">");
    out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"ID\" VALUE=\"" + IDOfPatroller + "\">");
//step 1
    out.println("  <p align=\"left\"><font size=\"4\">Step 1) </font><font size=\"4\">Only include patrollers: (MUST select at least 1 from each category)</font></p>");
    printStep1InstructionsAsTable();
    out.print("  <p align=\"left\"><font size=\"4\">Step 2) Select what fields to display:");
    if (resort.equalsIgnoreCase("Brighton") && isDirector) {
      out.println("<font color=RED>&nbsp;&nbsp;&nbsp;(Director ONLY access)</font>");
    }
    out.println("  </font></p>");
//step 2
    out.println("  <div align=\"center\">");
    out.println("    <center>");
    printStep2InstructionsAsTable();
    out.println("    </center>");
    out.println("  </div>");
//step 3
    out.println("  <p align=\"left\"><font size=\"4\">Step 3) Sort by:</font></p>");
    out.println("  <div align=\"center\">");
    out.println("    <center>");
    printStep3InstructionsAsTable();
    out.println("    </center>");
    out.println("  </div>");

    out.println("Font Point Size: <SELECT NAME=\"FontSize\" SIZE=1>");
    out.println("<OPTION>18");
    out.println("<OPTION>16");
    out.println("<OPTION SELECTED>14");
    out.println("<OPTION>13");
    out.println("<OPTION>12");
    out.println("<OPTION>11");
    out.println("<OPTION>10");
    out.println("<OPTION>9");
    out.println("</SELECT>");

    out.println("  <p align=\"center\"><br>");
    out.println("  <input type=\"submit\" value=\"Display / Print / E-mail\" ></p>");
    out.println("</form>");
  }
}

