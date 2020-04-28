package com.connexta.replication.adapters.webhdfs;

import org.codice.ditto.replication.api.NodeAdapter;
import org.codice.ditto.replication.api.NodeAdapterFactory;
import org.codice.ditto.replication.api.NodeAdapterType;

import java.net.URL;

public class WebHdfsNodeAdapterFactory implements NodeAdapterFactory {

    @Override
    public NodeAdapter create(URL url) {
        return null;
    }

    @Override
    public NodeAdapterType getType() {
        return null;
    }

}
