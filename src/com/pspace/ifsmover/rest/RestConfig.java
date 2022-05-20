/*
* Copyright (c) 2021 PSPACE, inc. KSAN Development Team ksan@pspace.co.kr
* KSAN is a suite of free software: you can redistribute it and/or modify it under the terms of
* the GNU General Public License as published by the Free Software Foundation, either version 
* 3 of the License.  See LICENSE for details
*
* 본 프로그램 및 관련 소스코드, 문서 등 모든 자료는 있는 그대로 제공이 됩니다.
* KSAN 프로젝트의 개발자 및 개발사는 이 프로그램을 사용한 결과에 따른 어떠한 책임도 지지 않습니다.
* KSAN 개발팀은 사전 공지, 허락, 동의 없이 KSAN 개발에 관련된 모든 결과물에 대한 LICENSE 방식을 변경 할 권리가 있습니다.
*/
package com.pspace.ifsmover.rest;

import static java.util.Objects.requireNonNull;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import com.google.common.base.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestConfig {
    private Properties properties;
    private static final Logger logger = LoggerFactory.getLogger(RestConfig.class);
    private URI endpoint;
	private URI secureEndpoint;
    private String keystorePath;
    private String keystorePassword;
    private long jettyMaxIdleTimeout;
    private String ifsmoverPath;
    
    public static RestConfig getInstance() {
        return LazyHolder.INSTANCE;
    }

    private static class LazyHolder {
        private static RestConfig INSTANCE = new RestConfig();
    }

    private RestConfig() {
        String path = System.getProperty("configure");
		if (path == null) {
			path = "./etc/ifsmoverRest.conf";
		}

        properties = new Properties();
        try (InputStream myis = new FileInputStream(path)) {
            properties.load(myis);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Properties file is not exist.");
        } catch (IOException e) {
            throw new IllegalArgumentException("Properties file load is failed.");
        }

        try {
            configure();
        } catch (URISyntaxException e) {
            PrintStack.logging(logger, e);
            throw new IllegalArgumentException("Properties file load is fail");
        }
    }

    public void configure() throws URISyntaxException {
        String endpoint = properties.getProperty("endpoint");
		String secureEndpoint = properties.getProperty("secure-endpoint");
		if (Strings.isNullOrEmpty(endpoint) && Strings.isNullOrEmpty(secureEndpoint)) {
			throw new IllegalArgumentException(
					"Properties file must contain: " +
							"endpoint" + " or " +
							"secure-endpoint");
		}

		if (!Strings.isNullOrEmpty(endpoint)) {
			this.endpoint = requireNonNull(new URI(endpoint));
		}

		if (!Strings.isNullOrEmpty(secureEndpoint)) {
			this.secureEndpoint = requireNonNull(new URI(secureEndpoint));
		}

        this.keystorePath = properties.getProperty("keystore-path");
		this.keystorePassword = properties.getProperty("keystore-password");

        String strJettyMaxIdleTimeout = properties.getProperty("jettyMaxIdleTimeout");
        if (strJettyMaxIdleTimeout == null) {
            jettyMaxIdleTimeout = 30000;
        } else {
            jettyMaxIdleTimeout = Long.parseLong(strJettyMaxIdleTimeout);
        }

        ifsmoverPath = properties.getProperty("ifsmover-path");
        if (Strings.isNullOrEmpty(ifsmoverPath)) {
            throw new IllegalArgumentException("Configuration file ifsmover-path is not exist.");
        }
    }

    public URI getEndpoint() {
		return this.endpoint;
	}
	
	public URI getSecureEndpoint() {
		return this.secureEndpoint;
	}

    public String getKeystorePath() {
        if (Strings.isNullOrEmpty(keystorePath)) {
            return null;
        }
        return this.keystorePath;
    }

    public String getKeystorePassword() {
        if (Strings.isNullOrEmpty(keystorePassword)) {
            return null;
        }
        return this.keystorePassword;
    }

    public long getJettyMaxIdleTimeout() {
        return jettyMaxIdleTimeout;
    }

    public String getPath() {
        return ifsmoverPath;
    }
}
