package org.nsponline.calendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Vector;


/**
 * @author Steve Gledhill
 */
@SuppressWarnings("ConstantConditions")
public class DayShift extends HttpServlet {

  final static String szDays[] = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
  final static String szMonths[] = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

  //-----
  final boolean debug = false;
  final boolean displayParameters = false;
  //-----

  //ALL the following data MUST be initialized in the constructor
  boolean isDirector;
  int dayOfWeek;
  int date;        //1 based
  int month;      //
  int year;
  String szDate;
  String szYear;
  Vector<Shifts> todaysData;
  private String resort;
  //    int maxPos;
  Hashtable<String, String> NumToName = new Hashtable<String, String>();
  //    Assignments[] assignments;
  Vector<Assignments> assignments;
  boolean missedShift;
  boolean doAssignments;
  PrintWriter out;
  String selectedShift;
  String dropdownShift;
  String dropdownShift2;
  Vector<Shifts> shifts;
  boolean newShift, deleteShift;
  int shiftCount, shiftToDelete;
  boolean saveShiftBtn;
  boolean saveAssignmentBtn;
  boolean deleteAllShiftsBtn;
  String szNameComment;
  String szOriginalName;
  String[] sortedRoster;
  int rosterSize;
  String IDOfEditor;

  //------------
// doGet
//------------
  public void doGet(HttpServletRequest request,
                    HttpServletResponse response)
      throws IOException, ServletException {
    MemberData editorsMemberData = null;
    response.setContentType("text/html");
    synchronized (this) {
      out = response.getWriter();
      if (debug) {
        System.out.println("------- Entering DayShifts --------");
      }
//      CookieID cookie = new CookieID(request, response, "DayShifts", null);
//      IDOfEditor = cookie.getID(); //editor's ID
      SessionData sessionData = new SessionData(request.getSession(), out);
      IDOfEditor = sessionData.getLoggedInUserId();
      doAssignments = request.getParameter("doAssignments") != null;
      resort = request.getParameter("resort");
      PatrolData patrol;
      patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData); //when reading members, read full data
      if (IDOfEditor != null) {
        editorsMemberData = patrol.getMemberByID(IDOfEditor); //ID from cookie
      }

      //noinspection SimplifiableIfStatement
      if (IDOfEditor.equalsIgnoreCase(PatrolData.backDoorUser)) {
        isDirector = true;
      }
      else {
        isDirector = editorsMemberData != null && editorsMemberData.isDirector();
      }

      readParameters(request, patrol);
      processData(request, patrol);

      patrol.close();

      if (saveShiftBtn || saveAssignmentBtn || deleteAllShiftsBtn) {
        response.sendRedirect(PatrolData.SERVLET_URL + "MonthCalendar?month=" + month + "&year=" + year + "&resort=" + resort + "&ID=" + IDOfEditor);
        return;
      }
      printTop();
      if (PatrolData.validResort(resort)) {
        printBody();
      }
      else {
        out.println("Invalid host resort.");
      }
      printBottom();
    } //end Syncronized
  }

  //------------
// doPost
//------------
  public void doPost(HttpServletRequest request,
                     HttpServletResponse response)
      throws IOException, ServletException {
    doGet(request, response);
  }

  //------------
// printTop
//------------
  private void printTop() {
    out.println("<html><head>");
    out.println("<meta http-equiv=\"Content-Language\" content=\"en-us\">");
    out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=windows-1252\">");
    out.println("<title>Display/Edit Ski Patrol Day Shifts</title>");
//all JavaScript code
    out.println("<SCRIPT LANGUAGE=\"JavaScript\">");
    out.println("var mustChangeName = true");
    out.println("var currName = \"" + selectedShift + "\"");
    out.println("function reloadPage(list) {");
    out.println("  var idx = list.selectedIndex");
    out.println("  var msg = list.options[idx].text");
    out.println("  location=\"" + PatrolData.SERVLET_URL + "DayShifts?resort=" + resort + "&dayOfWeek=" + dayOfWeek + "&date=" + date + "&month=" + month + "&year=" + year + "&ID=" + IDOfEditor + "&dropdownShift=\"+msg");
    out.println("}");

//Ignore 'Enter Key' in Text input fields
    out.println("function captureEnter(keycode) {");
//        out.println(" alert(\"keycode=\"+keycode)");
    out.println("return (keycode != 13);");  //return FALSE if Enter key (13).  so a enter key is thrown away
    out.println("}");

//confirm delete of shift with patrollers assigned
    out.println("function confirmShiftDelete(count) {");
//        out.println("var idx = form.NameList.selectedIndex");
    out.println("if(count == 0) return true;");
    out.println(" return confirm(\"This shift has patrollers assigned to it. \\nAre you sure you want to DELETE it?\")");
//        out.println("else");
//        out.println(" where.value=form.NameList.options[idx].text");
    out.println("}");

//Insert/Replace patroller assignment
    out.println("function insertName(form,where) {");
    out.println("var idx = form.NameList.selectedIndex");
    out.println("if(idx == -1)");
    out.println(" alert(\"Select a Name to Insert\")");
    out.println("else");
    out.println(" where.value=form.NameList.options[idx].text");
    out.println("}");

//delete patroller assignment
    out.println("function deleteName(form,where) {");
    out.println("where.value=\"\"");
    out.println("}");

//TEXT AREA JUST LOST FOCUS
    out.println("function testName(str) {");
    out.println("  currName = str");
    out.println("  if(str != \"" + PatrolData.NEW_SHIFT_STYLE + "\" && str != \"\" && str != \" \") ");
    out.println("   mustChangeName = false;");
    out.println("  if(str == \"\" || str == \" \")");
    out.println("   currName=\"Blank\"");
    out.println("}");
//ADD button pressed
    out.println("function CheckEventName() {");
//      out.println("  testName(currName)");
//      out.println("  if(mustChangeName) {");
//      out.println("    alert(\"Must change name.  '\"+currName+\"' is invalid\")");
//      out.println("    return false");
//      out.println("  } else {");
    out.println("    return true");
//      out.println("  }");
    out.println("}");

    out.println("</SCRIPT>");
//end JavaScript code

    out.println("<META HTTP-EQUIV=\"Pragma\" CONTENT=\"no-cache\">");
    out.println("<META HTTP-EQUIV=\"Expires\" CONTENT=\"-1\">");
    out.println("</head>");
    out.println("<body>");

  }

  //------------
// getTodaysAssignmentString
//------------
  private String GetTodaysAssignmentString(int pos) {
    // Format the current time.
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    // java.util.Date currentTime = new java.util.Date(year,month,date);
    Calendar cal = new GregorianCalendar(PatrolData.MDT);
    cal.set(year, month, date);
    //System.out.println(",,year="+year+" cal="+cal);
    String szAssignmentDate = formatter.format(cal.getTime());
    //String szAssignmentDate = "2001-12-05";
    szAssignmentDate = Assignments.createAssignmentName(szAssignmentDate, pos); //assignmentSize is 1 based
    //  2001-12-05_2 example
    return szAssignmentDate;
  }

  //------------
// processData
//------------
  private void processData(HttpServletRequest request, PatrolData patrol) {
    Assignments data;
    Shifts sData;
    int i;
//write out changes
    patrol.resetAssignments();
    int assignmentSize = assignments.size();
    if (saveAssignmentBtn) {
//-----Save Mass Assignments Changes ----------------------------------------
      String param, name;
      int j;
      insureAssignmentExists(patrol);
      assignmentSize = assignments.size(); //may have changed above
      if (debug) {
        System.out.println("===assignmentSize = (" + assignmentSize + ")");
      }
      //get shift=i,pos=j, and name=name
//zzzz
      for (i = 0; i < assignmentSize; ++i) {         //loop for each shift
        name = null;
        data = assignments.elementAt(i); //0 based
        data.setExisted(true);
        for (j = 0; data != null; ++j) {   //loop for each possible assignment within shift
          param = "name" + PatrolData.IndexToString(i) + "_" + PatrolData.IndexToString(j);
          name = request.getParameter(param);
          if (debug) {
            System.out.println("****param=" + param + ", name=(" + name + ")***");
          }
          if (name == null) {
            break;      //this is what really breaks us out of the loop
          }
          MemberData member = null;
          if (name.length() > 2) { //just a space in the name field
            member = patrol.getMemberByName2(name);
            if (displayParameters) {
              System.out.print("param=" + param + ", name=(" + name);
              if (member == null) {
                System.out.println(") no member exists");
              }
              else {
                System.out.println(") id=" + member.getID());
              }
            }
          }

          if (member != null && member.getID() != null) {
            data.insertAt(j, member.getID());
          }
          else {
            data.insertAt(j, "0");  //remove any existing member
          }
        }
        patrol.writeAssignment(data);

        if (name == null && j == 0) {
          break;
        }
      }
    }
    else if (deleteAllShiftsBtn) {
//--------DELETE ALL SHIFTS -------------------------------------
      if (debug) {
        System.out.println("***deleting all shifts.  assignmentSize=" + assignmentSize);
      }
      for (int j = assignmentSize - 1; j >= 0; --j) {    //loop from end of list
        data = assignments.elementAt(j); //0 based
        patrol.deleteAssignment(data);
        assignments.remove(j);                  //remove last item
      }
//          response.sendRedirect(PatrolData.SERVLET_URL+"MonthCalendar?month="+month+"&year="+year+"&resort="+resort+"&ID="+IDOfEditor);
      //noinspection UnnecessaryReturnStatement
      return;
    }
    else if (dropdownShift2 != null && !dropdownShift2.equals("") && !dropdownShift2.equals("--New Shift Style--")) {
//----------the drop down was selected----------
//A SHIFT FROM THE DROP-DOWN WAS SELECTED
//don't do a save, just load the new shift
      if (debug) {
        System.out.println("***************************");
      }
      if (debug) {
        System.out.println("deleteALL button, assignments.size()=" + assignments.size());
      }
      if (debug) {
        System.out.println("***************************");
      }
      assignments = new Vector<Assignments>(); //forget all existing assignments
      szOriginalName = selectedShift;
      for (i = 0; i < 7; ++i) {
        if (selectedShift.equals(szDays[i])) {
          szOriginalName = " ";
          break;
        }
      }
    }
    else if (deleteShift) {
//DELETE
      if (displayParameters) {
        System.out.println("deleteShift:  assignmentSize=" + assignmentSize + " shiftToDelete=" + shiftToDelete);
      }

      insureAssignmentExists(patrol);
      assignmentSize = assignments.size(); //may have changed above
      if (debug) {
        System.out.println("*****assignmentSize=" + assignmentSize);
      }
//if( assignmentSize > 1)
// saveShiftInfo(patrol, assignmentSize,shiftCount-1); //deletes two shifts...
      //noinspection StatementWithEmptyBody
      if (assignmentSize > 0) {
        //tweak assignments to remove shiftToDelete, and modify all following records
        if (shiftToDelete >= assignmentSize) {
          if (debug) {
            System.out.println("ERROR, shiftToDelete" + shiftToDelete + ") was >= assignmentSize(" + assignmentSize + ")");
          }
        }
        else {
          //delete starting record, and modify the rest from assignments
          data = assignments.elementAt(shiftToDelete); //get Assignment to delete
          patrol.deleteAssignment(data);
          assignments.remove(shiftToDelete);
          --assignmentSize;
          for (int j = shiftToDelete; j < assignmentSize; ++j) {
            data = assignments.elementAt(j);
            patrol.decrementAssignment(data);
          }
          if (assignmentSize == 0) {
            //nothing left, add a place holder
            sData = new Shifts(szNameComment, "", "<empty>", 0, Assignments.DAY_TYPE);
            //real assignments existed, so add one more
            //make date field yyy-mm-dd_#
            String szAssignmentDate = GetTodaysAssignmentString(++assignmentSize);
            Assignments assignment = new Assignments(szAssignmentDate, sData);
            assignments.add(assignment);
            assignment.setEventName(szNameComment);
            patrol.writeAssignment(assignment);
          }
        }
      }
      else {
        //to get here, the user clicked on delete on a virtual assignment (not yet real)
      }
    }
    else if (newShift) {
//ADD
      insureAssignmentExists(patrol);
      assignmentSize = assignments.size(); //may have changed above
      saveShiftInfo(patrol, assignmentSize, shiftCount); //????
      if (displayParameters) {
        System.out.println("--add shift--");
      }
      sData = new Shifts(szNameComment, "start time", "end time", 1, Assignments.DAY_TYPE);
      //real assignments existed, so add one more
      //make date field yyy-mm-dd_#
      String szAssignmentDate = GetTodaysAssignmentString(++assignmentSize);
      Assignments assignment = new Assignments(szAssignmentDate, sData);
      assignments.add(assignment);

      String name;
      if (szNameComment != null && szNameComment.length() > 1) {
        name = szNameComment;   //typed in name
      }
      else {
        name = "";  //name from dropdown
      }
//System.out.println("new event name ="+name);
      assignment.setEventName(name);

      patrol.writeAssignment(assignment);
//    } else if(saveShiftBtn && shiftCount == 0 && assignmentSize==0) { //can this happen
    }
    else if (saveShiftBtn && shiftCount == 0) {
//SAVE comment only (make bogus record - 0 the patrollers
//
//I don't think this could can ever get hit, because the last delete creates a shift with 0 records
//
      if (szNameComment.trim().length() == 0) {
        if (displayParameters) {
          System.out.println("NOTHING to do");
        }
      }
      else {

        if (displayParameters) {
          System.out.println("Adding a Comment ONLY assignment (" + szNameComment + ")");
        }
        sData = new Shifts(szNameComment, " ", " ", 0, Assignments.DAY_TYPE);    //shift with 0 patrollers
        String szAssignmentDate = GetTodaysAssignmentString(++assignmentSize);  //2002-12-31_1
        Assignments assignment = new Assignments(szAssignmentDate, sData);
        assignment.setEventName(szNameComment);
        assignments.add(assignment);
        patrol.writeAssignment(assignment);
      }
    }
    else //noinspection StatementWithEmptyBody
      if (saveShiftBtn && shiftCount > 0) {
//SAVE
        //data was modified
        if (displayParameters) {
          System.out.println("---- DayShifts-Data was modified, selectedShift=" + selectedShift + ", shiftCount=" + shiftCount + ", szNameComment=(" + szNameComment + ")");
        }
        saveShiftInfo(patrol, assignmentSize, shiftCount);
        szOriginalName = szNameComment;
      }
      else {
//        System.out.println("Initial state, assignmentSize="+assignmentSize);
      }
  } //end processData

  //------------
// saveShiftInfo
//------------
  private void saveShiftInfo(PatrolData patrol, int assignmentSize, int shiftCount) {
    int i;
    Assignments assignment;
    for (i = 0; i < shiftCount; ++i) {//loop thru shifts on command line
      Shifts sh = todaysData.elementAt(i); //args read in
      String szAssignmentDate;
      if (i < assignmentSize) { //size in database is size of args read in
        assignment = assignments.elementAt(i);
        assignment.setStartTime(sh.getStartString());
        assignment.setEndTime(sh.getEndString());
        assignment.setEventName(szNameComment);
        assignment.setType(sh.getType());
        assignment.setCount(sh.getCount());
      }
      else {
        szAssignmentDate = GetTodaysAssignmentString(i + 1);
        assignment = new Assignments(szAssignmentDate, sh);
        assignment.setEventName(szNameComment);
      }
      patrol.writeAssignment(assignment);
    }
  }

  //------------
// insureAssignmentExists
//------------
  private void insureAssignmentExists(PatrolData patrol) {
    int assignmentSize = assignments.size();
    if (debug) {
      System.out.println("****in insureAssignmentExists, initial assignmentSize = (" + assignmentSize + ")");
    }
    if (assignmentSize > 0) {
      return; //nothing to do.  It ALREADY exists
    }
//..        sData = new Shifts(szNameComment,"start time","end time",1);
//System.out.println("...selectedShift = ("+selectedShift+")");
//System.out.println("...dropdownShift = ("+dropdownShift+")");
//System.out.println("...dropdownShift2 = ("+dropdownShift2+")");
//System.out.println("...shifts.size() = ("+shifts.size()+")");
    assignments = new Vector<Assignments>(); //forget all existing assignments
    //copy shifts to today's database
    String name;
    if (szNameComment != null && szNameComment.length() > 1) {
      name = szNameComment;   //typed in name
    }
    else {
      name = "";  //name from dropdown
    }
//System.out.println("*****name="+name);
    if (selectedShift == null || selectedShift.length() <= 1) {
      selectedShift = szDays[dayOfWeek];
    }
//System.out.println("---selectedShift = ("+selectedShift+")");

    for (int i = 0; i < shifts.size(); ++i) {
      Shifts sData = shifts.elementAt(i);
      if (selectedShift.equals(sData.parsedEventName())) {
        String szAssignmentDate = GetTodaysAssignmentString(++assignmentSize);
        Assignments assignment = new Assignments(szAssignmentDate, sData);
        assignment.setEventName(name);
        assignments.add(assignment);
        patrol.writeAssignment(assignment);
      }
    }
//System.out.println("****in insureAssignmentExists, final assignmentSize = ("+assignmentSize+")");
  }

  //------------
// readParameters
//------------
  private void readParameters(HttpServletRequest request, PatrolData patrol) {
    int i;
//this is a little complicated, but there are 5 states we can enter into
// all entries must have dayOfWeek, date, month, year or ELSE!
// 1) just entered from Calendar - no other flags set (only dayOfWeek,date,month,year)
// 2) select new shift from drop down - selectedShift = String "--New Shift Style--" is nothing selected
// the following are posted from the form and will have
//    szNameComment(can be null) shiftCount and a set of (startTime_?, endTime_?, count_?) for each shift count
// 3) Save button pushed - saveShiftBtn = true
// 4) New button pushed - newShift = true
// 5) Del button pushed - deleteShift = true & MUST FIND shiftToDelete (delete_?)
// 5) Closed button pushed

//-- GLOBAL variables--
// todaysData = assignment/shift data passes in
// shifts     = shift data from the database
// assignments= assignment data for TODAY frad from the database
// NumToName  = quick hash table for converting id numbers to names
// missedShift
// isDirector
// deleteShift via a "delete_#"
// shiftToDelete
// dayOfWeek, (szDate, date), month, (year, szYear)
// String  szNameComment via "newName"
// boolean newShift   via "newShift"
// boolean saveShiftBtn    via "saveShiftBtn"
// int     shiftCount via "shiftCount"
    NumToName = new Hashtable<String, String>();
    missedShift = false;
//    maxPos = 0;
//    assignments = new Assignments[Assignments.MAX];
    assignments = new Vector<Assignments>();
    deleteShift = false;
    shiftToDelete = 0;
    szOriginalName = "";
    //read required parameters
    String szDay = request.getParameter("dayOfWeek");
    dayOfWeek = Integer.parseInt(szDay);            //0 based, 0 = Sunday
    szDate = request.getParameter("date");
    date = Integer.parseInt(szDate);
    String szMonth = request.getParameter("month");
    month = Integer.parseInt(szMonth);  //0 based
    szYear = request.getParameter("year");
//    String foo = request.getParameter("name0_0");
//System.out.println("*********** name0_0="+foo);
    year = Integer.parseInt(szYear);
    if (displayParameters) {
      System.out.println("dayOfWeek=" + dayOfWeek + " szDate=" + szDate + " month=" + month + " szYear=" + szYear + " year=" + year);
    }

    szNameComment = request.getParameter("newName");
//System.out.println("szname=(" + szNameComment + ")");	//hack
    if (szNameComment != null && szNameComment.length() == 0) {
      szNameComment = " ";
    }
    if (displayParameters) {
      System.out.println("szNameComment=(" + szNameComment + ")");
    }

//was the "New" shift button pressed
    String temp = request.getParameter("newShift");
    newShift = (temp != null);
    if (displayParameters) {
      System.out.println("newShift=" + newShift);
    }

//was "Delete All Shifts" button pushed
    deleteAllShiftsBtn = request.getParameter("deleteAllShiftsBtn") != null;
    if (displayParameters) {
      System.out.println("deleteAllShiftsBtn=" + deleteAllShiftsBtn);
    }

//was "Save Shift" button pushed
    saveShiftBtn = request.getParameter("saveShiftBtn") != null;
    if (displayParameters) {
      System.out.println("saveShiftBtn=" + saveShiftBtn);
    }

//get the count of how many shifts existed
    temp = request.getParameter("shiftCount");
    if (temp == null) {
      shiftCount = 0;
    }
    else {
      shiftCount = Integer.parseInt(temp);
    }
    if (displayParameters) {
      System.out.println("shiftCount=" + shiftCount);
    }


//was "Save Assignment" button pushed
    saveAssignmentBtn = request.getParameter("saveAssignmentBtn") != null;
    if (displayParameters) {
      System.out.println("saveAssignmentBtn=" + saveAssignmentBtn);
    }
//was the dropdown selected
//    Shifts[] todaysData = new Shifts[shiftCount];
    selectedShift = request.getParameter("selectedShift");
    dropdownShift = request.getParameter("dropdownShift");
    dropdownShift2 = request.getParameter("dropdownShift2");
//System.out.println("selectedShift = ("+selectedShift+")");
//System.out.println("dropdownShift = ("+dropdownShift+")");
//System.out.println("dropdownShift2 = ("+dropdownShift2+")");
    if (dropdownShift2 != null && dropdownShift2.length() > 1) {
      selectedShift = dropdownShift = dropdownShift2;
    }
    else if (dropdownShift != null && dropdownShift.length() > 1) {
      selectedShift = dropdownShift;
    }
//the dropdown for shifts was changed
    if (selectedShift == null) {
      selectedShift = "";
    }
    if (displayParameters) {
      System.out.println("selectedShift=(" + selectedShift + ")");
    }

    String idNum;
    int y, m, d;

    patrol.resetAssignments();
    patrol.resetRoster();
    MemberData member;
    shifts = new Vector<Shifts>();

//
// read in all patrol members
//
    patrol.resetRoster();
    sortedRoster = new String[PatrolData.MAX_PATROLLERS];
    rosterSize = 0;
    while ((member = patrol.nextMember("")) != null) {
      sortedRoster[rosterSize] = member.getFullName2();
      rosterSize++;
      idNum = member.getID();
      NumToName.put(idNum, member.getFullName2());
    }

//
// read Shift ski assignments for the specific date
//
    patrol.resetAssignments();
    Assignments data;
//    assignmentCount = 0;
    boolean isFirst = true;
    while ((data = patrol.readNextAssignment()) != null) {
//System.out.println("Assignment from disk="+data);
      y = data.getYear();
      m = data.getMonth() - 1; //make it 0 based
      d = data.getDay();
      if (year == y && month == m && date == d) {
        if (displayParameters) {
          System.out.println("actual assignment data=" + data.toString());
        }
        if (isFirst) {
          isFirst = false;
          if (deleteAllShiftsBtn) {
            szOriginalName = " ";
          }
          else {
            szOriginalName = data.getEventName();
          }
          if (displayParameters) {
            System.out.println("blah blah -displayParameters- szOriginalName=(" + szOriginalName + ")");
          }

        }
//            assignments[maxPos++] = data;
        assignments.add(data);
      }
    } //end while Shift ski assignments

    //noinspection PointlessBooleanExpression
    if (displayParameters && assignments.size() == 0) {
      System.out.println("No assignments read in from disk");
    }

//read shift data passed as parameters (shiftCount is count passed in)
//for every shiftCount there should be a startTime_?, endTime_?, count_?
//and there MAY be a delete_?
    todaysData = new Vector<Shifts>();
    for (i = 0; i < shiftCount; ++i) {
      temp = request.getParameter("delete_" + i);
      if (temp != null) {
        deleteShift = true;
        shiftToDelete = i;
        if (displayParameters) {
          System.out.println("delete FOUND at: " + i);
        }
        continue;
      }
      String tStart = request.getParameter("startTime_" + i);
      String tEnd = request.getParameter("endTime_" + i);
      String tCount = request.getParameter("count_" + i);
      if (tCount == null) {
        System.out.println("error, reading past assignment data");
        break;
      }
      int tCnt = Integer.parseInt(tCount);
      String tShift = request.getParameter("shift_" + i);
      if (tShift == null) {
        System.out.println("error, reading past assignment data");
        break;
      }
      int tType = Assignments.getTypeID(tShift);
      if (displayParameters) {
        System.out.println(i + ") " + tStart + ", " + tEnd + ", cnt=" + tCount + ", " + Assignments.szShiftTypes[tType]);
      }
      todaysData.add(new Shifts(szNameComment, tStart, tEnd, tCnt, tType));
    } //end shifts from arguments

    if (displayParameters) {
      System.out.println("deleteShift=" + deleteShift + " shiftToDelete=" + shiftToDelete);
    }

//build list of PREDEFINED SHIFTS for the drop down selection
    patrol.resetShifts();
    Shifts sData;
    while ((sData = patrol.readNextShift()) != null) {
      shifts.add(sData);
    } //end while Shifts
  } //end ReadParameters

  //------------
// showName
//------------
  private void showName(String id, String time, boolean missed) {
    String name = NumToName.get(id);
    if (name == null) {
      name = "&nbsp;";
    }
    out.println("    <tr>");
    out.println("      <td width=\"140\" bgcolor=\"#C0C0C0\">" + time + "</td>");
    out.println("      <td width=\"270\" bgcolor=\"#C0C0C0\">" + name + "</td>");
    out.println("      <td width=\"80\" bgcolor=\"#C0C0C0\">" + (missed ? "Missed" : "&nbsp;") + "</td>");
    out.println("    </tr>");
  }

  //------------
// printBottom
//------------
  private void printBottom() {
    out.println("<p><br></p>");
    out.println("As of: " + new java.util.Date());
    out.println("</body>");
//force page NOT to be cached
    out.println("<HEAD>");
    out.println("<META HTTP-EQUIV=\"Pragma\" CONTENT=\"no-cache\">");
    out.println("<META HTTP-EQUIV=\"Expires\" CONTENT=\"-1\">");
    out.println("</HEAD>");
    out.println("</html>");


  }

  //------------
// getServletInfo
//------------
  public String getServletInfo() {
    return "Change/View Shift Information";
  }

  //------------
// printBody
//------------
  private void printBody() {
//print date at top
    out.println("<h1><font color=\"#FF0000\">" + szDays[dayOfWeek] + " " + szMonths[month] + " " + szDate + ", " + szYear + "</font></h1>");

    if (isDirector) {
      if (selectedShift.equals("")) {
        selectedShift = szDays[dayOfWeek];
      }

      int defaultShiftSize = 0;
      for (Object shift : shifts) {
        Shifts shiftData = (Shifts) shift;
        String parsedName = shiftData.parsedEventName();
        if (parsedName.equals(selectedShift)) {
          defaultShiftSize += shiftData.getCount();
        }
      }
      if (doAssignments) {
        //do group assignment
        showEditAssignments(Math.max(defaultShiftSize, assignments.size()));
      }
      else {
        if ((assignments.size() > 0 || defaultShiftSize > 0) && dropdownShift == null) {
          //ask if director wants to do group assignment
          out.println("<form target='_self' action=\"" + PatrolData.SERVLET_URL + "DayShifts\" method=POST>");
          out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"ID\" VALUE=\"" + IDOfEditor + "\">");
          out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"dayOfWeek\" VALUE=\"" + dayOfWeek + "\">");
          out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"date\" VALUE=\"" + szDate + "\">");
          out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"month\" VALUE=\"" + month + "\">");
          out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"year\" VALUE=\"" + szYear + "\">");
          out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"resort\" VALUE=\"" + resort + "\">");
          out.println("<INPUT TYPE=SUBMIT NAME=doAssignments VALUE=\"Group Assignment of Patrollers to Shifts\">");
          out.println("&nbsp;&nbsp;&nbsp;(Fast if assigning a large number of patrollers at one time)</form>");
        }
        showEditShift();
      }
    }
    else {
      showShiftDetails();
    }
  } //end printBody


  //------------
// printShiftDetails
//------------
  private void showShiftDetails() {
//print table of values
    out.println("<div align=\"center\">");
    out.println("  <center>");
    String goHome = PatrolData.SERVLET_URL + "MonthCalendar?month=" + month + "&year=" + year + "&resort=" + resort + "&ID=" + IDOfEditor;
    out.println("<form target='_self' action=\"" + goHome + "\" method=POST>");
    out.println("  <table border=\"3\" cellpadding=\"0\" cellspacing=\"0\" width=\"438\">");
    out.println("    <tr>");
    out.println("      <td width=\"424\" colspan=\"3\">");
    out.println("        <h3 align=\"center\">Shift Details</h3>");
    out.println("      </td>");
    out.println("    </tr>");
    if (assignments.size() == 0) {
      out.println("    <tr><td width=\"340\" bgcolor=\"#C0C0C0\">No Shift Assignments have been made this date</td></tr>");
    }
    else {
      for (int i = 0; i < assignments.size(); ++i) {
        Assignments data = assignments.elementAt(i);
        int count = data.getCount();
        for (int j = 0; j < count; ++j) {
          String id = data.getPosID(j);
          boolean missed = false;
          if (id.charAt(0) == '-') { // missedShift
            id = id.substring(1);
            missed = true;
          }
          String time = data.getStartingTimeString() + " - " + data.getEndingTimeString();
          showName(id, time, missed);
        }
      }
    }
    out.println("  </table>");
    out.println("  </center>");
    out.println("</div>");
//return button
    out.println("<p align=\"center\"><input type=\"submit\" value=\"Return to Calendar\" name=\"B2\"></p>");
    out.println("</form>");
  } //end ShowShiftDetails

  //------------
// showEditShift
//------------
  private void showEditShift() {
// Change Today's Shift
//System.out.println("starting in showEditShift, selectedShift="+selectedShift);
    out.println("<hr>");
    out.println("<div align=\"center\">");
    out.println("  <table border=\"3\" cellpadding=\"0\" cellspacing=\"0\" width=\"375\" height=\"281\">");
    out.println("    <tr>");
    out.println("      <td width=\"554\" bgcolor=\"#C0C0C0\" height=\"279\">");
    out.println("<form target='_self' action=\"" + PatrolData.SERVLET_URL + "DayShifts\" onSubmit=\"return CheckEventName()\" method=POST>");

    out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"ID\" VALUE=\"" + IDOfEditor + "\">");
    if (dropdownShift != null && !dropdownShift.equals("") && !dropdownShift.equals("--New Shift Style--") && dropdownShift2 != null) {
      out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"dropdownShift2\" VALUE=\"" + dropdownShift + "\">");
    }

    out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"dayOfWeek\" VALUE=\"" + dayOfWeek + "\">");
    out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"date\" VALUE=\"" + szDate + "\">");
    out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"month\" VALUE=\"" + month + "\">");
    out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"year\" VALUE=\"" + szYear + "\">");
    out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"resort\" VALUE=\"" + resort + "\">");
    out.println("          <h2 align=\"center\">Change Today's Shift</h2>");
    out.println("          <div align=\"center\">");
    out.println("            <table border=\"3\" cellpadding=\"0\" cellspacing=\"0\" width=\"700\" height=\"192\">");

    out.println("          <center>");
    out.println("              <tr>");
    out.println("                <td height=\"150\" width=\"650\">");
    out.println("                  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
    out.println("                  Use Predefined Shift format: <select onChange=\"reloadPage(this)\" size=\"1\" name=\"selectedShift\">");
//popdown of predefined shifts
    PatrolData.AddShiftsToDropDown(out, shifts, selectedShift);
    out.println("                  </select>");

    out.println("                  <br>");
    out.println("                  &nbsp;&nbsp;");
    out.println("                  <div align=\"center\">");
    out.println("                    <table border=\"3\" cellpadding=\"0\" cellspacing=\"0\" width=\"636\" style=\"font-size: 12pt\">");
    out.println("                      <tr>");
    String name = szOriginalName;
    if (dropdownShift != null && dropdownShift.equalsIgnoreCase("Closed")) {
      name = dropdownShift;
    }
    else if (deleteShift || newShift) {
      if (szNameComment != null && szNameComment.length() > 1) {
        name = szNameComment;   //typed in name
      }
    }
    out.println("                        <td width=\"483\" colspan=\"5\">Event Name/Comment: <input type=\"text\" name=\"newName\" onKeyDown=\"javascript:return captureEnter(event.keyCode)\" value=\"" + name + "\" size=\"39\"></td>");
    out.println("                      </tr>");
    if (assignments.size() > 0) {
//display assignments in database
      PatrolData.AddAssignmentsToTable(out, assignments);
    }
    else {
//no assignments in database.
// if shift was passed in, use it
// otherwise get default, if one exists (by useing the dayOfWeek)
      if (selectedShift.equals("")) {
        selectedShift = szDays[dayOfWeek];
      }
      PatrolData.AddShiftsToTable(out, shifts, selectedShift);
    }

//New Button (new shift)
    out.println("                      <tr>");
    String newOK = "";
    if (assignments.size() >= Shifts.MAX) {
      newOK = " disabled";
    }
    if (debug) {
      System.out.println("shifts.size() = " + shifts.size());
    }
    if (debug) {
      System.out.println("assignments.size() = " + assignments.size());
    }
    out.println("                        <td width=\"90\"><input type=\"submit\" value=\"New\" " + newOK + " name=\"newShift\"></td>");
    out.println("                        <td width=\"113\">&nbsp;</td>");
    out.println("                        <td width=\"111\">&nbsp;</td>");
    out.println("                        <td width=\"166\">&nbsp;</td>");
    out.println("                        <td width=\"130\">&nbsp;</td>");
    out.println("                      </tr>");

    out.println("                    </table>");
    out.println("                  </div>");
    out.println("<p align=\"center\">");
//save, Cancel, Delete All buttons
    out.println("<input type=\"submit\" value=\"Save Shift Changes\" name=\"saveShiftBtn\">&nbsp;&nbsp;");
    String goHome = PatrolData.SERVLET_URL + "MonthCalendar?month=" + month + "&year=" + year + "&resort=" + resort + "&ID=" + IDOfEditor;
    out.println("<input type=\"button\" value=\"Cancel\" name=\"B6\" onClick=window.location=\"" + goHome + "\">&nbsp;&nbsp;");
    out.println("<input type=\"submit\" value=\"Delete All (restore template)\" name=\"deleteAllShiftsBtn\" >");
    out.println("</p>");

    out.println("                </td>");
    out.println("              </tr>");
    out.println("            </table>");
    out.println("          </div>");
    out.println(" <font size=\"3\">NOTE: pressing <B>Add</B> or <B>Delete</B> does a <B>Save Shift Changes</B> (<B>Cancel</B> won't undo change)</font>");
    out.println("          </form>");
    out.println("          <p>&nbsp;</center></td>");
    out.println("      </tr>");
    out.println("    </table>");
    out.println("</div>");
//System.out.println("endif in showEditShift, selectedShift="+selectedShift);
  }

  private void showEditAssignments(int count) {
    if (assignments.size() == 1) {
      Assignments data = assignments.elementAt(0);
      if (data.getCount() == 0) {
        return;                 //no people to assign
      }
    }

    out.println("<hr>");
    out.println("<table border=\"1\" width=\"100%\">");
    String action = PatrolData.SERVLET_URL + "DayShifts?resort=" + resort + "&dayOfWeek=" + dayOfWeek + "&date=" + date + "&month=" + month + "&year=" + year + "&ID=" + IDOfEditor;
//System.out.println("action="+action);
    out.println("<form target='_self' action=\"" + action + "\" method=POST>");
    out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"saveAssignmentBtn\" VALUE=\"yes\">");
    out.println("  <tr>");
    out.println("    <td width=\"100%\" bgcolor=\"#C0C0C0\">");
    out.println("      <h2 align=\"center\">Change Today's Assignments</h2>");
    out.println("    </td>");
    out.println("  </tr>");
    out.println("  <tr>");
    out.println("    <td width=\"100%\" bgcolor=\"#C0C0C0\">");
    out.println("      <table border=\"1\" width=\"100%\">");
    out.println("        <tr>");
    int rows = 8 + (count * 3) / 2;
    out.println("          <td width=\"20%\"><select size=\"" + rows + "\" name=\"NameList\">");
//build list of patrollers
    AddAllPatrollers();
    out.println("            </select>");
//      out.println("            <p><input type=\"button\" value=\"Display Team\" name=\"B13\">");
    out.println("          </td>");
    out.println("          <td width=\"80%\">");
    out.println("            <table border=\"1\" width=\"100%\" cellspacing=\"1\" bordercolorlight=\"#FFFFFF\">");
//build list of assignments
    if (assignments.size() > 0) {
      AddAllAssignments();
    }
    else {
      AddBlankAssignments();
    }
    out.println("            </table>");
    out.println("          </td>");
    out.println("        </tr>");
    out.println("      </table>");
    out.println("    </td>");
    out.println("  </tr>");
    out.println("  <tr>");
    out.println("    <td width=\"100%\" bgcolor=\"#C0C0C0\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
    out.println("      <input type=\"submit\" value=\"Save Assignment Changes\" name=\"B13\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
    out.println("      <input type=\"submit\" value=\"Cancel, return to Calendar\" onClick=\"history.back()\"></td>");
    out.println("  </tr>");
    out.println("</form>");
    out.println("</table>");
  } // end showEditAssignments

  //-----------------
// AddAllPatrollers
//-----------------
  private void AddAllPatrollers() {
    for (int i = 0; i < rosterSize; ++i) {
      out.println("<option>" + sortedRoster[i] + "</option>");
    }
  } // end AddAllPatrollers

  //-----------------
// AddBlankAssignments
//-----------------
  private void AddBlankAssignments() {
    int i, j;
    String time;
    String fieldName;
//System.out.println("AddBlankAssignments: shift count = "+shifts.size());
    int idx = 0;
    for (i = 0; i < shifts.size(); ++i) {
      Shifts shiftData = shifts.get(i);
      String parsedName = shiftData.parsedEventName();
      if (parsedName.equals(selectedShift)) {
        for (j = 0; j < shiftData.getCount(); ++j) {
          time = shiftData.getStartString() + " - " + shiftData.getEndString();
          fieldName = "name" + PatrolData.IndexToString(idx) + "_" + PatrolData.IndexToString(j);
//System.out.println("fieldName="+fieldName);
          AddNextAssignment(time, "", fieldName);
        }
        ++idx;
      }
    }
  }

  //-----------------
// AddAllAssignments
//-----------------
  private void AddAllAssignments() {
//System.out.println("AddAllAssignments: shift count = "+shifts.size());
//System.out.println("AddAllAssignments: assignments count = "+assignments.size());
    for (int i = 0; i < assignments.size(); ++i) {
      Assignments data = assignments.elementAt(i);
      int count = data.getCount();
      for (int j = 0; j < count; ++j) {
        String id = data.getPosID(j);
        if (id.charAt(0) == '-') { // missedShift
          id = id.substring(1);
        }
        String time = data.getStartingTimeString() + " - " + data.getEndingTimeString();
        String name = NumToName.get(id);
        String fieldName = "name" + PatrolData.IndexToString(i) + "_" + PatrolData.IndexToString(j);
//System.out.println("fieldName="+fieldName+", id="+id);
        AddNextAssignment(time, ((name != null) ? name : ""), fieldName);
      }
    }
  }

  //-----------------
// AddNextAssignment
//-----------------
  private void AddNextAssignment(String szTime, String szName, String szTextNameField) {
    out.println("<tr>");
    out.println(" <td width=\"20%\" bgcolor=\"#C0C0C0\"><input type=\"button\" value=\"Insert --&gt;\" onClick=\"insertName(this.form," + szTextNameField + ")\" name=\"B14\"></td>");
    out.println(" <td width=\"35%\" bgcolor=\"#C0C0C0\">" + szTime + "</td>");
    out.println(" <td width=\"30%\" bgcolor=\"#C0C0C0\"><input readonly type=\"text\" name=\"" + szTextNameField + "\" size=\"18\" value=\"" + szName + "\"></td>");
    out.println(" <td width=\"15%\" bgcolor=\"#C0C0C0\"><input type=\"button\" value=\"Clear\" onClick=\"deleteName(this.form," + szTextNameField + ")\" name=\"B18\"></td>");
    out.println("</tr>");

  }

}