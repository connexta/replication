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
package com.connexta.replication.query.cmp;

import com.connexta.replication.api.data.SiteKind;
import com.connexta.replication.api.data.SiteType;
import com.connexta.replication.api.impl.persistence.pojo.FilterPojo;
import com.connexta.replication.api.impl.persistence.pojo.SitePojo;
import com.connexta.replication.data.MetadataImpl;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A utility class that helps with the creation of test data. Most of the methods in this class will
 * create test data by accepting an integer (called the seed) which apply to the fields of the
 * generated data in various ways. Generally the creation of the data will follow these rules: - the
 * ID of the object will be set to the seed - any string fields will be set to the name of the field
 * concatenated with the seed - any numerical primitives will be set to the seed - booleans will be
 * set to false - any non-primitive fields or fields that will change how the data is handled will
 * be set to a reasonable default
 */
public class TestDataGenerationUtils {

  private TestDataGenerationUtils() {}

  /**
   * Creates a {@link SitePojo} with identifying information created with the given integer. The
   * created pojo is a DDF REGIONAL site with a 100ms polling period and a parallelism factor of 1.
   *
   * @param seed an integer used to create identifying information for the Site
   * @return the created site pojo
   */
  public static SitePojo generateSitePojo(int seed) {
    return new SitePojo()
        .setVersion(SitePojo.CURRENT_VERSION)
        .setId(String.valueOf(seed))
        .setName("name" + seed)
        .setDescription("description" + seed)
        .setUrl("https://site:" + seed)
        .setType(SiteType.DDF)
        .setKind(SiteKind.REGIONAL)
        .setPollingPeriod(100)
        .setParallelismFactor(1);
  }

  /**
   * Creates a {@link FilterPojo} with identifying information created with the given integer. The
   * created pojo has a priority of 0 and is not suspended by default.
   *
   * @param siteId the ID of the site associated with this filter
   * @param seed an integer used to create identifying information for the filter
   * @return the created filter pojo
   */
  public static FilterPojo generateFilterPojo(String siteId, int seed) {
    return new FilterPojo()
        .setVersion(FilterPojo.CURRENT_VERSION)
        .setId(String.valueOf(seed))
        .setSiteId(siteId)
        .setFilter("filter" + seed)
        .setName("name" + seed)
        .setDescription("description" + seed)
        .setSuspended(false)
        .setPriority((byte) 0);
  }

  /**
   * Creates a {@link MetadataImpl} with identifying information created with the given integer. The
   * raw metadata is an empty hashmap and the metadataModified date is the epoch.
   *
   * @param seed an integer used to create identifying information for the metadata
   * @return the created Metadata
   */
  public static MetadataImpl generateMetadata(int seed) {
    return new MetadataImpl(
        (long) seed, new HashMap(), Map.class, String.valueOf(seed), new Date(0));
  }

  /**
   * Creates a {@link MetadataImpl} with identifying information created with the given integer. The
   * raw metadata is an empty hashmap.
   *
   * @param seed an integer used to create identifying information for the metadata
   * @param metadataModified the metadataModified date to set on this metadata
   * @return the created Metadata
   */
  public static MetadataImpl generateMetadata(int seed, Date metadataModified) {
    return new MetadataImpl(
        (long) seed, new HashMap(), Map.class, String.valueOf(seed), metadataModified);
  }

  /**
   * Creates a {@link MetadataImpl} with identifying information created with the given integer.
   * Resource information is also generated and attached to the metadata. The raw metadata is an
   * empty hashmap.
   *
   * @param seed an integer used to create identifying information for the metadata
   * @param metadataModified the metadataModified date to set on this metadata
   * @return the created Metadata
   */
  public static MetadataImpl generateMetadataWithResource(int seed, Date metadataModified) {
    MetadataImpl metadata = generateMetadata(seed, metadataModified);
    return generateResource(seed, metadata);
  }

  /**
   * Creates a {@link MetadataImpl} with identifying information created with the given integer.
   * Resource information is also generated and attached to the metadata. The raw metadata is an
   * empty hashmap and the metadataModified is the epoch.
   *
   * @param seed an integer used to create identifying information for the metadata
   * @return teh created Metadata
   */
  public static MetadataImpl generateMetadataWithResource(int seed) {
    MetadataImpl metadata = generateMetadata(seed);
    return generateResource(seed, metadata);
  }

  private static MetadataImpl generateResource(int seed, MetadataImpl metadata) {
    URI resourceUri;
    try {
      resourceUri = new URI("https://resource." + seed);
    } catch (URISyntaxException e) {
      throw new AssertionError(e);
    }
    metadata.setResource((long) seed, resourceUri, new Date(seed));
    return metadata;
  }
}
