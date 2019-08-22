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
package com.connexta.replication.api.data;

import java.net.URL;
import java.time.Duration;
import java.util.Optional;
import java.util.OptionalInt;

/** A Site holds information about a system to be replicated to/from */
public interface Site extends Persistable {
  /**
   * Gets the human readable name of this site
   *
   * @return the site name
   */
  String getName();

  /**
   * Gets an optional description for this site.
   *
   * @return the site description or empty if none defined
   */
  Optional<String> getDescription();

  /**
   * Gets the URL of this site
   *
   * @return the site URL
   */
  URL getUrl();

  /**
   * Gets the type of this site. This type can be used to determine how to interact with the site.
   *
   * @return the type for this site
   */
  SiteType getType();

  /**
   * Gets the kind for this site. The kind can be used to determine how to best interact with the
   * site.
   *
   * @return the kind of site this is
   */
  SiteKind getKind();

  /**
   * Gets the optional amount of time to wait in between polling attempts whenever polling for intel
   * from this site.
   *
   * <p><i>Note:</i> The duration will always be representable in milliseconds as a long value.
   *
   * @return the maximum amount of time to wait in between polling attempts or empty if polling is
   *     not required or if the local configured default value should be used
   */
  Optional<Duration> getPollingPeriod();

  /**
   * Gets the parallelism factor for this site. This corresponds to the maximum number of pieces of
   * intel information that can be transferred from/to this site. For example, a tactical site might
   * want to ensure its bandwidth is not over utilized by forcing the local Ion site to
   * sequentialize all its communications with it by setting this factor to <code>1</code>.
   *
   * <p><i>Note:</i> The local Ion site cannot exceed this value if set. However it can decide to
   * ignore it and use a smaller factor based on its own requirements.
   *
   * @return the parallelism factor for this site or empty if it is up to Ion to decide
   */
  OptionalInt getParallelismFactor();
}
