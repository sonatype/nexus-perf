/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package com.sonatype.nexus.perftest;

import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.sonatype.nexus.perftest.db.PerformanceMetricDescriptor;
import com.sonatype.nexus.perftest.db.TestExecution;
import com.sonatype.nexus.perftest.db.TestExecutions;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Compare already recorded executions
 */
public class PerformanceTestAsserter {

    private static final String buildId = System.getProperty("perftest.buildId");

    private static final String baselineId = System.getProperty("perftest.baselineId");

    public static void main(String[] args) throws Exception {

        final Nexus nexus = new Nexus();
        ObjectMapper mapper = new XmlMapper();

        mapper.setInjectableValues(new InjectableValues() {
            @Override
            public Object findInjectableValue(Object valueId, DeserializationContext ctxt, BeanProperty forProperty,
                                              Object beanInstance) {
                if (Nexus.class.getName().equals(valueId)) {
                    return nexus;
                }
                return null;
            }
        });

        File src = new File(args[0]).getCanonicalFile();
        String name = src.getName().substring(0, src.getName().lastIndexOf("."));

        Collection<ClientSwarm> swarms = mapper.readValue(src, PerformanceTest.class).getSwarms();

        List<Metric> metrics = new ArrayList<>();
        for (ClientSwarm swarm : swarms) {
            metrics.add(swarm.getMetric());
        }

      System.out.println("Test " + name + " metrics:" + metrics);
        assertTest(name, metrics);
        System.out.println("Exit");
        System.exit(0);
    }

    public static void assertTest(String name, List<Metric> metrics) throws InterruptedException {


        TestExecution baseline = null;
        if (baselineId != null) {
            baseline = TestExecutions.select(name, baselineId);
            if (baseline == null) {
                throw new RuntimeException(String.format("Baseline build %s is not found", baselineId));
            }
        }

        System.out.println("Baseline " + baselineId + " " + baseline );

        TestExecution execution = null;
        if (buildId != null) {
            execution = TestExecutions.select(name, buildId);
            if (execution == null) {
                throw new RuntimeException(String.format("Build ID execution %s is not found", buildId));
            }
        }

        System.out.println("Execution " + buildId + " " + execution );

        Collection<PerformanceMetricDescriptor> descriptors = new ArrayList<>();
        for (Metric metric : metrics) {
            descriptors.add(new PerformanceMetricDescriptor(metric.getName() + ".successCount", 0.9f, 1.1f));
            descriptors.add(new PerformanceMetricDescriptor(metric.getName() + ".successDuration", 0.9f, 1.1f));
            descriptors.add(new PerformanceMetricDescriptor(metric.getName() + ".failureCount", 0.9f, 1.1f));
        }

        TestExecutions.assertPerformance(descriptors, baseline, execution);

    }

}
