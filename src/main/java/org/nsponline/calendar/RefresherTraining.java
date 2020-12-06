package org.nsponline.calendar;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.nsponline.calendar.misc.*;
import org.nsponline.calendar.store.Roster;

/**
 * @author Steve Gledhill
 *
 *
 */
public class RefresherTraining extends nspHttpServlet {
  private static final int MIN_LOG_LEVEL = Logger.INFO;

  private Logger LOG;

  Class<?> getServletClass() {
    return this.getClass();
  }

  String getParentIfBadCredentials() {
    return "LiftEvac";
  }

  void servletBody(final HttpServletRequest request, final HttpServletResponse response) {
    new InnerRefresherTraining().runner(request, response);
  }

  private class InnerRefresherTraining {
    PrintWriter out;
    private String resort;
    PatrolData patrol;

    public void runner(final HttpServletRequest request, final HttpServletResponse response) {

      response.setContentType("text/html");
      try {
        out = response.getWriter();
      }
      catch (IOException e) {
        return;
      }
      SessionData sessionData = new SessionData(request, out);
      ValidateCredentials credentials = new ValidateCredentials(sessionData, request, response, "LiftEvac", LOG);
      if (credentials.hasInvalidCredentials()) {
        return;
      }
      //by now, sessionData.getLoggedInUserId and sessionData.getLoggedInResort are valid
      resort = sessionData.getLoggedInResort();
      String IDOfEditor = sessionData.getLoggedInUserId();

      patrol = null;
      patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData, LOG);
      Roster editor = patrol.getMemberByID(IDOfEditor);

      OuterPage outerPage = new OuterPage(patrol.getResortInfo(), "", sessionData.getLoggedInUserId());
      outerPage.printResortHeader(out);
      printBody(IDOfEditor);
      patrol.close(); //must close connection!
      LOG.debug("ending LiftEvac");
      outerPage.printResortFooter(out);
    }

    public void printBody(String IDOfEditor) {
      out.println("<h2>Resort Protocols and Lift Evac for " + PatrolData.getResortFullName(resort) + " Ski Patrol Members</h2>");
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
      out.println("<font size =\"2\" color=\"red\" face=verdana,arial><A href='https://youtu.be/0g5ap1xCpS8' target='_blank'><b>November 2020 Brighton Ski Patrol Meeting</b></a></font>");

    }
  }
}

