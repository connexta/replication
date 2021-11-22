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
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import javax.annotation.Nullable;
import org.codice.ditto.replication.api.AdapterException;
import org.codice.ditto.replication.api.data.Metadata;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copied from DDF and modified for replication purposes.
 *
 * <p>Converts CSW Record to a Metadata.
 */
public class CswMinimalRecordConverter implements Converter {

  private static final Logger LOGGER = LoggerFactory.getLogger(CswMinimalRecordConverter.class);

  @Override
  public boolean canConvert(Class clazz) {
    return Metadata.class.isAssignableFrom(clazz);
  }

  @Override
  public void marshal(
      Object o,
      HierarchicalStreamWriter hierarchicalStreamWriter,
      MarshallingContext marshallingContext) {
    throw new UnsupportedOperationException(
        "Marshaling not supported by CswMinimalRecordConverter");
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {

    return createMetadataFromCswRecord(reader);
  }

  public static @Nullable Date convertToDate(String value) {
    if (value == null) {
      return null;
    }
    /* Dates are strings and expected to be in ISO8601 format, YYYY-MM-DD'T'hh:mm:ss.sss,
    per annotations in the CSW Record schema. At least the date portion must be present;
    the time zone and time are optional.*/
    try {
      return ISODateTimeFormat.dateOptionalTimeParser().parseDateTime(value).toDate();
    } catch (IllegalArgumentException e) {
      LOGGER.debug("Failed to convert to date {} from ISO Format: {}", value, e);
    }

    // try from java date serialization for the default locale
    try {
      return DateFormat.getDateInstance().parse(value);
    } catch (ParseException e) {
      LOGGER.debug("Unable to convert date {} from default locale format {} ", value, e);
    }

    // default to current date
    LOGGER.debug("Unable to convert {} to a date object", value);
    return null;
  }

  public static Metadata createMetadataFromCswRecord(HierarchicalStreamReader reader) {
    String id = reader.getAttribute("id");
    String metadataModified = reader.getAttribute("metadataDate");
    String resourceModified = reader.getAttribute("resourceDate");
    if (metadataModified == null || id == null) {
      throw new AdapterException(
          "Can't convert csw minimal metadata without a modified/metacard.modified and id field");
    }
    Date metacardModified = convertToDate(metadataModified);
    MetadataImpl metadata = new MetadataImpl("", String.class, id, metacardModified);
    if (resourceModified != null) {
      metadata.setResourceModified(convertToDate(resourceModified));
    }
    return metadata;
  }
}
