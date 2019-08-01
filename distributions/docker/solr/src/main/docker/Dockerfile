FROM solr:8.1.1

COPY maven/replication-solr-managed-schema.xml /opt/solr/server/solr/configsets/_default/conf/managed-schema

ENTRYPOINT  bash -c "precreate-core replication_item;precreate-core replication_site;precreate-core replication_config;exec solr -f"