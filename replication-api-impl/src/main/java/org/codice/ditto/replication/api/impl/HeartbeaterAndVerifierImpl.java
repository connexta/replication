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
import java.util.Optional;
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
import org.codice.ddf.branding.BrandingPlugin;
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

  private final ReplicatorHistory history;

  private final SiteManager siteManager;

  private final ReplicatorConfigManager configManager;

  private final ClientFactoryFactory clientFactoryFactory;

  private final ExecutorService executor;

  private final Security security;

  private final BrandingPlugin brandingPlugin;

  // very small chance of holding duplicates (see {@link #hearbeat) below
  private final BlockingQueue<ReplicationSite> pending = new LinkedBlockingQueue<>();

  public HeartbeaterAndVerifierImpl(
      ReplicatorHistory history,
      SiteManager siteManager,
      ReplicatorConfigManager configManager,
      ClientFactoryFactory clientFactoryFactory,
      BrandingPlugin brandingPlugin,
      ExecutorService executor) {
    this(
        history,
        siteManager,
        configManager,
        executor,
        clientFactoryFactory,
        brandingPlugin,
        Security.getInstance());
  }

  public HeartbeaterAndVerifierImpl(
      ReplicatorHistory history,
      SiteManager siteManager,
      ReplicatorConfigManager configManager,
      ExecutorService executor,
      ClientFactoryFactory clientFactoryFactory,
      BrandingPlugin brandingPlugin,
      Security security) {
    this.history = Validate.notNull(history);
    this.executor = Validate.notNull(executor);
    this.siteManager = Validate.notNull(siteManager);
    this.clientFactoryFactory = Validate.notNull(clientFactoryFactory);
    this.configManager = Validate.notNull(configManager);
    this.brandingPlugin = Validate.notNull(brandingPlugin);
    this.security = security;
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
    if (site.getVerifiedUrl() == null) {
      final String url = site.getUrl();
      HeartbeatResult heartbeatResult = doDiscovery(url);

      if (heartbeatResult.isHeartbeatSupported()) {
        site.setRemoteManaged(true);
        site.setVerifiedUrl(url);
        siteManager.save(site);
      } else if (heartbeatResult.isTmpRedirect()) {
        HeartbeatResult redirectResult = doDiscovery(heartbeatResult.getRedirectUrl());
        if (redirectResult.isHeartbeatSupported()) {
          site.setRemoteManaged(true);
          site.setVerifiedUrl(url);
          siteManager.save(site);
        }
        // do nothing and retry the heartbeat next time
      } else if (heartbeatResult.isPermRedirect()) {
        final String redirect = heartbeatResult.getRedirectUrl();
        HeartbeatResult redirectResult = doDiscovery(redirect);
        if (redirectResult.isHeartbeatSupported()) {
          site.setRemoteManaged(true);
          site.setUrl(redirect);
          site.setVerifiedUrl(url);
          siteManager.save(site);
        }
        // do nothing and retry the heartbeat next time
      } else if (!heartbeatResult.shouldRetryHeartbeat()) {
        LOGGER.trace("Heartbeat protocol not supported for site {}. Making REST head call.", url);
        if (restSupported(url)) {
          site.setVerifiedUrl(url);
          site.setRemoteManaged(false);
          siteManager.save(site);
        }
      }
    } else {
      LOGGER.trace("Doing nothing for already verified site {}", site.getVerifiedUrl());
    }
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
            .product(brandingPlugin.getProductName());

    final HeartbeatResult heartbeatResult = new HeartbeatResult();
    try {
      discovery.heartbeat(DISCOVERY_CLIENT_VERSION, systemInfo, false);
      heartbeatResult.heartbeatSupported();
    } catch (BadRequestException e) {
      heartbeatResult.retryHeartbeat();

      final ErrorMessage msg = e.getResponse().readEntity(ErrorMessage.class);
      LOGGER.warn(
          "Site {} could not be verified due to invalid heartbeat request. Heartbeat will be retried next check interval or next replication run. Verify system info is correct.\n {}",
          url,
          systemInfo);
      LOGGER.warn("Heartbeat response msg for site {}: {}", url, msg.getMessage());
      LOGGER.debug("Heartbeat errors for site {}\n{}", url, msg.getDetails());
    } catch (ServerErrorException e) {
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
      if (e.getCause() instanceof SSLException) {
        heartbeatResult.retryHeartbeat();
        LOGGER.debug(
            "SSL exception while heartbeating site {}. Verify certificates. A heartbeat against this site will be retried.",
            url,
            e);
      } else {
        LOGGER.debug(
            "Unexpected processing exception sending heartbeat request for site {}. This site will not be heartbeated again.",
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
          (replicationStatus, status) -> {
            if (status == Status.PULL_IN_PROGRESS || status == Status.PUSH_IN_PROGRESS) {
              // todo do we need to cancel the running job as well? ie push/pull in progress
              // status will just be overridden for current running jobs
              replicationStatus.setStatus(Status.CONNECTION_LOST);
            } else {
              replicationStatus.setStatus(Status.CONNECTION_UNAVAILABLE);
            }
          });
    } else {
      changeConfigStatusFor(
          site,
          (replicationStatus, status) -> {
            if (status == Status.CONNECTION_LOST || status == Status.CONNECTION_UNAVAILABLE) {
              replicationStatus.setStatus(Status.PENDING);
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
              Optional<ReplicationStatus> statusOptional = statusFor(config);
              if (statusOptional.isPresent()) {
                ReplicationStatus replicationStatus = statusOptional.get();
                updater.accept(replicationStatus, replicationStatus.getStatus());

                try {
                  // todo this is going to undesirably condense the status' stats...
                  history.addReplicationEvent(replicationStatus);
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
            30000 /* connectionTimeout */,
            30000 /* receiveTimeout */);
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

    @Nullable
    String getRedirectUrl() {
      return redirectUrl;
    }
  }
}
