package org.nsponline.calendar;

/**
 * @author Steve Gledhill
 */

import java.io.PrintWriter;
import java.util.*;

public class DailyReminder {
  String szDay[] = {"Error", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
  String szMonth[] = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
//  int status = 0;
//  PatrolData patrol = null;
  //    String from = "Steve@Gledhills.com <Automated Patrol Reminder>";
//  String from = "Steve@Gledhills.com";
//  String smtp = "mail.gledhills.com";
/* Creates a new instance of reminder */

  public DailyReminder(String resort, SessionData sessionData, MailMan mail) {
    System.out.println("processing email reminders for resort: " + resort);
    PatrolData patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData);
    DirectorSettings ds = patrol.readDirectorSettings();
    if (!ds.getSendReminder()) {
      System.out.println("Don't send email reminders for " + resort + ". The director settings denied this...");
      return;
    }
    int daysAhead = ds.getReminderDays();

    GregorianCalendar assignmentDate = getRemingerDateToSend(daysAhead);
    checkAndSend(assignmentDate, mail, resort, patrol);
//NOTE: important for this logic.
//      This program is only run Monday through Friday (morning).  Not on Saturday or Sunday
//       (since many people don't read email consistently on the weekend)
//Also, a daysAhead value of 0 is not valid
    if (assignmentDate.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
      //Sunday
      assignmentDate = getRemingerDateToSend(daysAhead + 1);
      checkAndSend(assignmentDate, mail, resort, patrol);
      //Monday
      assignmentDate = getRemingerDateToSend(daysAhead + 2);
      checkAndSend(assignmentDate, mail, resort, patrol);
    }
    System.out.println("finished processing ALL reminders");
    patrol.close();
  }

  private static GregorianCalendar getRemingerDateToSend(int daysAhead) {
    GregorianCalendar today = new GregorianCalendar();
    System.out.println("reminder daysAhead=" + daysAhead);
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

  private void checkAndSend(GregorianCalendar date, MailMan mail, String resort, PatrolData patrol) {
    Set<String> emailTo;
    String formattedDateString = getAssignmentDateString(date);
    int dayOfWeek = date.get(Calendar.DAY_OF_WEEK);
    int month = date.get(Calendar.MONTH);
//System.out.println("dayOfWeek="+dayOfWeek+" ("+szDay[dayOfWeek]+")");
    patrol.resetAssignments();
    Assignments assignment;
    while ((assignment = patrol.readNextAssignment()) != null) {
      String assignDate = assignment.getDateOnly();
      if (!formattedDateString.equals(assignDate)) {
        continue;
      }

      System.out.println("Assignment=" + assignment);
      String message = "Reminder\n\nYou are scheduled to Ski Patrol at " + resort + ", on " + szDay[dayOfWeek] + ", " + szMonth[month] + " " + date.get(Calendar.DAY_OF_MONTH) + ", " + date.get(Calendar.YEAR) + " from " +
          assignment.getStartingTimeString() + " to " +
          assignment.getEndingTimeString() + ".\n\nThanks, your help is greatly appreciated.\n\n";
      message += "Please do NOT reply to this automated reminder. \nUnless, you are NOT a member of the National Ski Patrol, and received this email accidently.";
      emailTo = getMemberEmailsWhoHaveAssignment(assignment, patrol);
      sendEmail(emailTo, mail, message);
    } //end loop for assignments
  }

  private Set<String> getMemberEmailsWhoHaveAssignment(Assignments assignment, PatrolData patrol) {
    Set<String> emailTo = new HashSet<String>();
    for (int i = 0; i < assignment.getCount(); ++i) {
      String id = assignment.getPosID(i);
      System.out.print(id + " ");
      MemberData member = patrol.getMemberByID(id);
      if (member != null) {
        String em = member.getEmail();
        System.out.println(member.getFullName() + " " + em);
        //check for valid email
        if (em != null && em.length() > 6 && em.indexOf('@') > 0 && em.indexOf('.') > 0) {
          emailTo.add(em);
        }
      }
      System.out.println();
    }
    return emailTo;
  }

  private void sendEmail(Set<String> emailTo, MailMan mail, String message) {
    try {
      System.out.println("Message:\n" + message);    //no e-mail, JUST LOG IT
      for (String emailAddress : emailTo) {
        mail.sendMessage("Ski Patrol Shift Reminder", message, emailAddress);
        //todo some day, just pass the entire set, the API's allow this
        System.out.println("mail sent to: " + emailAddress);    //no e-mail, JUST LOG IT
      }
    }
    catch (Exception ex) {
      System.out.println("error " + ex);
      System.out.println("attempting to send mail to: " + emailTo);    //no e-mail, JUST LOG IT
    } //end try/catch
  }


  /**
   * @param args the command line arguments (none, send to all resorts)
   */
  public static void main(String[] args) {
    Properties properties = System.getProperties();
    PrintWriter out = new PrintWriter(System.out);
    SessionData sessionData = new SessionData(properties, out);

    if (sessionData.getDbPassword() == null) {
      System.out.println("error session credentials not found");
      System.exit(1);
    }
    //setup credentials and connection
    MailMan mail = new MailMan(sessionData.getSmtpHost(), sessionData.getEmailUser(), "Automated Ski Patrol Reminder", sessionData);
    //loop for resorts
    for (String resort : PatrolData.resortMap.keySet()) {
      new DailyReminder(resort, sessionData, mail);
    }
  }

}
