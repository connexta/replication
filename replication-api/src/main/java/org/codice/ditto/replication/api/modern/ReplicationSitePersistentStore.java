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
package org.codice.ditto.replication.api.modern;

import java.util.Optional;
import java.util.Set;

/** A ReplicationSitePersistentStore performs CRUD operations for replication sites. */
public interface ReplicationSitePersistentStore {

  /**
   * Query for a site with the given ID.
   *
   * @param id the ID of the site to retrieve
   * @return an optional containing a site with the given ID or an empty optional if the site
   *     couldn't be found
   */
  Optional<ReplicationSite> getSite(String id);

  /**
   * Retrieve all the saved sites for replication.
   *
   * @return a set containing all saved sites
   */
  Set<ReplicationSite> getSites();

  /**
   * Save the given site.
   *
   * @param site the site to save
   * @return a boolean indicating whether the save was successful
   */
  ReplicationSite saveSite(ReplicationSite site);

  /**
   * Create and name a site with the given name and url.
   *
   * @param name the name of the site to save
   * @param url the url of the site to save
   * @return a boolean indicating whether the save was successful
   */
  ReplicationSite saveSite(String name, String url);

  /**
   * Attempt to update a currently saved site with the given ID, to have the given name and address.
   * The given name or url can be null to indicate no change in those values.
   *
   * @param id The ID of the site to edit
   * @param name The new name of the site. Can be null to keep the current name.
   * @param url The new url of the site. Can be null to keep the current url.
   * @return The site containing its new values
   */
  ReplicationSite editSite(String id, String name, String url);

  /**
   * Delete the site with the given ID.
   *
   * @param id the id of the site to delete
   * @return a boolean indicating whether the site was found and deleted
   */
  boolean deleteSite(String id);
}
