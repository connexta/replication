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
package org.codice.ditto.replication.api.impl;

import com.connexta.discovery.rest.cxf.client.DiscoveryApi;
import com.connexta.discovery.rest.models.ContactInfo;
import com.connexta.discovery.rest.models.ErrorMessage;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import ddf.security.Subject;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import javax.net.ssl.SSLException;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.RedirectionException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import org.apache.commons.lang3.Validate;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;
import org.codice.ddf.configuration.SystemBaseUrl;
import org.codice.ddf.configuration.SystemInfo;
import org.codice.ddf.cxf.client.ClientFactoryFactory;
import org.codice.ddf.cxf.client.SecureCxfClientFactory;
import org.codice.ddf.endpoints.rest.RESTService;
import org.codice.ddf.security.common.Security;
import org.codice.ditto.replication.api.Heartbeater;
import org.codice.ditto.replication.api.ReplicationPersistenceException;
import org.codice.ditto.replication.api.ReplicationStatus;
import org.codice.ditto.replication.api.ReplicatorHistory;
import org.codice.ditto.replication.api.Status;
import org.codice.ditto.replication.api.Verifier;
import org.codice.ditto.replication.api.data.ReplicationSite;
import org.codice.ditto.replication.api.data.ReplicatorConfig;
import org.codice.ditto.replication.api.persistence.ReplicatorConfigManager;
import org.codice.ditto.replication.api.persistence.SiteManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides an implementation for both the {@link Verifier} and {@link Heartbeater} services capable
 * of communicating with either Ion or DDF systems.
 */
public class HeartbeaterAndVerifierImpl implements Verifier, Heartbeater {

  private static final Logger LOGGER = LoggerFactory.getLogger(HeartbeaterAndVerifierImpl.class);

  private static final String DISCOVERY_CLIENT_VERSION = "1.0.1";

  private static final String CONNECTION_TIMEOUT_PROPERTY =
      "org.codice.replication.heartbeat.client.connectiontimeout";

  private static final String RECEIVE_TIMEOUT_PROPERTY =
      "org.codice.replication.heartbeat.client.receivetimeout";

  private static final String MAX_REDIRECTS_PROPERTY =
      "org.codice.replication.heartbeat.maxredirects";

  private static final int DEFAULT_MAX_REDIRECTS = 10;

  private static final int DEFAULT_CONNECTION_TIMEOUT_MS = 30000;

  private static final int DEFAULT_RECEIVE_TIMEOUT_MS = 30000;

  private final ReplicatorHistory history;

  private final SiteManager siteManager;

  private final ReplicatorConfigManager configManager;

  private final ClientFactoryFactory clientFactoryFactory;

  private final ExecutorService executor;

  private final Security security;

  private final Set<String> badRequestSites = new HashSet<>();

  // very small chance of holding duplicates (see {@link #hearbeat) below
  private final BlockingQueue<ReplicationSite> pending = new LinkedBlockingQueue<>();

  private int maxRedirects;

  private int connectionTimeoutMs;

  private int receiveTimeoutMs;

  public HeartbeaterAndVerifierImpl(
      ReplicatorHistory history,
      SiteManager siteManager,
      ReplicatorConfigManager configManager,
      ClientFactoryFactory clientFactoryFactory,
      ExecutorService executor) {
    this(
        history,
        siteManager,
        configManager,
        executor,
        clientFactoryFactory,
        Security.getInstance());
  }

  public HeartbeaterAndVerifierImpl(
      ReplicatorHistory history,
      SiteManager siteManager,
      ReplicatorConfigManager configManager,
      ExecutorService executor,
      ClientFactoryFactory clientFactoryFactory,
      Security security) {
    this.history = Validate.notNull(history);
    this.executor = Validate.notNull(executor);
    this.siteManager = Validate.notNull(siteManager);
    this.clientFactoryFactory = Validate.notNull(clientFactoryFactory);
    this.configManager = Validate.notNull(configManager);
    this.security = security;

    maxRedirects = readIntPropertyOrDefault(MAX_REDIRECTS_PROPERTY, DEFAULT_MAX_REDIRECTS);
    connectionTimeoutMs =
        readIntPropertyOrDefault(CONNECTION_TIMEOUT_PROPERTY, DEFAULT_CONNECTION_TIMEOUT_MS);
    receiveTimeoutMs =
        readIntPropertyOrDefault(RECEIVE_TIMEOUT_PROPERTY, DEFAULT_RECEIVE_TIMEOUT_MS);
  }

  /** Initializes the heartbeat and verification service. */
  public void init() {
    LOGGER.trace("Initializing the heartbeater and verifier");
    executor.execute(this::processingLoop);
  }

  /** Cleanups the heartbeat and verification service. */
  public void cleanUp() {
    LOGGER.trace("Cleaning the heartbeater and verifier");
    LOGGER.trace(
        "Shutting down now the single-thread scheduler that executes sync requests from the queue");
    executor.shutdownNow();
    LOGGER.trace("Successfully shut down heartbeater thread pool and scheduler");
  }

  @Override
  public void verify(ReplicationSite site) {
    if (badRequestSites.contains(site.getId())) {
      LOGGER.debug(
          "Heartbeat to Site {} skipped since last request was a bad request. Restart the system to trigger a retry of this site.",
          site.getUrl());
      return;
    }

    if (site.getVerifiedUrl() == null) {
      int numRedirects = 0;
      String url = site.getUrl();
      do {
        HeartbeatResult heartbeatResult = doDiscovery(url);

        boolean redirected = handleHeartbeatResult(heartbeatResult, site);
        if (redirected) {
          url = heartbeatResult.getRedirectUrl();
          numRedirects++;

          if (heartbeatResult.isPermRedirect()) {
            site.setRemoteManaged(true);
            site.setVerifiedUrl(url);
            siteManager.save(site);
          }
        } else {
          break;
        }
      } while (numRedirects <= maxRedirects);

      if (numRedirects > maxRedirects) {
        LOGGER.warn(
            "Maximum allowed redirects exceeded when heartbeating site {}. The site will be retried next interval.",
            site.getUrl());
      }
    }
  }

  private HeartbeatResult doHeartbeat(ReplicationSite site) {
    int numRedirects = 0;
    String url = site.getUrl();
    HeartbeatResult heartbeatResult = doDiscovery(url);

    do {
      boolean redirected = handleHeartbeatResult(heartbeatResult, site);
      if (redirected) {
        url = heartbeatResult.getRedirectUrl();
        numRedirects++;

        if (heartbeatResult.isPermRedirect()) {
          site.setRemoteManaged(true);
          site.setVerifiedUrl(url);
          siteManager.save(site);
        }
      } else {
        break;
      }
    } while (numRedirects <= DEFAULT_MAX_REDIRECTS);

    if (numRedirects > DEFAULT_MAX_REDIRECTS) {
      LOGGER.warn(
          "Maximum allowed redirects exceeded when heartbeating site {}. The site will be retried next interval.",
          site.getUrl());
    }

    return heartbeatResult;
  }

  /**
   * @param heartbeatResult
   * @param site
   * @return {@code true} if redirected, otherwise {@code false}
   */
  private boolean handleHeartbeatResult(
      final HeartbeatResult heartbeatResult, final ReplicationSite site) {
    final String url = site.getUrl();
    if (heartbeatResult.isHeartbeatSupported()) {
      site.setRemoteManaged(true);
      site.setVerifiedUrl(url);
      siteManager.save(site);
    } else if (heartbeatResult.isTmpRedirect() || heartbeatResult.isPermRedirect()) {
      return true;
    } else if (heartbeatResult.isBadRequest()) {
      badRequestSites.add(site.getId());
    } else if (!heartbeatResult.shouldRetryHeartbeat()) {
      LOGGER.trace("Heartbeat protocol not supported for site {}. Making REST head call.", url);
      if (restSupported(url)) {
        site.setVerifiedUrl(url);
        site.setRemoteManaged(false);
        siteManager.save(site);
      }
    }

    return false;
  }

  @Override
  public void heartbeat(ReplicationSite site) throws InterruptedException {
    // it is entirely possible that we still end up with duplicates since we don't prevent another
    // thread from checking and adding the same site to the queue. However, the probability is low
    // of that happening and if it ever happened, will just end up send 2 successive heartbeats in
    // a row
    if (!pending.contains(site)) {
      pending.put(site);
    }
  }

  private boolean restSupported(String url) {
    // use a generic WebClient because ddf RESTService interface does not have head, but the
    // implementation supports it
    SecureCxfClientFactory<WebClient> restClientFactory =
        clientFactoryFactory.getSecureCxfClientFactory(
            url + "/" + RESTService.CONTEXT_ROOT, WebClient.class);

    WebClient restService = restClientFactory.getClientForSubject(security.getSystemSubject());

    try {
      return restService.head().getStatusInfo().getFamily().equals(Family.SUCCESSFUL);
    } catch (ProcessingException e) {
      LOGGER.debug("Error sending head to rest endpoint for site {}", url, e);
    }

    return false;
  }

  private HeartbeatResult doDiscovery(final String url) {
    DiscoveryApi discovery = createDiscoveryClient(url);

    ContactInfo contactInfo = new ContactInfo().email(SystemInfo.getSiteContatct());
    com.connexta.discovery.rest.models.SystemInfo systemInfo =
        new com.connexta.discovery.rest.models.SystemInfo()
            .contact(contactInfo)
            .id(System.getProperty("org.codice.ddf.system.registry-id"))
            .name(SystemInfo.getSiteName())
            .organization(SystemInfo.getOrganization())
            .version(SystemInfo.getVersion())
            .url(SystemBaseUrl.EXTERNAL.getBaseUrl())
            .product(System.getProperty("org.codice.ddf.system.branding"));

    final HeartbeatResult heartbeatResult = new HeartbeatResult();
    try {
      discovery.heartbeat(DISCOVERY_CLIENT_VERSION, systemInfo, false);
      heartbeatResult.heartbeatSupported();
    } catch (BadRequestException e) {
      final ErrorMessage msg = e.getResponse().readEntity(ErrorMessage.class);
      LOGGER.warn(
          "Site {} could not be verified due to invalid heartbeat request. Heartbeat will be retried on system restart. Verify system info is correct.\n {}",
          url,
          systemInfo);
      LOGGER.warn("Heartbeat response msg for site {}: {}", url, msg.getMessage());
      LOGGER.debug("Heartbeat errors for site {}\n{}", url, msg.getDetails());
      heartbeatResult.badRequest();
    } catch (ServerErrorException e) {
      heartbeatResult.retryHeartbeat();
      final ErrorMessage msg = e.getResponse().readEntity(ErrorMessage.class);

      final int code = msg.getCode();
      // we should never get 501001 (cannot understand version) server error code since client
      // version 1.0.1 is hardcoded
      if (code == 501002 || code == 501003 || code == 501004) {
        LOGGER.debug("Client version unsupported by heartbeat server {}", url);
      }
    } catch (RedirectionException e) {
      final Response response = e.getResponse();
      final int status = response.getStatus();
      if (status == HttpStatus.SC_TEMPORARY_REDIRECT) {
        heartbeatResult.tmpRedirect();
        heartbeatResult.setRedirectUrl(response.getLocation().toASCIIString());
      } else if (status == 308) {
        heartbeatResult.permRedirect();
        heartbeatResult.setRedirectUrl(response.getLocation().toASCIIString());
      }
    } catch (ProcessingException e) {
      Throwable cause = e.getCause();
      if (cause instanceof SSLException) {
        LOGGER.debug(
            "SSL exception while heartbeating site {}. Verify certificates. A heartbeat against this site will be retried.",
            url,
            e);
      } else {
        LOGGER.debug(
            "Unexpected processing exception sending heartbeat request for site {}. This site will not be heartbeated again until a restart.",
            url,
            e);
      }
    }

    return heartbeatResult;
  }

  private void processSite(ReplicationSite site) {
    final Subject systemSubject = security.runAsAdmin(security::getSystemSubject);

    systemSubject.execute(() -> processSiteAsAdmin(site));
  }

  /**
   * If the heartbeat fails, get all configurations that involve that site and mark their status to
   * Status.CONNECTION_LOST if the current status was XXXX_IN_PROGRESS and to
   * Status.CONNECTION_UNAVAILABLE for any other status
   *
   * <p>If the heartbeat succeeds, get all configurations that involves that site and if their
   * current status is Status.CONNECTION_LOST or Status.CONNECTION_UNAVAILABLE, mark their status to
   * Status.PENDING indicating the remote end should restart soon to replicate. For all other
   * status, leave untouched
   */
  private void processSiteAsAdmin(ReplicationSite site) {
    LOGGER.trace("Processing heartbeat for {} with subject", site.getUrl());
    verify(site);
    // next let's make sure it is a remotely managed site
    if (!site.isRemoteManaged()) {
      LOGGER.trace("Site {}'s is not remotely managed, not sending a heartbeat", site.getUrl());
    } else if (site.getVerifiedUrl() == null) {
      changeConfigStatusFor(
          site,
          (replicationStatus, status) ->
              replicationStatus.setStatus(Status.CONNECTION_UNAVAILABLE));
    } else {
      changeConfigStatusFor(
          site,
          (replicationStatus, status) -> {
            HeartbeatResult result = doHeartbeat(site);
            if (result.isHeartbeatSupported()
                && (status == Status.CONNECTION_LOST || status == Status.CONNECTION_UNAVAILABLE)) {
              replicationStatus.setStatus(Status.PENDING);
            } else {
              if (status == Status.PULL_IN_PROGRESS || status == Status.PUSH_IN_PROGRESS) {
                replicationStatus.setStatus(Status.CONNECTION_LOST);
              } else {
                replicationStatus.setStatus(Status.CONNECTION_UNAVAILABLE);
              }
            }
          });
    }
  }

  private void changeConfigStatusFor(
      ReplicationSite site, BiConsumer<ReplicationStatus, Status> updater) {
    configManager
        .objects()
        .filter(config -> config.sourceOrDestinationIs(site.getId()))
        .forEach(
            config -> {
              ReplicationStatus replicationStatus = statusFor(config).orElse(null);
              if (replicationStatus != null) {
                updater.accept(replicationStatus, replicationStatus.getStatus());

                try {
                  history.updateReplicationEvent(replicationStatus);
                } catch (ReplicationPersistenceException e) {
                  LOGGER.debug(
                      "Failed to update status while heartbeating site {}", site.getUrl(), e);
                }
              } else {
                LOGGER.warn("No existing status to update for config {}", config.getName());
              }
            });
  }

  private Optional<ReplicationStatus> statusFor(ReplicatorConfig config) {
    return history.getReplicationEvents(config.getName()).stream().findFirst();
  }

  @SuppressWarnings("squid:S2189" /* Run inside executor and will exit loop on executor shutdown */)
  private void processingLoop() {
    while (true) {
      try {
        processNextSite();
      } catch (InterruptedException e) { // propagate interruption and bail
        LOGGER.trace("Heartbeater was interrupted");
        Thread.currentThread().interrupt();
        return;
      }
    }
  }

  private DiscoveryApi createDiscoveryClient(final String url) {
    final ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    SecureCxfClientFactory<DiscoveryApi> cxfFactory =
        clientFactoryFactory.getSecureCxfClientFactory(
            url,
            DiscoveryApi.class,
            Collections.singletonList(
                new JacksonJaxbJsonProvider(
                    objectMapper, JacksonJaxbJsonProvider.DEFAULT_ANNOTATIONS)),
            null,
            true /* disableCnCheck */,
            false /* allowRedirects */,
            connectionTimeoutMs /* connectionTimeout */,
            receiveTimeoutMs /* receiveTimeout */);
    return cxfFactory.getClientForSubject(security.getSystemSubject());
  }

  private void processNextSite() throws InterruptedException {
    final ReplicationSite site = pending.take();

    LOGGER.trace(
        "Pulled heartbeat request for {}; {} sites left in the queue",
        site.getUrl(),
        pending.size());
    processSite(site);
  }

  private class HeartbeatResult {

    private boolean tmpRedirect = false;

    private boolean permRedirect = false;

    private boolean retryHeartbeat = false;

    private boolean heartbeatSupported = false;

    private boolean badRequest = false;

    private String redirectUrl = null;

    void tmpRedirect() {
      this.tmpRedirect = true;
    }

    void permRedirect() {
      this.permRedirect = true;
    }

    void retryHeartbeat() {
      this.retryHeartbeat = true;
    }

    void heartbeatSupported() {
      this.heartbeatSupported = true;
    }

    void badRequest() {
      this.badRequest = true;
    }

    void setRedirectUrl(final String redirectUrl) {
      this.redirectUrl = redirectUrl;
    }

    boolean isTmpRedirect() {
      return tmpRedirect;
    }

    boolean isPermRedirect() {
      return permRedirect;
    }

    boolean shouldRetryHeartbeat() {
      return retryHeartbeat;
    }

    boolean isHeartbeatSupported() {
      return heartbeatSupported;
    }

    boolean isBadRequest() {
      return badRequest;
    }

    @Nullable
    String getRedirectUrl() {
      return redirectUrl;
    }
  }

  private int readIntPropertyOrDefault(String property, int defaultValue) {
    String value = System.getProperty(property);
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      LOGGER.warn(
          "Failed to parse system property {}. Please fix the value. Defaulting to {}.",
          property,
          defaultValue);
    }
    return defaultValue;
  }
}
