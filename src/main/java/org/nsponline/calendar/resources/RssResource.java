package org.nsponline.calendar.resources;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.nsponline.calendar.utils.Logger;
import org.nsponline.calendar.utils.StaticUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class RssResource {
  private static final int MIN_LOG_LEVEL = Logger.INFO;

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
  public static class RssInit extends HttpServlet {
    public static final String HOME_DIRECTORY = "/var/www/html/rss/";

    static Map<String, String> nameCache = new HashMap<String, String>();

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(this.getClass(), request, "POST", request.getParameter("resort"), MIN_LOG_LEVEL);
      new GetRss(request, response, "POST", LOG);
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    private class GetRss {
      private String pid;
      private String fullName;
      private ObjectMapper mapper = new ObjectMapper();

      private GetRss(HttpServletRequest request, HttpServletResponse response, String methodType, Logger LOG) throws IOException {
        JsonNode node = mapper.readTree(request.getInputStream());
        pid = node.path("pid").asText();
        fullName = node.path("fullName").asText();
        nameCache.put(pid, fullName);
        if(StaticUtils.isEmpty(pid)) {
          StaticUtils.buildAndLogErrorResponse(response, 400, "missing required field 'pid'", LOG);
          return;
        }
        if(StaticUtils.isEmpty(fullName)) {
          StaticUtils.buildAndLogErrorResponse(response, 400, "missing required field 'fullName'", LOG);
          return;
        }

        String rssFeedNameJson = HOME_DIRECTORY + pid + ".json";
        String rssFeedNameXml = HOME_DIRECTORY + pid + ".xml";
        LOG.info("look for file=" + rssFeedNameJson);
        File fileJson = new File(rssFeedNameJson);
        if (fileJson.exists()) {
          System.out.println("RSS feed exists for " + pid);
          StaticUtils.buildAndLogErrorResponse(response, 304, "RSS feed exists for " + pid, LOG);
          return;
        }
        ObjectNode initNode = constructInitNode(pid, fullName);
        try {
          //write json
          FileWriter myWriter = new FileWriter(rssFeedNameJson);
          myWriter.write(initNode.toPrettyString());
          myWriter.close();
          LOG.info("Successfully wrote to the file." + rssFeedNameJson);
          StaticUtils.buildOkResponse(response, toNode(pid, fullName), LOG);
        } catch (IOException e) {
          LOG.info("An error occurred. e.getMessage=" + e.getMessage());
          StaticUtils.buildAndLogErrorResponse(response, 400, "Failed to create RSS feed.  Exception " + e.getMessage(), LOG);
        }
        writeXml(pid, fullName, null);
      }

      private ObjectNode constructInitNode(final String pid, final String fullName) {
        ObjectNode initNode = StaticUtils.nodeFactory.objectNode();
        ObjectNode rssNode = StaticUtils.nodeFactory.objectNode();
        ObjectNode channelNode = StaticUtils.nodeFactory.objectNode();
        String date = new Date().toString();
        channelNode.put("title", fullName + "'s ancestors from FamilySearch");
        channelNode.put("description", fullName + " Ancestor images from FamilySearch for " + fullName);
        channelNode.put("pubDate", date);
        channelNode.put("lastBuildDate", date);
        channelNode.put("generator", "http://www.familysearch.org/");
        rssNode.set("channel", channelNode);
        rssNode.put("version", "2.0");
        initNode.set("rss", rssNode);
        return initNode;
      }

      public ObjectNode toNode(String pid, String fullName) {
        ObjectNode returnNode = StaticUtils.nodeFactory.objectNode();

        returnNode.put("status", "Initialized rss feed for pid=" + pid + ", name=" + fullName);
        return returnNode;
      }

    }

    public static void writeXml(String pid, String fullName, ObjectNode rssNode) {
        /*
  <?xml version="1.0" encoding="utf-8"?>
  <rss version="2.0">
      <channel>
          <title>Steven Gledhill's ancestors from FamilySearch</title>
          <description>Ancestor images from FamilySearch for Steven Gledhill</description>
          <pubDate>Thu, 23 April 2020 12:45:32 -0600</pubDate>
          <lastBuildDate>Thu, 23 April 2020 12:45:32 -0600</lastBuildDate>
          <generator>http://www.familysearch.org/</generator>
          <item>
              <title>Rex Karl Naegle</title>
              <enclosure url="https://sg31b0.familysearch.org/service/records/storage/das-mem/patron/v2/TH-303-38090-73-8/dist.jpg" length="1062139" type="image/jpeg" />
              <pubDate>Thu, 23 April 2020 12:45:32 -0600</pubDate>
          </item>
      </channel>
  </rss>
         */
      if (fullName == null) {
        fullName = nameCache.get(pid) == null ? "fullName" : nameCache.get(pid);
      }
      String rssFeedNameXml = HOME_DIRECTORY + pid + ".xml";
      String date = new Date().toString();

      try {
        System.out.println("writeXml");

        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();

        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();

        Document document = documentBuilder.newDocument();

        // root rss element
        Element root = document.createElement("rss");
        Attr attr = document.createAttribute("version");
        attr.setValue("2.0");
        root.setAttributeNode(attr);
        document.appendChild(root);

        // channel element
        Element channel = document.createElement("channel");
        root.appendChild(channel);

        // title element
        Element title = document.createElement("title");
        title.appendChild(document.createTextNode(fullName + " - " + pid));
        channel.appendChild(title);

        // description element
        Element description = document.createElement("description");
        description.appendChild(document.createTextNode("Ancestor images from FamilySearch for " + fullName + " - " + pid));
        channel.appendChild(description);

        // pubDate element
        Element pubDate = document.createElement("pubDate");
        pubDate.appendChild(document.createTextNode(date));
        channel.appendChild(pubDate);

        // lastBuildDate elements
        Element lastBuildDate = document.createElement("lastBuildDate");
        lastBuildDate.appendChild(document.createTextNode(date));
        channel.appendChild(lastBuildDate);

        // generator elements
        Element generator = document.createElement("generator");
        generator.appendChild(document.createTextNode("http://www.familysearch.org/"));
        channel.appendChild(generator);


        if (rssNode != null) {
          //loop adding item's
  //        <item>
  //            <title>Rex Karl Naegle</title>
  //            <enclosure url="https://sg31b0.familysearch.org/service/records/storage/das-mem/patron/v2/TH-303-38090-73-8/dist.jpg" length="1062139" type="image/jpeg" />
  //            <pubDate>Thu, 23 April 2020 12:45:32 -0600</pubDate>
  //        </item>
          System.out.println("new xml items=" + rssNode.path("rss").path("channel").path("items"));
          if (rssNode.path("rss").path("channel").path("items").isArray()) {
            for (JsonNode itemNode : rssNode.path("rss").path("channel").path("items")) {
              System.out.println("new xml item=" + itemNode.toPrettyString());
              Element itemElement = document.createElement("item");
              addElementIfExists(itemNode.path("title").asText(), "title", document, itemElement);
              addElementIfExists(date, "pubDate", document, itemElement);
  //            addElementIfExists(itemNode, "type", itemElement);
  //            addElementIfExists(itemNode, "size", itemElement);
              addElementIfExists(itemNode.path("height").asText(), "height", document, itemElement);
              addElementIfExists(itemNode.path("width").asText(), "width", document, itemElement);
              addEnclosure(document, itemElement, itemNode.path("url"), itemNode.path("size"), itemNode.path("type"));
  //            itemElement.appendChild(document.createTextNode("Ancestor images from FamilySearch for " + fullName + " - " + pid));
              channel.appendChild(itemElement);
            }
          }
        }

        // create the xml file
        //transform the DOM Object to an XML File
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource domSource = new DOMSource(document);
        StreamResult streamResult = new StreamResult(new File(rssFeedNameXml));


        // If you use
        // StreamResult result = new StreamResult(System.out);
        // the output will be pushed to the standard output ...
        // You can use that for debugging
        transformer.transform(domSource, streamResult);

        System.out.println("Done creating XML File");

      } catch (ParserConfigurationException pce) {
        System.out.println(pce.getMessage());
      } catch (TransformerException tfe) {
        System.out.println(tfe.getMessage());
      }
    }

    private static void addEnclosure(final Document document, final Element itemElement, final JsonNode url, final JsonNode length, final JsonNode type) {
  //            <enclosure url="https://sg31b0.familysearch.org/service/records/storage/das-mem/patron/v2/TH-303-38090-73-8/dist.jpg" length="1062139" type="image/jpeg" />
      System.out.println("addEnclosure=" + url.asText() + "  as " + "enclosure");
      Element enclosure = document.createElement("enclosure");
      Attr attr = document.createAttribute("url");
      attr.setValue(url.asText());
      enclosure.setAttributeNode(attr);
      if (length.asText() != "") {
        attr = document.createAttribute("length");
        attr.setValue(length.asText());
        enclosure.setAttributeNode(attr);
      }
      if (type.asText() != "") {
        attr = document.createAttribute("type");
        attr.setValue(type.asText());
        enclosure.setAttributeNode(attr);
      }
      itemElement.appendChild(enclosure);
    }

    private static void addElementIfExists(String itemText, String elementName, Document document, final Element itemElement) {
      System.out.println("addElementIfExists=" + itemText + "  as " + elementName);
      if (itemText != null && !itemText.trim().equals("")) {
        Element element = document.createElement(elementName);
        element.appendChild(document.createTextNode(itemText));
        itemElement.appendChild(element);
      }
    }

  }

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
  public static class RssAddItem extends HttpServlet {

    private  ObjectMapper mapper = new ObjectMapper();

    //  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
  //    new GetRss(request, response, "GET");
  //  }
  //
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(this.getClass(), request, "GET", request.getParameter("resort"), MIN_LOG_LEVEL);
      new GetRss(request, response, "POST", LOG);
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    private class GetRss {
      private String pid = "";
      private String url = "";
      private String mimeType = "";
      private String size = "";
      private String height = "";
      private String width = "";
      private String title = "";
      private String description = "";

      private GetRss(HttpServletRequest request, HttpServletResponse response, String methodType, Logger LOG) throws IOException {
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
          StaticUtils.buildAndLogErrorResponse(response, 400, "missing required field 'pid'", LOG);
          return;
        }
        if(StaticUtils.isEmpty(url)) {
          StaticUtils.buildAndLogErrorResponse(response, 400, "missing required field 'url'", LOG);
          return;
        }
        if(StaticUtils.isEmpty(mimeType)) {
          StaticUtils.buildAndLogErrorResponse(response, 400, "missing required field 'mimeType'", LOG);
          return;
        }

        String extension = ".json";
        String rssFeedName = "/var/www/html/rss/" + pid + extension;
        System.out.println("look to read file=" + rssFeedName);

        File rssFile = new File(rssFeedName);
        if (!rssFile.exists()) {
          System.out.println("RSS feed not found for " + pid);
          StaticUtils.buildAndLogErrorResponse(response, 400, "RSS feed not found for " + pid, LOG);
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
          StaticUtils.buildOkResponse(response, rssNode, LOG);
          RssInit.writeXml(pid, null, rssNode);
        } catch (FileNotFoundException e) {
          System.out.println("FileNotFoundException: " + e.getMessage());
          StaticUtils.buildAndLogErrorResponse(response, 400, "Error reading RSS feed for " + pid + " err=" + e.getMessage(), LOG);
          return;
        } catch (Exception e) {
          System.out.println("Exception: " + e.getMessage());
          StaticUtils.buildAndLogErrorResponse(response, 400, "Error writing RSS feed for " + pid + " err=" + e.getMessage(), LOG);
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
  public static class RssTerminate extends HttpServlet {

    public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Logger LOG = new Logger(this.getClass(), request, "DELETE", request.getParameter("resort"), MIN_LOG_LEVEL);
      new DeleteRss(request, response, "DELETE", LOG);
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    private class DeleteRss {
      private String pid;

      private DeleteRss(HttpServletRequest request, HttpServletResponse response, String methodType, Logger LOG) throws IOException {
        pid = request.getParameter("pid");
        System.out.println("delete rss feed for pid=" + pid);
        if(StaticUtils.isEmpty(pid)) {
          StaticUtils.buildAndLogErrorResponse(response, 400, "missing required field 'pid'", LOG);
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

        StaticUtils.build204Response(response, LOG);
      }
    }
  }
}
