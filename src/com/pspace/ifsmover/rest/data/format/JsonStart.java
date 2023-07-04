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
package com.pspace.ifsmover.rest.data.format;

import com.fasterxml.jackson.annotation.JsonProperty;

import jnr.ffi.Struct.pid_t;

public class JsonStart {
    @JsonProperty("UserId")
	private String userId;

    @JsonProperty("Type")
	private String type;

	@JsonProperty("Source")
    private Source source;

    public static final class Source {
        @JsonProperty("Mountpoint")
		private String mountPoint;

        @JsonProperty("Endpoint")
        private String endPoint;

        @JsonProperty("Access")
        private String access;

        @JsonProperty("Secret")
        private String secret;

        @JsonProperty("Bucket")
        private String bucket;

        @JsonProperty("Prefix")
        private String prefix;

        @JsonProperty("Move_size")
        private String moveSize;

        public String getMountPoint() {
            return mountPoint;
        }

        public void setMountPoint(String mountPoint) {
            this.mountPoint = mountPoint;
        }

        public String getEndPoint() {
            return endPoint;
        }

        public void setEndPoint(String endPoint) {
            this.endPoint = endPoint;
        }

        public String getAccess() {
            return access;
        }

        public void setAccess(String access) {
            this.access = access;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getMoveSize() {
            return moveSize;
        }

        public void setMoveSize(String moveSize) {
            this.moveSize = moveSize;
        }
    }

    @JsonProperty("Target")
    public Target target;

    public static final class Target {
        @JsonProperty("Endpoint")
        private String endPoint;

        @JsonProperty("Access")
        private String access;

        @JsonProperty("Secret")
        private String secret;

        @JsonProperty("Bucket")
        private String bucket;

        @JsonProperty("Prefix")
        private String prefix;

        public String getEndPoint() {
            return endPoint;
        }

        public void setEndPoint(String endPoint) {
            this.endPoint = endPoint;
        }

        public String getAccess() {
            return access;
        }

        public void setAccess(String access) {
            this.access = access;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }
}
