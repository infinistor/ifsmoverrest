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
package com.pspace.ifsmover.rest.handler;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.pspace.ifsmover.rest.RestConfig;
import com.pspace.ifsmover.rest.Utils;

import org.eclipse.jetty.http.HttpCompliance;
import org.eclipse.jetty.http.UriCompliance;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.ProxyConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GW {
    private Server server;
    private GWHandlerJetty handler;
    private static final Logger logger = LoggerFactory.getLogger(GW.class);

    public void init() {
        checkArgument(RestConfig.getInstance().getEndpoint() != null || RestConfig.getInstance().getSecureEndpoint() != null,
				"Must provide endpoint or secure-endpoint");
		
		if (RestConfig.getInstance().getEndpoint() != null) {
			checkArgument(RestConfig.getInstance().getEndpoint().getPath().isEmpty(),
					"endpoint path must be empty, was: %s",	RestConfig.getInstance().getEndpoint().getPath());
		}
		
		if (RestConfig.getInstance().getSecureEndpoint() != null) {
			checkArgument(RestConfig.getInstance().getSecureEndpoint().getPath().isEmpty(),
					"secure-endpoint path must be empty, was: %s",
					RestConfig.getInstance().getSecureEndpoint().getPath());
			requireNonNull(RestConfig.getInstance().getKeystorePath(), "Must provide keyStorePath with HTTPS endpoint");
			requireNonNull(RestConfig.getInstance().getKeystorePassword(), "Must provide keyStorePassword with HTTPS endpoint");
            logger.info("keystorePath: {}", RestConfig.getInstance().getKeystorePath());
            logger.info("keystorePassword: {}", RestConfig.getInstance().getKeystorePassword());
            if (RestConfig.getInstance().getKeystorePassword() == null) {
                logger.info("null");
            }
		}

        ExecutorThreadPool pool = new ExecutorThreadPool((int)10);
        pool.setName("ifsmoverREST");
        server = new Server(pool);

        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setSendServerVersion(false);

        HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(httpConfig);
		HttpCompliance customHttpCompliance = HttpCompliance.from("RFC7230,MULTIPLE_CONTENT_LENGTHS");
		httpConnectionFactory.getHttpConfiguration().setHttpCompliance(customHttpCompliance);
		UriCompliance customUriCompliance = UriCompliance.from("RFC3986,-AMBIGUOUS_PATH_SEPARATOR");
		httpConnectionFactory.getHttpConfiguration().setUriCompliance(customUriCompliance);

		ServerConnector connector;
        if (RestConfig.getInstance().getEndpoint() != null) {
            ProxyConnectionFactory httpProxyConnectionFactory = new ProxyConnectionFactory(httpConnectionFactory.getProtocol());
            connector = new ServerConnector(server, httpProxyConnectionFactory, httpConnectionFactory);
            connector.setHost(RestConfig.getInstance().getEndpoint().getHost());
            connector.setPort(RestConfig.getInstance().getEndpoint().getPort());
            connector.setIdleTimeout(RestConfig.getInstance().getJettyMaxIdleTimeout());
            connector.setReuseAddress(true);
            server.addConnector(connector);
        }
        
        if (RestConfig.getInstance().getSecureEndpoint() != null) {
            SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
            sslContextFactory.setKeyStorePath(RestConfig.getInstance().getKeystorePath());
            sslContextFactory.setKeyStorePassword(RestConfig.getInstance().getKeystorePassword());
            connector = new ServerConnector(server, sslContextFactory, httpConnectionFactory);
            connector.setHost(RestConfig.getInstance().getSecureEndpoint().getHost());
            connector.setPort(RestConfig.getInstance().getSecureEndpoint().getPort());
            connector.setIdleTimeout(RestConfig.getInstance().getJettyMaxIdleTimeout());
            connector.setReuseAddress(true);
            server.addConnector(connector);
        }

        handler = new GWHandlerJetty();
        server.setHandler(handler);
        
        try {
            Utils.getDBInstance().init(RestConfig.getInstance().getDbHost(),
                RestConfig.getInstance().getDbPort(), 
                RestConfig.getInstance().getDatabase(),
                RestConfig.getInstance().getDbUser(),
                RestConfig.getInstance().getDbPass(),
                RestConfig.getInstance().getDbPoolSize());
        } catch (Exception e) {
            logger.error(e.getMessage());
            System.exit(-1);
        }
    }

    public void start() throws Exception {
        server.start();
    }

    public void join() throws Exception {
        server.join();
    }

    public void stop() throws Exception {
        server.stop();
    }
}
