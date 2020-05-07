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

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedHashMap;
import org.apache.commons.io.IOUtils;
import org.codice.ditto.replication.api.data.Metadata;
import org.junit.Before;
import org.junit.Test;

public class GetRecordsMessageBodyReaderTest {

  private GetRecordsMessageBodyReader reader;

  @Before
  public void setup() {
    reader = new GetRecordsMessageBodyReader();
  }

  @Test
  public void isReadable() {
    assertThat(reader.isReadable(CswRecordCollection.class, null, null, null), is(true));
    assertThat(reader.isReadable(Metadata.class, null, null, null), is(false));
  }

  @Test
  public void readFrom() throws Exception {
    InputStream inputStream = new ByteArrayInputStream(getResponse().getBytes());
    CswRecordCollection response =
        reader.readFrom(
            CswRecordCollection.class, null, null, null, new MultivaluedHashMap<>(), inputStream);
    assertThat(response.getCswRecords().size(), is(1));
  }

  @Test(expected = WebApplicationException.class)
  public void readFromBadXml() throws Exception {
    InputStream inputStream = new ByteArrayInputStream("notxml".getBytes());
    reader.readFrom(
        CswRecordCollection.class, null, null, null, new MultivaluedHashMap<>(), inputStream);
  }

  private String getResponse() throws IOException {
    return IOUtils.toString(
        new FileInputStream("src/test/resources/csw-response.xml"), StandardCharsets.UTF_8);
  }
}
