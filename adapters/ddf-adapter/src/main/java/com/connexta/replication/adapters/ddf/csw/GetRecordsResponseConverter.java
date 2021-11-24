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

import com.connexta.replication.data.MetadataImpl;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.codice.ditto.replication.api.AdapterException;
import org.codice.ditto.replication.api.data.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copied from DDF and modified for replication purposes.
 *
 * <p>Converts a {@link CswRecordCollection} into a {@link
 * net.opengis.cat.csw.v_2_0_2.GetRecordsResponseType} with CSW records
 */
public class GetRecordsResponseConverter implements Converter {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetRecordsResponseConverter.class);

  private Map<String, Converter> transformProviders;

  /**
   * Creates a new GetRecordsResponseConverter Object
   *
   * @param transformProviders The converter map which will be used to lookup the appropriate
   *     transformer to transform a {@link Metadata} into an appropriate XML format and vice versa.
   */
  public GetRecordsResponseConverter(Map<String, Converter> transformProviders) {
    this.transformProviders = transformProviders;
  }

  @Override
  public boolean canConvert(Class type) {
    boolean canConvert = CswRecordCollection.class.isAssignableFrom(type);
    LOGGER.trace("Can convert? {}", canConvert);
    return canConvert;
  }

  @Override
  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    throw new UnsupportedOperationException();
  }

  /**
   * Parses GetRecordsResponse XML of this form:
   *
   * <p>
   *
   * <pre>{@code
   * <csw:GetRecordsResponse xmlns:csw="http://www.opengis.net/cat/csw">
   *     <csw:SearchStatus status="subset" timestamp="2013-05-01T02:13:36+0200"/>
   *     <csw:SearchResults elementSet="full" nextRecord="11"
   *         numberOfRecordsMatched="479" numberOfRecordsReturned="10"
   *         recordSchema="csw:Record">
   *         <csw:Record xmlns:csw="http://www.opengis.net/cat/csw">
   *         ...
   *         </csw:Record>
   *         <csw:Record xmlns:csw="http://www.opengis.net/cat/csw">
   *         ...
   *         </csw:Record>
   *
   * }</pre>
   */
  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    if (transformProviders == null || transformProviders.isEmpty()) {
      throw new AdapterException(
          "Unable to locate Converter for outputSchema: " + Constants.CSW_OUTPUT_SCHEMA);
    }
    CswRecordCollection cswRecords = new CswRecordCollection();
    List<Metadata> metacards = cswRecords.getCswRecords();

    copyXmlNamespaceDeclarationsIntoContext(reader, context);
    while (reader.hasMoreChildren()) {
      reader.moveDown();

      if (reader.getNodeName().contains("SearchResults")) {
        setSearchResults(reader, cswRecords);

        // Loop through the <SearchResults>, converting each
        // <csw:Record> into a Metacard
        while (reader.hasMoreChildren()) {
          reader.moveDown(); // move down to the <csw:Record> tag
          String name = reader.getNodeName();
          LOGGER.trace("node name = {}", name);
          Metadata metacard =
              (Metadata)
                  context.convertAnother(
                      null,
                      MetadataImpl.class,
                      transformProviders.get(cswRecords.getRecordSchema()));
          metacards.add(metacard);
          reader.moveUp();
        }
      }
      reader.moveUp();
    }

    LOGGER.debug("Unmarshalled {} metacards", metacards.size());
    if (LOGGER.isTraceEnabled()) {
      int index = 1;
      for (Metadata m : metacards) {
        LOGGER.trace("metacard {}: ", index);
        LOGGER.trace("    id = {}", m.getId());
        index++;
      }
    }

    return cswRecords;
  }

  private void setSearchResults(HierarchicalStreamReader reader, CswRecordCollection cswRecords) {

    String numberOfRecordsMatched = reader.getAttribute("numberOfRecordsMatched");
    LOGGER.debug("numberOfRecordsMatched = {}", numberOfRecordsMatched);
    String numberOfRecordsReturned = reader.getAttribute("numberOfRecordsReturned");
    LOGGER.debug("numberOfRecordsReturned = {}", numberOfRecordsReturned);
    cswRecords.setNumberOfRecordsMatched(Long.parseLong(numberOfRecordsMatched));
    cswRecords.setNumberOfRecordsReturned(Long.parseLong(numberOfRecordsReturned));
    cswRecords.setRecordSchema(reader.getAttribute("recordSchema"));
  }

  /**
   * Copies the namespace declarations on the XML element {@code reader} is currently at into {@code
   * context}. The namespace declarations will be available in {@code context} at the key {@link
   * Constants#NAMESPACE_DECLARATIONS}. The new namespace declarations will be added to any existing
   * ones already in {@code context}.
   *
   * @param reader the reader currently at the XML element with namespace declarations you want to
   *     copy
   * @param context the {@link UnmarshallingContext} that the namespace declarations will be copied
   *     to
   */
  public static void copyXmlNamespaceDeclarationsIntoContext(
      HierarchicalStreamReader reader, UnmarshallingContext context) {
    @SuppressWarnings("unchecked")
    Map<String, String> namespaces =
        (Map<String, String>) context.get(Constants.NAMESPACE_DECLARATIONS);

    if (namespaces == null) {
      namespaces = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    Iterator<String> attributeNames = reader.getAttributeNames();
    while (attributeNames.hasNext()) {
      String name = attributeNames.next();
      if (StringUtils.startsWith(name, Constants.XMLNS)) {
        String attributeValue = reader.getAttribute(name);
        namespaces.put(name, attributeValue);
      }
    }
    if (!namespaces.isEmpty()) {
      context.put(Constants.NAMESPACE_DECLARATIONS, namespaces);
    }
  }
}
