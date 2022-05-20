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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import com.pspace.ifsmover.rest.RestConfig;
import com.pspace.ifsmover.rest.S3Config;
import com.pspace.ifsmover.rest.PrintStack;
import com.pspace.ifsmover.rest.data.DataCheck;
import com.pspace.ifsmover.rest.data.format.JsonCheck;
import com.pspace.ifsmover.rest.exception.ErrCode;
import com.pspace.ifsmover.rest.exception.RestException;
import com.pspace.ifsmover.rest.repository.IfsS3;
import com.pspace.ifsmover.rest.repository.Repository;
import com.pspace.ifsmover.rest.repository.RepositoryFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class Check extends MoverRequest {
    private static final Logger logger = LoggerFactory.getLogger(Start.class);
    private JsonCheck jsonCheck = null;
    
    public Check(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
    }

    @Override
    public void process() throws RestException {
        // TODO Auto-generated method stub
        logger.info("Check ...");
        try {
            DataCheck dataCheck = new DataCheck(request.getInputStream());

            try {
                dataCheck.extract();
            } catch (Exception e) {
                throw new RestException(ErrCode.BAD_REQUEST);
            }
            
            jsonCheck = dataCheck.getJsonCheck();
    
            if (jsonCheck.getType().equalsIgnoreCase(Repository.SWIFT)) {
                throw new RestException(ErrCode.NOT_IMPLEMENTED);
            }

            // Check
            S3Config config = null;
            config = new S3Config(jsonCheck.getSource().getMountPoint(),
                                  jsonCheck.getSource().getEndPoint(), 
                                  jsonCheck.getSource().getAccess(), 
                                  jsonCheck.getSource().getSecret(), 
                                  jsonCheck.getSource().getBucket(), 
                                  jsonCheck.getSource().getPrefix());

            RepositoryFactory factory = new RepositoryFactory();
            Repository sourceRepository = factory.getSourceRepository(jsonCheck.getType());
            sourceRepository.setConfig(config, true);
            int result = sourceRepository.check();
            if (result != 0) {
                setReturnJaonError(sourceRepository.getErrMessage(), false);
                return;
            }

            config = new S3Config(null,
                                  jsonCheck.getTarget().getEndPoint(), 
                                  jsonCheck.getTarget().getAccess(), 
                                  jsonCheck.getTarget().getSecret(), 
                                  jsonCheck.getTarget().getBucket(), 
                                  jsonCheck.getTarget().getPrefix());
            IfsS3 ifsS3 = new IfsS3();
            ifsS3.setConfig(config, false);
            result = ifsS3.check();
            if (result != 0) {
                setReturnJaonError(ifsS3.getErrMessage(), false);
                return;
            }

            String returnJson = "{\"Result\":\"success\", \"Message\":\"Check success.\"}";
            response.getOutputStream().write(returnJson.getBytes());
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (IOException e) {
            PrintStack.logging(logger, e);
            throw new RestException(ErrCode.INTERNAL_SERVER_ERROR);
        }
    }
}
