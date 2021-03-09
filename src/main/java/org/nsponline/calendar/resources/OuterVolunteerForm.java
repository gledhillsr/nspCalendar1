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
    initBase(response);

    String id = request.getParameter("ID");

    sessionData.clearLoggedInResort();
    sessionData.clearLoggedInUserId();
    if ("Brighton".equals(resort) && "192443".equals(id)) {
      String subject = "Brighton volunteer application";
      String message =
        "new Ski Patrol applicant: " + request.getParameter("firstName") + " " + request.getParameter("lastName") +
        "\n address: " + request.getParameter("city") + ", " + request.getParameter("state") + " " + request.getParameter("zip") +
        "\n cellNumber: " + request.getParameter("cellNumber") +
        "\n email: " + request.getParameter("email") +
        "\n yearsSkied: " + request.getParameter("yearsSkied") +
        "\n skiExperience: \n" + request.getParameter("skiExperience");

      Roster member = patrolData.getMemberByID(id);
      MailMan mail = new MailMan(member.getEmailAddress(), sessionData, LOG);

      mail.sendMessage(subject, message, "brightonnsp@gmail.com");
      mail.sendMessage(subject, message, "steve@gledhills.com");

    }
    String newLoc = PatrolData.SERVLET_URL + "MonthCalendar?resort=" + resort;
    response.sendRedirect(newLoc);
    patrolData.close();
  }
}