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

import com.connexta.replication.api.data.InvalidFieldException;
import com.connexta.replication.api.data.Persistable;
import com.connexta.replication.api.impl.persistence.pojo.Pojo;
import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
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
  protected final String persistableType;

  private String id;

  /**
   * Instantiates a new persistable corresponding to a new object that do not yet exist in the
   * database.
   *
   * @param type a string representing the type of this object (used when generating exception or
   *     logs)
   */
  protected AbstractPersistable(String type) {
    this(type, UUID.randomUUID().toString());
  }

  /**
   * Instantiates a new persistable corresponding to an object being reloaded from the database in
   * which case the identifier can either be provided or <code>null</code> can be provided with the
   * expectation that the identifier will be restored as soon as the subclass calls {@link
   * #readFrom(Pojo)}.
   *
   * <p><i>Note:</i> It is the responsibility of the derived class which calls this constructor with
   * a <code>null</code> identifier to ensure that the identifier is restored or set before
   * returning any instances of the object.
   *
   * @param type a string representing the type of this object (used when generating exception or
   *     logs)
   * @param id the previously generated identifier for this object or <code>null</code> if it is
   *     expected to be restored later when {@link #readFrom(Pojo)} is called by the subclass
   */
  protected AbstractPersistable(String type, @Nullable String id) {
    this.persistableType = type;
    this.id = id;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (obj instanceof AbstractPersistable<?>) {
      final AbstractPersistable<?> persistable = (AbstractPersistable<?>) obj;

      return Objects.equals(id, persistable.id);
    }
    return false;
  }

  /**
   * Serializes this object to the specified pojo that can be serialized to a spring-data supported
   * data store. Any implementation of this method in a subclass should first make a call to the
   * super version of this method before serializing its own properties.
   *
   * @param pojo the pojo to serialize this object to
   * @return <code>pojo</code> for chaining
   * @throws InvalidFieldException if an error occurs while trying to serialize this object
   */
  protected P writeTo(P pojo) {
    setOrFailIfNullOrEmpty("id", this::getId, pojo::setId);
    return pojo;
  }

  /**
   * Deserializes the specified pojo into this persistable object. This method is responsible for
   * performing any required conversion to the latest version. Any implementation of this method in
   * a subclass should first make a call to the super version before performing its own
   * functionality.
   *
   * @param pojo the pojo to deserialize
   * @throws InvalidFieldException if an error occurs while trying to deserialize the pojo object
   */
  protected void readFrom(P pojo) {
    setOrFailIfNullOrEmpty("id", pojo::getId, this::setId);
  }

  /**
   * Useful method that can be used to validate if the value of a field is not <code>null</code>
   * before setting it in a given destination. If it is <code>null</code> than an exception is
   * thrown otherwise the destination's corresponding field would be set accordingly. The field's
   * value is retrieved using a consumer (e.g. <code>pojo::getName</code>) and set in the
   * destination using a supplier (e.g. <code>this::setName</code>).
   *
   * @param field the name of the field being checked for presence (used in the exception message)
   * @param supplier a supplier capable of retrieving the current value for the field
   * @param consumer a consumer capable of updating the destination with the field's value if it is
   *     defined
   * @param <F> the field's type
   * @throws InvalidFieldException if the field's value as supplied by <code>supplier
   *     </code> is <code>null</code>
   */
  protected <F> void setOrFailIfNull(
      String field, Supplier<? extends F> supplier, Consumer<? super F> consumer) {
    consumer.accept(validateNotNull(field, supplier.get()));
  }

  /**
   * Useful method that can be used to validate if the value of a field is not <code>null</code>
   * before converting and setting it in a given destination. If it is <code>null</code> than an
   * exception is thrown otherwise the destination's corresponding field would be set accordingly.
   * The field's value is retrieved using a consumer (e.g. <code>pojo::getName</code>) then
   * converted before finally being set in the destination using a supplier (e.g. <code>
   * this::setName</code>).
   *
   * @param field the name of the field being checked for presence (used in the exception message)
   * @param supplier a supplier capable of retrieving the current value for the field
   * @param converter a converter to convert from the field's value to the one set in the
   *     destination
   * @param consumer a consumer capable of updating the destination with the converted field's value
   *     if it is defined
   * @param <F> the field's type
   * @param <P> the consumed type
   * @throws InvalidFieldException if the field's value as supplied by <code>supplier
   *     </code> is <code>null</code> or failed to be converted
   */
  protected <F, P> void convertAndSetOrFailIfNull(
      String field,
      Supplier<? extends F> supplier,
      Function<? super F, ? extends P> converter,
      Consumer<? super P> consumer) {
    consumer.accept(converter.apply(validateNotNull(field, supplier.get())));
  }

  /**
   * Useful method that can be used to validate if the value of a field is not <code>null</code> and
   * not empty before setting it in a given destination. If it is <code>null</code> or empty than an
   * exception is thrown otherwise the destination's corresponding field would be set accordingly.
   * The field's value is retrieved using a consumer (e.g. <code>pojo::getName</code>) and set in
   * the destination using a supplier (e.g. <code>this::setName</code>).
   *
   * @param field the name of the field being checked for presence (used in the exception message)
   * @param supplier a supplier capable of retrieving the current value for the field
   * @param consumer a consumer capable of updating the destination with the field's value if it is
   *     defined
   * @throws InvalidFieldException if the field's value as supplied by <code>supplier
   *     </code> is <code>null</code> or empty
   */
  protected void setOrFailIfNullOrEmpty(
      String field, Supplier<String> supplier, Consumer<String> consumer) {
    consumer.accept(validateNotNullAndNotEmpty(field, supplier.get()));
  }

  /**
   * Useful method that can be used to validate if the value of a field is not <code>null</code> and
   * not empty before converting and setting it in a given destination. If it is <code>null
   * </code> or empty than an exception is thrown otherwise the destination's corresponding field
   * would be set accordingly. The pojo field's value is retrieved using a consumer (e.g. <code>
   * pojo::getName</code>) and set in the destination using a supplier (e.g. <code>this::setName
   * </code>).
   *
   * @param field the name of the field being checked for presence (used in the exception message)
   * @param supplier a supplier capable of retrieving the current value for the field
   * @param converter a converter to convert from the field's value to the one set in the
   *     destination
   * @param consumer a consumer capable of updating the destination with the field's value if it is
   *     defined
   * @param <P> the consumed type
   * @throws InvalidFieldException if the field's value as supplied by <code>supplier
   *     </code> is <code>null</code> or empty or cannot be converted
   */
  protected <P> void convertAndSetOrFailIfNullOrEmpty(
      String field,
      Supplier<String> supplier,
      Function<String, ? extends P> converter,
      Consumer<? super P> consumer) {
    consumer.accept(converter.apply(validateNotNullAndNotEmpty(field, supplier.get())));
  }

  /**
   * Useful method that can be used to validate that the value of an enum field is defined as a
   * valid string representation of an enumeration value before setting it in a given destination.
   * If it is not a valid value than <code>unknown</code> is used as the value to set in the
   * destination. Otherwise, the corresponding enum value is set accordingly. The field's value is
   * retrieved using a consumer (e.g. <code>pojo::getType</code>) and set in the destination using a
   * supplier (e.g. <code>this::setType</code>).
   *
   * @param field the name of the field being checked for presence (used in the exception message)
   * @param clazz the field's enumeration class to convert to
   * @param unknown the enumeration value to be used when the corresponding field's string value
   *     doesn't match any of the defined values
   * @param supplier a supplier capable of retrieving the current value for the field
   * @param consumer a consumer capable of updating the destination with the field's value if it is
   *     defined
   * @param <E> the field's enum type
   * @throws InvalidFieldException if the field's value as supplied by <code>supplier
   *     </code> is <code>null</code> or empty
   */
  protected <E extends Enum<E>> void convertAndSetEnumValueOrFailIfNullOrEmpty(
      String field, Class<E> clazz, E unknown, Supplier<String> supplier, Consumer<E> consumer) {
    try {
      consumer.accept(Enum.valueOf(clazz, validateNotNullAndNotEmpty(field, supplier.get())));
      return;
    } catch (IllegalArgumentException e) { // ignore
    }
    consumer.accept(unknown);
  }

  /**
   * Useful method that can be used to validate that the value of an enum field is defined as a
   * valid string representation of an enumeration value before setting it a given destination. If
   * it is not a valid value than <code>unknown</code> is used as the value to set in this object.
   * Otherwise, the corresponding enum value is set accordingly. The field's value is retrieved
   * using a consumer (e.g. <code>pojo::getType</code>) and set in the destination using a supplier
   * (e.g. <code>this::setType</code>).
   *
   * @param clazz the field's enumeration class to convert to
   * @param unknown the enumeration value to be used when the corresponding field's string value
   *     doesn't match any of the defined values or is <code>null</code>
   * @param supplier a supplier capable of retrieving the current value for the field
   * @param consumer a consumer capable of updating the destination with the field's value if it is
   *     defined
   * @param <E> the field's enum type
   */
  protected <E extends Enum<E>> void convertAndSetEnumValue(
      Class<E> clazz, E unknown, Supplier<String> supplier, Consumer<E> consumer) {
    convertAndSetEnumValue(clazz, unknown, unknown, supplier, consumer);
  }

  /**
   * Useful method that can be used to validate that the value of an enum field is defined as a
   * valid string representation of an enumeration value before setting it in a given destination.
   * If it is not a valid value than <code>unknown</code> is used as the value to set in the
   * destination. Otherwise, the corresponding enum value is set accordingly. The field's value is
   * retrieved using a consumer (e.g. <code>pojo::getType</code>) and set in the destination using a
   * supplier (e.g. <code>this::setType</code>).
   *
   * @param clazz the field's enumeration class to convert to
   * @param ifNull the enumeration value to be used when the corresponding field's string value is
   *     <code>null</code> (<code>null</code> can be passed which means <code>null</code> will be
   *     set in the field's value)
   * @param unknown the enumeration value to be used when the corresponding field's string value
   *     doesn't match any of the defined values (or is empty)
   * @param supplier a supplier capable of retrieving the current value for the field
   * @param consumer a consumer capable of updating the destination with the field's value if it is
   *     defined
   * @param <E> the field's enum type
   */
  protected <E extends Enum<E>> void convertAndSetEnumValue(
      Class<E> clazz,
      @Nullable E ifNull,
      E unknown,
      Supplier<String> supplier,
      Consumer<E> consumer) {
    final String value = supplier.get();

    if (value != null) {
      try {
        consumer.accept(Enum.valueOf(clazz, value));
        return;
      } catch (IllegalArgumentException e) { // ignore
      }
      consumer.accept(unknown);
    } else {
      consumer.accept(ifNull);
    }
  }

  /**
   * Validates the specified field's value cannot be <code>null</code>.
   *
   * @param field the name of the field being checked (used in the exception message)
   * @param value the field value to verify
   * @param <F> the type for the field value
   * @return <code>value</code> for chaining
   * @throws InvalidFieldException if <code>value</code> is <code>null</code>
   */
  protected <F> F validateNotNull(String field, @Nullable F value) {
    if (value == null) {
      if (id != null) {
        throw new InvalidFieldException(
            "missing " + persistableType + " " + field + " for object: " + id);
      }
      throw new InvalidFieldException("missing " + persistableType + " " + field);
    }
    return value;
  }

  /**
   * Validates the specified field's value cannot be <code>null</code> or empty.
   *
   * @param field the name of the field being checked (used in the exception message)
   * @param value the field value to verify
   * @return <code>value</code> for chaining
   * @throws InvalidFieldException if <code>value</code> is <code>null</code> or empty
   */
  @SuppressWarnings("squid:S2259" /* value is verified first via validateNotNull() */)
  protected String validateNotNullAndNotEmpty(String field, @Nullable String value) {
    validateNotNull(field, value);
    if (value.isEmpty()) {
      if (id != null) {
        throw new InvalidFieldException(
            "empty " + persistableType + " " + field + " for object: " + id);
      }
      throw new InvalidFieldException("empty " + persistableType + " " + field);
    }
    return value;
  }

  @VisibleForTesting
  String getPersistableType() {
    return persistableType;
  }

  @VisibleForTesting
  void setId(String id) {
    this.id = id;
  }
}
