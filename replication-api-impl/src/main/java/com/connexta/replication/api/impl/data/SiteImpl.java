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

import com.connexta.ion.replication.api.InvalidFieldException;
import com.connexta.ion.replication.api.UnsupportedVersionException;
import com.connexta.replication.api.data.Site;
import com.connexta.replication.api.data.SiteKind;
import com.connexta.replication.api.data.SiteType;
import com.connexta.replication.api.impl.persistence.pojo.SitePojo;
import com.google.common.annotations.VisibleForTesting;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import javax.annotation.Nullable;

/**
 * SiteImpl represents a replication site and has methods that allow it to easily be converted to,
 * or from, a map.
 */
public class SiteImpl extends AbstractPersistable<SitePojo> implements Site {
  private static final String PERSISTABLE_TYPE = "replication site";

  private String name;

  @Nullable private String description;

  private URL url;

  private SiteType type = SiteType.UNKNOWN;

  private SiteKind kind = SiteKind.UNKNOWN;

  @Nullable private Duration pollingPeriod = null;

  private int parallelismFactor = 0;

  public SiteImpl() {
    super(SiteImpl.PERSISTABLE_TYPE);
  }

  protected SiteImpl(SitePojo pojo) {
    super(SiteImpl.PERSISTABLE_TYPE, null);
    readFrom(pojo);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }

  @Override
  public URL getUrl() {
    return url;
  }

  @Override
  public SiteType getType() {
    return type;
  }

  @Override
  public SiteKind getKind() {
    return kind;
  }

  @Override
  public Optional<Duration> getPollingPeriod() {
    return Optional.ofNullable(pollingPeriod);
  }

  @Override
  public OptionalInt getParallelismFactor() {
    return (parallelismFactor <= 0) ? OptionalInt.empty() : OptionalInt.of(parallelismFactor);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        super.hashCode(), name, description, url, type, kind, pollingPeriod, parallelismFactor);
  }

  @Override
  public boolean equals(Object obj) {
    if (super.equals(obj) && (obj instanceof SiteImpl)) {
      final SiteImpl persistable = (SiteImpl) obj;

      return (parallelismFactor == persistable.parallelismFactor)
          && Objects.equals(name, persistable.name)
          && Objects.equals(description, persistable.description)
          && Objects.equals(url, persistable.url)
          && Objects.equals(type, persistable.type)
          && Objects.equals(kind, persistable.kind)
          && Objects.equals(pollingPeriod, persistable.pollingPeriod);
    }
    return false;
  }

  @Override
  public String toString() {
    return String.format(
        "SiteImpl[id=%s, name=%s, url=%s, type=%s, kind=%s, pollingPeriod=%s, parallelismFactor=%d, description=%s]",
        getId(), name, url, type, kind, pollingPeriod, parallelismFactor, description);
  }

  @Override
  protected SitePojo writeTo(SitePojo pojo) {
    super.writeTo(pojo);
    setOrFailIfNullOrEmpty("name", this::getName, pojo::setName);
    convertAndSetOrFailIfNull("url", this::getUrl, URL::toString, pojo::setUrl);
    convertAndSetOrFailIfNull("type", this::getType, SiteType::name, pojo::setType);
    convertAndSetOrFailIfNull("kind", this::getKind, SiteKind::name, pojo::setKind);
    return pojo.setVersion(SitePojo.CURRENT_VERSION)
        .setDescription(description)
        .setPollingPeriod(SiteImpl.toMillis(pollingPeriod))
        .setParallelismFactor(Math.max(0, parallelismFactor));
  }

  @Override
  protected void readFrom(SitePojo pojo) {
    super.readFrom(pojo);
    if (pojo.getVersion() < SitePojo.MINIMUM_VERSION) {
      throw new UnsupportedVersionException(
          "unsupported "
              + SiteImpl.PERSISTABLE_TYPE
              + " version: "
              + pojo.getVersion()
              + " for object: "
              + getId());
    } // do support pojo.getVersion() > CURRENT_VERSION for forward compatibility
    readFromCurrentOrFutureVersion(pojo);
  }

  @VisibleForTesting
  void setName(String name) {
    this.name = name;
  }

  @VisibleForTesting
  void setDescription(@Nullable String description) {
    this.description = description;
  }

  @VisibleForTesting
  void setUrl(URL url) {
    this.url = url;
  }

  @VisibleForTesting
  void setType(SiteType type) {
    this.type = type;
  }

  @VisibleForTesting
  void setKind(SiteKind kind) {
    this.kind = kind;
  }

  @VisibleForTesting
  void setPollingPeriod(@Nullable Duration pollingPeriod) {
    this.pollingPeriod = pollingPeriod;
  }

  @VisibleForTesting
  void setParallelismFactor(int parallelismFactor) {
    this.parallelismFactor = parallelismFactor;
  }

  private void readFromCurrentOrFutureVersion(SitePojo pojo) {
    setOrFailIfNullOrEmpty("name", pojo::getName, this::setName);
    convertAndSetOrFailIfNullOrEmpty("url", pojo::getUrl, SiteImpl::toUrl, this::setUrl);
    convertAndSetEnumValueOrFailIfNullOrEmpty(
        "type", SiteType.class, SiteType.UNKNOWN, pojo::getType, this::setType);
    convertAndSetEnumValueOrFailIfNullOrEmpty(
        "kind", SiteKind.class, SiteKind.UNKNOWN, pojo::getKind, this::setKind);
    this.description = pojo.getDescription();
    this.pollingPeriod =
        (pojo.getPollingPeriod() > 0) ? Duration.ofMillis(pojo.getPollingPeriod()) : null;
    this.parallelismFactor = Math.max(0, pojo.getParallelismFactor());
  }

  private static URL toUrl(String url) {
    try {
      return new URL(url);
    } catch (MalformedURLException e) {
      throw new InvalidFieldException("invalid url: " + url, e);
    }
  }

  private static long toMillis(@Nullable Duration pollingPeriod) {
    try {
      return (pollingPeriod != null) ? pollingPeriod.toMillis() : 0L;
    } catch (ArithmeticException e) {
      throw new InvalidFieldException("invalid polling period: " + pollingPeriod, e);
    }
  }
}
