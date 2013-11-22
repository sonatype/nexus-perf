/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package com.sonatype.nexus.perftest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Nexus {
  private final String baseurl;

  private final String username;

  private final String password;

  public Nexus() {
    this.baseurl = System.getProperty("nexus.baseurl");
    this.username = System.getProperty("nexus.username");
    this.password = System.getProperty("nexus.password");
  }

  @JsonCreator
  public Nexus(@JsonProperty("baseurl") String baseurl, @JsonProperty("username") String username,
      @JsonProperty("password") String password) {
    this.baseurl = baseurl;
    this.username = username;
    this.password = password;
  }

  public String getBaseurl() {
    return baseurl;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }
}
