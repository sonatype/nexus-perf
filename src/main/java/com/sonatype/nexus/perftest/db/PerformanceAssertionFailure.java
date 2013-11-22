/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package com.sonatype.nexus.perftest.db;

import java.util.Collection;

public class PerformanceAssertionFailure extends AssertionError {

  private static final long serialVersionUID = -6209672222493199949L;

  public PerformanceAssertionFailure(TestExecution baseline, TestExecution actual, Collection<String> errors) {
    super(formatMessage(baseline, actual, errors));
  }

  private static String formatMessage(TestExecution baseline, TestExecution actual, Collection<String> errors) {
    StringBuilder sb = new StringBuilder();

    sb.append("test=").append(actual.getTestId());
    sb.append(" buildId=").append(actual.getExecutionId());
    sb.append(" baselineId=").append(baseline.getExecutionId());
    
    for (String error : errors) {
      sb.append("\n   ").append(error);
    }

    return sb.toString();
  }
}
