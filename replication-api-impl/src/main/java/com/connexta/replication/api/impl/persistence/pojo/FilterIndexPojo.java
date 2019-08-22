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
package com.connexta.replication.api.impl.persistence.pojo;

import com.connexta.replication.api.data.FilterIndex;
import java.time.Instant;
import java.util.Objects;
import javax.annotation.Nullable;
import org.springframework.data.solr.core.mapping.Indexed;
import org.springframework.data.solr.core.mapping.SolrDocument;

/**
 * This class provides a pojo implementation for a {@link
 * com.connexta.replication.api.data.FilterIndex} capable of reloading all supported fields for all
 * supported versions from the database. It also provides the capability of persisting back the
 * fields based on the latest version format.
 */
@SolrDocument(collection = FilterIndexPojo.COLLECTION)
public class FilterIndexPojo extends Pojo<FilterIndexPojo> {

  /**
   * List of possible versions:
   *
   * <ul>
   *   <li>1 - initial version.
   * </ul>
   */
  public static final int CURRENT_VERSION = 1;

  /** The oldest version supported by the current code (anything before that will fail). */
  public static final int MINIMUM_VERSION = 1;

  public static final String COLLECTION = "filter_index";

  @Nullable
  @Indexed(name = "modified_since", searchable = false)
  private Instant modifiedSince;

  public FilterIndexPojo() {
    super.setVersion(FilterIndexPojo.CURRENT_VERSION);
  }

  /**
   * @see FilterIndex#getModifiedSince()
   * @return the {@link Instant}, or {@code null} if no metadata has attempted to be replicated
   */
  @Nullable
  public Instant getModifiedSince() {
    return modifiedSince;
  }

  /**
   * @param modifiedSince the modified since time
   * @return this pojo
   */
  public FilterIndexPojo setModifiedSince(@Nullable Instant modifiedSince) {
    this.modifiedSince = modifiedSince;
    return this;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), modifiedSince);
  }

  @Override
  public boolean equals(Object obj) {
    if (super.equals(obj) && (obj instanceof FilterIndexPojo)) {
      final FilterIndexPojo pojo = (FilterIndexPojo) obj;

      return Objects.equals(modifiedSince, pojo.modifiedSince);
    }
    return false;
  }

  @Override
  public String toString() {
    return String.format(
        "FilterIndexPojo[id=%s, version=%d, modifiedSince=%s]",
        getId(), getVersion(), modifiedSince);
  }
}
