package org.nsponline.calendar;


/**
 * @author Steve Gledhill
 */
@SuppressWarnings("WeakerAccess")
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class LoginPayload {
  public String patrollerId;
  public String resort;
  public String password;

  public LoginPayload() { }

  public String toString() {
    return "patrollerId: [" + patrollerId + "], resort: [" + resort + "], password: [" + password + "]";
  }

  @com.fasterxml.jackson.annotation.JsonProperty("resort")
  String getResort() {
    return resort;
  }

  @com.fasterxml.jackson.annotation.JsonProperty("patrollerId")
  String getPatrollerId() {
    return patrollerId;
  }

  @com.fasterxml.jackson.annotation.JsonProperty("password")
  String getPassword() {
    return password;
  }

}

