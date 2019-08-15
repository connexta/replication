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
package com.connexta.replication.api.impl.data;

import com.connexta.ion.replication.api.NotFoundException;
import com.connexta.replication.api.data.Site;
import com.connexta.replication.api.impl.persistence.SiteManager;
import com.connexta.replication.api.impl.persistence.pojo.SitePojo;
import com.connexta.replication.api.impl.persistence.spring.SiteRepository;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class SiteManagerImpl implements SiteManager {

  private SiteRepository siteRepository;

  public SiteManagerImpl(SiteRepository siteRepository) {
    this.siteRepository = siteRepository;
  }

  @Override
  public Site create() {
    return new SiteImpl();
  }

  @Override
  public Site create(String name, String url) {
    SiteImpl site = new SiteImpl();
    site.setName(name);
    site.setUrl(url);
    return site;
  }

  @Override
  public Site get(String id) {
    return siteRepository
        .findById(id)
        .map(SiteImpl::new)
        .orElseThrow(
            () -> new NotFoundException(String.format("Cannot find site with ID: %s", id)));
  }

  @Override
  public Stream<Site> objects() {
    return StreamSupport.stream(siteRepository.findAll().spliterator(), false)
        .map(SiteImpl::new)
        .map(Site.class::cast);
  }

  @Override
  public void save(Site site) {
    if (site instanceof SiteImpl) {
      siteRepository.save(((SiteImpl) site).writeTo(new SitePojo()));
    } else {
      throw new IllegalArgumentException(
          "Expected a SiteImpl but got a " + site.getClass().getSimpleName());
    }
  }

  @Override
  public void remove(String id) {
    siteRepository.deleteById(id);
  }
}
