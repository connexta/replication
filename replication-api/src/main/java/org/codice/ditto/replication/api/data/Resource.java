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

import java.io.InputStream;
import java.net.URI;
import javax.annotation.Nullable;

public interface Resource {

  /**
   * A globally unique ID.
   *
   * @return the id
   */
  String getId();

  /** @return the human-readable name of this resource */
  String getName();

  /** @return the location of this resource */
  URI getResourceUri();

  /** @return the qualifier of this resource, or null if there isn't one */
  @Nullable
  String getQualifier();

  /** @return the input stream of the resource */
  InputStream getInputStream();

  /** @return the mime type of the input stream */
  String getMimeType();

  /** @return the resource size, otherwise -1 if unknown */
  long getSize();

  /** @return the {@link Metadata} associated with this resource */
  Metadata getMetadata();
}
