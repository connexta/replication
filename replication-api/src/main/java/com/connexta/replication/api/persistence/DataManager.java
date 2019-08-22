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
package com.connexta.replication.api.persistence;

import com.connexta.replication.api.data.Persistable;
import java.util.stream.Stream;

/**
 * The DataManager interface provides a common interface for the manager classes which manage the
 * creating, saving, retrieving, and deleting of data.
 */
public interface DataManager<T extends Persistable> {

  /**
   * Get the object of type T associated with the given id
   *
   * @param id The object id
   * @return the T object with the given id
   * @throws com.connexta.ion.replication.api.NotFoundException if an object with the given id
   *     cannot be found
   * @throws com.connexta.ion.replication.api.ReplicationPersistenceException if any other error
   *     occurs while trying to retrieve the object
   * @throws IllegalStateException if multiple objects were found with the given id
   */
  T get(String id);

  /**
   * Gets all the currently saved T objects
   *
   * @return Stream of all T objects
   * @throws com.connexta.ion.replication.api.ReplicationPersistenceException if an error occurs
   *     while trying to retrieve the objects
   */
  Stream<T> objects();

  /**
   * Overwrites an existing T object with the same id if there is one. Otherwise, saves the new T
   * object. To ensure that the given object can be saved, it should be created using the create
   * method included in this interface.
   *
   * @param object The T object to save or update
   * @throws IllegalArgumentException if the T implementation is not one that can be saved
   * @throws com.connexta.ion.replication.api.ReplicationPersistenceException if an error occurs
   *     while trying to save the object
   */
  void save(T object);

  /**
   * Deletes a T object with the given id
   *
   * @param id The id of the object to be removed
   * @throws com.connexta.ion.replication.api.NotFoundException if an object with the given id
   *     cannot be found
   * @throws com.connexta.ion.replication.api.ReplicationPersistenceException if any other error
   *     occurs while trying to delete the object
   */
  void remove(String id);
}
