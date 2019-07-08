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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.converters.DataHolder;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.thoughtworks.xstream.io.xml.XppReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.MessageBodyReader;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * Copied from DDF and modified for replication purposes.
 *
 * <p>Custom JAX-RS MessageBodyReader for parsing a CSW GetRecords response, extracting the search
 * results and CSW records.
 */
public class GetRecordsMessageBodyReader implements MessageBodyReader<CswRecordCollection> {
  private static final Logger LOGGER = LoggerFactory.getLogger(GetRecordsMessageBodyReader.class);

  public static final String BYTES_SKIPPED = "bytes-skipped";

  private XStream xstream;

  private DataHolder argumentHolder;

  public GetRecordsMessageBodyReader() {
    xstream = new XStream(new XppDriver());
    xstream.setClassLoader(this.getClass().getClassLoader());
    xstream.registerConverter(new GetRecordsResponseConverter(new CswRecordConverter()));
    xstream.alias(Constants.GET_RECORDS_RESPONSE, CswRecordCollection.class);
    xstream.alias(
        Constants.CSW_NAMESPACE_PREFIX
            + Constants.NAMESPACE_DELIMITER
            + Constants.GET_RECORDS_RESPONSE,
        CswRecordCollection.class);
    buildArguments();
  }

  private void buildArguments() {
    argumentHolder = xstream.newDataHolder();
    argumentHolder.put(Constants.OUTPUT_SCHEMA_PARAMETER, Constants.METACARD_SCHEMA);
  }

  @Override
  public boolean isReadable(
      Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return CswRecordCollection.class.isAssignableFrom(type);
  }

  @Override
  public CswRecordCollection readFrom(
      Class<CswRecordCollection> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders,
      InputStream inStream)
      throws IOException {

    CswRecordCollection cswRecords = null;

    // Save original input stream for any exception message that might need to be
    // created
    String originalInputStream = IOUtils.toString(inStream, StandardCharsets.UTF_8);
    LOGGER.trace("Converting to CswRecordCollection: \n {}", originalInputStream);

    // Re-create the input stream (since it has already been read for potential
    // exception message creation)
    inStream = new ByteArrayInputStream(originalInputStream.getBytes(StandardCharsets.UTF_8));

    try (InputStream stream = inStream) {
      HierarchicalStreamReader reader =
          new XppReader(
              new InputStreamReader(stream, StandardCharsets.UTF_8),
              XmlPullParserFactory.newInstance().newPullParser());
      cswRecords = (CswRecordCollection) xstream.unmarshal(reader, null, argumentHolder);
    } catch (XmlPullParserException e) {
      LOGGER.debug("Unable to create XmlPullParser, and cannot parse CSW Response.", e);
    } catch (XStreamException e) {
      // If an ExceptionReport is sent from the remote CSW site it will be sent with an
      // JAX-RS "OK" status, hence the ErrorResponse exception mapper will not fire.
      // Instead the ExceptionReport will come here and be treated like a GetRecords
      // response, resulting in an XStreamException since ExceptionReport cannot be
      // unmarshalled. So this catch clause is responsible for catching that XStream
      // exception and creating a JAX-RS response containing the original stream
      // (with the ExceptionReport) and rethrowing it as a WebApplicatioNException,
      // which CXF will wrap as a ClientException that the CswSource catches, converts
      // to a CswException, and logs.
      ByteArrayInputStream bis =
          new ByteArrayInputStream(originalInputStream.getBytes(StandardCharsets.UTF_8));
      ResponseBuilder responseBuilder = Response.ok(bis);
      responseBuilder.type("text/xml");
      Response response = responseBuilder.build();
      throw new WebApplicationException(e, response);
    }

    return cswRecords;
  }
}
