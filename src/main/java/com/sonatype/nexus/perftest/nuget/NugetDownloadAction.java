/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package com.sonatype.nexus.perftest.nuget;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.HttpClientUtils;

/**
 * Makes a GET request to a Nuget repository
 */
public class NugetDownloadAction
{
  private final HttpClient httpClient;

  private final String baseUrl;

  public NugetDownloadAction(HttpClient httpClient, String baseUrl) {
    this.httpClient = httpClient;
    this.baseUrl = baseUrl;
  }

  public void download(String path) throws IOException {
    final String url = baseUrl.endsWith("/") ? baseUrl + path : baseUrl + "/" + path;

    final HttpGet httpGet = new HttpGet(url);
    final HttpResponse response = httpClient.execute(httpGet);

    try {
      if (!isSuccess(response)) {
        System.err.println(url);
        System.err.println(response.getStatusLine());
        if (response.getStatusLine().getStatusCode() != 404) {
          throw new IOException(response.getStatusLine().toString());
        }
      }
    }
    finally {
      HttpClientUtils.closeQuietly(response);
    }
  }

  protected boolean isSuccess(HttpResponse response) {
    return response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() <= 299;
  }
}
