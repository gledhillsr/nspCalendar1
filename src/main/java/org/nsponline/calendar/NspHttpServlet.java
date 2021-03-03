package org.nsponline.calendar;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.nsponline.calendar.utils.*;

abstract public class NspHttpServlet extends HttpServlet {
  //todo ******
  //todo 1/20/2021  delete this class
  //todo ******

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    ServletData servletData = nspInit(request, response, "GET");
    commonSetupServletBody(request, response, servletData);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    ServletData servletData = nspInit(request, response, "POST");
    commonSetupServletBody(request, response, servletData);
  }

  public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
    ServletData servletData = nspInit(request, response, "DELETE");
    commonSetupServletBody(request, response, servletData);
  }

  private void commonSetupServletBody(HttpServletRequest request, HttpServletResponse response, ServletData servletData) throws IOException {
//    resort = request.getParameter("resort");
    if (!PatrolData.isValidResort(servletData.getResort())) {
      StaticUtils.buildAndLogErrorResponse(response, 400, "Resort not found (" + servletData.getResort() + "). Class=" + getServletClass().getSimpleName());
      return;
    }
    String userAgent = request.getHeader("user-agent");
    if (StaticUtils.isRequestFromBot(userAgent)) {
      StaticUtils.buildAndLogErrorResponse(response, 401, "Unauthorized agent (" + userAgent + "). Class=" + getServletClass().getSimpleName());
      return;
    }
    servletBody(request, response, servletData);
  }

  public String getServletInfo() {
    return "Database maintenance for " + getServletClass().getName();
  }

  abstract void servletBody(HttpServletRequest request, HttpServletResponse response, ServletData servletData) throws IOException;

  abstract Class<?> getServletClass();

  abstract String getParentIfBadCredentials();

  private ServletData nspInit(HttpServletRequest request, HttpServletResponse response, String methodType) throws IOException{
    ServletData servletData = new ServletData(request, response, methodType);
    //todo 2/11/2021 remove these globals.  Already in servletData
//    out = response.getWriter();
//    resort = request.getParameter("resort");
//    sessionData = new SessionData(request, response.getWriter(), servletData.getLOG());
//    credentials = new ValidateCredentials(sessionData, request, response, getParentIfBadCredentials(), servletData.getLOG());
    return servletData;
  }

  protected class ServletData {
    private Logger sdLOG;
    private String sdResort;
    private PrintWriter sdOut;
    private SessionData sdSessionData;
    private ValidateCredentialsRedirectIfNeeded sdCredentials;

    ServletData(HttpServletRequest request, HttpServletResponse response, String methodType) throws IOException {
      sdResort = request.getParameter("resort");
      sdLOG = new Logger(getServletClass(), request, methodType, sdResort, Logger.INFO);
      sdOut = response.getWriter();
      sdSessionData = new SessionData(request, sdOut, sdLOG);
      sdCredentials = new ValidateCredentialsRedirectIfNeeded(sdSessionData, request, response, getParentIfBadCredentials(), sdLOG);

      response.setContentType("text/html");
      sdLOG.logRequestParameters();
    }

    Logger getLOG() {
      return sdLOG;
    }

    String getResort() {
      return sdResort;
    }

    PrintWriter getOut() {
      return sdOut;
    }

    ValidateCredentialsRedirectIfNeeded getCredentials() {
      return sdCredentials;
    }

    SessionData getSessionData() {
      return sdSessionData;
    }
  }
}
