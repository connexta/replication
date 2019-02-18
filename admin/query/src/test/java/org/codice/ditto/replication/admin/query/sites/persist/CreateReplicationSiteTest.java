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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codice.ddf.admin.api.report.ErrorMessage;
import org.codice.ddf.admin.common.fields.common.AddressField;
import org.codice.ditto.replication.admin.query.ReplicationMessages;
import org.codice.ditto.replication.admin.query.ReplicationUtils;
import org.codice.ditto.replication.admin.query.sites.fields.ReplicationSiteField;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CreateReplicationSiteTest {

  CreateReplicationSite site;

  Map<String, Object> input;

  @Mock ReplicationUtils utils;

  @Before
  public void setUp() throws Exception {
    site = new CreateReplicationSite(utils);
    when(utils.createSite(any(String.class), any(AddressField.class)))
        .thenReturn(new ReplicationSiteField());
    input = new HashMap<>();
    input.put("id", "myid");
    input.put("name", "myname");
  }

  @Test
  public void validate() {
    when(utils.siteExists("myname")).thenReturn(false);
    List<ErrorMessage> errors = site.execute(input, null).getErrorMessages();
    assertThat(errors.size(), is(0));
  }

  @Test
  public void validateDuplicateSite() {
    when(utils.siteExists("myname")).thenReturn(true);
    List<ErrorMessage> errors = site.execute(input, null).getErrorMessages();
    assertThat(errors.size(), is(1));
    assertThat(errors.get(0).getCode(), is(ReplicationMessages.DUPLICATE_SITE));
  }
}
