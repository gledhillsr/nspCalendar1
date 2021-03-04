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

  /**
   * From the calendar, clicked on a specific shift
   * Modify a single specific shift yyyy/mm/dd/shiftIndex/positionIndex to insert/modify/delete the assignment for that shift
   * GET resort=Brighton,  dayOfWeek=4,  date=4,  month=1,  year=2021,  ID=192443,  pos=1 (1 based),  index=0
   * then POST's updates to ProcessChangeShiftAssignments, or return to MonthCalendar
   *
   * @author Steve Gledhill
   */
  public static class ChangeShiftAssignments extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(this.getClass(), request, "GET", request.getParameter("resort"), MIN_LOG_LEVEL);
      new InnerChangeShiftAssignments(request, response, LOG).runner(OUTER_CLASS_IF_CREDENTIALS_FAIL);
    }
  }

  /**
   * From the calendar, clicked on a date
   * Override a single day shift template.  The date and position are required
   * note date in form YYYY-MM-DD
   * GET resort=Brighton,  dayOfWeek=1 (0=Sunday),  date=1 (1 based),  month=1,  year=2021,  ID=192443
   * (can call self for POST or DEL), cancel goes back to MonthCalendar
   * POST  (Group Assignment or simple modify)
   * DEL  (delete all shifts including override template)
   *
   * @author Steve Gledhill
   */
  public static class DayShifts extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(this.getClass(), request, "GET", request.getParameter("resort"), MIN_LOG_LEVEL);
      new InnerChangeSingleDayShift(request, response, LOG).runner(OUTER_CLASS_IF_CREDENTIALS_FAIL);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(this.getClass(), request, "POST", request.getParameter("resort"), MIN_LOG_LEVEL);
      new InnerChangeSingleDayShift(request, response, LOG).runner(OUTER_CLASS_IF_CREDENTIALS_FAIL);
    }

    public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(this.getClass(), request, "DELETE", request.getParameter("resort"), MIN_LOG_LEVEL);
      new InnerChangeSingleDayShift(request, response, LOG).runner(OUTER_CLASS_IF_CREDENTIALS_FAIL);
    }
  }

  /**
   * remove all the day shift assignments prior to a specified date (and ask for confirmation)
   * GET resort=Brighton ID=192443
   * POST XYZ=XYZ (sanity check) startDay=1 (1 based), startMonth=1 startYear=2019
   *
   * @author Steve Gledhill
   */
  public static class PurgeAssignments extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(PurgeAssignments.class, request, "GET", null, MIN_LOG_LEVEL);
      new InnerPurgeAssignments(request, response, LOG).runner(OUTER_CLASS_IF_CREDENTIALS_FAIL);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(PurgeAssignments.class, request, "POST", null, MIN_LOG_LEVEL);
      new InnerPurgeAssignments(request, response, LOG).runner(OUTER_CLASS_IF_CREDENTIALS_FAIL);
    }
  }

  /**
   * web site preferences
   * GET resort=Brighton ID=192443
   * POST todo
   *
   * @author Steve Gledhill
   */
  public static class WebSitePreferences extends HttpServlet {
    private static final int MIN_LOG_LEVEL = Logger.DEBUG;

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(this.getClass(), request, "GET", null, MIN_LOG_LEVEL);
      new InnerWebSitePreferences(request, response, LOG).runner(OUTER_CLASS_IF_CREDENTIALS_FAIL);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(this.getClass(), request, "POST", null, MIN_LOG_LEVEL);
      new InnerWebSitePreferences(request, response, LOG).runner(OUTER_CLASS_IF_CREDENTIALS_FAIL);
    }
  }

  /**
   * update shift templates for this resort
   * GET resort=Brighton ID=192443
   * POST eventName, selectedShift, addShift, shiftCount, delete_#, startTime_#, endTime_#, count_#, shift_#, SaveChangesBtn, DeleteTemplateBtn
   *
   * @author Steve Gledhill
   */
  public static class EditShiftTemplates extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(this.getClass(), request, "GET", null, MIN_LOG_LEVEL);
      new InnerEditShiftTemplates(request, response, LOG).runner(OUTER_CLASS_IF_CREDENTIALS_FAIL);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(this.getClass(), request, "POST", null, MIN_LOG_LEVEL);
      new InnerEditShiftTemplates(request, response, LOG).runner(OUTER_CLASS_IF_CREDENTIALS_FAIL);
    }
  }

  /**
   * process the changes made in ChangeShiftAssignments
   *
   * POST
   *
   * @author Steve Gledhill
   */
  public static class ProcessChangeShiftAssignments extends HttpServlet {
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(this.getClass(), request, "POST", null, MIN_LOG_LEVEL);
      new InnerProcessChangeShiftAssignments(request, response, LOG).runner(OUTER_CLASS_IF_CREDENTIALS_FAIL);
    }
  }

  /**
   * Ask questions to create a fully customized list of patrollers (ListPatrollers generates the list)
   * called from directors page
   * GET
   *
   * @author Steve Gledhill
   */
  public static class SetupListPatrollers extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(this.getClass(), request, "GET", null, MIN_LOG_LEVEL);
      new InnerSetupListPatrollers(request, response, LOG).runner(OUTER_CLASS_IF_CREDENTIALS_FAIL);
    }
  }

  /**
   * send emails
   * 
   * GET (don't know)
   * POST
   * 
   * @author Steve Gledhill
   */
  public static class EmailForm extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(this.getClass(), request, "GET", null, MIN_LOG_LEVEL);
      new InnerEmailForm(request, response, LOG).runner(OUTER_CLASS_IF_CREDENTIALS_FAIL);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(this.getClass(), request, "POST", null, MIN_LOG_LEVEL);
      new InnerEmailForm(request, response, LOG).runner(OUTER_CLASS_IF_CREDENTIALS_FAIL);
    }
  }
}
