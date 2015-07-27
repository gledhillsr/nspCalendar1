package org.nsponline.calendar;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Vector;

/**
 * @author Steve Gledhill
 */
@SuppressWarnings("unused")
public class MemberData {

  public static final String HARD_SPACE_NBSP = "&nbsp;";
  //static data
  final static Hashtable<String, String> lookupClassification = new Hashtable<String, String>(10);
  final static String month[] = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

  static {
    lookupClassification.put("BAS", "Basic");
//        lookupClassification.put("INA","Inactive");
    lookupClassification.put("SR", "Senior");
    lookupClassification.put("SRA", "Senior Auxilary");
    lookupClassification.put("ALM", "Alumni");
    lookupClassification.put("PRO", "Pro");
    lookupClassification.put("AUX", "Aulilary");
    lookupClassification.put("TRA", "Transfer");
    lookupClassification.put("CAN", "Candidate");
    lookupClassification.put("OTH", "Other");
  }

  final static int DB_NAME = 0;      //EXACTLY as it appears in database
  final static int DLG_NAME = 1;      //how should it be displayed on edit screens
  final static int SERVLET_NAME = 2;  //how should it be passes into/from servlet forms
  final static int EDIT_BY_DIRECTOR = 3; //need to be a director to edit these fields (ie ID#, classification, Director)
  final static int TABLE_NAME = 4; //need to be a director to edit these fields (ie ID#, classification, Director)
  final static int TABLE_WIDTH = 5; //need to be a director to edit these fields (ie ID#, classification, Director)
  //note, the values in dbData and the following defines MUST match
//      they are in the ORDER THEY APPEAR ON THE DIALOG
//      not in the order in the actual database
  final static String dbData[][] = {
//DB_NAME,              DLG_NAME,                SERVLET_NAME    EDIT_BY_DIRECTOR   Column Heading    Column width
//(name in database)  (displayed on dialog)  (passed arg name) (who can edit) (name on printout) (width on printout)
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
      {"NightSubsitute", "Subsitute List:&nbsp;", "NightSubsitute", "n", "Sub", "50"},      //16
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
      {"", "", "", "n", "Day Shifts", "40"},              //29
      {"", "", "", "n", "Swing Shifts", "40"},            //30
      {"", "", "", "n", "Night Shifts", "40"},            //31
      {"", "", "", "n", "Training Shifts", "40"},         //32
      {"", "", "", "n", "Day Shift Details", "200"},     //33
      {"", "", "", "n", "Swing Shift Details", "200"},   //34
      {"", "", "", "n", "Night Shift Details", "200"},   //35
      {"", "", "", "n", "Training Shift Details", "200"}}; //36
  //indexes into dbData array (displayed order, not defined order)
  static final int ID_NUM = 0;
  static final int CLASSIFICATION = 1;
  static final int LAST = 2;
  static final int FIRST = 3;
  static final int SPOUSE = 4;
  static final int ADDRESS = 5;
  static final int CITY = 6;
  static final int STATE = 7;
  static final int ZIPCODE = 8;
  static final int HOMEPHONE = 9;
  static final int WORKPHONE = 10;
  static final int CELLPHONE = 11;
  static final int PAGER = 12;
  static final int EMAIL = 13;
  static final int EMERGENCY = 14;
  static final int PASSWORD = 15;
  static final int SUB = 16;
  static final int COMMITMENT = 17;
  static final int INSTRUCTOR = 18;
  static final int DIRECTOR = 19;
  static final int TEAM_LEAD = 20;
  static final int MENTORING = 21;
  static final int LAST_UPDATED = 22;
  static final int CAN_EARN_CREDITS = 23;
  static final int LAST_CREDIT_UPDATE = 24;
  static final int CARRY_OVER_CREDITS = 25;
  static final int CREDITS_EARNED = 26;
  static final int CREDITS_USED = 27;
  static final int COMMENTS = 28;
  //remember to update ALL the databases if this changes, eVen though only brighton uses these fields
  static final int DB_SIZE = 29;     //next available location
  static final int BLANK = 98;
  static final int BLANK_WIDE = 99;

  static final int SHOW_DAY_CNT = DB_SIZE;
  static final int SHOW_SWING_CNT = DB_SIZE + 1;
  static final int SHOW_NIGHT_CNT = DB_SIZE + 2;
  static final int SHOW_TRAINING_CNT = DB_SIZE + 3;

  static final int SHOW_DAY_LIST = DB_SIZE + 4;
  static final int SHOW_SWING_LIST = DB_SIZE + 5;
  static final int SHOW_NIGHT_LIST = DB_SIZE + 6;
  static final int SHOW_TRAINING_LIST = DB_SIZE + 7;
  //-------------------
  static final int FULL_DB_SIZE = SHOW_TRAINING_LIST;

  static int MAX_MEMBERS = 300;
  static int HASH_NAMES = 0x0001;
  static int HASH_ASSIGNMENTS = 0x0002;

  static int[] columns = new int[FULL_DB_SIZE];
  static int lastColumn = 0;
//  final static Hashtable columnInfo=new Hashtable(10);
//  static {
//      for(int i=0; i < DB_SIZE; ++i) {
//          int w = 50;
//          try {
//              w = Integer.parseInt(dbData[i][TABLE_WIDTH]);
//          } catch (Exception e) {}
//          columnInfo.put(dbData[i][TABLE_NAME],new Integer(i));
//      }
//  }

  //order is the defined sequence in the database
  static int order[] = {
      ID_NUM, CLASSIFICATION, LAST, FIRST, SPOUSE,
      ADDRESS, CITY, STATE, ZIPCODE,
      HOMEPHONE, WORKPHONE, CELLPHONE, PAGER,
      EMAIL, EMERGENCY, PASSWORD, SUB, COMMITMENT, INSTRUCTOR, DIRECTOR, LAST_UPDATED, COMMENTS,
      CARRY_OVER_CREDITS, LAST_CREDIT_UPDATE, CAN_EARN_CREDITS, CREDITS_EARNED, CREDITS_USED, TEAM_LEAD, MENTORING}; //BRIGHTON ONLY
//CARRY_OVER_CREDITS, CREDITS_EARNED, CREDITS_USED }; //BRIGHTON ONLY

  //instance data
  boolean exceptionError;
  int idNum;
  public String[] memberData;         //data for 'this' member
  public int[] AssignmentCount = new int[Assignments.MAX_SHIFT_TYPES];
  public String[] szAssignments = new String[Assignments.MAX_SHIFT_TYPES];

  /***************/
    /* constructor */

  /***************/
  public MemberData() {
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

  /***************/
    /* constructor */

  /***************/
  public MemberData(HttpServletRequest request) {

//System.out.println("Constructor-MemberData(request)");
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
//System.out.println("in MemberData constructor, szDay="+szDay+" szMonth="+szMonth+" szYear="+szYear);
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
              System.out.println("Exception in Memberdata(request): " + e);
            }
          }
        }

      }
      else {
        memberData[i] = request.getParameter(MemberData.dbData[i][SERVLET_NAME]);
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
//System.out.println("Constructor-MemberData["+i+"] = ("+memberData[i]+")");
    }

  }

  /*************************/
    /* printEmergencyCallRow */

  /*************************/
  public void printEmergencyCallRow(PrintWriter out, String other) {
    out.println("<tr>");
//old        out.println(" <td>"+getFullName()+"</td>");
    out.println(" <td>" + getFullName2() + "</td>");
    if (other != null) {
      out.println(" <td ALIGN=\"center\">" + other + "</td>");
    }
    out.println(" <td>" + getHomePhone() + "</td>");
    out.println(" <td>" + getWorkPhone() + "</td>");
    out.println(" <td>" + getCellPhone() + "</td>");
    out.println(" <td>" + getPager() + "</td>");
    String str = getEmail();
    if (!HARD_SPACE_NBSP.equals(getEmail())) {
      str = "<a href=\"mailto:" + getEmail() + "\">" + getEmail() + "</a>";
    }
    out.println(" <td>" + str + "</td>");
    out.println("</tr>\n");
  }

  /*************/
    /* addColumn */

  /*************/
  public static void addColumn(int index) {
//System.out.println("in addColumn, index="+index);

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
  /*****************************/
    /* printMemberListRowHeading */

  /*****************************/
  public static void printMemberListRowHeading(PrintWriter out, String resort) {
//System.out.println("in printMemberListRowHeading, lastColumn="+lastColumn);
    for (int i = 0; i < lastColumn; ++i) {
      //String title = (String)columns.elementAt(i);
      int idx = columns[i];
//System.out.println("idx="+idx);
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

  /**************************/
    /* printMemberListRowData */

  /**************************/
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
          str = "&nbsp";
          break;
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
          str = getFullName2();
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
          str = getEmail();
          if (!HARD_SPACE_NBSP.equals(getEmail())) {
            str = "<a href=\"mailto:" + getEmail() + "\">" + getEmail() + "</a>";
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
        case CARRY_OVER_CREDITS:
          str = getCarryOverCredits();
          align = " align=center";
          break;
        case LAST_CREDIT_UPDATE:
          str = getLastCreditDateStr();
          break;
        case CAN_EARN_CREDITS:
          str = getCanEarnCredits().equals("1") ? "Yes" : "No";
          align = " align=center";
          break;
        case CREDITS_EARNED:
          str = getCreditsEarned();
          align = " align=center";
          break;
        case CREDITS_USED:
          str = getCreditsUsed();
          align = " align=center";
          break;
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
      }
      out.print(" <td" + align + ">" + str + "</td>");
    }
//System.out.println("=======");
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
    String str = MemberData.HARD_SPACE_NBSP;
    try {
//System.out.println("instructor=("+szTmp+")");
      int idx = Integer.parseInt(szTmp);
//System.out.println("idx="+idx);
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
  /**********************/
    /* printMemberListRow */

  /**********************/
  public void printMemberListRow(PrintWriter out) {
    printEmergencyCallRow(out, null);
  }

  /***********************************************/
    /* readPartialFromRoster (ID, first, last ONLY)*/

  /***********************************************/
  public boolean readPartialFromRoster(ResultSet rosterResults, String defaultString) {
    exceptionError = false;
    idNum = readInt(rosterResults, dbData[ID_NUM][DB_NAME]);
    memberData[ID_NUM] = readString(rosterResults, dbData[ID_NUM][DB_NAME], defaultString);
    memberData[LAST] = readString(rosterResults, dbData[LAST][DB_NAME], defaultString);
    memberData[FIRST] = readString(rosterResults, dbData[FIRST][DB_NAME], defaultString);
    memberData[DIRECTOR] = readString(rosterResults, dbData[DIRECTOR][DB_NAME], defaultString);
    return !exceptionError;
  } //end readFromRoster

  /**********************/
    /* readFullFromRoster */

  /**********************/
  public boolean readFullFromRoster(ResultSet rosterResults, String defaultString) {
    exceptionError = false;
    idNum = readInt(rosterResults, dbData[ID_NUM][DB_NAME]);
    for (int i = 0; i < DB_SIZE; ++i) {
      memberData[i] = readString(rosterResults, dbData[i][DB_NAME], defaultString);
//System.out.println(i+") "+memberData[i]);
    }

    return !exceptionError;
  }

  /***********/
    /* readInt */

  /***********/
  private int readInt(ResultSet rosterResults, String tag) {
    try {
      return rosterResults.getInt(tag);
    }
    catch (Exception e) {
      exceptionError = true;
      return 0;
    } //end try
  }

  /**************/
    /* readString */

  /**************/
  private String readString(ResultSet rosterResults, String tag, String szDefault) {
    try {
      String str = rosterResults.getString(tag);

      if (!testIfValid(str)) {
        return szDefault;
      }
      else {
        return str;
      }
    }
    catch (Exception e) {
      exceptionError = true;
      return null;
    } //end try
  }

  /*************************/
    /* getFullClassification */

  /*************************/
  public String getFullClassification() {
    String classification = null;
    try {
      classification = lookupClassification.get(getClassification());
    }
    catch (Exception ex) {
      System.out.println("ERROR, classification not found" + getClassification());
    }
    return classification;
  }

  public String getExcelHeader() {
    int i;
    String qryString = "";
    for (i = 0; i < DB_SIZE; ++i) {
      qryString += dbData[order[i]][DB_NAME];
      if (i + 1 < DB_SIZE) {
        qryString += "\t";      //tab between fields
      }
    }
//System.out.println(qryString);
    return qryString;
  }

  public String getExcelString() {
    int i;
    String qryString = "";
    String szTmp;
    for (i = 0; i < DB_SIZE; ++i) {
      szTmp = memberData[order[i]].replace(',', ' ');  //remove all commas
      if (i == 0) {
        qryString = szTmp;
      }
      else {
        qryString += "\t" + szTmp;
      }
    }
//System.out.println(qryString);
    return qryString;
  }

  public String getPalmString() {
    int i;
    int palmOrder[] = {LAST, FIRST, CLASSIFICATION, SPOUSE,
        WORKPHONE, HOMEPHONE, PAGER, CELLPHONE, EMAIL, ADDRESS, CITY, STATE, ZIPCODE};
    String qryString = "";
    String szTmp;
    for (i = 0; i < 13; ++i) {

      szTmp = memberData[palmOrder[i]].trim().replace(',', ' ');   //remove all commas
      szTmp = szTmp.replace('"', ' '); //remove all quotes
      if (i > 0) {
        qryString += ",";
      }
      if (palmOrder[i] == CLASSIFICATION) {
        qryString += "\"Classification: " + getFullClassification() + "\"";
      }
      else if (palmOrder[i] == SPOUSE) {
        qryString += "\"Spouse: " + szTmp + "\"";
      }
      else if (szTmp.length() > 0) {
        qryString += "\"" + szTmp + "\"";
      }
    }
    qryString += ",,,,,,,\"0\",\"Ski Patrol\""; //country,1,2,3,4,note,private,catagory
//System.out.println(memberData[0]+" = ("+qryString+")");
    return qryString;
  }

  public String getInsertSQLString(String resort) {
    int i;
    //at time of insert/Modify, set the lastModified date value
//lastUpdated
    String str;
    Calendar rightNow = Calendar.getInstance();
    //str = rightNow.getTimeInMillis() + "";  //convert long to string
//System.out.println("in getInsertSQLString, time=(string)"+str+" or (long)"+rightNow.getTimeInMillis());
    int m = rightNow.get(Calendar.MONTH) + 1;
    int d = rightNow.get(Calendar.DATE);
    int y = rightNow.get(Calendar.YEAR);

    str = y + "-";
    if (m < 10) {
      str += "0";
    }
    str += m + "-";
    if (d < 10) {
      str += "0";
    }
    str += d;
    memberData[LAST_UPDATED] = str;

    String qryString = "INSERT INTO roster (";

    for (i = 0; i < DB_SIZE; ++i) {
      qryString += dbData[order[i]][DB_NAME];
      if (i < DB_SIZE - 1) {
        qryString += ", ";
      }
    }
    qryString += ") VALUES (";

    for (i = 0; i < DB_SIZE; ++i) {
      String goodStr = memberData[order[i]];
      try {
        //make the ' character an excape sequence
        goodStr = goodStr.replaceAll("'", "\\'");
      }
      catch (Exception e) {
        //go back to original
        goodStr = memberData[order[i]];
      }
      if (order[i] == CARRY_OVER_CREDITS || order[i] == CREDITS_USED) {
        goodStr = "0";
      }
      qryString += "'" + goodStr + "'";
      if (i < DB_SIZE - 1) {
        qryString += ", ";
      }
    }
    qryString += " )";
    PatrolData.logger(resort, qryString);
    return qryString;
  }

  public String getUpdateSQLString(String resort) {
    int i;
//at time of insert/Modify, set the lastUpdated date value
    String str;
    Calendar rightNow = Calendar.getInstance();
    //str = rightNow.getTimeInMillis() + "";  //convert long to string
//System.out.println("in getUpdateSQLString, time=(string)"+str+" or (long)"+rightNow.getTimeInMillis());
    int m = rightNow.get(Calendar.MONTH) + 1;
    int d = rightNow.get(Calendar.DATE);
    int y = rightNow.get(Calendar.YEAR);
    str = y + "-";
    if (m < 10) {
      str += "0";
    }
    str += m + "-";
    if (d < 10) {
      str += "0";
    }
    str += d;
    memberData[LAST_UPDATED] = str;
//UPDATE phone_book SET name = 'John Doe', number = '555-1212'

    String qryString = "UPDATE roster SET ";
    for (i = 1; i < DB_SIZE; ++i) { //don't start with the key value (ID_NUM)
      String goodStr = memberData[order[i]];
      try {
        //make the ' character an excape sequence
        goodStr = goodStr.replaceAll("'", "\\'");
//System.out.println("goodStr="+goodStr+" oldString="+memberData[order[i]]);
      }
      catch (Exception e) {
        //go back to original
        goodStr = memberData[order[i]];
//System.out.println("exception="+goodStr);
      }
//            qryString += "'"  + goodStr + "')";
//zzz
      if (order[i] == CARRY_OVER_CREDITS || order[i] == CREDITS_USED) {
        goodStr = "0";
      }
      qryString += dbData[order[i]][DB_NAME] + " = \"" + goodStr + "\" ";
      if (i < DB_SIZE - 1) {
        qryString += ", ";
      }
    }
    qryString += " WHERE " + dbData[ID_NUM][DB_NAME] + " =" + memberData[ID_NUM];
    PatrolData.logger(resort, qryString);
    return qryString;
  }

  public static String vouchersToCredits(String vouchers) {
    String credits;
    try {
//          double xx = Double.parseDouble(vouchers);
//        ct = (int)(xx * 2.0);
      credits = Integer.parseInt(vouchers) + "";
//System.out.println("-------"+ct+" credits="+credits);
    }
    catch (Exception e) {
      System.out.println("Error in vouchersToCredits, parsing voucher count of " + vouchers + ".  Setting Credits to 0");
      credits = "0";
    }
    return credits;
  }

  public static String creditsToVouchers(String szCredits) {
    String szVouchers = "0";
    try {
//            double xx = (double)Integer.parseInt(szCredits) / 2.0;
      double xx = (double) Integer.parseInt(szCredits);
//System.out.println("xx="+xx);
      NumberFormat fmtr = NumberFormat.getInstance();
      // fmtr.applyPattern("#,##0.#;(#,##0.#)");
      szVouchers = fmtr.format(xx);
//System.out.println("yy="+szDefault);
    }
    catch (Exception e) {
      //ignore
    }
    return szVouchers;
  }

  public String getDeleteSQLString() {
    int i;
    //noinspection UnnecessaryLocalVariable
    String qryString = "DELETE FROM roster WHERE " + dbData[ID_NUM][DB_NAME] + " = '" + memberData[ID_NUM] + "'";
//System.out.println(qryString);
    return qryString;
  }

  public boolean isDirector() {
    String dir = getDirector();
    if (testIfValid(dir)) {
      if (dir.substring(0, 1).equalsIgnoreCase("Y")) {
        return true;
      }
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

  public String getFullName2() {
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

  public String getEmail() {
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

  public String getLastCreditDateStr() {
    String szDate = MemberData.HARD_SPACE_NBSP;
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

  boolean okToDisplay(boolean EveryBody, boolean SubList, boolean bListAll,
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
    if (vClassificationsToDisplay.indexOf(getClassification()) == -1) {
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
//System.out.println("commitment status = "+value);
    if (value == 3 || iCommitmentToDisplay == 0 || ((1 << value & iCommitmentToDisplay) == 0)) {
      return false;
    }

//AND 3rd test Everyone/director/cpr/oec/ski/toboggan
//System.out.println(member.getID()+ ") listDirector = "+listDirector+"  isDirector="+member.isDirector());
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
//System.out.println("in OKToDisplay, "+getID()+": totalDays="+totalDays+", MinDays="+MinDays);
      if (totalDays >= MinDays) {
        return false;
      }
    }

    return true;
  }


  public String toString() {
    return memberData[ID_NUM] + "  " + memberData[FIRST] + " " + memberData[LAST];
  }
} //end MemberData class
