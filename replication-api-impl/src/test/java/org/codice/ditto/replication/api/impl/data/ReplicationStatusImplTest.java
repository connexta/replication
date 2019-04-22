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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Date;
import java.util.Random;
import org.codice.ditto.replication.api.ReplicationStatus;
import org.junit.Before;
import org.junit.Test;

public class ReplicationStatusImplTest {

  private static final String REPLICATOR_NAME = "replicatorName";

  private ReplicationStatus replicationStatus;

  @Before
  public void setup() {
    replicationStatus = new ReplicationStatusImpl(REPLICATOR_NAME);
  }

  @Test
  public void testNoLastMetadataModifiedReturnsLastSuccess() {
    final Date lastSuccess = new Date();
    replicationStatus.setLastSuccess(lastSuccess);
    assertThat(replicationStatus.getLastMetadataModified(), is(lastSuccess));
  }

  @Test
  public void testLastMetadataModifiedPresent() {
    Random r = new Random();
    final Date lastSuccess = new Date(r.nextInt());
    final Date lastMetadataModified = new Date(r.nextInt());
    replicationStatus.setLastSuccess(lastSuccess);
    replicationStatus.setLastMetadataModified(lastMetadataModified);

    assertThat(replicationStatus.getLastMetadataModified(), is(lastMetadataModified));
  }
}
