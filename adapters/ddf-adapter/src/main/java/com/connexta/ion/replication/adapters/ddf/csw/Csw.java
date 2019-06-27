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

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import net.opengis.cat.csw.v_2_0_2.CapabilitiesType;
import net.opengis.cat.csw.v_2_0_2.GetCapabilitiesType;
import net.opengis.cat.csw.v_2_0_2.GetRecordsType;

/** JAX-RS Interface to define an OGC Catalogue Service for Web (CSW). */
@Path("/")
public interface Csw {

  /**
   * GetCapabilities - HTTP POST
   *
   * @param request
   * @return
   */
  @SuppressWarnings("squid:S00112" /* Pulled from DDF to match that interface */)
  @POST
  @Consumes({MediaType.TEXT_XML, MediaType.APPLICATION_XML})
  @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML})
  CapabilitiesType getCapabilities(GetCapabilitiesType request) throws Exception;

  /**
   * GetRecords - HTTP POST
   *
   * @param request
   * @return
   */
  @SuppressWarnings("squid:S00112" /* Pulled from DDF to match that interface */)
  @POST
  @Consumes({MediaType.TEXT_XML, MediaType.APPLICATION_XML})
  @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML})
  CswRecordCollection getRecords(GetRecordsType request) throws Exception;
}
