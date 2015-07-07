/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package com.sonatype.nexus.perftest.nuget;

import java.io.File;
import java.net.URI;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A program that interacts with a NuGet repository, and prints a stream of interaction URIs to a data file for use
 * by RunNugetInitialAndSearch.
 */
public class NugetRequestScriptMaker
{
  /**
   * Visual Studio's initial artifact query.
   */
  private static final String VISUAL_STUDIO_INITIAL_FEED_QUERY =
      "Search()?$filter=IsLatestVersion&$orderby=DownloadCount%20desc,Id&$skip=0&$top=1000&searchTerm=''&targetFramework='net45'&includePrerelease=false";

  /**
   * A {@link String#format} template representing visual studio's keyword search request.
   */
  public static final String VS_SEARCH_FEED_TEMPLATE = "Search()?$filter=IsAbsoluteLatestVersion&$skip=0&$top=1000&searchTerm='%s'&targetFramework='net45'&includePrerelease=true";

  public static final String[] SEARCH_TERMS = new String[]{
      "jQuery", "Entity", "Net", "Web", "Json", "javascript", "validation", "Microsoft", "Asp", "System", "Sql", "Ajax",
      "Windows", "Angular", "AspNet", "Collection", "Core", "Data", "File"
  };

  private final URI galleryRoot;

  private final NugetHttp http = new NugetHttp();

  private NugetRequestLogger requestLogger;

  /**
   * @param args The URL to the root of the NuGet server to interact with
   */
  public static void main(String[] args) throws Exception {
    checkArgument(args.length >= 1);

    new NugetRequestScriptMaker(new URI(args[0])).generateLoad();
  }

  public NugetRequestScriptMaker(URI galleryRoot) {
    this.galleryRoot = galleryRoot;
  }

  private void generateLoad() throws Exception {
    try (GzipTextFileOutput gzipOutput = new GzipTextFileOutput(new File("data/nuget-initial-and-searches.csv.gz"))) {
      requestLogger = new NugetRequestLogger(galleryRoot, gzipOutput.getWriter());

      final NugetFeedProcessor feedProcessor = new NugetFeedProcessor(requestLogger, http, galleryRoot)
          .requestAllVersions(false);

      // Start off by downloading the initial feed - nuget.org lets this run forever, but Nexus cuts this off after 80
      // packages
      feedProcessor.process(VISUAL_STUDIO_INITIAL_FEED_QUERY);

      // Now search for a bunch of commonly used terms, following all the package and 'next' links
      for (String term : SEARCH_TERMS) {
        feedProcessor.process(searchUrl(term));
      }
    }
  }

  private String searchUrl(String term) {
    return String.format(VS_SEARCH_FEED_TEMPLATE, term);
  }
}
