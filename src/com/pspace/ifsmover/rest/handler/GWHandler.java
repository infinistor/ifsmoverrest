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
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.google.common.base.Strings;
import com.pspace.ifsmover.rest.api.Check;
import com.pspace.ifsmover.rest.api.MoverRequest;
import com.pspace.ifsmover.rest.api.Remove;
import com.pspace.ifsmover.rest.api.Rerun;
import com.pspace.ifsmover.rest.api.Start;
import com.pspace.ifsmover.rest.api.Status;
import com.pspace.ifsmover.rest.api.Stop;
import com.pspace.ifsmover.rest.exception.ErrCode;
import com.pspace.ifsmover.rest.exception.RestException;

import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class GWHandler {
    private static final Logger logger = LoggerFactory.getLogger(GWHandler.class);
	private String moverPath;
    private final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
    
    public final void doHandle(Request baseRequest, HttpServletRequest request, HttpServletResponse response, InputStream is) throws RestException {
		String method = request.getMethod();
		long startTime = System.currentTimeMillis();

		String uri = request.getRequestURI();

		logger.info("request url : {}", request.getRequestURL());
		logger.info("PREURI - {}", uri);
		logger.info("client address - {}", request.getRemoteAddr());
		logger.info("client host - {}", request.getRemoteHost());
		logger.info("method - {}", method);

		for (String parameter : Collections.list(request.getParameterNames())) {
			logger.info("parameter {} - {}", parameter, Strings.nullToEmpty(request.getParameter(parameter)));
		}
		for (String headerName : Collections.list(request.getHeaderNames())) {
			for (String headerValue : Collections.list(request.getHeaders(headerName))) {
				logger.info("header {} - {}", headerName, Strings.nullToEmpty(headerValue));
			}
		}

		String[] path = uri.split("/");
		try {
			for (int i = 0; i < path.length; i++) {
				path[i] = URLDecoder.decode(path[i], "UTF-8");
				logger.info("path[{}] : {}", i, path[i]);
			}
		} catch (UnsupportedEncodingException e) {
			throw new RestException(ErrCode.BAD_REQUEST);
		}

		MoverRequest moverRequest = null;

		if (!path[1].equalsIgnoreCase("api")) {
			throw new RestException(ErrCode.BAD_REQUEST);
		} else {
			if (path[2].equalsIgnoreCase("Check")) {
				moverRequest = new Check(request, response);
			} else if (path[2].equalsIgnoreCase("Start")) {
				moverRequest = new Start(request, response);
			} else if (path[2].equalsIgnoreCase("Stop")) {
				if (path.length == 5) {
					String userId = path[3];
					String jobId =  path[4];
					moverRequest = new Stop(request, response, userId, jobId);
				} else {
					throw new RestException(ErrCode.BAD_REQUEST);
				}
			} else if (path[2].equalsIgnoreCase("Rerun")) {
				if (path.length == 5) {
					String userId = path[3];
					String jobId =  path[4];
					moverRequest = new Rerun(request, response, userId, jobId);
				} else {
					throw new RestException(ErrCode.BAD_REQUEST);
				}
			} else if (path[2].equalsIgnoreCase("Status")) {
				if (path.length == 4) {
					String userId =  path[3];
					moverRequest = new Status(request, response, userId);
				} else {
					throw new RestException(ErrCode.BAD_REQUEST);
				}
			} else if (path[2].equalsIgnoreCase("Remove")) {
				if (path.length == 5) {
					String userId = path[3];
					String jobId =  path[4];
					moverRequest = new Remove(request, response, userId, jobId);
				} else {
					throw new RestException(ErrCode.BAD_REQUEST);
				}
			} else {
				throw new RestException(ErrCode.BAD_REQUEST);
			}
		}

		moverRequest.process();
    }

    protected final void sendSimpleErrorResponse(
			HttpServletRequest request, HttpServletResponse response,
			ErrCode code, String message,
			Map<String, String> elements) throws IOException {

		response.setStatus(code.getHttpStatusCode());

		if (request.getMethod().equals("HEAD")) {
			// The HEAD method is identical to GET except that the server MUST
			// NOT return a message-body in the response.
			return;
		}

		try (Writer writer = response.getWriter()) {
			response.setContentType("application/xml");
			XMLStreamWriter xml = xmlOutputFactory.createXMLStreamWriter(writer);
			xml.writeStartDocument();
			xml.writeStartElement("Error");

			writeSimpleElement(xml, "Code", code.getErrorCode());
			writeSimpleElement(xml, "Message", message);

			for (Map.Entry<String, String> entry : elements.entrySet()) {
				writeSimpleElement(xml, entry.getKey(), entry.getValue());
			}

			writeSimpleElement(xml, "RequestId", "4442587FB7D0A2F9");

			xml.writeEndElement();
			xml.flush();
		} catch (XMLStreamException xse) {
			throw new IOException(xse);
		}
	}

    private void writeSimpleElement(XMLStreamWriter xml, String elementName, String characters) throws XMLStreamException {
		xml.writeStartElement(elementName);
		xml.writeCharacters(characters);
		xml.writeEndElement();
	}

	public void setMoverPath(String moverPath) {
		this.moverPath = moverPath;
	}

	public String getMoverPath() {
		return moverPath;
	}
}
