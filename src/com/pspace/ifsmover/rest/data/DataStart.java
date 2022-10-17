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

import java.io.InputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pspace.ifsmover.rest.PrintStack;
import com.pspace.ifsmover.rest.data.format.JsonStart;
import com.pspace.ifsmover.rest.exception.ErrCode;
import com.pspace.ifsmover.rest.exception.RestException;

import org.slf4j.LoggerFactory;

public class DataStart extends DataRequest {
    private String jsonData;
    private JsonStart jsonStart = null;

    public DataStart(InputStream inputStream) {
        super(inputStream);
        logger = LoggerFactory.getLogger(DataStart.class);
    }

    @Override
    public void extract() throws RestException {
        // TODO Auto-generated method stub
        jsonData = readJson();
        ObjectMapper jsonMapper = new ObjectMapper();
        try {
            jsonStart = jsonMapper.readValue(jsonData, JsonStart.class);
        } catch (JsonMappingException e) {
            PrintStack.logging(logger, e);
            throw new RestException(ErrCode.INTERNAL_SERVER_ERROR);
        } catch (JsonProcessingException e) {
            PrintStack.logging(logger, e);
            throw new RestException(ErrCode.INTERNAL_SERVER_ERROR);
        }
    }
    
    public JsonStart getJsonStart() {
        return jsonStart;
    }
}
