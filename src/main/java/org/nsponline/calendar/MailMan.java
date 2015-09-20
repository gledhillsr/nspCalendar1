package org.nsponline.calendar;


import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.*;


/**
 * @author Steve Gledhill
 */

public class MailMan {

  private final static boolean DEBUG = true;
  private final static boolean DEBUG_DONT_SEND = false;
//  private final static String POP_MAIL = "pop3";
//  private final static String INBOX = "INBOX";
//  private final static String SMTP_MAIL = "smtp";
  AmazonSimpleEmailServiceClient sesClient;
  private final String fromAddress;
  private final String fromText;
//  private final SessionData sessionData;
//  private InternetAddress hostAddress;
//  private InternetAddress fromAddress;
//
//  private Store store;
//  private Folder folder;

  /**
   * MailMan constructor.
   *
   * @param host        The smtp host address.
   * @param fromAddress The return address.
   * @param fromText    fromAddress text
   */
  public MailMan(String host, String fromAddress, String fromText, SessionData sessionData) {
//    this.sessionData = sessionData;
    debugOut(" CONSTRUCTOR- (host=" + host + ", fromAddress=" + fromAddress + ", fromText='" + fromText + "')");
    if (DEBUG_DONT_SEND) {
      debugOutDontSend("NOTHING WILL BE SENT BECAUSE OF DEBUG SETTING!");
      return;
    }
//    this.fromAddress = fromAddress;
    this.fromAddress = "steve@gledhills.com";   //todo I want to get this fixed!!!!
    this.fromText = fromText;
    // Instantiate an Amazon SES client, which will make the service call. The service call requires your AWS credentials.
    // Because we're not providing an argument when instantiating the client, the SDK will attempt to find your AWS credentials
    // using the default credential provider chain. The first place the chain looks for the credentials is in environment variables
    // AWS_ACCESS_KEY_ID and AWS_SECRET_KEY.
    // For more information, see http://docs.aws.amazon.com/AWSSdkDocsJava/latest/DeveloperGuide/credentials.html
    BasicAWSCredentials awsCredentials = new BasicAWSCredentials(sessionData.getAWSAccessKeyId(), sessionData.getAWSSecretKey());
    sesClient = new AmazonSimpleEmailServiceClient(awsCredentials);

    // Choose the AWS region of the Amazon SES endpoint you want to connect to. Note that your sandbox
    // status, sending limits, and Amazon SES identity-related settings are specific to a given AWS
    // region, so be sure to select an AWS region in which you set up Amazon SES. Here, we are using
    // the US West (Oregon) region. Examples of other regions that Amazon SES supports are US_EAST_1
    // and EU_WEST_1. For a complete list, see http://docs.aws.amazon.com/ses/latest/DeveloperGuide/regions.html
    Region REGION = Region.getRegion(Regions.US_EAST_1);
    sesClient.setRegion(REGION);
//    try {
//      hostAddress = new InternetAddress(host);
//      try {
//        fromAddress = new InternetAddress(fromAddress, fromText);
//        debugOut("  new fromAddress=" + fromAddress);
//
//        Session session = Session.getDefaultInstance(System.getProperties(), null);
//        session.setDebug(false);
//        store = session.getStore(POP_MAIL);
////        debugOut("  getStore=" + store);
//        store.connect(sessionData.getPopHost(), -1, sessionData.getEmailUser(), sessionData.getEmailPassword());
//        debugOut("  store.connect(pop3Host=" + sessionData.getPopHost() + ", user=" + sessionData.getEmailUser());
//        folder = store.getDefaultFolder();
////        debugOut("  folder=" + folder);
//        folder = folder.getFolder(INBOX);
////        debugOut("  folder=" + folder);
//        folder.open(Folder.READ_WRITE);
//        int totalMessages = folder.getMessageCount();
//        debugOut("  MessageCount is " + totalMessages + " for " + sessionData.getPopHost());
//      }
//      catch (Exception e) {
//        System.out.println("ERROR-MailMan constructor mail exception e=" + e);
//      }
//    }
//    catch (AddressException ex) {
//      throw new IllegalArgumentException();
//    }
  }

//  public void close() {
//    try {
//      if (folder != null) {
////        debugOut("folder.close");
//        folder.close(true);
//      }
//      if (store != null) {
////        debugOut("store.close");
//        store.close();
//      }
//    }
//    catch (Exception e) {
//      System.out.println("ERROR-MailMan close() exception e=" + e);
//    }
//  }
//
//  /**
//   * Sets the smtp host address.
//   *
//   * @param host The smtp host address.
//   */
//  @SuppressWarnings("UnusedDeclaration")
//  public void setHostAddress(String host) {
//    try {
//      hostAddress = new InternetAddress(host);
//    }
//    catch (AddressException ex) {
//      throw new IllegalArgumentException("ERROR-MailMan invalid 'host' address (" + host + ")");
//    }
//  }
//
//  /**
//   * Gets the smtp host address.
//   *
//   * @return The smtp host address.
//   */
//  @SuppressWarnings("UnusedDeclaration")
//  public String getHostAddress() {
//    return hostAddress.getAddress();
//  }
//
//  /**
//   * Sets the return address.
//   *
//   * @param from The return address.
//   */
//  @SuppressWarnings("UnusedDeclaration")
//  public void setFromAddress(String from) {
//    try {
//      fromAddress = new InternetAddress(from);
//    }
//    catch (AddressException ex) {
//      throw new IllegalArgumentException("ERROR-MailMan invalid 'from' address (" + from + ")");
//    }
//  }
//
//  /**
//   * Gets the return address.
//   *
//   * @return The return address.
//   */
//  @SuppressWarnings("UnusedDeclaration")
//  public String getFromAddress() {
//    return fromAddress.getAddress();
//  }

  /**
   * Sends a message with the given subject and message body to the given
   * of recipient.  The message is sent via the pre-set smtp host with the
   * pre-set return address.
   *
   * @param subject   Subject of the message.
   * @param message   The message body.
   * @param recipient The recipient.
   */
  public void sendMessage(String subject, String message, String recipient) {
    String[] recipients = new String[1];
    recipients[0] = recipient;
    sendMessage(subject, message, recipients);
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
  public void sendMessage(String subject, String messageBody, String[] recipients) {
    debugOut("sendMessage(subject='" + subject + "', message='\n---- message body ----\n" + messageBody + "\n---- end body ---\nrecipients=" + recipients[0] + ")");
    if (DEBUG_DONT_SEND) {
      debugOutDontSend("nothing sent because of debug");
      return;
    }

    sendAmazonEmail(fromAddress, recipients, subject, messageBody);
//    debugOut("  hostAddress=" + hostAddress.getAddress());
//    Properties props = new Properties();
//    Session session;
//    MimeMessage msg;
//    InternetAddress[] rcptAddresses = new InternetAddress[recipients.length];
//    MimeBodyPart body;
//    Multipart mp;
//    props.put("mail.smtp.host", hostAddress.getAddress());
//    props.put("mail.smtp.port", "465"); //(added 10/8/2011)
//    props.put("mail.smtp.auth", "true");//(added 10/8/2011)
//    session = Session.getDefaultInstance(props, null);
////    debugOut("  session: " + session);
//    msg = new MimeMessage(session);
//    try {
//      msg.setFrom(fromAddress);
//      for (int i = 0; i < rcptAddresses.length; i++) {
//        try {
//          rcptAddresses[i] = new InternetAddress(recipients[i]);
//        }
//        catch (AddressException ex) {
//          throw new MailManException("Invalid recipient address: " + recipients[i]);
//        }
//      }
//      msg.setRecipients(Message.RecipientType.TO, rcptAddresses);
//      msg.setSubject(subject);
//      msg.setSentDate(new Date());
//      // create and fill the first message part
//      body = new MimeBodyPart();
//      body.setText(message);
//      // create the Multipart and add its parts to it
//      mp = new MimeMultipart();
//      mp.addBodyPart(body);
//      // add the Multipart to the message
//      msg.setContent(mp);
//      // send the message
//      Transport transport = session.getTransport(SMTP_MAIL);
////      debugOut("getTransport=" + transport);
//      transport.connect(sessionData.getSmtpHost(), sessionData.getEmailUser(), sessionData.getEmailPassword());
////      debugOut("transport.connect(" + _smtpHost + ", user=" + _user + ", pass=" + _password + ")");
//      transport.sendMessage(msg, rcptAddresses);
////      debugOut("transport.close()");
//      transport.close();
//      debugOut("  1 email sent");
//    }
//    catch (SendFailedException ex) {
//      Address[] validUnsent = ex.getValidUnsentAddresses();
//      System.out.println("ERROR, there were " + validUnsent.length + " email's not sent");
//      throw new MailManException(ex.toString());
//    }
//    catch (MessagingException ex) {
//      throw new MailManException(ex.toString());
//    }
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

  private void sendAmazonEmail(String from, String[] recipients, String subjectText, String bodyText) {
    try {
      // Construct an object to contain the recipient address.
      Destination destination = new Destination().withToAddresses(recipients);

      // Create the subject and body of the message.
      Content subject = new Content().withData(subjectText);
      Content textBody = new Content().withData(bodyText);
      Body body = new Body().withText(textBody);

      // Create a message with the specified subject and body.
      Message message = new Message().withSubject(subject).withBody(body);

      // Assemble the email.
      SendEmailRequest request = new SendEmailRequest().withSource(from).withDestination(destination).withMessage(message);

      System.out.println("Attempting to send an email through Amazon SES by using the AWS SDK for Java...");

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

