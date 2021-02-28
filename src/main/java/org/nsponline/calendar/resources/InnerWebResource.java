package org.nsponline.calendar.resources;

import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.nsponline.calendar.utils.*;

/**
 * @author Steve Gledhill
 */
public class InnerWebResource {
  private static final int MIN_LOG_LEVEL = Logger.INFO;
  private static final String OUTER_CLASS_IF_CREDENTIALS_FAIL = "MonthCalendar";

  /*
   * @author Steve Gledhill
   */
  public static class ChangeShift extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(this.getClass(), request, "GET", request.getParameter("resort"), MIN_LOG_LEVEL);
      new InnerChangeShift(request, response, LOG).runner(OUTER_CLASS_IF_CREDENTIALS_FAIL);
    }
  }
}
