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
package com.pspace.ifsmover.rest.exception;

import static java.util.Objects.requireNonNull;
import com.google.common.base.CaseFormat;
import jakarta.servlet.http.HttpServletResponse;

public enum ErrCode {
    SERVER_ERROR(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error"),
    BAD_REQUEST(HttpServletResponse.SC_BAD_REQUEST, "Bad Request"),
    INVALID_ARGUMENT(HttpServletResponse.SC_BAD_REQUEST, "Bad Request"),
    NOT_IMPLEMENTED(HttpServletResponse.SC_NOT_IMPLEMENTED,
            "A header you provided implies functionality that is not" +
            " implemented."),
    INTERNAL_SERVER_DB_ERROR(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "database error has occurred"),
    INTERNAL_SERVER_ERROR(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "internal server error has occurred");

    private final String errorCode;
    private final int httpStatusCode;
    private final String message;

    ErrCode(int httpStatusCode, String message) {
        this.errorCode = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name());
        this.httpStatusCode = httpStatusCode;
        this.message = requireNonNull(message);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return getHttpStatusCode() + " " + getErrorCode() + " " + getMessage();
    }
}
