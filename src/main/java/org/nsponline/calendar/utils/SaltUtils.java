package org.nsponline.calendar.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.nsponline.calendar.DailyReminder;

public class SaltUtils {
  MessageDigest md;

  public SaltUtils() throws NoSuchAlgorithmException {
    md = MessageDigest.getInstance("SHA-512");
  }

  public void doOneTimeWriteToProperties() {
    SecureRandom random = new SecureRandom();
    byte[] salt = new byte[16];
    random.nextBytes(salt);
    System.out.println("ZZ NEW salt=" + Arrays.toString(salt));
    String hex = Hex.encodeHexString(salt);
    System.out.println(hex);
    try {
      byte[] salt2 = Hex.decodeHex(hex);
      System.out.println("ZZ  salt2=" + Arrays.toString(salt2));
    }
    catch (DecoderException e) {
      System.exit(1);
    }
  }

  private byte[] hashPassword(SessionData sessionData, String passwordToHash) {
    byte[] salt = sessionData.readSalt();
    if (salt == null) {
      return null;
    }
    md.update(salt);
    return md.digest(passwordToHash.getBytes(StandardCharsets.UTF_8));
  }

  @SuppressWarnings("DuplicatedCode")
  public static void main(String[] args) throws NoSuchAlgorithmException {    //this NEEDS to be static
    Properties properties = System.getProperties();
    PrintWriter out = new PrintWriter(System.out);
    SessionData sessionData = new SessionData(properties, out);
    Logger LOG = new Logger(DailyReminder.class, null, "DailyReminder", "DailyReminder", Logger.INFO);
    out.println("******************************************************");
    out.println("START RUNNING DAILY REMINDER: " + new Date().toString());
    out.println("******************************************************");
    if (sessionData.getDbPassword() == null) {
      System.out.println("properties not found");
      System.exit(1);
    }
    new SaltUtils().doOneTimeWriteToProperties();
    String pass = "xyzzy";
    byte[] saltedPass = new SaltUtils().hashPassword(sessionData, pass);
//    System.out.println(new String(saltedPass));
  }
}
