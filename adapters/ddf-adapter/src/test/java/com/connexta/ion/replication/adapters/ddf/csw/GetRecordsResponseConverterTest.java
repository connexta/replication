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
package com.connexta.ion.replication.adapters.ddf.csw;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import com.connexta.ion.replication.api.data.Metadata;
import com.connexta.ion.replication.data.MetadataImpl;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.XppReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.xmlpull.v1.XmlPullParserFactory;

@RunWith(MockitoJUnitRunner.class)
public class GetRecordsResponseConverterTest {

  @Mock Converter converter;

  @Mock HierarchicalStreamWriter writer;

  @Mock MarshallingContext marshallingContext;

  @Mock UnmarshallingContext unmarshallingContext;

  @Test
  public void canConvert() {
    assertThat(
        new GetRecordsResponseConverter(converter).canConvert(CswRecordCollection.class), is(true));
    assertThat(new GetRecordsResponseConverter(converter).canConvert(Metadata.class), is(false));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void marshal() {
    new GetRecordsResponseConverter(converter)
        .marshal(new CswRecordCollection(), writer, marshallingContext);
  }

  @Test
  public void unmarshal() throws Exception {
    InputStream inStream = new FileInputStream("src/test/resources/csw-response.xml");
    HierarchicalStreamReader reader =
        new XppReader(
            new InputStreamReader(inStream, StandardCharsets.UTF_8),
            XmlPullParserFactory.newInstance().newPullParser());
    when(unmarshallingContext.convertAnother(
            any(Object.class), any(Class.class), any(Converter.class)))
        .thenReturn(
            new MetadataImpl(new HashMap<String, String>(), Map.class, "123456789", new Date()));
    CswRecordCollection cswRecords =
        (CswRecordCollection)
            new GetRecordsResponseConverter(converter).unmarshal(reader, unmarshallingContext);
    assertThat(cswRecords.getNumberOfRecordsMatched(), is(1L));
    assertThat(cswRecords.getNumberOfRecordsReturned(), is(1L));
    assertThat(cswRecords.getCswRecords().size(), is(1));
  }
}
