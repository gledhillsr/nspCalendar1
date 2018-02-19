package org.nsponline.calendar;

import org.nsponline.calendar.misc.PatrolData;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Steve Gledhill
 * <p>
 * clear cookies, and push to MonthCalendar (no longer logged in)
 */
public class MemberLogout extends nspHttpServlet {

  Class getServletClass() {
    return this.getClass();
  }

  String getParentIfBadCredentials() {
    return null;
  }

  void servletBody(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
    sessionData.clearLoggedInResort();
    sessionData.clearLoggedInUserId();
    String newLoc = PatrolData.SERVLET_URL + "MonthCalendar?resort=" + resort;
    debugOut("Logout sendRedirect to: " + newLoc);
    response.sendRedirect(newLoc);
  }

  private void debugOut(String str) {
      LOG.info("DEBUG-Logout(" + resort + "): " + str);
  }
}

