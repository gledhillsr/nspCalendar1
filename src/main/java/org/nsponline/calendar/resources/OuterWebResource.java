package org.nsponline.calendar.resources;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.nsponline.calendar.NspHttpServlet;
import org.nsponline.calendar.store.*;
import org.nsponline.calendar.utils.*;

public class OuterWebResource {
  private static final int MIN_LOG_LEVEL = Logger.INFO;

  /**
   * @author Steve Gledhill
   *
   * List patrollers who have selected "available as a substitute" on their preferences
   * With a button to email this entire list, or links on each patrollers email address
   */
  public static class SubList extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(this.getClass(), request, "GET", request.getParameter("resort"), MIN_LOG_LEVEL);
      new OuterSubList(request, response, LOG);
    }
  }

  /**
   * @author Steve Gledhill
   */
  public static class UpdateInfo extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      Logger LOG = new Logger(this.getClass(), request, "GET", request.getParameter("resort"), MIN_LOG_LEVEL);
      new OuterUpdateInfo(request, response, LOG);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(this.getClass(), request, "POST", request.getParameter("resort"), MIN_LOG_LEVEL);
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
      Logger LOG = new Logger(this.getClass(), request, "GET", request.getParameter("resort"), Logger.INFO);
      new OuterListAssignments(request, response, LOG);
    }
  }

  /**
   * @author Steve Gledhill
   */
  public static class RefresherTraining extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(this.getClass(), request, "GET", request.getParameter("resort"), Logger.INFO);
      new OuterRefresherTraining(request, response, LOG).runner(this.getClass().getSimpleName());
    }
  }

  /*
   * @author Steve Gledhill
   */
  public static class Directors extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(this.getClass(), request, "GET", request.getParameter("resort"), Logger.INFO);
      new OuterDirector(request, response, LOG).runner(this.getClass().getSimpleName());
    }
  }

  /*
   * @author Steve Gledhill
   */
  public static class ListPatrollers extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(this.getClass(), request, "GET", request.getParameter("resort"), Logger.INFO);
      new OuterListPatrollers(request, response, LOG).runner(this.getClass().getSimpleName());
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(this.getClass(), request, "POST", request.getParameter("resort"), Logger.INFO);
      new OuterListPatrollers(request, response, LOG).runner(this.getClass().getSimpleName());
    }
  }

  /**
   * @author Steve Gledhill
   *         <p/>
   *         display a 1 month calendar
   */
  public static class MonthCalendar extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(this.getClass(), request, "GET", request.getParameter("resort"), Logger.INFO);
      new OuterMonthCalendar(request, response, LOG).runner(this.getClass().getSimpleName());
    }
  }
}
