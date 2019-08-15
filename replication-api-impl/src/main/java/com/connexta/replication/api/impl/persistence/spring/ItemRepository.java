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
package com.connexta.replication.api.impl.persistence.spring;

import com.connexta.ion.replication.api.Status;
import com.connexta.replication.api.impl.persistence.pojo.ReplicationItemPojo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

// This will be AUTO IMPLEMENTED by Spring into a Bean called itemRepository
public interface ItemRepository extends PagingAndSortingRepository<ReplicationItemPojo, String> {

  Page<ReplicationItemPojo> findByConfigIdAndMetadataIdOrderByDoneTimeDesc(
      String configId, String metadataId, Pageable pageable);

  Page<ReplicationItemPojo> findByConfigId(String configId, Pageable pageable);

  void deleteByIdAndSourceAndDestination(String id, String source, String destination);

  void deleteByConfigId(String configId);

  Page<ReplicationItemPojo> findByConfigIdAndStatus(
      String configId, Status status, Pageable pageable);
}
