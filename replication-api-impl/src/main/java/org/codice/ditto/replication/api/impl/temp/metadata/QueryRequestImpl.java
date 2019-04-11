package org.codice.ditto.replication.api.impl.temp.metadata;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.codice.ditto.replication.api.temp.metadata.QueryRequest;

public class QueryRequestImpl implements QueryRequest {

  private final String cql;

  private final List<String> excludedNodes;

  private final List<String> failedItemIds;

  private final Date modifiedAfter;

  public QueryRequestImpl(String cql) {
    this(cql, Collections.emptyList());
  }

  public QueryRequestImpl(String cql, List<String> excludedNodes) {
    this(cql, excludedNodes, Collections.emptyList());
  }

  public QueryRequestImpl(String cql, List<String> excludedNodes, List<String> failedItemIds) {
    this(cql, excludedNodes, failedItemIds, null);
  }

  public QueryRequestImpl(
      String cql, List<String> excludedNodes, List<String> failedItemIds, Date modifiedAfter) {
    this.cql = cql;
    this.excludedNodes = excludedNodes;
    this.failedItemIds = failedItemIds;
    this.modifiedAfter = modifiedAfter;
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
}
