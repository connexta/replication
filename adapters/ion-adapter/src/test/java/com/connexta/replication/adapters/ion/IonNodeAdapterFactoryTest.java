package com.connexta.replication.adapters.ion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.net.URL;
import org.codice.ditto.replication.api.NodeAdapter;
import org.codice.ditto.replication.api.NodeAdapterType;
import org.junit.Test;

public class IonNodeAdapterFactoryTest {

  @Test
  public void create() throws Exception {
    NodeAdapter adapter = new IonNodeAdapterFactory().create(new URL("https://localhost:1234"));
    assertThat(adapter.getClass().isAssignableFrom(IonNodeAdapter.class), is(true));
  }

  @Test
  public void getType() {
    assertThat(new IonNodeAdapterFactory().getType(), is(NodeAdapterType.ION));
  }
}
