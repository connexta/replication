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
package com.connexta.replication.adapters.ddf.transformer;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.BinaryContent;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.types.Core;
import ddf.catalog.transform.CatalogTransformerException;
import ddf.catalog.transform.MetacardTransformer;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

public class MinimalMetacardTransformer implements MetacardTransformer {

  @Override
  public BinaryContent transform(Metacard metacard, Map<String, Serializable> map)
      throws CatalogTransformerException {
    Attribute metacardModifed = metacard.getAttribute(Core.METACARD_MODIFIED);
    if (metacardModifed == null) {
      throw new CatalogTransformerException(
          "Minimal transformer requires metacard.modified to be set");
    }
    String metadataDate = getXmlDate((Date) metacardModifed.getValue());
    String resourceDate = getXmlDate(metacard.getModifiedDate());

    return new BinaryContentImpl(
        new ByteArrayInputStream(
            ("<minimal id=\""
                    + metacard.getId()
                    + "\" metadataDate=\""
                    + metadataDate
                    + "\" resourceDate=\""
                    + resourceDate
                    + "\"/>")
                .getBytes(StandardCharsets.UTF_8)));
  }

  private String getXmlDate(Date date) throws CatalogTransformerException {
    GregorianCalendar cal = new GregorianCalendar();
    cal.setTime(date);
    try {
      return DatatypeFactory.newInstance().newXMLGregorianCalendar(cal).toXMLFormat();
    } catch (DatatypeConfigurationException e) {
      throw new CatalogTransformerException(
          "Could not parse metacard.modified. XML Date could not be generated.", e);
    }
  }
}
