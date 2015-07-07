/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package com.sonatype.nexus.perftest.nuget;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;

import com.google.common.base.Charsets;

/**
 * Util for dumping text to a gzip file.
 */
public class GzipTextFileOutput
    implements AutoCloseable
{
  final BufferedWriter writer;

  public GzipTextFileOutput(final File output) throws IOException {
    GZIPOutputStream zip = new GZIPOutputStream(new FileOutputStream(output));
    writer = new BufferedWriter(new OutputStreamWriter(zip, Charsets.UTF_8));
  }

  public BufferedWriter getWriter() {
    return writer;
  }

  @Override
  public void close() throws Exception {
    writer.close();
  }
}
