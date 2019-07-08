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
import com.connexta.ion.replication.api.Replication;
import com.connexta.ion.replication.api.data.Metadata;
import com.connexta.ion.replication.data.MetadataImpl;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
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
 * <p>Converts CSW Record to a Metadata.
 */
public class CswRecordConverter implements Converter {

  private static final Logger LOGGER = LoggerFactory.getLogger(CswRecordConverter.class);
  private static final HierarchicalStreamCopier COPIER = new HierarchicalStreamCopier();

  private static final List<String> COMPLEX_TYPE = Arrays.asList("geometry", "stringxml");

  @Override
  public boolean canConvert(Class clazz) {
    return Metadata.class.isAssignableFrom(clazz);
  }

  @Override
  public void marshal(
      Object o,
      HierarchicalStreamWriter hierarchicalStreamWriter,
      MarshallingContext marshallingContext) {
    throw new UnsupportedOperationException("Marshaling not supported by CswRecordConverter");
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {

    Map<String, String> namespaceMap = null;
    Object namespaceObj = context.get(Constants.NAMESPACE_DECLARATIONS);

    if (namespaceObj instanceof Map<?, ?>) {
      namespaceMap = (Map<String, String>) namespaceObj;
    }

    return createMetadataFromCswRecord(reader, namespaceMap);
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

  public static Metadata createMetadataFromCswRecord(
      HierarchicalStreamReader hreader, Map<String, String> namespaceMap) {

    StringWriter metadataWriter = new StringWriter();
    HierarchicalStreamReader reader = copyXml(hreader, metadataWriter, namespaceMap);

    Map<String, MetacardAttribute> metadataMap = new HashMap<>();
    metadataMap.put(
        Constants.RAW_METADATA_KEY,
        new MetacardAttribute(Constants.RAW_METADATA_KEY, null, metadataWriter.toString()));
    String id = reader.getAttribute("gml:id");
    metadataMap.put(Constants.METACARD_ID, new MetacardAttribute(Constants.METACARD_ID, null, id));
    // If we want to grab the type we will have to do so below. As you move through the child nodes
    // check if the node name is type and save the value.

    parseToMap(reader, metadataMap);

    Date metacardModified = convertToDate(metadataMap.get(Constants.METACARD_MODIFIED));
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

    MetadataImpl metadata = new MetadataImpl(metadataMap, Map.class, id, metacardModified);
    if (metadataMap.get(Constants.RESOURCE_SIZE) != null) {
      metadata.setResourceSize(Long.parseLong(metadataMap.get(Constants.RESOURCE_SIZE).getValue()));
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

    metadataMap.get(Constants.METACARD_TAGS).getValues().forEach(metadata::addTag);
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
}
