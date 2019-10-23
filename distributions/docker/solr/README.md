# Replication Solr Docker Image

The replication solr docker images adds 6 cores that have a custom solr schema.
The cores created are
- replication_site
- replication_filter
- replication_index
- replication_config
- replication_status
- replication_item

The image exposes port 8983.

## Running
```
docker run cnxta/replication-solr:0.3.0-SNAPSHOT
```

