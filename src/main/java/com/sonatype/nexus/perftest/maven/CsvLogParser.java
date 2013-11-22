/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package com.sonatype.nexus.perftest.maven;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

public class CsvLogParser
    implements DownloadPaths
{

  private final List<String> paths;

  private final AtomicInteger nextIndex = new AtomicInteger(0);

  @JsonCreator
  public CsvLogParser(@JsonProperty("logfile") File logfile) throws IOException {

    ArrayList<String> paths = new ArrayList<>();
    try (BufferedReader br =
             new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(logfile))))) {
      String str;
      while ((str = br.readLine()) != null) {
        StringTokenizer st = new StringTokenizer(str, ",");
        paths.add(st.nextToken());  // full path
      }
    }
    this.paths = Collections.unmodifiableList(paths);
  }

  @Override
  public String getNext() {
    return paths.get(nextIndex.getAndIncrement() % paths.size());
  }

  @Override
  public Iterable<String> getAll() {
    return paths;
  }
}
