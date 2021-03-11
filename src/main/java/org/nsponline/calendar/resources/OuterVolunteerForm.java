package org.nsponline.calendar.resources;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.nsponline.calendar.store.Roster;
import org.nsponline.calendar.utils.*;

/**
 * if (hidden fields exist) send email's
 * goto calendar
 */

public class OuterVolunteerForm extends ResourceBase {


  public OuterVolunteerForm(final HttpServletRequest request, final HttpServletResponse response, Logger LOG) throws IOException {
    super(request, response, LOG);
    String id = request.getParameter("ID"); //verify my hard coded ID from within Volunteer.htm
    if (!initBase(response) && "192443".equals(id) && "Brighton".equals(resort)) {
      return; //valid 'resort' and NOT a crawler
    }

    printCommonHeader();
    printBody(request, LOG, id);
    printBottom();
    printCommonFooter();
    patrolData.close();
  }


  private void printBody(HttpServletRequest request, Logger LOG, String id) {
    out.println("<br><br><br><br><br><br><h2>Submitting application... please wait</h2>");
    String subject = "Brighton volunteer application";
    String message =
      "new Ski Patrol applicant: " + request.getParameter("firstName") + " " + request.getParameter("lastName") +
      "\n address: " + request.getParameter("city") + ", " + request.getParameter("state") + " " + request.getParameter("zip") +
      "\n cellNumber: " + request.getParameter("cellNumber") +
      "\n email: " + request.getParameter("email") +
      "\n yearsSkied: " + request.getParameter("yearsSkied") +
      "\n skiExperience: \n" + request.getParameter("skiExperience");

    Roster member = patrolData.getMemberByID(id); //use hard coded id in Volunteer.htm
    MailMan mail = new MailMan(member.getEmailAddress(), sessionData, LOG);

    mail.sendMessage(subject, message, "brightonnsp@gmail.com");
    mail.sendMessage(subject, message, "steve@gledhills.com");
  }

  private void printBottom()  {
    String newLoc = PatrolData.SERVLET_URL + "MonthCalendar?resort=" + resort;
    out.println("<script type=\"text/javascript\">");
    out.println("  var timer = setTimeout(function() {");
    out.println("    window.alert(\"Application submitted\");");
    out.println("    window.location='" + newLoc + "';");
    out.println("  }, 2000);");
    out.println("</script>");
  }
}