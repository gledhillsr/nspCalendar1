package org.nsponline.calendar.resources;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.nsponline.calendar.utils.*;

public class OuterWebResource {
  private static final int MIN_LOG_LEVEL = Logger.DEBUG;


  /**
   * @author Steve Gledhill
   *
   * List patrollers who have selected "available as a substitute" on their preferences
   * With a button to email this entire list, or links on each patrollers email address
   */
  public static class SubList extends HttpServlet {
    private static final int MIN_LOG_LEVEL = Logger.INFO;


    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(SubList.class, request, "GET", null, MIN_LOG_LEVEL);
      new OuterSubList(request, response, LOG);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(SubList.class, request, "POST", null, MIN_LOG_LEVEL);
      new OuterSubList(request, response, LOG);
    }


  }

  /**
   * @author Steve Gledhill
   */
  public static class UpdateInfo extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      Logger LOG = new Logger(OuterWebResource.UpdateInfo.class, request, "GET", null, MIN_LOG_LEVEL);
      new OuterUpdateInfo(request, response, LOG);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(OuterWebResource.UpdateInfo.class, request, "POST", null, MIN_LOG_LEVEL);
      new OuterUpdateInfo(request, response, LOG);
    }

  }

  /**
   * @author Steve Gledhill
   *         <p/>
   *         List calendar assignments for the patroller.
   *         Brighton patrollers also get a view of the locker room assignments
   */
  public static class ListAssignments extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(ListAssignments.class, request, "GET", null, Logger.INFO);
      new OuterListAssignments(request, response, LOG);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(ListAssignments.class, request, "POST", null, Logger.INFO);
      new OuterListAssignments(request, response, LOG);
    }

  }
}
