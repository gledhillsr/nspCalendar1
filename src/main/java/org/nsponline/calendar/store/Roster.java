package org.nsponline.calendar.store;

import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringEscapeUtils;
import org.nsponline.calendar.utils.Logger;
import org.nsponline.calendar.utils.SaltUtils;
import org.nsponline.calendar.utils.SessionData;
import org.nsponline.calendar.utils.StaticUtils;

/**
 * @author Steve Gledhill
 */
@SuppressWarnings("unused")
public class Roster {
  private final Logger LOG;

  public static final String HARD_SPACE_NBSP = "&nbsp;";
  //static data
  final static Hashtable<String, String> lookupClassification = new Hashtable<>(10);
  final static String[] month = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

  static {
    lookupClassification.put("BAS", "Basic");
//        lookupClassification.put("INA","Inactive");
    lookupClassification.put("SR", "Senior");
    lookupClassification.put("SRA", "Senior Auxiliary");
    lookupClassification.put("ALM", "Alumni");
    lookupClassification.put("PRO", "Pro");
    lookupClassification.put("AUX", "Auxiliary");
    lookupClassification.put("TRA", "Transfer");
    lookupClassification.put("CAN", "Candidate");
    lookupClassification.put("OTH", "Other");
  }

  public final static int DB_NAME = 0;      //EXACTLY as it appears in database
  public final static int DLG_NAME = 1;      //how should it be displayed on edit screens
  public final static int SERVLET_NAME = 2;  //how should it be passes into/from servlet forms
  final static int EDIT_BY_DIRECTOR = 3; //need to be a director to edit these fields (ie ID#, classification, Director)
  final static int TABLE_NAME = 4; //need to be a director to edit these fields (ie ID#, classification, Director)
  final static int TABLE_WIDTH = 5; //need to be a director to edit these fields (ie ID#, classification, Director)
  //note, the values in dbData and the following defines MUST match
//      they are in the ORDER THEY APPEAR ON THE DIALOG
//      not in the order in the actual database
  public static final String[][] dbData = {
//DB_NAME,              DLG_NAME,                SERVLET_NAME    EDIT_BY_DIRECTOR   Column Heading    Column width
//(name in database)  (displayed on dialog)  (passed arg name) (director only edit) (name on printout) (width on printout)
      {"IDNumber", "ID Number:&nbsp;", "IDToEdit", "y", "ID", "40"},      //0
      {"ClassificationCode", "Classification:&nbsp;", "Class", "y", "Class", "50"},      //1
      {"LastName", "Last Name:&nbsp;", "LastName", "n", "Name", "90"},      //2
      {"FirstName", "First Name:&nbsp;", "FirstName", "n", "Name", "90"},      //3
      {"Spouse", "Spouse:&nbsp;", "Spouse", "n", "Spouse", "40"},      //4
      {"Address", "Address:&nbsp;", "Address", "n", "Address&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;", "90"},      //5
      {"City", "City:&nbsp;", "City", "n", "City", "90"},      //6
      {"State", "State:&nbsp;", "State", "n", "State", "50"},      //7
      {"ZipCode", "Zip Code:&nbsp;", "ZipCode", "n", "Zip", "60"},      //8
      {"HomePhone", "Home Phone:&nbsp;", "HomePhone", "n", "Home", "80"},      //9
      {"WorkPhone", "Work Phone:&nbsp;", "WorkPhone", "n", "Work", "80"},      //10
      {"CellPhone", "Cell Phone:&nbsp;", "CellPhone", "n", "Cell", "70"},      //11
      {"Pager", "Pager:&nbsp;", "Pager", "n", "Pager", "70"},      //12
      {"email", "E-mail:&nbsp;", "E-mailAddress", "n", "Email", "130"},     //13
      {"EmergencyCallUp", "Emergency Call Up:&nbsp;", "EmergencyCall", "n", "Call Up", "50"},      //14
      {"Password", "Password:&nbsp;", "Password", "n", "Password", "60"},      //15
      {"NightSubsitute", "Substitute List:&nbsp;", "NightSubsitute", "n", "Sub", "50"},      //16
      {"Commitment", "Commitment:&nbsp;", "Commitment", "n", "Commitment", "50"},      //17
      {"Instructor", "Instructor:&nbsp;", "Instructor", "n", "Instructor", "50"},      //18
      {"Director", "Director:&nbsp;", "Director", "y", "Director", "50"},      //19
      {"teamLead", "Team Lead Trained:", "teamLead", "y", "Team Lead", "40"},          //21
      {"mentoring", "Currently Being Mentored:", "mentoring", "y", "Mentoring", "40"},  //20
      {"lastUpdated", "Last Updated:&nbsp;", "lastUpdated", "y", "Last Updated", "90"},      //22
      {"canEarnCredits", "Can Earn Credits:", "earnCredits", "y", "Can Earn Credits", "40"},      //23
      {"lastCreditUpdate", "Last Update of Credit Info:", "lastCreditUpdate", "y", "Last Credit Update", "80"}, //24
      {"carryOverCredits", "Carry Over Credits:", "carryOverCredits", "y", "Carry Over Credits", "50"},  //25
      {"creditsEarned", "Total Credits Available:", "creditsEarned", "y", "Credits Available", "40"},     //26
      {"creditsUsed", "not used:", "creditsUsed", "y", "Credits Used", "40"},       //27
      {"comments", "Private&nbsp;Comments<br>(<b>Only</b>&nbsp;visible&nbsp;to&nbsp;directors)", "Comments", "y", "Private Comments", "100"},          //28
      {"newPassword", "hash:&nbsp;", "newPassword", "n", "NEVER DISPLAY", "10"},          //29
      {"", "", "", "n", "Day Shifts", "40"},              //30
      {"", "", "", "n", "Swing Shifts", "40"},            //31
      {"", "", "", "n", "Night Shifts", "40"},            //32
      {"", "", "", "n", "Training Shifts", "40"},         //33
      {"", "", "", "n", "Day Shift Details", "200"},     //34
      {"", "", "", "n", "Swing Shift Details", "200"},   //35
      {"", "", "", "n", "Night Shift Details", "200"},   //36
      {"", "", "", "n", "Training Shift Details", "200"},//37
      {"", "", "", "n", "Other Shifts", "40"},           //38
      {"", "", "", "n", "Holiday Shifts", "40"},         //39
      {"", "", "", "n", "Other Shift Details", "200"},   //40
      {"", "", "", "n", "Holiday Shift Details", "200"}  //41
};
  //indexes into dbData array (displayed order, not defined order)
  public final static int ID_NUM = 0;
  public final static int CLASSIFICATION = 1;
  public final static int LAST = 2;
  public final static int FIRST = 3;
  public final static int SPOUSE = 4;
  public final static int ADDRESS = 5;
  public final static int CITY = 6;
  public final static int STATE = 7;
  public final static int ZIPCODE = 8;
  public final static int HOMEPHONE = 9;
  public final static int WORKPHONE = 10;
  public final static int CELLPHONE = 11;
  public final static int PAGER = 12;
  public final static int EMAIL = 13;
  public final static int EMERGENCY = 14;
  public final static int PASSWORD = 15;
  public final static int SUB = 16;
  public final static int COMMITMENT = 17;
  public final static int INSTRUCTOR = 18;
  public final static int DIRECTOR = 19;
  public final static int TEAM_LEAD = 20;
  public final static int MENTORING = 21;
  public final static int LAST_UPDATED = 22;
  public final static int CAN_EARN_CREDITS = 23;
  public final static int LAST_CREDIT_UPDATE = 24;
  public final static int CARRY_OVER_CREDITS = 25;
  public final static int CREDITS_EARNED = 26;
  public final static int CREDITS_USED = 27;
  public final static int COMMENTS = 28;
  public final static int NEW_PASSWORD = 29;
  //remember to update ALL the databases if this changes, eVen though only brighton uses these fields
  public final static int DB_SIZE = 30;     //next available location.  MUST also update "order[]" when increased
  public final static int BLANK = 98;
  public final static int BLANK_WIDE = 99;

  public final static int SHOW_DAY_CNT = DB_SIZE;
  public final static int SHOW_SWING_CNT = DB_SIZE + 1;
  public final static int SHOW_NIGHT_CNT = DB_SIZE + 2;
  public final static int SHOW_TRAINING_CNT = DB_SIZE + 3;

  public final static int SHOW_DAY_LIST = DB_SIZE + 4;
  public final static int SHOW_SWING_LIST = DB_SIZE + 5;
  public final static int SHOW_NIGHT_LIST = DB_SIZE + 6;
  public final static int SHOW_TRAINING_LIST = DB_SIZE + 7;

  //added Oct 7, 2021
  public final static int SHOW_OTHER_CNT = DB_SIZE + 8;
  public final static int SHOW_HOLIDAY_CNT = DB_SIZE + 9;
  public final static int SHOW_OTHER_LIST = DB_SIZE + 10;
  public final static int SHOW_HOLIDAY_LIST = DB_SIZE + 11;

  //-------------------
  static final int FULL_DB_SIZE = SHOW_HOLIDAY_LIST;

  public final static int MAX_MEMBERS = 300;  //todo HACK fix me
  static int HASH_NAMES = 0x0001;
  static int HASH_ASSIGNMENTS = 0x0002;

  static int[] columns = new int[FULL_DB_SIZE];
  static int lastColumn = 0;

  //order is the defined sequence in the database
  static int[] order = {
      ID_NUM, CLASSIFICATION, LAST, FIRST, SPOUSE,
      ADDRESS, CITY, STATE, ZIPCODE,
      HOMEPHONE, WORKPHONE, CELLPHONE, PAGER,
      EMAIL, EMERGENCY, PASSWORD, SUB, COMMITMENT, INSTRUCTOR, DIRECTOR, LAST_UPDATED, COMMENTS,
      CARRY_OVER_CREDITS, LAST_CREDIT_UPDATE, CAN_EARN_CREDITS, CREDITS_EARNED, CREDITS_USED, TEAM_LEAD, MENTORING, NEW_PASSWORD}; //BRIGHTON ONLY

  //instance data
  boolean exceptionError;
  int idNum;
  public String[] memberData;         //data for 'this' member
  public int[] AssignmentCount = new int[Assignments.MAX_SHIFT_TYPES];
  public String[] szAssignments = new String[Assignments.MAX_SHIFT_TYPES];

  public Roster(Logger parentLogger) {
    LOG = parentLogger; //new Logger(Roster.class, parentLogger, null, Logger.INFO);
    idNum = 0;
    for (int i = 0; i < Assignments.MAX_SHIFT_TYPES; ++i) {
      AssignmentCount[i] = 0;
      szAssignments[i] = "";
    }
    memberData = new String[DB_SIZE];           //data for 'this' member
    for (int i = 0; i < DB_SIZE; ++i) {
      memberData[i] = " ";
    }
    memberData[CLASSIFICATION] = "CAN";
    memberData[CAN_EARN_CREDITS] = "0";
    memberData[CARRY_OVER_CREDITS] = "0";
    memberData[LAST_CREDIT_UPDATE] = "0";
    memberData[CREDITS_EARNED] = "0";
    memberData[CREDITS_USED] = "0";
    memberData[TEAM_LEAD] = "0";
    memberData[MENTORING] = "0";
    memberData[COMMENTS] = "";
  }

  public Roster(HttpServletRequest request) {
    LOG = new Logger(Roster.class, request, null, null, Logger.INFO);
    idNum = 0;
    memberData = new String[DB_SIZE];           //data for 'this' member
    for (int i = 0; i < Assignments.MAX_SHIFT_TYPES; ++i) {
      AssignmentCount[i] = 0;
      szAssignments[i] = "";
    }
    for (int i = 0; i < DB_SIZE; ++i) {
      if (i == INSTRUCTOR) {
        int idx = 0;
        if (request.getParameter("OEC") != null) {
          idx += 1;
        }
        if (request.getParameter("CPR") != null) {
          idx += 2;
        }
        if (request.getParameter("SKI") != null) {
          idx += 4;
        }
        if (request.getParameter("TOBOGGAN") != null) {
          idx += 8;
        }
        memberData[i] = "" + idx;
      }
      else if (i == LAST_CREDIT_UPDATE) {
        memberData[i] = "0"; //just incase
        if (request.getParameter("lastCreditUpdate") != null) {
          memberData[i] = request.getParameter("lastCreditUpdate"); //just incase
        }
        else if (request.getParameter("voucherYear") != null) {
          String szDay = request.getParameter("voucherDay");
          String szMonth = request.getParameter("voucherMonth");
          String szYear = request.getParameter("voucherYear");
          //if values are valid, parse into milliseconds
//Log.log("in MemberData constructor, szDay="+szDay+" szMonth="+szMonth+" szYear="+szYear);
          if (szDay != null && szMonth != null && szYear != null) {
            int m, d, y;
            try {
              int year = Integer.parseInt(szYear);
              int month = Integer.parseInt(szMonth);
              int day = Integer.parseInt(szDay);
              Calendar cal = Calendar.getInstance();
              //noinspection MagicConstant
              cal.set(year, month, day);
              memberData[i] = "" + cal.getTimeInMillis();
            }
            catch (Exception e) {
              LOG.logException("Exception in Roster(request): ", e);
            }
          }
        }

      }
      else {
        memberData[i] = request.getParameter(Roster.dbData[i][SERVLET_NAME]);
      }

      if (memberData[i] == null || memberData[i].length() == 0) {
        memberData[i] = " ";
      }
      else if (memberData[i] != null && memberData[i].length() > 1) {
        //kill leading & trailing blanks
        memberData[i] = memberData[i].trim();
        //replace all " characters with a ' character.  The " character is a delimiter for SQL
        memberData[i] = memberData[i].replace('"', '\'');
      }
//Log.log("Constructor-MemberData["+i+"] = ("+memberData[i]+")");
    }

  }

  public int getIdNum() {
    return idNum;
  }

  public void printEmergencyCallRow(PrintWriter out, String other) {
    out.println("<tr>");
    out.println(" <td>" + getFullName_lastNameFirst() + "</td>");
    if (other != null) {
      out.println(" <td ALIGN=\"center\">" + other + "</td>");
    }
    out.println(" <td>" + getHomePhone() + "</td>");
    out.println(" <td>" + getWorkPhone() + "</td>");
    out.println(" <td>" + getCellPhone() + "</td>");
//    out.println(" <td>" + getPager() + "</td>");
    String str = getEmailAddress();
    if (!HARD_SPACE_NBSP.equals(getEmailAddress())) {
      str = "<a href=\"mailto:" + getEmailAddress() + "\">" + getEmailAddress() + "</a>";
    }
    out.println(" <td>" + str + "</td>");
    out.println("</tr>\n");
  }

  public static void addColumn(int index) {
    if (index == -1) {
      lastColumn = 0;
    }
    else if (index < FULL_DB_SIZE) {
      columns[lastColumn++] = index;
    }
    else if (index == BLANK || index == BLANK_WIDE) {
      columns[lastColumn++] = index;
    }

  }

  public static void printMemberListRowHeading(PrintWriter out, String resort) {
//Log.log("in printMemberListRowHeading, lastColumn="+lastColumn);
    for (int i = 0; i < lastColumn; ++i) {
      //String title = (String)columns.elementAt(i);
      int idx = columns[i];
//Log.log("idx="+idx);
      String title = "Error";
      String wid = "Error";
      if (idx < FULL_DB_SIZE) {
//                if(idx == SHOW_NIGHT_CNT && !resort.equalsIgnoreCase("Brighton"))
//                    title = "Shift Cnt";
//                else
        title = dbData[idx][TABLE_NAME];
        wid = dbData[idx][TABLE_WIDTH];
      }
      else if (idx == BLANK) {
        title = "&nbsp";
        wid = "50";
      }
      else if (idx == BLANK_WIDE) {
        title = "&nbsp";
        wid = "200";
      }

      out.println("  <td width=\"" + wid + "\" bgcolor=\"#C0C0C0\"><font face=\"Verdana, Arial, Helvetica\">" + title + "</font></td>");
// font now specified it style
//          out.println("  <td width=\""+wid+"\" bgcolor=\"#C0C0C0\"><font face=\"Verdana, Arial, Helvetica\"><font size=\"2\">"+title+"</font></font></td>");
    }
  }

  public void printMemberListRowData(PrintWriter out) {
    out.println("<tr>");
//System.out.print("----------");
    for (int i = 0; i < lastColumn; ++i) {
      //String title = (String)columns.elementAt(i);
      int idx = columns[i];
//System.out.print(" ("+idx+")");
      String str = "";
      String align = "";
      switch (idx) {
        case BLANK:
        case BLANK_WIDE:
          str = "&nbsp";
          break;
        case ID_NUM:
          str = getID();
          break;
        case CLASSIFICATION:
          str = getClassification();
          break;
        case LAST:
          str = getFullName_lastNameFirst();
          break;
        case FIRST:
          str = getFullName();
          break;
        case SPOUSE:
          str = getSpouse();
          break;
        case ADDRESS:
          str = getAddress();
          break;
        case CITY:
          str = getCity();
          break;
        case STATE:
          str = getState();
          break;
        case ZIPCODE:
          str = getZipCode();
          break;
        case HOMEPHONE:
          str = getHomePhone();
          break;
        case WORKPHONE:
          str = getWorkPhone();
          break;
        case CELLPHONE:
          str = getCellPhone();
          break;
        case PAGER:
          str = getPager();
          break;
        case EMAIL:
          str = getEmailAddress();
          if (!HARD_SPACE_NBSP.equals(getEmailAddress())) {
            str = "<a href=\"mailto:" + getEmailAddress() + "\">" + getEmailAddress() + "</a>";
          }
          break;
        case EMERGENCY:
          str = getEmergency();
          break;
        case PASSWORD:
          str = getPassword();
          break;
        case SUB:
          str = getSub();
          break;
        case COMMITMENT:
          str = getCommitmentString(getCommitment());
          break;
        case INSTRUCTOR:
          str = getInstructorString(getInstructor());
          break;
        case DIRECTOR:
          str = getDirector();
          break;
        case LAST_UPDATED:
          str = getLastUpdated();
          break;
        case COMMENTS:
          str = getComments();
          break;
//        CARRY_OVER_CREDITS,LAST_CREDIT_UPDATE,CAN_EARN_CREDITS, CREDITS_EARNED, CREDITS_USED, TEAM_LEAD, MENTORING }; //BRIGHTON ONLY
//        case CARRY_OVER_CREDITS:
//          str = getCarryOverCredits();
//          align = " align=center";
//          break;
//        case LAST_CREDIT_UPDATE:
//          str = getLastCreditDateStr();
//          break;
//        case CAN_EARN_CREDITS:
//          str = getCanEarnCredits().equals("1") ? "Yes" : "No";
//          align = " align=center";
//          break;
//        case CREDITS_EARNED:
//          str = getCreditsEarned();
//          align = " align=center";
//          break;
//        case CREDITS_USED:
//          str = getCreditsUsed();
//          align = " align=center";
//          break;
        case TEAM_LEAD:
          str = getTeamLead().equals("1") ? "Yes" : "No";
          align = " align=center";
          break;
        case MENTORING:
          str = getMentoring().equals("1") ? "Yes" : "No";
          align = " align=center";
          break;

        case SHOW_NIGHT_CNT:
          str = AssignmentCount[Assignments.NIGHT_TYPE] + "";
          align = " align=center";
          break;
        case SHOW_SWING_CNT:
          str = AssignmentCount[Assignments.SWING_TYPE] + "";
          align = " align=center";
          break;
        case SHOW_DAY_CNT:
          str = AssignmentCount[Assignments.DAY_TYPE] + "";
          align = " align=center";
          break;
        case SHOW_TRAINING_CNT:
          str = AssignmentCount[Assignments.TRAINING_TYPE] + "";
          align = " align=center";
          break;
        case SHOW_OTHER_CNT:
          str = AssignmentCount[Assignments.OTHER_TYPE] + "";
          align = " align=center";
          break;
        case SHOW_HOLIDAY_CNT:
          str = AssignmentCount[Assignments.HOLIDAY_TYPE] + "";
          align = " align=center";
          break;
        case SHOW_NIGHT_LIST:
          str = szAssignments[Assignments.NIGHT_TYPE];
          break;
        case SHOW_SWING_LIST:
          str = szAssignments[Assignments.SWING_TYPE];
          break;
        case SHOW_DAY_LIST:
          str = szAssignments[Assignments.DAY_TYPE];
          break;
        case SHOW_TRAINING_LIST:
          str = szAssignments[Assignments.TRAINING_TYPE];
          break;
        case SHOW_OTHER_LIST:
          str = szAssignments[Assignments.OTHER_TYPE];
          break;
        case SHOW_HOLIDAY_LIST:
          str = szAssignments[Assignments.HOLIDAY_TYPE];
          break;
      }
      out.print(" <td" + align + ">" + str + "</td>");
    }
//Log.log("=======");
    out.println("</tr>\n");
  }

  public static String getCommitmentString(String str) {
    if (str.equals("0")) {
      return "Inactive";  //no longer used
    }
    else if (str.equals("1")) {
      return "Part Time";
    }
    return "Full Time";
  }

  public static String getInstructorString(String szTmp) {
    String str = Roster.HARD_SPACE_NBSP;
    try {
//Log.log("instructor=("+szTmp+")");
      int idx = Integer.parseInt(szTmp);
//Log.log("idx="+idx);
      if ((idx & 1) == 1) {
        str += "OEC";
        idx -= 1;
        if (idx > 1) {
          str += ", ";
        }
      }
      if ((idx & 2) == 2) {
        idx -= 2;
        str += "CPR";
        if (idx > 2) {
          str += ", ";
        }
      }
      if ((idx & 4) == 4) {
        idx -= 4;
        str += "Ski";
        if (idx > 4) {
          str += ", ";
        }
      }
      if ((idx & 8) == 8) {
        str += "Toboggan";
      }
    }
    catch (Exception e) {
      str = "Error, parsing " + szTmp;
    }
    return str;
  }

  public void printMemberListRow(PrintWriter out) {
    printEmergencyCallRow(out, null);
  }

  //todo make MemberData constructor
  public void readFullFromRoster(ResultSet rosterResults, String defaultString) {
    idNum = readInt(rosterResults, dbData[ID_NUM][DB_NAME]);
    for (int i = 0; i < DB_SIZE; ++i) {
      memberData[i] = readString(rosterResults, dbData[i][DB_NAME], defaultString);
    }
  }

  private int readInt(ResultSet rosterResults, String tag) {
    try {
      return rosterResults.getInt(tag);
    }
    catch (Exception e) {
      exceptionError = true;
      return 0;
    } //end try
  }

  private String readString(ResultSet rosterResults, String tag, String szDefault) {
    try {
      String originalValue = rosterResults.getString(tag);
      String str = originalValue == null ? "" : originalValue.replaceAll("''", "'");

//todo srg 2017 - decode all roster data here
      String unescapedStr = StringEscapeUtils.unescapeHtml4(str);
      if (!testIfValid(unescapedStr)) {
        return szDefault;
      }
      else {
        return unescapedStr;
      }
    }
    catch (Exception e) {
      exceptionError = true;
      return null;
    } //end try
  }

  public String getFullClassification() {
    String classification = null;
    try {
      classification = lookupClassification.get(getClassification());
    }
    catch (Exception ex) {
      LOG.error("ERROR, classification not found" + getClassification());
    }
    return classification;
  }

  public String getExcelHeader() {
    int i;
    StringBuilder qryString = new StringBuilder();
    for (i = 0; i < DB_SIZE; ++i) {
      if (i > 0) {
        qryString.append("\t");      //tab between fields
      }
      qryString.append(dbData[order[i]][DB_NAME]);
    }
//Log.log(qryString);
    return qryString.toString();
  }

  public String getExcelString() {
    int i;
    StringBuilder qryString = new StringBuilder();
    String szTmp;
    for (i = 0; i < DB_SIZE; ++i) {
      szTmp = memberData[order[i]].replace(',', ' ');  //remove all commas
      if (i != 0) {
        qryString.append("\t");
      }
      qryString.append(szTmp);
    }
//Log.log(qryString);
    return qryString.toString();
  }

  public String getInsertSQLString(String resort, SessionData sessionData) {
    int i;
    //at time of insert/Modify, set the lastModified date value
//lastUpdated
    setLastUpdatedFieldToNow();

    String qryString = "INSERT INTO roster (";
    qryString += buildFieldsForInsertQuery();
    qryString += ") VALUES (";

    qryString += buildValuesForInsertQuery(sessionData);
    qryString += " )";
    LOG.logSqlStatement(qryString);
    return qryString;
  }

  private String buildFieldsForInsertQuery() {
    int i;
    StringBuilder qryString = new StringBuilder();
    for (i = 0; i < DB_SIZE; ++i) {
      qryString.append(dbData[order[i]][DB_NAME]);
      if (i < DB_SIZE - 1) {
        qryString.append(", ");
      }
    }
    return qryString.toString();
  }


  public String getUpdateSQLString(String resort, SessionData sessionData) {
//at time of insert/Modify, set the lastUpdated date value
    setLastUpdatedFieldToNow();
//UPDATE phone_book SET name = 'John Doe', number = '555-1212'
    LOG.debug("starting getUpdateRosterSQLString DB_SIZE=" + DB_SIZE);
    String qryString = "UPDATE roster SET ";
    qryString += buildBodyForUpdateQuery(sessionData);
    qryString += " WHERE " + dbData[ID_NUM][DB_NAME] + " =" + memberData[ID_NUM];
    LOG.logSqlStatement(qryString);
    return qryString;
  }

  private void setLastUpdatedFieldToNow() {
    StringBuilder str = new StringBuilder();
    Calendar rightNow = Calendar.getInstance();
    int m = rightNow.get(Calendar.MONTH) + 1;
    int d = rightNow.get(Calendar.DATE);
    int y = rightNow.get(Calendar.YEAR);

    str.append(y).append("-");
    if (m < 10) {
      str.append("0");
    }
    str.append(m).append("-");
    if (d < 10) {
      str.append("0");
    }
    str.append(d);
    memberData[LAST_UPDATED] = str.toString();
  }

  private String buildValuesForInsertQuery(SessionData sessionData) {
    //NOTE: any changes to this code, should also be reflected in buildBodyForUpdateQuery
    int i;
    StringBuilder qryString = new StringBuilder();
    for (i = 0; i < DB_SIZE; ++i) {
      String originalValue = memberData[order[i]];
      //this mapping must also be done on deserialization 'O'Keefe' -> 'O''Keefe'
      String valueStr = originalValue == null ? "" : originalValue.replaceAll("'", "''");
      if (isUnsupportedCreditsField(order[i])) {
        valueStr = "0";
      }
      else if ( i == NEW_PASSWORD) {
        //new password is calculated,  it did not previously exist
        try {
          SaltUtils saltShaker = new SaltUtils();
          valueStr = saltShaker.hashPassword(sessionData, memberData[PASSWORD]);
          /*
          TODO FUTURE, zero out PASSWORD memberData[PASSWORD]
           */
        }
        catch (NoSuchAlgorithmException e) {
          e.printStackTrace();
        }
      }
      qryString.append("'").append(valueStr).append("'");
      if (i < DB_SIZE - 1) {
        qryString.append(", ");
      }
    }
    return qryString.toString();
  }

  @SuppressWarnings("DuplicatedCode")
  private String buildBodyForUpdateQuery(SessionData sessionData) {
    //NOTE: any changes to this code, should also be reflected in buildValuesForInsertQuery
    int i;
    StringBuilder qryString = new StringBuilder();
    for (i = 1; i < DB_SIZE; ++i) { //don't start with the key value (ID_NUM)
      String originalValue = memberData[order[i]];
      //this mapping must also be done on deserialization 'O'Keefe' -> 'O''Keefe'
      String valueStr = originalValue == null ? "" : originalValue.replaceAll("'", "''");
      //CARRY_OVER_CREDITS & CREDITS_USED
      if (isUnsupportedCreditsField(order[i])) {
        valueStr = "0";
      }
      else if (order[i] == PASSWORD && (memberData[PASSWORD] == null || memberData[PASSWORD].trim().isEmpty())) {
        //the password field is blank, which is normal when not CHANGING the password.  So DON'T accidentally overwrite the existing password
        continue;
      }
      else if ( order[i] == NEW_PASSWORD) {
        if (memberData[PASSWORD] == null || memberData[PASSWORD].trim().isEmpty()) {
          //the password field is blank, which is normal when not CHANGING the password.  So DON'T accidentally overwrite the existing "new password"
          continue;
        }
        //the "new password" is calculated from a non-empty password field
        try {
          SaltUtils saltShaker = new SaltUtils();
          valueStr = saltShaker.hashPassword(sessionData, memberData[PASSWORD]);
          /*
          TODO FUTURE, zero out PASSWORD memberData[PASSWORD]
           */
        }
        catch (NoSuchAlgorithmException e) {
          e.printStackTrace();
        }
      }
      qryString.append(dbData[order[i]][DB_NAME]).append(" = \"").append(valueStr).append("\" ");
      if (i < DB_SIZE - 1) {
        qryString.append(", ");
      }
    }
    String qry = qryString.toString();
    //if body ends in a ", " then trim that off
    if (qry.endsWith(", ")) {
      int last = qry.length() - 2;
      return qry.substring(0, last);
    }
    return qry;
  }

  public String getDeleteSQLString(String resort) {
    String qryString = "DELETE FROM roster WHERE " + dbData[ID_NUM][DB_NAME] + " = '" + memberData[ID_NUM] + "'";
    LOG.logSqlStatement(qryString);
    return qryString;
  }

  public boolean isDirector() {
    String dir = getDirector();
    if (testIfValid(dir)) {
      return dir.substring(0, 1).equalsIgnoreCase("Y");
    }
    return false;
  }

  private boolean testIfValid(String str) {
    if (str == null) {
      return false;
    }
    //noinspection SimplifiableIfStatement
    if (str.equals("null")) {
      return false;
    }
    return (!str.equals("") && !str.equals(" "));
  }

  public String getFullName() {
    return memberData[FIRST] + " " + memberData[LAST];
  }

  public String getFullName_lastNameFirst() {
    return memberData[LAST] + ", " + memberData[FIRST];
  }

  public String getLast() {
    return memberData[LAST];
  }

  public String getFirst() {
    return memberData[FIRST];
  }

  public String getSpouse() {
    return memberData[SPOUSE];
  }

  public String getAddress() {
    return memberData[ADDRESS];
  }

  public String getCity() {
    return memberData[CITY];
  }

  public String getState() {
    return memberData[STATE];
  }

  public String getZipCode() {
    return memberData[ZIPCODE];
  }

  public String getHomePhone() {
    return memberData[HOMEPHONE];
  }

  public String getWorkPhone() {
    return memberData[WORKPHONE];
  }

  public String getCellPhone() {
    return memberData[CELLPHONE];
  }

  public String getPager() {
    return memberData[PAGER];
  }

  public String getEmailAddress() {
    return memberData[EMAIL];
  }

  public String getClassification() {
    return memberData[CLASSIFICATION];
  }

  public String getID() {
    return memberData[ID_NUM];
  }

  public String getEmergency() {
    return memberData[EMERGENCY];
  }

  public String getPassword() {
    return memberData[PASSWORD];
  }

  public String getCommitment() {
    return memberData[COMMITMENT];
  }

  public String getInstructor() {
    return memberData[INSTRUCTOR];
  }

  public String getDirector() {
    return memberData[DIRECTOR];
  }

  public String getSub() {
    return memberData[SUB];
  }

  public String getLastUpdated() {
    return memberData[LAST_UPDATED];
  }

  public String getComments() {
    return memberData[COMMENTS];
  }

  public String getCanEarnCredits() {
    return memberData[CAN_EARN_CREDITS];
  }

  public String getCreditsEarned() {
    return memberData[CREDITS_EARNED];
  }

  public String getCreditsUsed() {
    return "0"; /*memberData[CREDITS_USED]; */
  }

  public String getTeamLead() {
    return memberData[TEAM_LEAD];
  }

  public String getMentoring() {
    return memberData[MENTORING];
  }

  public String getCarryOverCredits() {
    return "0"; /* memberData[CARRY_OVER_CREDITS]; */
  }

  public String getLastCreditDate() {
    return memberData[LAST_CREDIT_UPDATE];
  }

  public static boolean isUnsupportedCreditsField(int i) {
    return i == Roster.CAN_EARN_CREDITS || i == Roster.LAST_CREDIT_UPDATE || i == Roster.CARRY_OVER_CREDITS || i == Roster.CREDITS_EARNED || i == Roster.CREDITS_USED;
  }

  public String getLastCreditDateStr() {
    String szDate = Roster.HARD_SPACE_NBSP;
    String str = memberData[LAST_CREDIT_UPDATE];
    if (str.length() > 8) {
      try {
        Calendar cal = Calendar.getInstance();
        long millis = Long.parseLong(str);
        cal.setTimeInMillis(millis);
        szDate = month[cal.get(Calendar.MONTH)] + "-" + cal.get(Calendar.DATE) + "-" + cal.get(Calendar.YEAR);
      }
      catch (Exception e) {
        //IGNORE
      }
    }
    return szDate;
  }

  public void setLast(String str) {
    memberData[LAST] = str;
  }

  public void setFirst(String str) {
    memberData[FIRST] = str;
  }

  public void setSpouse(String str) {
    memberData[SPOUSE] = str;
  }

  public void setAddress(String str) {
    memberData[ADDRESS] = str;
  }

  public void setCity(String str) {
    memberData[CITY] = str;
  }

  public void setState(String str) {
    memberData[STATE] = str;
  }

  public void setZipCode(String str) {
    memberData[ZIPCODE] = str;
  }

  public void setHomePhone(String str) {
    memberData[HOMEPHONE] = str;
  }

  public void setWorkPhone(String str) {
    memberData[WORKPHONE] = str;
  }

  public void setCellPhone(String str) {
    memberData[CELLPHONE] = str;
  }

  public void setPager(String str) {
    memberData[PAGER] = str;
  }

  public void setEmail(String str) {
    memberData[EMAIL] = str;
  }

  public void setClassification(String str) {
    memberData[CLASSIFICATION] = str;
  }

  public void setID(String str) {
    memberData[ID_NUM] = str;
  }

  public void setEmergency(String str) {
    memberData[EMERGENCY] = str;
  }

  public void setPassword(String str) {
    memberData[PASSWORD] = str;
  }

  public void setCommitment(String str) {
    memberData[COMMITMENT] = str;
  }

  public void setInstructor(String str) {
    memberData[INSTRUCTOR] = str;
  }

  public void setDirector(String str) {
    memberData[DIRECTOR] = str;
  }

  public void setSub(String str) {
    memberData[SUB] = str;
  }

  public void setComments(String str) {
    str = str.replaceAll("\"", "'");
    memberData[COMMENTS] = str;
  }

  public void setLastUpdated(String str) {
    memberData[LAST_UPDATED] = str;
  }

  public void setCarryOverCredits(String str) {
    memberData[CARRY_OVER_CREDITS] = str;
  }

  public void setLastCreditDate(String str) {
    memberData[LAST_CREDIT_UPDATE] = str;
  }

  public void setCanEarnCredits(String str) {
    memberData[CAN_EARN_CREDITS] = str;
  }

  public boolean okToDisplay(boolean EveryBody, boolean SubList, boolean bListAll,
                      Vector vClassificationsToDisplay, int iCommitmentToDisplay,
                      boolean listDirector, int instructorFlags, int MinDays) {

    boolean ok = false;
//----check overrides
    if (EveryBody) {
      return true;
    }

    if (SubList) {
      String sub = getSub();
      //noinspection RedundantIfStatement
      if (sub != null && sub.length() >= 1 && sub.substring(0, 1).equalsIgnoreCase("Y")) {
        return true;
      }
      return false;
    }
//----end overrides

//1st test classification
    if (!vClassificationsToDisplay.contains(getClassification())) {
      return false;
    }

//AND 2nd test commitment
    String str = getCommitment();
    int value = 3;
    try {
      value = Integer.parseInt(str);
    }
    catch (Exception e) {
      //ignore
    }
//Log.log("commitment status = "+value);
    if (value == 3 || iCommitmentToDisplay == 0 || ((1 << value & iCommitmentToDisplay) == 0)) {
      return false;
    }

//AND 3rd test Everyone/director/cpr/oec/ski/toboggan
//Log.log(member.getID()+ ") listDirector = "+listDirector+"  isDirector="+member.isDirector());
    if (bListAll) {
      ok = true;
    }

    if (listDirector && isDirector()) {
      ok = true;
    }
    value = 0;
    try {
      str = getInstructor();
      value = Integer.parseInt(str);
    }
    catch (Exception e) {
      //ignore
    }

    if ((instructorFlags & value) > 0) {
      ok = true;
    }
    if (!ok) {
      return false;
    }
// end of 3rd group ----

    if (MinDays > 0) {
      int totalDays = 0;
      for (int j = 0; j < Assignments.MAX_SHIFT_TYPES; ++j) {
        totalDays += AssignmentCount[j];
      }
//Log.log("in OKToDisplay, "+getID()+": totalDays="+totalDays+", MinDays="+MinDays);
      return totalDays < MinDays;
    }

    return true;
  }


  public String toString() {
    return memberData[ID_NUM] + "  " + memberData[FIRST] + " " + memberData[LAST];
  }

  public ObjectNode toNode() {
    ObjectNode returnNode = StaticUtils.nodeFactory.objectNode();
    returnNode.put("IDNumber", getID());
    setIfNotEmpty(returnNode, "ClassificationCode", getClassification());
    setIfNotEmpty(returnNode, "ClassificationCode", getClassification());
    setIfNotEmpty(returnNode, "LastName", getLast());
    setIfNotEmpty(returnNode, "FirstName", getFirst());
    setIfNotEmpty(returnNode, "Spouse", getSpouse());
    setIfNotEmpty(returnNode, "Address", getAddress());
    setIfNotEmpty(returnNode, "City", getCity());
    setIfNotEmpty(returnNode, "State", getState());
    setIfNotEmpty(returnNode, "ZipCode", getZipCode());
    setIfNotEmpty(returnNode, "HomePhone", getHomePhone());
    setIfNotEmpty(returnNode, "WorkPhone", getWorkPhone());
    setIfNotEmpty(returnNode, "CellPhone", getCellPhone());
    setIfNotEmpty(returnNode, "Pager", getPager());
    setIfNotEmpty(returnNode, "email", getEmailAddress());
    setIfNotEmpty(returnNode, "EmergencyCallUp", getEmergency());
    setIfNotEmpty(returnNode, "NightSubsitute", getSub());
    setIfNotEmpty(returnNode, "Commitment", getCommitment());
    setIfNotEmpty(returnNode, "Instructor", getInstructor());
    setIfNotEmpty(returnNode, "Director", getDirector());
    setIfNotEmpty(returnNode, "teamLead", getTeamLead());
    setIfNotEmpty(returnNode, "mentoring", getMentoring());
    setIfNotEmpty(returnNode, "lastUpdated", getLastUpdated());

    return returnNode;
  }

  private void setIfNotEmpty(ObjectNode returnNode, String key, String value) {
    if (StaticUtils.isNotEmpty(value)) {
      returnNode.put(key, value);
    }
  }

} //end MemberData class

