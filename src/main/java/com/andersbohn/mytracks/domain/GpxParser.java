package com.andersbohn.mytracks.domain;

import java.io.ByteArrayInputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

public class GpxParser {

  private GpxParser() {}

  public record GpxMetadata(String name, String type) {}

  public static GpxMetadata parse(byte[] gpxBytes) {
    try {
      var factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      var doc = factory.newDocumentBuilder().parse(new ByteArrayInputStream(gpxBytes));
      var xpath = XPathFactory.newInstance().newXPath();
      var base = "/*[local-name()='gpx']/*[local-name()='trk']/*[local-name()='";
      var name = (String) xpath.evaluate(base + "name']", doc, XPathConstants.STRING);
      var type = (String) xpath.evaluate(base + "type']", doc, XPathConstants.STRING);
      return new GpxMetadata(blankToNull(name), blankToNull(type));
    } catch (Exception e) {
      return new GpxMetadata(null, null);
    }
  }

  private static String blankToNull(String s) {
    return (s != null && !s.isBlank()) ? s.trim() : null;
  }
}
