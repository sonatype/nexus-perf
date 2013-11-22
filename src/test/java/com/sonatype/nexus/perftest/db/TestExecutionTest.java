/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package com.sonatype.nexus.perftest.db;

import java.util.Arrays;

import org.junit.Test;

public class TestExecutionTest {

  @Test(expected = IllegalArgumentException.class)
  public void testDuplicateMetric() {
    TestExecution actual = new TestExecution("testid", "label");

    actual.addMetric("metric", 123);
    actual.addMetric("metric", 123);
  }

  @Test
  public void testBasicSuccess() {
    PerformanceMetricDescriptor metricA = new PerformanceMetricDescriptor("metric.A", 0.8f, 1.2f);
    PerformanceMetricDescriptor metricB = new PerformanceMetricDescriptor("metric.B", 0.1f, 1.1f);

    TestExecution baseline = new TestExecution("testid", "label-a");
    baseline.addMetric(metricA.getMetricId(), 10);
    baseline.addMetric(metricB.getMetricId(), 255);

    TestExecution actual = new TestExecution("testid", "label-b");
    actual.addMetric(metricA.getMetricId(), 11);
    actual.addMetric(metricB.getMetricId(), 260);

    TestExecutions.assertPerformance(Arrays.asList(metricA, metricB), baseline, actual);
  }

  @Test(expected = PerformanceAssertionFailure.class)
  public void testBasicFailure() {
    PerformanceMetricDescriptor metric = new PerformanceMetricDescriptor("metric", 0.8f, 1.2f);

    TestExecution baseline = new TestExecution("testid", "label-a");
    baseline.addMetric(metric.getMetricId(), 10);

    TestExecution actual = new TestExecution("testid", "label-b");
    actual.addMetric(metric.getMetricId(), 100);

    TestExecutions.assertPerformance(Arrays.asList(metric), baseline, actual);
  }
}
