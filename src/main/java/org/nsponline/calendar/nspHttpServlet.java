package org.nsponline.calendar;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.nsponline.calendar.misc.*;

abstract public class nspHttpServlet extends HttpServlet {
  public Logger LOG;
  public PrintWriter out;
  public ValidateCredentials credentials;
  public String resort;
  public SessionData sessionData;

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    nspInit(request, response, "GET");
    if (!PatrolData.isValidResort(resort)) {
      Utils.buildAndLogErrorResponse(response, 400, "Resort not found (" + resort + "). Class=" + getServletClass().getSimpleName());

      return;
    }
    String userAgent = request.getHeader("user-agent");
    if (Utils.isRequestFromBot(userAgent)) {
      Utils.buildAndLogErrorResponse(response, 401, "Unauthorized agent (" + userAgent + "). Class=" + getServletClass().getSimpleName());
      return;
    }
    servletBody(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    nspInit(request, response, "POST");
    if (!PatrolData.isValidResort(resort)) {
      Utils.buildAndLogErrorResponse(response, 400, "Resort not found (" + resort + "). Class=" + getServletClass().getSimpleName());
      return;
    }
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

  abstract Class<?> getServletClass();

  abstract String getParentIfBadCredentials();

  private void nspInit(HttpServletRequest request, HttpServletResponse response, String methodType) throws IOException{
    out = response.getWriter();
    resort = request.getParameter("resort");
    LOG = new Logger(getServletClass(), request, methodType, resort, Logger.INFO);
    sessionData = new SessionData(request, out, LOG);
    credentials = new ValidateCredentials(sessionData, request, response, getParentIfBadCredentials(), LOG);
    response.setContentType("text/html");
//    String sessionId = request.getHeader("Authorization");
//    if (Utils.isEmpty(sessionId)) {
//      Utils.buildErrorResponse(response, 400, "Authorization header not found");
//      return;
//    }

    LOG.logRequestParameters();
  }
}
