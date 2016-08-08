package org.nsponline.calendar.rest;

import org.nsponline.calendar.misc.PatrolData;
import org.nsponline.calendar.misc.SessionData;
import org.nsponline.calendar.misc.Utils;
import org.nsponline.calendar.store.NspSession;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

/**
 * @author Steve Gledhill
 *
 * clear cookies, and push to MonthCalendar (no longer logged in)
 */
public class Logout extends HttpServlet {

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    Utils.printRequestParameters(this.getClass().getSimpleName(), request);
    new InnerLogout(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    Utils.printRequestParameters(this.getClass().getSimpleName(), request);
    new InnerLogout(request, response);
  }

  public void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    Utils.printRequestParameters(this.getClass().getSimpleName(), request);
    new InnerLogout(request, response);
  }


  private class InnerLogout {
    private String resort;

    InnerLogout(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();
      resort = request.getParameter("resort");
      String sessionId = request.getHeader("Authorization");
      if(Utils.isEmpty(sessionId)) {
        Utils.buildErrorResponse(response, 400, "Authorization header not found");
        return;
      }
      if (!PatrolData.isValidResort(resort)) {
        Utils.buildErrorResponse(response, 400, "Resort not found (" + resort + ")");
        return;
      }
      SessionData sessionData = new SessionData(request, out);
      PatrolData patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData);
      Connection connection = patrol.getConnection();
      NspSession nspSession = NspSession.read(connection, sessionId);
      if (nspSession == null) {
        Utils.buildErrorResponse(response, 400, "Authorization not found (" + sessionId + ")");
        return;
      }
      nspSession.deleteRow(connection);
      Utils.build204Response(response);

//      sessionData.clearLoggedInResort();
//      sessionData.clearLoggedInUserId();
      patrol.close();
    }
  }
}

