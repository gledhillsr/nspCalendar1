package org.nsponline.calendar;

/**
 * @author Steve Gledhill
 */
public class ResortData {
  private String resortShortName;
  private String resortFullName;
  private String resortURL;
  private String resortImage;
  private int imageHeight;
  private int imageWidth;

  public ResortData(String resortShortName, String resortFullName, String resortURL, String resortImage, int imageHeight, int imageWidth) {
    this.resortShortName = resortShortName;
    this.resortFullName = resortFullName;
    this.resortURL = resortURL;
    this.resortImage = resortImage;
    this.imageHeight = imageHeight;
    this.imageWidth = imageWidth;
  }

  public String getResortShortName() {
    return resortShortName;
  }

  public String getResortFullName() {
    return resortFullName;
  }

  public String getResortURL() {
    return resortURL;
  }

  public String getResortImage() {
    return resortImage;
  }

  public int getImageHeight() {
    return imageHeight;
  }

  public int getImageWidth() {
    return imageWidth;
  }
}
