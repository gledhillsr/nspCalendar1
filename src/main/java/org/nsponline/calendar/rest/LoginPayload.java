package org.nsponline.calendar.rest;


/**
 * @author Steve Gledhill
 */
@SuppressWarnings("WeakerAccess")
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class LoginPayload {
  public String id;
  public String resort;
  public String password;

  public LoginPayload() { }

  public String toString() {
    return "id: [" + id + "], password: [" + password + "]";
  }

  @com.fasterxml.jackson.annotation.JsonProperty("id")
  String getId() {
    return id;
  }

  @com.fasterxml.jackson.annotation.JsonProperty("password")
  String getPassword() {
    return password;
  }

}

