/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package com.sonatype.nexus.perftest;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Measures number of times and duration of successful execution of one test operation. Also measures number of times
 * the operation failed.
 */
public class Metric {
  private final String name;

  private final AtomicInteger outstanding = new AtomicInteger();

  private final AtomicInteger successes = new AtomicInteger();

  private final AtomicLong successDuration = new AtomicLong();

  private final AtomicInteger failures = new AtomicInteger();

  public class Context {
    final long start = System.currentTimeMillis();

    // counters are not updated atomically but that's okay
    // we only use them for progress feedback

    public void success() {
      successDuration.addAndGet(System.currentTimeMillis() - start);
      outstanding.decrementAndGet();
      successes.incrementAndGet();
    }

    public void failure(String message) {
      outstanding.decrementAndGet();
      failures.incrementAndGet();
    }
  }

  public Metric(String name) {
    this.name = name;
  }

  Context time() {
    outstanding.incrementAndGet();
    return new Context();
  }

  public String getName() {
    return name;
  }

  public int getOutstanding() {
    return outstanding.intValue();
  }

  public int getSuccesses() {
    return successes.intValue();
  }

  public long getSuccessDuration() {
    return successDuration.longValue();
  }

  public int getFailures() {
    return failures.intValue();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Metric{");
    sb.append("name='").append(name).append('\'');
    sb.append(", failures=").append(failures);
    sb.append(", outstanding=").append(outstanding);
    sb.append(", successDuration=").append(successDuration);
    sb.append(", successes=").append(successes);
    sb.append('}');
    return sb.toString();
  }
}
