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
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.codice.ddf.admin.api.report.ErrorMessage;
import org.codice.ddf.admin.api.report.FunctionReport;
import org.codice.ditto.replication.admin.query.ReplicationMessages;
import org.codice.ditto.replication.admin.query.ReplicationUtils;
import org.codice.ditto.replication.admin.query.replications.fields.ReplicationField;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CreateReplicationTest {
  CreateReplication create;

  Map<String, Object> input;

  @Mock ReplicationUtils utils;

  @Before
  public void setUp() throws Exception {
    create = new CreateReplication(utils);
    when(utils.createReplication(anyString(), anyString(), anyString(), anyString(), anyBoolean()))
        .thenReturn(new ReplicationField());
    input = new HashMap<>();
    input.put("id", "myid");
    input.put("name", "myname");
    input.put("filter", "title like '*'");
    input.put("sourceId", "srcId");
    input.put("destinationId", "destId");
    input.put("biDirectional", false);
  }

  @Test
  public void validate() {
    FunctionReport report = create.execute(input, null);
    assertThat(report.getErrorMessages().size(), is(0));
  }

  @Test
  public void validateDupConfig() {
    when(utils.replicationConfigExists(anyString())).thenReturn(true);
    FunctionReport report = create.execute(input, null);
    assertThat(report.getErrorMessages().size(), is(1));
    assertThat(
        ((ErrorMessage) report.getErrorMessages().get(0)).getCode(),
        is(ReplicationMessages.DUPLICATE_CONFIGURATION));
  }

  @Test
  public void validateSameSource() {
    input.put("destinationId", "srcId");
    FunctionReport report = create.execute(input, null);
    assertThat(report.getErrorMessages().size(), is(1));
    assertThat(
        ((ErrorMessage) report.getErrorMessages().get(0)).getCode(),
        is(ReplicationMessages.SAME_SITE));
  }

  @Test
  public void validateBadCql() {
    input.put("filter", "asdf");
    FunctionReport report = create.execute(input, null);
    assertThat(report.getErrorMessages().size(), is(1));
    assertThat(
        ((ErrorMessage) report.getErrorMessages().get(0)).getCode(),
        is(ReplicationMessages.INVALID_FILTER));
  }
}
