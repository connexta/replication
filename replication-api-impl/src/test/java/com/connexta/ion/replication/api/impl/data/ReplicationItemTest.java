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
package com.connexta.ion.replication.api.impl.data;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.connexta.ion.replication.api.Action;
import com.connexta.ion.replication.api.ReplicationItem;
import com.connexta.ion.replication.api.Status;
import com.jparams.verifier.tostring.NameStyle;
import com.jparams.verifier.tostring.ToStringVerifier;
import java.util.Date;
import org.junit.Test;

public class ReplicationItemTest {

  private static final String ID = "id";

  private static final String CONFIG_ID = "configId";

  private static final String SOURCE = "source";

  private static final String DESTINATION = "destination";

  private static final Date METADATA_MODIFIED = new Date();

  private static final Action ACTION = Action.CREATE;

  private static final Status STATUS = Status.SUCCESS;

  private ReplicationItem replicationItem;

  @Test(expected = IllegalArgumentException.class)
  public void testBuilderIdRequired() {
    new ReplicationItemImpl.Builder("", CONFIG_ID, SOURCE, DESTINATION).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuilderConfigIdRequired() {
    new ReplicationItemImpl.Builder(ID, "", SOURCE, DESTINATION).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuilderSourceRequired() {
    new ReplicationItemImpl.Builder(ID, CONFIG_ID, "", DESTINATION).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuilderDestinationRequired() {
    new ReplicationItemImpl.Builder(ID, CONFIG_ID, SOURCE, "").build();
  }

  @Test(expected = NullPointerException.class)
  public void testBuilderMetadataModifiedRequired() {
    new ReplicationItemImpl.Builder(ID, CONFIG_ID, SOURCE, DESTINATION)
        .markStartTime()
        .markDoneTime()
        .action(ACTION)
        .status(STATUS)
        .build();
  }

  @Test(expected = NullPointerException.class)
  public void testBuilderStartTimeRequired() {
    new ReplicationItemImpl.Builder(ID, CONFIG_ID, SOURCE, DESTINATION)
        .metadataModified(METADATA_MODIFIED)
        .action(ACTION)
        .status(STATUS)
        .build();
  }

  @Test(expected = NullPointerException.class)
  public void testBuilderDoneTimeRequired() {
    new ReplicationItemImpl.Builder(ID, CONFIG_ID, SOURCE, DESTINATION)
        .metadataModified(METADATA_MODIFIED)
        .markStartTime()
        .action(ACTION)
        .status(STATUS)
        .build();
  }

  @Test(expected = NullPointerException.class)
  public void testBuilderActionRequired() {
    new ReplicationItemImpl.Builder(ID, CONFIG_ID, SOURCE, DESTINATION)
        .metadataModified(METADATA_MODIFIED)
        .markStartTime()
        .markDoneTime()
        .status(STATUS)
        .build();
  }

  @Test(expected = NullPointerException.class)
  public void testBuilderStatusRequired() {
    new ReplicationItemImpl.Builder(ID, CONFIG_ID, SOURCE, DESTINATION)
        .metadataModified(METADATA_MODIFIED)
        .markStartTime()
        .markDoneTime()
        .action(ACTION)
        .build();
  }

  @Test(expected = IllegalStateException.class)
  public void testBuilderSettingDoneTimeBeforeStartTime() {
    new ReplicationItemImpl.Builder(ID, CONFIG_ID, SOURCE, DESTINATION)
        .metadataModified(METADATA_MODIFIED)
        .markDoneTime();
  }

  @Test
  public void testResourceTransferRate() {
    replicationItem = new TestReplicationItem();
    assertThat(replicationItem.getResourceTransferRate(), is(.001));
  }

  @Test(expected = IllegalStateException.class)
  public void testResourceSizeRequiresResourceModified() {
    new ReplicationItemImpl.Builder(ID, CONFIG_ID, SOURCE, DESTINATION)
        .metadataModified(METADATA_MODIFIED)
        .markStartTime()
        .markDoneTime()
        .action(ACTION)
        .status(STATUS)
        .resourceModified(new Date())
        .build();
  }

  @Test(expected = IllegalStateException.class)
  public void testResourceModifiedRequiresResourceSize() {
    new ReplicationItemImpl.Builder(ID, CONFIG_ID, SOURCE, DESTINATION)
        .metadataModified(METADATA_MODIFIED)
        .markStartTime()
        .markDoneTime()
        .action(ACTION)
        .status(STATUS)
        .resourceSize(10)
        .build();
  }

  @Test
  public void testResourceTransferRateWithoutResource() {
    replicationItem =
        new ReplicationItemImpl.Builder(ID, CONFIG_ID, SOURCE, DESTINATION)
            .metadataModified(METADATA_MODIFIED)
            .markStartTime()
            .markDoneTime()
            .action(ACTION)
            .status(STATUS)
            .build();
    assertThat(replicationItem.getResourceTransferRate(), is(0.0D));
  }

  @Test
  public void testToString() {
    ToStringVerifier.forClass(ReplicationItemImpl.class)
        .withClassName(NameStyle.SIMPLE_NAME)
        .verify();
  }

  class TestReplicationItem extends ReplicationItemImpl {

    @Override
    public long getDuration() {
      return 5;
    }

    @Override
    public long getResourceSize() {
      return 5;
    }
  }
}
