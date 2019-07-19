package com.connexta.ion.replication.api.impl.spring;

import com.connexta.ion.replication.api.impl.data.ReplicationItemImpl;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

// This will be AUTO IMPLEMENTED by Spring into a Bean called itemRepository
public interface ItemRepository extends PagingAndSortingRepository<ReplicationItemImpl, String> {

  Optional<ReplicationItemImpl> findByIdAndSourceAndDestination(
      String id, String source, String destination);

  Page<ReplicationItemImpl> findByConfigId(String configId, Pageable pageable);

  void deleteByIdAndSourceAndDestination(String id, String source, String destination);

  void deleteByConfigId(String configId);

  //  Page<ReplicationItemImpl> findByConfigIdAndStatus(
  //      String configId, Status status, Pageable pageable);
}
