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
package com.pspace.ifsmover.rest.api;

import java.io.IOException;

import com.pspace.ifsmover.rest.exception.RestException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public abstract class MoverRequest {
    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected String userId;
    protected String jobId;

    public MoverRequest(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    protected void setReturnJaonError(String errMsg) throws IOException {
        String returnJson = null;
        returnJson = "{\"Result\":\"failed\", \"Message\":\"" + errMsg + "\"}";
        response.getOutputStream().write(returnJson.getBytes());
        response.setStatus(HttpServletResponse.SC_OK);
    }

    public abstract void process() throws RestException;

    protected String getUserId() {
        return userId;
    }
    protected void setUserId(String userId) {
        this.userId = userId;
    }
    protected String getJobId() {
        return jobId;
    }
    protected void setJobId(String jobId) {
        this.jobId = jobId;
    }
}
