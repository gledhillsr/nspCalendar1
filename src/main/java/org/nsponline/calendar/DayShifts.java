package org.nsponline.calendar;

import org.nsponline.calendar.misc.*;
import org.nsponline.calendar.store.Assignments;
import org.nsponline.calendar.store.Roster;
import org.nsponline.calendar.store.ShiftDefinitions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * note date in form YYYY-MM-DD-p   p (position is 1 based)
 *
 * @author Steve Gledhill
 */
@SuppressWarnings("ConstantConditions")
public class DayShifts extends nspHttpServlet {

  private final static boolean DEBUG = false;


  Class<?> getServletClass() {
    return this.getClass();
  }

  String getParentIfBadCredentials() {
    return null;
  }

  void servletBody( HttpServletRequest request,  HttpServletResponse response) throws IOException {
    new InnerDayShift().runner(request, response);
  }

  private class InnerDayShift {
    //ALL the following data MUST be initialized in the constructor
    private boolean isDirector;
    private int dayOfWeek;
    private int date;        //YYYY-MM-DD_p   where p is 1 based
    private int month;      //
    private int year;
    private String szDate;
    private String szYear;
    private Hashtable<String, String> NumToName = new Hashtable<String, String>();
    private boolean doAssignments;
    private String selectedShift;
    private String dropdownShift;
    private String dropdownShift2;
    private ArrayList<ShiftDefinitions> shiftsTemplates;
    private boolean newShift, deleteShift;
    private int shiftToDelete;
    private boolean saveShiftBtn;
    private boolean saveAssignmentBtn;
    private boolean deleteAllShiftsBtn;
    private String szNameComment;
    private String szOriginalName;
    private String[] sortedRoster;
    private int rosterSize;
    private String IDOfEditor;

    public void runner(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
      if (credentials.hasInvalidCredentials()) {
        return;
      }

      Roster editorsMemberData;
      IDOfEditor = sessionData.getLoggedInUserId();
      doAssignments = request.getParameter("doAssignments") != null;
      PatrolData patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData, LOG); //when reading members, read full data
      editorsMemberData = patrol.getMemberByID(IDOfEditor); //ID from cookie

      //noinspection SimplifiableIfStatement
      if (IDOfEditor.equalsIgnoreCase(sessionData.getBackDoorUser())) {
        isDirector = true;
      } else {
        isDirector = editorsMemberData != null && editorsMemberData.isDirector();
      }
      ArrayList<Assignments> assignmentsFromDisk = new ArrayList<Assignments>();  //INCLUDES scheduled patrollers
      ArrayList<ShiftDefinitions> parameterShifts = new ArrayList<ShiftDefinitions>();  //does not include scheduled patrollers
      readParameters(sessionData, patrol, assignmentsFromDisk, parameterShifts);
      processChangeRequest(request, patrol, assignmentsFromDisk, parameterShifts);


      if (saveShiftBtn || saveAssignmentBtn || deleteAllShiftsBtn) {
        //final change request.  Go back to calendar
        patrol.close();
        response.sendRedirect(PatrolData.SERVLET_URL + "MonthCalendar?month=" + month + "&year=" + year + "&resort=" + resort + "&ID=" + IDOfEditor);
        return;
      }

      OuterPage outerPage = new OuterPage(patrol.getResortInfo(), "", sessionData.getLoggedInUserId());
      outerPage.printResortHeader(out);
      printTop();
      printBody(request, assignmentsFromDisk);
      printBottom();
      patrol.close();
      outerPage.printResortFooter(out);
    }

    private void printTop() {
//all JavaScript code
      out.println("<SCRIPT LANGUAGE='JavaScript'>");
      out.println("var mustChangeName = true");
      out.println("var currName = '" + selectedShift + "'");
      out.println("function reloadPage(list) {");
      out.println("  var idx = list.selectedIndex");
      out.println("  var msg = list.options[idx].text");
      out.println("  location='" + PatrolData.SERVLET_URL + "DayShifts?resort=" + resort + "&dayOfWeek=" + dayOfWeek + "&date=" + date + "&month=" + month + "&year=" + year + "&ID=" + IDOfEditor + "&dropdownShift='+msg");
      out.println("}");

//Ignore 'Enter Key' in Text input fields
      out.println("function captureEnter(keycode) {");
//        out.println(" alert('keycode='+keycode)");
      out.println("return (keycode != 13);");  //return FALSE if Enter key (13).  so a enter key is thrown away
      out.println("}");

//confirm delete of shift with patrollers assigned
      out.println("function confirmShiftDelete(count) {");
//        out.println("var idx = form.NameList.selectedIndex");
      out.println("if(count == 0) return true;");
      out.println(" return confirm('This shift has patrollers assigned to it. \\nAre you sure you want to DELETE it?')");
//        out.println("else");
//        out.println(" where.value=form.NameList.options[idx].text");
      out.println("}");

//Insert/Replace patroller assignment
      out.println("function insertName(form,where) {");
      out.println("var idx = form.NameList.selectedIndex");
      out.println("if(idx == -1)");
      out.println(" alert('Select a Name to Insert')");
      out.println("else");
      out.println(" where.value=form.NameList.options[idx].text");
      out.println("}");

//delete patroller assignment
      out.println("function deleteName(form,where) {");
      out.println("where.value=''");
      out.println("}");

//TEXT AREA JUST LOST FOCUS
      out.println("function testName(str) {");
      out.println("  currName = str");
      out.println("  if(str != '" + PatrolData.NEW_SHIFT_STYLE + "' && str != '' && str != ' ') ");
      out.println("   mustChangeName = false;");
      out.println("  if(str == '' || str == ' ')");
      out.println("   currName='Blank'");
      out.println("}");
//ADD button pressed
      out.println("function CheckEventName() {");
//      out.println("  testName(currName)");
//      out.println("  if(mustChangeName) {");
//      out.println("    alert('Must change name.  ''+currName+'' is invalid')");
//      out.println("    return false");
//      out.println("  } else {");
      out.println("    return true");
//      out.println("  }");
      out.println("}");

      out.println("</SCRIPT>");
//end JavaScript code
    }

    private String GetTodaysAssignmentString(int pos) {
      // Format the current time.
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
      // java.util.Date currentTime = new java.util.Date(year,month,date);
      Calendar cal = new GregorianCalendar(TimeZone.getDefault());
////      debugOut("GetTodaysAssignmentString, date=" + date);
      cal.set(year, month, date);
      //Log.log(",,year="+year+" cal="+cal);
      String szAssignmentDate = formatter.format(cal.getTime());
////      debugOut("GetTodaysAssignmentString, cal.getTime()=" + cal.getTime());
////      debugOut("GetTodaysAssignmentString, szAssignmentDate=" + szAssignmentDate);
      //String szAssignmentDate = "2001-12-05";
      szAssignmentDate = Assignments.createAssignmentName(szAssignmentDate, pos); //assignmentSize is 1 based
      //  2001-12-05_2 example
//      debugOut("GetTodaysAssignmentString, szAssignmentDate=" + szAssignmentDate);
      return szAssignmentDate;
    }

    private void processChangeRequest(HttpServletRequest request, PatrolData patrol, ArrayList<Assignments> assignmentsFromDisk, ArrayList<ShiftDefinitions> parameterShifts) {
//write out changes
      patrol.resetAssignments();
//      int assignmentSize = assignmentsFromDisk.size();
      if (saveAssignmentBtn) {
        doSaveAssignments(request, patrol, assignmentsFromDisk);
      } else if (deleteAllShiftsBtn) {
        doDeleteAllShifts(request, patrol, assignmentsFromDisk);
      } else if (dropdownShift2 != null && !dropdownShift2.equals("") && !dropdownShift2.equals("--New Shift Style--")) {
        doInsertNewShiftFromDropDown(request, assignmentsFromDisk);
      } else if (deleteShift) {
        doDelete1ShiftAssignment(request, patrol, assignmentsFromDisk);
      } else if (newShift) {
        doAdd1NewShift(request, patrol, assignmentsFromDisk, parameterShifts);
      } else if (saveShiftBtn) {  //pressed button at bottom on "Change Today's Shift" page, labeled "Save Shift Changes"
        doSaveShiftAssignments(request, patrol, assignmentsFromDisk, parameterShifts);
      }
      //note, nothing to process on 1st pass
    } //end processChangeRequest

    private void doSaveShiftAssignments(HttpServletRequest request, PatrolData patrol, ArrayList<Assignments> assignmentsFromDisk, ArrayList<ShiftDefinitions> parameterShifts) {
      ShiftDefinitions sData;
      int assignmentSize = assignmentsFromDisk.size();
      if (parameterShifts.size() > 0) {
        //data was modified
        saveShiftInfo(request, patrol, assignmentsFromDisk, parameterShifts);
        szOriginalName = szNameComment;
      } else {
        if (szNameComment.trim().length() == 0) {
          debugOut(request, "saveShiftBtn with no nameComment.  NOTHING to do");
        } else {
          debugOut(request, "Adding a Comment ONLY assignment (" + szNameComment + ")");
          sData = new ShiftDefinitions(szNameComment, " ", " ", 0, Assignments.DAY_TYPE, LOG);    //shift with 0 patrollers
          String szAssignmentDate = GetTodaysAssignmentString(assignmentSize + 1);  //2002-12-31_1  index is 1 based
          Assignments assignment = new Assignments(szAssignmentDate, sData, LOG);
          assignment.setEventName(sessionData, szNameComment);
          debugOut(request, "assignmentsFromDisk.add(" + assignment + ")");
          assignmentsFromDisk.add(assignment);
          patrol.writeAssignment(assignment);
        }
      }
    }

    private void doAdd1NewShift(HttpServletRequest request, PatrolData patrol, ArrayList<Assignments> assignmentsFromDisk, ArrayList<ShiftDefinitions> parameterShifts) {
      insureAssignmentExists(request, patrol, assignmentsFromDisk);
      int assignmentSize = assignmentsFromDisk.size(); //may have changed above
      saveShiftInfo(request, patrol, assignmentsFromDisk, parameterShifts); //????
      debugOut(request, "--add shift--");
      ShiftDefinitions sData = new ShiftDefinitions(szNameComment, "start time", "end time", 1, Assignments.DAY_TYPE, LOG);
      //real assignmentsFromDisk existed, so add one more
      //make date field yyy-mm-dd_#
      String szAssignmentDate = GetTodaysAssignmentString(assignmentSize + 1); //YYYY-MM-DD_p  where p is 1 based
      Assignments assignment = new Assignments(szAssignmentDate, sData, LOG);
      debugOut(request, "adding assignment: " + assignment.toString());
      assignmentsFromDisk.add(assignment);

      String name;
      if (szNameComment != null && szNameComment.length() > 1) {
        name = szNameComment;   //typed in name
      } else {
        name = "";  //name from dropdown
      }
      assignment.setEventName(sessionData, name);
      patrol.writeAssignment(assignment);
    }

    private void doDelete1ShiftAssignment(HttpServletRequest request, PatrolData patrol, ArrayList<Assignments> assignmentsFromDisk) {
      Assignments data;
      ShiftDefinitions sData;//DELETE
      insureAssignmentExists(request, patrol, assignmentsFromDisk);
      int assignmentSize = assignmentsFromDisk.size(); //assignmentsFromDisk may have changed above
      debugOut(request, "--deleteShift:  assignmentSize(current assignmentsFromDisk on disk)=" + assignmentSize + " shiftToDelete=" + shiftToDelete);
      //note: assignmentSize is 0 if editing from template
      debugOut(request, "*****assignmentSize=" + assignmentSize);
//if( assignmentSize > 1)
// saveShiftInfo(patrol, assignmentSize,shiftCount-1); //deletes two shifts...
      //noinspection StatementWithEmptyBody
      if (assignmentSize > 0) {
        //tweak assignmentsFromDisk to remove shiftToDelete, and modify all following records
        if (shiftToDelete >= assignmentSize) {
          debugOut(request, "ERROR, shiftToDelete" + shiftToDelete + ") was >= assignmentSize(" + assignmentSize + ")");
        } else {
          //delete starting record, and modify the rest from assignmentsFromDisk
          data = assignmentsFromDisk.get(shiftToDelete); //get Assignment to delete
          patrol.deleteAssignment(data);
          debugOut(request, "assignmentsFromDisk.remove(" + shiftToDelete + ")");
          assignmentsFromDisk.remove(shiftToDelete);
          patrol.deleteAssignment(data);
          --assignmentSize;
          for (int j = shiftToDelete; j < assignmentSize; ++j) {
            data = assignmentsFromDisk.get(j);
            patrol.decrementAssignment(data);
          }
          if (assignmentSize == 0) {
            //nothing left, add a place holder
            sData = new ShiftDefinitions(szNameComment, "", "<empty>", 0, Assignments.DAY_TYPE, LOG);
            //real assignmentsFromDisk existed, so add one more
            //make date field yyy-mm-dd_#
            String szAssignmentDate = GetTodaysAssignmentString(assignmentSize + 1);   //YYYY-MM-DD_p  where p is 1 based
            Assignments assignment = new Assignments(szAssignmentDate, sData, LOG);
            debugOut(request, "assignmentsFromDisk.add placeholder(" + assignment + ")");
            assignmentsFromDisk.add(assignment);
            assignment.setEventName(sessionData, szNameComment);
            patrol.writeAssignment(assignment);
          }
        }
      } else {
        //to get here, the user clicked on delete on a virtual assignment (not yet real)
      }
    }

    private void doInsertNewShiftFromDropDown(HttpServletRequest request, ArrayList<Assignments> assignmentsFromDisk) {
      int i;//----------the drop down was selected----------
//A SHIFT FROM THE DROP-DOWN WAS SELECTED
//don't do a save, just load the new shift
      debugOut(request, "***************************");
      debugOut(request, "insert new shift with new shift: (" + dropdownShift2 + "), assignmentsFromDisk.size()=" + assignmentsFromDisk.size());
      debugOut(request, "***************************");
      assignmentsFromDisk.clear(); //forget all existing assignmentsFromDisk
      debugOut(request, "assignmentsFromDisk.clear()");
      szOriginalName = selectedShift;
      for (i = 0; i < 7; ++i) {
        if (selectedShift.equals(Utils.szDays[i])) {
          szOriginalName = " ";
          break;
        }
      }
    }

    private void doDeleteAllShifts(HttpServletRequest request, PatrolData patrol, ArrayList<Assignments> assignmentsFromDisk) {
      Assignments data;//--------DELETE ALL SHIFTS -------------------------------------
      int assignmentSize = assignmentsFromDisk.size();
      debugOut(request, "***************************");
      debugOut(request, "*** deleting all shifts.  assignmentSize (from disk)=" + assignmentSize);
      debugOut(request, "***************************");
      for (int assignmentIndex = assignmentSize - 1; assignmentIndex >= 0; --assignmentIndex) {    //loop from end of list
        data = assignmentsFromDisk.get(assignmentIndex); //0 based
        patrol.deleteAssignment(data);
        debugOut(request, "assignmentsFromDisk.remove(" + assignmentIndex + ")");
        assignmentsFromDisk.remove(assignmentIndex);                  //remove last item
      }
//          response.sendRedirect(PatrolData.SERVLET_URL+"MonthCalendar?month="+month+"&year="+year+"&resort="+resort+"&ID="+IDOfEditor);
    }

    private void doSaveAssignments(HttpServletRequest request, PatrolData patrol, ArrayList<Assignments> paramaterAssignments) {
      int i;
      Assignments data;//-----Save Mass Assignments Changes ----------------------------------------
      String param, name;
      int j;
      insureAssignmentExists(request, patrol, paramaterAssignments);
      int assignmentSize = paramaterAssignments.size(); //may have changed above
      debugOut(request, "===assignmentSize = (" + assignmentSize + ")");
      //get shift=i,pos=j, and name=name
//zzzz
      for (i = 0; i < assignmentSize; ++i) {         //loop for each shift
        name = null;
        data = paramaterAssignments.get(i); //0 based
        data.setExisted(true);
        for (j = 0; data != null; ++j) {   //loop for each possible assignment within shift
          param = "name" + PatrolData.IndexToString(i) + "_" + PatrolData.IndexToString(j);
          name = request.getParameter(param);
          debugOut(request, "****param=" + param + ", name=(" + name + ")***");
          if (name == null) {
            break;      //this is what really breaks us out of the loop
          }
          Roster member = null;
          if (name.length() > 2) { //just a space in the name field
            member = patrol.getMemberByName2(name);
            debugOut(request, "param=" + param + ", name=(" + name);
            if (member == null) {
              debugOut(request, ") no member exists");
            } else {
              debugOut(request, ") id=" + member.getID());
            }
          }

          if (member != null && member.getID() != null) {
            data.insertAt(sessionData.getLoggedInResort(), j, member.getID());
          } else {
            data.insertAt(sessionData.getLoggedInResort(), j, "0");  //remove any existing member
          }
        }
        patrol.writeAssignment(data);

        if (name == null && j == 0) {
          break;
        }
      }
    }

    private void saveShiftInfo(HttpServletRequest request, PatrolData patrol,
                               @SuppressWarnings("UnusedParameters") ArrayList<Assignments> assignmentsFromDisk, //includes scheduled patrollers
                               ArrayList<ShiftDefinitions> parameterShifts) {  //does not include scheduled patrollers
      int assignmentSize = assignmentsFromDisk.size();
      debugOut(request, "---- DayShifts.saveShiftInfo(assignmentSize(fromDisk)=" + assignmentSize + ", shiftCount(fromArgs)=" + parameterShifts.size() + ")-Data was modified, selectedShift=" + selectedShift + ", szNameComment=(" + szNameComment + ")");
      int shiftIndex;
      String szAssignmentDate;
      Assignments assignment;
      for (shiftIndex = 0; shiftIndex < parameterShifts.size(); ++shiftIndex) {//loop thru shifts on command line
        ShiftDefinitions sh = parameterShifts.get(shiftIndex); //args read in
        szAssignmentDate = GetTodaysAssignmentString(shiftIndex + 1);  //pos is 1 based
        debugOut(request, "  DayShifts.saveShiftInfo() newDateIndex=" + szAssignmentDate + ", old shift=" + sh.toString());
//        if (shiftIndex < assignmentSize) { //size in database is size of args read in
//          szAssignmentDate = GetTodaysAssignmentString(shiftIndex);
//        }
//        else {
//          szAssignmentDate = GetTodaysAssignmentString(shiftIndex + 1);
//        }
        assignment = new Assignments(szAssignmentDate, sh, LOG);
        assignment.setEventName(sessionData, szNameComment);
//        Assignments old = patrol.readAssignment(assignment.getDate());
//        debugOut("ZZZZZZZ HACK TO REMOVE, old=" + ((old == null) ? "null" : old.toString()));
        Assignments prevAssignment = patrol.readAssignment(assignment.getDate());
        patrol.deleteAssignment(assignment);
        assignment.setExisted(false);
        if (prevAssignment != null) {
          assignment.copyAssignedPatrollers(prevAssignment);
        }
        patrol.writeAssignment(assignment);
      }
    }

    private void insureAssignmentExists(HttpServletRequest request, PatrolData patrol, ArrayList<Assignments> assignmentsFromDisk) {
      int assignmentSize = assignmentsFromDisk.size();
      debugOut(request, "****in insureAssignmentExists, initial assignmentSize = (" + assignmentSize + ")");
      if (assignmentSize > 0) {
        return; //nothing to do.  It ALREADY exists
      }
      //copy shifts to today's database
      String name;
      if (szNameComment != null && szNameComment.length() > 1) {
        name = szNameComment;   //typed in name
      } else {
        name = "";  //name from dropdown
      }
      if (selectedShift == null || selectedShift.length() <= 1) {
        selectedShift = Utils.szDays[dayOfWeek];
      }
//Log.log("---selectedShift = ("+selectedShift+")");

      for (ShiftDefinitions sData : shiftsTemplates) {
        if (selectedShift.equals(sData.parsedEventName())) {
          String szAssignmentDate = GetTodaysAssignmentString(++assignmentSize);   //1 based, so increment first
          Assignments assignment = new Assignments(szAssignmentDate, sData, LOG);
          assignment.setEventName(sessionData, name);
          debugOut(request, "assignmentsFromDisk.add(" + assignment + ")");
          assignmentsFromDisk.add(assignment);
          patrol.writeAssignment(assignment);
        }
      }
//Log.log("****in insureAssignmentExists, final assignmentSize = ("+assignmentSize+")");
    }

    private void readParameters(SessionData sessionData, PatrolData patrol,
                                ArrayList<Assignments> assignmentsFromDisk,
                                ArrayList<ShiftDefinitions> parameterShifts) {
      int i;
      HttpServletRequest request = sessionData.getRequest();
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
// parameterShifts = assignment/shift data passes in
// shifts     = shift data from the database
// assignmentsFromDisk= assignment data for TODAY from from the database
// numToName  = quick hash table for converting id numbers to names
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
//    maxPos = 0;
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
//Log.log("*********** name0_0="+foo);
      year = Integer.parseInt(szYear);
      debugOut(request, "dayOfWeek=" + dayOfWeek + " date=" + date + " month=" + month + " szYear=" + szYear + " year=" + year);

      szNameComment = request.getParameter("newName");
//Log.log("szname=(" + szNameComment + ")");	//hack
      if (szNameComment != null && szNameComment.length() == 0) {
        szNameComment = " ";
      }

//was the "New" shift button pressed
      String temp = request.getParameter("newShift");
      newShift = (temp != null);

//was "Delete All Shifts" button pushed
      deleteAllShiftsBtn = request.getParameter("deleteAllShiftsBtn") != null;

//was "Save Shift" button pushed
      saveShiftBtn = request.getParameter("saveShiftBtn") != null;

//get the count of how many shifts existed
      temp = request.getParameter("shiftCount");
      int shiftCount = (temp == null) ? 0 : Integer.parseInt(temp);

//was "Save Assignment" button pushed
      saveAssignmentBtn = request.getParameter("saveAssignmentBtn") != null;
      debugOut(request, "saveShiftBtn=" + saveShiftBtn + ", newShift=" + newShift + ", deleteAllShiftsBtn=" + deleteAllShiftsBtn + ", saveAssignmentBtn=" + saveAssignmentBtn);
//was the dropdown selected
//    Shifts[] parameterShifts = new Shifts[shiftCount];
      selectedShift = request.getParameter("selectedShift");
      dropdownShift = request.getParameter("dropdownShift");
      dropdownShift2 = request.getParameter("dropdownShift2");
      debugOut(request, "selectedShift = (" + selectedShift + "), dropdownShift = (" + dropdownShift
        + "), dropdownShift2 = (" + dropdownShift2 + ")");
      if (dropdownShift2 != null && dropdownShift2.length() > 1) {
        selectedShift = dropdownShift = dropdownShift2;
      } else if (dropdownShift != null && dropdownShift.length() > 1) {
        selectedShift = dropdownShift;
      }
//the dropdown for shifts was changed
      if (selectedShift == null) {
        selectedShift = "";
      }
      debugOut(request, "selectedShift=(" + selectedShift + "), szNameComment=(" + szNameComment + "), parameter shiftCount=" + shiftCount);

      String idNum;
      int y, m, d;

      patrol.resetAssignments();
      ResultSet rosterResults = patrol.resetRoster();
      Roster member;
      shiftsTemplates = new ArrayList<ShiftDefinitions>();

//
// read in all patrol members
//
      sortedRoster = new String[PatrolData.MAX_PATROLLERS];
      rosterSize = 0;
      while ((member = patrol.nextMember("", rosterResults)) != null) {
        sortedRoster[rosterSize] = member.getFullName_lastNameFirst();
        rosterSize++;
        idNum = member.getID();
        NumToName.put(idNum, member.getFullName_lastNameFirst());
      }

//
// read Shift ski assignmentsFromDisk for the specific date
//
      ArrayList<Assignments> monthOfAssignments = patrol.readSortedAssignments(year, month + 1);
      boolean isFirst = true;
      for (Assignments data : monthOfAssignments) {
        y = data.getYear();
        m = data.getMonth() - 1; //make it 0 based
        d = data.getDay();
        if (year == y && month == m && date == d) {
          if (isFirst) {
            isFirst = false;
            if (deleteAllShiftsBtn) {
              szOriginalName = " ";
            } else {
              szOriginalName = data.getEventName();
            }
            debugOut(request, "szOriginalName=(" + szOriginalName + ")");
          }
//            assignmentsFromDisk[maxPos++] = data;
          debugOut(request, "assignmentsFromDisk.add: " + data.toString());
          assignmentsFromDisk.add(data);
        }
      } //end while Shift ski assignmentsFromDisk

      if (assignmentsFromDisk.size() == 0) {
        debugOut(request, "No assignmentsFromDisk read in from disk");
      }

//read shift data passed as parameters (shiftCount is count passed in)
//for every shiftCount there should be a startTime_?, endTime_?, count_?
//and there MAY be a delete_?
      for (i = 0; i < shiftCount; ++i) {
        temp = request.getParameter("delete_" + i);
        if (temp != null) {
          deleteShift = true;
          shiftToDelete = i;
          debugOut(request, "user pressed DELETE at index: " + i);
          continue;
        }
        String tStart = request.getParameter("startTime_" + i);
        String tEnd = request.getParameter("endTime_" + i);
        String tCount = request.getParameter("count_" + i);
        if (tCount == null) {
          Logger.printToLogFileStatic(request, resort, "ERROR, reading past assignment data");
          break;
        }
        int tCnt = Integer.parseInt(tCount);
        String tShift = request.getParameter("shift_" + i);
        if (tShift == null) {
          Logger.printToLogFileStatic(request, resort, "ERROR, reading past assignment data");
          break;
        }
        int tType = Assignments.getTypeID(sessionData, tShift);

        debugOut(request, "shift(" + i + ") '" + tStart + "', '" + tEnd + "', cnt=" + tCount + ", " + Assignments.getShiftName(tType));
        parameterShifts.add(new ShiftDefinitions(szNameComment, tStart, tEnd, tCnt, tType, LOG));
      } //end shifts from arguments

      debugOut(request, "deleteShift=" + deleteShift + ", shiftToDelete=" + shiftToDelete);

//build list of PREDEFINED SHIFTS for the drop down selection
//      patrol.resetShiftDefinitions();   //'shiftdefinitions'
//      Shifts sData;
      //end while Shifts
      shiftsTemplates.addAll(patrol.readShiftDefinitions());
    } //end ReadParameters

    private void debugOut(HttpServletRequest request, String msg) {
      if (DEBUG) {
        Logger.printToLogFileStatic(request, resort, "Debug-DayShifts: " + msg);
      }
    }

    private void showName(String id, String time, boolean missed) {
      String name = NumToName.get(id);
      if (name == null) {
        name = "&nbsp;";
      }
      out.println("    <tr>");
      out.println("      <td width='140' bgcolor='#C0C0C0'>" + time + "</td>");
      out.println("      <td width='270' bgcolor='#C0C0C0'>" + name + "</td>");
      out.println("      <td width='80' bgcolor='#C0C0C0'>" + (missed ? "Missed" : "&nbsp;") + "</td>");
      out.println("    </tr>");
    }

    private void printBottom() {
      out.println("<p><br></p>");
      out.println("As of: " + new java.util.Date());
      out.println("</body>");
    }

    private void printBody(HttpServletRequest request, ArrayList<Assignments> assignmentsFromDisk) {
//print date at top
      out.println("<h1><font color='#FF0000'>" + Utils.szDays[dayOfWeek] + " " + Utils.szMonthsFull[month] + " " + szDate + ", " + szYear + "</font></h1>");

      if (isDirector) {
        if (selectedShift.equals("")) {
          selectedShift = Utils.szDays[dayOfWeek];
        }

        int defaultShiftSize = 0;
        for (Object shift : shiftsTemplates) {
          ShiftDefinitions shiftData = (ShiftDefinitions) shift;
          String parsedName = shiftData.parsedEventName();
          if (parsedName.equals(selectedShift)) {
            defaultShiftSize += shiftData.getCount();
          }
        }
        if (doAssignments) {
          //do group assignment
          showEditAssignments(Math.max(defaultShiftSize, assignmentsFromDisk.size()), assignmentsFromDisk);
        } else {
          if ((assignmentsFromDisk.size() > 0 || defaultShiftSize > 0) && dropdownShift == null) {
            //ask if director wants to do group assignment
            out.println("<form target='_self' action='" + PatrolData.SERVLET_URL + "DayShifts' method=POST>");
            out.println("<INPUT TYPE='HIDDEN' NAME='ID' VALUE='" + IDOfEditor + "'>");
            out.println("<INPUT TYPE='HIDDEN' NAME='dayOfWeek' VALUE='" + dayOfWeek + "'>");
            out.println("<INPUT TYPE='HIDDEN' NAME='date' VALUE='" + szDate + "'>");
            out.println("<INPUT TYPE='HIDDEN' NAME='month' VALUE='" + month + "'>");
            out.println("<INPUT TYPE='HIDDEN' NAME='year' VALUE='" + szYear + "'>");
            out.println("<INPUT TYPE='HIDDEN' NAME='resort' VALUE='" + resort + "'>");
            out.println("<INPUT TYPE=SUBMIT NAME=doAssignments VALUE='Group Assignment of Patrollers to Shifts'>");
            out.println("&nbsp;&nbsp;&nbsp;(Fast if assigning a large number of patrollers at one time)</form>");
          }
          showEditShift(request, assignmentsFromDisk);
        }
      } else {
        showShiftDetails(assignmentsFromDisk);
      }
    } //end printBody

    private void showShiftDetails(ArrayList<Assignments> assignmentsFromDisk) {
//print table of values
      out.println("<div align='center'>");
      out.println("  <center>");
      String goHome = PatrolData.SERVLET_URL + "MonthCalendar?month=" + month + "&year=" + year + "&resort=" + resort + "&ID=" + IDOfEditor;
      out.println("<form target='_self' action='" + goHome + "' method=POST>");
      out.println("  <table border='3' cellpadding='0' cellspacing='0' width='438'>");
      out.println("    <tr>");
      out.println("      <td width='424' colspan='3'>");
      out.println("        <h3 align='center'>Shift Details</h3>");
      out.println("      </td>");
      out.println("    </tr>");
      if (assignmentsFromDisk.size() == 0) {
        out.println("    <tr><td width='340' bgcolor='#C0C0C0'>No Shift Assignments have been made this date</td></tr>");
      } else {
        for (Assignments data : assignmentsFromDisk) {
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
      out.println("<p align='center'><input type='submit' value='Return to Calendar' name='B2'></p>");
      out.println("</form>");
    } //end ShowShiftDetails

    private void showEditShift(HttpServletRequest request, ArrayList<Assignments> assignmentsFromDisk) {
// Change Today's Shift
//Log.log("starting in showEditShift, selectedShift="+selectedShift);
      out.println("<hr>");
      out.println("<div align='center'>");
      out.println("  <table border='3' cellpadding='0' cellspacing='0' width='375' height='281'>");
      out.println("    <tr>");
      out.println("      <td width='554' bgcolor='#C0C0C0' height='279'>");
      out.println("<form target='_self' action='" + PatrolData.SERVLET_URL + "DayShifts' onSubmit='return CheckEventName()' method=POST>");

      out.println("<INPUT TYPE='HIDDEN' NAME='ID' VALUE='" + IDOfEditor + "'>");
      if (dropdownShift != null && !dropdownShift.equals("") && !dropdownShift.equals("--New Shift Style--") && dropdownShift2 != null) {
        out.println("<INPUT TYPE='HIDDEN' NAME='dropdownShift2' VALUE='" + dropdownShift + "'>");
      }

      out.println("<INPUT TYPE='HIDDEN' NAME='dayOfWeek' VALUE='" + dayOfWeek + "'>");
      out.println("<INPUT TYPE='HIDDEN' NAME='date' VALUE='" + szDate + "'>");
      out.println("<INPUT TYPE='HIDDEN' NAME='month' VALUE='" + month + "'>");
      out.println("<INPUT TYPE='HIDDEN' NAME='year' VALUE='" + szYear + "'>");
      out.println("<INPUT TYPE='HIDDEN' NAME='resort' VALUE='" + resort + "'>");
      out.println("          <h2 align='center'>Change Today's Shift</h2><h4>Please report any bugs to steve@gledhills.com</h4>");
      out.println("          <div align='center'>");
      out.println("            <table border='3' cellpadding='0' cellspacing='0' width='700' height='192'>");

      out.println("          <center>");
      out.println("              <tr>");
      out.println("                <td height='150' width='650'>");
      out.println("                  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
      out.println("                  Use Predefined Shift format: <select onChange='reloadPage(this)' size='1' name='selectedShift'>");
//popdown of predefined shifts
      PatrolData.AddShiftsToDropDown(out, shiftsTemplates, selectedShift);
      out.println("                  </select>");

      out.println("                  <br>");
      out.println("                  &nbsp;&nbsp;");
      out.println("                  <div align='center'>");
      out.println("                    <table border='3' cellpadding='0' cellspacing='0' width='636' style='font-size: 12pt'>");
      out.println("                      <tr>");
      String name = szOriginalName;
      if (dropdownShift != null && dropdownShift.equalsIgnoreCase("Closed")) {
        name = dropdownShift;
      } else if (deleteShift || newShift) {
        if (szNameComment != null && szNameComment.length() > 1) {
          name = szNameComment;   //typed in name
        }
      }
      out.println("                        <td width='483' colspan='5'>Event Name/Comment: <input type='text' name='newName' onKeyDown='javascript:return captureEnter(event.keyCode)' value='" + name + "' size='39'></td>");
      out.println("                      </tr>");
      if (assignmentsFromDisk.size() > 0) {
//display assignmentsFromDisk in database
        PatrolData.AddAssignmentsToTable(out, assignmentsFromDisk);
      } else {
//no assignmentsFromDisk in database.
// if shift was passed in, use it
// otherwise get default, if one exists (by useing the dayOfWeek)
        if (selectedShift.equals("")) {
          selectedShift = Utils.szDays[dayOfWeek];
        }
        PatrolData.AddShiftsToTable(out, shiftsTemplates, selectedShift);
      }

//New Button (new shift)
      out.println("                      <tr>");
      String newOK = "";
      if (assignmentsFromDisk.size() >= ShiftDefinitions.MAX) {
        newOK = " disabled";
      }
      debugOut(request, "shifts.size() = " + shiftsTemplates.size());
      debugOut(request, "assignmentsFromDisk.size() = " + assignmentsFromDisk.size());
      out.println("                        <td width='90'><input type='submit' value='New' " + newOK + " name='newShift'></td>");
      out.println("                        <td width='113'>&nbsp;</td>");
      out.println("                        <td width='111'>&nbsp;</td>");
      out.println("                        <td width='166'>&nbsp;</td>");
      out.println("                        <td width='130'>&nbsp;</td>");
      out.println("                      </tr>");

      out.println("                    </table>");
      out.println("                  </div>");
      out.println("<p align='center'>");
//save, Cancel, Delete All buttons
      out.println("<input type='submit' value='Save Shift Changes' name='saveShiftBtn'>&nbsp;&nbsp;");
      String goHome = PatrolData.SERVLET_URL + "MonthCalendar?month=" + month + "&year=" + year + "&resort=" + resort + "&ID=" + IDOfEditor;
      out.println("<input type='button' value='Cancel' name='B6' onClick=window.location='" + goHome + "'>&nbsp;&nbsp;");
      out.println("<input type='submit' value='Delete All (restore template)' name='deleteAllShiftsBtn' >");
      out.println("</p>");

      out.println("                </td>");
      out.println("              </tr>");
      out.println("            </table>");
      out.println("          </div>");
      out.println(" <font size='3'>NOTE: pressing <B>Add</B> or <B>Delete</B> does a <B>Save Shift Changes</B> (<B>Cancel</B> won't undo change)</font>");
      out.println("          </form>");
      out.println("          <p>&nbsp;</center></td>");
      out.println("      </tr>");
      out.println("    </table>");
      out.println("</div>");
//Log.log("endif in showEditShift, selectedShift="+selectedShift);
    }

    private void showEditAssignments(int count, ArrayList<Assignments> assignmentsFromDisk) {
      if (assignmentsFromDisk.size() == 1) {
        Assignments data = assignmentsFromDisk.get(0);
        if (data.getCount() == 0) {
          return;                 //no people to assign
        }
      }

      out.println("<hr>");
      out.println("<table border='1' width='100%'>");
      String action = PatrolData.SERVLET_URL + "DayShifts?resort=" + resort + "&dayOfWeek=" + dayOfWeek + "&date=" + date + "&month=" + month + "&year=" + year + "&ID=" + IDOfEditor;
//Log.log("action="+action);
      out.println("<form target='_self' action='" + action + "' method=POST>");
      out.println("<INPUT TYPE='HIDDEN' NAME='saveAssignmentBtn' VALUE='yes'>");
      out.println("  <tr>");
      out.println("    <td width='100%' bgcolor='#C0C0C0'>");
      out.println("      <h2 align='center'>Change Today's Assignments (is mostly BROKEN! check back in a few days, or email steve@gledhills.com)</h2>");
      out.println("    </td>");
      out.println("  </tr>");
      out.println("  <tr>");
      out.println("    <td width='100%' bgcolor='#C0C0C0'>");
      out.println("      <table border='1' width='100%'>");
      out.println("        <tr>");
      int rows = 8 + (count * 3) / 2;
      out.println("          <td width='20%'><select size='" + rows + "' name='NameList'>");
//build list of patrollers
      AddAllPatrollers();
      out.println("            </select>");
//      out.println("            <p><input type='button' value='Display Team' name='B13'>");
      out.println("          </td>");
      out.println("          <td width='80%'>");
      out.println("            <table border='1' width='100%' cellspacing='1' bordercolorlight='#FFFFFF'>");
//build list of assignmentsFromDisk
      if (assignmentsFromDisk.size() > 0) {
        AddAllAssignments(assignmentsFromDisk);
      } else {
        AddBlankAssignments();
      }
      out.println("            </table>");
      out.println("          </td>");
      out.println("        </tr>");
      out.println("      </table>");
      out.println("    </td>");
      out.println("  </tr>");
      out.println("  <tr>");
      out.println("    <td width='100%' bgcolor='#C0C0C0'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
      out.println("      <input type='submit' value='Save Assignment Changes' name='B13'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
      out.println("      <input type='submit' value='Cancel, return to Calendar' onClick='history.back()'></td>");
      out.println("  </tr>");
      out.println("</form>");
      out.println("</table>");
    } // end showEditAssignments

    private void AddAllPatrollers() {
      for (int i = 0; i < rosterSize; ++i) {
        out.println("<option>" + sortedRoster[i] + "</option>");
      }
    } // end AddAllPatrollers

    private void AddBlankAssignments() {
      int j;
      String time;
      String fieldName;
//Log.log("AddBlankAssignments: shift count = "+shifts.size());
      int idx = 0;
      for (ShiftDefinitions shiftData : shiftsTemplates) {
        String parsedName = shiftData.parsedEventName();
        if (parsedName.equals(selectedShift)) {
          for (j = 0; j < shiftData.getCount(); ++j) {
            time = shiftData.getStartString() + " - " + shiftData.getEndString();
            fieldName = "name" + PatrolData.IndexToString(idx) + "_" + PatrolData.IndexToString(j);
//Log.log("fieldName="+fieldName);
            AddNextAssignment(time, "", fieldName);
          }
          ++idx;
        }
      }
    }

    private void AddAllAssignments(ArrayList<Assignments> assignmentsFromDisk) {
//Log.log("AddAllAssignments: shift count = "+shifts.size());
//Log.log("AddAllAssignments: assignments count = "+assignments.size());
      for (int i = 0; i < assignmentsFromDisk.size(); ++i) {
        Assignments assignmentRow = assignmentsFromDisk.get(i);
        int count = assignmentRow.getCount();
        for (int j = 0; j < count; ++j) {
          String id = assignmentRow.getPosID(j);
          if (id.charAt(0) == '-') { // missedShift
            id = id.substring(1);
          }
          String time = assignmentRow.getStartingTimeString() + " - " + assignmentRow.getEndingTimeString();
          String name = NumToName.get(id);
          String fieldName = "name" + PatrolData.IndexToString(i) + "_" + PatrolData.IndexToString(j);
//Log.log("fieldName="+fieldName+", id="+id);
          AddNextAssignment(time, ((name != null) ? name : ""), fieldName);
        }
      }
    }

    private void AddNextAssignment(String szTime, String szName, String szTextNameField) {
      out.println("<tr>");
      out.println(" <td width='20%' bgcolor='#C0C0C0'><input type='button' value='Insert --&gt;' onClick='insertName(this.form," + szTextNameField + ")' name='B14'></td>");
      out.println(" <td width='35%' bgcolor='#C0C0C0'>" + szTime + "</td>");
      out.println(" <td width='30%' bgcolor='#C0C0C0'><input readonly type='text' name='" + szTextNameField + "' size='18' value='" + szName + "'></td>");
      out.println(" <td width='15%' bgcolor='#C0C0C0'><input type='button' value='Clear' onClick='deleteName(this.form," + szTextNameField + ")' name='B18'></td>");
      out.println("</tr>");

    }
  }
}