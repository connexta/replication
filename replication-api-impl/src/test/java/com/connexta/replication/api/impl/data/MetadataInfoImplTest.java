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
import com.connexta.replication.api.data.Metadata;
import com.connexta.replication.api.data.MetadataInfo;
import com.connexta.replication.api.data.UnsupportedVersionException;
import com.connexta.replication.api.impl.persistence.pojo.MetadataInfoPojo;
import com.github.npathai.hamcrestopt.OptionalMatchers;
import java.time.Instant;
import java.util.Date;
import java.util.OptionalLong;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

public class MetadataInfoImplTest {
  private static final String ID = "id";
  private static final String TYPE = "type";
  private static final Date LAST_MODIFIED_DATE = new Date();
  private static final Instant LAST_MODIFIED = MetadataInfoImplTest.LAST_MODIFIED_DATE.toInstant();
  private static final long SIZE = 34L;

  @Rule public ExpectedException exception = ExpectedException.none();

  private final MetadataInfo info = Mockito.mock(MetadataInfo.class);

  private final MetadataInfoImpl persistable;

  public MetadataInfoImplTest() {
    Mockito.when(info.getType()).thenReturn(MetadataInfoImplTest.TYPE);
    Mockito.when(info.getLastModified()).thenReturn(MetadataInfoImplTest.LAST_MODIFIED);
    Mockito.when(info.getSize()).thenReturn(OptionalLong.of(MetadataInfoImplTest.SIZE));

    this.persistable = new MetadataInfoImpl(info);
    persistable.setId(MetadataInfoImplTest.ID);
  }

  @Test
  public void testCtorWithMetadataInfo() throws Exception {
    final MetadataInfoImpl persistable = new MetadataInfoImpl(info);

    Assert.assertThat(persistable.getId(), Matchers.not(Matchers.emptyOrNullString()));
    Assert.assertThat(persistable.getType(), Matchers.equalTo(MetadataInfoImplTest.TYPE));
    Assert.assertThat(
        persistable.getLastModified(), Matchers.equalTo(MetadataInfoImplTest.LAST_MODIFIED));
    Assert.assertThat(
        persistable.getSize().stream().boxed().findFirst(),
        OptionalMatchers.isPresentAndIs(MetadataInfoImplTest.SIZE));
  }

  @Test
  public void testCtorWithMetadata() throws Exception {
    final Metadata metadata = Mockito.mock(Metadata.class);

    Mockito.when(metadata.getMetadataModified())
        .thenReturn(Date.from(MetadataInfoImplTest.LAST_MODIFIED));
    Mockito.when(metadata.getMetadataSize()).thenReturn(MetadataInfoImplTest.SIZE);

    final MetadataInfoImpl persistable = new MetadataInfoImpl(MetadataInfoImplTest.TYPE, metadata);

    Assert.assertThat(persistable.getId(), Matchers.not(Matchers.emptyOrNullString()));
    Assert.assertThat(persistable.getType(), Matchers.equalTo(MetadataInfoImplTest.TYPE));
    Assert.assertThat(
        persistable.getLastModified(), Matchers.equalTo(MetadataInfoImplTest.LAST_MODIFIED));
    Assert.assertThat(
        persistable.getSize().stream().boxed().findFirst(),
        OptionalMatchers.isPresentAndIs(MetadataInfoImplTest.SIZE));
  }

  @Test
  public void testCtor() throws Exception {
    final MetadataInfoImpl persistable =
        new MetadataInfoImpl(
            MetadataInfoImplTest.TYPE,
            MetadataInfoImplTest.LAST_MODIFIED,
            MetadataInfoImplTest.SIZE);

    Assert.assertThat(persistable.getId(), Matchers.not(Matchers.emptyOrNullString()));
    Assert.assertThat(persistable.getType(), Matchers.equalTo(MetadataInfoImplTest.TYPE));
    Assert.assertThat(
        persistable.getLastModified(), Matchers.equalTo(MetadataInfoImplTest.LAST_MODIFIED));
    Assert.assertThat(
        persistable.getSize().stream().boxed().findFirst(),
        OptionalMatchers.isPresentAndIs(MetadataInfoImplTest.SIZE));
  }

  @Test
  public void testCtorWithPojo() throws Exception {
    final MetadataInfoPojo<?> pojo =
        new MetadataInfoPojo<>()
            .setVersion(MetadataInfoPojo.CURRENT_VERSION)
            .setId(MetadataInfoImplTest.ID)
            .setType(MetadataInfoImplTest.TYPE)
            .setLastModified(MetadataInfoImplTest.LAST_MODIFIED)
            .setSize(MetadataInfoImplTest.SIZE);

    final MetadataInfoImpl persistable = new MetadataInfoImpl(pojo);

    Assert.assertThat(persistable.getId(), Matchers.not(Matchers.emptyOrNullString()));
    Assert.assertThat(persistable.getType(), Matchers.equalTo(MetadataInfoImplTest.TYPE));
    Assert.assertThat(
        persistable.getLastModified(), Matchers.equalTo(MetadataInfoImplTest.LAST_MODIFIED));
    Assert.assertThat(
        persistable.getSize().stream().boxed().findFirst(),
        OptionalMatchers.isPresentAndIs(MetadataInfoImplTest.SIZE));
  }

  @Test
  public void testGetType() throws Exception {
    Assert.assertThat(persistable.getType(), Matchers.equalTo(MetadataInfoImplTest.TYPE));
  }

  @Test
  public void testGetLastModified() throws Exception {
    Assert.assertThat(
        persistable.getLastModified(), Matchers.equalTo(MetadataInfoImplTest.LAST_MODIFIED));
  }

  @Test
  public void testGetLastSize() throws Exception {
    Assert.assertThat(
        persistable.getSize().stream().boxed().findFirst(),
        OptionalMatchers.isPresentAndIs(MetadataInfoImplTest.SIZE));
  }

  @Test
  public void testGetLastSizeWhenNoneDefined() throws Exception {
    persistable.setSize(-1L);

    Assert.assertThat(persistable.getSize().isPresent(), Matchers.equalTo(false));
  }

  @Test
  public void testWriteTo() throws Exception {
    final MetadataInfoPojo<?> pojo = new MetadataInfoPojo<>();

    persistable.writeTo(pojo);

    Assert.assertThat(pojo.getId(), Matchers.equalTo(persistable.getId()));
    Assert.assertThat(pojo.getVersion(), Matchers.equalTo(MetadataInfoPojo.CURRENT_VERSION));
    Assert.assertThat(pojo.getType(), Matchers.equalTo(persistable.getType()));
    Assert.assertThat(pojo.getLastModified(), Matchers.equalTo(persistable.getLastModified()));
    Assert.assertThat(pojo.getSize(), Matchers.equalTo(persistable.getSize().getAsLong()));
  }

  @Test
  public void testWriteToWhenTypeIsNull() throws Exception {
    exception.expect(InvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*missing.*type.*"));

    final MetadataInfoImpl persistable =
        new MetadataInfoImpl(null, MetadataInfoImplTest.LAST_MODIFIED, MetadataInfoImplTest.SIZE);
    final MetadataInfoPojo<?> pojo = new MetadataInfoPojo<>();

    persistable.writeTo(pojo);
  }

  @Test
  public void testWriteToWhenNameIsEmpty() throws Exception {
    exception.expect(InvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*empty.*type.*"));

    final MetadataInfoImpl persistable =
        new MetadataInfoImpl("", MetadataInfoImplTest.LAST_MODIFIED, MetadataInfoImplTest.SIZE);
    final MetadataInfoPojo<?> pojo = new MetadataInfoPojo<>();

    persistable.writeTo(pojo);
  }

  @Test
  public void testWriteToWhenLastModifiedIsNull() throws Exception {
    exception.expect(InvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*missing.*last modified.*"));

    final MetadataInfoImpl persistable =
        new MetadataInfoImpl(MetadataInfoImplTest.TYPE, null, MetadataInfoImplTest.SIZE);
    final MetadataInfoPojo<?> pojo = new MetadataInfoPojo<>();

    persistable.writeTo(pojo);
  }

  @Test
  public void testWriteToWhenSizeIsNegative() throws Exception {
    final MetadataInfoImpl persistable =
        new MetadataInfoImpl(MetadataInfoImplTest.TYPE, MetadataInfoImplTest.LAST_MODIFIED, -3L);
    final MetadataInfoPojo<?> pojo = new MetadataInfoPojo<>();

    persistable.writeTo(pojo);

    Assert.assertThat(pojo.getId(), Matchers.equalTo(persistable.getId()));
    Assert.assertThat(pojo.getVersion(), Matchers.equalTo(MetadataInfoPojo.CURRENT_VERSION));
    Assert.assertThat(pojo.getType(), Matchers.equalTo(persistable.getType()));
    Assert.assertThat(pojo.getLastModified(), Matchers.equalTo(persistable.getLastModified()));
    Assert.assertThat(pojo.getSize(), Matchers.equalTo(-1L));
  }

  @Test
  public void testWriteToWhenSizeIs0() throws Exception {
    final MetadataInfoImpl persistable =
        new MetadataInfoImpl(MetadataInfoImplTest.TYPE, MetadataInfoImplTest.LAST_MODIFIED, 0L);
    final MetadataInfoPojo<?> pojo = new MetadataInfoPojo<>();

    persistable.writeTo(pojo);

    Assert.assertThat(pojo.getId(), Matchers.equalTo(persistable.getId()));
    Assert.assertThat(pojo.getVersion(), Matchers.equalTo(MetadataInfoPojo.CURRENT_VERSION));
    Assert.assertThat(pojo.getType(), Matchers.equalTo(persistable.getType()));
    Assert.assertThat(pojo.getLastModified(), Matchers.equalTo(persistable.getLastModified()));
    Assert.assertThat(pojo.getSize(), Matchers.equalTo(0L));
  }

  @Test
  public void testReadFromCurrentVersion() throws Exception {
    final MetadataInfoPojo<?> pojo =
        new MetadataInfoPojo<>()
            .setVersion(MetadataInfoPojo.CURRENT_VERSION)
            .setId(MetadataInfoImplTest.ID)
            .setType(MetadataInfoImplTest.TYPE)
            .setLastModified((MetadataInfoImplTest.LAST_MODIFIED))
            .setSize(MetadataInfoImplTest.SIZE);
    final MetadataInfoImpl persistable = new MetadataInfoImpl(null, null, -1L);

    persistable.readFrom(pojo);

    Assert.assertThat(persistable.getId(), Matchers.equalTo(pojo.getId()));
    Assert.assertThat(persistable.getType(), Matchers.equalTo(pojo.getType()));
    Assert.assertThat(persistable.getLastModified(), Matchers.equalTo(pojo.getLastModified()));
    Assert.assertThat(
        persistable.getSize().stream().boxed().findFirst(),
        OptionalMatchers.isPresentAndIs(pojo.getSize()));
  }

  @Test
  public void testReadFromFutureVersion() throws Exception {
    final MetadataInfoPojo<?> pojo =
        new MetadataInfoPojo<>()
            .setId(MetadataInfoImplTest.ID)
            .setVersion(9999999)
            .setType(MetadataInfoImplTest.TYPE)
            .setLastModified((MetadataInfoImplTest.LAST_MODIFIED))
            .setSize(MetadataInfoImplTest.SIZE);
    final MetadataInfoImpl persistable = new MetadataInfoImpl(null, null, -1L);

    persistable.readFrom(pojo);

    Assert.assertThat(persistable.getId(), Matchers.equalTo(pojo.getId()));
    Assert.assertThat(persistable.getType(), Matchers.equalTo(pojo.getType()));
    Assert.assertThat(persistable.getLastModified(), Matchers.equalTo(pojo.getLastModified()));
    Assert.assertThat(
        persistable.getSize().stream().boxed().findFirst(),
        OptionalMatchers.isPresentAndIs(pojo.getSize()));
  }

  @Test
  public void testReadFromUnsupportedVersion() throws Exception {
    exception.expect(UnsupportedVersionException.class);
    exception.expectMessage(Matchers.matchesPattern(".*unsupported.*version.*"));

    final MetadataInfoPojo<?> pojo =
        new MetadataInfoPojo<>()
            .setId(MetadataInfoImplTest.ID)
            .setVersion(-1)
            .setType(MetadataInfoImplTest.TYPE)
            .setLastModified((MetadataInfoImplTest.LAST_MODIFIED))
            .setSize(MetadataInfoImplTest.SIZE);
    final MetadataInfoImpl persistable = new MetadataInfoImpl(null, null, -1L);

    persistable.readFrom(pojo);
  }

  @Test
  public void testReadFromCurrentVersionWithNullType() throws Exception {
    exception.expect(InvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*missing.*type.*"));

    final MetadataInfoPojo<?> pojo =
        new MetadataInfoPojo<>()
            .setVersion(MetadataInfoPojo.CURRENT_VERSION)
            .setId(MetadataInfoImplTest.ID)
            .setLastModified((MetadataInfoImplTest.LAST_MODIFIED))
            .setSize(MetadataInfoImplTest.SIZE);
    final MetadataInfoImpl persistable = new MetadataInfoImpl(null, null, -1L);

    persistable.readFrom(pojo);
  }

  @Test
  public void testReadFromCurrentVersionWithEmptyType() throws Exception {
    exception.expect(InvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*empty.*type.*"));

    final MetadataInfoPojo<?> pojo =
        new MetadataInfoPojo<>()
            .setVersion(MetadataInfoPojo.CURRENT_VERSION)
            .setId(MetadataInfoImplTest.ID)
            .setType("")
            .setLastModified((MetadataInfoImplTest.LAST_MODIFIED))
            .setSize(MetadataInfoImplTest.SIZE);
    final MetadataInfoImpl persistable = new MetadataInfoImpl(null, null, -1L);

    persistable.readFrom(pojo);
  }

  @Test
  public void testReadFromCurrentVersionWithNullLastModified() throws Exception {
    exception.expect(InvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*missing.*last modified.*"));

    final MetadataInfoPojo<?> pojo =
        new MetadataInfoPojo<>()
            .setVersion(MetadataInfoPojo.CURRENT_VERSION)
            .setId(MetadataInfoImplTest.ID)
            .setType(MetadataInfoImplTest.TYPE)
            .setSize(MetadataInfoImplTest.SIZE);
    final MetadataInfoImpl persistable = new MetadataInfoImpl(null, null, -1L);

    persistable.readFrom(pojo);
  }

  @Test
  public void testReadFromCurrentVersionWithNegativeSize() throws Exception {
    final MetadataInfoPojo<?> pojo =
        new MetadataInfoPojo<>()
            .setVersion(MetadataInfoPojo.CURRENT_VERSION)
            .setId(MetadataInfoImplTest.ID)
            .setType(MetadataInfoImplTest.TYPE)
            .setLastModified((MetadataInfoImplTest.LAST_MODIFIED))
            .setSize(-5L);
    final MetadataInfoImpl persistable = new MetadataInfoImpl(null, null, -1L);

    persistable.readFrom(pojo);

    Assert.assertThat(persistable.getId(), Matchers.equalTo(pojo.getId()));
    Assert.assertThat(persistable.getType(), Matchers.equalTo(pojo.getType()));
    Assert.assertThat(persistable.getLastModified(), Matchers.equalTo(pojo.getLastModified()));
    Assert.assertThat(persistable.getSize().isPresent(), Matchers.equalTo(false));
  }

  @Test
  public void testHashCodeWhenEquals() throws Exception {
    final MetadataInfoImpl persistable2 =
        new MetadataInfoImpl(
            MetadataInfoImplTest.TYPE,
            MetadataInfoImplTest.LAST_MODIFIED,
            MetadataInfoImplTest.SIZE);

    persistable2.setId(MetadataInfoImplTest.ID);

    Assert.assertThat(persistable.hashCode(), Matchers.equalTo(persistable2.hashCode()));
  }

  @Test
  public void testHashCodeWhenDifferent() throws Exception {
    final MetadataInfoImpl persistable2 =
        new MetadataInfoImpl(
            MetadataInfoImplTest.TYPE,
            MetadataInfoImplTest.LAST_MODIFIED,
            MetadataInfoImplTest.SIZE);

    persistable2.setId(MetadataInfoImplTest.ID + "2");

    Assert.assertThat(
        persistable.hashCode(), Matchers.not(Matchers.equalTo(persistable2.hashCode())));
  }

  @Test
  public void testEqualsWhenEquals() throws Exception {
    final MetadataInfoImpl persistable2 =
        new MetadataInfoImpl(
            MetadataInfoImplTest.TYPE,
            MetadataInfoImplTest.LAST_MODIFIED,
            MetadataInfoImplTest.SIZE);

    persistable2.setId(MetadataInfoImplTest.ID);

    Assert.assertThat(persistable.equals(persistable2), Matchers.equalTo(true));
  }

  @Test
  public void testEqualsWhenIdentical() throws Exception {
    Assert.assertThat(persistable.equals(persistable), Matchers.equalTo(true));
  }

  @SuppressWarnings("PMD.EqualsNull" /* purposely testing equals() when called with null */)
  @Test
  public void testEqualsWhenNull() throws Exception {
    Assert.assertThat(persistable.equals(null), Matchers.equalTo(false));
  }

  @SuppressWarnings(
      "PMD.PositionLiteralsFirstInComparisons" /* purposely testing equals() when call with
                                               something else than expected */)
  @Test
  public void testEqualsWhenNotAMetadataInfoPojo() throws Exception {
    Assert.assertThat(persistable.equals("test"), Matchers.equalTo(false));
  }

  @Test
  public void testEqualsWhenIdIsDifferent() throws Exception {
    final MetadataInfoImpl persistable2 =
        new MetadataInfoImpl(
            MetadataInfoImplTest.TYPE,
            MetadataInfoImplTest.LAST_MODIFIED,
            MetadataInfoImplTest.SIZE);

    persistable2.setId(MetadataInfoImplTest.ID + "2");

    Assert.assertThat(persistable.equals(persistable2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenTypeIsDifferent() throws Exception {
    final MetadataInfoImpl persistable2 =
        new MetadataInfoImpl(
            MetadataInfoImplTest.TYPE + "2",
            MetadataInfoImplTest.LAST_MODIFIED,
            MetadataInfoImplTest.SIZE);

    persistable2.setId(MetadataInfoImplTest.ID);

    Assert.assertThat(persistable.equals(persistable2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenlastModifiedIsDifferent() throws Exception {
    final MetadataInfoImpl persistable2 =
        new MetadataInfoImpl(
            MetadataInfoImplTest.TYPE,
            MetadataInfoImplTest.LAST_MODIFIED.plusMillis(2L),
            MetadataInfoImplTest.SIZE);

    persistable2.setId(MetadataInfoImplTest.ID);

    Assert.assertThat(persistable.equals(persistable2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenSizeIsDifferent() throws Exception {
    final MetadataInfoImpl persistable2 =
        new MetadataInfoImpl(
            MetadataInfoImplTest.TYPE,
            MetadataInfoImplTest.LAST_MODIFIED,
            MetadataInfoImplTest.SIZE + 2L);

    persistable2.setId(MetadataInfoImplTest.ID);

    Assert.assertThat(persistable.equals(persistable2), Matchers.not(Matchers.equalTo(true)));
  }
}
