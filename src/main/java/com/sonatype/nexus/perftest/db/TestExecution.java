/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package com.sonatype.nexus.perftest.db;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Performance metrics of a one execution of one test.
 */
public class TestExecution {
  private final String testId;
  private final String executionId;
  private final Map<String, Double> metrics = new HashMap<>();

  public TestExecution(String testId, String executionId) {
    this.testId = testId;
    this.executionId = executionId;
  }

  public String getTestId() {
    return testId;
  }

  public String getExecutionId() {
    return executionId;
  }

  public void addMetric(String metricId, double value) {
    if (metrics.containsKey(metricId)) {
      throw new IllegalArgumentException(String.format("Duplicate metric=%s", metricId));
    }
    metrics.put(metricId, value);
  }

  public double getMetric(String metricId) {
    if (!metrics.containsKey(metricId)) {
      throw new IllegalArgumentException(String.format("No such metric=%s", metricId));
    }
    return metrics.get(metricId);
  }

  public Set<String> getMetricIds() {
    return Collections.unmodifiableSet(metrics.keySet());
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("TestExecution{");
    sb.append("testId='").append(testId).append('\'');
    sb.append(", executionId='").append(executionId).append('\'');
    sb.append(", metrics=").append(metrics);
    sb.append('}');
    return sb.toString();
  }
}
