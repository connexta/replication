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
package com.connexta.replication.adapters.ddf.rest;

import java.io.InputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;

/** REST endpoint interface */
@Path("/")
public interface RESTService {

  /**
   * REST Get. Retrieves the metadata entry specified by the id. Transformer argument is optional,
   * but is used to specify what format the data should be returned.
   *
   * @param id the unique metadata id
   * @param transformerParam (OPTIONAL) transform used to turn raw resource into desired response
   *     format
   * @param uriInfo the request UriInfo
   * @param httpRequest the http request
   * @return REST response containing resource in the requested format
   */
  @GET
  @Path("/{id}")
  Response getDocument(
      @PathParam("id") String id,
      @QueryParam("transform") String transformerParam,
      @Context UriInfo uriInfo,
      @Context HttpServletRequest httpRequest);

  /**
   * REST Get. Retrieves information regarding sources available.
   *
   * @param uriInfo the request UriInfo
   * @param httpRequest the http request
   * @return REST response
   */
  @GET
  @Path("/sources")
  Response getDocument(@Context UriInfo uriInfo, @Context HttpServletRequest httpRequest);

  /**
   * REST Get. Retrieves the metadata entry specified by the id from the federated source specified
   * by sourceid. Transformer argument is optional, but is used to specify what format the data
   * should be returned.
   *
   * @param sourceid the source to retrieve the metadata from
   * @param id the unique metadata id
   * @param transformerParam transform used to turn raw resource into desired response format
   * @param uriInfo the request UriInfo
   * @param httpRequest the http request
   * @return REST response containing resource in the requested format
   */
  @GET
  @Path("/sources/{sourceid}/{id}")
  Response getDocument(
      @PathParam("sourceid") String sourceid,
      @PathParam("id") String id,
      @QueryParam("transform") String transformerParam,
      @Context UriInfo uriInfo,
      @Context HttpServletRequest httpRequest);

  /**
   * REST Put. Updates the specified metadata entry with the provided metadata.
   *
   * @param id the unique metadata id
   * @param headers the http headers
   * @param httpRequest the http request
   * @param transformerParam transform used to turn raw resource into desired response format
   * @param message the updated metadata
   * @return REST response indicating the success/failure of the operation
   */
  @PUT
  @Path("/{id}")
  Response updateDocument(
      @PathParam("id") String id,
      @Context HttpHeaders headers,
      @Context HttpServletRequest httpRequest,
      @QueryParam("transform") String transformerParam,
      InputStream message);

  /**
   * REST Put. Updates the specified metadata entry with the provided metadata.
   *
   * @param id the unique metadata id
   * @param headers the http headers
   * @param httpRequest the http request
   * @param multipartBody the multipart body containing the resource
   * @param transformerParam transform used to turn raw resource into desired response format
   * @param message the updated metadata
   * @return REST response indicating the success/failure of the operation
   */
  @PUT
  @Path("/{id}")
  Response updateDocument(
      @PathParam("id") String id,
      @Context HttpHeaders headers,
      @Context HttpServletRequest httpRequest,
      MultipartBody multipartBody,
      @QueryParam("transform") String transformerParam,
      InputStream message);

  /**
   * REST Post. Creates a new metadata entry in the catalog.
   *
   * @param headers the http headers
   * @param requestUriInfo the request UriInfo
   * @param httpRequest the http request
   * @param transformerParam transform used to turn raw resource into desired response format
   * @param message the metadata to be added
   * @return REST response indicating the success/failure of the operation
   */
  @POST
  Response addDocument(
      @Context HttpHeaders headers,
      @Context UriInfo requestUriInfo,
      @Context HttpServletRequest httpRequest,
      @QueryParam("transform") String transformerParam,
      InputStream message);

  /**
   * REST Post. Creates a new metadata entry in the catalog.
   *
   * @param headers the http headers
   * @param requestUriInfo the request UriInfo
   * @param httpRequest the http request
   * @param multipartBody the multipart body containing the resource
   * @param transformerParam transform used to turn raw resource into desired response format
   * @param message the metadata to be added
   * @return REST response indicating the success/failure of the operation
   */
  @POST
  Response addDocument(
      @Context HttpHeaders headers,
      @Context UriInfo requestUriInfo,
      @Context HttpServletRequest httpRequest,
      MultipartBody multipartBody,
      @QueryParam("transform") String transformerParam,
      InputStream message);

  /**
   * REST Delete. Deletes a record from the catalog.
   *
   * @param id the unique metadata id
   * @param httpRequest the http request
   * @return REST response indicating the success/failure of the operation
   */
  @DELETE
  @Path("/{id}")
  Response deleteDocument(@PathParam("id") String id, @Context HttpServletRequest httpRequest);
}
