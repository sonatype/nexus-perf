/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package com.sonatype.nexus.perftest.maven;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import com.sonatype.nexus.perftest.Digests;

/**
 * Downloads specified artifact, verifies checksum, throws IOException if downloads fails or checksum is invalid
 */
public class DownloadAction {

  private final HttpClient httpClient;

  private final String baseUrl;

  private static class Checksumer {
    private final HttpEntity entity;

    private String sha1;

    public Checksumer(HttpEntity entity) {
      this.entity = entity;
    }

    public void consumeEntity() throws IOException {
      this.sha1 = Digests.getDigest(entity, "sha1");
    }

    public String getSha1() {
      return sha1;
    }
  }

  public DownloadAction(HttpClient httpClient, String baseUrl) {
    this.httpClient = httpClient;
    this.baseUrl = baseUrl;
  }


  public void download(String path) throws IOException {


    final String url = baseUrl.endsWith("/") ? baseUrl + path : baseUrl + "/" + path;

    final HttpGet httpGet = new HttpGet(url);

    final HttpResponse response = httpClient.execute(httpGet);

    if (!isSuccess(response)) {
      EntityUtils.consume(response.getEntity());

      if (response.getStatusLine().getStatusCode() != 404) {
        throw new IOException(response.getStatusLine().toString());
      }

      return;
    }

    // consume entity entirely
    final Checksumer checksumer = new Checksumer(response.getEntity());
    checksumer.consumeEntity();

    if(!url.contains(".meta/nexus-smartproxy-plugin/handshake/")){
      final String sha1 = getUrlContents(url + ".sha1");
      if (sha1 != null) {
        if (!sha1.startsWith(checksumer.getSha1())) {
          throw new IOException("Wrong SHA1 " + url);
        }
      }
    }

  }

  protected boolean isSuccess(HttpResponse response) {
    return response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() <= 299;
  }

  private String getUrlContents(String url) throws IOException {
    final HttpGet httpGet = new HttpGet(url);

    HttpResponse response = httpClient.execute(httpGet);

    if (!isSuccess(response)) {
      EntityUtils.consume(response.getEntity());
      return null;
    }

    return EntityUtils.toString(response.getEntity(), (Charset) null);
  }

}
