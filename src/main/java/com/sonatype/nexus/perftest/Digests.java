/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package com.sonatype.nexus.perftest;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpEntity;

public class Digests {
  public static String getDigest(HttpEntity entity, String algorithm) throws IOException {
    try (InputStream is = entity.getContent()) {
      byte[] buffer = new byte[1024];

      MessageDigest md = MessageDigest.getInstance(algorithm);

      int numRead;

      do {
        numRead = is.read(buffer);

        if (numRead > 0) {
          md.update(buffer, 0, numRead);
        }
      } while (numRead != -1);

      return new String(encodeHex(md.digest()));
    } catch (NoSuchAlgorithmException e) {
      throw new IOException(e);
    }
  }

  private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

  public static char[] encodeHex(byte[] data) {
    int l = data.length;

    char[] out = new char[l << 1];

    // two characters form the hex value.
    for (int i = 0, j = 0; i < l; i++) {
      out[j++] = DIGITS[(0xF0 & data[i]) >>> 4];
      out[j++] = DIGITS[0x0F & data[i]];
    }

    return out;
  }

}
