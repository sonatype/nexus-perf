/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package com.sonatype.nexus.perftest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Prints test execution progress to STDOUT
 */
public class ProgressTickThread extends Thread {
  private static final DateFormat DF = new SimpleDateFormat("YYYY-MM-dd hh:mm:ss");

  private final Metric[] metrics;

  private final long start = System.currentTimeMillis();

  public ProgressTickThread(Metric... metrics) {
    this.metrics = metrics;
    setDaemon(true);
    start();
  }

  @Override
  public void run() {
    while (sleep()) {
      printTick();
    }
  }

  public void printTick() {
    System.out.format(DF.format(new Date()));
    System.out.format(" (%d)", (System.currentTimeMillis() - start) / 1000);
    for (Metric metric : metrics) {
      int successes = metric.getSuccesses();
      long duration = successes > 0 ? metric.getSuccessDuration() / successes : 0;
      System.out.format(" %s=%d/%d(~%d)/%d", metric.getName(), metric.getOutstanding(), successes, duration,
          metric.getFailures());
    }
    System.out.println();
  }

  private boolean sleep() {
    try {
      sleep(5000L);
      return true;
    } catch (InterruptedException e) {
      return false;
    }
  }
}
