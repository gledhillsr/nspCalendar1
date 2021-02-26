package org.nsponline.calendar;

import org.nsponline.calendar.utils.PatrolData;
import org.nsponline.calendar.utils.SessionData;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Steve Gledhill
 * <p>
 * clear cookies, and push to MonthCalendar (no longer logged in)
 */
public class MemberLogout extends NspHttpServlet {

  Class<?> getServletClass() {
    return this.getClass();
  }

  String getParentIfBadCredentials() {
    return null;
  }

  void servletBody(final HttpServletRequest request, final HttpServletResponse response, ServletData servletData) throws IOException {
    SessionData sessionData = servletData.getSessionData();
    sessionData.clearLoggedInResort();
    sessionData.clearLoggedInUserId();
    String newLoc = PatrolData.SERVLET_URL + "MonthCalendar?resort=" + servletData.getResort();
    servletData.getLOG().info("Logout sendRedirect to: " + newLoc);
    response.sendRedirect(newLoc);
  }
}

