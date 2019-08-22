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
package com.connexta.replication.api.impl.data;

import com.connexta.replication.api.data.InvalidFieldException;
import com.connexta.replication.api.data.SiteKind;
import com.connexta.replication.api.data.SiteType;
import com.connexta.replication.api.data.UnsupportedVersionException;
import com.connexta.replication.api.impl.persistence.pojo.SitePojo;
import com.github.npathai.hamcrestopt.OptionalMatchers;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SiteImplTest {
  private static final boolean REMOTE_MANAGED = true;
  private static final String ID = "id";
  private static final String NAME = "testName";
  private static final String DESCRIPTION = "description";
  private static final URL URL;
  private static final URL URL2;
  private static final SiteType TYPE = SiteType.DDF;
  private static final SiteKind KIND = SiteKind.TACTICAL;
  private static final Duration POLLING_PERIOD = Duration.ofMinutes(5L);
  private static final int PARALLELISM_FACTOR = 3;

  static {
    try {
      URL = new URL("https://host:44");
      URL2 = new URL("https://host:44/2");
    } catch (MalformedURLException e) {
      throw new AssertionError(e);
    }
  }

  @Rule public ExpectedException exception = ExpectedException.none();

  private final SiteImpl persistable = new SiteImpl();

  public SiteImplTest() {
    persistable.setId(SiteImplTest.ID);
    persistable.setName(SiteImplTest.NAME);
    persistable.setDescription(SiteImplTest.DESCRIPTION);
    persistable.setUrl(SiteImplTest.URL);
    persistable.setType(SiteImplTest.TYPE);
    persistable.setKind(SiteImplTest.KIND);
    persistable.setPollingPeriod(SiteImplTest.POLLING_PERIOD);
    persistable.setParallelismFactor(SiteImplTest.PARALLELISM_FACTOR);
  }

  @Test
  public void testDefaultCtor() throws Exception {
    final SiteImpl persistable = new SiteImpl();

    Assert.assertThat(persistable.getId(), Matchers.not(Matchers.emptyOrNullString()));
    Assert.assertThat(persistable.getName(), Matchers.nullValue());
    Assert.assertThat(persistable.getDescription(), OptionalMatchers.isEmpty());
    Assert.assertThat(persistable.getUrl(), Matchers.nullValue());
    Assert.assertThat(persistable.getType(), Matchers.equalTo(SiteType.UNKNOWN));
    Assert.assertThat(persistable.getKind(), Matchers.equalTo(SiteKind.UNKNOWN));
    Assert.assertThat(persistable.getPollingPeriod(), OptionalMatchers.isEmpty());
    Assert.assertThat(persistable.getParallelismFactor().isEmpty(), Matchers.equalTo(true));
  }

  @Test
  public void testCtorWithPojo() throws Exception {
    final SitePojo pojo =
        new SitePojo()
            .setId(SiteImplTest.ID)
            .setName(SiteImplTest.NAME)
            .setDescription(SiteImplTest.DESCRIPTION)
            .setUrl(SiteImplTest.URL.toString())
            .setType(SiteImplTest.TYPE.name())
            .setKind(SiteImplTest.KIND.name())
            .setPollingPeriod(SiteImplTest.POLLING_PERIOD.toMillis())
            .setParallelismFactor(SiteImplTest.PARALLELISM_FACTOR);

    final SiteImpl persistable = new SiteImpl(pojo);

    Assert.assertThat(persistable.getId(), Matchers.equalTo(pojo.getId()));
    Assert.assertThat(persistable.getName(), Matchers.equalTo(pojo.getName()));
    Assert.assertThat(
        persistable.getDescription(), OptionalMatchers.isPresentAndIs(pojo.getDescription()));
    Assert.assertThat(persistable.getUrl().toString(), Matchers.equalTo(pojo.getUrl()));
    Assert.assertThat(persistable.getType().name(), Matchers.equalTo(pojo.getType()));
    Assert.assertThat(persistable.getKind().name(), Matchers.equalTo(pojo.getKind()));
    Assert.assertThat(persistable.getPollingPeriod(), OptionalMatchers.isPresent());
    Assert.assertThat(
        persistable.getPollingPeriod().get().toMillis(), Matchers.equalTo(pojo.getPollingPeriod()));
    Assert.assertThat(persistable.getParallelismFactor().isPresent(), Matchers.equalTo(true));
    Assert.assertThat(
        persistable.getParallelismFactor().getAsInt(),
        Matchers.equalTo(pojo.getParallelismFactor()));
  }

  @Test
  public void testGetName() throws Exception {
    Assert.assertThat(persistable.getName(), Matchers.equalTo(SiteImplTest.NAME));
  }

  @Test
  public void testGetDescription() throws Exception {
    Assert.assertThat(
        persistable.getDescription(), OptionalMatchers.isPresentAndIs(SiteImplTest.DESCRIPTION));
  }

  @Test
  public void testGetDescriptionWhenNoneDefined() throws Exception {
    persistable.setDescription(null);

    Assert.assertThat(persistable.getDescription(), OptionalMatchers.isEmpty());
  }

  @Test
  public void testGetUrl() throws Exception {
    Assert.assertThat(persistable.getUrl(), Matchers.equalTo(SiteImplTest.URL));
  }

  @Test
  public void testGetType() throws Exception {
    Assert.assertThat(persistable.getType(), Matchers.equalTo(SiteImplTest.TYPE));
  }

  @Test
  public void testGetKind() throws Exception {
    Assert.assertThat(persistable.getKind(), Matchers.equalTo(SiteImplTest.KIND));
  }

  @Test
  public void testGetPollingPeriod() throws Exception {
    Assert.assertThat(
        persistable.getPollingPeriod(),
        OptionalMatchers.isPresentAndIs(SiteImplTest.POLLING_PERIOD));
  }

  @Test
  public void testGetPollingPeriodWhenNoneDefined() throws Exception {
    persistable.setPollingPeriod(null);

    Assert.assertThat(persistable.getPollingPeriod(), OptionalMatchers.isEmpty());
  }

  @Test
  public void testGetParallelismFactor() throws Exception {
    Assert.assertThat(persistable.getParallelismFactor().isPresent(), Matchers.equalTo(true));
    Assert.assertThat(
        persistable.getParallelismFactor().getAsInt(),
        Matchers.equalTo(SiteImplTest.PARALLELISM_FACTOR));
  }

  @Test
  public void testGetParallelismFactorWhenNotDefined_0() throws Exception {
    persistable.setParallelismFactor(0);

    Assert.assertThat(persistable.getParallelismFactor().isPresent(), Matchers.equalTo(false));
  }

  @Test
  public void testGetParallelismFactorWhenNotDefined_Negative() throws Exception {
    persistable.setParallelismFactor(-1);

    Assert.assertThat(persistable.getParallelismFactor().isPresent(), Matchers.equalTo(false));
  }

  @Test
  public void testWriteTo() throws Exception {
    final SitePojo pojo = new SitePojo();

    persistable.writeTo(pojo);

    Assert.assertThat(pojo.getId(), Matchers.equalTo(persistable.getId()));
    Assert.assertThat(pojo.getVersion(), Matchers.equalTo(SitePojo.CURRENT_VERSION));
    Assert.assertThat(pojo.getName(), Matchers.equalTo(persistable.getName()));
    Assert.assertThat(pojo.getDescription(), Matchers.equalTo(persistable.getDescription().get()));
    Assert.assertThat(pojo.getUrl(), Matchers.equalTo(persistable.getUrl().toString()));
    Assert.assertThat(pojo.getType(), Matchers.equalTo(persistable.getType().name()));
    Assert.assertThat(pojo.getKind(), Matchers.equalTo(persistable.getKind().name()));
    Assert.assertThat(
        pojo.getPollingPeriod(), Matchers.equalTo(persistable.getPollingPeriod().get().toMillis()));
    Assert.assertThat(
        pojo.getParallelismFactor(),
        Matchers.equalTo(persistable.getParallelismFactor().getAsInt()));
  }

  @Test
  public void testWriteToWhenNameIsNull() throws Exception {
    exception.expect(InvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*missing.*name.*"));

    final SiteImpl persistable = new SiteImpl();
    final SitePojo pojo = new SitePojo();

    persistable.setDescription(SiteImplTest.DESCRIPTION);
    persistable.setUrl(SiteImplTest.URL);
    persistable.setType(SiteImplTest.TYPE);
    persistable.setKind(SiteImplTest.KIND);
    persistable.setPollingPeriod(SiteImplTest.POLLING_PERIOD);
    persistable.setParallelismFactor(SiteImplTest.PARALLELISM_FACTOR);

    persistable.writeTo(pojo);
  }

  @Test
  public void testWriteToWhenNameIsEmpty() throws Exception {
    exception.expect(InvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*empty.*name.*"));

    final SiteImpl persistable = new SiteImpl();
    final SitePojo pojo = new SitePojo();

    persistable.setName("");
    persistable.setDescription(SiteImplTest.DESCRIPTION);
    persistable.setUrl(SiteImplTest.URL);
    persistable.setType(SiteImplTest.TYPE);
    persistable.setKind(SiteImplTest.KIND);
    persistable.setPollingPeriod(SiteImplTest.POLLING_PERIOD);
    persistable.setParallelismFactor(SiteImplTest.PARALLELISM_FACTOR);

    persistable.writeTo(pojo);
  }

  @Test
  public void testWriteToWhenDescriptionIsNull() throws Exception {
    final SiteImpl persistable = new SiteImpl();
    final SitePojo pojo = new SitePojo();

    persistable.setName(SiteImplTest.NAME);
    persistable.setUrl(SiteImplTest.URL);
    persistable.setType(SiteImplTest.TYPE);
    persistable.setKind(SiteImplTest.KIND);
    persistable.setPollingPeriod(SiteImplTest.POLLING_PERIOD);
    persistable.setParallelismFactor(SiteImplTest.PARALLELISM_FACTOR);

    persistable.writeTo(pojo);

    Assert.assertThat(pojo.getId(), Matchers.equalTo(persistable.getId()));
    Assert.assertThat(pojo.getVersion(), Matchers.equalTo(SitePojo.CURRENT_VERSION));
    Assert.assertThat(pojo.getName(), Matchers.equalTo(persistable.getName()));
    Assert.assertThat(pojo.getDescription(), Matchers.nullValue());
    Assert.assertThat(pojo.getUrl(), Matchers.equalTo(persistable.getUrl().toString()));
    Assert.assertThat(pojo.getType(), Matchers.equalTo(persistable.getType().name()));
    Assert.assertThat(pojo.getKind(), Matchers.equalTo(persistable.getKind().name()));
    Assert.assertThat(
        pojo.getPollingPeriod(), Matchers.equalTo(persistable.getPollingPeriod().get().toMillis()));
    Assert.assertThat(
        pojo.getParallelismFactor(),
        Matchers.equalTo(persistable.getParallelismFactor().getAsInt()));
  }

  @Test
  public void testWriteToWhenUrlIsNull() throws Exception {
    exception.expect(InvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*missing.*url.*"));

    final SiteImpl persistable = new SiteImpl();
    final SitePojo pojo = new SitePojo();

    persistable.setName(SiteImplTest.NAME);
    persistable.setDescription(SiteImplTest.DESCRIPTION);
    persistable.setType(SiteImplTest.TYPE);
    persistable.setKind(SiteImplTest.KIND);
    persistable.setPollingPeriod(SiteImplTest.POLLING_PERIOD);
    persistable.setParallelismFactor(SiteImplTest.PARALLELISM_FACTOR);

    persistable.writeTo(pojo);
  }

  @Test
  public void testWriteToWhenTypeIsNull() throws Exception {
    exception.expect(InvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*missing.*type.*"));

    final SiteImpl persistable = new SiteImpl();
    final SitePojo pojo = new SitePojo();

    persistable.setName(SiteImplTest.NAME);
    persistable.setDescription(SiteImplTest.DESCRIPTION);
    persistable.setUrl(SiteImplTest.URL);
    persistable.setType(null);
    persistable.setKind(SiteImplTest.KIND);
    persistable.setPollingPeriod(SiteImplTest.POLLING_PERIOD);
    persistable.setParallelismFactor(SiteImplTest.PARALLELISM_FACTOR);

    persistable.writeTo(pojo);
  }

  @Test
  public void testWriteToWhenKindIsNull() throws Exception {
    exception.expect(InvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*missing.*kind.*"));

    final SiteImpl persistable = new SiteImpl();
    final SitePojo pojo = new SitePojo();

    persistable.setName(SiteImplTest.NAME);
    persistable.setDescription(SiteImplTest.DESCRIPTION);
    persistable.setUrl(SiteImplTest.URL);
    persistable.setType(SiteImplTest.TYPE);
    persistable.setKind(null);
    persistable.setPollingPeriod(SiteImplTest.POLLING_PERIOD);
    persistable.setParallelismFactor(SiteImplTest.PARALLELISM_FACTOR);

    persistable.writeTo(pojo);
  }

  @Test
  public void testWriteToWhenPollingPeriodIsNull() throws Exception {
    final SiteImpl persistable = new SiteImpl();
    final SitePojo pojo = new SitePojo();

    persistable.setName(SiteImplTest.NAME);
    persistable.setDescription(SiteImplTest.DESCRIPTION);
    persistable.setUrl(SiteImplTest.URL);
    persistable.setType(SiteImplTest.TYPE);
    persistable.setKind(SiteImplTest.KIND);
    persistable.setParallelismFactor(SiteImplTest.PARALLELISM_FACTOR);

    persistable.writeTo(pojo);

    Assert.assertThat(pojo.getId(), Matchers.equalTo(persistable.getId()));
    Assert.assertThat(pojo.getVersion(), Matchers.equalTo(SitePojo.CURRENT_VERSION));
    Assert.assertThat(pojo.getName(), Matchers.equalTo(persistable.getName()));
    Assert.assertThat(pojo.getDescription(), Matchers.equalTo(persistable.getDescription().get()));
    Assert.assertThat(pojo.getUrl(), Matchers.equalTo(persistable.getUrl().toString()));
    Assert.assertThat(pojo.getType(), Matchers.equalTo(persistable.getType().name()));
    Assert.assertThat(pojo.getKind(), Matchers.equalTo(persistable.getKind().name()));
    Assert.assertThat(pojo.getPollingPeriod(), Matchers.equalTo(0L));
    Assert.assertThat(
        pojo.getParallelismFactor(),
        Matchers.equalTo(persistable.getParallelismFactor().getAsInt()));
  }

  @Test
  public void testWriteToWhenPollingPeriodResolutionIsLessThanMilliseconds() throws Exception {
    exception.expect(InvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*invalid.*polling period.*"));

    final SiteImpl persistable = new SiteImpl();
    final SitePojo pojo = new SitePojo();

    persistable.setName(SiteImplTest.NAME);
    persistable.setDescription(SiteImplTest.DESCRIPTION);
    persistable.setUrl(SiteImplTest.URL);
    persistable.setType(SiteImplTest.TYPE);
    persistable.setKind(SiteImplTest.KIND);
    persistable.setPollingPeriod(Duration.ofSeconds(Long.MAX_VALUE / 1000L + 1L));
    persistable.setParallelismFactor(SiteImplTest.PARALLELISM_FACTOR);

    persistable.writeTo(pojo);
  }

  @Test
  public void testWriteToWhenParallelismFactorIs0() throws Exception {
    final SiteImpl persistable = new SiteImpl();
    final SitePojo pojo = new SitePojo();

    persistable.setName(SiteImplTest.NAME);
    persistable.setDescription(SiteImplTest.DESCRIPTION);
    persistable.setUrl(SiteImplTest.URL);
    persistable.setType(SiteImplTest.TYPE);
    persistable.setKind(SiteImplTest.KIND);
    persistable.setPollingPeriod(SiteImplTest.POLLING_PERIOD);
    persistable.setParallelismFactor(0);

    persistable.writeTo(pojo);

    Assert.assertThat(pojo.getId(), Matchers.equalTo(persistable.getId()));
    Assert.assertThat(pojo.getVersion(), Matchers.equalTo(SitePojo.CURRENT_VERSION));
    Assert.assertThat(pojo.getName(), Matchers.equalTo(persistable.getName()));
    Assert.assertThat(pojo.getDescription(), Matchers.equalTo(persistable.getDescription().get()));
    Assert.assertThat(pojo.getUrl(), Matchers.equalTo(persistable.getUrl().toString()));
    Assert.assertThat(pojo.getType(), Matchers.equalTo(persistable.getType().name()));
    Assert.assertThat(pojo.getKind(), Matchers.equalTo(persistable.getKind().name()));
    Assert.assertThat(
        pojo.getPollingPeriod(), Matchers.equalTo(persistable.getPollingPeriod().get().toMillis()));
    Assert.assertThat(pojo.getParallelismFactor(), Matchers.equalTo(0));
  }

  @Test
  public void testWriteToWhenParallelismFactorIsNegative() throws Exception {
    final SiteImpl persistable = new SiteImpl();
    final SitePojo pojo = new SitePojo();

    persistable.setName(SiteImplTest.NAME);
    persistable.setDescription(SiteImplTest.DESCRIPTION);
    persistable.setUrl(SiteImplTest.URL);
    persistable.setType(SiteImplTest.TYPE);
    persistable.setKind(SiteImplTest.KIND);
    persistable.setPollingPeriod(SiteImplTest.POLLING_PERIOD);
    persistable.setParallelismFactor(-1);

    persistable.writeTo(pojo);

    Assert.assertThat(pojo.getId(), Matchers.equalTo(persistable.getId()));
    Assert.assertThat(pojo.getVersion(), Matchers.equalTo(SitePojo.CURRENT_VERSION));
    Assert.assertThat(pojo.getName(), Matchers.equalTo(persistable.getName()));
    Assert.assertThat(pojo.getDescription(), Matchers.equalTo(persistable.getDescription().get()));
    Assert.assertThat(pojo.getUrl(), Matchers.equalTo(persistable.getUrl().toString()));
    Assert.assertThat(pojo.getType(), Matchers.equalTo(persistable.getType().name()));
    Assert.assertThat(pojo.getKind(), Matchers.equalTo(persistable.getKind().name()));
    Assert.assertThat(
        pojo.getPollingPeriod(), Matchers.equalTo(persistable.getPollingPeriod().get().toMillis()));
    Assert.assertThat(pojo.getParallelismFactor(), Matchers.equalTo(0));
  }

  @Test
  public void testReadFromCurrentVersion() throws Exception {
    final SitePojo pojo =
        new SitePojo()
            .setId(SiteImplTest.ID)
            .setName(SiteImplTest.NAME)
            .setDescription((SiteImplTest.DESCRIPTION))
            .setUrl(SiteImplTest.URL.toString())
            .setType(SiteImplTest.TYPE.name())
            .setKind(SiteImplTest.KIND.name())
            .setPollingPeriod(SiteImplTest.POLLING_PERIOD.toMillis())
            .setParallelismFactor(SiteImplTest.PARALLELISM_FACTOR);
    final SiteImpl persistable = new SiteImpl();

    persistable.readFrom(pojo);

    Assert.assertThat(persistable.getId(), Matchers.equalTo(pojo.getId()));
    Assert.assertThat(persistable.getName(), Matchers.equalTo(pojo.getName()));
    Assert.assertThat(persistable.getDescription().get(), Matchers.equalTo(pojo.getDescription()));
    Assert.assertThat(persistable.getUrl().toString(), Matchers.equalTo(pojo.getUrl()));
    Assert.assertThat(persistable.getType().name(), Matchers.equalTo(pojo.getType()));
    Assert.assertThat(persistable.getKind().name(), Matchers.equalTo(pojo.getKind()));
    Assert.assertThat(persistable.getPollingPeriod(), OptionalMatchers.isPresent());
    Assert.assertThat(
        persistable.getPollingPeriod().get().toMillis(), Matchers.equalTo(pojo.getPollingPeriod()));
    Assert.assertThat(
        persistable.getParallelismFactor().getAsInt(),
        Matchers.equalTo(pojo.getParallelismFactor()));
  }

  @Test
  public void testReadFromFutureVersion() throws Exception {
    final SitePojo pojo =
        new SitePojo()
            .setId(SiteImplTest.ID)
            .setVersion(9999999)
            .setName(SiteImplTest.NAME)
            .setDescription((SiteImplTest.DESCRIPTION))
            .setUrl(SiteImplTest.URL.toString())
            .setType(SiteImplTest.TYPE.name())
            .setKind(SiteImplTest.KIND.name())
            .setPollingPeriod(SiteImplTest.POLLING_PERIOD.toMillis())
            .setParallelismFactor(SiteImplTest.PARALLELISM_FACTOR);
    final SiteImpl persistable = new SiteImpl();

    persistable.readFrom(pojo);

    Assert.assertThat(persistable.getId(), Matchers.equalTo(pojo.getId()));
    Assert.assertThat(persistable.getName(), Matchers.equalTo(pojo.getName()));
    Assert.assertThat(persistable.getDescription().get(), Matchers.equalTo(pojo.getDescription()));
    Assert.assertThat(persistable.getUrl().toString(), Matchers.equalTo(pojo.getUrl()));
    Assert.assertThat(persistable.getType().name(), Matchers.equalTo(pojo.getType()));
    Assert.assertThat(persistable.getKind().name(), Matchers.equalTo(pojo.getKind()));
    Assert.assertThat(persistable.getPollingPeriod(), OptionalMatchers.isPresent());
    Assert.assertThat(
        persistable.getPollingPeriod().get().toMillis(), Matchers.equalTo(pojo.getPollingPeriod()));
    Assert.assertThat(
        persistable.getParallelismFactor().getAsInt(),
        Matchers.equalTo(pojo.getParallelismFactor()));
  }

  @Test
  public void testReadFromUnsupportedVersion() throws Exception {
    exception.expect(UnsupportedVersionException.class);
    exception.expectMessage(Matchers.matchesPattern(".*unsupported.*version.*"));

    final SitePojo pojo = new SitePojo().setVersion(-1).setId(SiteImplTest.ID);
    final SiteImpl persistable = new SiteImpl();

    persistable.readFrom(pojo);
  }

  @Test
  public void testReadFromCurrentVersionWithNullName() throws Exception {
    exception.expect(InvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*missing.*name.*"));

    final SitePojo pojo =
        new SitePojo()
            .setId(SiteImplTest.ID)
            .setDescription((SiteImplTest.DESCRIPTION))
            .setUrl(SiteImplTest.URL.toString())
            .setType(SiteImplTest.TYPE.name())
            .setKind(SiteImplTest.KIND.name())
            .setPollingPeriod(SiteImplTest.POLLING_PERIOD.toMillis())
            .setParallelismFactor(SiteImplTest.PARALLELISM_FACTOR);
    final SiteImpl persistable = new SiteImpl();

    persistable.readFrom(pojo);
  }

  @Test
  public void testReadFromCurrentVersionWithEmptyName() throws Exception {
    exception.expect(InvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*empty.*name.*"));

    final SitePojo pojo =
        new SitePojo()
            .setId(SiteImplTest.ID)
            .setName("")
            .setDescription((SiteImplTest.DESCRIPTION))
            .setUrl(SiteImplTest.URL.toString())
            .setType(SiteImplTest.TYPE.name())
            .setKind(SiteImplTest.KIND.name())
            .setPollingPeriod(SiteImplTest.POLLING_PERIOD.toMillis())
            .setParallelismFactor(SiteImplTest.PARALLELISM_FACTOR);
    final SiteImpl persistable = new SiteImpl();

    persistable.readFrom(pojo);
  }

  @Test
  public void testReadFromCurrentVersionWithNullUrl() throws Exception {
    exception.expect(InvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*missing.*url.*"));

    final SitePojo pojo =
        new SitePojo()
            .setId(SiteImplTest.ID)
            .setName(SiteImplTest.NAME)
            .setDescription((SiteImplTest.DESCRIPTION))
            .setType(SiteImplTest.TYPE.name())
            .setKind(SiteImplTest.KIND.name())
            .setPollingPeriod(SiteImplTest.POLLING_PERIOD.toMillis())
            .setParallelismFactor(SiteImplTest.PARALLELISM_FACTOR);
    final SiteImpl persistable = new SiteImpl();

    persistable.readFrom(pojo);
  }

  @Test
  public void testReadFromCurrentVersionWithEmptyUrl() throws Exception {
    exception.expect(InvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*empty.*url.*"));

    final SitePojo pojo =
        new SitePojo()
            .setId(SiteImplTest.ID)
            .setName(SiteImplTest.NAME)
            .setDescription((SiteImplTest.DESCRIPTION))
            .setUrl("")
            .setType(SiteImplTest.TYPE.name())
            .setKind(SiteImplTest.KIND.name())
            .setPollingPeriod(SiteImplTest.POLLING_PERIOD.toMillis())
            .setParallelismFactor(SiteImplTest.PARALLELISM_FACTOR);
    final SiteImpl persistable = new SiteImpl();

    persistable.readFrom(pojo);
  }

  @Test
  public void testReadFromCurrentVersionWithInvalidUrl() throws Exception {
    exception.expect(InvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*invalid.*url.*"));
    exception.expectCause(Matchers.instanceOf(MalformedURLException.class));

    final SitePojo pojo =
        new SitePojo()
            .setId(SiteImplTest.ID)
            .setName(SiteImplTest.NAME)
            .setDescription((SiteImplTest.DESCRIPTION))
            .setUrl("invalid-url")
            .setType(SiteImplTest.TYPE.name())
            .setKind(SiteImplTest.KIND.name())
            .setPollingPeriod(SiteImplTest.POLLING_PERIOD.toMillis())
            .setParallelismFactor(SiteImplTest.PARALLELISM_FACTOR);
    final SiteImpl persistable = new SiteImpl();

    persistable.readFrom(pojo);
  }

  @Test
  public void testReadFromCurrentVersionWithNullType() throws Exception {
    exception.expect(InvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*missing.*type.*"));

    final SitePojo pojo =
        new SitePojo()
            .setId(SiteImplTest.ID)
            .setName(SiteImplTest.NAME)
            .setDescription((SiteImplTest.DESCRIPTION))
            .setUrl(SiteImplTest.URL.toString())
            .setKind(SiteImplTest.KIND.name())
            .setPollingPeriod(SiteImplTest.POLLING_PERIOD.toMillis())
            .setParallelismFactor(SiteImplTest.PARALLELISM_FACTOR);
    final SiteImpl persistable = new SiteImpl();

    persistable.readFrom(pojo);
  }

  @Test
  public void testReadFromCurrentVersionWithEmptyType() throws Exception {
    exception.expect(InvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*empty.*type.*"));

    final SitePojo pojo =
        new SitePojo()
            .setId(SiteImplTest.ID)
            .setName(SiteImplTest.NAME)
            .setDescription((SiteImplTest.DESCRIPTION))
            .setUrl(SiteImplTest.URL.toString())
            .setType("")
            .setKind(SiteImplTest.KIND.name())
            .setPollingPeriod(SiteImplTest.POLLING_PERIOD.toMillis())
            .setParallelismFactor(SiteImplTest.PARALLELISM_FACTOR);
    final SiteImpl persistable = new SiteImpl();

    persistable.readFrom(pojo);
  }

  @Test
  public void testReadFromCurrentVersionWithNewType() throws Exception {
    final SitePojo pojo =
        new SitePojo()
            .setId(SiteImplTest.ID)
            .setName(SiteImplTest.NAME)
            .setDescription((SiteImplTest.DESCRIPTION))
            .setUrl(SiteImplTest.URL.toString())
            .setType("some-new-type")
            .setKind(SiteImplTest.KIND.name())
            .setPollingPeriod(SiteImplTest.POLLING_PERIOD.toMillis())
            .setParallelismFactor(SiteImplTest.PARALLELISM_FACTOR);
    final SiteImpl persistable = new SiteImpl();

    persistable.readFrom(pojo);

    Assert.assertThat(persistable.getId(), Matchers.equalTo(pojo.getId()));
    Assert.assertThat(persistable.getName(), Matchers.equalTo(pojo.getName()));
    Assert.assertThat(persistable.getDescription().get(), Matchers.equalTo(pojo.getDescription()));
    Assert.assertThat(persistable.getUrl().toString(), Matchers.equalTo(pojo.getUrl()));
    Assert.assertThat(persistable.getType(), Matchers.equalTo(SiteType.UNKNOWN));
    Assert.assertThat(persistable.getKind().name(), Matchers.equalTo(pojo.getKind()));
    Assert.assertThat(persistable.getPollingPeriod(), OptionalMatchers.isPresent());
    Assert.assertThat(
        persistable.getPollingPeriod().get().toMillis(), Matchers.equalTo(pojo.getPollingPeriod()));
    Assert.assertThat(
        persistable.getParallelismFactor().getAsInt(),
        Matchers.equalTo(pojo.getParallelismFactor()));
  }

  @Test
  public void testReadFromCurrentVersionWithNullKind() throws Exception {
    exception.expect(InvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*missing.*kind.*"));

    final SitePojo pojo =
        new SitePojo()
            .setId(SiteImplTest.ID)
            .setName(SiteImplTest.NAME)
            .setDescription((SiteImplTest.DESCRIPTION))
            .setUrl(SiteImplTest.URL.toString())
            .setType(SiteImplTest.TYPE.name())
            .setPollingPeriod(SiteImplTest.POLLING_PERIOD.toMillis())
            .setParallelismFactor(SiteImplTest.PARALLELISM_FACTOR);
    final SiteImpl persistable = new SiteImpl();

    persistable.readFrom(pojo);
  }

  @Test
  public void testReadFromCurrentVersionWithEmptyKind() throws Exception {
    exception.expect(InvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*empty.*kind.*"));

    final SitePojo pojo =
        new SitePojo()
            .setId(SiteImplTest.ID)
            .setName(SiteImplTest.NAME)
            .setDescription((SiteImplTest.DESCRIPTION))
            .setUrl(SiteImplTest.URL.toString())
            .setType(SiteImplTest.TYPE.name())
            .setKind("")
            .setPollingPeriod(SiteImplTest.POLLING_PERIOD.toMillis())
            .setParallelismFactor(SiteImplTest.PARALLELISM_FACTOR);
    final SiteImpl persistable = new SiteImpl();

    persistable.readFrom(pojo);
  }

  @Test
  public void testReadFromCurrentVersionWithNewKind() throws Exception {
    final SitePojo pojo =
        new SitePojo()
            .setId(SiteImplTest.ID)
            .setName(SiteImplTest.NAME)
            .setDescription((SiteImplTest.DESCRIPTION))
            .setUrl(SiteImplTest.URL.toString())
            .setType(SiteImplTest.TYPE.name())
            .setKind("some-new-kind")
            .setPollingPeriod(SiteImplTest.POLLING_PERIOD.toMillis())
            .setParallelismFactor(SiteImplTest.PARALLELISM_FACTOR);
    final SiteImpl persistable = new SiteImpl();

    persistable.readFrom(pojo);

    Assert.assertThat(persistable.getId(), Matchers.equalTo(pojo.getId()));
    Assert.assertThat(persistable.getName(), Matchers.equalTo(pojo.getName()));
    Assert.assertThat(persistable.getDescription().get(), Matchers.equalTo(pojo.getDescription()));
    Assert.assertThat(persistable.getUrl().toString(), Matchers.equalTo(pojo.getUrl()));
    Assert.assertThat(persistable.getType().name(), Matchers.equalTo(pojo.getType()));
    Assert.assertThat(persistable.getKind(), Matchers.equalTo(SiteKind.UNKNOWN));
    Assert.assertThat(persistable.getPollingPeriod(), OptionalMatchers.isPresent());
    Assert.assertThat(
        persistable.getPollingPeriod().get().toMillis(), Matchers.equalTo(pojo.getPollingPeriod()));
    Assert.assertThat(
        persistable.getParallelismFactor().getAsInt(),
        Matchers.equalTo(pojo.getParallelismFactor()));
  }

  @Test
  public void testReadFromCurrentVersionWithUndefinedParallelismFactor_0() throws Exception {
    final SitePojo pojo =
        new SitePojo()
            .setId(SiteImplTest.ID)
            .setName(SiteImplTest.NAME)
            .setDescription((SiteImplTest.DESCRIPTION))
            .setUrl(SiteImplTest.URL.toString())
            .setType(SiteImplTest.TYPE.name())
            .setKind(SiteImplTest.KIND.name())
            .setPollingPeriod(SiteImplTest.POLLING_PERIOD.toMillis())
            .setParallelismFactor(0);
    final SiteImpl persistable = new SiteImpl();

    persistable.readFrom(pojo);

    Assert.assertThat(persistable.getId(), Matchers.equalTo(pojo.getId()));
    Assert.assertThat(persistable.getName(), Matchers.equalTo(pojo.getName()));
    Assert.assertThat(persistable.getDescription().get(), Matchers.equalTo(pojo.getDescription()));
    Assert.assertThat(persistable.getUrl().toString(), Matchers.equalTo(pojo.getUrl()));
    Assert.assertThat(persistable.getType().name(), Matchers.equalTo(pojo.getType()));
    Assert.assertThat(persistable.getKind().name(), Matchers.equalTo(pojo.getKind()));
    Assert.assertThat(persistable.getPollingPeriod(), OptionalMatchers.isPresent());
    Assert.assertThat(
        persistable.getPollingPeriod().get().toMillis(), Matchers.equalTo(pojo.getPollingPeriod()));
    Assert.assertThat(persistable.getParallelismFactor().isEmpty(), Matchers.equalTo(true));
  }

  @Test
  public void testReadFromCurrentVersionWithUndefinedParallelismFactor_Negative() throws Exception {
    final SitePojo pojo =
        new SitePojo()
            .setId(SiteImplTest.ID)
            .setName(SiteImplTest.NAME)
            .setDescription((SiteImplTest.DESCRIPTION))
            .setUrl(SiteImplTest.URL.toString())
            .setType(SiteImplTest.TYPE.name())
            .setKind(SiteImplTest.KIND.name())
            .setPollingPeriod(SiteImplTest.POLLING_PERIOD.toMillis())
            .setParallelismFactor(-1);
    final SiteImpl persistable = new SiteImpl();

    persistable.readFrom(pojo);

    Assert.assertThat(persistable.getId(), Matchers.equalTo(pojo.getId()));
    Assert.assertThat(persistable.getName(), Matchers.equalTo(pojo.getName()));
    Assert.assertThat(persistable.getDescription().get(), Matchers.equalTo(pojo.getDescription()));
    Assert.assertThat(persistable.getUrl().toString(), Matchers.equalTo(pojo.getUrl()));
    Assert.assertThat(persistable.getType().name(), Matchers.equalTo(pojo.getType()));
    Assert.assertThat(persistable.getKind().name(), Matchers.equalTo(pojo.getKind()));
    Assert.assertThat(persistable.getPollingPeriod(), OptionalMatchers.isPresent());
    Assert.assertThat(
        persistable.getPollingPeriod().get().toMillis(), Matchers.equalTo(pojo.getPollingPeriod()));
    Assert.assertThat(persistable.getParallelismFactor().isEmpty(), Matchers.equalTo(true));
  }

  @Test
  public void testHashCodeWhenEquals() throws Exception {
    final SiteImpl persistable2 = new SiteImpl();

    persistable2.setId(SiteImplTest.ID);
    persistable2.setName(SiteImplTest.NAME);
    persistable2.setDescription(SiteImplTest.DESCRIPTION);
    persistable2.setUrl(SiteImplTest.URL);
    persistable2.setType(SiteImplTest.TYPE);
    persistable2.setKind(SiteImplTest.KIND);
    persistable2.setPollingPeriod(SiteImplTest.POLLING_PERIOD);
    persistable2.setParallelismFactor(SiteImplTest.PARALLELISM_FACTOR);

    Assert.assertThat(persistable.hashCode(), Matchers.equalTo(persistable2.hashCode()));
  }

  @Test
  public void testHashCodeWhenDifferent() throws Exception {
    final SiteImpl persistable2 = new SiteImpl();

    persistable2.setId(SiteImplTest.ID + "2");
    persistable2.setName(SiteImplTest.NAME);
    persistable2.setDescription(SiteImplTest.DESCRIPTION);
    persistable2.setUrl(SiteImplTest.URL);
    persistable2.setType(SiteImplTest.TYPE);
    persistable2.setKind(SiteImplTest.KIND);
    persistable2.setPollingPeriod(SiteImplTest.POLLING_PERIOD);
    persistable2.setParallelismFactor(SiteImplTest.PARALLELISM_FACTOR);

    Assert.assertThat(
        persistable.hashCode(), Matchers.not(Matchers.equalTo(persistable2.hashCode())));
  }

  @Test
  public void testEqualsWhenEquals() throws Exception {
    final SiteImpl persistable2 = new SiteImpl();

    persistable2.setId(SiteImplTest.ID);
    persistable2.setName(SiteImplTest.NAME);
    persistable2.setDescription(SiteImplTest.DESCRIPTION);
    persistable2.setUrl(SiteImplTest.URL);
    persistable2.setType(SiteImplTest.TYPE);
    persistable2.setKind(SiteImplTest.KIND);
    persistable2.setPollingPeriod(SiteImplTest.POLLING_PERIOD);
    persistable2.setParallelismFactor(SiteImplTest.PARALLELISM_FACTOR);

    Assert.assertThat(persistable.equals(persistable2), Matchers.equalTo(true));
  }

  @Test
  public void testEqualsWhenIdentical() throws Exception {
    Assert.assertThat(persistable.equals(persistable), Matchers.equalTo(true));
  }

  @SuppressWarnings("PMD.EqualsNull" /* purposely testing equals() when called with null */)
  @Test
  public void testEqualsWhenNull() throws Exception {
    Assert.assertThat(persistable.equals(null), Matchers.equalTo(false));
  }

  @SuppressWarnings(
      "PMD.PositionLiteralsFirstInComparisons" /* purposely testing equals() when call with something else than expected */)
  @Test
  public void testEqualsWhenNotASitePojo() throws Exception {
    Assert.assertThat(persistable.equals("test"), Matchers.equalTo(false));
  }

  @Test
  public void testEqualsWhenIdIsDifferent() throws Exception {
    final SiteImpl persistable2 = new SiteImpl();

    persistable2.setId(SiteImplTest.ID + "2");
    persistable2.setName(SiteImplTest.NAME);
    persistable2.setDescription(SiteImplTest.DESCRIPTION);
    persistable2.setUrl(SiteImplTest.URL);
    persistable2.setType(SiteImplTest.TYPE);
    persistable2.setKind(SiteImplTest.KIND);
    persistable2.setPollingPeriod(SiteImplTest.POLLING_PERIOD);
    persistable2.setParallelismFactor(SiteImplTest.PARALLELISM_FACTOR);

    Assert.assertThat(persistable.equals(persistable2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenNameIsDifferent() throws Exception {
    final SiteImpl persistable2 = new SiteImpl();

    persistable2.setId(SiteImplTest.ID);
    persistable2.setName(SiteImplTest.NAME + "2");
    persistable2.setDescription(SiteImplTest.DESCRIPTION);
    persistable2.setUrl(SiteImplTest.URL);
    persistable2.setType(SiteImplTest.TYPE);
    persistable2.setKind(SiteImplTest.KIND);
    persistable2.setPollingPeriod(SiteImplTest.POLLING_PERIOD);
    persistable2.setParallelismFactor(SiteImplTest.PARALLELISM_FACTOR);

    Assert.assertThat(persistable.equals(persistable2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenDescriptionIsDifferent() throws Exception {
    final SiteImpl persistable2 = new SiteImpl();

    persistable2.setId(SiteImplTest.ID);
    persistable2.setName(SiteImplTest.NAME);
    persistable2.setDescription(SiteImplTest.DESCRIPTION + "2");
    persistable2.setUrl(SiteImplTest.URL);
    persistable2.setType(SiteImplTest.TYPE);
    persistable2.setKind(SiteImplTest.KIND);
    persistable2.setPollingPeriod(SiteImplTest.POLLING_PERIOD);
    persistable2.setParallelismFactor(SiteImplTest.PARALLELISM_FACTOR);

    Assert.assertThat(persistable.equals(persistable2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenUrlIsDifferent() throws Exception {
    final SiteImpl persistable2 = new SiteImpl();

    persistable2.setId(SiteImplTest.ID);
    persistable2.setName(SiteImplTest.NAME);
    persistable2.setDescription(SiteImplTest.DESCRIPTION);
    persistable2.setUrl(SiteImplTest.URL2);
    persistable2.setType(SiteImplTest.TYPE);
    persistable2.setKind(SiteImplTest.KIND);
    persistable2.setPollingPeriod(SiteImplTest.POLLING_PERIOD);
    persistable2.setParallelismFactor(SiteImplTest.PARALLELISM_FACTOR);

    Assert.assertThat(persistable.equals(persistable2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenTypesIsDifferent() throws Exception {
    final SiteImpl persistable2 = new SiteImpl();

    persistable2.setId(SiteImplTest.ID);
    persistable2.setName(SiteImplTest.NAME);
    persistable2.setDescription(SiteImplTest.DESCRIPTION);
    persistable2.setUrl(SiteImplTest.URL);
    persistable2.setType(SiteType.UNKNOWN);
    persistable2.setKind(SiteImplTest.KIND);
    persistable2.setPollingPeriod(SiteImplTest.POLLING_PERIOD);
    persistable2.setParallelismFactor(SiteImplTest.PARALLELISM_FACTOR);

    Assert.assertThat(persistable.equals(persistable2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenKindIsDifferent() throws Exception {
    final SiteImpl persistable2 = new SiteImpl();

    persistable2.setId(SiteImplTest.ID);
    persistable2.setName(SiteImplTest.NAME);
    persistable2.setDescription(SiteImplTest.DESCRIPTION);
    persistable2.setUrl(SiteImplTest.URL);
    persistable2.setType(SiteImplTest.TYPE);
    persistable2.setKind(SiteKind.UNKNOWN);
    persistable2.setPollingPeriod(SiteImplTest.POLLING_PERIOD);
    persistable2.setParallelismFactor(SiteImplTest.PARALLELISM_FACTOR);

    Assert.assertThat(persistable.equals(persistable2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenPollingPeriodIsDifferent() throws Exception {
    final SiteImpl persistable2 = new SiteImpl();

    persistable2.setId(SiteImplTest.ID);
    persistable2.setName(SiteImplTest.NAME);
    persistable2.setDescription(SiteImplTest.DESCRIPTION);
    persistable2.setUrl(SiteImplTest.URL);
    persistable2.setType(SiteImplTest.TYPE);
    persistable2.setKind(SiteImplTest.KIND);
    persistable2.setPollingPeriod(Duration.ZERO);
    persistable2.setParallelismFactor(SiteImplTest.PARALLELISM_FACTOR);

    Assert.assertThat(persistable.equals(persistable2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenParallelismFactorIsDifferent() throws Exception {
    final SiteImpl persistable2 = new SiteImpl();

    persistable2.setId(SiteImplTest.ID);
    persistable2.setName(SiteImplTest.NAME);
    persistable2.setDescription(SiteImplTest.DESCRIPTION);
    persistable2.setUrl(SiteImplTest.URL);
    persistable2.setType(SiteImplTest.TYPE);
    persistable2.setKind(SiteImplTest.KIND);
    persistable2.setPollingPeriod(SiteImplTest.POLLING_PERIOD);
    persistable2.setParallelismFactor(SiteImplTest.PARALLELISM_FACTOR + 2);

    Assert.assertThat(persistable.equals(persistable2), Matchers.not(Matchers.equalTo(true)));
  }
}
