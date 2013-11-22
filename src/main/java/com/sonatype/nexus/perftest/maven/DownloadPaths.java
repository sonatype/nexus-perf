/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package com.sonatype.nexus.perftest.maven;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, defaultImpl = HttpdLogParser.class, include = JsonTypeInfo.As.PROPERTY, property = "class")
@JsonSubTypes({
    @JsonSubTypes.Type(value=HttpdLogParser.class, name="httpd"),
    @JsonSubTypes.Type(value=CsvLogParser.class, name="csv")
})
public interface DownloadPaths {
  /**
   * Returns next path to download. Each call returns new path, but will eventually "wrap" and start path sequence from
   * the beginning. Can be called from multiple threads; each thread gets next path the same path sequence, in other
   * works, each thread gets different path.
   */
  public String getNext();

  /**
   * Returns all paths in the sequence. Can be called from multiple threads; each thread gets full path sequence, in
   * other words, each thread gets the same complete path sequence.
   */
  public Iterable<String> getAll();
}
