package org.nsponline.calendar.resources;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.nsponline.calendar.utils.Logger;
import org.nsponline.calendar.utils.PatrolData;

public class OuterMemberLogout extends ResourceBase {
  public OuterMemberLogout(final HttpServletRequest request, final HttpServletResponse response, Logger LOG) throws IOException {
    super(request, response, LOG);
    initBase(response);

    sessionData.clearLoggedInResort();
    sessionData.clearLoggedInUserId();
    String newLoc = PatrolData.SERVLET_URL + "MonthCalendar?resort=" + resort;
    LOG.info("Logout sendRedirect to: " + newLoc);
    response.sendRedirect(newLoc);
  }
}
