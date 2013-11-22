/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package com.sonatype.nexus.perftest.db;

import org.junit.Assert;
import org.junit.Test;

public class ToleranceTest {
  @Test
  public void testSameValue() {
    Assert.assertTrue(new Tolerance(0.9, 1.1).equals(1.23, 1.23));
  }

  @Test
  public void testGreaterThan() {
    Assert.assertFalse(new Tolerance(0.9, 1.1).equals(1.23, 2.23));
  }

  @Test
  public void testLessThan() {
    Assert.assertFalse(new Tolerance(0.9, 1.1).equals(1.23, 0.23));
  }
}
