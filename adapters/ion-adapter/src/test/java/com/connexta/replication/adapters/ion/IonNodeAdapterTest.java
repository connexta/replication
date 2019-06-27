package com.connexta.replication.adapters.ion;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;

import com.google.common.collect.ImmutableList;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import org.codice.ditto.replication.api.data.CreateStorageRequest;
import org.codice.ditto.replication.api.data.Metadata;
import org.codice.ditto.replication.api.data.Resource;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.response.DefaultResponseCreator;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

public class IonNodeAdapterTest {

  RestTemplate restTemplate;

  MockRestServiceServer mockServer;

  IonNodeAdapter adapter;

  @Before
  public void setup() throws Exception {
    restTemplate = new RestTemplate();
    mockServer = MockRestServiceServer.createServer(restTemplate);
    adapter = new IonNodeAdapter(new URL("http://localhost:1234"), restTemplate);
  }

  @Test
  public void isAvailable() {
    DefaultResponseCreator response = MockRestResponseCreators.withSuccess();
    HttpHeaders headers = new HttpHeaders();
    headers.addAll("Allow", ImmutableList.of("POST", "OPTIONS"));
    response.headers(headers);
    mockServer
        .expect(requestTo("http://localhost:1234/ingest"))
        .andExpect(method(HttpMethod.OPTIONS))
        .andRespond(response);
    assertThat(adapter.isAvailable(), is(true));
    mockServer.verify();
  }

  @Test
  public void isNotAvailable() throws Exception {
    restTemplate = new RestTemplate();
    adapter = new IonNodeAdapter(new URL("http://localhost:1234321"), restTemplate);
    assertThat(adapter.isAvailable(), is(false));
  }

  @Test
  public void getSystemName() {
    assertThat(adapter.getSystemName(), is("ION"));
  }

  @Test
  public void createResource() {
    mockServer
        .expect(requestTo("http://localhost:1234/ingest"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(MockRestResponseCreators.withStatus(HttpStatus.ACCEPTED));

    assertThat(adapter.createResource(getCreateStorageRequest()), is(true));
    mockServer.verify();
  }

  @Test
  public void createResource200() {
    mockServer
        .expect(requestTo("http://localhost:1234/ingest"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK));

    assertThat(adapter.createResource(getCreateStorageRequest()), is(true));
    mockServer.verify();
  }

  @Test
  public void createResourceFailed300() {
    mockServer
        .expect(requestTo("http://localhost:1234/ingest"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(MockRestResponseCreators.withStatus(HttpStatus.MULTIPLE_CHOICES));

    assertThat(adapter.createResource(getCreateStorageRequest()), is(false));
    mockServer.verify();
  }

  @Test
  public void createResourceFailed401() {
    mockServer
        .expect(requestTo("http://localhost:1234/ingest"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(MockRestResponseCreators.withStatus(HttpStatus.UNAUTHORIZED));

    assertThat(adapter.createResource(getCreateStorageRequest()), is(false));
    mockServer.verify();
  }

  @Test
  public void unimplementedMethods() {
    // many of the unimplemented methods return true since we don't want to wast time retrying them
    // later
    // when we know they will never succeed. We will just let replication think it was successful.
    assertThat(adapter.createRequest(null), is(true));
    assertThat(adapter.updateRequest(null), is(true));
    assertThat(adapter.deleteRequest(null), is(true));
    assertThat(adapter.readResource(null), is(nullValue()));
    assertThat(adapter.updateResource(null), is(true));
    assertThat(adapter.query(null).getMetadata().iterator().hasNext(), is(false));
  }

  private CreateStorageRequest getCreateStorageRequest() {
    CreateStorageRequest request =
        () ->
            Collections.singletonList(
                new Resource() {
                  @Override
                  public String getId() {
                    return "1234";
                  }

                  @Override
                  public String getName() {
                    return "test";
                  }

                  @Override
                  public URI getResourceUri() {
                    return URI.create("https://host:1234/path/to/resource");
                  }

                  @Nullable
                  @Override
                  public String getQualifier() {
                    return null;
                  }

                  @Override
                  public InputStream getInputStream() {
                    return new ByteArrayInputStream("This is a test".getBytes());
                  }

                  @Override
                  public String getMimeType() {
                    return MediaType.TEXT_PLAIN.toString();
                  }

                  @Override
                  public long getSize() {
                    return 14;
                  }

                  @Override
                  public Metadata getMetadata() {
                    return null;
                  }
                });
    return request;
  }
}
