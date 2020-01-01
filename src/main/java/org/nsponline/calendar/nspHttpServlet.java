package org.nsponline.calendar;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.nsponline.calendar.misc.Logger;
import org.nsponline.calendar.misc.SessionData;
import org.nsponline.calendar.misc.ValidateCredentials;

abstract public class nspHttpServlet extends HttpServlet {
  public Logger LOG;
  public PrintWriter out;
  public ValidateCredentials credentials;
  public String resort;
  public SessionData sessionData;

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    nspInit(request, response, "GET");
    servletBody(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    nspInit(request, response, "POST");
    servletBody(request, response);
  }

  public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
    nspInit(request, response, "DELETE");
    servletBody(request, response);
  }

  public String getServletInfo() {
    return "Database maintenance for " + getServletClass().getName();
  }

  abstract void servletBody(HttpServletRequest request, HttpServletResponse response) throws IOException;

  abstract Class getServletClass();

  abstract String getParentIfBadCredentials();

  private void nspInit(HttpServletRequest request, HttpServletResponse response, String methodType) throws IOException{
    out = response.getWriter();
    sessionData = new SessionData(request, out);
    resort = request.getParameter("resort");
    LOG = new Logger(getServletClass(), request, methodType, resort);
    credentials = new ValidateCredentials(sessionData, request, response, getParentIfBadCredentials(), LOG);
    response.setContentType("text/html");

    LOG.logRequestParameters();
  }
}
