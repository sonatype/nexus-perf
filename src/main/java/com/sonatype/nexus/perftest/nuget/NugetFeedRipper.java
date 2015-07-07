/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package com.sonatype.nexus.perftest.nuget;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.copyOf;

/**
 * Parses a NuGet feed, returning a next page URL and a list of package content URLs.
 */
public class NugetFeedRipper
{
  private final List<String> contentUrls = new ArrayList<>();

  private final List<String> packageIds = new ArrayList<>();

  private String nextUrl;

  // The element names that showing where are in the document structure
  private Stack<String> currentElements = new Stack<>();

  public NugetFeedData rip(InputStream feedXml) throws XMLStreamException {

    XMLInputFactory factory = XMLInputFactory.newInstance();
    XMLStreamReader reader = factory.createXMLStreamReader(feedXml);

    while (reader.hasNext()) {
      final int event = reader.next();

      updateCurrentElements(reader, event);


      if (event == XMLStreamConstants.START_ELEMENT) {
        if ("content".equals(reader.getLocalName())) {
          parseContentUrl(reader);
        }
        else if ("link".equals(reader.getLocalName())) {
          parseNextPage(reader);
        }
        else if ("title".equals(reader.getLocalName())) {

          if (currentElements.contains("entry")) {
            // If we're inside an <entry> element, then this <title> is the package id.
            checkState(reader.next() == XMLStreamConstants.CHARACTERS);
            packageIds.add(reader.getText());
          }
        }
      }
    }

    return new NugetFeedData(copyOf(contentUrls), packageIds, nextUrl);
  }

  private void updateCurrentElements(final XMLStreamReader reader, final int event) {
    if (event == XMLStreamConstants.START_ELEMENT) {
      currentElements.push(reader.getLocalName());
    }
    else if (event == XMLStreamConstants.END_ELEMENT) {
      currentElements.pop();
    }
  }

  private void parseContentUrl(final XMLStreamReader reader) {
    // We're looking for <content src="____"/>
    final String src = reader.getAttributeValue(null, "src");
    if (src != null) {
      contentUrls.add(src);
    }
  }

  private void parseNextPage(final XMLStreamReader reader) {
    // We're looking for <link re="next" href="____"/>
    final String rel = reader.getAttributeValue(null, "rel");
    if ("next".equals(rel)) {

      final String href = reader.getAttributeValue(null, "href");
      if (href != null) {
        nextUrl = href;
      }
    }
  }
}
