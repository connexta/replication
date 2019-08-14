/**
 * Copyright (c) Connexta
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package com.connexta.ion.replication.adapters.ddf.csw;

import com.connexta.ion.replication.adapters.ddf.MetacardAttribute;
import com.connexta.ion.replication.api.AdapterException;
import com.connexta.ion.replication.api.data.Metadata;
import com.google.common.collect.ImmutableMap;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copied from DDF and modified for replication purposes.
 *
 * <p>Marshals Metadata into Metacard xml
 */
public class MetacardMarshaller {

  private static final String GML_PREFIX = "gml";

  private static final String SOURCE_KEY = "source";

  private static final String TYPE_KEY = "type";

  private static final Map<String, String> NAMESPACE_MAP;

  static {
    String nsPrefix = "xmlns";

    NAMESPACE_MAP =
        new ImmutableMap.Builder<String, String>()
            .put(nsPrefix, Constants.METACARD_SCHEMA)
            .put(nsPrefix + ":" + GML_PREFIX, "http://www.opengis.net/gml")
            .put(nsPrefix + ":xlink", "http://www.w3.org/1999/xlink")
            .put(nsPrefix + ":smil", "http://www.w3.org/2001/SMIL20/")
            .put(nsPrefix + ":smillang", "http://www.w3.org/2001/SMIL20/Language")
            .build();
  }

  private MetacardMarshaller() {}

  public static String marshal(Metadata metadata) {
    EscapingPrintWriter writer = new EscapingPrintWriter(new StringWriter(1024));
    Map<String, MetacardAttribute> attributes = getAttributeMap(metadata);
    writer.startNode("metacard");
    for (Map.Entry<String, String> nsRow : NAMESPACE_MAP.entrySet()) {
      writer.addAttribute(nsRow.getKey(), nsRow.getValue());
    }

    writer.addAttribute(GML_PREFIX + ":id", metadata.getId());

    writer.startNode(TYPE_KEY);
    if (!attributes.containsKey(TYPE_KEY)) {
      writer.setValue(Constants.DEFAULT_METACARD_TYPE_NAME);
    } else {
      writer.setValue(attributes.remove(TYPE_KEY).getValue());
    }
    writer.endNode(); // type

    if (attributes.containsKey(SOURCE_KEY)) {
      writer.startNode(SOURCE_KEY);
      writer.setValue(attributes.remove(SOURCE_KEY).getValue());
      writer.endNode(); // source
    }

    for (MetacardAttribute metacardAttribute : attributes.values()) {
      String attributeName = metacardAttribute.getName();
      if (attributeName.equals(Constants.METACARD_ID)
          || attributeName.equals(Constants.RAW_METADATA_KEY)) {
        continue;
      }
      writeAttributeToXml(writer, metacardAttribute);
    }
    writer.endNode(); // metacard
    return writer.makeString();
  }

  private static Map<String, MetacardAttribute> getAttributeMap(Metadata metadata) {
    if (!(metadata.getRawMetadata() instanceof Map)) {
      throw new AdapterException(
          "Metadata type " + metadata.getType() + " is incompatible with the DDF adapter");
    }
    Map<String, MetacardAttribute> attributes = new HashMap<>();
    ((Map) metadata.getRawMetadata())
        .values().stream()
            .filter(MetacardAttribute.class::isInstance)
            .map(MetacardAttribute.class::cast)
            .forEach(e -> attributes.put(((MetacardAttribute) e).getName(), (MetacardAttribute) e));
    return attributes;
  }

  private static void writeAttributeToXml(EscapingPrintWriter writer, MetacardAttribute attribute) {
    String attributeName = attribute.getName();
    List<String> values = attribute.getValues();
    String format = attribute.getType();
    if (!values.isEmpty() && format != null) {
      writer.startNode(format);
      for (String xmlns : attribute.getXmlns()) {
        String[] nsparts = xmlns.split("=");
        writer.addAttribute(nsparts[0], nsparts[1]);
      }
      writer.addAttribute("name", attributeName);

      for (String value : values) {

        writer.startNode("value");

        if (format.equals("stringxml") || format.equals("geometry")) {
          writer.setRawValue(value);
        } else {
          writer.setValue(value);
        }
        writer.endNode(); // value
      }
      writer.endNode(); // type
    }
  }
}
