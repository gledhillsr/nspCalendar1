package org.nsponline.calendar.resources;

import java.io.IOException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.nsponline.calendar.utils.Logger;
import org.nsponline.calendar.utils.PatrolData;

import static org.nsponline.calendar.utils.ValidateCredentialsRedirectIfNeeded.NSP_TOKEN_NAME;

public class OuterMemberLogout extends ResourceBase {
  public OuterMemberLogout(final HttpServletRequest request, final HttpServletResponse response, Logger LOG) throws IOException {
    super(request, response, LOG);
    Cookie uiColorCookie = new Cookie(NSP_TOKEN_NAME, "");
    uiColorCookie.setMaxAge(0);
    response.addCookie(uiColorCookie);
    LOG.warn("deleting cookie in member logout");

    if (!initBase(response)) {
      return; //"resort" not found, or requestFromBot
    }

    sessionData.clearLoggedInResort();
    sessionData.clearLoggedInUserId();
    String newLoc = PatrolData.SERVLET_URL + "MonthCalendar?resort=" + resort;
    LOG.info("Logout sendRedirect to: " + newLoc);
    response.sendRedirect(newLoc);
  }
}
