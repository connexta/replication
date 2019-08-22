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

import com.connexta.replication.api.Action;
import com.connexta.replication.api.Status;
import java.util.Date;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class ItemPojoTest {
  private static final int VERSION = 1;
  private static final String ID = "1234";
  private static final String METADATA_ID = "m123";
  private static final Date RESOURCE_MODIFIED = new Date(1L);
  private static final Date METADATA_MODIFIED = new Date(2L);
  private static final Date DONE_TIME = new Date(3L);
  private static final String SOURCE = "source";
  private static final String DESTINATION = "destination";
  private static final String CONFIG_ID = "config.id";
  private static final long METADATA_SIZE = 123L;
  private static final long RESOURCE_SIZE = 234L;
  private static final Date START_TIME = new Date(4L);
  private static final String STATUS = Status.FAILURE.name();
  private static final String ACTION = Action.DELETE.name();

  private static final ItemPojo POJO =
      new ItemPojo()
          .setVersion(ItemPojoTest.VERSION)
          .setId(ItemPojoTest.ID)
          .setMetadataId(ItemPojoTest.METADATA_ID)
          .setResourceModified(ItemPojoTest.RESOURCE_MODIFIED)
          .setMetadataModified(ItemPojoTest.METADATA_MODIFIED)
          .setDoneTime(ItemPojoTest.DONE_TIME)
          .setSource(ItemPojoTest.SOURCE)
          .setDestination(ItemPojoTest.DESTINATION)
          .setConfigId(ItemPojoTest.CONFIG_ID)
          .setMetadataSize(ItemPojoTest.METADATA_SIZE)
          .setResourceSize(ItemPojoTest.RESOURCE_SIZE)
          .setStartTime(ItemPojoTest.START_TIME)
          .setStatus(ItemPojoTest.STATUS)
          .setAction(ItemPojoTest.ACTION);

  @Test
  public void testSetAndGetId() throws Exception {
    final ItemPojo pojo = new ItemPojo().setId(ItemPojoTest.ID);

    Assert.assertThat(pojo.getId(), Matchers.equalTo(ItemPojoTest.ID));
  }

  @Test
  public void testSetAndGetVersion() throws Exception {
    final ItemPojo pojo = new ItemPojo().setVersion(ItemPojoTest.VERSION);

    Assert.assertThat(pojo.getVersion(), Matchers.equalTo(ItemPojoTest.VERSION));
  }

  @Test
  public void testSetAndGetMetadataId() throws Exception {
    final ItemPojo pojo = new ItemPojo().setMetadataId(ItemPojoTest.METADATA_ID);

    Assert.assertThat(pojo.getMetadataId(), Matchers.equalTo(ItemPojoTest.METADATA_ID));
  }

  @Test
  public void testSetAndGetResourceModified() throws Exception {
    final ItemPojo pojo = new ItemPojo().setResourceModified(ItemPojoTest.RESOURCE_MODIFIED);

    Assert.assertThat(pojo.getResourceModified(), Matchers.equalTo(ItemPojoTest.RESOURCE_MODIFIED));
  }

  @Test
  public void testSetAndGetMetadataModified() throws Exception {
    final ItemPojo pojo = new ItemPojo().setMetadataModified(ItemPojoTest.METADATA_MODIFIED);

    Assert.assertThat(pojo.getMetadataModified(), Matchers.equalTo(ItemPojoTest.METADATA_MODIFIED));
  }

  @Test
  public void testSetAndGetDoneTime() throws Exception {
    final ItemPojo pojo = new ItemPojo().setDoneTime(ItemPojoTest.DONE_TIME);

    Assert.assertThat(pojo.getDoneTime(), Matchers.equalTo(ItemPojoTest.DONE_TIME));
  }

  @Test
  public void testSetAndGetSource() throws Exception {
    final ItemPojo pojo = new ItemPojo().setSource(ItemPojoTest.SOURCE);

    Assert.assertThat(pojo.getSource(), Matchers.equalTo(ItemPojoTest.SOURCE));
  }

  @Test
  public void testSetAndGetDestination() throws Exception {
    final ItemPojo pojo = new ItemPojo().setDestination(ItemPojoTest.DESTINATION);

    Assert.assertThat(pojo.getDestination(), Matchers.equalTo(ItemPojoTest.DESTINATION));
  }

  @Test
  public void testSetAndGetConfigId() throws Exception {
    final ItemPojo pojo = new ItemPojo().setConfigId(ItemPojoTest.CONFIG_ID);

    Assert.assertThat(pojo.getConfigId(), Matchers.equalTo(ItemPojoTest.CONFIG_ID));
  }

  @Test
  public void testSetAndGetMetadataSize() throws Exception {
    final ItemPojo pojo = new ItemPojo().setMetadataSize(ItemPojoTest.METADATA_SIZE);

    Assert.assertThat(pojo.getMetadataSize(), Matchers.equalTo(ItemPojoTest.METADATA_SIZE));
  }

  @Test
  public void testSetAndGetResourceSize() throws Exception {
    final ItemPojo pojo = new ItemPojo().setResourceSize(ItemPojoTest.RESOURCE_SIZE);

    Assert.assertThat(pojo.getResourceSize(), Matchers.equalTo(ItemPojoTest.RESOURCE_SIZE));
  }

  @Test
  public void testSetAndGetStartTime() throws Exception {
    final ItemPojo pojo = new ItemPojo().setStartTime(ItemPojoTest.START_TIME);

    Assert.assertThat(pojo.getStartTime(), Matchers.equalTo(ItemPojoTest.START_TIME));
  }

  @Test
  public void testSetAndGetStatus() throws Exception {
    final ItemPojo pojo = new ItemPojo().setStatus(ItemPojoTest.STATUS);

    Assert.assertThat(pojo.getStatus(), Matchers.equalTo(ItemPojoTest.STATUS));
  }

  @Test
  public void testHashCodeWhenEquals() throws Exception {
    final ItemPojo pojo2 =
        new ItemPojo()
            .setVersion(ItemPojoTest.VERSION)
            .setId(ItemPojoTest.ID)
            .setMetadataId(ItemPojoTest.METADATA_ID)
            .setResourceModified(ItemPojoTest.RESOURCE_MODIFIED)
            .setMetadataModified(ItemPojoTest.METADATA_MODIFIED)
            .setDoneTime(ItemPojoTest.DONE_TIME)
            .setSource(ItemPojoTest.SOURCE)
            .setDestination(ItemPojoTest.DESTINATION)
            .setConfigId(ItemPojoTest.CONFIG_ID)
            .setMetadataSize(ItemPojoTest.METADATA_SIZE)
            .setResourceSize(ItemPojoTest.RESOURCE_SIZE)
            .setStartTime(ItemPojoTest.START_TIME)
            .setStatus(ItemPojoTest.STATUS)
            .setAction(ItemPojoTest.ACTION);

    Assert.assertThat(ItemPojoTest.POJO.hashCode(), Matchers.equalTo(pojo2.hashCode()));
  }

  @Test
  public void testHashCodeWhenDifferent() throws Exception {
    final ItemPojo pojo2 =
        new ItemPojo()
            .setVersion(ItemPojoTest.VERSION)
            .setId(ItemPojoTest.ID + "2")
            .setMetadataId(ItemPojoTest.METADATA_ID)
            .setResourceModified(ItemPojoTest.RESOURCE_MODIFIED)
            .setMetadataModified(ItemPojoTest.METADATA_MODIFIED)
            .setDoneTime(ItemPojoTest.DONE_TIME)
            .setSource(ItemPojoTest.SOURCE)
            .setDestination(ItemPojoTest.DESTINATION)
            .setConfigId(ItemPojoTest.CONFIG_ID)
            .setMetadataSize(ItemPojoTest.METADATA_SIZE)
            .setResourceSize(ItemPojoTest.RESOURCE_SIZE)
            .setStartTime(ItemPojoTest.START_TIME)
            .setStatus(ItemPojoTest.STATUS)
            .setAction(ItemPojoTest.ACTION);

    Assert.assertThat(
        ItemPojoTest.POJO.hashCode(), Matchers.not(Matchers.equalTo(pojo2.hashCode())));
  }

  @Test
  public void testEqualsWhenEquals() throws Exception {
    final ItemPojo pojo2 =
        new ItemPojo()
            .setVersion(ItemPojoTest.VERSION)
            .setId(ItemPojoTest.ID)
            .setMetadataId(ItemPojoTest.METADATA_ID)
            .setResourceModified(ItemPojoTest.RESOURCE_MODIFIED)
            .setMetadataModified(ItemPojoTest.METADATA_MODIFIED)
            .setDoneTime(ItemPojoTest.DONE_TIME)
            .setSource(ItemPojoTest.SOURCE)
            .setDestination(ItemPojoTest.DESTINATION)
            .setConfigId(ItemPojoTest.CONFIG_ID)
            .setMetadataSize(ItemPojoTest.METADATA_SIZE)
            .setResourceSize(ItemPojoTest.RESOURCE_SIZE)
            .setStartTime(ItemPojoTest.START_TIME)
            .setStatus(ItemPojoTest.STATUS)
            .setAction(ItemPojoTest.ACTION);

    Assert.assertThat(ItemPojoTest.POJO.equals(pojo2), Matchers.equalTo(true));
  }

  @Test
  public void testEqualsWhenIdentical() throws Exception {
    Assert.assertThat(ItemPojoTest.POJO.equals(ItemPojoTest.POJO), Matchers.equalTo(true));
  }

  @SuppressWarnings("PMD.EqualsNull" /* purposely testing equals() when called with null */)
  @Test
  public void testEqualsWhenNull() throws Exception {
    Assert.assertThat(ItemPojoTest.POJO.equals(null), Matchers.equalTo(false));
  }

  @SuppressWarnings(
      "PMD.PositionLiteralsFirstInComparisons" /* purposely testing equals() when call with something else than expected */)
  @Test
  public void testEqualsWhenNotAReplicationItemPojo() throws Exception {
    Assert.assertThat(ItemPojoTest.POJO.equals("test"), Matchers.equalTo(false));
  }

  @Test
  public void testEqualsWhenIdIsDifferent() throws Exception {
    final ItemPojo pojo2 =
        new ItemPojo()
            .setVersion(ItemPojoTest.VERSION)
            .setId(ItemPojoTest.ID + "2")
            .setMetadataId(ItemPojoTest.METADATA_ID)
            .setResourceModified(ItemPojoTest.RESOURCE_MODIFIED)
            .setMetadataModified(ItemPojoTest.METADATA_MODIFIED)
            .setDoneTime(ItemPojoTest.DONE_TIME)
            .setSource(ItemPojoTest.SOURCE)
            .setDestination(ItemPojoTest.DESTINATION)
            .setConfigId(ItemPojoTest.CONFIG_ID)
            .setMetadataSize(ItemPojoTest.METADATA_SIZE)
            .setResourceSize(ItemPojoTest.RESOURCE_SIZE)
            .setStartTime(ItemPojoTest.START_TIME)
            .setStatus(ItemPojoTest.STATUS)
            .setAction(ItemPojoTest.ACTION);

    Assert.assertThat(ItemPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenVersionIsDifferent() throws Exception {
    final ItemPojo pojo2 =
        new ItemPojo()
            .setVersion(ItemPojoTest.VERSION + 2)
            .setId(ItemPojoTest.ID)
            .setMetadataId(ItemPojoTest.METADATA_ID)
            .setResourceModified(ItemPojoTest.RESOURCE_MODIFIED)
            .setMetadataModified(ItemPojoTest.METADATA_MODIFIED)
            .setDoneTime(ItemPojoTest.DONE_TIME)
            .setSource(ItemPojoTest.SOURCE)
            .setDestination(ItemPojoTest.DESTINATION)
            .setConfigId(ItemPojoTest.CONFIG_ID)
            .setMetadataSize(ItemPojoTest.METADATA_SIZE)
            .setResourceSize(ItemPojoTest.RESOURCE_SIZE)
            .setStartTime(ItemPojoTest.START_TIME)
            .setStatus(ItemPojoTest.STATUS)
            .setAction(ItemPojoTest.ACTION);

    Assert.assertThat(ItemPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenMetadataIdRemoteManagedIsDifferent() throws Exception {
    final ItemPojo pojo2 =
        new ItemPojo()
            .setVersion(ItemPojoTest.VERSION)
            .setId(ItemPojoTest.ID)
            .setMetadataId(ItemPojoTest.METADATA_ID + "2")
            .setResourceModified(ItemPojoTest.RESOURCE_MODIFIED)
            .setMetadataModified(ItemPojoTest.METADATA_MODIFIED)
            .setDoneTime(ItemPojoTest.DONE_TIME)
            .setSource(ItemPojoTest.SOURCE)
            .setDestination(ItemPojoTest.DESTINATION)
            .setConfigId(ItemPojoTest.CONFIG_ID)
            .setMetadataSize(ItemPojoTest.METADATA_SIZE)
            .setResourceSize(ItemPojoTest.RESOURCE_SIZE)
            .setStartTime(ItemPojoTest.START_TIME)
            .setStatus(ItemPojoTest.STATUS)
            .setAction(ItemPojoTest.ACTION);

    Assert.assertThat(ItemPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenResourceModifiedIsDifferent() throws Exception {
    final ItemPojo pojo2 =
        new ItemPojo()
            .setVersion(ItemPojoTest.VERSION)
            .setId(ItemPojoTest.ID)
            .setMetadataId(ItemPojoTest.METADATA_ID)
            .setResourceModified(new Date())
            .setMetadataModified(ItemPojoTest.METADATA_MODIFIED)
            .setDoneTime(ItemPojoTest.DONE_TIME)
            .setSource(ItemPojoTest.SOURCE)
            .setDestination(ItemPojoTest.DESTINATION)
            .setConfigId(ItemPojoTest.CONFIG_ID)
            .setMetadataSize(ItemPojoTest.METADATA_SIZE)
            .setResourceSize(ItemPojoTest.RESOURCE_SIZE)
            .setStartTime(ItemPojoTest.START_TIME)
            .setStatus(ItemPojoTest.STATUS)
            .setAction(ItemPojoTest.ACTION);

    Assert.assertThat(ItemPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenMetadataModifiedIsDifferent() throws Exception {
    final ItemPojo pojo2 =
        new ItemPojo()
            .setVersion(ItemPojoTest.VERSION)
            .setId(ItemPojoTest.ID)
            .setMetadataId(ItemPojoTest.METADATA_ID)
            .setResourceModified(ItemPojoTest.RESOURCE_MODIFIED)
            .setMetadataModified(new Date())
            .setDoneTime(ItemPojoTest.DONE_TIME)
            .setSource(ItemPojoTest.SOURCE)
            .setDestination(ItemPojoTest.DESTINATION)
            .setConfigId(ItemPojoTest.CONFIG_ID)
            .setMetadataSize(ItemPojoTest.METADATA_SIZE)
            .setResourceSize(ItemPojoTest.RESOURCE_SIZE)
            .setStartTime(ItemPojoTest.START_TIME)
            .setStatus(ItemPojoTest.STATUS)
            .setAction(ItemPojoTest.ACTION);

    Assert.assertThat(ItemPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenDoneTimeIsDifferent() throws Exception {
    final ItemPojo pojo2 =
        new ItemPojo()
            .setVersion(ItemPojoTest.VERSION)
            .setId(ItemPojoTest.ID)
            .setMetadataId(ItemPojoTest.METADATA_ID)
            .setResourceModified(ItemPojoTest.RESOURCE_MODIFIED)
            .setMetadataModified(ItemPojoTest.METADATA_MODIFIED)
            .setDoneTime(new Date())
            .setSource(ItemPojoTest.SOURCE)
            .setDestination(ItemPojoTest.DESTINATION)
            .setConfigId(ItemPojoTest.CONFIG_ID)
            .setMetadataSize(ItemPojoTest.METADATA_SIZE)
            .setResourceSize(ItemPojoTest.RESOURCE_SIZE)
            .setStartTime(ItemPojoTest.START_TIME)
            .setStatus(ItemPojoTest.STATUS)
            .setAction(ItemPojoTest.ACTION);

    Assert.assertThat(ItemPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenSourceIsDifferent() throws Exception {
    final ItemPojo pojo2 =
        new ItemPojo()
            .setVersion(ItemPojoTest.VERSION)
            .setId(ItemPojoTest.ID)
            .setMetadataId(ItemPojoTest.METADATA_ID)
            .setResourceModified(ItemPojoTest.RESOURCE_MODIFIED)
            .setMetadataModified(ItemPojoTest.METADATA_MODIFIED)
            .setDoneTime(ItemPojoTest.DONE_TIME)
            .setSource(ItemPojoTest.SOURCE + "2")
            .setDestination(ItemPojoTest.DESTINATION)
            .setConfigId(ItemPojoTest.CONFIG_ID)
            .setMetadataSize(ItemPojoTest.METADATA_SIZE)
            .setResourceSize(ItemPojoTest.RESOURCE_SIZE)
            .setStartTime(ItemPojoTest.START_TIME)
            .setStatus(ItemPojoTest.STATUS)
            .setAction(ItemPojoTest.ACTION);

    Assert.assertThat(ItemPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenDestinationIsDifferent() throws Exception {
    final ItemPojo pojo2 =
        new ItemPojo()
            .setVersion(ItemPojoTest.VERSION)
            .setId(ItemPojoTest.ID)
            .setMetadataId(ItemPojoTest.METADATA_ID)
            .setResourceModified(ItemPojoTest.RESOURCE_MODIFIED)
            .setMetadataModified(ItemPojoTest.METADATA_MODIFIED)
            .setDoneTime(ItemPojoTest.DONE_TIME)
            .setSource(ItemPojoTest.SOURCE)
            .setDestination(ItemPojoTest.DESTINATION + "2")
            .setConfigId(ItemPojoTest.CONFIG_ID)
            .setMetadataSize(ItemPojoTest.METADATA_SIZE)
            .setResourceSize(ItemPojoTest.RESOURCE_SIZE)
            .setStartTime(ItemPojoTest.START_TIME)
            .setStatus(ItemPojoTest.STATUS)
            .setAction(ItemPojoTest.ACTION);

    Assert.assertThat(ItemPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenConfigIdIsDifferent() throws Exception {
    final ItemPojo pojo2 =
        new ItemPojo()
            .setVersion(ItemPojoTest.VERSION)
            .setId(ItemPojoTest.ID)
            .setMetadataId(ItemPojoTest.METADATA_ID)
            .setResourceModified(ItemPojoTest.RESOURCE_MODIFIED)
            .setMetadataModified(ItemPojoTest.METADATA_MODIFIED)
            .setDoneTime(ItemPojoTest.DONE_TIME)
            .setSource(ItemPojoTest.SOURCE)
            .setDestination(ItemPojoTest.DESTINATION)
            .setConfigId(ItemPojoTest.CONFIG_ID + "2")
            .setMetadataSize(ItemPojoTest.METADATA_SIZE)
            .setResourceSize(ItemPojoTest.RESOURCE_SIZE)
            .setStartTime(ItemPojoTest.START_TIME)
            .setStatus(ItemPojoTest.STATUS)
            .setAction(ItemPojoTest.ACTION);

    Assert.assertThat(ItemPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenMetadataSizeIsDifferent() throws Exception {
    final ItemPojo pojo2 =
        new ItemPojo()
            .setVersion(ItemPojoTest.VERSION)
            .setId(ItemPojoTest.ID)
            .setMetadataId(ItemPojoTest.METADATA_ID)
            .setResourceModified(ItemPojoTest.RESOURCE_MODIFIED)
            .setMetadataModified(ItemPojoTest.METADATA_MODIFIED)
            .setDoneTime(ItemPojoTest.DONE_TIME)
            .setSource(ItemPojoTest.SOURCE)
            .setDestination(ItemPojoTest.DESTINATION)
            .setConfigId(ItemPojoTest.CONFIG_ID)
            .setMetadataSize(ItemPojoTest.METADATA_SIZE + 2L)
            .setResourceSize(ItemPojoTest.RESOURCE_SIZE)
            .setStartTime(ItemPojoTest.START_TIME)
            .setStatus(ItemPojoTest.STATUS)
            .setAction(ItemPojoTest.ACTION);

    Assert.assertThat(ItemPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenResourceSizeIsDifferent() throws Exception {
    final ItemPojo pojo2 =
        new ItemPojo()
            .setVersion(ItemPojoTest.VERSION)
            .setId(ItemPojoTest.ID)
            .setMetadataId(ItemPojoTest.METADATA_ID)
            .setResourceModified(ItemPojoTest.RESOURCE_MODIFIED)
            .setMetadataModified(ItemPojoTest.METADATA_MODIFIED)
            .setDoneTime(ItemPojoTest.DONE_TIME)
            .setSource(ItemPojoTest.SOURCE)
            .setDestination(ItemPojoTest.DESTINATION)
            .setConfigId(ItemPojoTest.CONFIG_ID)
            .setMetadataSize(ItemPojoTest.METADATA_SIZE)
            .setResourceSize(ItemPojoTest.RESOURCE_SIZE + 2L)
            .setStartTime(ItemPojoTest.START_TIME)
            .setStatus(ItemPojoTest.STATUS)
            .setAction(ItemPojoTest.ACTION);

    Assert.assertThat(ItemPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenStartTimeIsDifferent() throws Exception {
    final ItemPojo pojo2 =
        new ItemPojo()
            .setVersion(ItemPojoTest.VERSION)
            .setId(ItemPojoTest.ID)
            .setMetadataId(ItemPojoTest.METADATA_ID)
            .setResourceModified(ItemPojoTest.RESOURCE_MODIFIED)
            .setMetadataModified(ItemPojoTest.METADATA_MODIFIED)
            .setDoneTime(ItemPojoTest.DONE_TIME)
            .setSource(ItemPojoTest.SOURCE)
            .setDestination(ItemPojoTest.DESTINATION)
            .setConfigId(ItemPojoTest.CONFIG_ID)
            .setMetadataSize(ItemPojoTest.METADATA_SIZE)
            .setResourceSize(ItemPojoTest.RESOURCE_SIZE)
            .setStartTime(new Date())
            .setStatus(ItemPojoTest.STATUS)
            .setAction(ItemPojoTest.ACTION);

    Assert.assertThat(ItemPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenStatusIsDifferent() throws Exception {
    final ItemPojo pojo2 =
        new ItemPojo()
            .setVersion(ItemPojoTest.VERSION)
            .setId(ItemPojoTest.ID)
            .setMetadataId(ItemPojoTest.METADATA_ID)
            .setResourceModified(ItemPojoTest.RESOURCE_MODIFIED)
            .setMetadataModified(ItemPojoTest.METADATA_MODIFIED)
            .setDoneTime(ItemPojoTest.DONE_TIME)
            .setSource(ItemPojoTest.SOURCE)
            .setDestination(ItemPojoTest.DESTINATION)
            .setConfigId(ItemPojoTest.CONFIG_ID)
            .setMetadataSize(ItemPojoTest.METADATA_SIZE)
            .setResourceSize(ItemPojoTest.RESOURCE_SIZE)
            .setStartTime(ItemPojoTest.START_TIME)
            .setStatus(ItemPojoTest.STATUS + "2")
            .setAction(ItemPojoTest.ACTION);

    Assert.assertThat(ItemPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenActionIsDifferent() throws Exception {
    final ItemPojo pojo2 =
        new ItemPojo()
            .setVersion(ItemPojoTest.VERSION)
            .setId(ItemPojoTest.ID)
            .setMetadataId(ItemPojoTest.METADATA_ID)
            .setResourceModified(ItemPojoTest.RESOURCE_MODIFIED)
            .setMetadataModified(ItemPojoTest.METADATA_MODIFIED)
            .setDoneTime(ItemPojoTest.DONE_TIME)
            .setSource(ItemPojoTest.SOURCE)
            .setDestination(ItemPojoTest.DESTINATION)
            .setConfigId(ItemPojoTest.CONFIG_ID)
            .setMetadataSize(ItemPojoTest.METADATA_SIZE)
            .setResourceSize(ItemPojoTest.RESOURCE_SIZE)
            .setStartTime(ItemPojoTest.START_TIME)
            .setStatus(ItemPojoTest.STATUS)
            .setAction(ItemPojoTest.ACTION + "2");

    Assert.assertThat(ItemPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }
}
