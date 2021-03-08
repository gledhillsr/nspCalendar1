package org.nsponline.calendar.resources;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.nsponline.calendar.store.Roster;
import org.nsponline.calendar.utils.*;

public class OuterRefresherTraining extends ResourceBase {
  HttpServletResponse response;

  OuterRefresherTraining(HttpServletRequest request, HttpServletResponse response, Logger LOG) throws IOException {
    super(request, response, LOG);
    this.response = response;
  }

  public void runner(String parentClassName) {
    if (!initBaseAndAskForValidCredentials(response, parentClassName)) {
      return;
    }

    printCommonHeader();
    printBody(sessionData.getLoggedInUserId());
    patrolData.close(); //must close connection!
    printCommonFooter();
  }

  private void printBody(String IDOfEditor) {
    out.println("<h2>" + PatrolData.getResortFullName(resort, LOG) + " Ski Patrol - Refresher and Training information</h2>");
    Roster member = patrolData.getMemberByID(IDOfEditor);
    String siteUrl = "<font size =\"2\" color=\"red\" face=verdana,arial><A href='http://gledhills.com/Brighton/training/index.php?id=" + IDOfEditor + "' target='_blank'><b>2020 Brighton Ski Patrol Refresher</b></a></font>";
    out.println("<br><br> " + member.getFullName() + ", this is <b>your</b> link the new online refresher: " + siteUrl);
    //show custom link
    out.println("       ");
    out.println("        ");
    //scary stuff
    out.println("<br><br><br>Please do NOT share this link.  IT IS NOT PUBLIC INFORMATION. <br><br>Also, this link is specific to you, it will be how we track your progress!<br><br><br>");
    //Training
    out.println("<h3>Ongoing Training</h3><br>");
    out.println("<font size =\"2\" color=\"red\" face=verdana,arial><A href='https://youtu.be/0g5ap1xCpS8' target='_blank'><b>November 2020 Brighton Ski Patrol Meeting</b></a></font><br>");
    out.println("<font size =\"2\" color=\"red\" face=verdana,arial><A href='https://youtu.be/1p6wa02OVGQ' target='_blank'><b>December 2020 Brighton Ski Patrol Meeting</b></a></font><br>");
    out.println("<font size =\"2\" color=\"red\" face=verdana,arial><A href='https://youtu.be/Vf2PrelElxY' target='_blank'><b>January 2020 Brighton Ski Patrol Meeting</b></a></font><br>");
    out.println("<font size =\"2\" color=\"red\" face=verdana,arial><A href='https://youtu.be/ESc3fHzvm2A' target='_blank'><b>February 2020 Brighton Ski Patrol Meeting (15 min)</b></a></font><br>");

    out.println("<br>");
    out.println("<font size =\"2\" color=\"red\" face=verdana,arial><A href='https://www.youtube.com/watch?v=xvVSpyec5LM&feature=youtu.be' target='_blank'><b>Know Before You Go avalanche awareness refresher presented by Peter Tucker (27 min), Joey Manship (30 min), and Craig Gordon (55 min)</b></a></font><br>");
  }
}
