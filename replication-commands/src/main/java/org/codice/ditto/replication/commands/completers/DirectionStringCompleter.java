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
package org.codice.ditto.replication.commands.completers;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.CommandLine;
import org.apache.karaf.shell.api.console.Completer;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.support.completers.StringsCompleter;
import org.codice.ditto.replication.api.Direction;

@Service
public class DirectionStringCompleter implements Completer {

  @Override
  public int complete(Session session, CommandLine commandLine, List<String> candidates) {
    final StringsCompleter delegate = new StringsCompleter();
    delegate
        .getStrings()
        .addAll(Arrays.stream(Direction.values()).map(Enum::name).collect(Collectors.toSet()));
    return delegate.complete(session, commandLine, candidates);
  }
}
