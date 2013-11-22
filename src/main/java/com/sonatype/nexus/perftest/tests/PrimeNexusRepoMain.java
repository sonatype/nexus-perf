/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package com.sonatype.nexus.perftest.tests;

import java.io.File;
import java.util.concurrent.TimeUnit;

import com.sonatype.nexus.perftest.ClientSwarm;
import com.sonatype.nexus.perftest.Metric;
import com.sonatype.nexus.perftest.Nexus;
import com.sonatype.nexus.perftest.ProgressTickThread;
import com.sonatype.nexus.perftest.RequestRate;
import com.sonatype.nexus.perftest.maven.CsvLogParser;
import com.sonatype.nexus.perftest.maven.DownloadOperation;
import com.sonatype.nexus.perftest.maven.DownloadPaths;
import com.sonatype.nexus.perftest.maven.HttpdLogParser;

public class PrimeNexusRepoMain {

  public static final int DOWNLOAD_TCOUNT = 20;

  public static void main(String[] args) throws Exception {
    Nexus nexus = new Nexus();

    String data = System.getProperty("data.file", "maven-3.1-build-artifact-access.log.gz");

    DownloadPaths paths = null;

    if (data.contains("csv")) {
      paths = new CsvLogParser(new File(data));
    }
    else {
      paths = new HttpdLogParser(new File(data));
    }


    final DownloadOperation download = new DownloadOperation(nexus, "public", paths);
    final RequestRate downloadRate = new RequestRate(5, TimeUnit.SECONDS);
    final ClientSwarm downloaders = new ClientSwarm("download", download, downloadRate, DOWNLOAD_TCOUNT);
    final Metric downloadMetric = downloaders.getMetric();

    downloaders.start();

    final Metric[] metrics = new Metric[]{downloadMetric};
    new ProgressTickThread(metrics);

    Thread.sleep(100L * 1000L);
  }
}
