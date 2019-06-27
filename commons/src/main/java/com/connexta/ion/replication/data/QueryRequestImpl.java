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
package com.connexta.ion.replication.data;

import com.connexta.ion.replication.api.data.QueryRequest;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/** Simple implementation of {@link QueryRequest}. */
public class QueryRequestImpl implements QueryRequest {

  private final String cql;

  private final List<String> excludedNodes;

  private final List<String> failedItemIds;

  private final Date modifiedAfter;

  private final int startIndex;

  private final int pageSize;

  public QueryRequestImpl(String cql) {
    this(cql, Collections.emptyList());
  }

  public QueryRequestImpl(String cql, List<String> excludedNodes) {
    this(cql, excludedNodes, Collections.emptyList());
  }

  public QueryRequestImpl(String cql, int startIndex, int pageSize) {
    this(cql, Collections.emptyList(), Collections.emptyList(), null, startIndex, pageSize);
  }

  public QueryRequestImpl(String cql, List<String> excludedNodes, List<String> failedItemIds) {
    this(cql, excludedNodes, failedItemIds, null);
  }

  public QueryRequestImpl(
      String cql, List<String> excludedNodes, List<String> failedItemIds, Date modifiedAfter) {
    this(cql, excludedNodes, failedItemIds, modifiedAfter, 1, 100);
  }

  public QueryRequestImpl(
      String cql,
      List<String> excludedNodes,
      List<String> failedItemIds,
      Date modifiedAfter,
      int startIndex,
      int pageSize) {
    this.cql = cql;
    this.excludedNodes = excludedNodes;
    this.failedItemIds = failedItemIds;
    this.modifiedAfter = modifiedAfter;
    this.startIndex = startIndex;
    this.pageSize = pageSize;
  }

  @Override
  public String getCql() {
    return cql;
  }

  @Override
  public List<String> getExcludedNodes() {
    return excludedNodes;
  }

  @Override
  public List<String> getFailedItemIds() {
    return failedItemIds;
  }

  @Override
  public Date getModifiedAfter() {
    return modifiedAfter;
  }

  @Override
  public int getStartIndex() {
    return startIndex;
  }

  @Override
  public int getPageSize() {
    return pageSize;
  }
}
