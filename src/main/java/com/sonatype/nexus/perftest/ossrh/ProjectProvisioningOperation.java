/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package com.sonatype.nexus.perftest.ossrh;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.sonatype.nexus.perftest.AbstractNexusOperation;
import com.sonatype.nexus.perftest.ClientSwarm.ClientRequestInfo;
import com.sonatype.nexus.perftest.ClientSwarm.Operation;
import com.sonatype.nexus.perftest.Nexus;

public class ProjectProvisioningOperation extends AbstractNexusOperation implements Operation {

  public ProjectProvisioningOperation(Nexus nexus) {
    super(nexus);
  }

  @Override
  public void perform(ClientRequestInfo requestInfo) throws Exception {
    DefaultHttpClient httpclient = getHttpClient();

    StringBuilder url = new StringBuilder(nexusBaseurl);
    if (!nexusBaseurl.endsWith("/")) {
      url.append("/");
    }
    url.append("service/siesta/onboard");

    url.append("?users=").append("jvanzyl");
    url.append("&groupId=").append(String.format("test.nexustaging-%03d", requestInfo.getRequestId()));

    HttpPost request = new HttpPost(url.toString());

    HttpResponse response = httpclient.execute(request);

    String json = EntityUtils.toString(response.getEntity());

    if (!isSuccess(response)) {
      throw new IOException(request.toString() + " : " + response.getStatusLine().toString());
    }
  }
}
