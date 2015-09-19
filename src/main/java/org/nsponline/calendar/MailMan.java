package org.nsponline.calendar;

//package org.nspOnline;


import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;


/**
 * Title:        MailMan
 * Description:  Mail Manager
 * Copyright:    Copyright (c) 2001
 * Company:      JarLink
 *
 * @author Jared Allen
 * @version 1.0
 */

public class MailMan {

  private final static boolean DEBUG = true;
  private final static boolean DEBUG_DONT_SEND = true;

  private final static String POP_MAIL = "pop3";
  private final static String INBOX = "INBOX";
  private final static String SMTP_MAIL = "smtp";

  private InternetAddress hostAddress;
  private InternetAddress fromAddress;

  private Store store;
  private Folder folder;

  /**
   * MailMan constructor.
   *
   * @param host     The smtp host address.
   * @param from     The return address.
   * @param fromText from text
   */
  public MailMan(String host, String from, String fromText, SessionData sessionData) {
    debugOut(" CONSTRUCTOR- (host=" + host + ", from=" + from + ", fromText='" + fromText + "')");
    if (DEBUG_DONT_SEND) {
      debugOutDontSend("NOTHING WILL BE SENT BECAUSE OF DEBUG SETTING!");
      return;
    }
    try {
      hostAddress = new InternetAddress(host);
      try {
        fromAddress = new InternetAddress(from, fromText);
        debugOut("  new fromAddress=" + fromAddress);

        Session session = Session.getDefaultInstance(System.getProperties(), null);
        session.setDebug(false);
        store = session.getStore(POP_MAIL);
//        debugOut("  getStore=" + store);
        store.connect(sessionData.getPopHost(), -1, sessionData.getEmailUser(), sessionData.getEmailPassword());
        debugOut("  store.connect(pop3Host=" + sessionData.getPopHost() + ", user=" + sessionData.getEmailUser());
        folder = store.getDefaultFolder();
//        debugOut("  folder=" + folder);
        folder = folder.getFolder(INBOX);
//        debugOut("  folder=" + folder);
        folder.open(Folder.READ_WRITE);
        int totalMessages = folder.getMessageCount();
        debugOut("  MessageCount is " + totalMessages + " for " + sessionData.getPopHost());
      }
      catch (Exception e) {
        System.out.println("ERROR-MailMan constructor mail exception e=" + e);
      }
    }
    catch (AddressException ex) {
      throw new IllegalArgumentException();
    }
  }

  public void close() {
    try {
      if (folder != null) {
//        debugOut("folder.close");
        folder.close(true);
      }
      if (store != null) {
//        debugOut("store.close");
        store.close();
      }
    }
    catch (Exception e) {
      System.out.println("ERROR-MailMan close() exception e=" + e);
    }
  }

  /**
   * Sets the smtp host address.
   *
   * @param host The smtp host address.
   */
  @SuppressWarnings("UnusedDeclaration")
  public void setHostAddress(String host) {
    try {
      hostAddress = new InternetAddress(host);
    }
    catch (AddressException ex) {
      throw new IllegalArgumentException("ERROR-MailMan invalid 'host' address (" + host + ")");
    }
  }

  /**
   * Gets the smtp host address.
   *
   * @return The smtp host address.
   */
  @SuppressWarnings("UnusedDeclaration")
  public String getHostAddress() {
    return hostAddress.getAddress();
  }

  /**
   * Sets the return address.
   *
   * @param from The return address.
   */
  @SuppressWarnings("UnusedDeclaration")
  public void setFromAddress(String from) {
    try {
      fromAddress = new InternetAddress(from);
    }
    catch (AddressException ex) {
      throw new IllegalArgumentException("ERROR-MailMan invalid 'from' address (" + from + ")");
    }
  }

  /**
   * Gets the return address.
   *
   * @return The return address.
   */
  @SuppressWarnings("UnusedDeclaration")
  public String getFromAddress() {
    return fromAddress.getAddress();
  }

  /**
   * Sends a message with the given subject and message body to the given
   * of recipient.  The message is sent via the pre-set smtp host with the
   * pre-set return address.
   *
   * @param subject   Subject of the message.
   * @param message   The message body.
   * @param recipient The recipient.
   * @throws MailManException oops
   */
  public void sendMessage(String subject, String message, String recipient, SessionData sessionData) throws MailManException {
    String[] recipients = new String[1];
    recipients[0] = recipient;
    sendMessage(subject, message, recipients, sessionData);
  }

  /**
   * Sends a message with the given subject and message body to the given list
   * of recipients.  The message is sent via the pre-set smtp host with the
   * pre-set return address.
   *
   * @param subject    Subject of the message.
   * @param message    The message body.
   * @param recipients An array of strings representing all of the
   *                   recipients.
   * @throws MailManException oops
   */
  public void sendMessage(String subject, String message, String[] recipients, SessionData sessionData) throws MailManException {
    debugOut("sendMessage(subject='" + subject + "', message='\n---- message body ----\n" + message + "\n---- end body ---\nrecipients=" + recipients[0] + ")");
    if (DEBUG_DONT_SEND) {
      debugOutDontSend("nothing sent because of debug");
      return;
    }
    debugOut("  hostAddress=" + hostAddress.getAddress());
    Properties props = new Properties();
    Session session;
    MimeMessage msg;
    InternetAddress[] rcptAddresses = new InternetAddress[recipients.length];
    MimeBodyPart body;
    Multipart mp;
    props.put("mail.smtp.host", hostAddress.getAddress());
    props.put("mail.smtp.port", "465"); //(added 10/8/2011)
    props.put("mail.smtp.auth", "true");//(added 10/8/2011)
    session = Session.getDefaultInstance(props, null);
//    debugOut("  session: " + session);
    msg = new MimeMessage(session);
    try {
      msg.setFrom(fromAddress);
      for (int i = 0; i < rcptAddresses.length; i++) {
        try {
          rcptAddresses[i] = new InternetAddress(recipients[i]);
        }
        catch (AddressException ex) {
          throw new MailManException("Invalid recipient address: " + recipients[i]);
        }
      }
      msg.setRecipients(Message.RecipientType.TO, rcptAddresses);
      msg.setSubject(subject);
      msg.setSentDate(new Date());
      // create and fill the first message part
      body = new MimeBodyPart();
      body.setText(message);
      // create the Multipart and add its parts to it
      mp = new MimeMultipart();
      mp.addBodyPart(body);
      // add the Multipart to the message
      msg.setContent(mp);
      // send the message
      Transport transport = session.getTransport(SMTP_MAIL);
//      debugOut("getTransport=" + transport);
      transport.connect(sessionData.getSmtpHost(), sessionData.getEmailUser(), sessionData.getEmailPassword());
//      debugOut("transport.connect(" + _smtpHost + ", user=" + _user + ", pass=" + _password + ")");
      transport.sendMessage(msg, rcptAddresses);
//      debugOut("transport.close()");
      transport.close();
      debugOut("  1 email sent");
    }
    catch (SendFailedException ex) {
      Address[] validUnsent = ex.getValidUnsentAddresses();
      System.out.println("ERROR, there were " + validUnsent.length + " email's not sent");
      throw new MailManException(ex.toString());
    }
    catch (MessagingException ex) {
      throw new MailManException(ex.toString());
    }
  }

  private void debugOut(Object... msg) {
    if (DEBUG) {
      System.out.print("DEBUG-MailMan: ");
      for (Object item : msg) {
        System.out.print(item);
      }
      System.out.println();
    }
  }

  private void debugOutDontSend(String msg) {
    if (DEBUG_DONT_SEND) {
      System.out.println("DEBUG_DONT_SEND-Mailman: " + msg);
    }
  }
//  public static void main(String[] args) throws MailManException {
//    if (args.length < 5) {
//      System.out.println("Invalid arguments");
//      System.out.println("java org.nspOnline.MailMan <host> <from address> <subject> <message> <recipient address>");
//      return;
//    }
//    String smtpHost = args[0];      //zimbra.xmission.com
//    String fromAddress = args[1];   //nancy@gledhills.com
//    String fromName = "Nancy Gledhill";
//    String subject = args[2];       //"subject text"
//    String message = args[3];       //"body text\nline 2"
//    String toAddress = args[4];     //gledhillsr@familysearch.org
//    MailMan mailMan = new MailMan(smtpHost, fromAddress, fromName);
//    mailMan.sendMessage(subject, message, toAddress);
//    mailMan.close();
//  }
}

