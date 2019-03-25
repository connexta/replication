/**
 * Copyright (c) Codice Foundation
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
package org.codice.ditto.replication.itests;

import org.codice.ddf.dominion.commons.options.DDFCommonOptions;
import org.codice.ditto.replication.dominion.options.ReplicationOptions;
import org.codice.dominion.Dominion;
import org.codice.dominion.________________________________Dominion;
import org.codice.junit.___________________________________JUnit;
import org.codice.maven.MavenUrl;
import org.codice.pax.exam.junit.ConfigurationAdmin;
import org.codice.pax.exam.junit.ServiceAdmin;
import org.codice.pax.exam.junit.__________________________Configurations;
import org.codice.pax.exam.junit.________________________________Services;
import org.codice.pax.exam.service.Feature;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@________________________________Dominion
@DDFCommonOptions.ConfigureVMOptionsForTesting
@DDFCommonOptions.ConfigureDebugging
@DDFCommonOptions.ConfigurePorts
@DDFCommonOptions.ConfigureLogging
@ReplicationOptions.Install
@___________________________________JUnit
@RunWith(Dominion.class)
@________________________________Services
@ServiceAdmin
@Feature.Start(
    repository =
        @MavenUrl(
            groupId = "replication.distributions.features",
            artifactId = "replication",
            version = MavenUrl.AS_IN_PROJECT,
            type = "xml",
            classifier = "features"),
    name = "replication")
@__________________________Configurations
@ConfigurationAdmin
public class ITStart {
  @BeforeClass
  public static void setupClass() {
    System.out.println("*** SETUP CLASS");
  }

  @AfterClass
  public static void cleanupClass() {
    System.out.println("*** AFTER CLASS");
  }

  @Before
  public void setup() {
    System.out.println("*** SETUP");
  }

  @After
  public void cleanup() {
    System.out.println("*** AFTER");
  }

  @Test
  public void test1() throws Exception {
    System.out.println("*** TEST 1");
  }

  @Test
  public void test3() throws Exception {
    System.out.println("*** TEST 3");
  }
}
