/*
* Copyright (c) 2021 PSPACE, inc. KSAN Development Team ksan@pspace.co.kr
* ifsmover is a suite of free software: you can redistribute it and/or modify it under the terms of
* the GNU General Public License as published by the Free Software Foundation, either version 
* 3 of the License.  See LICENSE for details
*
* 본 프로그램 및 관련 소스코드, 문서 등 모든 자료는 있는 그대로 제공이 됩니다.
* KSAN 프로젝트의 개발자 및 개발사는 이 프로그램을 사용한 결과에 따른 어떠한 책임도 지지 않습니다.
* KSAN 개발팀은 사전 공지, 허락, 동의 없이 KSAN 개발에 관련된 모든 결과물에 대한 LICENSE 방식을 변경 할 권리가 있습니다.
*/
package com.pspace.ifsmover.rest;

import java.net.MalformedURLException;
import java.net.URL;

import com.google.common.base.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S3Config {
    private static final Logger logger = LoggerFactory.getLogger(S3Config.class);
	private URL url;
	private String mountPoint;
	private String endPoint;
	private String accessKey;
	private String secretKey;
	private String bucket;
	private String prefix;
    private boolean isAWS;
    
    public S3Config(String mountPoint, String endPoint, String accessKey, String secretKey, String bucket, String prefix) {
		this.mountPoint = mountPoint;
        this.endPoint = endPoint;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.bucket = bucket;
        this.prefix = prefix;

        if (!Strings.isNullOrEmpty(endPoint)) {
            endPoint = endPoint.toLowerCase();
            if (endPoint.startsWith("http")) {
                isAWS = false;
				try {
					url = new URL(endPoint);
				} catch (MalformedURLException e) {
					logger.error(e.getMessage());
				}
            } else {
                isAWS = true;
            }
        } else {
            isAWS = false;
        }
    }

    public boolean isAWS() {
		return isAWS;
	}

	public String getMountPoint() {
		return mountPoint;
	}

	public String getEndPointProtocol() {
		if (url == null) {
			return null;
		}
		return url.getProtocol();
	}

	public String getEndPoint() {
		if (isAWS) {
			return endPoint;
		} else {
			if (url == null) {
				return null;
			}
			return url.getHost() + ":" + url.getPort();
		}
	}

	public String getAccessKey() {
		return accessKey;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public String getBucket() {
		return bucket;
	}

	public String getPrefix() {
		return prefix;
	}
}
