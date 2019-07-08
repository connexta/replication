package com.connexta.ion.replication.api.persistence;

import com.connexta.ion.replication.api.data.Persistable;
import java.util.stream.Stream;

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
}
