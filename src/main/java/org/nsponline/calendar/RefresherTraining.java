package org.nsponline.calendar;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.nsponline.calendar.utils.*;
import org.nsponline.calendar.store.Roster;

/**
 * @author Steve Gledhill
 *
 *
 */
public class RefresherTraining extends NspHttpServlet {

  Class<?> getServletClass() {
    return this.getClass();
  }

  String getParentIfBadCredentials() {
    return "RefresherTraining";
  }

  void servletBody(final HttpServletRequest request, final HttpServletResponse response, ServletData servletData) {
    new InnerRefresherTraining().runner(request, response, servletData);
  }

  private class InnerRefresherTraining {
    PrintWriter out;
    private String resort;
    PatrolData patrol;

    public void runner(final HttpServletRequest request, final HttpServletResponse response, ServletData servletData) {

      response.setContentType("text/html");
      try {
        out = response.getWriter();
      }
      catch (IOException e) {
        return;
      }
      SessionData sessionData = new SessionData(request, out, servletData.getLOG());
      ValidateCredentials credentials = new ValidateCredentials(sessionData, request, response, "RefresherTraining", servletData.getLOG());
      if (credentials.hasInvalidCredentials()) {
        return;
      }
      //by now, sessionData.getLoggedInUserId and sessionData.getLoggedInResort are valid
      resort = sessionData.getLoggedInResort();
      String IDOfEditor = sessionData.getLoggedInUserId();

      patrol = null;
      patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData, servletData.getLOG());
      Roster editor = patrol.getMemberByID(IDOfEditor);

      OuterPage outerPage = new OuterPage(patrol.getResortInfo(), "", sessionData.getLoggedInUserId());
      outerPage.printResortHeader(out);
      printBody(IDOfEditor);
      patrol.close(); //must close connection!
      servletData.getLOG().debug("ending RefresherTraining");
      outerPage.printResortFooter(out);
    }

    public void printBody(String IDOfEditor) {
      out.println("<h2>" + PatrolData.getResortFullName(resort) + " Ski Patrol - Refresher and Training information</h2>");
      Roster member = patrol.getMemberByID(IDOfEditor);
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
}

