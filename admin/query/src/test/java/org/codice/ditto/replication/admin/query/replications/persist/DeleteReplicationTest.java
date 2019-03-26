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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.codice.ddf.admin.api.report.ErrorMessage;
import org.codice.ddf.admin.api.report.FunctionReport;
import org.codice.ditto.replication.admin.query.ReplicationMessages;
import org.codice.ditto.replication.admin.query.ReplicationUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DeleteReplicationTest {

  private static final String ID = "abc123";

  DeleteReplication deleteReplication;

  Map<String, Object> args;

  @Mock ReplicationUtils utils;

  @Before
  public void setup() {
    deleteReplication = new DeleteReplication(utils);
    args = new HashMap<>();
    args.put("id", ID);
  }

  @Test
  public void testValidateNoConfig() {
    when(utils.configExists(ID)).thenReturn(false);
    FunctionReport report =
        deleteReplication.execute(args, Collections.singletonList(DeleteReplication.FIELD_NAME));
    assertThat(report.getErrorMessages().size(), is(1));
    assertThat(
        ((ErrorMessage) report.getErrorMessages().get(0)).getCode(),
        is(ReplicationMessages.CONFIG_DOES_NOT_EXIST));
  }

  @Test
  public void testGetFunctionErrorCodes() {
    assertThat(
        deleteReplication
            .getFunctionErrorCodes()
            .contains(ReplicationMessages.CONFIG_DOES_NOT_EXIST),
        is(true));
  }

  @Test
  public void testDeleteDataArgDefaultsToFalse() {
    when(utils.configExists(ID)).thenReturn(true);
    FunctionReport report =
        deleteReplication.execute(args, Collections.singletonList(DeleteReplication.FIELD_NAME));
    assertThat(report.getErrorMessages().size(), is(0));
    verify(utils, times(1)).markConfigDeleted(ID, false);
  }
}
