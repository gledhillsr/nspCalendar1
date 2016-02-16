package org.nsponline.calendar.misc;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.*;
import com.mysql.jdbc.StringUtils;

import java.util.Collections;

/**
 * @author Steve Gledhill
 */

public class MailMan {

  @SuppressWarnings("unused")
  private final static boolean DEBUG = false;
  private final static boolean DEBUG_DONT_SEND = false;

  private AmazonSimpleEmailServiceClient sesClient;
  private String fromAddress;
  private String replyToAddress;

  /**
   * MailMan constructor.
   *
   * @param host        The smtp host address.
   * @param fromAddress The return address. ie: steve@gledhills.com
   * @param fromText    fromAddress text    ie: Steve Gledhill
   */
  public MailMan(String host, String fromAddress, String fromText, SessionData sessionData) {
    if (DEBUG_DONT_SEND) {
      debugOutDontSend(sessionData, "NOTHING WILL BE SENT BECAUSE OF DEBUG SETTING!");
      return;
    }
    if (Utils.isValidEmailAddress(fromAddress)) {
      this.replyToAddress = fromAddress;
      System.out.println("DEBUG - MailMan setting replyToAddress to: " + fromAddress);
    }
    else {
      this.replyToAddress = null;
      System.out.println("DEBUG - MailMan failed to set replyToAddress to: [" + fromAddress + "]");
    }
    String resort = sessionData.getLoggedInResort();
    ResortData resortInfo = PatrolData.getResortInfo(resort);
    if (resortInfo != null && Utils.isNotEmpty(resortInfo.getDirectorsVerifiedEmail())) {
      this.fromAddress = resortInfo.getDirectorsVerifiedEmail();
    }
    else {
      this.fromAddress = "steve@gledhills.com";
    }
    logger(sessionData, "MailMan(host=" + host + ", fromAddress=" + this.fromAddress+ ", replyToAddress=" + this.replyToAddress + ", fromText='" + fromText + "')");
    // Instantiate an Amazon SES client, which will make the service call. The service call requires your AWS credentials.
    // Because we're not providing an argument when instantiating the client, the SDK will attempt to find your AWS credentials
    // using the default credential provider chain. The first place the chain looks for the credentials is in environment variables
    // AWS_ACCESS_KEY_ID and AWS_SECRET_KEY.
    // For more information, see http://docs.aws.amazon.com/AWSSdkDocsJava/latest/DeveloperGuide/credentials.html
    if (!StringUtils.isNullOrEmpty(sessionData.getAWSAccessKeyId())) {
      BasicAWSCredentials awsCredentials = new BasicAWSCredentials(sessionData.getAWSAccessKeyId(), sessionData.getAWSSecretKey());
      sesClient = new AmazonSimpleEmailServiceClient(awsCredentials);
    }
    else {
      //if no access key was found, then use credentials from server.  Usually an instance profile
      sesClient = new AmazonSimpleEmailServiceClient();
    }

    // Choose the AWS region of the Amazon SES endpoint you want to connect to. Note that your sandbox
    // status, sending limits, and Amazon SES identity-related settings are specific to a given AWS
    // region, so be sure to select an AWS region in which you set up Amazon SES. Here, we are using
    // the US West (Oregon) region. Examples of other regions that Amazon SES supports are US_EAST_1
    // and EU_WEST_1. For a complete list, see http://docs.aws.amazon.com/ses/latest/DeveloperGuide/regions.html
    Region REGION = Region.getRegion(Regions.US_EAST_1);
    sesClient.setRegion(REGION);
  }

  /**
   * Sends a message with the given subject and message body to the given
   * of recipient.  The message is sent via the pre-set smtp host with the
   * pre-set return address.
   *
   * @param subject   Subject of the message.
   * @param message   The message body.
   * @param recipient The recipient.
   */
  public void sendMessage(SessionData sessionData, String subject, String message, String recipient) {
    String[] recipients = new String[1];
    recipients[0] = recipient;
    sendMessage(sessionData, subject, message, recipients);
  }

  /**
   * Sends a message with the given subject and message body to the given list
   * of recipients.  The message is sent via the pre-set smtp host with the
   * pre-set return address.
   *
   * @param subject     Subject of the message.
   * @param messageBody The message body.
   * @param recipients  An array of strings representing all of the
   *                    recipients.
   */
  public void sendMessage(SessionData sessionData, String subject, String messageBody, String[] recipients) {
    logger(sessionData, "sendMessage(subject='" + subject + "', message='\n---- message body ----\n" + messageBody + "\n---- end body ---\nrecipients=" + recipients[0] + ")");
    if (DEBUG_DONT_SEND) {
      debugOutDontSend(sessionData, "nothing sent because of debug");
      return;
    }

    sendAmazonEmail(fromAddress, replyToAddress, recipients, subject, messageBody);
  }

  private void logger(SessionData sessionData, Object... msg) {
    Utils.printToLogFile(sessionData.getRequest(), "MailMan: ");
    for (Object item : msg) {
      System.out.print(item); //keep this here
    }
    System.out.println(); //keep this here
  }

  private void debugOutDontSend(SessionData sessionData, String msg) {
    if (DEBUG_DONT_SEND) {
      Utils.printToLogFile(sessionData.getRequest(), "DEBUG_DONT_SEND-Mailman: " + msg);
    }
  }

  private void sendAmazonEmail(String from, String replyToAddress, String[] recipients, String subjectText, String bodyText) {
    try {
      // Construct an object to contain the recipient address.
      Destination destination = new Destination().withToAddresses(recipients);
/*
'From' => '"Joe" <joe@website.com>',  //verified address
'Reply-To' => '<joe+hello@website.com>',
'Subject' => 'subject',
'Date' => date("r"),
'X-Mailer' => 'SS/Application'
 */
      // Create the subject and body of the message.
      Content subject = new Content().withData(subjectText);
      Content textBody = new Content().withData(bodyText);
      Body body = new Body().withText(textBody);

      // Create a message with the specified subject and body.
      Message message = new Message().withSubject(subject).withBody(body);

      // Assemble the email.
      SendEmailRequest request = new SendEmailRequest().withSource(from).withDestination(destination).withMessage(message);
      if (replyToAddress != null) {
        request.setReplyToAddresses(Collections.singletonList(replyToAddress));
      }
//      debugOut("Attempting to send an email through Amazon SES by using the AWS SDK for Java...");

      // Send the email.
      Long startMillis = System.nanoTime() / 1000;
      SendEmailResult result = sesClient.sendEmail(request);
      Long endMillis = System.nanoTime() / 1000;
      System.out.println("Email sent in " + (endMillis - startMillis) + " milli seconds.  result=" + result.toString());
    }
    catch (Exception ex) {
      System.out.println("The email was not sent.");
      System.out.println("Error message: " + ex.getMessage());
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

