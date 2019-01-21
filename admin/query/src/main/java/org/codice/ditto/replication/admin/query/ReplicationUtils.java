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
package org.codice.ditto.replication.admin.query;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.codice.ddf.admin.api.fields.ListField;
import org.codice.ddf.admin.common.fields.common.AddressField;
import org.codice.ditto.replication.admin.query.replications.fields.ReplicationField;
import org.codice.ditto.replication.admin.query.sites.fields.ReplicationSiteField;
import org.codice.ditto.replication.api.modern.ReplicationSite;
import org.codice.ditto.replication.api.modern.ReplicationSitePersistentStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: replace static methods as we replace the mock with real solutions
public class ReplicationUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReplicationUtils.class);

  private static final int TEST_ENTRIES = 3;

  private final ReplicationSitePersistentStore persistentStore;

  public ReplicationUtils(ReplicationSitePersistentStore persistentStore) {
    this.persistentStore = persistentStore;
  }

  // mutator methods
  // TODO: likely need to make these thread safe in the future
  // TODO: reject site if a site with that name already exists
  public ReplicationSiteField createSite(String name, AddressField address) {
    String urlString = addressFieldToUrlString(address);
    ReplicationSite newSite = persistentStore.saveSite(name, urlString);

    ReplicationSiteField siteField = new ReplicationSiteField();
    siteField.address(new AddressField().url(newSite.getUrl().toString()));
    siteField.id(newSite.getId());
    siteField.name(newSite.getName());

    return siteField;
  }

  public ReplicationSiteField updateSite(String id, String name, AddressField address) {
    String urlString = addressFieldToUrlString(address);
    ReplicationSite site = persistentStore.editSite(id, name, urlString);
    ReplicationSiteField siteField = new ReplicationSiteField();
    siteField.address(new AddressField().url(site.getUrl().toString()));
    siteField.id(site.getId());
    siteField.name(site.getName());

    return siteField;
  }

  private String addressFieldToUrlString(AddressField address) {
    return address.host().hostname() == null
        ? address.url()
        : String.format("https://%s:%s", address.host().hostname(), address.host().port());
  }

  public Boolean deleteSite(String id) {
    return persistentStore.deleteSite(id);
  }

  public static ReplicationField createReplication(
      String name, String sourceId, String destinationId, String filter) {
    ReplicationField config = new ReplicationField();
    String id = "id" + gen();
    config.id(id);
    config.name(name);
    config.source(createSite(0));
    config.destination(createSite(1));
    config.filter(filter);
    config.itemsTransferred(0);
    config.dataTransferred("0 MB");

    return config;
  }

  public static ReplicationField updateReplication(
      String id, String name, String sourceId, String destinationId, String filter) {
    ReplicationField config = new ReplicationField();
    config.id(id);
    config.name(name);
    config.source(createSite(0));
    config.destination(createSite(1));
    config.filter(filter);
    config.itemsTransferred(gen());
    config.dataTransferred(gen() + " MB");

    return config;
  }

  public static boolean deleteConfig(String id) {
    return true;
  }

  public ListField<ReplicationSiteField> getSites() {
    ListField<ReplicationSiteField> siteFields = new ReplicationSiteField.ListImpl();
    Set<ReplicationSite> sites = persistentStore.getSites();

    for (ReplicationSite site : sites) {
      ReplicationSiteField field = new ReplicationSiteField();
      field.id(site.getId());
      field.name(site.getName());
      AddressField address = new AddressField();
      address.url(site.getUrl().toString());
      field.address(address);
      siteFields.add(field);
    }

    return siteFields;
  }

  public static ListField<ReplicationField> getReplications() {
    ListField<ReplicationField> configs = new ReplicationField.ListImpl();
    for (int i = 0; i < TEST_ENTRIES; i++) {
      configs.add(createReplication(i));
    }
    return configs;
  }

  private static ReplicationSiteField createSite(int num) {
    ReplicationSiteField site = new ReplicationSiteField();
    AddressField address = new AddressField();
    address.hostname("site");
    address.port(num);
    site.address(address);
    site.id("id" + num);
    site.name("site" + num);

    return site;
  }

  private static ReplicationField createReplication(int num) {
    ReplicationField config = new ReplicationField();
    String id = "id" + num;
    config.id(id);
    config.name("config" + num);
    config.source(createSite(num));
    config.destination(createSite(num + 1));
    config.filter("config is like " + num);
    config.itemsTransferred(gen());
    config.dataTransferred(gen() + " MB");

    return config;
  }

  // generates a random int
  private static int gen() {
    return gen(10000);
  }

  private static int gen(int bound) {
    return ThreadLocalRandom.current().nextInt(0, bound);
  }
}
