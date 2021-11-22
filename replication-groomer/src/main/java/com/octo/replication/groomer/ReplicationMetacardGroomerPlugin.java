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
package com.octo.replication.groomer;

import static com.google.common.base.Strings.isNullOrEmpty;

import ddf.catalog.Constants;
import ddf.catalog.content.data.ContentItem;
import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.types.Core;
import ddf.catalog.operation.CreateRequest;
import ddf.catalog.operation.Request;
import ddf.catalog.operation.UpdateRequest;
import ddf.catalog.plugin.StopProcessingException;
import ddf.catalog.plugin.groomer.AbstractMetacardGroomerPlugin;
import ddf.security.SecurityConstants;
import ddf.security.SubjectOperations;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.shiro.subject.Subject;
import org.codice.ddf.configuration.SystemInfo;
import org.codice.ddf.platform.util.uuidgenerator.UuidGenerator;
import org.codice.ditto.replication.api.Replication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copy of the StandardMetacardGroomer and updated for replication purposes. This groomer replaces
 * the StandardMetacardGroomer and should be the only groomer running in the system.
 *
 * <p>Applies general Create and Update grooming rules such as populating the {@link Metacard#ID},
 * {@link Metacard#MODIFIED}, and {@link Metacard#CREATED} fields.
 */
public class ReplicationMetacardGroomerPlugin extends AbstractMetacardGroomerPlugin {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(ReplicationMetacardGroomerPlugin.class);

  private UuidGenerator uuidGenerator;

  private SubjectOperations subjectOperations;

  public void setUuidGenerator(UuidGenerator uuidGenerator) {
    this.uuidGenerator = uuidGenerator;
  }

  public void setSubjectOperations(SubjectOperations subjectOperations) {
    this.subjectOperations = subjectOperations;
  }

  @Override
  protected void applyCreatedOperationRules(
      CreateRequest createRequest, Metacard aMetacard, Date now) throws StopProcessingException {
    LOGGER.debug("Applying standard rules on CreateRequest");
    if ((aMetacard.getResourceURI() != null && !isCatalogResourceUri(aMetacard.getResourceURI()))
        || !uuidGenerator.validateUuid(aMetacard.getId())) {
      aMetacard.setAttribute(new AttributeImpl(Metacard.ID, uuidGenerator.generateUuid()));
    }

    if (aMetacard.getCreatedDate() == null) {
      aMetacard.setAttribute(new AttributeImpl(Metacard.CREATED, now));
    }

    if (aMetacard.getModifiedDate() == null) {
      aMetacard.setAttribute(new AttributeImpl(Metacard.MODIFIED, now));
    }

    if (aMetacard.getEffectiveDate() == null) {
      aMetacard.setAttribute(new AttributeImpl(Metacard.EFFECTIVE, now));
    }

    if (isDateAttributeEmpty(aMetacard, Core.METACARD_CREATED)) {
      aMetacard.setAttribute(new AttributeImpl(Core.METACARD_CREATED, now));
      logMetacardAttributeUpdate(aMetacard, Core.METACARD_CREATED, now);
    }

    handleReplicationUpdates(aMetacard, createRequest, now);
  }

  private String getUser(Request request) {
    Subject subject = (Subject) request.getProperties().get(SecurityConstants.SECURITY_SUBJECT);
    String user = subjectOperations.getEmailAddress(subject);
    if (isNullOrEmpty(user)) {
      user = subjectOperations.getName(subject);
    }
    return user;
  }

  private boolean isCatalogResourceUri(URI uri) {
    return uri != null && ContentItem.CONTENT_SCHEME.equals(uri.getScheme());
  }

  @Override
  protected void applyUpdateOperationRules(
      UpdateRequest updateRequest,
      Entry<Serializable, Metacard> anUpdate,
      Metacard aMetacard,
      Date now)
      throws StopProcessingException {

    if (UpdateRequest.UPDATE_BY_ID.equals(updateRequest.getAttributeName())
        && !anUpdate.getKey().toString().equals(aMetacard.getId())) {

      LOGGER.debug(
          "{} in metacard must match the Update {}, overwriting metacard {} [{}] with the update identifier [{}]",
          Metacard.ID,
          Metacard.ID,
          Metacard.ID,
          aMetacard.getId(),
          anUpdate.getKey());
      aMetacard.setAttribute(new AttributeImpl(Metacard.ID, anUpdate.getKey()));
    }

    if (aMetacard.getCreatedDate() == null) {
      LOGGER.debug(
          "{} date should match the original metacard. Changing date to current timestamp so it is at least not null.",
          Metacard.CREATED);
      aMetacard.setAttribute(new AttributeImpl(Metacard.CREATED, now));
    }

    if (isDateAttributeEmpty(aMetacard, Core.METACARD_CREATED)) {
      aMetacard.setAttribute(new AttributeImpl(Core.METACARD_CREATED, now));
      LOGGER.debug(
          "{} date should not be null on an update operation. Changing date to current timestamp so it is at least not null.",
          Core.METACARD_CREATED);
    }

    if (aMetacard.getModifiedDate() == null) {
      aMetacard.setAttribute(new AttributeImpl(Metacard.MODIFIED, now));
    }

    if (aMetacard.getEffectiveDate() == null) {
      aMetacard.setAttribute(new AttributeImpl(Metacard.EFFECTIVE, now));
    }

    handleReplicationUpdates(aMetacard, updateRequest, now);
  }

  private void handleReplicationUpdates(Metacard aMetacard, Request request, Date date)
      throws StopProcessingException {
    if (!aMetacard.getTags().contains(Replication.REPLICATION_RUN_TAG)) {
      aMetacard.setAttribute(new AttributeImpl(Core.METACARD_MODIFIED, date));
      aMetacard.setAttribute(new AttributeImpl(Replication.CHANGE_USER, getUser(request)));
      aMetacard.setAttribute(
          new AttributeImpl(
              Replication.CHANGE_LOCATION,
              aMetacard.getSourceId() == null
                  ? SystemInfo.getSiteName()
                  : aMetacard.getSourceId()));
      aMetacard.setAttribute(new AttributeImpl(Replication.CHANGE_DATE, date));
      logMetacardAttributeUpdate(aMetacard, Core.METACARD_MODIFIED, date);
    } else {
      Map<String, Metacard> updates =
          ((Map<String, Metacard>) request.getPropertyValue(Constants.ATTRIBUTE_UPDATE_MAP_KEY));
      if (updates != null
          && ((Date) updates.get(aMetacard.getId()).getAttribute(Core.METACARD_MODIFIED).getValue())
              .after((Date) aMetacard.getAttribute(Core.METACARD_MODIFIED).getValue())) {
        throw new StopProcessingException("Updated metacard is older that the existing metacard.");
      }

      ArrayList<String> tags = new ArrayList<>(aMetacard.getTags());
      tags.remove(Replication.REPLICATION_RUN_TAG);
      aMetacard.setAttribute(new AttributeImpl(Metacard.TAGS, (Serializable) tags));
    }
  }

  private void logMetacardAttributeUpdate(Metacard metacard, String attribute, Object value) {
    LOGGER.debug(
        "Applying {} attribute with value {} to metacard [{}].",
        attribute,
        value,
        metacard.getId());
  }

  private boolean isDateAttributeEmpty(Metacard metacard, String attribute) {
    Attribute origAttribute = metacard.getAttribute(attribute);
    return (origAttribute == null || !(origAttribute.getValue() instanceof Date));
  }
}
