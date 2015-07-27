package org.nsponline.calendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * Simple Hello servlet.
 */

public final class Hello2 extends HttpServlet {


  /**
   * Respond to a GET request for the content produced by
   * this servlet.
   *
   * @param request  The servlet request we are processing
   * @param response The servlet response we are producing
   * @throws IOException      if an input/output error occurs
   * @throws ServletException if a servlet error occurs
   */
  public void doGet(HttpServletRequest request,
                    HttpServletResponse response)
      throws IOException, ServletException {

    response.setContentType("text/html");
    PrintWriter writer = response.getWriter();
    writer.println("<html>");
    writer.println("<head>");
    writer.println("<title>NSP Online Servlet Page</title>");
    writer.println("</head>");
    writer.println("<body bgcolor=white>");

    writer.println("<table border=\"0\" cellpadding=\"10\">");
    writer.println("<tr>");
    writer.println("<td>");
    writer.println("<img src=\"images/maestro.png\">");
    writer.println("</td>");
    writer.println("<td>");
    writer.println("<h1>NSP Online Servlet</h1>");
    writer.println("</td>");
    writer.println("</tr>");
    writer.println("</table>");

    writer.println("<br>NSP Online.org");
    writer.println("<br>1) added memberLogin");
    writer.println("<br>2) renamed war to calendar-1");
    writer.println("<br>3) added loginHelp");
    writer.println("<br>4) fixed link in MemberLogin from nscCode to calendar-1");
    writer.println("<br>5) connect to DB  :-)");
    writer.println("<br>6) MonthCalendar, NewIndividualAssignment");
    writer.println("<br>7) DayShifts - Calendar");
    writer.println("<br>8) ChangeShift - Calendar");
    writer.println("<br>9) ProcessChanges - Calendar - ChangeShift, MailMan!!");
    writer.println("<br>10) Emails are 'silently' broken ;-(");
    writer.println("<br>11) fixed broken link in LoginHelp.  but is a hack");
    writer.println("<br>11) WIP hiding credentials. adding to MonthCalendar6..");

    writer.println("</body>");
    writer.println("</html>");
  }
}
