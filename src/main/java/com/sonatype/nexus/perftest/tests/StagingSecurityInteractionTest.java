/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package com.sonatype.nexus.perftest.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import com.sonatype.nexus.perftest.ClientSwarm;
import com.sonatype.nexus.perftest.Metric;
import com.sonatype.nexus.perftest.Nexus;
import com.sonatype.nexus.perftest.ProgressTickThread;
import com.sonatype.nexus.perftest.RequestRate;
import com.sonatype.nexus.perftest.db.PerformanceMetricDescriptor;
import com.sonatype.nexus.perftest.db.TestExecution;
import com.sonatype.nexus.perftest.db.TestExecutions;
import com.sonatype.nexus.perftest.maven.DownloadOperation;
import com.sonatype.nexus.perftest.maven.HttpdLogParser;
import com.sonatype.nexus.perftest.maven.StagingOperation;
import com.sonatype.nexus.perftest.ossrh.ProjectProvisioningOperation;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class StagingSecurityInteractionTest {
  // request types
  // - large number of clients perform download requests against public group
  // - few clients stage artifacts
  // this usage pattern is common to large forges like oss.sonatype.org
  // and triggers cpu-expensive recalculation of security configuration
  // after each staging repository created or dropped

  public static final int DOWNLOAD_TCOUNT = 500;

  public static final int STAGE_TCOUNT = 0;

  private static final String executionId = System.getProperty("perftest.executionId");
  private static final String baselineId = System.getProperty("perftest.baselineId");

  @Rule
  public TestName testName = new TestName();

  private String getTestId() {
    return getClass().getName() + "#" + testName.getMethodName();
  }

  @Test
  public void testStagingSecurityInteraction() throws Exception {
    if (executionId != null) {
      TestExecutions.assertUnique(getTestId(), executionId);
    }

    TestExecution baseline = null;
    if (baselineId != null) {
      baseline = TestExecutions.select(getTestId(), baselineId);
      Assert.assertNotNull(baseline);
    }

    Nexus nexus = new Nexus();

    // download client swarm
    HttpdLogParser paths = new HttpdLogParser(new File("ossrh-2013-08-01-ssl.public.log.gz"));
    final DownloadOperation download = new DownloadOperation(nexus, "releases", paths);
    final RequestRate downloadRate = new RequestRate(5, TimeUnit.SECONDS);
    final ClientSwarm downloaders = new ClientSwarm("download", download, downloadRate, DOWNLOAD_TCOUNT);
    final Metric downloadMetric = downloaders.getMetric();

    // staging client swarm
    final StagingOperation stage = new StagingOperation(nexus, new File("pom.xml"));
    final RequestRate stageRate = new RequestRate(2, TimeUnit.MINUTES);
    final ClientSwarm stagers = new ClientSwarm("stage", stage, stageRate, STAGE_TCOUNT);
    final Metric stageMetric = stagers.getMetric();

    // project provisioning
    final ProjectProvisioningOperation provisioning = new ProjectProvisioningOperation(nexus);
    final RequestRate provisioningRate = new RequestRate(1, TimeUnit.MINUTES);
    final ClientSwarm provisioners = new ClientSwarm("provisioning", provisioning, provisioningRate, 0);
    final Metric provisioningMetric = provisioners.getMetric();

    downloaders.start();
    stagers.start();
    provisioners.start();

    final Metric[] metrics = new Metric[] {downloadMetric, stageMetric, provisioningMetric};

    new ProgressTickThread(metrics);

    Thread.sleep(TimeUnit.MINUTES.toMillis(1));

    downloaders.stop();
    stagers.stop();
    provisioners.stop();

    if (executionId != null) {
      assertPerformance(metrics, baseline);
    }

    System.err.println("All done.");
  }

  private void assertPerformance(Metric[] metrics, TestExecution baseline) {
    TestExecution execution = new TestExecution(getTestId(), executionId);
    Collection<PerformanceMetricDescriptor> descriptors = new ArrayList<>();
    for (Metric metric : metrics) {
      descriptors.add(new PerformanceMetricDescriptor(metric.getName() + ".successCount", 0.9f, 1.1f));
      execution.addMetric(metric.getName() + ".successCount", metric.getSuccesses());

      descriptors.add(new PerformanceMetricDescriptor(metric.getName() + ".successDuration", 0.9f, 1.1f));
      execution.addMetric(metric.getName() + ".successDuration", metric.getSuccessDuration());

      descriptors.add(new PerformanceMetricDescriptor(metric.getName() + ".failureCount", 0.9f, 1.1f));
      execution.addMetric(metric.getName() + ".failureCount", metric.getFailures());
    }

    TestExecutions.insert(execution);

    if (baseline != null) {
      TestExecutions.assertPerformance(descriptors, baseline, execution);
    }
  }
}
