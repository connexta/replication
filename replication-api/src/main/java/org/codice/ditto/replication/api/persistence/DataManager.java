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
package org.codice.ditto.replication.api.persistence;

import java.util.stream.Stream;
import org.codice.ditto.replication.api.ReplicationPersistenceException;
import org.codice.ditto.replication.api.data.Persistable;

/**
 * The DataManager interface provides a common interface for the manager classes which manage the
 * creating, saving, retrieving, and deleting of data.
 */
public interface DataManager<T extends Persistable> {

  /**
   * Creates a new implementation of T
   *
   * @return a new implementation of T
   */
  T create();

  /**
   * Get the object of type T associated with the given id
   *
   * @param id The object id
   * @return the T object with the given id
   * @throws ReplicationPersistenceException if an error occurs while trying to retrieve the object
   * @throws NotFoundException if an object with the given id cannot be found
   * @throws IllegalStateException if multiple objects were found with the given id
   */
  T get(String id);

  /**
   * Gets all the currently saved T objects
   *
   * @return Stream of all T objects
   * @throws ReplicationPersistenceException if an error occurs while trying to retrieve the objects
   */
  Stream<T> objects();

  /**
   * Overwrites an existing T object with the same id if there is one. Otherwise, saves the new T
   * object. To ensure that the given object can be saved, it should be created using the create
   * method included in this interface.
   *
   * @param object The T object to save or update
   * @throws ReplicationPersistenceException if an error occurs while trying to save the object
   * @throws IllegalArgumentException if the T implementation is not one that can be saved
   */
  void save(T object);

  /**
   * Deletes a T object with the given id
   *
   * @param id The id of the object to be removed
   * @throws ReplicationPersistenceException if an error occurs while trying to delete the object
   * @throws NotFoundException if an object with the given id cannot be found
   */
  void remove(String id);

  /**
   * @param id unique id of the {@link Persistable}
   * @return {@code true} if the {@link Persistable} exists, otherwise {@code false}.
   * @throws ReplicationPersistenceException if there is an error accessing storage
   */
  boolean exists(String id);
}
