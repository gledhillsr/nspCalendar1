package org.nsponline.calendar.resources;

import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.nsponline.calendar.store.Assignments;
import org.nsponline.calendar.store.ShiftDefinitions;
import org.nsponline.calendar.utils.Logger;
import org.nsponline.calendar.utils.PatrolData;
import org.nsponline.calendar.utils.SessionData;

@SuppressWarnings("CommentedOutCode")
public class InnerEditShiftTemplates extends ResourceBase {
  final private HttpServletResponse response; //todo move into ResourceBase
  String szMyID;
  ArrayList<ShiftDefinitions> shifts;
  String selectedShift;
  boolean addShift, deleteShift;
  int shiftCount, shiftToDelete;
  String eventName;
  boolean newShift;
  boolean SaveChangesBtn;
  boolean DeleteTemplateBtn;

  public InnerEditShiftTemplates(HttpServletRequest request, HttpServletResponse response, Logger LOG) throws IOException {
    super(request, response, LOG);
    this.response = response;
  }

  public void runner(String parentClassName) throws IOException {
    if (!initBaseAndAskForValidCredentials(response, parentClassName)) {
      return;
    }

    szMyID = sessionData.getLoggedInUserId();
    SaveChangesBtn = (request.getParameter("SaveChangesBtn") != null);
    DeleteTemplateBtn = (request.getParameter("DeleteTemplateBtn") != null);
    readParameters(request, sessionData);

    if (SaveChangesBtn) {
      response.sendRedirect(PatrolData.SERVLET_URL + "Directors?resort=" + resort + "&ID=" + szMyID);
      return;
    }
    else if (DeleteTemplateBtn) {
      selectedShift = ""; //shift no longer exists
    }

//    PatrolData patrol = new PatrolData(PatrolData.FETCH_ALL_DATA, resort, sessionData, LOG); //when reading members, read full data
    printCommonHeader();
    printTop();
    if (DeleteTemplateBtn) {
      out.println("<h4>Template Was Deleted</h4>");
    }
    printBody();
    printCommonFooter();
  }

  public void printTop() {
    //all JavaScript code
    out.println("<SCRIPT LANGUAGE=\"JavaScript\">");
    out.println("var mustChangeName = true");
    out.println("var currName = \"" + selectedShift + "\"");

    //Ignore Enter Key in Text input
    out.println("function captureEnter(keycode) {");
    //        out.println(" alert(\"keycode=\"+keycode)");
    out.println("return (keycode != 13);");  //return FALSE if Enter key (13).  so a enter key is thrown away
    out.println("}");

    //confirmDelete
    out.println("function confirmDelete() {");
    out.println("   return confirm(\"Are you sure you want to delete the shift template named '\"+currName+\"'.\")");
    out.println("}");

    //reloadPage
    out.println("function reloadPage(list) {");
    out.println("  var idx = list.selectedIndex");
    out.println("  var msg = list.options[idx].text");
    out.println("  location=\"" + PatrolData.SERVLET_URL + "EditShiftTemplates?resort=" + resort + "&ID=" + szMyID + "&selectedShift=\"+msg");
    out.println("}");

    //TEXT AREA JUST LOST FOCUS
    out.println("function testName(str) {");
    out.println("  currName = str");
    out.println("  if(str != \"" + PatrolData.newShiftStyle + "\" && str != \"\" && str != \" \") ");
    out.println("   mustChangeName = false;");
    out.println("  if(str == \"\" || str == \" \")");
    out.println("   currName=\"Blank\"");
    out.println("}");
    //ADD button pressed
    out.println("function CheckEventName() {");
    out.println("  testName(currName)");
    out.println("  if(mustChangeName) {");
    out.println("    alert(\"This 'shift' MUST have a valid name BEFORE anything else can be done.  '\"+currName+\"' is invalid\")");
    out.println("    return false");
    out.println("  } else {");
    out.println("    return true");
    out.println("  }");
    out.println("}");

    //cancel button pressed
    out.println("function goHome() {");
    out.println("location.href = \"" + PatrolData.SERVLET_URL + "Directors?resort=" + resort + "&ID=" + szMyID + "\"");
    out.println("}");
    ////DELETE button pressed
    //      out.println("function DeleteBtn() {");
    //      out.println("  if(mustChangeName)");
    //      out.println("   alert(\"Must change name.  '\"+currName+\"' is invalid\")");
    //      out.println("  else");
    //      out.println("   document.forms[0].submit()");
    //      out.println("}");
    ////SAVE button pressed
    //      out.println("function SaveBtn() {");
    //      out.println("  if(mustChangeName)");
    //      out.println("   alert(\"Must change name.  '\"+currName+\"' is invalid\")");
    //      out.println("  else");
    //      out.println("   document.forms[0].submit()");
    //      out.println("}");

    out.println("</SCRIPT>");

    //end JavaScript code
  }


  @SuppressWarnings("ConstantConditions")
  private void readParameters(HttpServletRequest request, SessionData sessionData) {
    //-----
    final boolean displayParameters = true;
    //-----
    //event name text field
    eventName = request.getParameter("eventName");
    //shift selected for dorp down list
    selectedShift = request.getParameter("selectedShift");
    if (selectedShift == null) {
      selectedShift = "";
    }
    //"add" shift button pushed
    String temp = request.getParameter("addShift");
    addShift = (temp != null);
    temp = request.getParameter("shiftCount");
    if (temp == null) {
      shiftCount = 0;
    }
    else {
      shiftCount = Integer.parseInt(temp);
    }
    if (displayParameters) {
      LOG.info("----eventName=(" + eventName + ")  selectedShift=(" + selectedShift + ")-------");
    }
    //did the shift name change
    newShift = (eventName != null && selectedShift != null && !eventName.equals(selectedShift));
    if (newShift) {
      selectedShift = eventName;
    }
    if (displayParameters) {
      LOG.info("newShift=" + newShift + ", addShift=" + addShift + ",  shiftCount=" + shiftCount);
    }

    deleteShift = false;
    shiftToDelete = 0;
    ShiftDefinitions[] todaysData = new ShiftDefinitions[shiftCount];
    int cnt = 0;
    PatrolData patrol = new PatrolData(resort, sessionData, LOG); //when reading members, read full data
    // read Shift ski assignments for the specific day
    shifts = new ArrayList<ShiftDefinitions>();
    patrol.resetShiftDefinitions();  //todo 12/30/15  this does nothing or is broken
    if (displayParameters) {
      LOG.info("READING (" + shiftCount + ")shifts passed as arguments");
    }
    for (int i = 0; i < shiftCount; ++i) {
      //for every shiftCount there should be a startTime_?, endTime_?, count_?
      //and there MAY be a delete_?
      temp = request.getParameter("delete_" + i);
      if (temp != null) {
        deleteShift = true;
        shiftToDelete = i;
        break;
      }
      String tStart = request.getParameter("startTime_" + i);
      String tEnd = request.getParameter("endTime_" + i);
      String tCount = request.getParameter("count_" + i);
      int tCnt = Integer.parseInt(tCount);
      String tType = request.getParameter("shift_" + i);
      int tTyp = Assignments.DAY_TYPE;
      LOG.info("shift type read was (" + tType + ")");
      for (int shiftType = 0; shiftType < Assignments.MAX_SHIFT_TYPES && tType != null; ++shiftType) {
        if (tType.equals(Assignments.getShiftName(shiftType))) {
          tTyp = shiftType;
        }
      }

      String tEvent;
      tEvent = ShiftDefinitions.createShiftName(eventName, i);
      if (displayParameters) {
        LOG.info(tEvent + " " + tStart + ", " + tEnd + ", " + tCount + ", " + tTyp);
      }
      todaysData[cnt] = new ShiftDefinitions(tEvent, tStart, tEnd, tCnt, tTyp, LOG);
      if (newShift) {
        if (displayParameters) {
          LOG.info("INSERT this shift: " + todaysData[cnt]);
        }
        patrol.writeShift(todaysData[cnt]);
      }
      todaysData[cnt].setExists(true);

      ++cnt;
    }
    if (displayParameters) {
      LOG.info("DeleteTemplateBtn=" + DeleteTemplateBtn + ", deleteShift=" + deleteShift + " shiftToDelete=" + shiftToDelete);
    }

    // read Shift ski assignments for the specific day
    //      patrol.resetShiftDefinitions();
    //    String lastPos = " ";
    //      Shifts data;
    //just get the shift count
    int selectedSize = 0;

    boolean foundDeleteShift = false;
    for (ShiftDefinitions shiftDefinition : patrol.readShiftDefinitions()) {
      if (displayParameters) {
        LOG.info("read shift:" + shiftDefinition);
      }
      String parsedName = shiftDefinition.parsedEventName();
      if (parsedName.equals(selectedShift)) {
        //check for delete button

        if (displayParameters) {
          LOG.info("deleteShift=" + deleteShift + " shiftToDelete=" + shiftToDelete + " selectedSize=" + selectedSize);
        }
        if ((deleteShift && shiftToDelete == selectedSize) || DeleteTemplateBtn) {
          deleteShift = false;    //don't delete any more (used for single delete's only)
          patrol.deleteShift(shiftDefinition);
          foundDeleteShift = true;
          continue;               //don't add this shift to the 'shifts' array
        }
        else if (foundDeleteShift) {
          //example, if the 2nd of 5 shifts was deleted, then 3,4,& 5th need to be renumbered down to the 2,3, & 4th
          patrol.decrementShift(shiftDefinition);
        }
        else if (!newShift && shiftCount > 0 && !shiftDefinition.equals(todaysData[selectedSize])) {
          if (displayParameters) {
            LOG.info("update this shift: " + selectedSize);
          }
          shiftDefinition = todaysData[selectedSize];
          patrol.writeShift(shiftDefinition);
        }
        ++selectedSize;
      }
      shifts.add(shiftDefinition);
    } //end while Shifts

    //--------------
    // add new shift  - NOTE: I cannot get a Delete and a Add at the same time
    //--------------
    if (addShift) {
      if (displayParameters) {
        LOG.info("--add shift--");
      }
      String start = "8:30";
      String end = "14:00";
      cnt = 1;
      ShiftDefinitions shiftDefinition = new ShiftDefinitions(ShiftDefinitions.createShiftName(selectedShift, selectedSize), start, end, cnt, Assignments.DAY_TYPE, LOG);
      shifts.add(shiftDefinition);   //add to my local array
      patrol.writeShift(shiftDefinition);    //write to database
    }
    patrol.close();
  }

  public void printBody() {
    //only if director
    //      out.println("<hr>");
    out.println("<h2>Edit Shift Templates for the Calendar</h2>");

    out.println("<div align=\"center\">");
    out.println("  <table border=\"3\" cellpadding=\"0\" cellspacing=\"0\" width=\"750\" height=\"368\">");
    out.println("    <tr>");
    out.println("      <td width=\"654\" bgcolor=\"#C0C0C0\" height=\"366\">");
    out.println("<form target='_self' action=\"" + PatrolData.SERVLET_URL + "EditShiftTemplates\" onSubmit=\"return CheckEventName()\" method=POST>");
    out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"resort\" VALUE=\"" + resort + "\">");
    out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"ID\" VALUE=\"" + szMyID + "\">");
    out.println("          <h2 align=\"center\">");
    out.println("          &nbsp; Change/Edit Shift Format</h2>");
    out.println("          <div align=\"center\">");
    out.println("            <table border=\"3\" cellpadding=\"0\" cellspacing=\"0\" width=\"716\" height=\"192\">");
    out.println("          <center>");
    out.println("              <tr>");
    out.println("                <td height=\"148\" width=\"750\">");
    out.println("                  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
    out.println("                  Use Predefined Shift format : <select onChange=\"reloadPage(this)\" size=\"1\" name=\"selectedShift\">");
    PatrolData.AddShiftsToDropDown(out, shifts, selectedShift);
    out.println("                  </select>");
    out.println("                  <br>");
    out.println("                  &nbsp;&nbsp;");
    out.println("                  <div align=\"center\">");
    out.println("                    <table border=\"3\" cellpadding=\"0\" cellspacing=\"0\" width=\"683\" style=\"font-size: 12pt\">");
    out.println("                      <tr>");
    out.println("                        <td width=\"646\" colspan=\"5\">Event Name/Day of Week: <input type=\"text\" onBlur=\"testName(this.value)\" onKeyDown=\"javascript:return captureEnter(event.keyCode)\" name=\"eventName\" value=\"" + selectedShift + "\"size=\"39\"></td>");
    out.println("                      </tr>");
    PatrolData.AddShiftsToTable(out, shifts, selectedShift);
    //New Button
    out.println("                      <tr>");
    //disable "New" button, if at Maximum count
    String newOK = "";
    //12/1/08 this was wrong, the shifts.size() is the total size in the database, not the count of shifts within this shift
    //		if(shifts.size() >= Shifts.MAX_ASSIGNMENT_SIZE)
    //			newOK = " disabled";
    out.println("                        <td width=\"90\"><input type=\"submit\" value=\"New\" " + newOK + " name=\"addShift\"></td>");
    out.println("                        <td width=\"170\">&nbsp;</td>");
    out.println("                        <td width=\"170\">&nbsp;</td>");
    out.println("                        <td width=\"200\">&nbsp;</td>");
    out.println("                        <td width=\"120\">&nbsp;</td>");
    out.println("                      </tr>");

    out.println("                    </table>");
    //      out.println("                   <p><br></p>");
    out.println("                  </div>");
    out.println("                </td>");
    out.println("              </tr>");
    out.println("            </table>");
    out.println("          </div>");
    out.println("NOTE: Pressing 'New' or 'Delete' will do a <b>Save Changes</b> first.");
    out.println("          <p align=\"center\"><input type=\"submit\" value=\"Save Changes\" name=\"SaveChangesBtn\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
    out.println("          <input type=\"button\" value=\"Cancel\" name=\"B6\" onClick=\"goHome()\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
    out.println("          <input type=\"submit\" value=\"Delete Template\" name=\"DeleteTemplateBtn\" onclick=\"return confirmDelete()\">");
    out.println("          </p>");
    out.println("          </form>");
    //      out.println("          <p>&nbsp;</center></td>");
    out.println("      </tr>");
    out.println("    </table>");
    out.println("</div>");

    out.println("-- NOTE: The Shift names '<B>Sunday</B>', '<B>Monday</B>', etc. are <B>Special</B>.  If defined here, they will");
    out.println(" fill in that -empty- day-of-the-week on the calandar.<br>");
    out.println("--Also the shift name '<B>Closed</B>' is reserved, and will not hilight the day, but it will display <font color=\"#FF0000\" size=\"4\">Closed</font>.<br>");
    out.println("--deleteting all the entries for a template, will delete the template.<br>");
    out.println("--If your want more that 10 patrollers on a shift, ie 23, then create two shifts with 10 patrollers, and one with 3 patrollers (all with the same start and end time).");
  /*
      PatrolData patrol = new PatrolData(PatrolData.FETCH_ALL_DATA);
      MemberData member = patrol.nextMember("&nbsp;");
      while(member != null) {
          if(member.getEmailAddress() != "&nbsp;")
              member.setEmail("<a href=\"mailto:"+member.getEmailAddress()+"\">"+member.getEmailAddress()+"</a>");

          if(member.getSub() != null && (member.getSub().startsWith("y") || member.getSub().startsWith("Y")))
              printRow(member.getFullName(),member.getHomePhone(),member.getWorkPhone(),member.getCellPhone(),member.getPager(),member.getEmailAddress());
          member = patrol.nextMember("&nbsp;");   // "&nbsp;" is the default string field
      }
      patrol.close(); //must close connection!
  */
  }
}
