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
package com.connexta.replication.distributions.test;

import static com.connexta.replication.distributions.test.TestUtils.getProjectVersion;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.RabbitMQContainer;

public class RabbitMQIntegrationTest {

  private static final String IMAGE =
      "r.ion.phx.connexta.com/replication-rabbitmq:" + getProjectVersion();

  private static final int AMQP_PORT = 5672;

  private static final int HTTP_PORT = 15672;

  @ClassRule public static RabbitMQContainer rabbitmq = new RabbitMQContainer(IMAGE);

  @Test
  public void testContainerStartsUp() {
    assertThat(rabbitmq.getDockerImageName(), is(IMAGE));
    assertThat(rabbitmq.getAdminPassword(), is("guest"));
    assertThat(rabbitmq.getAdminUsername(), is("guest"));

    rabbitmq.start();

    assertThat(
        rabbitmq.getAmqpUrl(),
        is(
            String.format(
                "amqp://%s:%d",
                rabbitmq.getContainerIpAddress(), rabbitmq.getMappedPort(AMQP_PORT))));
    assertThat(
        rabbitmq.getHttpUrl(),
        is(
            String.format(
                "http://%s:%d",
                rabbitmq.getContainerIpAddress(), rabbitmq.getMappedPort(HTTP_PORT))));

    assertThat(rabbitmq.getAmqpPort(), is(rabbitmq.getMappedPort(AMQP_PORT)));
    assertThat(rabbitmq.getHttpPort(), is(rabbitmq.getMappedPort(HTTP_PORT)));
  }
}
