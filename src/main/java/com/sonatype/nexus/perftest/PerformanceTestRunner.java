/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package com.sonatype.nexus.perftest;

import java.io.File;

import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class PerformanceTestRunner {
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
    System.out.format("Using test configuration %s\n", src);
    PerformanceTest test = mapper.readValue(src, PerformanceTest.class);
    test.run();
    System.out.println("Exit");
    System.exit(0);
  }
}
