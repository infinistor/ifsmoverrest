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
package com.pspace.ifsmover.rest.handler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import com.pspace.ifsmover.rest.exception.RestException;

import org.apache.http.client.utils.URLEncodedUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.MultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class GWHandlerJetty extends AbstractHandler {
    private static final Logger logger = LoggerFactory.getLogger(GWHandlerJetty.class);
    private final GWHandler handler;

    public GWHandlerJetty() {
        handler = new GWHandler();
    }
    
    private void sendS3Exception(HttpServletRequest request, HttpServletResponse response, RestException e)
			throws IOException {
		handler.sendSimpleErrorResponse(request, response, e.getError(), e.getMessage(), e.getElements());
	}

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        // TODO Auto-generated method stub
        try (InputStream is = request.getInputStream()) {
            logger.info(baseRequest.getRootURL() + baseRequest.getOriginalURI());
            List<NameValuePair> params = URLEncodedUtils.parse(baseRequest.getHttpURI().toURI(), Charset.forName("UTF-8"));

            MultiMap<String> queryParameters = new MultiMap<String>();
			for (NameValuePair param : params) {
				logger.info(param.getName() + " : " + param.getValue());

				String encodevalue = "";
				if(param.getValue() != null) {
					if(param.getName().equals("Signature"))
						encodevalue = param.getValue().replaceAll(" ", "+");
					else
						encodevalue = param.getValue();
				}

				queryParameters.put(param.getName(), encodevalue);
			}
            baseRequest.setQueryParameters(queryParameters);
            handler.doHandle(baseRequest, request, response, is);
			baseRequest.setHandled(true);
        } catch (RestException e) {
            sendS3Exception(request, response, e);
			baseRequest.setHandled(true);
        }
    }
    
    public GWHandler getHandler() {
        return this.handler;
    }
}
