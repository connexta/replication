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

import com.connexta.ion.replication.api.ReplicationPersistenceException;
import com.connexta.replication.api.data.Persistable;
import com.connexta.replication.api.impl.persistence.pojo.Pojo;
import com.google.common.annotations.VisibleForTesting;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * This class defines some data that any object to be saved in persistence should have and methods
 * to manipulate that data.
 *
 * @param <P> the type of pojo associated with this persistable
 */
public abstract class AbstractPersistable<P extends Pojo> implements Persistable {
  /** A string representing the type of this object. Used when generating exception or logs. */
  protected final String type;

  private String id;

  protected AbstractPersistable(String type) {
    this.type = type;
    this.id = UUID.randomUUID().toString();
  }

  protected AbstractPersistable(String type, @Nullable String id) {
    this.type = type;
    this.id = id;
  }

  @Override
  public String getId() {
    return id;
  }

  /**
   * Serializes this object to the specified pojo that can be serialized to a spring-data supported
   * data store. Any implementation of this method in a subclass should first make a call to the
   * super version of this method before serializing its own properties.
   *
   * @param pojo the pojo to serialize this object to
   * @return <code>pojo</code> for chaining
   * @throws ReplicationPersistenceException if an error occurs while trying to serialize this
   *     object
   */
  protected P writeTo(P pojo) {
    pojo.setId(id);
    return pojo;
  }

  /**
   * Deserializes the specified pojo into this persistable object. This method is responsible for
   * performing any required conversion to the latest version. Any implementation of this method in
   * a subclass should first make a call to the super version before performing its own
   * functionality.
   *
   * @param pojo the pojo to deserialize
   * @throws ReplicationPersistenceException if an error occurs while trying to deserialize the pojo
   *     object
   */
  protected void readFrom(P pojo) {
    setOrFailIfNull("identifier", pojo::getId, this::setId);
  }

  /**
   * Useful method that can be used to validate if the value of a field is not <code>null</code> in
   * the pojo before setting it in this object. If it is <code>null</code> than an exception is
   * thrown otherwise the object's corresponding field would be set accordingly. The pojo field's
   * value is retrieved using a consumer (e.g. <code>pojo::getName</code>) and set in this object
   * using a supplier (e.g. <code>this::setName</code>).
   *
   * @param field the name of the field being check for presence (used in the exception message)
   * @param supplier a supplier capable of retrieving the pojo's current value for the field
   * @param consumer a consumer capable of updating this object with the field's value if it is
   *     defined
   * @param <F> the field's type
   * @throws ReplicationPersistenceException if the field's value as supplied by <code>supplier
   *     </code> is <code>null</code>
   */
  protected <F> void setOrFailIfNull(
      String field, Supplier<? extends F> supplier, Consumer<? super F> consumer) {
    final F value = supplier.get();

    if (value == null) {
      if (id != null) {
        throw new ReplicationPersistenceException(
            "missing " + type + " " + field + " for object: " + id);
      }
      throw new ReplicationPersistenceException("missing " + type + " " + field);
    }
    consumer.accept(value);
  }

  /**
   * Useful method that can be used to validate if the value of a field is not <code>null</code> and
   * not empty in the pojo before setting it in this object. If it is <code>null</code> or empty
   * than an exception is thrown otherwise the object's corresponding field would be set
   * accordingly. The pojo field's value is retrieved using a consumer (e.g. <code>pojo::getName
   * </code>) and set in this object using a supplier (e.g. <code>this::setName</code>).
   *
   * @param field the name of the field being check for presence (used in the exception message)
   * @param supplier a supplier capable of retrieving the pojo's current value for the field
   * @param consumer a consumer capable of updating this object with the field's value if it is
   *     defined
   * @throws ReplicationPersistenceException if the field's value as supplied by <code>supplier
   *     </code> is <code>null</code> or empty
   */
  protected <F> void setOrFailIfNullOrEmpty(
      String field, Supplier<String> supplier, Consumer<String> consumer) {
    final String value = supplier.get();

    if (value == null) {
      if (id != null) {
        throw new ReplicationPersistenceException(
            "missing " + type + " " + field + " for object: " + id);
      }
      throw new ReplicationPersistenceException("missing " + type + " " + field);
    } else if (value.isEmpty()) {
      if (id != null) {
        throw new ReplicationPersistenceException(
            "empty " + type + " " + field + " for object: " + id);
      }
      throw new ReplicationPersistenceException("empty " + type + " " + field);
    }
    consumer.accept(value);
  }

  /**
   * Useful method that can be used to validate that the value of an enum field is defined as a
   * valid string representation of an enumeration value before setting it in this object. If it is
   * not a valid value than <code>unknown</code>> is used as the value to set in this object.
   * Otherwise, the corresponding enum value is set accordingly. The pojo field's value is retrieved
   * using a consumer (e.g. <code>pojo::getType</code>) and set in this object using a supplier
   * (e.g. <code>this::setType</code>).
   *
   * @param clazz the field's enumeration class to convert to
   * @param unknown the enumeration value to be used when the corresponding pojo filed's string
   *     value doesn't match any of the defined values
   * @param supplier a supplier capable of retrieving the pojo's current value for the field
   * @param consumer a consumer capable of updating this object with the field's value if it is
   *     defined
   * @param <E> the field's enum type
   */
  protected <E extends Enum<E>> void convertAndSetEnumValue(
      Class<E> clazz, E unknown, Supplier<String> supplier, Consumer<E> consumer) {
    final String value = supplier.get();

    if (value != null) {
      try {
        consumer.accept(Enum.valueOf(clazz, value));
        return;
      } catch (IllegalArgumentException e) {
      }
    }
    consumer.accept(unknown);
  }

  @VisibleForTesting
  void setId(String id) {
    this.id = id;
  }
}
