/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package com.sonatype.nexus.perftest.maven;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HttpdLogParser implements DownloadPaths {

  private static final String PREFIX = "/content/groups/public/";

  // ossrh ssl public repo access log for 2013-08-01 contains 214895 paths 15458118 chars in total
  // this fits in ~30M of heap, so heap should not be a problem for any meaningful test.
  private final List<String> paths;

  private final AtomicInteger nextIndex = new AtomicInteger(0);

  @JsonCreator
  public HttpdLogParser(@JsonProperty("logfile") File logfile) throws IOException {
    ArrayList<String> paths = new ArrayList<>();
    try (BufferedReader br =
        new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(logfile))))) {
      String str;
      while ((str = br.readLine()) != null) {
        StringTokenizer st = new StringTokenizer(str, "[]\" ");
        st.nextToken(); // ip
        st.nextToken(); // not sure
        st.nextToken(); // username
        st.nextToken(); // [date:time
        st.nextToken(); // timezoneoffset]
        String method = st.nextToken(); // "METHOD
        if ("GET".equals(method)) {
          String path = st.nextToken(); // path
          if (path.startsWith(PREFIX)) {
            paths.add(path.substring(PREFIX.length()));
          }
        }
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
