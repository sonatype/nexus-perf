/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package com.sonatype.nexus.perftest.maven;

import com.sonatype.nexus.perftest.AbstractNexusOperation;
import com.sonatype.nexus.perftest.ClientSwarm.ClientRequestInfo;
import com.sonatype.nexus.perftest.ClientSwarm.Operation;
import com.sonatype.nexus.perftest.Nexus;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Simulates repository requests performed during a maven build. List of artifacts requested by the build comes from
 * httpd/jetty access log file
 */
public class BatchDownloadsOperation extends AbstractNexusOperation implements Operation {

  private final String repoBaseurl;

  private final DownloadPaths paths;

  @JsonCreator
  public BatchDownloadsOperation(@JacksonInject Nexus nexus, @JsonProperty("repo") String repo,
      @JsonProperty("paths") HttpdLogParser paths) {
    super(nexus);
    this.repoBaseurl = getRepoBaseurl(repo);
    this.paths = paths;
  }

  @Override
  public void perform(ClientRequestInfo requestInfo) throws Exception {
    // parse contents of httd log, download artifacts in the log, check sha1,
    // fail if any is not available or checksum fails

    DefaultHttpClient httpClient = getHttpClient();

    for (String path : paths.getAll()) {
      if (path.endsWith(".jar") || path.endsWith(".pom")) {
        new DownloadAction(httpClient, repoBaseurl).download(path);
      }
    }
  }
}
