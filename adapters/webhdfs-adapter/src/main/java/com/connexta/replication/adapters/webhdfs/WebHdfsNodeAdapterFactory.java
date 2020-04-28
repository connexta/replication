package com.connexta.replication.adapters.webhdfs;

import org.codice.ditto.replication.api.NodeAdapter;
import org.codice.ditto.replication.api.NodeAdapterFactory;
import org.codice.ditto.replication.api.NodeAdapterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

public class WebHdfsNodeAdapterFactory implements NodeAdapterFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebHdfsNodeAdapterFactory.class);

    private final int connectionTimeout;

    private final int receiveTimeout;

    public WebHdfsNodeAdapterFactory(int connectionTimeout, int receiveTimeout) {
        this.connectionTimeout = connectionTimeout;
        this.receiveTimeout = receiveTimeout;
        LOGGER.debug(
                "Created a WebHdfsNodeAdapterFactory with a connection timeout of {}ms and a receive timeout of {}ms",
                connectionTimeout,
                receiveTimeout);
    }

    @Override
    public NodeAdapter create(URL url) {
        return null;
    }

    @Override
    public NodeAdapterType getType() {
        return NodeAdapterType.WEBHDFS;
    }

}
