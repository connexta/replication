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
package com.connexta.replication.api.impl.data;

import com.connexta.replication.api.data.Site;
import com.connexta.replication.api.impl.persistence.pojo.SitePojo;
import com.google.common.annotations.VisibleForTesting;

/**
 * SiteImpl represents a replication site and has methods that allow it to easily be converted to,
 * or from, a map.
 */
public class SiteImpl extends AbstractPersistable<SitePojo> implements Site {
  private static final String TYPE = "replication site";

  private boolean isRemoteManaged = false;

  private String name;

  private String url;

  private String type;

  public SiteImpl() {
    super(SiteImpl.TYPE);
  }

  protected SiteImpl(SitePojo pojo) {
    super(SiteImpl.TYPE);
    readFrom(pojo);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getUrl() {
    return url;
  }

  @Override
  public void setUrl(String url) {
    this.url = url;
  }

  @Override
  public boolean isRemoteManaged() {
    return isRemoteManaged;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public void setType(String type) {
    this.type = type;
  }

  @Override
  public SitePojo writeTo(SitePojo pojo) {
    super.writeTo(pojo);
    return pojo.setName(name).setUrl(url).setRemoteManaged(isRemoteManaged).setType(type);
  }

  @Override
  public void readFrom(SitePojo pojo) {
    super.readFrom(pojo);
    setOrFailIfNullOrEmpty("name", pojo::getName, this::setName);
    setOrFailIfNullOrEmpty("url", pojo::getUrl, this::setUrl);
    this.type = pojo.getType();
    this.isRemoteManaged = pojo.isRemoteManaged();
  }

  @VisibleForTesting
  void setName(String name) {
    this.name = name;
  }

  @VisibleForTesting
  void setRemoteManaged(boolean remoteManaged) {
    this.isRemoteManaged = remoteManaged;
  }
}
