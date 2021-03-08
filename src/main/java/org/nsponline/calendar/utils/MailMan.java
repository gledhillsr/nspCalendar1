package org.nsponline.calendar.utils;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;

import java.util.Collections;

/**
 * @author Steve Gledhill
 */

@SuppressWarnings("CommentedOutCode")
public class MailMan {

  @SuppressWarnings("unused")
  private static final boolean DEBUG = true;
  private static final boolean DEBUG_DONT_SEND = false;
  @SuppressWarnings("WeakerAccess")
  static final String CONFIGSET = "nspMail";

  private final AmazonSimpleEmailService sesClient;
  private String fromAddress;
  private final String replyToAddress;
  private final Logger LOG;

  public MailMan(String fromAddress, SessionData sessionData, Logger LOG) {
    this.LOG = LOG;
    if (DEBUG_DONT_SEND) {
      debugOutDontSend("NOTHING WILL BE SENT BECAUSE OF DEBUG SETTING!");
      return;
    }
    if (StaticUtils.isValidEmailAddress(fromAddress)) {
      this.replyToAddress = fromAddress;
      LOG.debug("DEBUG - MailMan setting replyToAddress to: " + fromAddress);
    }
    else {
      this.replyToAddress = null;
      if (StaticUtils.isNotEmpty(fromAddress)) {
        LOG.debug("DEBUG - replyToAddress invalid email address: [" + fromAddress + "]");
      }
    }
    try {
      String resort = sessionData.getLoggedInResort();
      ResortData resortInfo = PatrolData.getResortInfo(resort); //will return null if invalid resort (like in dailyReminder because no HttpSession)
      if (resortInfo != null && StaticUtils.isNotEmpty(resortInfo.getDirectorsVerifiedEmail())) {
        //todo NOTE: this is where we load the resort/directors email address
        this.fromAddress = resortInfo.getDirectorsVerifiedEmail();
      }
      else {
        this.fromAddress = "steve@gledhills.com";
      }
    } catch (NoClassDefFoundError e) {
      this.fromAddress = "steve@gledhills.com";
    }

    sesClient = AmazonSimpleEmailServiceClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
//    logger();
  }

  public void sendMessage(String subject, String messageBody, String toAddress) {
//    logger();
    if (DEBUG_DONT_SEND) {
      debugOutDontSend("nothing sent because of debug");
      return;
    }
    sendAmazonEmail(fromAddress, replyToAddress, toAddress, subject, messageBody);
  }

  private void sendAmazonEmail(String from, String replyToAddress, String toAddress, String subject, String textBody) {
    try {
      Destination destination = new Destination().withToAddresses(toAddress);
      final Message message = new Message()
        .withBody(new Body()
//                    .withHtml(new Content()
//                                .withCharset("UTF-8").withData(htmlBody))
                    .withText(new Content()
                                .withData(textBody)))
        .withSubject(new Content()
                       .withData(subject));
      SendEmailRequest request = new SendEmailRequest()
        .withDestination(destination)
        .withMessage(message)
        .withSource(from)
        // Comment or remove the next line if you are not using a
        // configuration set
        .withConfigurationSetName(CONFIGSET);

      if (replyToAddress != null) {
        request.setReplyToAddresses(Collections.singletonList(replyToAddress));
      }

      Long startMillis = System.nanoTime() / 1000000L;
      SendEmailResult result = sesClient.sendEmail(request);
      Long endMillis = System.nanoTime() / 1000000L;
      LOG.info("Email sent from=" + from + ", to: " + destination + ", mailTime=" + (endMillis - startMillis) + " milliSeconds.  result=" + result.toString());
    }
    catch (Exception ex) {
      LOG.error("The email was not sent.  Exception message: " + ex.getMessage());
    }
  }

//  private static void logger(Object... msg) {
//    System.out.println("MailMan: zzz");
//    for (Object item : msg) {
//      System.out.print(item); //keep this here
//    }
//    System.out.println("");
//  }
//
  private void debugOutDontSend(String msg) {
    if (DEBUG_DONT_SEND) {
//      Logger.printToLogFileStatic(sessionData.getRequest(), sessionData.getLoggedInResort(), "DEBUG_DONT_SEND-Mailman: " + msg);
      LOG.info("DEBUG_DONT_SEND-Mailman: " + msg);
    }
  }

//  public static void main(String[] args) throws MailManException {
//    if (args.length < 5) {
//      Log.log("Invalid arguments");
//      Log.log("java org.nspOnline.MailMan <host> <from address> <subject> <message> <recipient address>");
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

