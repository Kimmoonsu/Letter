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
package com.kakao.network;

import android.util.Base64;

import com.kakao.network.multipart.MultipartRequestEntity;
import com.kakao.network.multipart.Part;
import com.kakao.util.helper.log.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * INetwork를 구현한 구현체.
 * google android에서 제공하는 HttpURLConnection을 기준으로 만들어졌다.
 * Kakao Api를 요청하기위한 하나의 Connection의 용도로 사용되며, Thread Safe 하지 않음.
 * @author leo.shin
 */
public class KakaoNetworkImpl implements INetwork {
    private HttpURLConnection urlConnection = null;
    private String charset = "ISO-8859-1";
    private final Set<Part> partSet = new HashSet<Part>();
    private final Map<String, String> params = new HashMap<String, String>();
    private final Map<String, String> header = new HashMap<String, String>();
    private static final int DEFAULT_CONNECTION_TO_IN_MS = 5000;
    private static final int DEFAULT_REQUEST_TO_IN_MS = 30 * 1000;

    private int statusCode = -1;

    /**
     * HttpUrlConnection 을 생성하며, property설정을 한다.
     * @param url 연결될 url.
     * @param method 연결 메소드
     * @throws IOException
     */
    @Override
    public void create(String url, String method, String charset) throws IOException {
        Logger.d("++ url : " + url);
        Logger.d("++ method : " + method);
        this.charset = charset;
        this.urlConnection = (HttpURLConnection) new URL(url).openConnection(Proxy.NO_PROXY);
        if (url.startsWith("https")) {

            HttpsURLConnection secure = (HttpsURLConnection) urlConnection;

            SSLContext sslContext = null;
            try {
                sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            } catch (GeneralSecurityException e) {
                throw new IOException(e.getMessage());
            }

            secure.setSSLSocketFactory(sslContext.getSocketFactory());
            secure.setHostnameVerifier(DO_NOT_VERIFY);
        }
        urlConnection.setRequestMethod(method);
    }

    @Override
    public void configure() throws IOException {
        urlConnection.setDoInput(true);
        urlConnection.setConnectTimeout(DEFAULT_CONNECTION_TO_IN_MS);
        urlConnection.setReadTimeout(DEFAULT_REQUEST_TO_IN_MS);
        urlConnection.setInstanceFollowRedirects(false);
        urlConnection.setRequestProperty("Connection", "keep-alive");

        if (header != null && header.size() > 0) {
            for(String key : header.keySet()) {
                urlConnection.setRequestProperty(key, header.get(key));
            }
        }

        String reqType = urlConnection.getRequestMethod();
        if ("POST".equals(reqType) || "PUT".equals(reqType)) {
            urlConnection.setRequestProperty("Content-Length", "0");
            urlConnection.setDoOutput(true);
            int contentLength = 0;
            String postParamString = "";
            MultipartRequestEntity mre = null;
            if (params != null && params.size() > 0) {
                postParamString = getPostDataString(params);
                contentLength += postParamString.length();
            } else  if (partSet.size() > 0) {
                mre = new MultipartRequestEntity(partSet);
                contentLength += mre.getContentLength();
                urlConnection.setRequestProperty("Content-Type", mre.getContentType());
            }

            if (contentLength > 0) {
                urlConnection.setFixedLengthStreamingMode(contentLength);
                urlConnection.setRequestProperty("Content-Length", String.valueOf(contentLength));
            }

            if (postParamString != null && postParamString.length() > 0) {
                urlConnection.getOutputStream().write(postParamString.getBytes(charset));
            }

            if (mre != null) {
                mre.writeRequest(urlConnection.getOutputStream());
            }
        }
    }

    @Override
    public void connect() throws IOException {
        try {
            statusCode = urlConnection.getResponseCode();
        } catch (IOException e) {
            statusCode = urlConnection.getResponseCode();
        }
    }

    @Override
    public void disconnect() {
        // todo variable clear
        params.clear();
        header.clear();
        partSet.clear();

        if (urlConnection != null) {
            urlConnection.disconnect();
        }
        statusCode = HttpURLConnection.HTTP_OK;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public void addHeader(String key, String value) {
        header.put(key, value);
    }

    @Override
    public void addParam(String key, String value) {
        params.put(key, value);
    }

    @Override
    public byte[] readFully() throws IOException {
        InputStream is = getInputStream(urlConnection);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] byteBuffer = new byte[1024];
            int nLength = 0;
            while((nLength = is.read(byteBuffer, 0, byteBuffer.length)) != -1) {
                baos.write(byteBuffer, 0, nLength);
            }
            return baos.toByteArray();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ignor) {
            }
        }
    }

    @Override
    public void addPart(Part part) {
        partSet.add(part);
    }

    final private static TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[] {};
        }

        @Override
        public void checkClientTrusted(
                java.security.cert.X509Certificate[] chain,
                String authType)
                throws java.security.cert.CertificateException {
        }

        @Override
        public void checkServerTrusted(
                java.security.cert.X509Certificate[] chain,
                String authType)
                throws java.security.cert.CertificateException {
        }
    }};

    final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    private String getPostDataString(Map<String, String> params) throws UnsupportedEncodingException{
        StringBuilder result = new StringBuilder();
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (result.length() > 0) {
                result.append("&");
            }

            result.append(URLEncoder.encode(entry.getKey(), charset));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), charset));
        }

        return result.toString();
    }

    private InputStream getInputStream(HttpURLConnection urlConnection) throws IOException {
        if (urlConnection.getResponseCode() < 400) {
            return urlConnection.getInputStream();
        } else {
            InputStream ein = urlConnection.getErrorStream();
            return (ein != null) ? ein : new ByteArrayInputStream(new byte[0]);
        }
    }
}
