#Replication Solr Docker Image

The replication solr docker images adds 4 cores that have a custom solr schema.
The cores created are
- replication_sites
- replication_configs
- replication_status
- replication_items

The image exposes port 8983.
##Running
```
docker run cnxta/replication-solr:0.3.0-SNAPSHOT
```

