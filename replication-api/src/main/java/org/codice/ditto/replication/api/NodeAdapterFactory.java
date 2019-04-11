package org.codice.ditto.replication.api;

import java.net.URL;
import org.codice.ditto.replication.api.NodeAdapter;

public interface NodeAdapterFactory {

  NodeAdapter create(URL url);
}
