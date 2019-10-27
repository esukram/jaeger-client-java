/*
 * Copyright (c) 2018, The Jaeger Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package io.jaegertracing.thrift.internal.senders;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.object.IsCompatibleType.typeCompatibleWith;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Properties;
import io.jaegertracing.Configuration;
import io.jaegertracing.internal.senders.NoopSender;
import io.jaegertracing.spi.Sender;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ThriftSenderFactoryTest {

  private static Properties originalProps;

  @BeforeClass
  public static void beforeClass() {
    originalProps = new Properties(System.getProperties());
  }

  @AfterClass
  public static void afterClass() {
    System.setProperties(originalProps);
  }

  @Before
  public void setup() {
    System.clearProperty(Configuration.JAEGER_ENDPOINT);
    System.clearProperty(Configuration.JAEGER_AUTH_TOKEN);
    System.clearProperty(Configuration.JAEGER_USER);
    System.clearProperty(Configuration.JAEGER_PASSWORD);

    System.clearProperty(Configuration.JAEGER_AGENT_HOST);
    System.clearProperty(Configuration.JAEGER_AGENT_PORT);
  }

  @Test
  public void testSenderWithEndpointWithoutAuthData() {
    System.setProperty(Configuration.JAEGER_ENDPOINT, "https://jaeger-collector:14268/api/traces");

    Sender sender = Configuration.SenderConfiguration.fromEnv().getSender();

    assertThat(sender.getClass(), is(typeCompatibleWith(HttpSender.class)));
  }

  @Test
  public void testSenderWithAgentDataFromEnv() {
    System.setProperty(Configuration.JAEGER_AGENT_HOST, "jaeger-agent");
    System.setProperty(Configuration.JAEGER_AGENT_PORT, "6832");

    Sender sender = Configuration.SenderConfiguration.fromEnv().getSender();

    assertThat(sender.getClass(), is(typeCompatibleWith(NoopSender.class)));
  }

  @Test
  public void testSenderWithBasicAuthUsesHttpSender() {
    Sender sender = new Configuration.SenderConfiguration()
      .withEndpoint("https://jaeger-collector:14268/api/traces")
      .withAuthUsername("username")
      .withAuthPassword("password")
      .getSender();

    assertThat(sender.getClass(), is(typeCompatibleWith(HttpSender.class)));
  }
  
  @Test
  public void testSenderWithAuthTokenUsesHttpSender() {
    Sender sender = new Configuration.SenderConfiguration()
      .withEndpoint("https://jaeger-collector:14268/api/traces")
      .withAuthToken("authToken")
      .getSender();

    assertThat(sender.getClass(), is(typeCompatibleWith(HttpSender.class)));
  }
  
  @Test
  public void testSenderWithAllPropertiesReturnsHttpSender() {
    System.setProperty(Configuration.JAEGER_ENDPOINT, "https://jaeger-collector:14268/api/traces");
    System.setProperty(Configuration.JAEGER_AGENT_HOST, "jaeger-agent");
    System.setProperty(Configuration.JAEGER_AGENT_PORT, "6832");
    
    Sender sender = Configuration.SenderConfiguration.fromEnv().getSender();

    assertThat(sender.getClass(), is(typeCompatibleWith(HttpSender.class)));
  }
  
  @Test
  public void testDefaultConfigurationReturnsUdpSender() {
    Sender sender = Configuration.SenderConfiguration.fromEnv().getSender();

    assertThat(sender.getClass(), is(typeCompatibleWith(UdpSender.class)));
  }

}
