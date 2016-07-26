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
public class Login extends HttpServlet {

  static final boolean DEBUG = true;

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    Utils.printRequestParameters(this.getClass().getSimpleName(), request);
    new InnerLogin(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    Utils.printRequestParameters(this.getClass().getSimpleName(), request);
    new InnerLogin(request, response);
  }

  private final class InnerLogin {
    private String resort;
    SessionData sessionData;

    public InnerLogin(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();

      sessionData = new SessionData(request, out);
      resort = request.getParameter("resort");
      if (Utils.isEmpty(resort) || !PatrolData.isValidResort(resort)) {
        logger("ERROR, unknown resort (" + resort + ")");
        response.setStatus(400, "ERROR, unknown resort (" + resort + ")");
        return;
      }
//      PatrolData patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData); //when reading members, read full data
    }

    private void logger(String str) {
      Utils.printToLogFile(sessionData.getRequest(), str);
    }
  }
}

