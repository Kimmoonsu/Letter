/**
 * Copyright 2014 Daum Kakao Corp.
 *
 * Redistribution and modification in source or binary forms are not permitted without specific prior written permission. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kakao.network.multipart;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Random;
import java.util.Set;

/**
 * @author leoshin, created at 15. 7. 31..
 */
public class MultipartRequestEntity {
    private static final String MULTIPART_FORM_CONTENT_TYPE = "multipart/form-data";
    private static final byte[] MULTIPART_CHARS = getAsciiBytes("-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");

    public static byte[] getAsciiBytes(String data) {
        try {
            return data.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException var2) {
            throw new RuntimeException(var2);
        }
    }

    public static byte[] getBytes(String data, String charset) {
        if(data == null) {
            throw new IllegalArgumentException("data may not be null");
        } else if(charset != null && charset.length() != 0) {
            try {
                return data.getBytes(charset);
            } catch (UnsupportedEncodingException var3) {
                throw new IllegalArgumentException(String.format("Unsupported encoding: %s", new Object[]{charset}));
            }
        } else {
            throw new IllegalArgumentException("charset may not be null or empty");
        }
    }

    public static byte[] generateMultipartBoundary() {
        Random rand = new Random();
        byte[] bytes = new byte[rand.nextInt(11) + 30]; // a random size from 30 to 40
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)];
        }
        return bytes;
    }

    private final Set<Part> partSet;

    private final byte[] multipartBoundary;

    private final String contentType;

    private final long contentLength;

    /**
     * Creates a new multipart entity containing the given parts.
     * @param partSet The parts to include.
     */
    public MultipartRequestEntity(Set<Part> partSet) {
        this.partSet = partSet;
        this.multipartBoundary = generateMultipartBoundary();
        this.contentType = computeContentType(MULTIPART_FORM_CONTENT_TYPE);
        this.contentLength = Part.getLengthOfParts(partSet, multipartBoundary);
    }

    private String computeContentType(String base) {
        StringBuilder buffer = new StringBuilder(base);
        if (!base.endsWith(";")) {
            buffer.append(";");
        }
        try {
            return buffer.append(" boundary=").append(new String(multipartBoundary, "US-ASCII")).toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the MIME boundary string that is used to demarcate boundaries of this part.
     *
     * @return The boundary string of this entity in ASCII encoding.
     */
    public byte[] getMultipartBoundary() {
        return multipartBoundary;
    }

    public void writeRequest(OutputStream out) throws IOException {
        Part.sendParts(out, partSet, multipartBoundary);
    }

    public long getContentLength() {
        return contentLength;
    }

    public String getContentType() {
        return contentType;
    }
}
