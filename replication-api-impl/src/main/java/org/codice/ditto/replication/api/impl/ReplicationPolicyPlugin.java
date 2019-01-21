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

import ddf.catalog.data.Metacard;
import ddf.catalog.data.Result;
import ddf.catalog.operation.Query;
import ddf.catalog.operation.ResourceRequest;
import ddf.catalog.operation.ResourceResponse;
import ddf.catalog.plugin.PolicyPlugin;
import ddf.catalog.plugin.PolicyResponse;
import ddf.catalog.plugin.StopProcessingException;
import ddf.catalog.plugin.impl.PolicyResponseImpl;
import ddf.catalog.util.impl.Requests;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.codice.ditto.replication.api.ReplicationException;
import org.codice.ditto.replication.api.mcard.ReplicationConfig;
import org.codice.ditto.replication.api.mcard.ReplicationHistory;

/**
 * This CatalogFramework PolicyPlugin restricts access to metacards associated with replication. It
 * adds a configurable policy to replication metacard operations.
 */
public class ReplicationPolicyPlugin implements PolicyPlugin {

  private Map<String, Set<String>> replicationPolicy = new HashMap<>();

  @Override
  public PolicyResponse processPreCreate(Metacard metacard, Map<String, Serializable> map)
      throws StopProcessingException {
    return getReplicationPolicy(metacard, map);
  }

  @Override
  public PolicyResponse processPreUpdate(Metacard metacard, Map<String, Serializable> map)
      throws StopProcessingException {
    return getReplicationPolicy(metacard, map);
  }

  @Override
  public PolicyResponse processPreDelete(List<Metacard> list, Map<String, Serializable> map)
      throws StopProcessingException {
    return list.stream()
        .map(metacard -> getReplicationPolicy(metacard, map))
        .filter(policy -> !policy.itemPolicy().isEmpty())
        .findFirst()
        .orElse(new PolicyResponseImpl());
  }

  @Override
  public PolicyResponse processPostDelete(Metacard metacard, Map<String, Serializable> map)
      throws StopProcessingException {
    return new PolicyResponseImpl();
  }

  @Override
  public PolicyResponse processPreQuery(Query query, Map<String, Serializable> map)
      throws StopProcessingException {
    return new PolicyResponseImpl();
  }

  @Override
  public PolicyResponse processPostQuery(Result result, Map<String, Serializable> map)
      throws StopProcessingException {
    return getReplicationPolicy(result.getMetacard(), map);
  }

  @Override
  public PolicyResponse processPreResource(ResourceRequest resourceRequest)
      throws StopProcessingException {
    return new PolicyResponseImpl();
  }

  @Override
  public PolicyResponse processPostResource(ResourceResponse resourceResponse, Metacard metacard)
      throws StopProcessingException {
    return new PolicyResponseImpl();
  }

  public void setReplicationPolicyStrings(List<String> accessPolicyStrings) {
    parsePermissionsFromString(accessPolicyStrings, replicationPolicy);
  }

  private PolicyResponse getReplicationPolicy(Metacard metacard, Map<String, Serializable> map) {
    if (Requests.isLocal(map)
        && metacard != null
        && (metacard.getTags().contains(ReplicationConfig.METACARD_TAG)
            || metacard.getTags().contains(ReplicationHistory.METACARD_TAG))) {
      return new PolicyResponseImpl(new HashMap<>(), replicationPolicy);
    }
    return new PolicyResponseImpl();
  }

  private void parsePermissionsFromString(
      List<String> permStrings, Map<String, Set<String>> policy) {
    policy.clear();
    if (permStrings != null) {
      for (String perm : permStrings) {
        String[] parts = perm.trim().split("=");
        if (parts.length != 2) {
          throw new ReplicationException("Invalid replication policy permission string: " + perm);
        }
        String attributeName = parts[0];
        String attributeValue = parts[1];
        policy.put(attributeName, Collections.singleton(attributeValue));
      }
    }
  }
}
