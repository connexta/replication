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

import java.io.IOException;
import java.io.InputStream;
import javax.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.client.ResponseExceptionMapper;
import org.codice.ditto.replication.api.AdapterException;

/**
 * Copied from DDF and modified for replication purposes.
 *
 * <p>Maps CSW exception responses to AdapterExceptions
 */
public class CswResponseExceptionMapper implements ResponseExceptionMapper<AdapterException> {

  @Override
  public AdapterException fromResponse(Response response) {
    AdapterException cswException;

    if (response != null) {
      if (response.getEntity() instanceof InputStream) {
        try {
          InputStream is = (InputStream) response.getEntity();
          if (is.markSupported()) {
            is.reset();
          }
          cswException = new AdapterException("Csw exception: " + IOUtils.toString(is));
        } catch (IOException e) {
          cswException = new AdapterException("Unable to process Csw exception report", e);
        }
      } else {
        cswException =
            new AdapterException(
                "Error reading response, entity type not understood: "
                    + response.getEntity().getClass().getName());
      }
    } else {
      cswException = new AdapterException("Error handling response, response is null");
    }
    return cswException;
  }
}
