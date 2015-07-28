package org.nsponline.calendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/*
 * @author Steve Gledhill
 */
public class Directors extends HttpServlet {

  //    ResourceBundle rb = ResourceBundle.getBundle("LocalStrings");
  PrintWriter out;
  String patrollerId;
  boolean isDirector;
  String[] sortedRoster;
  String MyName;
  int rosterSize;
  private String resort;

  public void doGet(HttpServletRequest request,
                    HttpServletResponse response)
      throws IOException, ServletException {
    response.setContentType("text/html");
    synchronized (this) {
      out = response.getWriter();
      SessionData sessionData = new SessionData(getServletContext(), out);
//      CookieID cookie = new CookieID(request,response,"Directors",null);
//      if(cookie.error)
//        return;
      resort = request.getParameter("resort");

      //if no correct cookie, then a redirect will happen above
      patrollerId = sessionData.getLoggedInUserId();    //editor's ID
      if (patrollerId != null) {
        int found = readData(patrollerId, sessionData); //to get director (ID from cookie)

        printTop();
        if (found != 1) {
          out.println("Error reading patroller id [" + patrollerId + "]");
        }
        else if (PatrolData.validResort(resort)) {
          printBody();
        }
        else {
          out.println("Invalid host resort. [" + resort + "]");
        }
        printBottom();
      }
    } //end Syncronized
  }

  public void doPost(HttpServletRequest request,
                     HttpServletResponse response)
      throws IOException, ServletException {
    doGet(request, response);
  }

  public String getServletInfo() {
    return "Database matainance for directors";
  }

  public int readData(String readID, SessionData sessionData) {
    PatrolData patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData);

    sortedRoster = new String[400];

    rosterSize = 0;
    MemberData member;
    while ((member = patrol.nextMember("")) != null) {
      sortedRoster[rosterSize++] = member.getLast() + ", " + member.getFirst();
    }
    if (readID.equalsIgnoreCase(PatrolData.backDoorUser)) {
      isDirector = true;
    }
    else {
      member = patrol.getMemberByID(readID);
      if (member == null) {
        return 0;
      }
      MyName = member.getLast() + ", " + member.getFirst();
      isDirector = member.isDirector();
    }
    patrol.close(); //must close connection!

    return 1;
  } //end ReadData

  public void printTop() {
    out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
    out.println("<html><head>");
    out.println("<meta http-equiv=\"Content-Language\" content=\"en-us\">");
    out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=windows-1252\">");
    out.println("<title>Database Maintenance for " + PatrolData.getResortFullName(resort) + "</title>");
    out.println("<META HTTP-EQUIV=\"Pragma\" CONTENT=\"no-cache\">");
    out.println("<META HTTP-EQUIV=\"Expires\" CONTENT=\"-1\">");
    out.println("</head><body>");
    out.println("<CENTER>");
    out.println("<h1>Database Maintenance for " + PatrolData.getResortFullName(resort) + "</h1>");
    out.println("</CENTER>");
  }

  public void printBody() {
    String nextPage;
    if (!isDirector) {
      out.println("<hr>");
      out.println("<CENTER>");
      out.println("<p>Sorry, you are -not- authorized for this menu.&nbsp; Try becoming a director");
      out.println("next year <img border=\"0\" src=\"/nspImages/EvilSmile.gif\" align=\"absmiddle\" width=\"32\" height=\"32\"></p>");
      out.println("</CENTER>");
//            nextPage = PatrolData.SERVLET_URL+"CustomizedList?resort="+resort+"&ID="+ID;
      nextPage = "CustomizedList?resort=" + resort + "&ID=" + patrollerId;
      out.println("<br><br>Only option available is: <input type=\"button\" value=\"Customized Listing ...\"  onClick=window.location=\"" + nextPage + "\"></td>");
    }
    else {
//            out.println("<form action=\""+PatrolData.SERVLET_URL+"UpdateInfo\" method=POST id=form02 name=form02>");
      out.println("<form action=\"UpdateInfo\" method=POST id=form02 name=form02>");
      out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"resort\" VALUE=\"" + resort + "\">");
      out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"ID\" VALUE=\"" + patrollerId + "\">");

//            nextPage=PatrolData.SERVLET_URL+"UpdateInfo?resort="+resort+"&Purpose=Add_Patroller&ID="+ID;
      nextPage = "UpdateInfo?resort=" + resort + "&Purpose=Add_Patroller&ID=" + patrollerId;

      out.println("<table border=2 cellpadding=0 cellspacing=3 width=820 bgcolor=\"#E4E4E4\">");
      out.println("  <tr>");
//
// Patroller Maintenance
//
      out.println("    <td width=307 align=\"center\" bgcolor=\"#FFFFBB\"><font size=5>Patroller Maintenance:</font></td>");
      out.println("    <td width=529 bgcolor=\"#FFFFBB\">");
      out.println("      <table border=1 cellpadding=0 cellspacing=0 width=\"100%\">");
      out.println("        <tr>");
      out.println("          <td width=496 colspan=2 height=40>");
      out.println("            <p align=\"left\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
      out.println("<INPUT TYPE=button VALUE=\"Add New Patroller ...\" onClick=window.location=\"" + nextPage + "\">");
      out.println("          </td>");
      out.println("        </tr>");
      out.println("        <tr>");
      out.println("          <td width=288>");
      out.println("            <table border=0 cellpadding=2 cellspacing=0 width=\"100%\">");
      out.println("              <tr>");
      out.println("                <td width=\"100%\"><font face=\"AR Sans Serif\" size=1>Select");
      out.println("                  Patroller on right then<br>");
      out.println("                  Click button below&nbsp;</font></td>");
      out.println("              </tr>");
      out.println("              <tr>");
      out.println("                <td width=\"100%\" align=\"center\"><INPUT TYPE=submit NAME=setNewID VALUE=\"Change ID Number ...\"> </td>");
      out.println("              </tr>");
      out.println("              <tr>");
      out.println("                <td width=\"100%\" align=\"center\"><INPUT TYPE=submit NAME=EditInfo VALUE=\"Edit Patroller Info ...\"></td>");
      out.println("              </tr>");
      out.println("              <tr>");
      out.println("                <td width=\"100%\" align=\"center\"><INPUT TYPE=submit NAME=removeName VALUE=\"Delete Patroller ...\"></td>");
      out.println("              </tr>");
      out.println("            </table>");
      out.println("          </td>");
      out.println("          <td width=208><SELECT NAME=\"NameToEdit\" SIZE=7>");
      addNames(MyName);
      out.println("          </SELECT></td>");
      out.println("        </tr>");
      out.println("      </table>");
      out.println("    </td>");
      out.println("  </tr>");
//
// Patroller Listings
//
      out.println("  <tr>");
      out.println("    <td width=307 align=\"center\" bgcolor=\"#F2928C\"><font size=5>Patrol Listings:</font></td>");
      out.println("    <td width=529 bgcolor=\"#F2928C\">");
      out.println("      <table border=0 cellspacing=0 width=500 cellpadding=2>");
      out.println("        <tr>");
      out.println("          <td>&nbsp;&nbsp;<font size=2>&nbsp;&nbsp; Custom Reports:</font></td>");
      out.println("          <td>&nbsp;&nbsp;<font size=2>&nbsp;&nbsp; Quick Reports:</font></td>");
      out.println("        </tr>");
//custom listing
      out.println("        <tr>");
//            nextPage = PatrolData.SERVLET_URL+"CustomizedList?resort="+resort+"&ID="+ID;
      nextPage = "CustomizedList?resort=" + resort + "&ID=" + patrollerId;
      out.println("		   <td rowspan=3><input type=\"button\" value=\"Customized Listing ...\"  onClick=window.location=\"" + nextPage + "\"></td>");
//Day Shift
//            String disableStatus = "disabled";
      String disableStatus = "";
//            nextPage = PatrolData.SERVLET_URL+"CustomizedList2?resort="+resort+"&CLASS=1&AUX=1&BAS=1&SR=1&SRA=1&TRA=1&CAN=1&PRO=1&ALM=1&INA=1&FullTime=1&PartTime=1&Inactive=1&FIRST=1&DAY_CNT=1&DAY_DETAILS=1&ALL=1&FirstSort=shiftCnt&SecondSort=None&ThirdSort=None&FontSize=12&ID="+ID;
      nextPage = "CustomizedList2?resort=" + resort + "&NAME=LAST&AUX=1&BAS=1&SR=1&SRA=1&TRA=1&CAN=1&PRO=1&ALM=1&INA=1&FullTime=1&PartTime=1&Inactive=1&FIRST=1&DAY_CNT=1&DAY_DETAILS=1&ALL=1&FirstSort=shiftCnt&SecondSort=None&ThirdSort=None&FontSize=12&ID=" + patrollerId;
      out.println("          <td><input type=\"button\" value=\"Sorted By Day Shifts\"  onClick=window.location=\"" + nextPage + "\"></td>");
      out.println("        </tr>");
//swing shift
      out.println("        <tr>");
      nextPage = "CustomizedList2?resort=" + resort + "&NAME=LAST&AUX=1&BAS=1&SR=1&SRA=1&TRA=1&CAN=1&PRO=1&ALM=1&INA=1&FullTime=1&PartTime=1&Inactive=1&FIRST=1&SWING_CNT=1&SWING_DETAILS=1&ALL=1&FirstSort=shiftCnt&SecondSort=None&ThirdSort=None&FontSize=12&ID=" + patrollerId;
      out.println("          <td><input type=\"button\" value=\"Sorted By Swing Shifts\" " + disableStatus + "  onClick=window.location=\"" + nextPage + "\"></td>");
      out.println("        </tr>");
//night shift
      out.println("        <tr>");
//            if(resort.equalsIgnoreCase("Brighton"))
//                disableStatus = "";
      nextPage = PatrolData.SERVLET_URL + "CustomizedList2?resort=" + resort + "&NAME=LAST&AUX=1&BAS=1&SR=1&SRA=1&TRA=1&CAN=1&PRO=1&ALM=1&INA=1&FullTime=1&PartTime=1&Inactive=1&FIRST=1&NIGHT_CNT=1&NIGHT_DETAILS=1&ALL=1&FirstSort=shiftCnt&SecondSort=None&ThirdSort=None&FontSize=12&ID=" + patrollerId;
      out.println("            <td><input type=\"button\" value=\"Sorted By Night Shifts\"  onClick=window.location=\"" + nextPage + "\" " + disableStatus + "></td>");
      out.println("        </tr>");
      out.println("      </table>");
      out.println("    </td>");
      out.println("  </tr>");
//
// Patrol Setup / Cleanup
//
      out.println("  <tr>");
      out.println("    <td width=307 align=\"center\" bgcolor=\"#49D3FC\"><font size=5>Patrol Setup / Cleanup:");
      out.println("        </font></td>");
      out.println("    <td width=529 bgcolor=\"#49D3FC\">");
      out.println("      <table border=0 cellpadding=2 cellspacing=0 width=500>");
      out.println("        <tr>");
      out.println("          <td align=\"left\">");
//            nextPage = PatrolData.SERVLET_URL+"Preferences?resort="+resort+"&ID="+ID;
      nextPage = "Preferences?resort=" + resort + "&ID=" + patrollerId;
      out.println("            <input type=\"button\" value=\"Web Site Preferences ...\" onClick=window.location=\"" + nextPage + "\"></td>");
      out.println("        </tr>");
      out.println("        <tr>");
      out.println("          <td align=\"left\">");
//            nextPage = PatrolData.SERVLET_URL+"EditShifts?resort="+resort+"&ID="+ID;
      nextPage = "EditShifts?resort=" + resort + "&ID=" + patrollerId;
      out.println("            <input type=\"button\" value=\"Edit Shift Templates (for Calendar) ...\" onClick=window.location=\"" + nextPage + "\"></td>");
      out.println("        </tr>");
      out.println("        <tr>");
      out.println("          <td align=\"left\">");
//            nextPage = PatrolData.SERVLET_URL+"PurgeAssignments?resort="+resort+"&ID="+ID;
      nextPage = "PurgeAssignments?resort=" + resort + "&ID=" + patrollerId;
      out.println("            <input type=\"button\" value=\"Delete assignments from last season ...\" onClick=window.location=\"" + nextPage + "\"></td>");
      out.println("        </tr>");
      out.println("      </table>");
      out.println("    </td>");
      out.println("  </tr>");
      out.println("</table>");
      out.println("</form>");
    }
  }

  public void printBottom() {
    out.println("</body></html>");
  }

  private void addNames(String MyName) {
    int i;
    for (i = 0; i < rosterSize; ++i) {
      if (MyName != null && MyName.equals(sortedRoster[i])) {
        out.println("<OPTION SELECTED>" + MyName);
      }
      else {
        out.println("<OPTION>" + sortedRoster[i]);
      }
    }
  }
}

