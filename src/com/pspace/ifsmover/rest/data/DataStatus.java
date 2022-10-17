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
package com.pspace.ifsmover.rest.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import com.google.common.base.Strings;
import com.pspace.ifsmover.rest.exception.RestException;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.LoggerFactory;

public class DataStatus extends DataRequest {
    HttpServletRequest request;
    private String jobId;
    private String srcBucket;
    private String dstBucket;
    public static final String JOBID = "jobid";
    public static final String SRC_BUCKET = "srcbucket";
    public static final String DST_BUCKET = "dstbucket";

    public DataStatus(InputStream inputStream) {
        super(inputStream);
        logger = LoggerFactory.getLogger(DataStatus.class);
    }

    public DataStatus(HttpServletRequest request) throws IOException {
        super(request.getInputStream());
        this.request = request;
        logger = LoggerFactory.getLogger(DataStatus.class);
        jobId = "";
        srcBucket = "";
        dstBucket = "";
    }

    @Override
    public void extract() throws RestException {
        for (String parameter : Collections.list(request.getParameterNames())) {
            logger.debug("parameter : {}", parameter);
            String param = parameter.toLowerCase();
            if (param.equals(JOBID)) {
                jobId = request.getParameter(parameter);
            } else if (param.equals(SRC_BUCKET)) {
                srcBucket = request.getParameter(parameter);
            } else if (param.equals(DST_BUCKET)) {
                dstBucket = request.getParameter(parameter);
            }
		}
    }
    
    public String getJobId() {
        return jobId;
    }

    public String getSrcBucket() {
        return srcBucket;
    }

    public String getDstBucket() {
        return dstBucket;
    }
}
