package org.codice.ditto.replication.api.impl.persistence;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.codice.ditto.replication.api.NotFoundException;
import org.codice.ditto.replication.api.data.ReplicationSite;
import org.codice.ditto.replication.api.impl.data.ReplicationSiteImpl;
import org.codice.ditto.replication.api.impl.spring.SiteRepository;
import org.codice.ditto.replication.api.persistence.SiteManager;

public class SiteManagerImpl implements SiteManager {

  private SiteRepository siteRepository;

  public SiteManagerImpl(SiteRepository siteRepository) {
    this.siteRepository = siteRepository;
  }

  @Override
  public ReplicationSite create() {
    return new ReplicationSiteImpl();
  }

  @Override
  public ReplicationSite createSite(String name, String url) {
    ReplicationSite site = new ReplicationSiteImpl();
    site.setName(name);
    site.setUrl(url);
    return site;
  }

  @Override
  public ReplicationSite get(String id) {
    return siteRepository.findById(id).orElseThrow(NotFoundException::new);
  }

  @Override
  public Stream<ReplicationSite> objects() {
    return StreamSupport.stream(siteRepository.findAll().spliterator(), false)
        .map(ReplicationSite.class::cast);
  }

  @Override
  public void save(ReplicationSite site) {
    if (site instanceof ReplicationSiteImpl) {
      siteRepository.save((ReplicationSiteImpl) site);
    } else {
      throw new IllegalArgumentException(
          "Expected a ReplicationSiteImpl but got a " + site.getClass().getSimpleName());
    }
  }

  @Override
  public void remove(String id) {
    siteRepository.deleteById(id);
  }
}
