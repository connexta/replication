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
package com.connexta.replication.adapters.ddf.csw;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import com.connexta.replication.adapters.ddf.MetacardAttribute;
import com.connexta.replication.data.MetadataImpl;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.codice.ditto.replication.api.AdapterException;
import org.junit.Test;

public class MetacardMarshallerTest {

  private static final String expectedMetacardXml =
      "<metacard xmlns=\"urn:catalog:metacard\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:smil=\"http://www.w3.org/2001/SMIL20/\" xmlns:smillang=\"http://www.w3.org/2001/SMIL20/Language\" gml:id=\"123456789\">\n  <type>%s</type>\n  <source>source-id</source>\n  <stringxml name=\"metadata\">\n    <value><test><data>testing</data></test></value>\n  </stringxml>\n  <string name=\"metacard-tags\">\n    <value>tag</value>\n  </string>\n  <geometry xmlns:ns2=\"http://some/namespace\" name=\"location\">\n    <value><ns2:point>123,456</ns2:point></value>\n  </geometry>\n</metacard>";

  private static final String minimalMetacardXml =
      "<metacard xmlns=\"urn:catalog:metacard\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:smil=\"http://www.w3.org/2001/SMIL20/\" xmlns:smillang=\"http://www.w3.org/2001/SMIL20/Language\" gml:id=\"123456789\">\n  <type>ddf.metacard</type>\n</metacard>";

  @Test
  public void marshal() {
    Map<String, MetacardAttribute> map = new HashMap<>();

    map.put(Constants.METACARD_ID, new MetacardAttribute(Constants.METACARD_ID, null, "123456789"));
    map.put("type", new MetacardAttribute("type", null, "my-type"));
    map.put("source", new MetacardAttribute("source", null, "source-id"));
    map.put(
        Constants.METACARD_TAGS, new MetacardAttribute(Constants.METACARD_TAGS, "string", "tag"));
    map.put(
        "location",
        new MetacardAttribute(
            "location",
            "geometry",
            Collections.singletonList("<ns2:point>123,456</ns2:point>"),
            Collections.singletonList("xmlns:ns2=http://some/namespace")));
    map.put(
        "metadata",
        new MetacardAttribute("metadata", "stringxml", "<test><data>testing</data></test>"));
    map.put("not-marshaled", new MetacardAttribute("not-marshaled", null, "not-marshaled-value"));

    String metacardxml =
        MetacardMarshaller.marshal(new MetadataImpl(map, Map.class, "123456789", new Date(1)));
    assertThat(metacardxml, is(String.format(expectedMetacardXml, "my-type")));
  }

  @Test
  public void marshalNoType() {
    Map<String, MetacardAttribute> map = new HashMap<>();

    map.put(Constants.METACARD_ID, new MetacardAttribute(Constants.METACARD_ID, null, "123456789"));
    map.put("source", new MetacardAttribute("source", null, "source-id"));
    map.put(
        Constants.METACARD_TAGS, new MetacardAttribute(Constants.METACARD_TAGS, "string", "tag"));
    map.put(
        "location",
        new MetacardAttribute(
            "location",
            "geometry",
            Collections.singletonList("<ns2:point>123,456</ns2:point>"),
            Collections.singletonList("xmlns:ns2=http://some/namespace")));
    map.put(
        "metadata",
        new MetacardAttribute("metadata", "stringxml", "<test><data>testing</data></test>"));
    map.put("not-marshaled", new MetacardAttribute("not-marshaled", null, "not-marshaled-value"));

    String metacardxml =
        MetacardMarshaller.marshal(new MetadataImpl(map, Map.class, "123456789", new Date(1)));
    assertThat(metacardxml, is(String.format(expectedMetacardXml, "ddf.metacard")));
  }

  @Test(expected = AdapterException.class)
  public void marshalInvalidMetadataType() {
    MetacardMarshaller.marshal(
        new MetadataImpl("bad metadata", String.class, "123456789", new Date(1)));
  }

  @Test
  public void marshalEmptyMap() {
    String metacardxml =
        MetacardMarshaller.marshal(
            new MetadataImpl(
                new HashMap<String, MetacardAttribute>(), Map.class, "123456789", new Date(1)));
    assertThat(metacardxml, is(minimalMetacardXml));
  }
}
