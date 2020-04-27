package org.nsponline.calendar.rest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.nsponline.calendar.misc.Logger;
import org.nsponline.calendar.misc.Utils;

/**
 *   }
 * @Response 400 - Bad Request
 *     X-Reason: "Resort not found"
 *     X-Reason: "Authorization header not found"
 * @Response 401 - Unauthorized
 *     X-Reason: "Invalid Authorization"
 *
 * @author Steve Gledhill
 */
@SuppressWarnings("JavaDoc")
public class RssTerminate extends HttpServlet {

  public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
    new DeleteRss(request, response, "DELETE");
  }

  @SuppressWarnings("InnerClassMayBeStatic")
  private class DeleteRss {
    private String pid;

    private DeleteRss(HttpServletRequest request, HttpServletResponse response, String methodType) throws IOException {
      pid = request.getParameter("pid");
      System.out.println("delete rss feed for pid=" + pid);
      if(Utils.isEmpty(pid)) {
        Utils.buildErrorResponse(response, 400, "missing required field 'pid'");
        return;
      }
      String extension = ".json";
      String rssFeedName = "/var/www/html/rss/" + pid + extension;
      System.out.println("look for file=" + rssFeedName);
      File file = new File(rssFeedName);
      file.delete();

      extension = ".xml";
      rssFeedName = "/var/www/html/rss/" + pid + extension;
      System.out.println("look for file=" + rssFeedName);
      file = new File(rssFeedName);
      file.delete();

      Utils.build204Response(response);
    }
  }
}

