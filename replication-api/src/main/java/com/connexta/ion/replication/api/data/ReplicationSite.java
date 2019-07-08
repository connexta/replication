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
package com.connexta.ion.replication.api.data;

import com.connexta.ion.replication.api.NodeAdapterType;

/** A ReplicationSite holds information about a system to be replicated to/from */
public interface ReplicationSite extends Persistable {

  /**
   * Get the human readable name of this site
   *
   * @return site name
   */
  String getName();

  /**
   * Set the name of this site
   *
   * @param name the name to give this site
   */
  void setName(String name);

  /**
   * Get the URL of this site
   *
   * @return site URL
   */
  String getUrl();

  /**
   * Set the URL of this site
   *
   * @param url the URL to give this site
   */
  void setUrl(String url);

  /**
   * See {@link #isRemoteManaged()}.
   *
   * @param remoteManaged whether or not the local process is responsible for running replications
   *     this site is associated with.
   */
  void setRemoteManaged(boolean remoteManaged);

  /**
   * When {@code false}, the local process is responsible for running {@link ReplicatorConfig}s
   * associated with this site.
   *
   * <p>When {@code true}, the local process is no longer responsible for performing replication for
   * any {@link ReplicatorConfig}s associated with this site. This effectively disables running
   * replication locally.
   *
   * @return {@code true} if replication should not run locally, otherwise {@code false}.
   */
  boolean isRemoteManaged();

  /**
   * Get the type of this site as defined in {@link NodeAdapterType}. This type can be used to
   * determine how to interact with the site.
   *
   * @return {@link NodeAdapterType}
   */
  String getType();

  /**
   * Sets the {@link NodeAdapterType} of this site.
   *
   * @param type the {@link NodeAdapterType}
   */
  void setType(String type);
}
