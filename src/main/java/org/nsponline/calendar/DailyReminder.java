package org.nsponline.calendar;

import org.nsponline.calendar.utils.*;
import org.nsponline.calendar.store.Assignments;
import org.nsponline.calendar.store.DirectorSettings;
import org.nsponline.calendar.store.Roster;

import java.io.PrintWriter;
import java.util.*;

public class DailyReminder {
  final private static boolean DEBUG = true;
  private Logger LOG;

  public DailyReminder(String resort, SessionData sessionData, MailMan mail) {
    LOG = new Logger(this.getClass(), null, "DailyReminder", "DailyReminder", Logger.INFO);
    LOG.info("*** Processing email reminders for resort=" + resort);
    PatrolData patrol = new PatrolData(resort, sessionData, LOG);
    DirectorSettings ds = patrol.readDirectorSettings();
    if (!ds.getSendReminder()) {
      LOG.info("Don't send email reminders for resort=" + resort + ". The director settings denied this...");
      return;
    }
    int daysAhead = ds.getReminderDays();

    GregorianCalendar assignmentDate = getReminderDateToSend(daysAhead);
    int messagesSent = checkAndSend(sessionData, assignmentDate, mail, resort, patrol);
    LOG.info("finished processing ALL reminders for resort=" + resort + ", messagesSent=" + messagesSent);
    patrol.close();
  }

  private GregorianCalendar getReminderDateToSend(int daysAhead) {
    GregorianCalendar today = new GregorianCalendar();
    LOG.info("reminder daysAhead=" + daysAhead);
    GregorianCalendar date = new GregorianCalendar();
    long millis = today.getTimeInMillis() + (24 * 3600 * 1000 * daysAhead);
    date.setTimeInMillis(millis);
    return date;
  }

  private String getAssignmentDateString(GregorianCalendar date) {
    int month = date.get(Calendar.MONTH) + 1;
    int day = date.get(Calendar.DATE);
    String testDate = date.get(Calendar.YEAR) + "-";
    if (month < 10) {
      testDate += "0";
    }
    testDate += month + "-";
    if (day < 10) {
      testDate += "0";
    }
    testDate += day;
    return testDate;
  }

  private int checkAndSend(SessionData sessionData, GregorianCalendar date, MailMan mail, String resort, PatrolData patrol) {
    Set<String> emailTo;
    int messagesSent = 0;
    String formattedDateString = getAssignmentDateString(date);
    int dayOfWeek = date.get(Calendar.DAY_OF_WEEK) - 1;  //String stored 0 based, this API is 1 based
    int month = date.get(Calendar.MONTH);
    int year = date.get(Calendar.YEAR);
    int dayOfMonth = date.get(Calendar.DAY_OF_MONTH);
//Log.log("dayOfWeek="+dayOfWeek+" ("+szDay[dayOfWeek]+")");
//    patrol.resetAssignments();
//    Assignments assignment;
    for (Assignments assignment : patrol.readSortedAssignments(year, month + 1, dayOfMonth)) {
//    while ((assignment = patrol.readNextAssignment()) != null) {
      String assignDate = assignment.getDateOnly();
      if (!formattedDateString.equals(assignDate)) {
        debugOut("**** THIS SHOULD NEVER HAPPEN *****");
        continue;
      }

      String resortStr = PatrolData.getResortFullName(resort, LOG);
      debugOut("Assignment=" + assignment.toString());
      String message = "Reminder\n\nYou are scheduled to Ski Patrol at " + resortStr + ", on " + StaticUtils.szDays[dayOfWeek] + ", " + StaticUtils.szMonthsFull[month] + " " + date.get(Calendar.DAY_OF_MONTH) + ", " + date.get(Calendar.YEAR) + " from " +
          assignment.getStartingTimeString() + " to " +
          assignment.getEndingTimeString() + ".\n\nThanks, your help is greatly appreciated.\n\n";
      message += "Please do NOT reply to this automated reminder. \nUnless, you are NOT a member of the National Ski Patrol, and received this email accidentally.";
      emailTo = getMemberEmailsWhoHaveAssignment(assignment, patrol);
      if (!emailTo.isEmpty()) {
        messagesSent += sendEmail(sessionData, emailTo, mail, message);
      }
      else {
        debugOut("Warning: there were no patrollers signed up for this shift");
      }
    } //end loop for assignments
    return messagesSent;
  }

  private Set<String> getMemberEmailsWhoHaveAssignment(Assignments assignment, PatrolData patrol) {
    Set<String> emailTo = new HashSet<String>();
    for (int i = 0; i < assignment.getCount(); ++i) {
      String id = assignment.getPosID(i);
      if ("0".equals(id)) {
        continue;
      }
      System.out.print(id + " ");
      Roster member = patrol.getMemberByID(id);
      if (member != null) {
        String emailAddress = member.getEmailAddress();
        //check for valid email
        if (StaticUtils.isValidEmailAddress(emailAddress)) {
          debugOut(i + ") " + member.getFullName() + " " + emailAddress);
          emailTo.add(emailAddress);
        }
        else {
          if (StaticUtils.isEmpty(emailAddress)) {
            debugOut("WARNING, missing email address for " + member.getFullName());
          }
          else {
            debugOut("WARNING, invalid email address. " + member.getFullName() + " [" + emailAddress + "]");
          }
        }
      }
    }
    return emailTo;
  }

  private void debugOut(String msg) {
    if (DEBUG) {
      LOG.debug("DailyReminder=" + msg);
    }
  }

  private int sendEmail(SessionData sessionData, Set<String> emailTo, MailMan mail, String message) {
//todo, this should work ? (instead of for loop below)
//      String[] addressToArray = (String[]) emailTo.toArray();
//      mail.sendMessage(sessionData, "Ski Patrol Shift Reminder", message, addressToArray);
    int emailsSent = 0;
    for (String emailAddress : emailTo) {
      mail.sendMessage("Ski Patrol Shift Reminder", message, emailAddress);
      ++emailsSent;
    }
    return emailsSent;
  }


  /**
   * @param args the command line arguments (none, send to all resorts)
   */
  public static void main(String[] args) {    //this NEEDS to be static
    Properties properties = System.getProperties();
    PrintWriter out = new PrintWriter(System.out);
    SessionData sessionData = new SessionData(properties, out);
    Logger LOG = new Logger(DailyReminder.class, null, "DailyReminder", "DailyReminder", Logger.INFO);
    out.println("******************************************************");
    out.println("START RUNNING DAILY REMINDER: " + new Date().toString());
    out.println("******************************************************");
    if (sessionData.getDbPassword() == null) {
      LOG.error("error session credentials not found");
      System.exit(1);
    }
    //setup credentials and connection
    //loop for resorts
    for (String resort : PatrolData.resortMap.keySet()) {
      if (!"Sample".equals(resort)) {
        sessionData.setLoggedInResort(resort); //used to set From field in emails (if director has SES verified address)
        MailMan mail = new MailMan(sessionData.getEmailUser(), sessionData, LOG);
        new DailyReminder(resort, sessionData, mail);
      }
    }
  }

}
