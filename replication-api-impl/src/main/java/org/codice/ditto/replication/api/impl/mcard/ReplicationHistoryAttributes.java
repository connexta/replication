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
package org.codice.ditto.replication.api.impl.mcard;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.MetacardType;
import ddf.catalog.data.impl.AttributeDescriptorImpl;
import ddf.catalog.data.impl.BasicTypes;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.codice.ditto.replication.api.mcard.ReplicationConfig;
import org.codice.ditto.replication.api.mcard.ReplicationHistory;

public class ReplicationHistoryAttributes implements ReplicationHistory, MetacardType {
  private static final Set<AttributeDescriptor> DESCRIPTORS;

  private static final String NAME = "replication-history";

  static {
    Set<AttributeDescriptor> descriptors = new HashSet<>();
    descriptors.add(
        new AttributeDescriptorImpl(
            ReplicationHistory.START_TIME,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            false /* multivalued */,
            BasicTypes.DATE_TYPE));
    descriptors.add(
        new AttributeDescriptorImpl(
            ReplicationHistory.DURATION,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            false /* multivalued */,
            BasicTypes.LONG_TYPE));
    descriptors.add(
        new AttributeDescriptorImpl(
            ReplicationHistory.PULL_COUNT,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            false /* multivalued */,
            BasicTypes.LONG_TYPE));
    descriptors.add(
        new AttributeDescriptorImpl(
            ReplicationHistory.PULL_FAIL_COUNT,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            false /* multivalued */,
            BasicTypes.LONG_TYPE));
    descriptors.add(
        new AttributeDescriptorImpl(
            ReplicationHistory.PUSH_COUNT,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            false /* multivalued */,
            BasicTypes.LONG_TYPE));
    descriptors.add(
        new AttributeDescriptorImpl(
            ReplicationHistory.PUSH_FAIL_COUNT,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            false /* multivalued */,
            BasicTypes.LONG_TYPE));
    descriptors.add(
        new AttributeDescriptorImpl(
            ReplicationHistory.PULL_BYTES,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            false /* multivalued */,
            BasicTypes.LONG_TYPE));
    descriptors.add(
        new AttributeDescriptorImpl(
            ReplicationHistory.PUSH_BYTES,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            false /* multivalued */,
            BasicTypes.LONG_TYPE));
    descriptors.add(
        new AttributeDescriptorImpl(
            ReplicationHistory.STATUS,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    descriptors.add(
        new ReplicationConfigAttributes().getAttributeDescriptor(ReplicationConfig.NAME));
    DESCRIPTORS = Collections.unmodifiableSet(descriptors);
  }

  @Override
  public Set<AttributeDescriptor> getAttributeDescriptors() {
    return DESCRIPTORS;
  }

  @Override
  public AttributeDescriptor getAttributeDescriptor(String name) {
    for (AttributeDescriptor attributeDescriptor : DESCRIPTORS) {
      if (attributeDescriptor.getName().equals(name)) {
        return attributeDescriptor;
      }
    }
    return null;
  }

  @Override
  public String getName() {
    return NAME;
  }
}
