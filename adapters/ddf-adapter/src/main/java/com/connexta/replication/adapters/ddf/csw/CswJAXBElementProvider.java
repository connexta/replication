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
package com.connexta.replication.adapters.ddf.csw;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.provider.JAXBElementProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copied from DDF and modified for replication purposes.
 *
 * <p>Extends and overrides the JAXBElementProvider so that it is contextually aware of the package
 * names used in the services.
 *
 * @param <T> generic type
 */
public class CswJAXBElementProvider<T> extends JAXBElementProvider<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CswJAXBElementProvider.class);

  private static final JAXBContext JAXB_CONTEXT = initJaxbContext();

  public CswJAXBElementProvider() {
    super();

    Map<String, String> prefixes = new HashMap<>();
    prefixes.put(Constants.CSW_OUTPUT_SCHEMA, Constants.CSW_NAMESPACE_PREFIX);
    prefixes.put(Constants.OWS_NAMESPACE, Constants.OWS_NAMESPACE_PREFIX);
    prefixes.put(Constants.XML_SCHEMA_LANGUAGE, Constants.XML_SCHEMA_NAMESPACE_PREFIX);
    prefixes.put(Constants.OGC_SCHEMA, Constants.OGC_NAMESPACE_PREFIX);
    prefixes.put(Constants.GML_SCHEMA, Constants.GML_NAMESPACE_PREFIX);
    prefixes.put(Constants.DUBLIN_CORE_SCHEMA, Constants.DUBLIN_CORE_NAMESPACE_PREFIX);
    prefixes.put(Constants.DUBLIN_CORE_TERMS_SCHEMA, Constants.DUBLIN_CORE_TERMS_NAMESPACE_PREFIX);

    setNamespaceMapperPropertyName(NS_MAPPER_PROPERTY_RI);
    setNamespacePrefixes(prefixes);
  }

  private static JAXBContext initJaxbContext() {
    JAXBContext jaxbContext = null;

    // JAXB context path
    // "net.opengis.cat.csw.v_2_0_2:net.opengis.filter.v_1_1_0:net.opengis.gml.v_3_1_1:net.opengis.ows.v_1_0_0"
    String contextPath = StringUtils.join(new String[] {Constants.OGC_CSW_PACKAGE}, ":");

    try {
      LOGGER.trace("Creating JAXB context with context path: {}.", contextPath);
      jaxbContext =
          JAXBContext.newInstance(contextPath, CswJAXBElementProvider.class.getClassLoader());
    } catch (JAXBException e) {
      LOGGER.info("Unable to create JAXB context using contextPath: {}.", contextPath, e);
    }

    return jaxbContext;
  }

  @Override
  public JAXBContext getJAXBContext(Class<?> type, Type genericType) throws JAXBException {
    return JAXB_CONTEXT;
  }
}
