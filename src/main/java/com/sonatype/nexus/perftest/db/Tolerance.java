/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package com.sonatype.nexus.perftest.db;

public class Tolerance {
  private static final double PRECISION = 0.001;

  private final double lowerBand;
  private final double upperBand;

  public Tolerance(double lowerBand, double upperBand) {
    this.lowerBand = lowerBand;
    this.upperBand = upperBand;
  }

  public boolean equals(double baseline, double actual) {
    return (actual - PRECISION) < upperBand * baseline && (actual + PRECISION) > lowerBand * baseline;
  }

  @Override
  public String toString() {
    return String.format("%.2f-%.2f", lowerBand, upperBand);
  }
}
