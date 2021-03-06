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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import com.google.common.base.Strings;
import com.pspace.ifsmover.rest.RestConfig;
import com.pspace.ifsmover.rest.Utils;
import com.pspace.ifsmover.rest.db.Sqlite;
import com.pspace.ifsmover.rest.PrintStack;
import com.pspace.ifsmover.rest.exception.ErrCode;
import com.pspace.ifsmover.rest.exception.RestException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class Stop extends MoverRequest {
    private static final Logger logger = LoggerFactory.getLogger(Status.class);
    private String userId;
    private String jobId;

    public Stop(HttpServletRequest request, HttpServletResponse response, String userId, String jobId) {
        super(request, response);
        this.userId = userId;
        this.jobId = jobId;
    }

    @Override
    public void process() throws RestException {
        // TODO Auto-generated method stub
        logger.info("Stop ...");
        logger.info("userId : {}, jobId : {}", userId, jobId);

        try {
            long matchId = Utils.getDBInstance().getUserMatchJob(userId, jobId);
            if (matchId == -1) {
                String returnJson = null;
                returnJson = "{\"Result\":\"failed\", \"Message\":\"Not exist userId and jobId\"}";
                response.getOutputStream().write(returnJson.getBytes());
                response.setStatus(HttpServletResponse.SC_OK);
                return;
            }

            String command = null;
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("win")) {
                command = "python ifs_mover -jobstop=" + jobId;
            } else {
                command = "./ifs_mover -jobstop=" + jobId;
            }

            File file = new File(RestConfig.getInstance().getPath());
            Process process = Runtime.getRuntime().exec(command, null, file);
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String result = reader.readLine();
            logger.info("result : {}", result);
            
            String returnJson = null;
            if (Strings.isNullOrEmpty(result)) {
                returnJson = "{\"Result\":\"success\", \"Message\":\"Stop success\"}";
            } else {
                returnJson = "{\"Result\":\"failed\", \"Message\":\"" + result + "\"}";
            }

            response.getOutputStream().write(returnJson.getBytes());
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            PrintStack.logging(logger, e);
            throw new RestException(ErrCode.INTERNAL_SERVER_ERROR);
        }
    }
}
