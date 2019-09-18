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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import com.connexta.replication.adapters.ddf.DdfMetadata;
import com.connexta.replication.adapters.ddf.MetacardAttribute;
import com.connexta.replication.api.Replication;
import com.connexta.replication.api.data.Metadata;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.xml.XppReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.xmlpull.v1.XmlPullParserFactory;

@RunWith(MockitoJUnitRunner.class)
public class CswRecordConverterTest {

  private Map<String, MetacardAttribute> map;

  @Before
  public void setUp() throws Exception {
    map = new HashMap<>();
    map.put(Constants.METACARD_ID, new MetacardAttribute(Constants.METACARD_ID, null, "123456789"));
    map.put("type", new MetacardAttribute("type", null, "my-type"));
    map.put("source", new MetacardAttribute("source", null, "source-id"));
    map.put(
        Constants.METACARD_TAGS, new MetacardAttribute(Constants.METACARD_TAGS, "string", "tag"));
    map.put(
        Constants.METACARD_MODIFIED,
        new MetacardAttribute(Constants.METACARD_MODIFIED, "date", "1234-01-02T03:04:05.060"));
    map.put(
        "location",
        new MetacardAttribute(
            "location",
            "geometry",
            Collections.singletonList("<ns2:point>123,456</ns2:point>"),
            Collections.singletonList("xmlns:ns2=http://some/namespace")));
    map.put(Replication.ORIGINS, new MetacardAttribute(Replication.ORIGINS, "string", "otherhost"));
    map.put(
        Constants.RESOURCE_URI, new MetacardAttribute(Constants.RESOURCE_URI, "string", "my:uri"));
    map.put(
        Constants.RESOURCE_SIZE, new MetacardAttribute(Constants.RESOURCE_SIZE, "long", "1234"));
    map.put(
        Constants.MODIFIED,
        new MetacardAttribute(Constants.MODIFIED, "date", "1234-01-01T03:04:05.060"));
    map.put(
        Constants.DERIVED_RESOURCE_URI,
        new MetacardAttribute(Constants.DERIVED_RESOURCE_URI, "string", "my:derived:uri"));
    map.put(
        Constants.DERIVED_RESOURCE_DOWNLOAD_URL,
        new MetacardAttribute(
            Constants.DERIVED_RESOURCE_DOWNLOAD_URL, "string", "http://host/path"));
  }

  @Test
  public void canConvert() {
    assertThat(new CswRecordConverter().canConvert(Metadata.class), is(true));
    assertThat(new CswRecordConverter().canConvert(String.class), is(false));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void marshal() {
    new CswRecordConverter().marshal(null, null, null);
  }

  @Test
  public void unmarshal() throws Exception {
    String metacardXml =
        MetacardMarshaller.marshal(
            new DdfMetadata("mcard metadata", String.class, "123456789", new Date(1), map));

    InputStream inStream = new ByteArrayInputStream(metacardXml.getBytes());
    HierarchicalStreamReader reader =
        new XppReader(
            new InputStreamReader(inStream, StandardCharsets.UTF_8),
            XmlPullParserFactory.newInstance().newPullParser());
    UnmarshallingContext context = mock(UnmarshallingContext.class);
    Object obj = new CswRecordConverter().unmarshal(reader, context);
    assertThat(obj, instanceOf(Metadata.class));
    DdfMetadata metadata = (DdfMetadata) obj;
    assertThat(metadata.getId(), is("123456789"));
    assertThat(metadata.getTags().iterator().next(), is("tag"));
    assertThat(metadata.getLineage().get(0), is("otherhost"));
    assertThat(
        metadata.getMetadataModified(),
        is(
            ISODateTimeFormat.dateOptionalTimeParser()
                .parseDateTime("1234-01-02T03:04:05.060")
                .toDate()));
    assertThat(metadata.isDeleted(), is(false));
    assertThat(metadata.getResourceUri().toString(), is("my:uri"));
    assertThat(metadata.getResourceSize(), is(1234L));
    assertThat(
        metadata.getResourceModified(),
        is(
            ISODateTimeFormat.dateOptionalTimeParser()
                .parseDateTime("1234-01-01T03:04:05.060")
                .toDate()));
    Map<String, MetacardAttribute> map = metadata.getAttributes();
    assertThat(map.containsKey(Constants.DERIVED_RESOURCE_URI), is(false));
    assertThat(map.containsKey(Constants.DERIVED_RESOURCE_DOWNLOAD_URL), is(false));
    assertThat(map.get("location").getValue(), is("<ns2:point>123,456</ns2:point>"));
    assertThat(map.get("location").getXmlns().get(0), is("xmlns:ns2=http://some/namespace"));
  }

  @Test
  public void unmarshalDeletedRecord() throws Exception {
    map.put(
        Constants.VERSIONED_ON,
        new MetacardAttribute(Constants.VERSIONED_ON, "date", "1234-01-01T01:04:05.060"));
    map.put(
        Constants.VERSION_OF_ID,
        new MetacardAttribute(Constants.VERSION_OF_ID, "string", "987654321"));
    map.put(Constants.ACTION, new MetacardAttribute(Constants.ACTION, "string", "Deleted"));

    String metacardXml =
        MetacardMarshaller.marshal(
            new DdfMetadata("mcard Metadata", String.class, "123456789", new Date(1), map));

    InputStream inStream = new ByteArrayInputStream(metacardXml.getBytes());
    HierarchicalStreamReader reader =
        new XppReader(
            new InputStreamReader(inStream, StandardCharsets.UTF_8),
            XmlPullParserFactory.newInstance().newPullParser());
    UnmarshallingContext context = mock(UnmarshallingContext.class);
    Object obj = new CswRecordConverter().unmarshal(reader, context);
    assertThat(obj, instanceOf(Metadata.class));
    Metadata metadata = (Metadata) obj;
    assertThat(metadata.getId(), is("987654321"));
    assertThat(metadata.getTags().iterator().next(), is("tag"));
    assertThat(metadata.getLineage().get(0), is("otherhost"));
    assertThat(
        metadata.getMetadataModified(),
        is(
            ISODateTimeFormat.dateOptionalTimeParser()
                .parseDateTime("1234-01-01T01:04:05.060")
                .toDate()));
    assertThat(metadata.isDeleted(), is(true));
    assertThat(metadata.getResourceUri().toString(), is("my:uri"));
    assertThat(metadata.getResourceSize(), is(1234L));
    assertThat(
        metadata.getResourceModified(),
        is(
            ISODateTimeFormat.dateOptionalTimeParser()
                .parseDateTime("1234-01-01T03:04:05.060")
                .toDate()));
  }
}
