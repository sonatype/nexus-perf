/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package com.sonatype.nexus.perftest.nuget;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

/**
 * Performs basic HTTP operations.
 */
public class NugetHttp
{
  public static final int HTTP_TIMEOUT = Integer.parseInt(System.getProperty("perftest.http.timeout", "60000"));

  private DefaultHttpClient httpClient;

  NugetHttp() {
    httpClient = getHttpClient();
  }

  public HttpResponse get(String url) throws Exception {
    final HttpGet get = new HttpGet(url);
    return httpClient.execute(get);
  }

  /**
   * Download content from a URL and discard it.
   */
  void getAndEat(final String url) throws Exception {
    final HttpResponse httpResponse = get(url);
    EntityUtils.consumeQuietly(httpResponse.getEntity());
  }

  private DefaultHttpClient getHttpClient() {
    HttpParams params = new BasicHttpParams();
    HttpConnectionParams.setConnectionTimeout(params, HTTP_TIMEOUT);
    HttpConnectionParams.setSoTimeout(params, HTTP_TIMEOUT);
    return new DefaultHttpClient(params);
  }


}
