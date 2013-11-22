/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package com.sonatype.nexus.perftest;

import java.util.Random;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Schedules test client requests according to specified rate.
 */
public class RequestRate {
  private final Random rnd = new Random();

  private final long start;

  private final AtomicInteger count = new AtomicInteger();

  private final int period;

  /**
   * @param rate average number of requests per time {@code unit}
   * @param unit time unit of {@code rate} parameter
   */
  public RequestRate(int rate, TimeUnit unit) {
    this((int) (unit.toMillis(1) / rate));
  }

  private RequestRate(int period) {
    // TODO assert period is at least 10
    this.period = period;
    this.start = System.currentTimeMillis() + rnd.nextInt(period); // delay first event
  }

  @JsonCreator
  public RequestRate(String value) {
    this(parseRate(value));
  }

  private static int parseRate(String value) {
    StringTokenizer st = new StringTokenizer(value, " /");
    int time = Integer.parseInt(st.nextToken());
    TimeUnit unit = TimeUnit.valueOf(st.nextToken() + "S");
    return (int) (unit.toMillis(1) / time);
  }

  public void delay() throws InterruptedException {
    long next = start + (((long) period) * ((long) count.getAndIncrement())); // time of the next event
    long delay = Math.max(0, next - System.currentTimeMillis()); // delay until the next event

    Thread.sleep(delay);
  }

  public int getPeriod() {
    return period;
  }
}
