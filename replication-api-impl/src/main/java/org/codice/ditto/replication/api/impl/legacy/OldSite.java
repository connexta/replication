package org.codice.ditto.replication.api.impl.legacy;

import java.util.Map;
import org.codice.ditto.replication.api.impl.data.ReplicationSiteImpl;

/** OldSite is a class that we can use to retrieve 0.2.0 version sites from the persistent store */
public class OldSite extends ReplicationSiteImpl {

  // public so that the persistent store can access it using reflection
  public static final String PERSISTENCE_TYPE = "replication";

  private static final String NAME_KEY = "name";

  private static final String URL_KEY = "url";

  @Override
  public void fromMap(Map<String, Object> properties) {
    // super will throw a null pointer exception because these sites had no version
    // so just set the id and version here instead
    setId((String) properties.get("id"));
    setVersion(CURRENT_VERSION);

    setName((String) properties.get(NAME_KEY));
    setUrl((String) properties.get(URL_KEY));
  }
}
