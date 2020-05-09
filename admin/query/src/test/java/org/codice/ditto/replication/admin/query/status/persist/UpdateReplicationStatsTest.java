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
package org.codice.ditto.replication.admin.query.status.persist;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.sql.Date;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codice.ddf.admin.api.report.ErrorMessage;
import org.codice.ddf.admin.api.report.FunctionReport;
import org.codice.ditto.replication.admin.query.ReplicationUtils;
import org.codice.ditto.replication.admin.query.status.fields.ReplicationStats;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateReplicationStatsTest {

  private static final String FUNCTION_PATH = "functionPath";

  private static final String STATS_PATH = "stats";

  private static final String PID = "pid";

  private static final String START_TIME = "2017-04-20T15:25:46.327Z";

  private static final String LAST_RUN = "2016-04-20T15:25:46.327Z";

  private static final String LAST_SUCCESS = "2015-04-20T15:25:46.327Z";

  private static final long DURATION = 5;

  private static final String STATUS = "SUCCESS";

  private static final long PUSH_COUNT = 1;

  private static final long PULL_COUNT = 2;

  private static final long PUSH_FAIL_COUNT = 3;

  private static final long PULL_FAIL_COUNT = 4;

  private static final long PUSH_BYTES = 5;

  private static final long PULL_BYTES = 6;

  private static final String REPLICATION_NAME = "repName";

  private UpdateReplicationStats updateReplicationStats;

  @Mock ReplicationUtils replicationUtils;

  private Map<String, Object> args;

  private ReplicationStats replicationStats;

  @Before
  public void setup() {
    updateReplicationStats = new UpdateReplicationStats(replicationUtils);

    replicationStats = new ReplicationStats();
    replicationStats.setPid(PID);
    replicationStats.setStartTime(Date.from(Instant.parse(START_TIME)));
    replicationStats.setLastRun(Date.from(Instant.parse(LAST_RUN)));
    replicationStats.setLastSuccess(Date.from(Instant.parse(LAST_SUCCESS)));
    replicationStats.setDuration(DURATION);
    replicationStats.setStatus(STATUS);
    replicationStats.setPushCount(PUSH_COUNT);
    replicationStats.setPullCount(PULL_COUNT);
    replicationStats.setPushFailCount(PUSH_FAIL_COUNT);
    replicationStats.setPullFailCount(PULL_FAIL_COUNT);
    replicationStats.setPushBytes(PUSH_BYTES);
    replicationStats.setPullBytes(PULL_BYTES);

    args = new HashMap<>();
    args.put("stats", replicationStats.getValue());
    args.put("replicationName", REPLICATION_NAME);
  }

  @Test
  public void testUpdateReplicationStats() {
    // setup
    when(replicationUtils.replicationConfigExists(REPLICATION_NAME)).thenReturn(true);
    when(replicationUtils.updateReplicationStats(any(), any())).thenReturn(true);

    // when
    boolean result = updateReplicationStats.execute(args, null).getResult().getValue();

    // then
    assertThat(result, is(true));
  }

  @Test
  public void testReplicationNameDoesNotExist() {
    // setup
    when(replicationUtils.replicationConfigExists(REPLICATION_NAME)).thenReturn(false);

    // when
    FunctionReport report = updateReplicationStats.execute(args, ImmutableList.of(FUNCTION_PATH));
    List<ErrorMessage> errors = report.getErrorMessages();

    // then
    assertThat(report.getResult(), is(nullValue()));
    assertThat(errors.size(), is(1));
    assertThat(errors.get(0).getCode(), is("NO_EXISTING_CONFIG"));
    assertThat(errors.get(0).getPath(), is(ImmutableList.of(FUNCTION_PATH)));
  }

  @Test
  public void testMissingRequiredArgs() {
    // when
    FunctionReport report = updateReplicationStats.execute(null, ImmutableList.of(FUNCTION_PATH));
    List<ErrorMessage> errors = report.getErrorMessages();

    // then
    assertThat(report.getResult(), is(nullValue()));
    assertThat(errors.size(), is(12));
    assertRequiredArg(
        errors.get(0).getCode(),
        errors.get(0).getPath(),
        ImmutableList.of(FUNCTION_PATH, "replicationName"));
    assertRequiredArg(
        errors.get(1).getCode(),
        errors.get(1).getPath(),
        ImmutableList.of(FUNCTION_PATH, STATS_PATH, "pid"));
    assertRequiredArg(
        errors.get(2).getCode(),
        errors.get(2).getPath(),
        ImmutableList.of(FUNCTION_PATH, STATS_PATH, "startTime"));
    assertRequiredArg(
        errors.get(3).getCode(),
        errors.get(3).getPath(),
        ImmutableList.of(FUNCTION_PATH, STATS_PATH, "lastRun"));
    assertRequiredArg(
        errors.get(4).getCode(),
        errors.get(4).getPath(),
        ImmutableList.of(FUNCTION_PATH, STATS_PATH, "duration"));
    assertRequiredArg(
        errors.get(5).getCode(),
        errors.get(5).getPath(),
        ImmutableList.of(FUNCTION_PATH, STATS_PATH, "replicationStatus"));
    assertRequiredArg(
        errors.get(6).getCode(),
        errors.get(6).getPath(),
        ImmutableList.of(FUNCTION_PATH, STATS_PATH, "pushCount"));
    assertRequiredArg(
        errors.get(7).getCode(),
        errors.get(7).getPath(),
        ImmutableList.of(FUNCTION_PATH, STATS_PATH, "pullCount"));
    assertRequiredArg(
        errors.get(8).getCode(),
        errors.get(8).getPath(),
        ImmutableList.of(FUNCTION_PATH, STATS_PATH, "pushFailCount"));
    assertRequiredArg(
        errors.get(9).getCode(),
        errors.get(9).getPath(),
        ImmutableList.of(FUNCTION_PATH, STATS_PATH, "pullFailCount"));
    assertRequiredArg(
        errors.get(10).getCode(),
        errors.get(10).getPath(),
        ImmutableList.of(FUNCTION_PATH, STATS_PATH, "pushBytes"));
    assertRequiredArg(
        errors.get(11).getCode(),
        errors.get(11).getPath(),
        ImmutableList.of(FUNCTION_PATH, STATS_PATH, "pullBytes"));
  }

  private void assertRequiredArg(String code, List<Object> path, List<Object> expectedPath) {
    assertThat(code, is("MISSING_REQUIRED_FIELD"));
    assertThat(path, is(expectedPath));
  }
}
