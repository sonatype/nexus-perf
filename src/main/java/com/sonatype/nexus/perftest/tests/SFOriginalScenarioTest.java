/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package com.sonatype.nexus.perftest.tests;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.sonatype.nexus.perftest.ClientSwarm;
import com.sonatype.nexus.perftest.Metric;
import com.sonatype.nexus.perftest.Nexus;
import com.sonatype.nexus.perftest.ProgressTickThread;
import com.sonatype.nexus.perftest.RequestRate;
import com.sonatype.nexus.perftest.maven.BatchDownloadsOperation;
import com.sonatype.nexus.perftest.maven.HttpdLogParser;
import com.sonatype.nexus.perftest.maven.UniqueRepositoryDeployOperation;

public class SFOriginalScenarioTest {

  public final static int DOWNLOAD_TCOUNT = 1000;

  public final static int DEPLOY_TCOUNT = 40;

  public static void main(String[] args) throws InterruptedException, IOException {

    // target is ~2000 download requests per day and ~400 deploy requests per day
    // will test with 5 times the target rate

    Nexus nexus = new Nexus();

    final String basedir = System.getProperty("perftest.jars");

    // dev env provisioning swamp
    HttpdLogParser paths = new HttpdLogParser(new File("maven-3.1-build-artifact-access.log"));
    BatchDownloadsOperation provision = new BatchDownloadsOperation(nexus, "public", paths);
    final RequestRate provisionRate = new RequestRate(5 * 2000, TimeUnit.DAYS);
    final ClientSwarm provisioners = new ClientSwarm("provision", provision, provisionRate, DOWNLOAD_TCOUNT);
    final Metric provisionMetric = provisioners.getMetric();

    // deploy artifacts to unique repositories
    UniqueRepositoryDeployOperation deploy =
        new UniqueRepositoryDeployOperation(nexus, new File(basedir), new File("pom.xml"), true, false);
    final RequestRate deployRate = new RequestRate(5 * 400, TimeUnit.DAYS);
    final ClientSwarm deployers = new ClientSwarm("deploy", deploy, deployRate, DEPLOY_TCOUNT);
    final Metric deployMetric = deployers.getMetric();

    provisioners.start();
    deployers.start();

    final Metric[] metrics = new Metric[] {provisionMetric, deployMetric};
    new ProgressTickThread(metrics);
    Thread.sleep(TimeUnit.MINUTES.toMillis(10));

    provisioners.stop();
    deployers.stop();
  }
}
