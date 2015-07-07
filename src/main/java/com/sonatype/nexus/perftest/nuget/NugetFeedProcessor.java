/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package com.sonatype.nexus.perftest.nuget;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.xml.stream.XMLStreamException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.util.EntityUtils;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Processes a nuget feed/query URI, extracting links.
 */
public class NugetFeedProcessor
{
  /**
   * A {@link String#format} template representing Nuget Package Explorer's search for all versions (including
   * pre-release versions) of a given artifact.
   */
  private static final String PKG_EXPLORER_ALL_VERSIONS = "FindPackagesById()?$orderby=Published%%20desc&$select=Id,Version,Authors,DownloadCount,VersionDownloadCount,PackageHash,PackageSize,Published&id='%s'";

  private final URI galleryRoot;

  private final NugetHttp http;

  final NugetRequestLogger requestLogger;

  boolean followNextPageLinks = true;

  boolean requestAllVersions = true;

  boolean includePackageDownloadLinks = true;

  boolean downloadPackages = false;

  public NugetFeedProcessor(final NugetRequestLogger requestLogger, final NugetHttp http, final URI galleryRoot) {
    this.requestLogger = checkNotNull(requestLogger);
    this.http = checkNotNull(http);
    this.galleryRoot = checkNotNull(galleryRoot);
  }

  /**
   * Should we follow 'next page' links, or be satisfied with the first page of content?
   */
  public NugetFeedProcessor followNextPageLinks(boolean followNextPageLink) {
    this.followNextPageLinks = followNextPageLink;
    return this;
  }

  /**
   * Every time we learn about a package, should we request all versions of it?
   */
  public NugetFeedProcessor requestAllVersions(final boolean requestAllVersions) {
    this.requestAllVersions = requestAllVersions;
    return this;
  }

  /**
   * Should the resulting script include links to the package content?
   */
  public NugetFeedProcessor includePackageDownloadLinks(final boolean includePackageDownloadLinks) {
    this.includePackageDownloadLinks = includePackageDownloadLinks;
    return this;
  }

  /**
   * Should we download the packages while building the script, or just leave that up to when the script is run?
   * This is only used to generate load and/or to prime a proxy server. It has no essential role in creating a script.
   */
  public NugetFeedProcessor downloadPackages(final boolean downloadPackages) {
    this.downloadPackages = downloadPackages;
    return this;
  }

  public NugetFeedProcessor duplicate() {
    return new NugetFeedProcessor(requestLogger, http, galleryRoot)
        .downloadPackages(downloadPackages)
        .followNextPageLinks(followNextPageLinks)
        .includePackageDownloadLinks(includePackageDownloadLinks)
        .requestAllVersions(requestAllVersions);
  }

  /**
   * Processes the feed found at the provided URI. The URI should be relative to the gallery root.
   */
  public void process(final String uri)
      throws Exception
  {
    String feedUri = galleryRoot.resolve(uri).toString();

    while (feedUri != null) {
      requestLogger.logUri(feedUri);

      HttpResponse response = null;
      try {
        response = http.get(feedUri.toString());

        if (status(response) != 200) {
          System.err.println(feedUri);
          System.err.println(response.getStatusLine());
          break;
        }

        NugetFeedData rip = ripFeed(response);

        for (String packageUrl : rip.getPackageUrls()) {
          if (includePackageDownloadLinks) {
            requestLogger.logUri(packageUrl);
          }

          if (downloadPackages) {
            http.getAndEat(packageUrl);
          }
        }

        // Now request a feed for every version of the packages we've discovered
        if (requestAllVersions) {
          for (String packageId : rip.getPackageIds()) {
            requestAllVersions(packageId);
          }
        }

        if (followNextPageLinks) {
          feedUri = rip.getNextPageUri();
        }
        else {
          feedUri = null;
        }
      }
      finally {
        HttpClientUtils.closeQuietly(response);
      }
    }
  }

  private void requestAllVersions(final String packageId) throws Exception
  {
    // Recurse into each package, but don't download each version of each version, that's an endless loop
    // ..and don't actually download the packages for each version, having the metadata is enough.
    duplicate()
        .followNextPageLinks(false)
        .requestAllVersions(false)
        .downloadPackages(false)
        .includePackageDownloadLinks(false)
        .process(allVersions(packageId));
  }

  private NugetFeedData ripFeed(final HttpResponse response) throws IOException, XMLStreamException {
    final HttpEntity entity = response.getEntity();
    try (InputStream content = entity.getContent()) {
      return new NugetFeedRipper().rip(content);
    }
    finally {
      EntityUtils.consume(entity);
    }
  }

  private int status(final HttpResponse response) {
    return response.getStatusLine().getStatusCode();
  }

  private String allVersions(String packageId) {
    return String.format(PKG_EXPLORER_ALL_VERSIONS, checkNotNull(packageId));
  }
}
