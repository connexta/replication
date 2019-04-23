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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class ReplicatorConfigImplTest {

  private static final String ID_KEY = "id";

  private static final String NAME_KEY = "name";

  private static final String SOURCE_KEY = "source";

  private static final String DESTINATION_KEY = "destination";

  private static final String FILTER_KEY = "filter";

  private static final String RETRY_COUNT_KEY = "retry_count";

  private static final String BIDIRECTIONAL_KEY = "bidirectional";

  private static final String VERSION_KEY = "version";

  private static final String MODIFIED_KEY = "modified";

  private ReplicatorConfigImpl config;

  @Before
  public void setup() {
    config = new ReplicatorConfigImpl();
  }

  @Test
  public void writeToMap() {
    config.setId("id");
    config.setName("name");
    config.setSource("source");
    config.setDestination("destination");
    config.setFilter("filter");
    config.setBidirectional(true);
    config.setFailureRetryCount(5);
    Date modified = new Date();
    config.setModified(modified);

    Map<String, Object> props = config.toMap();

    assertThat(props.get(ID_KEY), is("id"));
    assertThat(props.get(NAME_KEY), is("name"));
    assertThat(props.get(SOURCE_KEY), is("source"));
    assertThat(props.get(DESTINATION_KEY), is("destination"));
    assertThat(props.get(FILTER_KEY), is("filter"));
    assertThat(props.get(BIDIRECTIONAL_KEY), is("true"));
    assertThat(props.get(RETRY_COUNT_KEY), is(5));
    assertThat(props.get(VERSION_KEY), is(1));
    assertThat(props.get(MODIFIED_KEY), is(modified));
  }

  @Test
  public void readFromMap() {
    Map<String, Object> props = new HashMap<>();
    props.put(ID_KEY, "id");
    props.put(NAME_KEY, "name");
    props.put(SOURCE_KEY, "source");
    props.put(DESTINATION_KEY, "destination");
    props.put(FILTER_KEY, "filter");
    props.put(BIDIRECTIONAL_KEY, "true");
    props.put(RETRY_COUNT_KEY, 5);
    props.put(VERSION_KEY, 1);
    Date modified = new Date();
    props.put(MODIFIED_KEY, modified);

    config.fromMap(props);

    assertThat(config.getId(), is("id"));
    assertThat(config.getName(), is("name"));
    assertThat(config.getSource(), is("source"));
    assertThat(config.getDestination(), is("destination"));
    assertThat(config.getFilter(), is("filter"));
    assertThat(config.isBidirectional(), is(true));
    assertThat(config.getFailureRetryCount(), is(5));
    assertThat(config.getVersion(), is(1));
    assertThat(config.getModified(), is(modified));
  }
}
