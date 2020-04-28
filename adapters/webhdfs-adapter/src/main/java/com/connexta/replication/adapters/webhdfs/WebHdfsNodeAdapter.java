package com.connexta.replication.adapters.webhdfs;

import org.codice.ditto.replication.api.NodeAdapter;
import org.codice.ditto.replication.api.data.CreateRequest;
import org.codice.ditto.replication.api.data.CreateStorageRequest;
import org.codice.ditto.replication.api.data.DeleteRequest;
import org.codice.ditto.replication.api.data.Metadata;
import org.codice.ditto.replication.api.data.QueryRequest;
import org.codice.ditto.replication.api.data.QueryResponse;
import org.codice.ditto.replication.api.data.ResourceRequest;
import org.codice.ditto.replication.api.data.ResourceResponse;
import org.codice.ditto.replication.api.data.UpdateRequest;
import org.codice.ditto.replication.api.data.UpdateStorageRequest;

import java.io.IOException;

public class WebHdfsNodeAdapter implements NodeAdapter {

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public String getSystemName() {
        return null;
    }

    @Override
    public QueryResponse query(QueryRequest queryRequest) {
        return null;
    }

    @Override
    public boolean exists(Metadata metadata) {
        return false;
    }

    @Override
    public boolean createRequest(CreateRequest createRequest) {
        return false;
    }

    @Override
    public boolean updateRequest(UpdateRequest updateRequest) {
        return false;
    }

    @Override
    public boolean deleteRequest(DeleteRequest deleteRequest) {
        return false;
    }

    @Override
    public ResourceResponse readResource(ResourceRequest resourceRequest) {
        return null;
    }

    @Override
    public boolean createResource(CreateStorageRequest createStorageRequest) {
        return false;
    }

    @Override
    public boolean updateResource(UpdateStorageRequest updateStorageRequest) {
        return false;
    }

    @Override
    public void close() throws IOException {

    }

}
