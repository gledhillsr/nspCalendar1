package org.nsponline.calendar;

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

  static final boolean DEBUG = true;

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    Utils.printRequestParameters(this.getClass().getSimpleName(), request);
    new InnerLogout(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    doGet(request, response);
  }

  private class InnerLogout {
    private String resort;

    public InnerLogout(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();
      resort = request.getParameter("resort");
      response.setContentType("text/html");
      SessionData sessionData = new SessionData(request, out);
      sessionData.clearLoggedInResort();
      sessionData.clearLoggedInUserId();
      String newLoc = PatrolData.SERVLET_URL + "MonthCalendar?resort=" + resort;
      debugOut("Logout sendRedirect to: " + newLoc);
      response.sendRedirect(newLoc);
    }

    private void debugOut(String str) {
      if (DEBUG) {
        System.out.println("DEBUG-Logout(" + resort + "): " + str);
      }
    }
  }
}

