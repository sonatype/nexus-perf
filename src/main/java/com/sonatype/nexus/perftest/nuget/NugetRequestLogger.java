/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package com.sonatype.nexus.perftest.nuget;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Relativizes requested URLs before sending them to output.
 */
public class NugetRequestLogger
{
  private final URI galleryRoot;

  private final BufferedWriter writer;

  public NugetRequestLogger(final URI galleryRoot, final BufferedWriter writer) {
    this.galleryRoot = checkNotNull(galleryRoot);
    this.writer = checkNotNull(writer);
  }

  /**
   * Logs the relative form of the provided absolute URI, in order to build a request script.
   */
  public void logUri(String absoluteUri) throws IOException {
    try {
      final URI uri = new URI(absoluteUri);
      final URI relative = galleryRoot.relativize(uri);

      System.out.println(relative);

      writer.write(relative.toString());
      writer.newLine();
    }
    catch (URISyntaxException e) {
      System.err.println("Bad URI " + absoluteUri);
    }
  }
}
