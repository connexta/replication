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
package com.connexta.replication.api.impl.persistence;

import com.connexta.replication.api.data.Site;

/** A SiteManager performs CRUD operations for replication sites. */
public interface SiteManager extends DataManager<Site> {

  /**
   * Creates a new {@link Site} implementation with the given name and url
   *
   * @param name the name to give the {@link Site}
   * @param url the for the {@link Site}
   * @return a new {@link Site} implementation
   */
  Site create(String name, String url);
}