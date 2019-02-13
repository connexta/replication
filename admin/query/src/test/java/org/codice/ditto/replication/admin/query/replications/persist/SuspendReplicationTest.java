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
package org.codice.ditto.replication.admin.query.replications.persist;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.NotFoundException;
import org.codice.ddf.admin.api.report.ErrorMessage;
import org.codice.ddf.admin.api.report.FunctionReport;
import org.codice.ddf.admin.common.report.message.DefaultMessages;
import org.codice.ditto.replication.admin.query.ReplicationMessages;
import org.codice.ditto.replication.admin.query.ReplicationUtils;
import org.codice.ditto.replication.api.impl.data.ReplicatorConfigImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SuspendReplicationTest {
  SuspendReplication suspend;

  Map<String, Object> input;

  @Mock ReplicationUtils utils;

  @Before
  public void setUp() {
    suspend = new SuspendReplication(utils);
    input = new HashMap<>();
    input.put("id", "myid");
    input.put("suspend", false);
  }

  @Test
  public void validate() {
    when(utils.getConfigForId("myid")).thenReturn(new ReplicatorConfigImpl());
    FunctionReport report = suspend.execute(input, null);
    assertThat(report.getErrorMessages().size(), is(0));
  }

  @Test
  public void validateNoConfig() {
    when(utils.getConfigForId("myid")).thenThrow(new NotFoundException());
    FunctionReport report = suspend.execute(input, null);
    assertThat(report.getErrorMessages().size(), is(1));
    assertThat(
        ((ErrorMessage) report.getErrorMessages().get(0)).getCode(),
        is(ReplicationMessages.CONFIG_DOES_NOT_EXIST));
  }

  @Test
  public void validateNoId() {
    when(utils.getConfigForId("myid")).thenReturn(new ReplicatorConfigImpl());
    input.clear();
    FunctionReport report = suspend.execute(input, null);
    assertThat(report.getErrorMessages().size(), is(2));
    assertThat(
        ((ErrorMessage) report.getErrorMessages().get(0)).getCode(),
        is(DefaultMessages.MISSING_REQUIRED_FIELD));
    assertThat(
        ((ErrorMessage) report.getErrorMessages().get(1)).getCode(),
        is(DefaultMessages.MISSING_REQUIRED_FIELD));
  }
}
