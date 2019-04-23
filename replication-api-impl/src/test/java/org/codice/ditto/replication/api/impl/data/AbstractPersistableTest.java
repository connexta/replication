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
package org.codice.ditto.replication.api.impl.data;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.Test;

public class AbstractPersistableTest {

  private static final String ID = "id";

  private static final String VERSION = "version";

  private static final String MODIFIED = "modified";

  @Test
  public void testAbstractPersistable() {
    String id = "testId";
    AbstractPersistable persistable = new TestPersistable();
    persistable.setId(id);
    assertThat(persistable.getId(), equalTo(id));
  }

  @Test
  public void generateId() {
    AbstractPersistable persistable = new TestPersistable();
    UUID.fromString(persistable.getId()); // will throw an exception if a valid id wasn't generated
  }

  @Test
  public void getVersion() {
    AbstractPersistable persistable = new TestPersistable();
    persistable.setVersion(1);
    assertThat(persistable.getVersion(), is(1));
  }

  @Test
  public void setId() {
    AbstractPersistable persistable = new TestPersistable();
    persistable.setId("id");
    assertThat(persistable.getId(), is("id"));
  }

  @Test
  public void toMap() {
    AbstractPersistable persistable = new TestPersistable();
    persistable.setId("test");
    persistable.setVersion(1);

    Map<String, Object> map = persistable.toMap();

    assertThat(map.get(ID), is("test"));
    assertThat(map.get(VERSION), is(1));
    assertThat(map.get(MODIFIED), notNullValue());
  }

  @Test
  public void fromMap() {
    Map<String, Object> map = new HashMap<>();
    map.put(ID, "test");
    map.put(VERSION, 1);
    Date modified = new Date();
    map.put(MODIFIED, modified);

    AbstractPersistable persistable = new TestPersistable();
    persistable.fromMap(map);

    assertThat(persistable.getId(), is("test"));
    assertThat(persistable.getVersion(), is(1));
    assertThat(persistable.getModified(), is(modified));
  }

  @Test
  public void fromMapNoModifiedDate() {
    Map<String, Object> map = new HashMap<>();
    map.put(ID, "test");
    map.put(VERSION, 1);

    AbstractPersistable persistable = new TestPersistable();
    persistable.fromMap(map);

    assertThat(persistable.getId(), is("test"));
    assertThat(persistable.getVersion(), is(1));
    assertThat(persistable.getModified(), is(new Date(0)));
  }

  private class TestPersistable extends AbstractPersistable {}
}
