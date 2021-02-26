package org.nsponline.calendar.rest;

import java.io.*;
import java.util.Date;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.nsponline.calendar.utils.Logger;
import org.nsponline.calendar.utils.StaticUtils;

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
public class RssAddItem extends HttpServlet {

  private  ObjectMapper mapper = new ObjectMapper();

  //  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
//    new GetRss(request, response, "GET");
//  }
//
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    new GetRss(request, response, "POST");
  }

  @SuppressWarnings("InnerClassMayBeStatic")
  private class GetRss {
    private Logger LOG;
    private String pid = "";
    private String url = "";
    private String mimeType = "";
    private String size = "";
    private String height = "";
    private String width = "";
    private String title = "";
    private String description = "";

    private GetRss(HttpServletRequest request, HttpServletResponse response, String methodType) throws IOException {
      JsonNode node = mapper.readTree(request.getInputStream());
      pid = node.path("pid").asText();
      url = node.path("url").asText();
      mimeType = node.path("mimeType").asText();
      size = node.path("size").asText();
      height = node.path("height").asText();
      width = node.path("width").asText();
      title = node.path("title").asText();
      description = node.path("description").asText();
      if(StaticUtils.isEmpty(pid)) {
        StaticUtils.buildAndLogErrorResponse(response, 400, "missing required field 'pid'");
        return;
      }
      if(StaticUtils.isEmpty(url)) {
        StaticUtils.buildAndLogErrorResponse(response, 400, "missing required field 'url'");
        return;
      }
      if(StaticUtils.isEmpty(mimeType)) {
        StaticUtils.buildAndLogErrorResponse(response, 400, "missing required field 'mimeType'");
        return;
      }

      String extension = ".json";
      String rssFeedName = "/var/www/html/rss/" + pid + extension;
      System.out.println("look to read file=" + rssFeedName);

      File rssFile = new File(rssFeedName);
      if (!rssFile.exists()) {
        System.out.println("RSS feed not found for " + pid);
        StaticUtils.buildAndLogErrorResponse(response, 400, "RSS feed not found for " + pid);
        return;
      }

      try {
        String date = new Date().toString();
        InputStream targetStream = new FileInputStream(rssFile);
        ObjectNode rssNode = (ObjectNode)mapper.readTree(targetStream);
        targetStream.close();

        System.out.println("before rssNode=" + rssNode.toPrettyString());
        addItem(date, (ObjectNode)rssNode.path("rss"), pid, url, mimeType, size, height, width, title, description);
        System.out.println("after rssNode=" + rssNode.toPrettyString());

        FileWriter myWriter = new FileWriter(rssFeedName);
        myWriter.write(rssNode.toPrettyString());
        myWriter.close();
        System.out.println("Successfully wrote to the file." + rssFeedName);
        StaticUtils.buildOkResponse(response, rssNode);
        RssInit.writeXml(pid, null, rssNode);
      } catch (FileNotFoundException e) {
        System.out.println("FileNotFoundException: " + e.getMessage());
        StaticUtils.buildAndLogErrorResponse(response, 400, "Error reading RSS feed for " + pid + " err=" + e.getMessage());
        return;
      } catch (Exception e) {
        System.out.println("Exception: " + e.getMessage());
        StaticUtils.buildAndLogErrorResponse(response, 400, "Error writing RSS feed for " + pid + " err=" + e.getMessage());
        return;
      }
    }

    private void addItem(String date, final ObjectNode rssNode, final String pid, final String url, final String mimeType, final String size, final String height, final String width, final String title, final String description) {
      ObjectNode channel = (ObjectNode)rssNode.path("channel");
      channel.put("pubDate", date);
      channel.put("lastBuildDate", date);

      JsonNode items = channel.path("items");
      if (items.isMissingNode()) {
        channel.set("items", StaticUtils.nodeFactory.arrayNode());
      }
      ArrayNode itemsArray = (ArrayNode)channel.path("items");
      ObjectNode item = toNode(date, pid, url, mimeType, size, height, width, title, description);
      itemsArray.add(item);
    }

    public ObjectNode toNode(String date, String pid, final String url, final String mimeType, final String size, final String height, final String width, final String title, final String description) {
      ObjectNode itemNode = StaticUtils.nodeFactory.objectNode();
      itemNode.put("pubDate", date);
//      ObjectNode enclosure = Utils.nodeFactory.objectNode();
      itemNode.put("url", url);
      itemNode.put("type", mimeType);
      if(!StringUtils.isEmpty(size)) {
        itemNode.put("size", size);
      }
      if(!StringUtils.isEmpty(height)) {
        itemNode.put("height", height);
      }
      if(!StringUtils.isEmpty(width)) {
        itemNode.put("width", width);
      }
      if(!StringUtils.isEmpty(title)) {
        itemNode.put("title", title);
      }
      if(!StringUtils.isEmpty(description)) {
        itemNode.put("description", description);
      }

      return itemNode;
    }

  }
}

