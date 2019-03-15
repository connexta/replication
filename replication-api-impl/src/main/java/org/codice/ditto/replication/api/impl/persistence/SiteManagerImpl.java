package org.codice.ditto.replication.api.impl.persistence;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import javax.ws.rs.NotFoundException;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.codice.ddf.configuration.SystemBaseUrl;
import org.codice.ddf.configuration.SystemInfo;
import org.codice.ditto.replication.api.ReplicationPersistenceException;
import org.codice.ditto.replication.api.data.ReplicationSite;
import org.codice.ditto.replication.api.impl.data.ReplicationSiteImpl;
import org.codice.ditto.replication.api.persistence.SiteManager;

public class SiteManagerImpl implements SiteManager {

  private ReplicationPersistentStore persistentStore;

  private static final String LOCAL_SITE_ID = "local-site-id-1234567890";

  public SiteManagerImpl(ReplicationPersistentStore persistentStore) {
    this.persistentStore = persistentStore;
  }

  public void init() {
    RetryPolicy retryPolicy =
        new RetryPolicy()
            .withDelay(5, TimeUnit.SECONDS)
            .withMaxDuration(5, TimeUnit.MINUTES)
            .retryOn(ReplicationPersistenceException.class);

    Failsafe.with(retryPolicy).run(this::createLocalSite);
  }

  private void createLocalSite() {
    if (!localSiteExistsAndIsCorrect()) {
      ReplicationSite site = new ReplicationSiteImpl();
      site.setId(LOCAL_SITE_ID);
      site.setName(SystemInfo.getSiteName());
      site.setUrl(SystemBaseUrl.EXTERNAL.getBaseUrl());
      save(site);
    }
  }

  private boolean localSiteExistsAndIsCorrect() {
    ReplicationSite site;

    try {
      site = get(LOCAL_SITE_ID);
    } catch (NotFoundException e) {
      return false;
    }

    return site.getName().equals(SystemInfo.getSiteName())
        && site.getUrl().equals(SystemBaseUrl.EXTERNAL.getBaseUrl());
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
