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

import com.connexta.replication.api.data.NonTransientReplicationPersistenceException;
import com.connexta.replication.api.data.NotFoundException;
import com.connexta.replication.api.data.RecoverableReplicationPersistenceException;
import com.connexta.replication.api.data.Site;
import com.connexta.replication.api.data.TransientReplicationPersistenceException;
import com.connexta.replication.api.impl.persistence.pojo.SitePojo;
import com.connexta.replication.api.impl.persistence.spring.SiteRepository;
import com.connexta.replication.api.persistence.SiteManager;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.TransientDataAccessException;

/** Provides an implementation for the site manager. */
public class SiteManagerImpl implements SiteManager {
  private final SiteRepository siteRepository;

  /**
   * Instantiates a new site manager with the given repository.
   *
   * @param siteRepository the repository to use for persistence
   */
  public SiteManagerImpl(SiteRepository siteRepository) {
    this.siteRepository = siteRepository;
  }

  @Override
  public Site get(String id) {
    try {
      return siteRepository
          .findById(id)
          .map(SiteImpl::new)
          .orElseThrow(
              () -> new NotFoundException(String.format("Cannot find site with ID: %s", id)));
    } catch (NonTransientDataAccessException e) {
      throw new NonTransientReplicationPersistenceException(e);
    } catch (TransientDataAccessException e) {
      throw new TransientReplicationPersistenceException(e);
    } catch (RecoverableDataAccessException e) {
      throw new RecoverableReplicationPersistenceException(e);
    }
  }

  @Override
  public Stream<Site> objects() {
    try {
      return StreamSupport.stream(siteRepository.findAll().spliterator(), false)
          .map(SiteImpl::new)
          .map(Site.class::cast);
    } catch (NonTransientDataAccessException e) {
      throw new NonTransientReplicationPersistenceException(e);
    } catch (TransientDataAccessException e) {
      throw new TransientReplicationPersistenceException(e);
    } catch (RecoverableDataAccessException e) {
      throw new RecoverableReplicationPersistenceException(e);
    }
  }

  @Override
  public void save(Site site) {
    if (!(site instanceof SiteImpl)) {
      throw new IllegalArgumentException(
          "Expected a SiteImpl but got a " + site.getClass().getSimpleName());
    }
    try {
      siteRepository.save(((SiteImpl) site).writeTo(new SitePojo()));
    } catch (NonTransientDataAccessException e) {
      throw new NonTransientReplicationPersistenceException(e);
    } catch (TransientDataAccessException e) {
      throw new TransientReplicationPersistenceException(e);
    } catch (RecoverableDataAccessException e) {
      throw new RecoverableReplicationPersistenceException(e);
    }
  }

  @Override
  public void remove(String id) {
    try {
      siteRepository.deleteById(id);
    } catch (NonTransientDataAccessException e) {
      throw new NonTransientReplicationPersistenceException(e);
    } catch (TransientDataAccessException e) {
      throw new TransientReplicationPersistenceException(e);
    } catch (RecoverableDataAccessException e) {
      throw new RecoverableReplicationPersistenceException(e);
    }
  }
}
