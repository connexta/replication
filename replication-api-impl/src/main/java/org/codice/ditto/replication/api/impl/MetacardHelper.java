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
package org.codice.ditto.replication.api.impl;

import ddf.catalog.CatalogFramework;
import ddf.catalog.Constants;
import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.Result;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.types.Core;
import ddf.catalog.federation.FederationException;
import ddf.catalog.filter.FilterBuilder;
import ddf.catalog.filter.impl.SortByImpl;
import ddf.catalog.operation.QueryRequest;
import ddf.catalog.operation.impl.QueryImpl;
import ddf.catalog.operation.impl.QueryRequestImpl;
import ddf.catalog.source.SourceUnavailableException;
import ddf.catalog.source.UnsupportedQueryException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetacardHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(MetacardHelper.class);

  private final CatalogFramework framework;

  private final FilterBuilder filterBuilder;

  public MetacardHelper(CatalogFramework framework, FilterBuilder filterBuilder) {
    this.framework = framework;
    this.filterBuilder = filterBuilder;
  }

  public void setIfPresent(Metacard mcard, String name, Serializable value) {
    setIfPresentOrDefault(mcard, name, value, null);
  }

  public void setIfPresentOrDefault(
      Metacard mcard, String name, Serializable value, Serializable defaultValue) {
    if (value == null && defaultValue == null) {
      return;
    }
    mcard.setAttribute(new AttributeImpl(name, value == null ? defaultValue : value));
  }

  public <T> T getAttributeValueOrDefault(Metacard mcard, String attribute, T defaultValue) {
    Attribute attr = mcard.getAttribute(attribute);
    if (attr != null) {
      Object value = attr.getValue();
      if (value != null) {
        try {
          return (T) value;
        } catch (ClassCastException e) {
          return defaultValue;
        }
      }
    }
    return defaultValue;
  }

  public Metacard getMetacardById(String id) {
    if (id == null) {
      return null;
    }
    QueryRequest request =
        new QueryRequestImpl(
            new QueryImpl(
                filterBuilder.allOf(
                    filterBuilder.attribute(Core.ID).is().equalTo().text(id),
                    filterBuilder.attribute(Metacard.TAGS).is().like().text("*"))));
    try {
      List<Result> results = framework.query(request).getResults();
      if (results.size() == 1) {
        return results.get(0).getMetacard();
      }
    } catch (UnsupportedQueryException | SourceUnavailableException | FederationException e) {
      LOGGER.warn("Unable to retrieve replication metacard for {}", id, e);
    }
    return null;
  }

  public <R> List<R> getTypeForFilter(Filter filter, Function<Metacard, R> function) {
    QueryRequest request =
        new QueryRequestImpl(
            new QueryImpl(
                filter,
                1,
                Constants.DEFAULT_PAGE_SIZE,
                new SortByImpl(Core.METACARD_CREATED, SortOrder.DESCENDING),
                true,
                0L));

    try {
      List<Result> results = framework.query(request).getResults();
      return results
          .stream()
          .map(Result::getMetacard)
          .map(function)
          .filter(Objects::nonNull)
          .collect(Collectors.toList());
    } catch (UnsupportedQueryException | SourceUnavailableException | FederationException e) {
      LOGGER.warn("Unable to retrieve replication configuration metacard for {}", filter, e);
    }
    return Collections.emptyList();
  }
}
