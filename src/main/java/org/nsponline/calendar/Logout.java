package org.nsponline.calendar;

import org.nsponline.calendar.misc.PatrolData;
import org.nsponline.calendar.misc.SessionData;
import org.nsponline.calendar.misc.Utils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Steve Gledhill
 *
 * clear cookies, and push to MonthCalendar (no longer logged in)
 */
public class Logout extends HttpServlet {
//todo remove me
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    Utils.printRequestParameters(this.getClass().getSimpleName(), request);
    new InnerLogout(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    Utils.printRequestParameters(this.getClass().getSimpleName(), request);
    new InnerLogout(request, response);
  }

  private class InnerLogout {
    private String resort;

    InnerLogout(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();
      resort = request.getParameter("resort");
      if (PatrolData.isValidResort(resort)) {
        response.setContentType("text/html");
        SessionData sessionData = new SessionData(request, out);
        sessionData.clearLoggedInResort();
        sessionData.clearLoggedInUserId();
        response.setStatus(204);
      }
      else {
        response.setStatus(400);
      }
    }
  }
}

