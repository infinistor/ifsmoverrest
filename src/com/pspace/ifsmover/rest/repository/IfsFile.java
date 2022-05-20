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
package com.pspace.ifsmover.rest.repository;

import java.io.File;

import com.google.common.base.Strings;
import com.pspace.ifsmover.rest.S3Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IfsFile implements Repository {
    private static final Logger logger = LoggerFactory.getLogger(IfsFile.class);
    private S3Config config;
    private boolean isSource;
	private String errCode;
	private String errMessage;
    private String path;

    @Override
    public String getErrCode() {
        return errCode;
    }

    @Override
    public String getErrMessage() {
        return errMessage;
    }

    @Override
    public void setConfig(S3Config config, boolean isSource) {
        this.config = config;
        this.isSource = isSource;
    }

    @Override
    public int check() {
        if (Strings.isNullOrEmpty(config.getPrefix())) {
            path = config.getMountPoint();
        } else {
            path = config.getMountPoint() + config.getPrefix();
        }
        
			
        File dir = new File(path);
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                errCode = "";
                errMessage = "mountpoint(" + path + ") is not directory.";
                return FILE_PATH_NOT_DIR;
            }
        } else {
            errCode = "";
            errMessage = "mountpoint is not exist. - " + path;
            return FILE_PATH_NOT_EXIST;
        }
        return NO_ERROR;
    }
    
}
