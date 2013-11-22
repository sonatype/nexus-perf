/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package com.sonatype.nexus.perftest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * Models a group of similar clients. The clients performs the same operation. Request rate is configured for the swarm.
 */
public class ClientSwarm {

  private final List<Thread> threads;

  private final Metric metric;

  public static interface ClientRequestInfo {
    String getSwarmName();

    int getClientId();

    int getRequestId();

    <T> void setContextValue(String key, T value);

    <T> T getContextValue(String key);
  }

  @JsonTypeInfo(use = Id.MINIMAL_CLASS, include = As.PROPERTY, property = "class")
  public static interface Operation {
    public void perform(ClientRequestInfo requestInfo) throws Exception;
  }

  private static class ClientThread extends Thread implements ClientRequestInfo {
    private final String swarmName;

    private final int clientId;

    private final Operation operation;

    private final RequestRate rate;

    private final Metric metric;

    private int requestId;

    private final HashMap<String, Object> context = new HashMap<>();

    public ClientThread(String swarmName, int clientId, Operation operation, Metric metric, RequestRate rate) {
      super(String.format("%s-%d", swarmName, clientId));
      this.swarmName = swarmName;
      this.clientId = clientId;
      this.operation = operation;
      this.metric = metric;
      this.rate = rate;
    }

    @Override
    public final void run() {
      while (true) {
        requestId++;
        try {
          rate.delay();
        } catch (InterruptedException e) {
          break;
        }

        Metric.Context context = metric.time();
        boolean success = false;
        String failureMessage = null;
        try {
          operation.perform(this);
          success = true;
        } catch (InterruptedException e) {
          // TODO more graceful shutdown
          break;
        } catch (Exception e) {
          failureMessage = e.getMessage();

          e.printStackTrace();
        } finally {
          if (success) {
            context.success();
          } else {
            context.failure(failureMessage);
          }
        }
      }
    }

    @Override
    public String getSwarmName() {
      return swarmName;
    }

    @Override
    public int getClientId() {
      return clientId;
    }

    @Override
    public int getRequestId() {
      return requestId;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getContextValue(String key) {
      return (T) context.get(key);
    }

    @Override
    public <T> void setContextValue(String key, T value) {
      context.put(key, value);
    }
  }

  @JsonCreator
  public ClientSwarm(@JsonProperty("name") String name, @JsonProperty("operation") Operation operation,
      @JsonProperty("rate") RequestRate rate, @JsonProperty("numberOfClients") int clientCount) {

    metric = new Metric(name);
    List<Thread> threads = new ArrayList<>();
    for (int i = 0; i < clientCount; i++) {
      threads.add(new ClientThread(name, i, operation, metric, rate));
    }
    this.threads = Collections.unmodifiableList(threads);
  }

  public void start() {
    for (Thread thread : threads) {
      thread.start();
    }
  }

  public void stop() throws InterruptedException {
    for (Thread thread : threads) {
      for (int i = 0; i < 3 && thread.isAlive(); i++) {
        thread.interrupt();
        thread.join(1000L);
      }
      if (thread.isAlive()) {
        StringBuilder sb = new StringBuilder(String.format("Thread %s ignored interrupt flag\n", thread.getName()));
        for (StackTraceElement f : thread.getStackTrace()) {
          sb.append("\t").append(f.toString()).append("\n");
        }
        System.err.println(sb.toString());
      }
    }
  }

  public Metric getMetric() {
    return metric;
  }
}
