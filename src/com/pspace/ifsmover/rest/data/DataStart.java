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
