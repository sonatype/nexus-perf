/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package com.sonatype.nexus.perftest.nuget;

import com.sonatype.nexus.perftest.AbstractNexusOperation;
import com.sonatype.nexus.perftest.ClientSwarm.ClientRequestInfo;
import com.sonatype.nexus.perftest.ClientSwarm.Operation;
import com.sonatype.nexus.perftest.Nexus;
import com.sonatype.nexus.perftest.maven.DownloadPaths;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Downloads series of artifacts from a maven2 repository.
 */
public class NugetDownloadOperation
    extends AbstractNexusOperation
    implements Operation
{
  private final String repoBaseurl;

  private final DownloadPaths paths;

  public NugetDownloadOperation(@JacksonInject Nexus nexus, @JsonProperty("repo") String repoid,
                                @JsonProperty("paths") DownloadPaths paths)
  {
    super(nexus);
    this.repoBaseurl = getRepoBaseurl(repoid);
    this.paths = paths;
  }

  @Override
  public void perform(ClientRequestInfo requestInfo) throws Exception {
    new NugetDownloadAction(getHttpClient(), repoBaseurl).download(paths.getNext());
  }

  @Override
  protected String getRepoBaseurl(final String repoId) {
    final String layout = System.getProperty("nexus.layout", "NX3");
    switch (layout) {
      case "NX2":
        return this.nexusBaseurl + "service/local/nuget/" + repoId;
      case "NX3":
        return super.getRepoBaseurl(repoId);
      default:
        throw new IllegalArgumentException("Unknown -Dnexus.layout: " + layout);
    }
  }
}
