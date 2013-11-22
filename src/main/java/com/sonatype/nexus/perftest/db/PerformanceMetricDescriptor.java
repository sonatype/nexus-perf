/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package com.sonatype.nexus.perftest.db;

public class PerformanceMetricDescriptor {
  private final String metricId;
  private final Tolerance tolerance;

  public PerformanceMetricDescriptor(String metricId, double lowerBand, double upperBand) {
    this.metricId = metricId;
    this.tolerance = new Tolerance(lowerBand, upperBand);
  }

  public String getMetricId() {
    return metricId;
  }

  public String assertPerformance(TestExecution baseline, TestExecution actual) {
    final double baselineValue = baseline.getMetric(metricId);
    final double actualValue = actual.getMetric(metricId);
    if (!tolerance.equals(baselineValue, actualValue)) {
      return String.format("%s=%f is not within %s of baseline=%f", metricId, actualValue, tolerance.toString(),
          baselineValue);
    }
    return null;
  }

  @Override
  public String toString() {
    return String.format("%s with %s", metricId, tolerance.toString());
  }
}
