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
package org.codice.ditto.replication.api.impl.data;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import javax.annotation.Nullable;
import org.apache.commons.lang.StringUtils;
import org.codice.ditto.replication.api.data.ReplicationSite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ReplicationSiteImpl represents a replication site and has methods that allow it to easily be
 * converted to, or from, a map.
 */
@SuppressWarnings(
    "squid:S2160" /* equals is only based on id and type which is handled in base class */)
public class ReplicationSiteImpl extends AbstractPersistable implements ReplicationSite {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReplicationSiteImpl.class);

  // public so that the persistent store can access it using reflection
  public static final String PERSISTENCE_TYPE = "replication_site";

  private static final String NAME_KEY = "name";

  private static final String URL_KEY = "url";

  private static final String VERIFIED_URL_KEY = "verified-url";

  private static final String IS_REMOTE_MANAGED_KEY = "is-remote-managed";

  /**
   * List of possible versions:
   *
   * <ul>
   *   <li>1 - initial version.
   *   <li>2 - Adds
   *       <ul>
   *         <li>is-remote-managed field of type boolean, defaults to false
   *         <li>
   *         <li>verified-url, defaults to url
   *       </ul>
   * </ul>
   */
  public static final int CURRENT_VERSION = 2;

  private boolean isRemoteManaged = false;

  private String name;

  /** This is the configured URL for the site. */
  private String url;

  /**
   * The verified url corresponds to the URL that should be used to replicate to/from or send a
   * heartbeat to this site. It will typically be the same as the configured url above but should be
   * changed if permanent redirects are received.
   */
  @Nullable private String verifiedUrl = null;

  public ReplicationSiteImpl() {
    super.setVersion(CURRENT_VERSION);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getUrl() {
    return url;
  }

  @Override
  public void setUrl(String url) {
    this.url = url;
    this.verifiedUrl = null; // make sure to clear the verified url since we are changing the url
  }

  @Nullable
  @Override
  public String getVerifiedUrl() {
    return verifiedUrl;
  }

  @Override
  public void setVerifiedUrl(@Nullable String url) {
    this.verifiedUrl = url;
  }

  @Override
  public void setRemoteManaged(boolean remoteManaged) {
    this.isRemoteManaged = remoteManaged;
  }

  @Override
  public boolean isRemoteManaged() {
    return isRemoteManaged;
  }

  @Override
  public Map<String, Object> toMap() {
    Map<String, Object> result = super.toMap();
    result.put(NAME_KEY, getName());
    result.put(URL_KEY, getUrl());
    final String vurl = getVerifiedUrl();

    if (vurl != null) {
      result.put(VERIFIED_URL_KEY, vurl);
    }
    result.put(IS_REMOTE_MANAGED_KEY, isRemoteManaged());
    return result;
  }

  @Override
  public void fromMap(Map<String, Object> properties) {
    super.fromMap(properties);
    final int version = super.getVersion();

    if (version == ReplicationSiteImpl.CURRENT_VERSION) {
      fromCurrentMap(properties);
    } else {
      fromIncompatibleMap(version, properties);
    }
  }

  private void fromCurrentMap(Map<String, Object> properties) {
    setName((String) properties.get(NAME_KEY));
    setUrl((String) properties.get(URL_KEY));
    setVerifiedUrl((String) properties.get(VERIFIED_URL_KEY)); // do this after setUrl()
    setRemoteManaged(Boolean.parseBoolean((String) properties.get(IS_REMOTE_MANAGED_KEY)));
  }

  private void fromIncompatibleMap(int version, Map<String, Object> properties) {
    switch (version) {
      case 1:
        fromVersionOneMap(properties);
        break;
      default:
        LOGGER.error("unsupported {} version: {}", ReplicationSiteImpl.PERSISTENCE_TYPE, version);
        throw new IllegalStateException("Unsupported version: " + version);
    }
    super.setVersion(CURRENT_VERSION);
  }

  private void fromVersionOneMap(Map<String, Object> properties) {
    setName((String) properties.get(NAME_KEY));

    String oldUrl = (String) properties.get(URL_KEY);
    try {
      URL url = new URL(oldUrl);
      if (StringUtils.isEmpty(url.getPath())) {
        setUrl(oldUrl + "/services");
      } else {
        // should never hit this since old urls do not have context paths
        setUrl(oldUrl);
      }
    } catch (MalformedURLException e) {
      // saved URLs will not have invalid URLs
    }

    setVerifiedUrl(getUrl()); // do this after setUrl()
    setRemoteManaged(false);
  }
}
