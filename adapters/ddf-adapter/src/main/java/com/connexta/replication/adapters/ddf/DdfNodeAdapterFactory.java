/**
 * Copyright (c) Connexta
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package com.connexta.replication.adapters.ddf;

import com.connexta.replication.adapters.ddf.csw.Constants;
import com.connexta.replication.adapters.ddf.csw.Csw;
import com.connexta.replication.adapters.ddf.csw.CswJAXBElementProvider;
import com.connexta.replication.adapters.ddf.csw.CswResponseExceptionMapper;
import com.connexta.replication.adapters.ddf.csw.GetRecordsMessageBodyReader;
import com.connexta.replication.adapters.ddf.rest.DdfRestClientFactory;
import com.connexta.replication.data.ReplicationConstants;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.xml.namespace.QName;
import net.opengis.cat.csw.v_2_0_2.AcknowledgementType;
import net.opengis.cat.csw.v_2_0_2.CapabilitiesType;
import net.opengis.cat.csw.v_2_0_2.GetCapabilitiesType;
import net.opengis.cat.csw.v_2_0_2.GetRecordsResponseType;
import net.opengis.cat.csw.v_2_0_2.GetRecordsType;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.codice.ddf.cxf.client.ClientFactoryFactory;
import org.codice.ddf.cxf.client.SecureCxfClientFactory;
import org.codice.ditto.replication.api.NodeAdapter;
import org.codice.ditto.replication.api.NodeAdapterFactory;
import org.codice.ditto.replication.api.NodeAdapterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating {@link DdfNodeAdapter}s for the {@link
 * org.codice.ditto.replication.api.Replicator}.
 */
public class DdfNodeAdapterFactory implements NodeAdapterFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(DdfNodeAdapterFactory.class);

  private final DdfRestClientFactory ddfRestClientFactory;
  private final ClientFactoryFactory clientFactory;
  private final int connectionTimeout;
  private final int receiveTimeout;

  private Map<String, NodeAdapter> adapterCache =
      Collections.synchronizedMap(new PassiveExpiringMap<>(25, TimeUnit.MINUTES));

  /**
   * Creates a DdfNodeAdapterFactory.
   *
   * @param clientFactory a factory for {@link SecureCxfClientFactory}s
   * @param connectionTimeout the connection timeout for any clients created by this factory
   * @param receiveTimeout the receive timeout for any clients created by this factory
   */
  public DdfNodeAdapterFactory(
      ClientFactoryFactory clientFactory, int connectionTimeout, int receiveTimeout) {
    this.connectionTimeout = connectionTimeout;
    this.receiveTimeout = receiveTimeout;
    this.ddfRestClientFactory =
        new DdfRestClientFactory(clientFactory, connectionTimeout, receiveTimeout);
    this.clientFactory = clientFactory;
    LOGGER.debug(
        "Created a DdfNodeAdapterFactory with a connection timeout of {} ms and a receive timeout of {} ms",
        connectionTimeout,
        receiveTimeout);
  }

  @Override
  public NodeAdapter create(URL url) {
    if (adapterCache.containsKey(url.toString())) {
      return adapterCache.get(url.toString());
    }
    SecureCxfClientFactory<Csw> ddfCswClientFactory =
        clientFactory.getSecureCxfClientFactory(
            url.toString() + "/csw",
            Csw.class,
            initProviders(),
            null,
            true,
            false,
            connectionTimeout,
            receiveTimeout,
            ReplicationConstants.getCertAlias(),
            ReplicationConstants.getKeystore(),
            ReplicationConstants.TLS_PROTOCOL);
    DdfNodeAdapter adapter = new DdfNodeAdapter(ddfRestClientFactory, ddfCswClientFactory, url);
    adapterCache.put(url.toString(), adapter);
    return adapter;
  }

  private List<Object> initProviders() {

    CswJAXBElementProvider getRecordsTypeProvider = new CswJAXBElementProvider<>();
    getRecordsTypeProvider.setMarshallAsJaxbElement(true);
    List<String> jaxbElementClassNames = new ArrayList<>(5);
    Map<String, String> jaxbElementClassMap = new HashMap<>();

    // Adding class names that need to be marshalled/unmarshalled to
    // jaxbElementClassNames list
    jaxbElementClassNames.add(GetRecordsType.class.getName());
    jaxbElementClassNames.add(CapabilitiesType.class.getName());
    jaxbElementClassNames.add(GetCapabilitiesType.class.getName());
    jaxbElementClassNames.add(GetRecordsResponseType.class.getName());
    jaxbElementClassNames.add(AcknowledgementType.class.getName());

    getRecordsTypeProvider.setJaxbElementClassNames(jaxbElementClassNames);

    // Adding map entry of <Class Name>,<Qualified Name> to jaxbElementClassMap
    String expandedName = new QName(Constants.CSW_OUTPUT_SCHEMA, Constants.GET_RECORDS).toString();
    String message = "{} expanded name: {}";
    LOGGER.debug(message, Constants.GET_RECORDS, expandedName);
    jaxbElementClassMap.put(GetRecordsType.class.getName(), expandedName);

    String getCapsExpandedName =
        new QName(Constants.CSW_OUTPUT_SCHEMA, Constants.GET_CAPABILITIES).toString();
    LOGGER.debug(message, Constants.GET_CAPABILITIES, expandedName);
    jaxbElementClassMap.put(GetCapabilitiesType.class.getName(), getCapsExpandedName);

    String capsExpandedName =
        new QName(Constants.CSW_OUTPUT_SCHEMA, Constants.CAPABILITIES).toString();
    LOGGER.debug(message, Constants.CAPABILITIES, capsExpandedName);
    jaxbElementClassMap.put(CapabilitiesType.class.getName(), capsExpandedName);

    String acknowledgmentName =
        new QName(Constants.CSW_OUTPUT_SCHEMA, "Acknowledgement").toString();
    jaxbElementClassMap.put(AcknowledgementType.class.getName(), acknowledgmentName);
    getRecordsTypeProvider.setJaxbElementClassMap(jaxbElementClassMap);

    GetRecordsMessageBodyReader grmbr = new GetRecordsMessageBodyReader();

    return Arrays.asList(getRecordsTypeProvider, new CswResponseExceptionMapper(), grmbr);
  }

  @Override
  public NodeAdapterType getType() {
    return NodeAdapterType.DDF;
  }
}
