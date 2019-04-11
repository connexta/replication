package org.codice.ditto.replication.api.temp.resources;

import org.codice.ditto.replication.api.temp.metadata.Metadata;

public interface ResourceRequest {

  Metadata getMetadata();
}
