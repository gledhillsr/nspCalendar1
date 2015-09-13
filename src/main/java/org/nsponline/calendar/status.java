package org.nsponline.calendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * status servlet.
 */

public final class Status extends HttpServlet {

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

    response.setContentType("text/html");
    PrintWriter writer = response.getWriter();
    writer.println("<html>");
    writer.println("<head>");
    writer.println("<title>NSP Online Servlet Status</title>");
    writer.println("</head>");
    writer.println("<body bgcolor=white>");

    writer.println("<table border=\"0\" cellpadding=\"10\">");
    writer.println("<tr>");
    writer.println("<td>");
    writer.println("<img src=\"images/maestro.png\">");
    writer.println("</td>");
    writer.println("<td>");
    writer.println("<h1>NSP Online Servlet Status</h1>");
    writer.println("</td>");
    writer.println("</tr>");
    writer.println("</table>");

    writer.println("<br>NSP Online.org");

    writer.println("</body>");
    writer.println("</html>");
  }
}
