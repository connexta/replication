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
package org.codice.ditto.replication.api.data;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * A wrapper for metadata objects received from {@link
 * org.codice.ditto.replication.api.NodeAdapter}s.
 */
public interface Metadata {

  /**
   * A globally unique ID. This ID must be the ID of the metadata on the {@link
   * org.codice.ditto.replication.api.NodeAdapter}.
   *
   * @return the id of this {@code Metadata}
   */
  String getId();

  /**
   * The type of the metadata being wrapped.
   *
   * @return the type
   */
  Class getType();

  /**
   * The original metadata being wrapped.
   *
   * @return the wrapped metadata
   */
  Object getRawMetadata();

  /**
   * @return the URI locating the metadata, or {@code null} if there is no resource associated with
   *     this metadata
   */
  URI getResourceUri();

  /** @param resourceUri the URI locating the metadata */
  void setResourceUri(URI resourceUri);

  /** @return the size of the {@link Resource} represented by the metadata */
  long getResourceSize();

  /** @param resourceSize the size of the {@link Resource} represented by this metadata */
  void setResourceSize(long resourceSize);

  /** @return the {@link Date} this metadata was last modified */
  Date getMetadataModified();

  /**
   * @return the {@link Date} the {@link Resource} represented by this metadata was last modified,
   *     or {@code null} if there is no resource associated with this metadata
   */
  Date getResourceModified();

  /**
   * @param resourceModified the {@link Date} the {@link Resource} represented by this metadata was
   *     last modified
   */
  void setResourceModified(Date resourceModified);

  /** @return a set of tags associated with the wrapped metadata */
  Set<String> getTags();

  /**
   * Adds a new tag to the set of existing tags associated with the wrapped metadata.
   *
   * @param tag the tag to add
   */
  void addTag(String tag);

  /**
   * The lineage of this metadata. For example, if metadata went from systems A to B to C, then the
   * metadata's lineage should be a list containing ["A", "B" , "C"].
   *
   * @return this metadata's lineage
   */
  List<String> getLineage();

  /**
   * Adds a new system name to the lineage.
   *
   * @param name the name to add.
   */
  void addLineage(String name);

  /** @return {@code true} if this metadata should be considered deleted, otherwise {@code false} */
  boolean isDeleted();

  /** @param isDeleted whether or not this metadata should be considered deleted */
  void setIsDeleted(boolean isDeleted);

  /**
   * Get the source of this metadata.
   *
   * @return the source of this metadata.
   */
  String getSource();

  /**
   * Set the source of this metadata.
   *
   * @param source the source of this metadata.
   */
  void setSource(String source);
}
