package org.codice.ditto.replication.api.impl.persistence;

import java.util.stream.Stream;
import org.codice.ditto.replication.api.data.ReplicationSite;
import org.codice.ditto.replication.api.impl.data.ReplicationSiteImpl;
import org.codice.ditto.replication.api.persistence.SiteManager;

public class SiteManagerImpl implements SiteManager {

  private ReplicationPersistentStore persistentStore;

  public SiteManagerImpl(ReplicationPersistentStore persistentStore) {
    this.persistentStore = persistentStore;
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
    return persistentStore.get(ReplicationSiteImpl.class, id);
  }

  @Override
  public Stream<ReplicationSite> objects() {
    return persistentStore.objects(ReplicationSiteImpl.class).map(ReplicationSite.class::cast);
  }

  @Override
  public void save(ReplicationSite site) {
    if (site instanceof ReplicationSiteImpl) {
      persistentStore.save((ReplicationSiteImpl) site);
    } else {
      throw new IllegalArgumentException(
          "Expected a ReplicationSiteImpl but got a " + site.getClass().getSimpleName());
    }
  }

  @Override
  public void remove(String id) {
    persistentStore.delete(ReplicationSiteImpl.class, id);
  }
}
