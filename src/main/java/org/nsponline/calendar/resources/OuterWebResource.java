package org.nsponline.calendar.resources;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
   * My Info tab, see and update patrollers roster data
   * GET
   * POST
   * @author Steve Gledhill
   */
  public static class UpdateInfo extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(this.getClass(), request, "GET", request.getParameter("resort"), MIN_LOG_LEVEL);
      new OuterUpdateInfo(request, response, LOG);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(this.getClass(), request, "POST", request.getParameter("resort"), MIN_LOG_LEVEL);
      new OuterUpdateInfo(request, response, LOG);
    }
  }

  /**
   *  My Assignments tab.  List calendar assignments for the patroller.
   *         Brighton patrollers also get a view of the locker room assignments
   *  GET
   * @author Steve Gledhill
   */
  public static class ListAssignments extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(this.getClass(), request, "GET", request.getParameter("resort"), Logger.INFO);
      new OuterListAssignments(request, response, LOG);
    }
  }

  /**
   * Refresher/Training tab (Brighton only)
   * GET
   * @author Steve Gledhill
   */
  public static class RefresherTraining extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(this.getClass(), request, "GET", request.getParameter("resort"), Logger.INFO);
      new OuterRefresherTraining(request, response, LOG).runner(this.getClass().getSimpleName());
    }
  }

  /**
   * Directors tab
   * GET
   * @author Steve Gledhill
   **/
  public static class Directors extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(this.getClass(), request, "GET", request.getParameter("resort"), Logger.INFO);
      new OuterDirector(request, response, LOG).runner(this.getClass().getSimpleName());
    }
  }

  /**
   * Patrollers tab for Brighton, also directors screen
   * ALMOST SAME as MemberList (Patrollers tab, for other resorts)
   * GET
   * POST
   * @author Steve Gledhill
   **/
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
   * todo  get rid of MemberList, and use ListPatrollers
   *
   * Patrollers tab, for other resorts (not Brighton)
   * ALMOST SAME as ListPatrollers (Patrollers tab, Brighton)
   * GET
   * POST
   * @author Steve Gledhill
   *
   */
  public static class MemberList extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(this.getClass(), request, "GET", request.getParameter("resort"), Logger.INFO);
      new OuterMemberList(request, response, LOG).runner(this.getClass().getSimpleName());
    }
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(this.getClass(), request, "POST", request.getParameter("resort"), Logger.INFO);
      new OuterMemberList(request, response, LOG).runner(this.getClass().getSimpleName());
    }
  }

  /**
   * @author Steve Gledhill
   *         <p/>
   *         display a one month calendar
   */
  public static class MonthCalendar extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(this.getClass(), request, "GET", request.getParameter("resort"), Logger.INFO);
      new OuterMonthCalendar(request, response, LOG).runner(this.getClass().getSimpleName());
    }
  }

  /**
   * @author Steve Gledhill
   * <p>
   * clear cookies, and push to MonthCalendar (no longer logged in)
   */
  public static class MemberLogout extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(this.getClass(), request, "GET", request.getParameter("resort"), Logger.INFO);
      new OuterMemberLogout(request, response, LOG);
    }
  }

  /**
   * @author Steve Gledhill
   *         <p/>
   *         1) validate user/password
   *         2) if valid, go to parent page
   *         3) else display login help screen
   */
  public static class LoginHelp extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(this.getClass(), request, "GET", request.getParameter("resort"), Logger.INFO);
      new OuterLoginHelp(request, response, LOG);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(this.getClass(), request, "POST", request.getParameter("resort"), Logger.INFO);
      new OuterLoginHelp(request, response, LOG);
    }
  }

  /**
   * login through my web client
   *
   * @author Steve Gledhill
   */
  @SuppressWarnings("SpellCheckingInspection")
  public static class MemberLogin extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(this.getClass(), request, "GET", request.getParameter("resort"), Logger.INFO);
      new OuterMemberLogin(request, response, LOG);
    }
  }
}
