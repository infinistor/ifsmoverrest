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

import com.google.common.base.Strings;
import com.pspace.ifsmover.rest.PrintStack;
import com.pspace.ifsmover.rest.exception.ErrCode;
import com.pspace.ifsmover.rest.exception.RestException;

import org.slf4j.Logger;

public abstract class DataRequest {
    protected InputStream inputStream;
    protected Logger logger;

    public DataRequest(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    protected String readJson() throws RestException {
		String ret = null;

		try {
			byte[] json = inputStream.readAllBytes();
			ret = new String(json);
		} catch (IOException e) {
			PrintStack.logging(logger, e);
			throw new RestException(ErrCode.INTERNAL_SERVER_ERROR);
		}

		logger.info(ret);
		
		if (Strings.isNullOrEmpty(ret)) {
			logger.warn(ErrCode.INVALID_ARGUMENT.getMessage());
			throw new RestException(ErrCode.INVALID_ARGUMENT);
		}
		
		return ret;
	}

    public abstract void extract() throws RestException;
}
