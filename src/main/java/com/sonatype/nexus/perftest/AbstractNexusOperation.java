/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package com.sonatype.nexus.perftest;

import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;

import com.sonatype.nexus.perftest.ClientSwarm.ClientRequestInfo;

import org.sonatype.nexus.client.core.NexusClient;
import org.sonatype.nexus.client.core.spi.SubsystemFactory;
import org.sonatype.nexus.client.core.spi.subsystem.repository.RepositoryFactory;
import org.sonatype.nexus.client.core.subsystem.repository.Repositories;
import org.sonatype.nexus.client.internal.rest.jersey.subsystem.repository.maven.JerseyMavenGroupRepositoryFactory;
import org.sonatype.nexus.client.internal.rest.jersey.subsystem.repository.maven.JerseyMavenHostedRepositoryFactory;
import org.sonatype.nexus.client.internal.rest.jersey.subsystem.repository.maven.JerseyMavenProxyRepositoryFactory;
import org.sonatype.nexus.client.rest.AuthenticationInfo;
import org.sonatype.nexus.client.rest.BaseUrl;
import org.sonatype.nexus.client.rest.ConnectionInfo;
import org.sonatype.nexus.client.rest.UsernamePasswordAuthenticationInfo;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClient;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClientFactory;
import org.sonatype.nexus.client.rest.jersey.subsystem.JerseyRepositoriesFactory;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public abstract class AbstractNexusOperation
{
  public static final int HTTP_TIMEOUT = Integer.parseInt(System.getProperty("perftest.http.timeout", "60000"));

  protected final String username;

  protected final String password;

  protected final String nexusBaseurl;

  public AbstractNexusOperation(Nexus nexus) {
    this.nexusBaseurl = sanitizeBaseurl(nexus.getBaseurl());
    this.username = nexus.getUsername();
    this.password = nexus.getPassword();
  }

  private static String sanitizeBaseurl(String nexusBaseurl) {
    if (nexusBaseurl == null || nexusBaseurl.trim().isEmpty()) {
      return null;
    }
    nexusBaseurl = nexusBaseurl.trim();
    return nexusBaseurl.endsWith("/") ? nexusBaseurl : (nexusBaseurl + "/");
  }

  protected DefaultHttpClient getHttpClient() {
    ClientRequestInfo info = ((ClientRequestInfo) Thread.currentThread());

    DefaultHttpClient httpclient = info.getContextValue("httpclient");
    if (httpclient == null) {
      HttpParams params = new BasicHttpParams();
      HttpConnectionParams.setConnectionTimeout(params, HTTP_TIMEOUT);
      HttpConnectionParams.setSoTimeout(params, HTTP_TIMEOUT);
      httpclient = new DefaultHttpClient(params);
      httpclient.getCredentialsProvider().setCredentials(AuthScope.ANY,
          new UsernamePasswordCredentials(username, password));
      info.setContextValue("httpclient", httpclient);
    }
    return httpclient;
  }

  protected boolean isSuccess(HttpResponse response) {
    return response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() <= 299;
  }

  @SuppressWarnings("unchecked")
  protected NexusClient getNexusClient(final SubsystemFactory<?, JerseyNexusClient>... subsystemFactories) {
    if (nexusBaseurl == null) {
      throw new IllegalStateException();
    }
    BaseUrl baseUrl;
    try {
      baseUrl = BaseUrl.baseUrlFrom(this.nexusBaseurl);
    }
    catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
    AuthenticationInfo authenticationInfo = new UsernamePasswordAuthenticationInfo(this.username, this.password);
    ConnectionInfo connectionInfo = new ConnectionInfo(baseUrl, authenticationInfo, null /* proxyInfos */);
    return new JerseyNexusClientFactory(subsystemFactories).createFor(connectionInfo);
  }

  @SuppressWarnings("rawtypes")
  protected JerseyRepositoriesFactory newRepositoryFactories() {
    Set<RepositoryFactory> repositoryFactories = new HashSet<>();
    repositoryFactories.add(new JerseyMavenHostedRepositoryFactory());
    repositoryFactories.add(new JerseyMavenGroupRepositoryFactory());
    repositoryFactories.add(new JerseyMavenProxyRepositoryFactory());
    return new JerseyRepositoriesFactory(repositoryFactories);
  }

  @SuppressWarnings("unchecked")
  protected String getRepoBaseurl(String repoid) {
    final String layout = System.getProperty("nexus.layout", "NX3");
    switch (layout) {
      case "NX2":
        return getNexusClient(newRepositoryFactories()).getSubsystem(Repositories.class).get(repoid).contentUri();
      case "NX3":
        return this.nexusBaseurl + "repository/" + repoid;
      default:
        throw new IllegalArgumentException("Unknown -Dnexus.layout: " + layout);
    }
  }
}
