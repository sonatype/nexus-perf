/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package com.sonatype.nexus.perftest.nuget;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * URLs {@link NugetFeedRipper ripped} from a NuGet XML feed.
 */
public class NugetFeedData
{
  private final List<String> packageUrls;

  private final List<String> packageIds;

  private final String nextPageUri;

  public NugetFeedData(final List<String> packageUrls, final List<String> packageIds, final String nextPageUri) {
    this.packageUrls = checkNotNull(packageUrls);
    this.packageIds = checkNotNull(packageIds);
    this.nextPageUri = nextPageUri;
  }

  public List<String> getPackageUrls() {
    return packageUrls;
  }

  public String getNextPageUri() {
    return nextPageUri;
  }

  public List<String> getPackageIds() {
    return packageIds;
  }
}
