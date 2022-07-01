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
    private static final String SYSTEM_CONFIG_KEY = "configure";
    private static final String CONFIG_FILE = "./etc/ifsmoverRest.conf";
    private static final String ENDPOINT = "endpoint";
    private static final String SECURE_ENDPOINT = "secure-endpoint";
    private static final String KEYSTORE_PATH = "keystore-path";
    private static final String KEYSTORE_PASSWORD = "keystore-password";
    private static final String JETTY_MAX_IDLE_TIMEOUT = "jettyMaxIdleTimeout";
    private static final String IFSMOVER_PATH = "ifsmover-path";
    private static final String DB_REPOSITORY = "db_repository";
    private static final String DB_HOST = "db_host";
    private static final String DB_NAME = "db_name";
    private static final String DB_PORT = "db_port";
    private static final String DB_USER = "db_user";
    private static final String DB_PASSWORD = "db_password";
    private static final String DB_POOL_SIZE = "db_pool_size";
    public static final String MARIADB = "mariadb";
    public static final String SQLITEDB = "sqlite";

    private static final String LOG_CONFIG_MUST_CONTAIN = "config file must contain: ";

    private Properties properties;
    private static final Logger logger = LoggerFactory.getLogger(RestConfig.class);
    private URI endpoint;
	private URI secureEndpoint;
    private String keystorePath;
    private String keystorePassword;
    private long jettyMaxIdleTimeout;
    private String ifsmoverPath;
    private static String dbRepository;
    private String dbHost;
	private String database;
	private String dbPort;
	private String dbUser;
	private String dbPass;
	private int dbPoolSize;
    
    public static RestConfig getInstance() {
        return LazyHolder.INSTANCE;
    }

    private static class LazyHolder {
        private static RestConfig INSTANCE = new RestConfig();
    }

    private RestConfig() {
        String path = System.getProperty(SYSTEM_CONFIG_KEY);
		if (path == null) {
			path = CONFIG_FILE;
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
        String endpoint = properties.getProperty(ENDPOINT);
		String secureEndpoint = properties.getProperty(SECURE_ENDPOINT);
		if (Strings.isNullOrEmpty(endpoint) && Strings.isNullOrEmpty(secureEndpoint)) {
			throw new IllegalArgumentException(
					"Properties file must contain: " +
                    ENDPOINT + " or " +
                    SECURE_ENDPOINT);
		}

		if (!Strings.isNullOrEmpty(endpoint)) {
			this.endpoint = requireNonNull(new URI(endpoint));
		}

		if (!Strings.isNullOrEmpty(secureEndpoint)) {
			this.secureEndpoint = requireNonNull(new URI(secureEndpoint));
		}

        this.keystorePath = properties.getProperty(KEYSTORE_PATH);
		this.keystorePassword = properties.getProperty(KEYSTORE_PASSWORD);

        String strJettyMaxIdleTimeout = properties.getProperty(JETTY_MAX_IDLE_TIMEOUT);
        if (strJettyMaxIdleTimeout == null) {
            jettyMaxIdleTimeout = 30000;
        } else {
            jettyMaxIdleTimeout = Long.parseLong(strJettyMaxIdleTimeout);
        }

        ifsmoverPath = properties.getProperty(IFSMOVER_PATH);
        if (Strings.isNullOrEmpty(ifsmoverPath)) {
            throw new IllegalArgumentException("Configuration file ifsmover-path is not exist.");
        }

        dbRepository = properties.getProperty(DB_REPOSITORY);
        dbHost = properties.getProperty(DB_HOST);
        if (dbRepository == null) {
            if (dbHost == null) {
                throw new IllegalArgumentException(LOG_CONFIG_MUST_CONTAIN + DB_HOST);
            }
        } else {
            if (dbRepository.equalsIgnoreCase(MARIADB)) {
                if (dbHost == null) {
                    throw new IllegalArgumentException(LOG_CONFIG_MUST_CONTAIN + DB_HOST);
                }
            }
        }

        database = properties.getProperty(DB_NAME);
        if (dbRepository == null) {
            if (database == null) {
                throw new IllegalArgumentException(LOG_CONFIG_MUST_CONTAIN + DB_NAME);
            }
        } else {
            if (dbRepository.equalsIgnoreCase(MARIADB)) {
                if (database == null) {
                    throw new IllegalArgumentException(LOG_CONFIG_MUST_CONTAIN + DB_NAME);
                }
            }
        }

        dbPort = properties.getProperty(DB_PORT);
        if (dbRepository == null) {
            if (dbPort == null) {
                throw new IllegalArgumentException(LOG_CONFIG_MUST_CONTAIN + DB_PORT);
            }
        } else {
            if (dbRepository.equalsIgnoreCase(MARIADB)) {
                if (dbPort == null) {
                    throw new IllegalArgumentException(LOG_CONFIG_MUST_CONTAIN + DB_PORT);
                }
            }
        }

        dbUser = properties.getProperty(DB_USER);
        if (dbRepository == null) {
            if (dbUser == null) {
                throw new IllegalArgumentException(LOG_CONFIG_MUST_CONTAIN + DB_USER);
            }
        } else {
            if (dbRepository.equalsIgnoreCase(MARIADB)) {
                if (dbUser == null) {
                    throw new IllegalArgumentException(LOG_CONFIG_MUST_CONTAIN + DB_USER);
                }
            }
        }

        dbPass = properties.getProperty(DB_PASSWORD);
        if (dbRepository == null) {
            if (dbPass == null) {
                throw new IllegalArgumentException(LOG_CONFIG_MUST_CONTAIN + DB_PASSWORD);
            }
        } else {
            if (dbRepository.equalsIgnoreCase(MARIADB)) {
                if (dbPass == null) {
                    throw new IllegalArgumentException(LOG_CONFIG_MUST_CONTAIN + DB_PASSWORD);
                }
            }
        }

        String dbPoolSize = properties.getProperty(DB_POOL_SIZE);
        if (dbRepository == null) {
            if (dbPoolSize == null) {
                throw new IllegalArgumentException(LOG_CONFIG_MUST_CONTAIN + DB_POOL_SIZE);
            } else {
                this.dbPoolSize = Integer.parseInt(dbPoolSize);
            }
        } else {
            if (dbRepository.equalsIgnoreCase(MARIADB)) {
                if (dbPoolSize == null) {
                    throw new IllegalArgumentException(LOG_CONFIG_MUST_CONTAIN + DB_POOL_SIZE);
                } else {
                    this.dbPoolSize = Integer.parseInt(dbPoolSize);
                }
            }
        }

        if (dbRepository == null) {
            dbRepository = MARIADB;
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

    public static String getDBRepository() {
        return dbRepository;
    }

    public String getDbHost() {
        return dbHost;
    }

    public String getDatabase() {
        return database;
    }

    public String getDbPort() {
        return dbPort;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPass() {
        return dbPass;
    }

    public int getDbPoolSize() {
        return dbPoolSize;
    }
}
