package io.avaje.nima.test;

import io.avaje.http.client.BodyAdapter;
import io.avaje.http.client.HttpClient;
import io.avaje.http.client.HttpClientRequest;
import io.avaje.http.client.UrlBuilder;

public class TestClient implements HttpClient {

  HttpClient delegate;

  public TestClient(HttpClient delegate) {
    this.delegate = delegate;
  }

  @Override
  public <T> T create(Class<T> clientInterface) {

    return delegate.create(clientInterface);
  }

  @Override
  public HttpClientRequest request() {

    return delegate.request();
  }

  @Override
  public UrlBuilder url() {

    return delegate.url();
  }

  @Override
  public BodyAdapter bodyAdapter() {

    return delegate.bodyAdapter();
  }

  @Override
  public Metrics metrics() {

    return delegate.metrics();
  }

  @Override
  public Metrics metrics(boolean reset) {

    return delegate.metrics(reset);
  }
}
