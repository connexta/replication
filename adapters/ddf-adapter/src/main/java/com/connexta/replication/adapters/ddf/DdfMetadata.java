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
package com.connexta.replication.adapters.ddf;

import static org.apache.commons.lang3.Validate.notNull;

import com.connexta.replication.data.MetadataImpl;
import java.util.Date;
import java.util.Map;

/**
 * Metacard metadata implementation of {@link com.connexta.replication.api.data.Metadata} which adds
 * support for the metacard attributes
 */
public class DdfMetadata extends MetadataImpl {
  private Map<String, MetacardAttribute> attributes;
  /**
   * @param metadata the raw metadata to wrap, cannot be null
   * @param type the type of the metadata, cannot be null
   * @param id id the of the metadata, cannot be null or empty
   * @param metadataModified the last time the metadata was modified, cannot be null
   * @param attributes the DDF metacard attribute map, cannot be null
   */
  public DdfMetadata(
      Object metadata,
      Class type,
      String id,
      Date metadataModified,
      Map<String, MetacardAttribute> attributes) {
    super(metadata, type, id, metadataModified);
    this.attributes = notNull(attributes);
  }

  /**
   * Returns the metacard attributes map
   *
   * @return the map of metacard attributes
   */
  public Map<String, MetacardAttribute> getAttributes() {
    return attributes;
  }
}
