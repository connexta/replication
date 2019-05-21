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
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.UUID;
import org.junit.Test;

public class AbstractPersistableTest {

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

  private class TestPersistable extends AbstractPersistable {}
}
