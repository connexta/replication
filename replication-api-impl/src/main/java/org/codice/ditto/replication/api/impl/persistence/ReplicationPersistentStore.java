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
package org.codice.ditto.replication.api.impl.persistence;

import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.ws.rs.NotFoundException;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.codice.ddf.persistence.PersistenceException;
import org.codice.ddf.persistence.PersistentItem;
import org.codice.ddf.persistence.PersistentStore;
import org.codice.ditto.replication.api.ReplicationPersistenceException;
import org.codice.ditto.replication.api.data.Persistable;
import org.codice.ditto.replication.api.impl.data.AbstractPersistable;

/**
 * ReplicationPersistentStore uses {@link Persistable}s to alter and retrieve data in a {@link
 * PersistentStore}
 */
public class ReplicationPersistentStore {

  private static final int DEFAULT_PAGE_SIZE = 100;

  private static final int DEFAULT_START_INDEX = 0;

  private final PersistentStore persistentStore;

  public ReplicationPersistentStore(PersistentStore persistentStore) {
    this.persistentStore = persistentStore;
  }

  @VisibleForTesting
  <T extends AbstractPersistable> T createPersistable(Class<T> klass) {
    try {
      return klass.newInstance();
    } catch (InstantiationException ie) {
      throw new ReplicationPersistenceException(
          "Couldn't instantiate "
              + klass.getSimpleName()
              + ", Make sure the class has a public default constructor.",
          ie);
    } catch (Exception e) {
      throw new ReplicationPersistenceException(
          "Couldn't create new persistable " + klass.getSimpleName(), e);
    }
  }

  public <T extends AbstractPersistable> T get(Class<T> klass, String id) {
    String cql = String.format("'id' = '%s'", id);
    List<Map<String, Object>> storedMaps;
    String typeName = getPersistenceType(klass);

    try {
      storedMaps = persistentStore.get(typeName, cql, DEFAULT_START_INDEX, DEFAULT_PAGE_SIZE);
    } catch (PersistenceException e) {
      throw new ReplicationPersistenceException(
          String.format(
              "An error occurred while trying to retrieve an object from persistence with class %s and id %s",
              klass.getName(), id),
          e);
    }

    if (storedMaps.isEmpty()) {
      throw new NotFoundException(
          String.format("Couldn't find persisted object of type %s with ID %s", typeName, id));
    } else {
      if (storedMaps.size() > 1) {
        throw new IllegalStateException(
            String.format("Found two objects in persistence of type %s with ID %s", typeName, id));
      }

      return getPersistableFromMap(klass, storedMaps.get(0));
    }
  }

  public <T extends AbstractPersistable> Stream<T> objects(Class<T> klass) {
    List<Map<String, Object>> storedMaps;
    String typeName = getPersistenceType(klass);
    try {
      storedMaps = persistentStore.get(typeName, "", DEFAULT_START_INDEX, DEFAULT_PAGE_SIZE);
    } catch (PersistenceException e) {
      throw new ReplicationPersistenceException(
          "An error occurred while retrieving data of type " + typeName, e);
    }

    return storedMaps.stream().map(map -> getPersistableFromMap(klass, map));
  }

  private <T extends AbstractPersistable> T getPersistableFromMap(
      Class<T> klass, Map<String, Object> map) {
    T persistable = createPersistable(klass);
    persistable.fromMap(PersistentItem.stripSuffixes(map));
    return persistable;
  }

  @VisibleForTesting
  String getPersistenceType(Class<? extends AbstractPersistable> klass) {
    try {
      return (String) FieldUtils.readStaticField(klass, "PERSISTENCE_TYPE");
    } catch (Exception e) {
      throw new ReplicationPersistenceException(
          "Failed to retrieve the persistence type of the class " + klass.getName(), e);
    }
  }

  public void save(AbstractPersistable persistable) {
    String persistenceType = getPersistenceType(persistable.getClass());
    PersistentItem persistentItem = getPersistentItemFromPersistable(persistable);

    try {
      persistentStore.add(persistenceType, persistentItem);
    } catch (PersistenceException e) {
      throw new ReplicationPersistenceException(
          "An error occurred while saving data of type " + persistenceType, e);
    }
  }

  public void delete(Class<? extends AbstractPersistable> klass, String id) {
    String cql = String.format("'id' = '%s'", id);
    try {
      boolean successful = persistentStore.delete(getPersistenceType(klass), cql) > 0;
      if (!successful) {
        throw new NotFoundException(
            String.format(
                "Couldn't find persisted object of type %s with ID %s", klass.getSimpleName(), id));
      }
    } catch (PersistenceException e) {
      throw new ReplicationPersistenceException(
          String.format(
              "An error occurred while trying to delete a %s with id: %s",
              klass.getSimpleName(), id),
          e);
    }
  }

  private PersistentItem getPersistentItemFromPersistable(AbstractPersistable persistable) {
    PersistentItem persistentItem = new PersistentItem();
    persistable.toMap().forEach(persistentItem::addProperty);
    return persistentItem;
  }
}
