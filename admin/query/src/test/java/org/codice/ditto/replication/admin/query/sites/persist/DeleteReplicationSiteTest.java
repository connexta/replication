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
package org.codice.ditto.replication.admin.query.sites.persist;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.codice.ddf.admin.api.fields.ListField;
import org.codice.ddf.admin.api.report.FunctionReport;
import org.codice.ddf.admin.common.fields.base.scalar.BooleanField;
import org.codice.ddf.admin.common.report.message.DefaultMessages;
import org.codice.ditto.replication.admin.query.ReplicationMessages;
import org.codice.ditto.replication.admin.query.ReplicationUtils;
import org.codice.ditto.replication.admin.query.replications.fields.ReplicationField;
import org.codice.ditto.replication.admin.query.sites.fields.ReplicationSiteField;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DeleteReplicationSiteTest {

  private static final String FUNCTION_PATH = "deleteReplicationSite";

  private static final String SITE_ID = "siteId";

  private static final String ID = "id";

  private DeleteReplicationSite deleteReplicationSite;

  @Mock ReplicationUtils replicationUtils;

  private Map<String, Object> input;

  @Before
  public void setup() {
    deleteReplicationSite = new DeleteReplicationSite(replicationUtils);

    input = new HashMap<>();
    input.put(ID, SITE_ID);
  }

  @Test
  public void testDeleteSite() {
    // setup
    when(replicationUtils.siteIdExists(SITE_ID)).thenReturn(true);
    when(replicationUtils.deleteSite(SITE_ID)).thenReturn(true);
    when(replicationUtils.getReplications()).thenReturn(new ReplicationField.ListImpl());

    // when
    FunctionReport<BooleanField> report = deleteReplicationSite.execute(input, null);

    // then
    assertThat(report.getErrorMessages().size(), is(0));
    assertThat(report.getResult().getValue(), is(true));
    verify(replicationUtils).deleteSite(SITE_ID);
  }

  @Test
  public void testNoExistingSite() {
    // setup
    when(replicationUtils.siteIdExists(SITE_ID)).thenReturn(false);
    when(replicationUtils.getReplications()).thenReturn(new ReplicationField.ListImpl());

    // when
    FunctionReport<BooleanField> report =
        deleteReplicationSite.execute(input, ImmutableList.of(FUNCTION_PATH));

    // then
    assertThat(report.getErrorMessages().size(), is(1));
    assertThat(report.getErrorMessages().get(0).getCode(), is(DefaultMessages.NO_EXISTING_CONFIG));
    assertThat(report.getErrorMessages().get(0).getPath(), is(ImmutableList.of(FUNCTION_PATH)));
  }

  @Test
  public void testSourceSiteInUseByReplication() {
    // setup
    when(replicationUtils.siteIdExists(SITE_ID)).thenReturn(true);

    ReplicationSiteField replicationSiteField = mock(ReplicationSiteField.class);
    when(replicationSiteField.id()).thenReturn(SITE_ID);

    ReplicationField replicationField = mock(ReplicationField.class);
    when(replicationField.source()).thenReturn(replicationSiteField);
    ListField<ReplicationField> replicationFieldList = mock(ListField.class);
    when(replicationFieldList.getList()).thenReturn(Collections.singletonList(replicationField));
    when(replicationUtils.getReplications()).thenReturn(replicationFieldList);

    // when
    FunctionReport<BooleanField> report =
        deleteReplicationSite.execute(input, ImmutableList.of(FUNCTION_PATH));

    // then
    assertThat(report.getErrorMessages().size(), is(1));
    assertThat(report.getErrorMessages().get(0).getCode(), is(ReplicationMessages.SITE_IN_USE));
    assertThat(report.getErrorMessages().get(0).getPath(), is(ImmutableList.of(FUNCTION_PATH)));
  }

  @Test
  public void testDestinationSiteInUseByReplication() {
    // setup
    when(replicationUtils.siteIdExists(SITE_ID)).thenReturn(true);

    ReplicationSiteField sourceSiteField = mock(ReplicationSiteField.class);
    when(sourceSiteField.id()).thenReturn("someOtherID");

    ReplicationSiteField destinationSiteField = mock(ReplicationSiteField.class);
    when(destinationSiteField.id()).thenReturn(SITE_ID);

    ReplicationField replicationField = mock(ReplicationField.class);
    when(replicationField.source()).thenReturn(sourceSiteField);
    when(replicationField.destination()).thenReturn(destinationSiteField);
    ListField<ReplicationField> replicationFieldList = mock(ListField.class);
    when(replicationFieldList.getList()).thenReturn(Collections.singletonList(replicationField));
    when(replicationUtils.getReplications()).thenReturn(replicationFieldList);

    // when
    FunctionReport<BooleanField> report =
        deleteReplicationSite.execute(input, ImmutableList.of(FUNCTION_PATH));

    // then
    assertThat(report.getErrorMessages().size(), is(1));
    assertThat(report.getErrorMessages().get(0).getCode(), is(ReplicationMessages.SITE_IN_USE));
    assertThat(report.getErrorMessages().get(0).getPath(), is(ImmutableList.of(FUNCTION_PATH)));
  }

  @Test
  public void testFunctionErrorCodes() {
    assertThat(deleteReplicationSite.getFunctionErrorCodes().size(), is(2));
    assertThat(
        deleteReplicationSite.getFunctionErrorCodes(), hasItem(ReplicationMessages.SITE_IN_USE));
    assertThat(
        deleteReplicationSite.getFunctionErrorCodes(), hasItem(DefaultMessages.NO_EXISTING_CONFIG));
  }

  @Test
  public void testMissingRequiredArguments() {
    // when
    FunctionReport<BooleanField> report =
        deleteReplicationSite.execute(null, ImmutableList.of(FUNCTION_PATH));

    // then
    assertThat(report.getErrorMessages().size(), is(1));
    assertThat(
        report.getErrorMessages().get(0).getCode(), is(DefaultMessages.MISSING_REQUIRED_FIELD));
    assertThat(report.getErrorMessages().get(0).getPath(), is(ImmutableList.of(FUNCTION_PATH, ID)));
  }
}
