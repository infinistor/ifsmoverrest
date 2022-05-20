package com.pspace.ifsmover.rest.data;

import java.io.InputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pspace.ifsmover.rest.PrintStack;
import com.pspace.ifsmover.rest.data.format.JsonCheck;
import com.pspace.ifsmover.rest.exception.ErrCode;
import com.pspace.ifsmover.rest.exception.RestException;

import org.slf4j.LoggerFactory;

public class DataCheck extends DataRequest {
    private String jsonData;
    private JsonCheck jsonCheck = null;

    public DataCheck(InputStream inputStream) {
        super(inputStream);
        logger = LoggerFactory.getLogger(DataCheck.class);
    }

    @Override
    public void extract() throws RestException {
        // TODO Auto-generated method stub
        jsonData = readJson();
        ObjectMapper jsonMapper = new ObjectMapper();
        try {
            jsonCheck = jsonMapper.readValue(jsonData, JsonCheck.class);
        } catch (JsonMappingException e) {
            PrintStack.logging(logger, e);
            throw new RestException(ErrCode.INTERNAL_SERVER_ERROR);
        } catch (JsonProcessingException e) {
            PrintStack.logging(logger, e);
            throw new RestException(ErrCode.INTERNAL_SERVER_ERROR);
        }
    }
    
    public JsonCheck getJsonCheck() {
        return jsonCheck;
    }
}
