package org.codice.ditto.replication.api.impl.spring;

import java.util.Optional;
import org.codice.ditto.replication.api.impl.data.ReplicationItemImpl;
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

  Page<ReplicationItemImpl> findByFailureCountBetweenAndSourceAndDestination(
      int min, int max, String source, String destination, Pageable pageable);
}
