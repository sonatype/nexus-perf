/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package com.sonatype.nexus.perftest.maven;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sonatype.nexus.perftest.AbstractNexusOperation;
import com.sonatype.nexus.perftest.ClientSwarm.ClientRequestInfo;
import com.sonatype.nexus.perftest.ClientSwarm.Operation;
import com.sonatype.nexus.perftest.Nexus;
import com.sonatype.nexus.staging.client.Profile;
import com.sonatype.nexus.staging.client.StagingWorkflowV3Service;
import com.sonatype.nexus.staging.client.rest.JerseyStagingWorkflowV3SubsystemFactory;
import com.sun.jersey.api.client.ClientHandlerException;

/**
 * Stages and immediately releases a number of artifacts in Nexus.
 */
@JsonTypeName
public class StagingOperation extends AbstractNexusOperation implements Operation {
  private static final String PROFILE_NAME = "test-staging-profile";

  /**
   * Number of deployed artifact per staging
   */
  private static final int ARTIFACT_COUNT = 10;

  private final File pomTemplate;

  @JsonCreator
  public StagingOperation(@JacksonInject Nexus nexus, @JsonProperty("pomTemplate") File pomTemplate) {
    super(nexus);
    this.pomTemplate = pomTemplate;
  }

  @Override
  public void perform(ClientRequestInfo requestInfo) throws IOException {
    final int threadId = requestInfo.getClientId();
    final int stageCount = requestInfo.getRequestId();

    StagingWorkflowV3Service staging =
        getNexusClient(new JerseyStagingWorkflowV3SubsystemFactory()).getSubsystem(StagingWorkflowV3Service.class);

    Profile profile = getProfile(staging, PROFILE_NAME);
    Map<String, String> tags = Collections.emptyMap();
    String repositoryId =
        staging.startStaging(profile, String.format("started staging %02d-%03d", threadId, stageCount), tags);

    deployArtifacts(staging.startedRepositoryBaseUrl(profile, repositoryId), threadId, stageCount);

    staging.finishStaging(profile, repositoryId, String.format("finish staging %02d-%03d", threadId, stageCount));

    staging.releaseStagingRepositories(String.format("release staging %02d-%03d", threadId, stageCount), repositoryId);

    try {
      staging.dropStagingRepositories(String.format("drop staging %02d-%03d", threadId, stageCount), repositoryId);
    } catch (ClientHandlerException e) {
      // appears to be expected with Nexus 2.4
    }
  }

  private void deployArtifacts(String repositoryUrl, int threadId, int stageCount) throws IOException {
    ArtifactDeployer deployer = new ArtifactDeployer(getHttpClient(), repositoryUrl);

    String groupId = String.format("test.nexustaging-%02d", threadId);
    String version = String.format("1.%d", stageCount);

    for (int i = 0; i < ARTIFACT_COUNT; i++) {
      deployer.deployPom(groupId, String.format("artifact-%d", i), version, pomTemplate);
    }
  }

  private Profile getProfile(StagingWorkflowV3Service staging, String profileId) {
    for (Profile profile : staging.listProfiles()) {
      if (profileId.equals(profile.getName())) {
        return profile;
      }
    }
    throw new IllegalArgumentException(String.format("Profile with id=%s was not found", profileId));
  }
}
