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
package com.connexta.replication.api.impl.persistence.pojo;

import java.time.Instant;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class ConfigPojoTest {
  private static final int VERSION = 1;
  private static final String ID = "1234";
  private static final String NAME = "site.name";
  private static final boolean BIDIRECTIONAL = true;
  private static final String SOURCE = "source";
  private static final String DESTINATION = "destination";
  private static final String FILTER = "filter";
  private static final String DESCRIPTION = "description";
  private static final boolean SUSPENDED = false;
  private static final Instant LAST_METADATA_MODIFIED = Instant.ofEpochMilli(1L);

  private static final ConfigPojo POJO =
      new ConfigPojo()
          .setVersion(ConfigPojoTest.VERSION)
          .setId(ConfigPojoTest.ID)
          .setName(ConfigPojoTest.NAME)
          .setBidirectional(ConfigPojoTest.BIDIRECTIONAL)
          .setSource(ConfigPojoTest.SOURCE)
          .setDestination(ConfigPojoTest.DESTINATION)
          .setFilter(ConfigPojoTest.FILTER)
          .setDescription(ConfigPojoTest.DESCRIPTION)
          .setSuspended(ConfigPojoTest.SUSPENDED)
          .setLastMetadataModified(ConfigPojoTest.LAST_METADATA_MODIFIED);

  @Test
  public void testSetAndGetId() throws Exception {
    final ConfigPojo pojo = new ConfigPojo().setId(ConfigPojoTest.ID);

    Assert.assertThat(pojo.getId(), Matchers.equalTo(ConfigPojoTest.ID));
  }

  @Test
  public void testSetAndGetVersion() throws Exception {
    final ConfigPojo pojo = new ConfigPojo().setVersion(ConfigPojoTest.VERSION);

    Assert.assertThat(pojo.getVersion(), Matchers.equalTo(ConfigPojoTest.VERSION));
  }

  @Test
  public void testSetAndGetName() throws Exception {
    final ConfigPojo pojo = new ConfigPojo().setName(ConfigPojoTest.NAME);

    Assert.assertThat(pojo.getName(), Matchers.equalTo(ConfigPojoTest.NAME));
  }

  @Test
  public void testSetAndIsBidirectional() throws Exception {
    final ConfigPojo pojo = new ConfigPojo().setBidirectional(ConfigPojoTest.BIDIRECTIONAL);

    Assert.assertThat(pojo.isBidirectional(), Matchers.equalTo(ConfigPojoTest.BIDIRECTIONAL));
  }

  @Test
  public void testSetAndGetSource() throws Exception {
    final ConfigPojo pojo = new ConfigPojo().setSource(ConfigPojoTest.SOURCE);

    Assert.assertThat(pojo.getSource(), Matchers.equalTo(ConfigPojoTest.SOURCE));
  }

  @Test
  public void testSetAndGetDestination() throws Exception {
    final ConfigPojo pojo = new ConfigPojo().setDestination(ConfigPojoTest.DESTINATION);

    Assert.assertThat(pojo.getDestination(), Matchers.equalTo(ConfigPojoTest.DESTINATION));
  }

  @Test
  public void testSetAndGetFilter() throws Exception {
    final ConfigPojo pojo = new ConfigPojo().setFilter(ConfigPojoTest.FILTER);

    Assert.assertThat(pojo.getFilter(), Matchers.equalTo(ConfigPojoTest.FILTER));
  }

  @Test
  public void testSetAndGetDescription() throws Exception {
    final ConfigPojo pojo = new ConfigPojo().setDescription(ConfigPojoTest.DESCRIPTION);

    Assert.assertThat(pojo.getDescription(), Matchers.equalTo(ConfigPojoTest.DESCRIPTION));
  }

  @Test
  public void testSetAndIsSuspended() throws Exception {
    final ConfigPojo pojo = new ConfigPojo().setSuspended(ConfigPojoTest.SUSPENDED);

    Assert.assertThat(pojo.isSuspended(), Matchers.equalTo(ConfigPojoTest.SUSPENDED));
  }

  @Test
  public void testSetAndGetLastMetadataModified() throws Exception {
    final ConfigPojo pojo =
        new ConfigPojo().setLastMetadataModified(ConfigPojoTest.LAST_METADATA_MODIFIED);

    Assert.assertThat(
        pojo.getLastMetadataModified(), Matchers.equalTo(ConfigPojoTest.LAST_METADATA_MODIFIED));
  }

  @Test
  public void testHashCodeWhenEquals() throws Exception {
    final ConfigPojo pojo2 =
        new ConfigPojo()
            .setVersion(ConfigPojoTest.VERSION)
            .setId(ConfigPojoTest.ID)
            .setName(ConfigPojoTest.NAME)
            .setBidirectional(ConfigPojoTest.BIDIRECTIONAL)
            .setSource(ConfigPojoTest.SOURCE)
            .setDestination(ConfigPojoTest.DESTINATION)
            .setFilter(ConfigPojoTest.FILTER)
            .setDescription(ConfigPojoTest.DESCRIPTION)
            .setSuspended(ConfigPojoTest.SUSPENDED)
            .setLastMetadataModified(ConfigPojoTest.LAST_METADATA_MODIFIED);

    Assert.assertThat(ConfigPojoTest.POJO.hashCode(), Matchers.equalTo(pojo2.hashCode()));
  }

  @Test
  public void testHashCodeWhenDifferent() throws Exception {
    final ConfigPojo pojo2 =
        new ConfigPojo()
            .setVersion(ConfigPojoTest.VERSION)
            .setId(ConfigPojoTest.ID + "2")
            .setName(ConfigPojoTest.NAME)
            .setBidirectional(ConfigPojoTest.BIDIRECTIONAL)
            .setSource(ConfigPojoTest.SOURCE)
            .setDestination(ConfigPojoTest.DESTINATION)
            .setFilter(ConfigPojoTest.FILTER)
            .setDescription(ConfigPojoTest.DESCRIPTION)
            .setSuspended(ConfigPojoTest.SUSPENDED)
            .setLastMetadataModified(ConfigPojoTest.LAST_METADATA_MODIFIED);

    Assert.assertThat(
        ConfigPojoTest.POJO.hashCode(), Matchers.not(Matchers.equalTo(pojo2.hashCode())));
  }

  @Test
  public void testEqualsWhenEquals() throws Exception {
    final ConfigPojo pojo2 =
        new ConfigPojo()
            .setVersion(ConfigPojoTest.VERSION)
            .setId(ConfigPojoTest.ID)
            .setName(ConfigPojoTest.NAME)
            .setBidirectional(ConfigPojoTest.BIDIRECTIONAL)
            .setSource(ConfigPojoTest.SOURCE)
            .setDestination(ConfigPojoTest.DESTINATION)
            .setFilter(ConfigPojoTest.FILTER)
            .setDescription(ConfigPojoTest.DESCRIPTION)
            .setSuspended(ConfigPojoTest.SUSPENDED)
            .setLastMetadataModified(ConfigPojoTest.LAST_METADATA_MODIFIED);

    Assert.assertThat(ConfigPojoTest.POJO.equals(pojo2), Matchers.equalTo(true));
  }

  @Test
  public void testEqualsWhenIdentical() throws Exception {
    Assert.assertThat(ConfigPojoTest.POJO.equals(ConfigPojoTest.POJO), Matchers.equalTo(true));
  }

  @SuppressWarnings("PMD.EqualsNull" /* purposely testing equals() when called with null */)
  @Test
  public void testEqualsWhenNull() throws Exception {
    Assert.assertThat(ConfigPojoTest.POJO.equals(null), Matchers.equalTo(false));
  }

  @SuppressWarnings(
      "PMD.PositionLiteralsFirstInComparisons" /* purposely testing equals() when call with something else than expected */)
  @Test
  public void testEqualsWhenNotAReplicatorConfigPojo() throws Exception {
    Assert.assertThat(ConfigPojoTest.POJO.equals("test"), Matchers.equalTo(false));
  }

  @Test
  public void testEqualsWhenIdIsDifferent() throws Exception {
    final ConfigPojo pojo2 =
        new ConfigPojo()
            .setVersion(ConfigPojoTest.VERSION)
            .setId(ConfigPojoTest.ID + "2")
            .setName(ConfigPojoTest.NAME)
            .setBidirectional(ConfigPojoTest.BIDIRECTIONAL)
            .setSource(ConfigPojoTest.SOURCE)
            .setDestination(ConfigPojoTest.DESTINATION)
            .setFilter(ConfigPojoTest.FILTER)
            .setDescription(ConfigPojoTest.DESCRIPTION)
            .setSuspended(ConfigPojoTest.SUSPENDED)
            .setLastMetadataModified(ConfigPojoTest.LAST_METADATA_MODIFIED);

    Assert.assertThat(ConfigPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenVersionIsDifferent() throws Exception {
    final ConfigPojo pojo2 =
        new ConfigPojo()
            .setVersion(ConfigPojoTest.VERSION + 2)
            .setId(ConfigPojoTest.ID)
            .setName(ConfigPojoTest.NAME)
            .setBidirectional(ConfigPojoTest.BIDIRECTIONAL)
            .setSource(ConfigPojoTest.SOURCE)
            .setDestination(ConfigPojoTest.DESTINATION)
            .setFilter(ConfigPojoTest.FILTER)
            .setDescription(ConfigPojoTest.DESCRIPTION)
            .setSuspended(ConfigPojoTest.SUSPENDED)
            .setLastMetadataModified(ConfigPojoTest.LAST_METADATA_MODIFIED);

    Assert.assertThat(ConfigPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenNameIsDifferent() throws Exception {
    final ConfigPojo pojo2 =
        new ConfigPojo()
            .setVersion(ConfigPojoTest.VERSION)
            .setId(ConfigPojoTest.ID)
            .setName(ConfigPojoTest.NAME + "2")
            .setBidirectional(ConfigPojoTest.BIDIRECTIONAL)
            .setSource(ConfigPojoTest.SOURCE)
            .setDestination(ConfigPojoTest.DESTINATION)
            .setFilter(ConfigPojoTest.FILTER)
            .setDescription(ConfigPojoTest.DESCRIPTION)
            .setSuspended(ConfigPojoTest.SUSPENDED)
            .setLastMetadataModified(ConfigPojoTest.LAST_METADATA_MODIFIED);

    Assert.assertThat(ConfigPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenBiDirectionalIsDifferent() throws Exception {
    final ConfigPojo pojo2 =
        new ConfigPojo()
            .setVersion(ConfigPojoTest.VERSION)
            .setId(ConfigPojoTest.ID)
            .setName(ConfigPojoTest.NAME)
            .setBidirectional(!ConfigPojoTest.BIDIRECTIONAL)
            .setSource(ConfigPojoTest.SOURCE)
            .setDestination(ConfigPojoTest.DESTINATION)
            .setFilter(ConfigPojoTest.FILTER)
            .setDescription(ConfigPojoTest.DESCRIPTION)
            .setSuspended(ConfigPojoTest.SUSPENDED)
            .setLastMetadataModified(ConfigPojoTest.LAST_METADATA_MODIFIED);

    Assert.assertThat(ConfigPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenSourceIsDifferent() throws Exception {
    final ConfigPojo pojo2 =
        new ConfigPojo()
            .setVersion(ConfigPojoTest.VERSION)
            .setId(ConfigPojoTest.ID)
            .setName(ConfigPojoTest.NAME)
            .setBidirectional(ConfigPojoTest.BIDIRECTIONAL)
            .setSource(ConfigPojoTest.SOURCE + "2")
            .setDestination(ConfigPojoTest.DESTINATION)
            .setFilter(ConfigPojoTest.FILTER)
            .setDescription(ConfigPojoTest.DESCRIPTION)
            .setSuspended(ConfigPojoTest.SUSPENDED)
            .setLastMetadataModified(ConfigPojoTest.LAST_METADATA_MODIFIED);

    Assert.assertThat(ConfigPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenDestinationIsDifferent() throws Exception {
    final ConfigPojo pojo2 =
        new ConfigPojo()
            .setVersion(ConfigPojoTest.VERSION)
            .setId(ConfigPojoTest.ID)
            .setName(ConfigPojoTest.NAME)
            .setBidirectional(ConfigPojoTest.BIDIRECTIONAL)
            .setSource(ConfigPojoTest.SOURCE)
            .setDestination(ConfigPojoTest.DESTINATION + "2")
            .setFilter(ConfigPojoTest.FILTER)
            .setDescription(ConfigPojoTest.DESCRIPTION)
            .setSuspended(ConfigPojoTest.SUSPENDED)
            .setLastMetadataModified(ConfigPojoTest.LAST_METADATA_MODIFIED);

    Assert.assertThat(ConfigPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenFilterIsDifferent() throws Exception {
    final ConfigPojo pojo2 =
        new ConfigPojo()
            .setVersion(ConfigPojoTest.VERSION)
            .setId(ConfigPojoTest.ID)
            .setName(ConfigPojoTest.NAME)
            .setBidirectional(ConfigPojoTest.BIDIRECTIONAL)
            .setSource(ConfigPojoTest.SOURCE)
            .setDestination(ConfigPojoTest.DESTINATION)
            .setFilter(ConfigPojoTest.FILTER + "2")
            .setDescription(ConfigPojoTest.DESCRIPTION)
            .setSuspended(ConfigPojoTest.SUSPENDED)
            .setLastMetadataModified(ConfigPojoTest.LAST_METADATA_MODIFIED);

    Assert.assertThat(ConfigPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenDescriptionIsDifferent() throws Exception {
    final ConfigPojo pojo2 =
        new ConfigPojo()
            .setVersion(ConfigPojoTest.VERSION)
            .setId(ConfigPojoTest.ID)
            .setName(ConfigPojoTest.NAME)
            .setBidirectional(ConfigPojoTest.BIDIRECTIONAL)
            .setSource(ConfigPojoTest.SOURCE)
            .setDestination(ConfigPojoTest.DESTINATION)
            .setFilter(ConfigPojoTest.FILTER)
            .setDescription(ConfigPojoTest.DESCRIPTION + "2")
            .setSuspended(ConfigPojoTest.SUSPENDED)
            .setLastMetadataModified(ConfigPojoTest.LAST_METADATA_MODIFIED);

    Assert.assertThat(ConfigPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenSuspendedIsDifferent() throws Exception {
    final ConfigPojo pojo2 =
        new ConfigPojo()
            .setVersion(ConfigPojoTest.VERSION)
            .setId(ConfigPojoTest.ID)
            .setName(ConfigPojoTest.NAME)
            .setBidirectional(ConfigPojoTest.BIDIRECTIONAL)
            .setSource(ConfigPojoTest.SOURCE)
            .setDestination(ConfigPojoTest.DESTINATION)
            .setFilter(ConfigPojoTest.FILTER)
            .setDescription(ConfigPojoTest.DESCRIPTION)
            .setSuspended(!ConfigPojoTest.SUSPENDED)
            .setLastMetadataModified(ConfigPojoTest.LAST_METADATA_MODIFIED);

    Assert.assertThat(ConfigPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenLastMetadataModifiedIsDifferent() throws Exception {
    final ConfigPojo pojo2 =
        new ConfigPojo()
            .setVersion(ConfigPojoTest.VERSION)
            .setId(ConfigPojoTest.ID)
            .setName(ConfigPojoTest.NAME)
            .setBidirectional(ConfigPojoTest.BIDIRECTIONAL)
            .setSource(ConfigPojoTest.SOURCE)
            .setDestination(ConfigPojoTest.DESTINATION)
            .setFilter(ConfigPojoTest.FILTER)
            .setDescription(ConfigPojoTest.DESCRIPTION)
            .setSuspended(ConfigPojoTest.SUSPENDED)
            .setLastMetadataModified(Instant.now());

    Assert.assertThat(ConfigPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }
}
