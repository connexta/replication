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
package org.codice.ditto.replication.dominion.pax.exam.options.extensions;

import org.codice.ddf.dominion.commons.options.DDFCommonOptions;
import org.codice.ditto.replication.dominion.options.ReplicationOptions.Install;
import org.codice.dominion.pax.exam.interpolate.PaxExamInterpolator;
import org.codice.dominion.pax.exam.options.PaxExamOption.Extension;
import org.codice.dominion.resources.ResourceLoader;
import org.codice.maven.MavenUrl;
import org.ops4j.pax.exam.Option;

/**
 * Extension point for the {@link Install} option annotation capable of installing DDF's
 * distribution along with the replication feature.
 */
@DDFCommonOptions.AddPolicyFile(
  name = "replication",
  artifact =
      @MavenUrl(
        groupId = "replication.distributions.security",
        artifactId = "replication",
        version = MavenUrl.AS_IN_PROJECT,
        type = "policy"
      )
)
public class InstallExtension implements Extension<Install> {
  @Override
  public Option[] options(
      Install annotation, PaxExamInterpolator interpolator, ResourceLoader resourceLoader) {
    return new Option[0];
  }
}
