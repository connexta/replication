package org.codice.ditto.replication.api;

import java.net.URL;

public interface NodeAdapterFactory {

  NodeAdapter create(URL url);
}
