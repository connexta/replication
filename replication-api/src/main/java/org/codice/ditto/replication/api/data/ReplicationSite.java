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

import javax.annotation.Nullable;

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
   * Set the URL of this site while clearing the verified URL.
   *
   * @param url the URL to give this site
   */
  void setUrl(String url);

  /**
   * Get the verified URL of this site. This should be the url to use to communicate with the site
   * when replicating. This URL will be <code>null</code> until the URL reported by {@link
   * #getUrl()} has been verified at which point the verified URL should be updated. It is also
   * possible that this URL be changed later in cases where the site permanently redirected us to a
   * new location at which point this new location should be saved as the new verified URL until
   * such time when the URL is changed via {@link #setUrl(String)} which will automatically clear
   * the verified URL.
   *
   * @return the verified site URL
   */
  @Nullable
  String getVerifiedUrl();

  /**
   * Set the verified URL of this site.
   *
   * @param url the verified URL to give this site or <code>null</code> to clear the URL
   */
  void setVerifiedUrl(@Nullable String url);

  /**
   * See {@link #isRemoteManaged()}.
   *
   * @param remoteManaged whether or not the local process is responsible for running replications
   *     this site is associated with.
   */
  void setRemoteManaged(boolean remoteManaged);

  /**
   * When {@link false}, the local process is responsible for running {@link
   * org.codice.ditto.replication.api.mcard.ReplicationConfig}s associated with this site.
   *
   * <p>When {@link true}, the local process is no longer responsible for performing replication for
   * any {@link org.codice.ditto.replication.api.mcard.ReplicationConfig}s associated with this
   * site. This effectively disables running replication locally.
   *
   * @return {@code true} if replication should not run locally, otherwise {@link false}.
   */
  boolean isRemoteManaged();
}
