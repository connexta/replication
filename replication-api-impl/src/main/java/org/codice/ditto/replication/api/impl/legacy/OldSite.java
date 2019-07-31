package org.codice.ditto.replication.api.impl.legacy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.codice.ditto.replication.api.impl.data.ReplicationSiteImpl;

/** OldSite is a class that we can use to retrieve 0.2.0 version sites from the persistent store */
public class OldSite extends ReplicationSiteImpl {

  // public so that the persistent store can access it using reflection
  public static final String PERSISTENCE_TYPE = "replication";

  private static final String NAME_KEY = "name";

  private static final String URL_KEY = "url";

  @Override
  public int fromMap(Map<String, Object> properties) {
    // super will throw a null pointer exception because these sites had no version
    // so just set the id and version here instead
    setId((String) properties.get("id"));
    setVersion(CURRENT_VERSION);

    setName((String) properties.get(NAME_KEY));

    String oldUrl = (String) properties.get(URL_KEY);
    try {
      URL url = new URL(oldUrl);
      if (StringUtils.isEmpty(url.getPath())) {
        setUrl(oldUrl + "/services");
      } else {
        // should never hit this since old urls do not have context paths
        setUrl(oldUrl);
      }
    } catch (MalformedURLException e) {
      // saved URLs will not have invalid URLs
    }

    setVerifiedUrl(getUrl()); // do this after setUrl()
    setRemoteManaged(false);
    return 0;
  }
}
