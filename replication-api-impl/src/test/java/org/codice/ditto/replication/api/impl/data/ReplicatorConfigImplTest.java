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
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class ReplicatorConfigImplTest {

  private ReplicatorConfigImpl config;

  @Before
  public void setup() {
    config = new ReplicatorConfigImpl();
  }

  @Test
  public void gettersAndSetters() {
    config.setName("name");
    config.setBidirectional(true);
    config.setSource("source");
    config.setDestination("destination");
    config.setFilter("filter");
    config.setFailureRetryCount(5);
    config.setDescription("description");
    config.setSuspended(true);
    assertThat(config.getName(), is("name"));
    assertThat(config.isBidirectional(), is(true));
    assertThat(config.getSource(), is("source"));
    assertThat(config.getDestination(), is("destination"));
    assertThat(config.getFilter(), is("filter"));
    assertThat(config.getFailureRetryCount(), is(5));
    assertThat(config.getDescription(), is("description"));
    assertThat(config.isSuspended(), is(true));
  }
}
