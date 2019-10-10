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
package com.connexta.replication.api.impl.jackson;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.google.common.annotations.VisibleForTesting;

/** This class provides utility functions for dealing with Json objects. */
public class JsonUtils {
  @VisibleForTesting
  static final ObjectMapper MAPPER =
      new ObjectMapper()
          .registerModule(new ParameterNamesModule())
          .registerModule(new Jdk8Module())
          .registerModule(new JavaTimeModule());

  private JsonUtils() {}

  /**
   * Deserializes JSON content from a given JSON content string.
   *
   * @param <D> the type of value to retrieve
   * @param clazz the class of the value to retrieve
   * @param content the Json content string to deserialize from
   * @return the deserialized value
   * @throws JsonParseException if underlying input contains invalid content
   * @throws com.fasterxml.jackson.databind.JsonMappingException if the input JSON structure does
   *     not match structure expected for result type (or has other mismatch issues)
   * @throws JsonProcessingException if a failure occurs while deserializing the value
   */
  public static <D> D read(Class<D> clazz, String content) throws JsonProcessingException {
    return JsonUtils.MAPPER.readValue(content, clazz);
  }

  /**
   * Serializes any Java value as a string.
   *
   * @throws JsonProcessingException if a failure occurs while serializing the value
   */
  public static String write(Object value) throws JsonProcessingException {
    return JsonUtils.MAPPER.writeValueAsString(value);
  }
}
