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
package com.connexta.replication.adapters.ddf.csw;

import com.connexta.replication.adapters.ddf.DdfMetadata;
import com.connexta.replication.adapters.ddf.MetacardAttribute;
import com.connexta.replication.api.AdapterException;
import com.connexta.replication.api.Replication;
import com.connexta.replication.api.data.Metadata;
import com.google.common.collect.ImmutableMap;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.copy.HierarchicalStreamCopier;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.io.xml.CompactWriter;
import com.thoughtworks.xstream.io.xml.XppReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * Copied from DDF and modified for replication purposes.
 *
 * <p>Marshals Metadata into Metacard xml
 */
public class MetacardMarshaller {
  private static final Logger LOGGER = LoggerFactory.getLogger(MetacardMarshaller.class);

  private static final HierarchicalStreamCopier COPIER = new HierarchicalStreamCopier();

  private static final List<String> COMPLEX_TYPE = Arrays.asList("geometry", "stringxml");

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

  public static Map<String, String> metacardNamespaceMap() {
    return new HashMap<>(NAMESPACE_MAP);
  }

  public static DdfMetadata unmarshal(HierarchicalStreamReader reader, Map<String, String> xmlns) {
    StringWriter metadataWriter = new StringWriter();
    MetacardMarshaller.copyXml(reader, metadataWriter, xmlns);

    String metadataStr = metadataWriter.toString();
    return unmarshal(metadataStr, MetacardMarshaller.metacardNamespaceMap());
  }

  public static DdfMetadata unmarshal(String mcardXml, Map<String, String> xmlns) {
    XmlPullParser parser;
    try {
      parser = XmlPullParserFactory.newInstance().newPullParser();
    } catch (XmlPullParserException e) {
      throw new ConversionException("Unable to create XmlPullParser, cannot parse XML.", e);
    }
    HierarchicalStreamReader reader;
    try {
      // NOTE: must specify encoding here, otherwise the platform default
      // encoding will be used which will not always work
      reader =
          new XppReader(
              new InputStreamReader(IOUtils.toInputStream(mcardXml, StandardCharsets.UTF_8.name())),
              parser);
    } catch (IOException e) {
      LOGGER.debug("Unable create reader with UTF-8 encoding", e);
      reader =
          new XppReader(
              new InputStreamReader(IOUtils.toInputStream(mcardXml, StandardCharsets.UTF_8)),
              parser);
    }
    return createMetadataFromMetacardRecord(mcardXml, reader, xmlns);
  }

  private static void copyElementWithAttributes(
      HierarchicalStreamReader source,
      HierarchicalStreamWriter destination,
      Map<String, String> namespaceMap) {
    destination.startNode(source.getNodeName());
    int attributeCount = source.getAttributeCount();
    for (int i = 0; i < attributeCount; i++) {
      destination.addAttribute(source.getAttributeName(i), source.getAttribute(i));
    }
    if (namespaceMap != null && !namespaceMap.isEmpty()) {
      for (Entry<String, String> entry : namespaceMap.entrySet()) {
        if (StringUtils.isBlank(source.getAttribute(entry.getKey()))) {
          destination.addAttribute(entry.getKey(), entry.getValue());
        }
      }
    }
    String value = source.getValue();
    if (value != null && value.length() > 0) {
      destination.setValue(value);
    }
    while (source.hasMoreChildren()) {
      source.moveDown();
      COPIER.copy(source, destination);
      source.moveUp();
    }
    destination.endNode();
  }

  /**
   * Copies the entire XML element {@code reader} is currently at into {@code writer} and returns a
   * new reader ready to read the copied element. After the call, {@code reader} will be at the end
   * of the element that was copied.
   *
   * <p>If {@code attributeMap} is provided, the attributes will be added to the copy.
   *
   * @param reader the reader currently at the XML element you want to copy
   * @param writer the writer that the element will be copied into
   * @param attributeMap the map of attribute names to values that will be added as attributes of
   *     the copy, may be null
   * @return a new reader ready to read the copied element
   * @throws ConversionException if a parser to use for the new reader can't be created
   */
  public static HierarchicalStreamReader copyXml(
      HierarchicalStreamReader reader, StringWriter writer, Map<String, String> attributeMap) {
    copyElementWithAttributes(reader, new CompactWriter(writer, new NoNameCoder()), attributeMap);

    XmlPullParser parser;
    try {
      parser = XmlPullParserFactory.newInstance().newPullParser();
    } catch (XmlPullParserException e) {
      throw new ConversionException("Unable to create XmlPullParser, cannot parse XML.", e);
    }

    try {
      // NOTE: must specify encoding here, otherwise the platform default
      // encoding will be used which will not always work
      return new XppReader(
          new InputStreamReader(
              IOUtils.toInputStream(writer.toString(), StandardCharsets.UTF_8.name())),
          parser);
    } catch (IOException e) {
      LOGGER.debug("Unable create reader with UTF-8 encoding", e);
      return new XppReader(
          new InputStreamReader(IOUtils.toInputStream(writer.toString(), StandardCharsets.UTF_8)),
          parser);
    }
  }

  public static @Nullable Date convertToDate(@Nullable MetacardAttribute value) {
    if (value == null) {
      return null;
    }
    /* Dates are strings and expected to be in ISO8601 format, YYYY-MM-DD'T'hh:mm:ss.sss,
    per annotations in the CSW Record schema. At least the date portion must be present -
    the time zone and time are optional.*/
    try {
      return ISODateTimeFormat.dateOptionalTimeParser().parseDateTime(value.getValue()).toDate();
    } catch (IllegalArgumentException e) {
      LOGGER.debug("Failed to convert to date {} from ISO Format: {}", value, e);
    }

    // try from java date serialization for the default locale
    try {
      return DateFormat.getDateInstance().parse(value.getValue());
    } catch (ParseException e) {
      LOGGER.debug("Unable to convert date {} from default locale format {} ", value, e);
    }

    // default to current date
    LOGGER.debug("Unable to convert {} to a date object", value);
    return null;
  }

  private static DdfMetadata createMetadataFromMetacardRecord(
      String metadataStr, HierarchicalStreamReader reader, Map<String, String> xmlns) {

    Map<String, MetacardAttribute> metadataMap = new HashMap<>();
    metadataMap.put(
        Constants.RAW_METADATA_KEY,
        new MetacardAttribute(Constants.RAW_METADATA_KEY, null, metadataStr));
    Iterator<String> attributeNames = reader.getAttributeNames();
    while (attributeNames.hasNext()) {
      String name = attributeNames.next();
      if (StringUtils.startsWith(name, Constants.XMLNS)) {
        String attributeValue = reader.getAttribute(name);
        xmlns.put(name, attributeValue);
      }
    }
    String gmlNs =
        xmlns.entrySet().stream()
            .filter(e -> e.getValue().equals("http://www.opengis.net/gml"))
            .map(e -> e.getKey().split(":")[1])
            .findFirst()
            .orElse("gml");
    String id = reader.getAttribute(gmlNs + ":id");
    metadataMap.put(Constants.METACARD_ID, new MetacardAttribute(Constants.METACARD_ID, null, id));
    // If we want to grab the type we will have to do so below. As you move through the child nodes
    // check if the node name is type and save the value.

    parseToMap(reader, metadataMap);

    Date metacardModified = convertToDate(metadataMap.get(Constants.METACARD_MODIFIED));
    if (metacardModified == null) {
      // older ddf instances only had the modified field not metacard.modified
      metacardModified = convertToDate(metadataMap.get(Constants.MODIFIED));
    }
    if (metadataMap.get(Constants.VERSION_OF_ID) != null
        && metadataMap.get(Constants.VERSION_OF_ID).getValue() != null) {
      // Since we are dealing with a delete revision metacard, we need to make sure the
      // returned metadata has the original metacard id and modified date.
      id = metadataMap.get(Constants.VERSION_OF_ID).getValue();
      metacardModified = convertToDate(metadataMap.get(Constants.VERSIONED_ON));
    }

    if (metacardModified == null) {
      throw new AdapterException("Can't convert csw metacard without a metacard.modified field");
    }

    DdfMetadata metadata =
        new DdfMetadata(metadataStr, String.class, id, metacardModified, metadataMap);
    metadata.setMetadataSize(metadataStr.length());
    if (metadataMap.get(Constants.RESOURCE_SIZE) != null) {
      metadata.setResourceSize(
          Long.parseLong(
              metadataMap.get(Constants.RESOURCE_SIZE).getValue().replace(" bytes", "")));
      metadata.setResourceModified(convertToDate(metadataMap.get(Constants.MODIFIED)));

      try {
        metadata.setResourceUri(new URI(metadataMap.get(Constants.RESOURCE_URI).getValue()));
      } catch (URISyntaxException e) {
        LOGGER.warn(
            "Invalid resource URI of {} for {}",
            metadataMap.get(Constants.RESOURCE_URI).getValue(),
            id);
      }
    }

    metadataMap
        .getOrDefault(
            Constants.METACARD_TAGS,
            new MetacardAttribute(Constants.METACARD_TAGS, "string", Constants.DEFAULT_TAG))
        .getValues()
        .forEach(metadata::addTag);
    MetacardAttribute origin = metadataMap.get(Replication.ORIGINS);
    if (origin != null) {
      origin.getValues().forEach(metadata::addLineage);
    }
    MetacardAttribute versionAction = metadataMap.get(Constants.ACTION);
    if (versionAction != null) {
      metadata.setIsDeleted(versionAction.getValue().startsWith("Deleted"));
    }
    metadataMap.remove(Constants.DERIVED_RESOURCE_URI);
    metadataMap.remove(Constants.DERIVED_RESOURCE_DOWNLOAD_URL);

    return metadata;
  }

  private static void parseToMap(
      HierarchicalStreamReader reader, Map<String, MetacardAttribute> metadataMap) {
    while (reader.hasMoreChildren()) {
      reader.moveDown();

      String entryType = reader.getNodeName();
      String attributeName = reader.getAttribute("name");
      List<String> xmlns = new ArrayList<>();
      int count = reader.getAttributeCount();
      for (int i = 0; i < count; i++) {
        if (reader.getAttributeName(i).startsWith("xmlns")) {
          xmlns.add(reader.getAttributeName(i) + "=" + reader.getAttribute(i));
        }
      }
      if (!reader.hasMoreChildren()) {
        metadataMap.put(entryType, new MetacardAttribute(entryType, null, reader.getValue()));
        reader.moveUp();
        continue;
      }

      if (COMPLEX_TYPE.contains(entryType)) {
        reader.moveDown();
        reader.moveDown();
        StringWriter xmlWriter = new StringWriter();
        copyXml(reader, xmlWriter, null);
        metadataMap.put(
            attributeName,
            new MetacardAttribute(
                attributeName, entryType, Collections.singletonList(xmlWriter.toString()), xmlns));
        reader.moveUp();
        reader.moveUp();
        reader.moveUp();
        continue;
      }
      List<String> values = new ArrayList<>();
      while (reader.hasMoreChildren()) {
        reader.moveDown();
        values.add(reader.getValue());
        reader.moveUp();
      }

      LOGGER.trace("attribute name: {} value: {}.", attributeName, values);
      if (StringUtils.isNotEmpty(attributeName) && !values.isEmpty()) {
        metadataMap.put(
            attributeName, new MetacardAttribute(attributeName, entryType, values, xmlns));
      }

      reader.moveUp();
    }
  }

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
    if (!(metadata instanceof DdfMetadata)) {
      throw new AdapterException(
          "Metadata type " + metadata.getClass() + " is incompatible with the DDF adapter");
    }
    return ((DdfMetadata) metadata).getAttributes();
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
