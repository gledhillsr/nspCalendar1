package org.nsponline.calendar.resources;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.nsponline.calendar.store.Roster;
import org.nsponline.calendar.utils.Logger;
import org.nsponline.calendar.utils.SessionData;

import static org.nsponline.calendar.store.Roster.*;


@SuppressWarnings({"SqlNoDataSourceInspection", "AccessStaticViaInstance"})
public class OuterUpdateInfo extends ResourceBase {
  @SuppressWarnings("SqlNoDataSourceInspection")
  private final static String[] IDpos = {"P0", "P1", "P2", "P3", "P4", "P5", "P6", "P7", "P8", "P9"};

  //class globals
  private String title;
  private String IDOfEditor, IDToEdit = null, NameToEdit, Purpose;
  private boolean editorIsDirector;
  private Roster userData;
  private Roster oldMemberData;
  private boolean isNewPatroller;
  private boolean deletePatroller;
  private boolean finalDelete;
  private boolean setNewID;
  private String oldMemberID;
  private boolean editInfo;

  OuterUpdateInfo(HttpServletRequest request, HttpServletResponse response, Logger LOG) throws IOException {
    super(request, response, LOG);
    if (!initBaseAndAskForValidCredentials(response, "UpdateInfo")) {
      return;
    }

    readUpdateInfoArgs(request);

    printCommonHeader();

    printTop();

    if (request.getParameter("HomePhone") != null || deletePatroller || finalDelete) //did I post this form to myself?
    {
      printBodySave(request, response, sessionData);
    }
    else {
      printBodyForm(request, response, sessionData);
    }

    printCommonFooter();
  }

  private void readUpdateInfoArgs(HttpServletRequest request) {
    IDOfEditor = sessionData.getLoggedInUserId();
    IDToEdit = IDOfEditor;          //same (for now!)
    NameToEdit = request.getParameter("NameToEdit");
    LOG.debug("NameToEdit=" + NameToEdit);
    oldMemberID = request.getParameter("removeOldID");  //only set on final pass
    LOG.debug("oldMemberID=" + oldMemberID);

    setNewID = deletePatroller = editInfo = false;

    Purpose = request.getParameter("Purpose");
    LOG.debug("Purpose=" + Purpose);
    isNewPatroller = (Purpose != null && Purpose.equals("Add_Patroller"));
    finalDelete = (Purpose != null && Purpose.equals("FinalDelete"));
    setNewID = (Purpose != null && Purpose.equals("setNewID"));  //set 2nd pass
    LOG.debug("Purpose: setNewID=" + setNewID);

    String str1 = request.getParameter("setNewID");                  //set 1st pass
    String str2 = request.getParameter("removeName");
    String str3 = request.getParameter("EditInfo");
    if (str1 != null) {
      LOG.debug("---setNewID(1)---");
      setNewID = true;
      Purpose = str1;
    }
    if (str2 != null) {
      LOG.debug("---deletePatroller---");
      deletePatroller = true;
      Purpose = str2;
    }
    if (str3 != null) {
      LOG.debug("---editInfo---");
      editInfo = true;
      Purpose = str3;
    }
  }

  public int readData(String readID, SessionData sessionData) {
    if ((setNewID || editInfo || deletePatroller || finalDelete) && NameToEdit != null) {

      LOG.debug("in UpdateInfo, setNewID=" + setNewID + ", editInfo=" + editInfo + ", NameToEdit=(" + NameToEdit + ")");
      userData = patrolData.getMemberByLastNameFirstName(NameToEdit);
      LOG.debug("user= (" + userData + ")");
      if (userData == null) {
        LOG.info("UpdateInfo - ERROR, member " + NameToEdit + " not found!");
        out.println("INTERNAL ERROR, member " + NameToEdit + " not found!");
        return 0;
      }
      IDToEdit = userData.getID();
    }
    else {
      IDToEdit = IDOfEditor;  //same
      userData = patrolData.getMemberByID(readID);
    }
    LOG.debug("in UpdateInfo, IDOfEditor" + IDOfEditor + " resort=" + resort);
    Roster editor;
    if (IDOfEditor.equals(sessionData.getBackDoorUser())) {
      editorIsDirector = true;
      return 1;
    }
    editor = patrolData.getMemberByID(IDOfEditor); //ID from cookie
    LOG.debug("in UpdateInfo, editor" + editor + " resort=" + resort);

    if (oldMemberID != null && oldMemberID.length() > 3) {
      oldMemberData = patrolData.getMemberByID(oldMemberID);
    }
    else {
      oldMemberData = null;
    }

    if (editor != null) {
      editorIsDirector = editor.isDirector();
    }

    LOG.debug("in UpdateInfo, returning from ReadData, resort=" + resort);
    return 1;
  } //end ReadData

  public void printTop() {

    title = "Update Personal Information";
    if (Purpose != null) {
      title += " ** " + Purpose + " Patroller **";
    }
    out.println("<title>" + title + "</title>");
    //      if(isNewPatroller) {
    //zz
    out.println("<SCRIPT LANGUAGE =\"JavaScript\">");
    out.println("function validate(val) {");
    out.println("   var last  = document.forms[0].LastName.value");
    out.println("   var first = document.forms[0].FirstName.value");
    out.println("   var ok = false;");
    out.println("   var found=false");
    out.println("   for(var i=0; i < last.length && !found; ++i) {");
    out.println("     var letter = last.charAt(i)");
    out.println("     if(letter != \" \") found = true;");
    out.println("   }");
    out.println("   if(!found)  { alert(\"You MUST enter a non-blank Last Name\"); return false; }");
    out.println("   found=false");
    out.println("   for(var i=0; i < first.length && !found; ++i) {");
    out.println("     var letter = first.charAt(i)");
    out.println("     if(letter != \" \") found = true;");
    out.println("   }");
    out.println("   if(!found)  { alert(\"You MUST enter a non-blank First Name\"); return false; }");
    out.println("   var cnt = document.forms[0].elements.length");
    out.println("   var str = document.forms[0].IDToEdit.value");
    out.println("   var num = parseInt(str);");
    out.println("   if (num >= 10001 && num <= 999999) ok = true;");
    out.println("   if(!ok) alert(\"'ID Number' must be a UNIQUE number between 10000 and 999999.  You entered '\"+str+\"'\");");
    out.println("   return ok;");
    out.println("}");

    out.println("function printWindow(){");
    out.println("   bV = parseInt(navigator.appVersion)");
    out.println("   if (bV >= 4) window.print()");
    out.println("}");

    out.println("</SCRIPT>");
    //      }
    out.println("<META HTTP-EQUIV=\"Pragma\" CONTENT=\"no-cache\">");
    out.println("<META HTTP-EQUIV=\"Expires\" CONTENT=\"-1\">");
    out.println("</head>");
    out.println("<body>");
  }

  public void updateAssignments(String oldMemberID, String IDToEdit, Connection c) {
    PreparedStatement cs;
    ResultSet cr;
    for (int i = 0; i < 10; ++i) {
      String qryString = "SELECT Date, " + IDpos[i] + " FROM `assignments` WHERE " + IDpos[i] + "=" + oldMemberID;
      LOG.logSqlStatement(qryString);
      try {
        cs = c.prepareStatement(qryString);
        cr = cs.executeQuery();
        while (cr.next()) {
          PreparedStatement cs1;
          String szDate = cr.getString("Date");
          qryString = "UPDATE assignments SET " + IDpos[i] + "=\"" + IDToEdit + "\" WHERE Date=\"" + szDate + "\"";
          LOG.logSqlStatement(qryString);
          cs1 = c.prepareStatement(qryString);
          cs1.executeUpdate();
        }

      }
      catch (Exception e) {
        LOG.logException("Error (UpdateInfo line 227) resetting Assignments table ", e);
      } //end try
    }
  }

  @SuppressWarnings({"UnusedDeclaration"})
  public void printBodySave(HttpServletRequest request, HttpServletResponse response, SessionData sessionData) {
    String sz;
    int i;
    if (readData(IDOfEditor, sessionData) == 0) {  //mostly just to get director (ID from cookie)
      out.println("<h2>ERROR, reading record for patroller id [" + IDOfEditor + "]</h2><br>");
    }

    try {

      // Change MyDSN, myUsername and myPassword to your specific DSN
      Connection c = patrolData.getConnection(resort, sessionData, LOG);
      PreparedStatement sRost;

      Roster roster;
      if (deletePatroller || finalDelete) {
        //          md = patrol.getMemberByID(IDToEdit);
        roster = userData;
        LOG.debug("deletePatroller=" + deletePatroller + " finalDelete=" + finalDelete);

      }
      else {
        IDToEdit = request.getParameter(Roster.dbData[Roster.ID_NUM][Roster.SERVLET_NAME]);
        roster = new Roster(request); //read from command line
        //          md.setID(IDToEdit);
      }
      LOG.debug("Member: " + roster);
      //      String szFirst = request.getParameter(MemberData.dbData[MemberData.FIRST][MemberData.SERVLET_NAME]);
      //      String szLast  = request.getParameter(MemberData.dbData[MemberData.LAST][MemberData.SERVLET_NAME]);
      String name = roster.getFullName();

      //        out.println("<h2>Your new record has been updated as follows:</h2><br>");
      if (deletePatroller) {
        out.println("<h2>CAUTION!!! DELETE record for " + name + ":</h2><br>");
      }
      else if (isNewPatroller) {
        out.println("<h2>NEW record for " + name + ":</h2>");
      }
      else if (finalDelete) {
        out.println("<h2>Deleted record for " + name + ":</h2>");
      }
      else {
        out.println("<h2>Updated record for " + name + ":</h2>");
      }
      out.println("host resort: " + patrolData.getResortFullName(resort, LOG) + "<br>");

      int first = 0; // was DB_START;
      int last = Roster.DB_SIZE; //director is last column

      if (!resort.equalsIgnoreCase("Brighton")) {
        last -= 3;
      }
      boolean isError = false;
      //Log.log("-----------************---------------");
      if (deletePatroller) {
        LOG.debug("delete Patroller");
        //          sz = md.getDeleteSQLString();
        //don't DELETE record here
      }
      else if (finalDelete) {
        LOG.debug("Final delete Patroller");
        sz = roster.getDeleteSQLString(resort);
        sRost = c.prepareStatement(sz);
        sRost.executeUpdate();
        //Log.log("executing delete");
        updateAssignments(IDToEdit, "0", c);    //replace ID with 0 in assignment table
      }
      else if (isNewPatroller) {
        LOG.debug("new patroller");
        //check for duplicate ID
        Roster mem = patrolData.getMemberByID(roster.getID());
        if (mem != null) {
          out.println("<h3>ERROR, id '" + roster.getID() + "' is already owned by " + mem.getFullName() + "</h3>");
          out.println("<br><br>Use the 'Back' button on your browser to make a change.");
          isError = true;
        }
        else {
          sz = roster.getInsertSQLString(resort, sessionData);
          sRost = c.prepareStatement(sz);
          sRost.executeUpdate();
        }
      }
      else if (setNewID) {
        LOG.debug("Changing Member ID.  OldID=" + oldMemberID + ", NewID=" + IDToEdit);
        //test if any ID change was made
        Roster mem = patrolData.getMemberByID(roster.getID());
        if (mem != null) {
          out.println("<h3>ERROR, id '" + roster.getID() + "' is already owned by " + mem.getFullName() + "</h3>");
          out.println("<br><br>Use the 'Back' button on your browser to make a change.");
        }
        else if (IDToEdit.equals(oldMemberID)) {
          //if ID was same, just update record
          //use data read from command line
          sz = roster.getUpdateSQLString(resort, sessionData);
          sRost = c.prepareStatement(sz);
          sRost.executeUpdate();
        }
        else {
          //if ID was new, insert new, then delete old
          //insert new ID
          updateAssignments(oldMemberID, IDToEdit, c);
          sz = roster.getInsertSQLString(resort, sessionData);
          sRost = c.prepareStatement(sz);
          sRost.executeUpdate();
          //remove OLD id
          sz = oldMemberData.getDeleteSQLString(resort);
          sRost = c.prepareStatement(sz);
          sRost.executeUpdate();
        }
      }
      else {
        LOG.debug("Simple Update");
        sz = roster.getUpdateSQLString(resort, sessionData);
        sRost = c.prepareStatement(sz);
        sRost.executeUpdate();
      }

      c.close();
      LOG.debug("UpdateInfo closed static connection for " + resort);
      if (editorIsDirector) {
        first = 0;
        last = Roster.DB_SIZE;      //show director field for director
        if (!resort.equalsIgnoreCase("Brighton")) {
          last -= 5; //skip display of all "Credit" stuff, except for brighton resort
        }
      }

      if (isError) {
        return; //don't continue
      }

      out.println("<table border=\"1\" width=\"580\" cellspacing=\"0\" cellpadding=\"0\">");
      //print data that is to be saved (not in FORM format)
      String szTmp;
      for (i = first; i < last; ++i) {
        if (!resort.equalsIgnoreCase("Brighton")) {
          if (i == Roster.TEAM_LEAD || i == Roster.MENTORING) {
            continue;   //only display if brighton
          }
        }
        //only display Team Lead trained to Directors
        if (i == Roster.TEAM_LEAD && !editorIsDirector) {
          continue;
        }
        //          localData[i] = request.getParameter(MemberData.dbData[i][MemberData.SERVLET_NAME]);
        szTmp = roster.memberData[i];
        if (szTmp == null || szTmp.length() == 0) {
          szTmp = " ";
        }
        //no longer support fields for "credits"
        if (isUnsupportedCreditsField(i) || i == NEW_PASSWORD) { //old fields
          continue;
        }

        out.println("<tr>");
        out.println("<td width=\"230\" valign=\"middle\"><font size=3>" + Roster.dbData[i][Roster.DLG_NAME] + "</font></td>");
        out.println("<td width=\"350\" valign=\"left\"><font size=3>");
        if (i == Roster.COMMITMENT) {
          out.println(Roster.getCommitmentString(szTmp));
        }
        else if (i == Roster.TEAM_LEAD || i == Roster.MENTORING ) {
          //Log.log(i+") = ("+szTmp+")");
          if (szTmp.equals("1")) {
            out.println("Yes");
          }
          else {
            out.println("No");
          }
        }
        else if (i == Roster.INSTRUCTOR) {
          out.println(Roster.getInstructorString(szTmp));
        }
        else if (i == Roster.COMMENTS && !editorIsDirector) {
          //ONLY display this field is the editor is a director
        }
        else {
          out.println(szTmp);
        }
        out.println("&nbsp;</font></td>");
        out.println("</tr>");
      }
      out.println("</table>");
      if (deletePatroller) {
        out.println("<form target='_self' action=\"" + patrolData.SERVLET_URL + "UpdateInfo\" method=POST>");
        out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"resort\" VALUE=\"" + resort + "\">");
        //Log.log("+++ in deletePatroller, IDOfEditor="+IDOfEditor);
        out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"ID\" VALUE=\"" + IDOfEditor + "\">");
        out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"Purpose\" VALUE=\"FinalDelete\">");
        out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"NameToEdit\" VALUE=\"" + NameToEdit + "\">");
        out.println("<INPUT TYPE=SUBMIT VALUE=\"Delete Record\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
        out.println("<input type=\"button\" value=\"Cancel\" name=\"B3\" onClick=\"history.back()\">");
        out.println("</form>");
        //???
      }
      else if (isNewPatroller) {
        String nextPage = patrolData.SERVLET_URL + "UpdateInfo?resort=" + resort + "&Purpose=Add_Patroller&ID=" + IDOfEditor;
        out.println("<br><INPUT TYPE=button VALUE=\"Add Another New Patroller\" onClick=window.location=\"" + nextPage + "\">&nbsp;&nbsp;&nbsp;&nbsp;");
        nextPage = patrolData.SERVLET_URL + "Directors?resort=" + resort + "&ID=" + IDOfEditor;
        out.println("<INPUT TYPE=button VALUE=\"Return to Director's Page\" onClick=window.location=\"" + nextPage + "\">");
      }
      else // if (finalDelete || setNewID)
      {
        String nextPage = patrolData.SERVLET_URL + "Directors?resort=" + resort + "&ID=" + IDOfEditor;
        if (!editorIsDirector) {
          nextPage = patrolData.SERVLET_URL + "UpdateInfo?resort=" + resort + "&ID=" + IDOfEditor;
        }
        out.println("<INPUT TYPE=button VALUE=\"Done\" onClick=window.location=\"" + nextPage + "\">");
      }
    }
    catch (Exception e) {
      out.println("Error connecting or reading table " + e.getMessage() + "<br>");
      LOG.logException("UpdateInfo: Error connecting or reading exception=", e);
      LOG.error("in UpdateInfo.  IDOfEditor=" + IDOfEditor);
      LOG.error("in UpdateInfo.  IDToEdit=" + IDToEdit);
    } //end try

  } //end printBodySave


  @SuppressWarnings({"UnusedDeclaration"})
  public void printBodyForm(HttpServletRequest request, HttpServletResponse response, SessionData sessionData) {
    int i;

    int isValidNum = 0;

    if (IDToEdit != null) {
      isValidNum = readData(IDToEdit, sessionData);
    }
    LOG.debug(" UpdateInfo:printBodyForm IDToEdit=" + IDToEdit + ", isValidNum=" + isValidNum);
    if (isValidNum == 1) {
      //valid #
      String fullName;
      if (isNewPatroller) {
        userData = new Roster(LOG);
        fullName = "NEW PATROLLER";
        IDToEdit = "";
      }
      else {
        if (userData == null) {
          out.println("<h2>Internal Error, invalid patroller number: [" + IDToEdit + "]</h2><br>");
          return;
        }
        fullName = userData.getFullName();
      }
      // header -------------------
      out.println("<h2>Personal Information for:  " + fullName + "</h2><br>");
      out.println("host resort: <b>" + patrolData.getResortFullName(resort, LOG) + "</b>");
      out.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
//      out.println("<a href=\"javascript:printWindow()\">Print This Page</a></font><br>");

      out.println("<P>");
      out.print("<form target='_self' action=\"");
      out.print(patrolData.SERVLET_URL + "UpdateInfo?resort=" + resort + "&ID=" + IDOfEditor);
      out.print("\" ");
      //          out.print("UpdateInfo\" ");
      out.println("method=POST ");
      if (isNewPatroller || setNewID) {
        out.println("onSubmit=\"return validate(1)\" ");
      }
      out.println(">");

      out.println("<table border=\"0\" width=\"900\" cellspacing=\"0\" cellpadding=\"0\">");

      int first = 0;
      int last = Roster.DB_SIZE;      //show director field for director
      //            if(!editorIsDirector))
      //                last -= 1;
      for (i = first; i < last; ++i) {
        showInputField(i, userData.memberData[i], editorIsDirector);
      }

      out.println("</table>");
      //hidden fields ----------
      if (isNewPatroller) {
        out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"Purpose\" VALUE=\"Add_Patroller\">");
        out.println("<input type=submit value=\"New Profile\">");
      }
      else if (setNewID) {
        out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"Purpose\" VALUE=\"setNewID\">");
        out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"removeOldID\" VALUE=\"" + IDToEdit + "\">");
        out.println("<input type=submit value=\"Set New ID\">");
      }
      else {

        //submit button ---------------------
        out.println("<input type=submit value=\"Update Profile\">");
      }
      out.println("</form>");
    }
    else {
      //no number
      out.println("<h3>" + title + "</h3><br>");

      //          out.println("Invalid ID number<br>");
      out.println("<P>");
      out.print("<form target='_self' action=\"");
      out.print("UpdateInfo?resort=" + resort + "&ID=" + IDOfEditor + "\" ");
      out.println("method=POST>");
      out.println("Patrol Number:");
      out.println("<input type=text size=7 maxlength=7 name=ID value=\"" + IDToEdit + "\">");
      out.println("<br>");
      //            out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"ID\" VALUE=\"" + IDOfEditor + "\">");
      out.println("<input type=submit>");
      out.println("</form>");
    }
    LOG.debug(" UpdateInfo: end of printBodyForm IDToEdit = " + IDToEdit + ", isValidNum=" + isValidNum);
  }

  public boolean isVisibleField(int index, boolean editorIsDirector) {
    //fields ONLY visible to directors
    if (!editorIsDirector) {
      switch (index) {
        case Roster.COMMENTS:
        case Roster.TEAM_LEAD:
          return false;
      }
    }
    if (isUnsupportedCreditsField(index) || index == NEW_PASSWORD) {
      return false;
    }
    //fields only visible is using locker room scheduling software
    if (!resort.equalsIgnoreCase("Brighton")) {
      switch (index) {
        case Roster.TEAM_LEAD:
        case Roster.MENTORING:
          return false;
      }
    }
    return true;
  }

  public void showInputField(int index, String szDefault, boolean editorIsDirector) {

    if (isUnsupportedCreditsField(index)) { //old field
      return;
    }

    String label = Roster.dbData[index][Roster.DLG_NAME];
    if (index == PASSWORD && !isNewPatroller && !deletePatroller && !finalDelete && !setNewID) {
      //the only other case is editing a patroller
      label = "Old password is hidden<br><b>Reset Password to</b>:&nbsp;<br>Blank keeps old password";
    }
    String szField = Roster.dbData[index][Roster.SERVLET_NAME];
    if (!isVisibleField(index, editorIsDirector)) {
      out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"" + szField + "\" VALUE=\"" + szDefault + "\">");
      //Log.log(index+") field "+szField+"=("+szDefault+") is hidden, for resort "+resort);
      return;
    }
    int len = 30;
    out.println("<tr>");
    if (index == Roster.COMMENTS && !editorIsDirector) {
      //display NOTHING if the editor is not a director
      label = "&nbsp;";
    }
    //display column 1, the field description
    out.println("<td width=\"351\" valign=\"middle\"><font size=3>" + label + "</font></td>");
    //display column 2, the input field
    int width = (index == Roster.INSTRUCTOR) ? 650 : 600;
    out.println("<td width=\"" + width + "\" valign=\"left\"><font size=3>");
    //  static final int CLASSIFICATION=1;
    if (index == Roster.ID_NUM) {
      len = 6;
    }

    if (index == Roster.COMMITMENT) {
      if (editorIsDirector) {
        out.println("<SELECT NAME=" + szField + " CLASS=\"select\">");
        out.println("<OPTION " + (szDefault.equals("2") ? "SELECTED " : "") + " VALUE=\"2\">Full Time");
        out.println("<OPTION " + (szDefault.equals("1") ? "SELECTED " : "") + " VALUE=\"1\">Part Time");
        out.println("<OPTION " + (szDefault.equals("0") ? "SELECTED " : "") + " VALUE=\"0\">Inactive");
        out.println("</SELECT>");
      }
      else {
        out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"" + szField + "\" VALUE=\"" + szDefault + "\">");
        if (szDefault.equals("2")) {
          out.println("Full Time&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(Can only be changed by a director)");
        }
        else if (szDefault.equals("1")) {
          out.println("Part Time&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(Can only be changed by a director)");
        }
        else {
          out.println("Inactive&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(Can only be changed by a director)");
        }
      }
    }
    else if (index == Roster.INSTRUCTOR) {
      //convert string to number
      int idx = 0;
      try {
        idx = Integer.parseInt(szDefault);
      }
      catch (Exception e) {
        //DO NOTHING
      }
      if (editorIsDirector) {
        out.println("                        <input type=\"checkbox\" name=\"OEC\"" + ((idx & 1) == 1 ? "checked" : "") + ">OEC");
        out.println("&nbsp;&nbsp;&nbsp;&nbsp;<input type=\"checkbox\" name=\"CPR\"" + ((idx & 2) == 2 ? "checked" : "") + ">CPR");
        out.println("&nbsp;&nbsp;&nbsp;&nbsp;<input type=\"checkbox\" name=\"SKI\"" + ((idx & 4) == 4 ? "checked" : "") + ">Ski");
        out.println("&nbsp;&nbsp;&nbsp;&nbsp;<input type=\"checkbox\" name=\"TOBOGGAN\"" + ((idx & 8) == 8 ? "checked" : "") + ">Toboggan");
      }
      else {
        if ((idx & 1) == 1) {
          out.println("OEC&nbsp;&nbsp;&nbsp;&nbsp;");
          out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"OEC\" VALUE=\"1\">");
        }
        if ((idx & 2) == 2) {
          out.println("CPR&nbsp;&nbsp;&nbsp;&nbsp;");
          out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"CPR\" VALUE=\"1\">");
        }
        if ((idx & 4) == 4) {
          out.println("SKI&nbsp;&nbsp;&nbsp;&nbsp;");
          out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"SKI\" VALUE=\"1\">");
        }
        if ((idx & 8) == 8) {
          out.println("TOBOGGAN&nbsp;&nbsp;&nbsp;&nbsp;");
          out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"TOBOGGAN\" VALUE=\"1\">");
        }
        if (idx == 0) {
          out.println("None&nbsp;&nbsp;&nbsp;&nbsp;");
        }
        out.println("&nbsp;&nbsp;&nbsp;&nbsp;(Can only be changed by a director)");
      }
    }
    else if (index == Roster.TEAM_LEAD) {
      // must be a director to get this far
      out.println("<SELECT NAME=" + szField + " CLASS=\"select\">");
      out.println("<OPTION " + (szDefault.equals("1") ? "SELECTED " : "") + " VALUE=\"1\">Yes");
      out.println("<OPTION " + (!szDefault.equals("1") ? "SELECTED " : "") + " VALUE=\"0\">No");
      out.println("</SELECT>");
    }
    else if (index == Roster.MENTORING) {
      //must be Brighton to get this far
      if (editorIsDirector) {
        out.println("<SELECT NAME=" + szField + " CLASS=\"select\">");
        out.println("<OPTION " + (szDefault.equals("1") ? "SELECTED " : "") + " VALUE=\"1\">Yes");
        out.println("<OPTION " + (!szDefault.equals("1") ? "SELECTED " : "") + " VALUE=\"0\">No");
        out.println("</SELECT>");
      }
      else {
        if (szDefault.equals("1")) {
          out.println("Yes");
        }
        else {
          out.println("No");
        }
        out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"" + szField + "\" VALUE=\"" + szDefault + "\">");
      }
    }
    else if (index == Roster.DIRECTOR) {
      if (editorIsDirector) {
        out.println("<SELECT NAME=" + szField + " CLASS=\"select\">");
        boolean isYes = szDefault.equals("yes");
        boolean isYesEmail = szDefault.equals("yesEmail");
        out.println("<OPTION " + (isYes ? "SELECTED " : "") + " VALUE=\"yes\">Yes");
        out.println("<OPTION " + (isYesEmail ? "SELECTED " : "") + " VALUE=\"yesEmail\">Yes & email me ALL schedule changes");
        out.println("<OPTION " + (!isYes && !isYesEmail ? "SELECTED " : "") + " VALUE=\"\">No");
        out.println("</SELECT>");
      }
      else {
        out.println("No");
        out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"" + szField + "\" VALUE=\"no\">");
      }
    }
    else if (index == Roster.CLASSIFICATION) {
      if (editorIsDirector) {
        out.println("<SELECT NAME=" + szField + " CLASS=\"select\">");
        out.println("<OPTION " + (szDefault.equals("ALM") ? "SELECTED " : "") + " VALUE=\"ALM\">Alumni");
        out.println("<OPTION " + (szDefault.equals("AUX") ? "SELECTED " : "") + " VALUE=\"AUX\">Auxiliary");
        out.println("<OPTION " + (szDefault.equals("BAS") ? "SELECTED " : "") + " VALUE=\"BAS\">Basic");
        out.println("<OPTION " + (szDefault.equals("CAN") ? "SELECTED " : "") + " VALUE=\"CAN\">Candidate");
        //                out.println("<OPTION "+(szDefault.equals("INA") ? "SELECTED " : "") +" VALUE=\"INA\">Inactive");
        out.println("<OPTION " + (szDefault.equals("PRO") ? "SELECTED " : "") + " VALUE=\"PRO\">Pro");
        out.println("<OPTION " + (szDefault.equals("SR") ? "SELECTED " : "") + " VALUE=\"SR\">Senior");
        out.println("<OPTION " + (szDefault.equals("SRA") ? "SELECTED " : "") + " VALUE=\"SRA\">Senior Auxiliary");
        out.println("<OPTION " + (szDefault.equals("TRA") ? "SELECTED " : "") + " VALUE=\"TRA\">Transfer");
        out.println("<OPTION " + (szDefault.equals("OTH") ? "SELECTED " : "") + " VALUE=\"OTH\">Other");
        out.println("</SELECT>");
      }
      else {
        String classification = userData.getFullClassification();
        if (classification == null) {
          classification = userData.getClassification();
        }
        out.println(classification);
        out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"" + szField + "\" VALUE=\"" + szDefault + "\">");
      }
    }
    else if (index == Roster.SUB) {
      out.println("<SELECT NAME=" + szField + " CLASS=\"select\">");
      out.println("<OPTION " + (szDefault.equals("yes") ? "SELECTED " : "") + " VALUE=\"yes\">Yes");
      out.println("<OPTION " + (!szDefault.equals("yes") ? "SELECTED " : "") + " VALUE=\"\">No");
      out.println("</SELECT>");
    }
    else if (index == Roster.EMERGENCY) {
      out.println("<SELECT NAME=" + szField + " CLASS=\"select\">");
      out.println("<OPTION " + (szDefault.trim().equals("") ? "SELECTED " : "") + " VALUE=\"\">No");
      out.println("<OPTION " + (szDefault.equals("day") ? "SELECTED " : "") + " VALUE=\"day\">Day Only");
      out.println("<OPTION " + (szDefault.equals("night") ? "SELECTED " : "") + " VALUE=\"night\">Night Only");  //brighton
      out.println("<OPTION " + (szDefault.equals("both") ? "SELECTED " : "") + " VALUE=\"both\">Both (Days & Nights)");
      out.println("</SELECT>");
    }
    else if (index == Roster.LAST_UPDATED) {
      //              out.println("<input type=text size=12 name="+szField+" value=\""+szDefault+"\" readonly>");
      out.println(szDefault);
      out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"" + szField + "\" VALUE=\"" + szDefault + "\">");
    }
    else if (index == Roster.ID_NUM) {
      if (isNewPatroller || setNewID) {
        //enable edit
        out.println("<input type=text size=7 maxlength=7 name=" + szField + " value=\"" + szDefault + "\">");
      }
      else {
        //display only
        out.println(IDToEdit);
        //but needs IDToEdit Field
        out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"IDToEdit\" VALUE=\"" + IDToEdit + "\">");
      }
    }
    else if (index == Roster.COMMENTS) {
      //display NOTHING if the editor is not a director
      if (editorIsDirector) {
        out.println("<textarea cols=70 rows=4 name=" + szField + ">" + szDefault + "</textarea>");
      }
    }
    else {
      if (szDefault == null || index == PASSWORD) {
        szDefault="";
      }
      out.println("<input type=text size=" + len + " name=" + szField + " value=\"" + szDefault + "\">");
    }

    out.println("</font></td>");
    out.println("</tr>");
  }
}
