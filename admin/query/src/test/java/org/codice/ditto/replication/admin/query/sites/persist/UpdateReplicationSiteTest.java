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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codice.ddf.admin.api.report.ErrorMessage;
import org.codice.ddf.admin.api.report.FunctionReport;
import org.codice.ddf.admin.common.fields.common.AddressField;
import org.codice.ddf.admin.common.report.message.DefaultMessages;
import org.codice.ditto.replication.admin.query.ReplicationMessages;
import org.codice.ditto.replication.admin.query.ReplicationUtils;
import org.codice.ditto.replication.admin.query.sites.fields.ReplicationSiteField;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateReplicationSiteTest {

  private static final String FUNCTION_PATH = "updateReplicationSite";

  private static final String ID = "id";

  private static final String NAME = "name";

  private static final String ROOT_CONTEXT = "rootContext";

  private static final String ADDRESS = "address";

  private static final String IS_DISABLED_LOCAL = "isDisabledLocal";

  private static final String SITE_ID = "siteId";

  private static final String SITE_NAME = "siteName";

  private static final String SITE_CONTEXT = "siteContext";

  private static final int SITE_PORT = 1234;

  private static final String SITE_HOSTNAME = "siteHostname";

  private static final boolean SITE_IS_DISABLED_LOCAL = true;

  private UpdateReplicationSite updateReplicationSite;

  @Mock ReplicationUtils replicationUtils;

  private Map<String, Object> input;

  @Before
  public void setup() {
    updateReplicationSite = new UpdateReplicationSite(replicationUtils);

    Map<String, Object> addressField = new HashMap<>();
    Map<String, Object> hostMap = new HashMap<>();
    hostMap.put("hostname", SITE_HOSTNAME);
    hostMap.put("port", SITE_PORT);
    addressField.put("host", hostMap);

    input = new HashMap<>();
    input.put(ID, SITE_ID);
    input.put(NAME, SITE_NAME);
    input.put(ROOT_CONTEXT, SITE_CONTEXT);
    input.put(ADDRESS, addressField);
    input.put(IS_DISABLED_LOCAL, SITE_IS_DISABLED_LOCAL);
  }

  @Test
  public void testUpdateSite() {
    // setup
    when(replicationUtils.siteIdExists(SITE_ID)).thenReturn(true);
    when(replicationUtils.isDuplicateSiteName(SITE_NAME)).thenReturn(false);
    when(replicationUtils.isNotUpdatedSiteName(SITE_ID, SITE_NAME)).thenReturn(false);

    ReplicationSiteField replicationSiteField = mock(ReplicationSiteField.class);
    when(replicationUtils.updateSite(
            anyString(), anyString(), any(AddressField.class), anyString(), anyBoolean()))
        .thenReturn(replicationSiteField);

    ArgumentCaptor<AddressField> addressFieldCaptor = ArgumentCaptor.forClass(AddressField.class);
    ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> contextCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Boolean> disabledLocalCaptor = ArgumentCaptor.forClass(Boolean.class);
    ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);

    // when
    FunctionReport<ReplicationSiteField> report = updateReplicationSite.execute(input, null);

    // then
    assertThat(report.getErrorMessages().size(), is(0));
    assertThat(report.getResult(), is(replicationSiteField));
    verify(replicationUtils)
        .updateSite(
            idCaptor.capture(),
            nameCaptor.capture(),
            addressFieldCaptor.capture(),
            contextCaptor.capture(),
            disabledLocalCaptor.capture());

    AddressField address = addressFieldCaptor.getValue();
    assertThat(address.host().hostname(), is(SITE_HOSTNAME));
    assertThat(address.host().port(), is(SITE_PORT));
    assertThat(idCaptor.getValue(), is(SITE_ID));
    assertThat(nameCaptor.getValue(), is(SITE_NAME));
    assertThat(contextCaptor.getValue(), is(SITE_CONTEXT));
    assertThat(disabledLocalCaptor.getValue(), is(SITE_IS_DISABLED_LOCAL));
  }

  @Test
  public void testDuplicateSiteName() {
    // setup
    when(replicationUtils.isDuplicateSiteName(SITE_NAME)).thenReturn(true);
    when(replicationUtils.siteIdExists(SITE_ID)).thenReturn(true);
    when(replicationUtils.isNotUpdatedSiteName(SITE_ID, SITE_NAME)).thenReturn(false);

    // when
    List<ErrorMessage> errors =
        updateReplicationSite.execute(input, ImmutableList.of(FUNCTION_PATH)).getErrorMessages();

    // then
    assertThat(errors.size(), is(1));
    assertThat(errors.get(0).getCode(), is(ReplicationMessages.DUPLICATE_SITE));
    assertThat(errors.get(0).getPath(), is(ImmutableList.of(FUNCTION_PATH)));
  }

  @Test
  public void testNoExistingSite() {
    // setup
    when(replicationUtils.isDuplicateSiteName(SITE_NAME)).thenReturn(false);
    when(replicationUtils.siteIdExists(SITE_ID)).thenReturn(false);
    when(replicationUtils.isNotUpdatedSiteName(SITE_ID, SITE_NAME)).thenReturn(false);

    // when
    List<ErrorMessage> errors =
        updateReplicationSite.execute(input, ImmutableList.of(FUNCTION_PATH)).getErrorMessages();

    // then
    assertThat(errors.size(), is(1));
    assertThat(errors.get(0).getCode(), is(DefaultMessages.NO_EXISTING_CONFIG));
    assertThat(errors.get(0).getPath(), is(ImmutableList.of(FUNCTION_PATH)));
  }

  @Test
  public void testFunctionErrorCodes() {
    assertThat(updateReplicationSite.getFunctionErrorCodes().size(), is(2));
    assertThat(
        updateReplicationSite.getFunctionErrorCodes(), hasItem(ReplicationMessages.DUPLICATE_SITE));
    assertThat(
        updateReplicationSite.getFunctionErrorCodes(), hasItem(DefaultMessages.NO_EXISTING_CONFIG));
  }

  @Test
  public void testMissingRequiredArguments() {
    // when
    List<ErrorMessage> errors =
        updateReplicationSite.execute(null, ImmutableList.of(FUNCTION_PATH)).getErrorMessages();

    // then
    assertThat(errors.size(), is(1));
    assertThat(errors.get(0).getCode(), is(DefaultMessages.MISSING_REQUIRED_FIELD));
    assertThat(errors.get(0).getPath(), is(ImmutableList.of(FUNCTION_PATH, ID)));
  }
}
