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
package org.codice.ditto.replication.dominion.options;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;
import org.codice.ddf.dominion.options.DDFOptions;
import org.codice.dominion.options.Option;
import org.codice.dominion.options.karaf.KarafOptions;
import org.codice.maven.MavenUrl;

/**
 * This class defines annotations that can be used to configure Dominion containers. It is solely
 * used for scoping.
 */
public class ReplicationOptions {
  /** Dominion option for installing replication. */
  @DDFOptions.InstallDistribution(solr = true)
  // The replication feature cannot be currently installed as a boot feature as it
  // relies on DDF features to already be started. We shall be forced to install the feature via
  // the SSH console after DDF has been installed with the above annotation
  @KarafOptions.InstallFeature(
    repository =
        @MavenUrl(
          groupId = "replication.distributions.features",
          artifactId = "replication",
          version = MavenUrl.AS_IN_PROJECT,
          type = "xml",
          classifier = "features"
        ),
    boot = false,
    timeout = 10L,
    units = TimeUnit.MINUTES
  )
  @Option.Annotation
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @Documented
  public @interface Install {}

  private ReplicationOptions() {}
}
