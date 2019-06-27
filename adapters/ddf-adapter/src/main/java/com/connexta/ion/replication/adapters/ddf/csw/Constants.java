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
package com.connexta.ion.replication.adapters.ddf.csw;

public final class Constants {

  private Constants() {}
  // Metacard constants
  public static final String REGISTRY_TAG = "registry";

  public static final String REGISTRY_IDENTITY_NODE = "registry.local.registry-identity-node";

  public static final String VERSIONED_ON = "metacard.version.versioned-on";

  public static final String VERSION_TAG = "revision";

  public static final String VERSION_OF_ID = "metacard.version.id";

  public static final String ACTION = "metacard.version.action";

  public static final String METACARD_ID = "id";

  public static final String METACARD_TAGS = "metacard-tags";

  public static final String METACARD_MODIFIED = "metacard.modified";

  public static final String DEFAULT_TAG = "resource";

  public static final String RESOURCE_URI = "resource-uri";

  public static final String RESOURCE_SIZE = "resource-size";

  public static final String DERIVED_RESOURCE_URI = "resource.derived-uri";

  public static final String DERIVED_RESOURCE_DOWNLOAD_URL = "resource.derived-download-url";

  public static final String MODIFIED = "modified";

  public static final String DEFAULT_METACARD_TYPE_NAME = "ddf.metacard";

  public static final String METACARD_SCHEMA = "urn:catalog:metacard";

  public static final String RAW_METADATA_KEY = "raw-metadata";

  // CSW constants
  public static final String NAMESPACE_DECLARATIONS = "NAMESPACE_DECLARATIONS";

  public static final String GET_CAPABILITIES = "GetCapabilities";

  public static final String GET_RECORDS = "GetRecords";

  public static final String GET_RECORDS_RESPONSE = "GetRecordsResponse";

  public static final String CAPABILITIES = "Capabilities";

  public static final String VERSION_2_0_2 = "2.0.2";

  public static final String OUTPUT_SCHEMA_PARAMETER = "OutputSchema";

  public static final String OWS_NAMESPACE = "http://www.opengis.net/ows";

  public static final String XMLNS = "xmlns";

  public static final String NAMESPACE_DELIMITER = ":";

  public static final String XML_SCHEMA_NAMESPACE_PREFIX = "xs";

  public static final String OWS_NAMESPACE_PREFIX = "ows";

  public static final String OGC_NAMESPACE_PREFIX = "ogc";

  public static final String GML_NAMESPACE_PREFIX = "gml";

  public static final String CSW_NAMESPACE_PREFIX = "csw";

  public static final String DUBLIN_CORE_NAMESPACE_PREFIX = "dc";

  public static final String DUBLIN_CORE_TERMS_NAMESPACE_PREFIX = "dct";

  public static final String CSW_OUTPUT_SCHEMA = "http://www.opengis.net/cat/csw/2.0.2";

  public static final String OGC_SCHEMA = "http://www.opengis.net/ogc";

  public static final String GML_SCHEMA = "http://www.opengis.net/gml";

  public static final String DUBLIN_CORE_SCHEMA = "http://purl.org/dc/elements/1.1/";

  public static final String DUBLIN_CORE_TERMS_SCHEMA = "http://purl.org/dc/terms/";

  public static final String CONSTRAINT_VERSION = "1.1.0";

  public static final String OGC_CSW_PACKAGE = "net.opengis.cat.csw.v_2_0_2";

  public static final String XML_SCHEMA_LANGUAGE = "http://www.w3.org/XML/Schema";
}
