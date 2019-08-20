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
import com.connexta.replication.api.impl.persistence.pojo.Pojo;
import javax.annotation.Nullable;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class AbstractPersistableTest {
  private static final String TYPE = "type";
  private static final String ID = "id-1234";
  private static final String STRING = "string-b";
  private static final TestEnum ENUM = TestEnum.ENUM_A;

  private static final TestPersistable PERSISTABLE =
      new TestPersistable(AbstractPersistableTest.TYPE, AbstractPersistableTest.ID);

  @Rule public ExpectedException exception = ExpectedException.none();

  @Test
  public void testCtorWithType() throws Exception {
    final TestPersistable persistable = new TestPersistable(AbstractPersistableTest.TYPE);

    Assert.assertThat(persistable.getType(), Matchers.equalTo(AbstractPersistableTest.TYPE));
    Assert.assertThat(persistable.getId(), Matchers.notNullValue());
  }

  @Test
  public void testCtorWithTypeAndId() throws Exception {
    Assert.assertThat(
        AbstractPersistableTest.PERSISTABLE.getType(),
        Matchers.equalTo(AbstractPersistableTest.TYPE));
    Assert.assertThat(
        AbstractPersistableTest.PERSISTABLE.getId(), Matchers.equalTo(AbstractPersistableTest.ID));
  }

  @Test
  public void testCtorWithTypeAndNullId() throws Exception {
    final TestPersistable persistable = new TestPersistable(AbstractPersistableTest.TYPE, null);

    Assert.assertThat(persistable.getType(), Matchers.equalTo(AbstractPersistableTest.TYPE));
    Assert.assertThat(persistable.getId(), Matchers.nullValue());
  }

  @Test
  public void testGetId() throws Exception {
    Assert.assertThat(
        AbstractPersistableTest.PERSISTABLE.getId(), Matchers.equalTo(AbstractPersistableTest.ID));
  }

  @Test
  public void testWriteTo() throws Exception {
    final TestPojo pojo = new TestPojo();

    final TestPojo written = AbstractPersistableTest.PERSISTABLE.writeTo(pojo);

    Assert.assertThat(written, Matchers.sameInstance(pojo));
    Assert.assertThat(pojo.getId(), Matchers.equalTo(AbstractPersistableTest.ID));
  }

  @Test
  public void testReadFrom() throws Exception {
    final TestPojo pojo =
        new TestPojo().setId(AbstractPersistableTest.ID).setVersion(TestPojo.CURRENT_VERSION);
    final TestPersistable persistable = new TestPersistable();

    persistable.readFrom(pojo);

    Assert.assertThat(persistable.getId(), Matchers.equalTo(AbstractPersistableTest.ID));
  }

  @Test
  public void testReadFromWithNullId() throws Exception {
    exception.expect(ReplicationPersistenceException.class);
    exception.expectMessage(
        Matchers.matchesPattern(".*missing.*" + AbstractPersistableTest.TYPE + ".*id.*"));

    final TestPojo pojo = new TestPojo().setId(null).setVersion(TestPojo.CURRENT_VERSION);
    final TestPersistable persistable = new TestPersistable();

    persistable.readFrom(pojo);
  }

  @Test
  public void testReadFromWithEmptyId() throws Exception {
    exception.expect(ReplicationPersistenceException.class);
    exception.expectMessage(
        Matchers.matchesPattern(".*empty.*" + AbstractPersistableTest.TYPE + ".*id.*"));

    final TestPojo pojo = new TestPojo().setId("").setVersion(TestPojo.CURRENT_VERSION);
    final TestPersistable persistable = new TestPersistable();

    persistable.readFrom(pojo);
  }

  @Test
  public void testSetOrFailIfNullWhenNullAndIdNotNull() throws Exception {
    exception.expect(ReplicationPersistenceException.class);
    exception.expectMessage(
        Matchers.matchesPattern(
            "missing "
                + AbstractPersistableTest.TYPE
                + " string.*: "
                + AbstractPersistableTest.ID
                + "$"));

    final TestPojo pojo = new TestPojo().setString(null);

    AbstractPersistableTest.PERSISTABLE.setOrFailIfNull(
        "string", pojo::getString, AbstractPersistableTest.PERSISTABLE::setString);
  }

  @Test
  public void testSetOrFailIfNullWhenNullAndIdIsNull() throws Exception {
    exception.expect(ReplicationPersistenceException.class);
    exception.expectMessage(
        Matchers.matchesPattern("missing " + AbstractPersistableTest.TYPE + " string$"));

    final TestPojo pojo = new TestPojo().setString(null);
    final TestPersistable persistable = new TestPersistable(AbstractPersistableTest.TYPE, null);

    persistable.setOrFailIfNull("string", pojo::getString, persistable::setString);
  }

  @Test
  public void testSetOrFailIfNullWhenValid() throws Exception {
    final TestPojo pojo = new TestPojo().setString(AbstractPersistableTest.STRING);

    AbstractPersistableTest.PERSISTABLE.setOrFailIfNull(
        "string-dummy", pojo::getString, AbstractPersistableTest.PERSISTABLE::setString);

    Assert.assertThat(
        AbstractPersistableTest.PERSISTABLE.getString(),
        Matchers.equalTo(AbstractPersistableTest.STRING));
  }

  @Test
  public void testSetOrFailIfNullOrEmptyWhenNullAndIdNotNull() throws Exception {
    exception.expect(ReplicationPersistenceException.class);
    exception.expectMessage(
        Matchers.matchesPattern(
            "missing "
                + AbstractPersistableTest.TYPE
                + " string.*: "
                + AbstractPersistableTest.ID
                + "$"));

    final TestPojo pojo = new TestPojo().setString(null);

    AbstractPersistableTest.PERSISTABLE.setOrFailIfNullOrEmpty(
        "string", pojo::getString, AbstractPersistableTest.PERSISTABLE::setString);
  }

  @Test
  public void testSetOrFailIfNullOrEmptyWhenNullAndIdIsNull() throws Exception {
    exception.expect(ReplicationPersistenceException.class);
    exception.expectMessage(
        Matchers.matchesPattern("missing " + AbstractPersistableTest.TYPE + " string$"));

    final TestPojo pojo = new TestPojo().setString(null);
    final TestPersistable persistable = new TestPersistable(AbstractPersistableTest.TYPE, null);

    persistable.setOrFailIfNullOrEmpty("string", pojo::getString, persistable::setString);
  }

  @Test
  public void testSetOrFailIfNullOrEmptyWhenEmptyAndIdNotNull() throws Exception {
    exception.expect(ReplicationPersistenceException.class);
    exception.expectMessage(
        Matchers.matchesPattern(
            "empty "
                + AbstractPersistableTest.TYPE
                + " string.*: "
                + AbstractPersistableTest.ID
                + "$"));

    final TestPojo pojo = new TestPojo().setString("");

    AbstractPersistableTest.PERSISTABLE.setOrFailIfNullOrEmpty(
        "string", pojo::getString, AbstractPersistableTest.PERSISTABLE::setString);
  }

  @Test
  public void testSetOrFailIfEmptyOrEmptyWhenNullAndIdIsNull() throws Exception {
    exception.expect(ReplicationPersistenceException.class);
    exception.expectMessage(
        Matchers.matchesPattern("empty " + AbstractPersistableTest.TYPE + " string$"));

    final TestPojo pojo = new TestPojo().setString("");
    final TestPersistable persistable = new TestPersistable(AbstractPersistableTest.TYPE, null);

    persistable.setOrFailIfNullOrEmpty("string", pojo::getString, persistable::setString);
  }

  @Test
  public void testSetOrFailIfNullOrEmptyWhenValid() throws Exception {
    final TestPojo pojo = new TestPojo().setString(AbstractPersistableTest.STRING);

    AbstractPersistableTest.PERSISTABLE.setOrFailIfNullOrEmpty(
        "string-dummy", pojo::getString, AbstractPersistableTest.PERSISTABLE::setString);

    Assert.assertThat(
        AbstractPersistableTest.PERSISTABLE.getString(),
        Matchers.equalTo(AbstractPersistableTest.STRING));
  }

  @Test
  public void testConvertAndSetEnumValueOrFailIfNullWhenNullAndIdNotNull() throws Exception {
    exception.expect(ReplicationPersistenceException.class);
    exception.expectMessage(
        Matchers.matchesPattern(
            "missing "
                + AbstractPersistableTest.TYPE
                + " enum.*: "
                + AbstractPersistableTest.ID
                + "$"));

    final TestPojo pojo = new TestPojo().setEnum(null);

    AbstractPersistableTest.PERSISTABLE.convertAndSetEnumValueOrFailIfNullOrEmpty(
        "enum",
        TestEnum.class,
        TestEnum.ENUM_UNKNOWN,
        pojo::getEnum,
        AbstractPersistableTest.PERSISTABLE::setEnum);
  }

  @Test
  public void testConvertAndSetEnumValueOrFailIfNullWhenNullAndIdIsNull() throws Exception {
    exception.expect(ReplicationPersistenceException.class);
    exception.expectMessage(
        Matchers.matchesPattern("missing " + AbstractPersistableTest.TYPE + " enum.*$"));

    final TestPojo pojo = new TestPojo().setEnum(null);
    final TestPersistable persistable = new TestPersistable(AbstractPersistableTest.TYPE, null);

    persistable.convertAndSetEnumValueOrFailIfNullOrEmpty(
        "enum",
        TestEnum.class,
        TestEnum.ENUM_UNKNOWN,
        pojo::getEnum,
        AbstractPersistableTest.PERSISTABLE::setEnum);
  }

  @Test
  public void testConvertAndSetEnumValueOrFailIfNullWhenEmptyAndIdNotNull() throws Exception {
    exception.expect(ReplicationPersistenceException.class);
    exception.expectMessage(
        Matchers.matchesPattern(
            "empty "
                + AbstractPersistableTest.TYPE
                + " enum.*: "
                + AbstractPersistableTest.ID
                + "$"));

    final TestPojo pojo = new TestPojo().setEnum("");

    AbstractPersistableTest.PERSISTABLE.convertAndSetEnumValueOrFailIfNullOrEmpty(
        "enum",
        TestEnum.class,
        TestEnum.ENUM_UNKNOWN,
        pojo::getEnum,
        AbstractPersistableTest.PERSISTABLE::setEnum);
  }

  @Test
  public void testConvertAndSetEnumValueOrFailIfNullWhenEmptyAndIdIsNull() throws Exception {
    exception.expect(ReplicationPersistenceException.class);
    exception.expectMessage(
        Matchers.matchesPattern("empty " + AbstractPersistableTest.TYPE + " enum.*$"));

    final TestPojo pojo = new TestPojo().setEnum("");
    final TestPersistable persistable = new TestPersistable(AbstractPersistableTest.TYPE, null);

    persistable.convertAndSetEnumValueOrFailIfNullOrEmpty(
        "enum",
        TestEnum.class,
        TestEnum.ENUM_UNKNOWN,
        pojo::getEnum,
        AbstractPersistableTest.PERSISTABLE::setEnum);
  }

  @Test
  public void testConvertAndSetEnumValueOrFailIfNullOrEmptyWhenValid() throws Exception {
    final TestPojo pojo = new TestPojo().setEnum(TestEnum.ENUM_A.name());

    AbstractPersistableTest.PERSISTABLE.convertAndSetEnumValueOrFailIfNullOrEmpty(
        "enum-dummy",
        TestEnum.class,
        TestEnum.ENUM_UNKNOWN,
        pojo::getEnum,
        AbstractPersistableTest.PERSISTABLE::setEnum);

    Assert.assertThat(
        AbstractPersistableTest.PERSISTABLE.getEnum(), Matchers.equalTo(TestEnum.ENUM_A));
  }

  @Test
  public void testConvertAndSetEnumValueOrFailIfNullOrEmptyWithNewValue() throws Exception {
    final TestPojo pojo = new TestPojo().setEnum("what-is-this");

    AbstractPersistableTest.PERSISTABLE.convertAndSetEnumValueOrFailIfNullOrEmpty(
        "enum-dummy",
        TestEnum.class,
        TestEnum.ENUM_UNKNOWN,
        pojo::getEnum,
        AbstractPersistableTest.PERSISTABLE::setEnum);

    Assert.assertThat(
        AbstractPersistableTest.PERSISTABLE.getEnum(), Matchers.equalTo(TestEnum.ENUM_UNKNOWN));
  }

  @Test
  public void testConvertAndSetEnumValueWhenNotSpecifyingANullValue() throws Exception {
    final TestPojo pojo = new TestPojo().setEnum(TestEnum.ENUM_A.name());

    AbstractPersistableTest.PERSISTABLE.convertAndSetEnumValue(
        TestEnum.class,
        TestEnum.ENUM_UNKNOWN,
        pojo::getEnum,
        AbstractPersistableTest.PERSISTABLE::setEnum);

    Assert.assertThat(
        AbstractPersistableTest.PERSISTABLE.getEnum(), Matchers.equalTo(TestEnum.ENUM_A));
  }

  @Test
  public void testConvertAndSetEnumValueWithNewValueWhenNotSpecifyingANullValue() throws Exception {
    final TestPojo pojo = new TestPojo().setEnum("what-is-this");

    AbstractPersistableTest.PERSISTABLE.convertAndSetEnumValue(
        TestEnum.class,
        TestEnum.ENUM_UNKNOWN,
        pojo::getEnum,
        AbstractPersistableTest.PERSISTABLE::setEnum);

    Assert.assertThat(
        AbstractPersistableTest.PERSISTABLE.getEnum(), Matchers.equalTo(TestEnum.ENUM_UNKNOWN));
  }

  @Test
  public void testConvertAndSetEnumValueWithNullWhenNotSpecifyingANullValue() throws Exception {
    final TestPojo pojo = new TestPojo().setEnum(null);

    AbstractPersistableTest.PERSISTABLE.convertAndSetEnumValue(
        TestEnum.class,
        TestEnum.ENUM_UNKNOWN,
        pojo::getEnum,
        AbstractPersistableTest.PERSISTABLE::setEnum);

    Assert.assertThat(
        AbstractPersistableTest.PERSISTABLE.getEnum(), Matchers.equalTo(TestEnum.ENUM_UNKNOWN));
  }

  @Test
  public void testConvertAndSetEnumValueWithEmptyWhenNotSpecifyingANullValue() throws Exception {
    final TestPojo pojo = new TestPojo().setEnum("");

    AbstractPersistableTest.PERSISTABLE.convertAndSetEnumValue(
        TestEnum.class,
        TestEnum.ENUM_UNKNOWN,
        pojo::getEnum,
        AbstractPersistableTest.PERSISTABLE::setEnum);

    Assert.assertThat(
        AbstractPersistableTest.PERSISTABLE.getEnum(), Matchers.equalTo(TestEnum.ENUM_UNKNOWN));
  }

  @Test
  public void testConvertAndSetEnumValueWhenSpecifyingANullValue() throws Exception {
    final TestPojo pojo = new TestPojo().setEnum(TestEnum.ENUM_A.name());

    AbstractPersistableTest.PERSISTABLE.convertAndSetEnumValue(
        TestEnum.class,
        TestEnum.ENUM_UNKNOWN,
        TestEnum.ENUM_UNKNOWN,
        pojo::getEnum,
        AbstractPersistableTest.PERSISTABLE::setEnum);

    Assert.assertThat(
        AbstractPersistableTest.PERSISTABLE.getEnum(), Matchers.equalTo(TestEnum.ENUM_A));
  }

  @Test
  public void testConvertAndSetEnumValueWithNewValueWhenSpecifyingANullValue() throws Exception {
    final TestPojo pojo = new TestPojo().setEnum("what-is-this");

    AbstractPersistableTest.PERSISTABLE.convertAndSetEnumValue(
        TestEnum.class,
        TestEnum.ENUM_UNKNOWN,
        TestEnum.ENUM_UNKNOWN,
        pojo::getEnum,
        AbstractPersistableTest.PERSISTABLE::setEnum);

    Assert.assertThat(
        AbstractPersistableTest.PERSISTABLE.getEnum(), Matchers.equalTo(TestEnum.ENUM_UNKNOWN));
  }

  @Test
  public void testConvertAndSetEnumValueWithNullWhenSpecifyingANullValue() throws Exception {
    final TestPojo pojo = new TestPojo().setEnum(null);

    AbstractPersistableTest.PERSISTABLE.convertAndSetEnumValue(
        TestEnum.class,
        null,
        TestEnum.ENUM_UNKNOWN,
        pojo::getEnum,
        AbstractPersistableTest.PERSISTABLE::setEnum);

    Assert.assertThat(AbstractPersistableTest.PERSISTABLE.getEnum(), Matchers.nullValue());
  }

  @Test
  public void testConvertAndSetEnumValueWithEmptyWhenSpecifyingANullValue() throws Exception {
    final TestPojo pojo = new TestPojo().setEnum("");

    AbstractPersistableTest.PERSISTABLE.convertAndSetEnumValue(
        TestEnum.class,
        TestEnum.ENUM_UNKNOWN,
        TestEnum.ENUM_UNKNOWN,
        pojo::getEnum,
        AbstractPersistableTest.PERSISTABLE::setEnum);

    Assert.assertThat(
        AbstractPersistableTest.PERSISTABLE.getEnum(), Matchers.equalTo(TestEnum.ENUM_UNKNOWN));
  }

  private static class TestPersistable extends AbstractPersistable<TestPojo> {
    @Nullable private String string;
    @Nullable private TestEnum anEnum;

    TestPersistable() {
      super(AbstractPersistableTest.TYPE);
    }

    TestPersistable(String type) {
      super(type);
    }

    TestPersistable(String type, String id) {
      super(type, id);
    }

    @Nullable
    public String getString() {
      return string;
    }

    public void setString(@Nullable String string) {
      this.string = string;
    }

    @Nullable
    public TestEnum getEnum() {
      return anEnum;
    }

    public void setEnum(@Nullable TestEnum anEnum) {
      this.anEnum = anEnum;
    }
  }

  private static class TestPojo extends Pojo<TestPojo> {
    public static final int CURRENT_VERSION = 1;

    @Nullable private String string;
    @Nullable private String anEnum;

    public TestPojo() {
      super.setVersion(TestPojo.CURRENT_VERSION);
    }

    @Nullable
    public String getString() {
      return string;
    }

    public TestPojo setString(@Nullable String string) {
      this.string = string;
      return this;
    }

    @Nullable
    public String getEnum() {
      return anEnum;
    }

    public TestPojo setEnum(@Nullable String anEnum) {
      this.anEnum = anEnum;
      return this;
    }
  }

  private static enum TestEnum {
    ENUM_A,
    ENUM_B,
    ENUM_UNKNOWN
  }
}
