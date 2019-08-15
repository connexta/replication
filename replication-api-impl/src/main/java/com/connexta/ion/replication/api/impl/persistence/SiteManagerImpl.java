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
package com.connexta.ion.replication.api.impl.persistence;

import com.connexta.ion.replication.api.NotFoundException;
import com.connexta.ion.replication.api.data.ReplicationSite;
import com.connexta.ion.replication.api.impl.data.ReplicationSiteImpl;
import com.connexta.ion.replication.api.impl.spring.SiteRepository;
import com.connexta.ion.replication.api.persistence.SiteManager;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
    return siteRepository
        .findById(id)
        .orElseThrow(
            () -> new NotFoundException(String.format("Cannot find site with ID: %s", id)));
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
