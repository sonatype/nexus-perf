/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package com.sonatype.nexus.perftest.nuget;

import java.io.InputStream;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

/**
 * Test for {@link NugetFeedData}
 */
public class NugetFeedRipperTest
{
  @Test
  public void parseNextPage() throws Exception {
    try (InputStream resourceAsStream = getClass().getResourceAsStream("/nuget/feed.xml")) {
      final NugetFeedData nugetFeedData = new NugetFeedRipper().rip(resourceAsStream);

      assertThat(nugetFeedData.getNextPageUri(), is(
          "https://www.nuget.org/api/v2/Search?searchTerm=''&targetFramework='net45'&includePrerelease=false" +
              "&$filter=IsLatestVersion&$orderby=DownloadCount%20desc,Id&$top=900&$skiptoken='Microsoft.AspNet.WebApi.OwinSelfHost','5.2.3',100"));

      assertThat(nugetFeedData.getPackageUrls().size(), is(100));

      assertThat(nugetFeedData.getPackageUrls(),
          hasItems("https://www.nuget.org/api/v2/package/AjaxControlToolkit/15.1.2",
              "https://www.nuget.org/api/v2/package/Angular.UI.Bootstrap/0.13.0",
              "https://www.nuget.org/api/v2/package/AngularJS.Core/1.3.15",
              "https://www.nuget.org/api/v2/package/AngularJS.Route/1.3.15",
              "https://www.nuget.org/api/v2/package/angularjs/1.4.0"));

      assertThat(nugetFeedData.getPackageIds(),
          hasItems("Newtonsoft.Json"));
    }
  }
}
