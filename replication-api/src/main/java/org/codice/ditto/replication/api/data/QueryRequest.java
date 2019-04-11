package org.codice.ditto.replication.api.data;

import java.util.Date;
import java.util.List;

public interface QueryRequest {

  String getCql();

  List<String> getExcludedNodes();

  List<String> getFailedItemIds();

  Date getModifiedAfter();
}
