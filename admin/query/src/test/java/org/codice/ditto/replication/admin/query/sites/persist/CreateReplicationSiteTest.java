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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codice.ddf.admin.api.report.ErrorMessage;
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
public class CreateReplicationSiteTest {

  private static final String FUNCTION_PATH = "createReplicationSite";

  private static final String NAME = "name";

  private static final String ROOT_CONTEXT = "rootContext";

  private static final String ADDRESS = "address";

  private static final String URL = "url";

  private static final String SITE_NAME = "siteName";

  private static final String SITE_CONTEXT = "siteContext";

  private static final String SITE_HOSTNAME = "siteHostname";

  private static final int SITE_PORT = 1234;

  private CreateReplicationSite createReplicationSite;

  private Map<String, Object> input;

  @Mock ReplicationUtils replicationUtils;

  @Before
  public void setUp() throws Exception {
    createReplicationSite = new CreateReplicationSite(replicationUtils);

    Map<String, Object> addressField = new HashMap<>();
    Map<String, Object> hostMap = new HashMap<>();
    hostMap.put("hostname", SITE_HOSTNAME);
    hostMap.put("port", SITE_PORT);
    addressField.put("host", hostMap);

    input = new HashMap<>();
    input.put(NAME, SITE_NAME);
    input.put(ROOT_CONTEXT, SITE_CONTEXT);
    input.put(ADDRESS, addressField);
  }

  @Test
  public void testCreateSite() {
    // setup
    when(replicationUtils.isDuplicateSiteName(SITE_NAME)).thenReturn(false);

    ArgumentCaptor<AddressField> addressFieldCaptor = ArgumentCaptor.forClass(AddressField.class);
    ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> contextCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Boolean> disabledLocalCaptor = ArgumentCaptor.forClass(Boolean.class);

    // when
    List<ErrorMessage> errors = createReplicationSite.execute(input, null).getErrorMessages();

    // then
    assertThat(errors.size(), is(0));
    verify(replicationUtils)
        .createSite(
            nameCaptor.capture(),
            addressFieldCaptor.capture(),
            contextCaptor.capture(),
            disabledLocalCaptor.capture());

    AddressField addressField = addressFieldCaptor.getValue();
    assertThat(addressField.host().hostname(), is(SITE_HOSTNAME));
    assertThat(addressField.host().port(), is(SITE_PORT));
    assertThat(nameCaptor.getValue(), is(SITE_NAME));
    assertThat(contextCaptor.getValue(), is(SITE_CONTEXT));
    assertThat(disabledLocalCaptor.getValue(), is(false));
  }

  @Test
  public void validateDuplicateSite() {
    when(replicationUtils.createSite(
            any(String.class), any(AddressField.class), anyString(), anyBoolean()))
        .thenReturn(new ReplicationSiteField());

    when(replicationUtils.isDuplicateSiteName(SITE_NAME)).thenReturn(true);
    List<ErrorMessage> errors = createReplicationSite.execute(input, null).getErrorMessages();
    assertThat(errors.size(), is(1));
    assertThat(errors.get(0).getCode(), is(ReplicationMessages.DUPLICATE_SITE));
  }

  @Test
  public void testFunctionErrorCodes() {
    assertThat(createReplicationSite.getFunctionErrorCodes().size(), is(1));
    assertThat(
        createReplicationSite.getFunctionErrorCodes(), hasItem(ReplicationMessages.DUPLICATE_SITE));
  }

  @Test
  public void testMissingRequiredArguments() {
    // when
    List<ErrorMessage> errors =
        createReplicationSite.execute(null, ImmutableList.of(FUNCTION_PATH)).getErrorMessages();

    // then
    assertThat(errors.size(), is(3));
    assertThat(errors.get(0).getCode(), is(DefaultMessages.MISSING_REQUIRED_FIELD));
    assertThat(errors.get(0).getPath(), is(ImmutableList.of(FUNCTION_PATH, NAME)));
    assertThat(errors.get(1).getCode(), is(DefaultMessages.MISSING_REQUIRED_FIELD));
    assertThat(errors.get(1).getPath(), is(ImmutableList.of(FUNCTION_PATH, ADDRESS, URL)));
    assertThat(errors.get(2).getCode(), is(DefaultMessages.MISSING_REQUIRED_FIELD));
    assertThat(errors.get(2).getPath(), is(ImmutableList.of(FUNCTION_PATH, ROOT_CONTEXT)));
  }
}
