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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import com.pspace.ifsmover.rest.RestConfig;
import com.pspace.ifsmover.rest.S3Config;
import com.pspace.ifsmover.rest.Utils;
import com.pspace.ifsmover.rest.PrintStack;
import com.pspace.ifsmover.rest.data.DataRerun;
import com.pspace.ifsmover.rest.data.format.JsonRerun;
import com.pspace.ifsmover.rest.db.DBManager;
import com.pspace.ifsmover.rest.exception.ErrCode;
import com.pspace.ifsmover.rest.exception.RestException;
import com.pspace.ifsmover.rest.repository.IfsS3;
import com.pspace.ifsmover.rest.repository.Repository;
import com.pspace.ifsmover.rest.repository.RepositoryFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class Rerun extends MoverRequest {
    private static final Logger logger = LoggerFactory.getLogger(Rerun.class);
    private String userId = null;
    private String jobId = null;
    private JsonRerun jsonRerun = null;
    private String command = null;
    
    public Rerun(HttpServletRequest request, HttpServletResponse response, String userId, String jobId) {
        super(request, response);
        this.userId = userId;
        this.jobId = jobId;
    }

    @Override
    public void process() throws RestException {
        // TODO Auto-generated method stub
        logger.info("Rerun ...");
        logger.info("userId : {}, jobId : {}", userId, jobId);

        try {
            DataRerun dataRerun = new DataRerun(request.getInputStream());

            try {
                dataRerun.extract();
            } catch (Exception e) {
                throw new RestException(ErrCode.BAD_REQUEST);
            }

            jsonRerun = dataRerun.getJsonRerun();

            long matchId = Utils.getDBInstance().getUserMatchJob(userId, jobId);
            if (matchId == -1) {
                logger.error("Not exist userId and jobId");
                setReturnJaonError("Not exist userId and jobId", false);
                return;
            }

            Map<String, Object> info = null;
            info = Utils.getDBInstance().getJobInfo(jobId);

            if (info == null) {
                logger.error("Can't find job({})", jobId);
                throw new RestException(ErrCode.BAD_REQUEST);
            }

            // Check
            S3Config config = null;
            config = new S3Config(jsonRerun.getSource().getMountPoint(),
                                  jsonRerun.getSource().getEndPoint(), 
                                  jsonRerun.getSource().getAccess(), 
                                  jsonRerun.getSource().getSecret(), 
                                  jsonRerun.getSource().getBucket(), 
                                  jsonRerun.getSource().getPrefix());

            RepositoryFactory factory = new RepositoryFactory();
            Repository sourceRepository = factory.getSourceRepository((String)info.get(DBManager.JOB_TABLE_COLUMN_JOB_TYPE));
            sourceRepository.setConfig(config, true);
            int result = sourceRepository.check();
            if (result != 0) {
                setReturnJaonError(sourceRepository.getErrMessage(), false);
                return;
            }

            config = new S3Config(null,
                                  jsonRerun.getTarget().getEndPoint(), 
                                  jsonRerun.getTarget().getAccess(), 
                                  jsonRerun.getTarget().getSecret(), 
                                  jsonRerun.getTarget().getBucket(), 
                                  jsonRerun.getTarget().getPrefix());
            IfsS3 ifsS3 = new IfsS3();
            ifsS3.setConfig(config, false);
            result = ifsS3.check();
            if (result != 0) {
                setReturnJaonError(ifsS3.getErrMessage(), false);
                return;
            }

            String uuid = UUID.randomUUID().toString();
            String sourceFileName = "source-" + uuid;
            String targetFileName = "target-" + uuid;
            
            printJsonRerun();
            saveSourceConfFile(sourceFileName);
            saveTargetConfFile(targetFileName);
            
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("win")) {
                command = "python ifs_mover -rerun=" + jobId + " -source=" + sourceFileName + " -target=" + targetFileName;
            } else {
                command = "./ifs_mover -rerun=" + jobId + " -source=" + sourceFileName + " -target=" + targetFileName;
            }
            
            File file = new File(RestConfig.getInstance().getPath());
            Process process = Runtime.getRuntime().exec(command, null, file);
            process.waitFor();

            info = Utils.getDBInstance().getJobInfo(jobId);
            if (info == null) {
                logger.warn("Not exist userId and jobId");
                setReturnJaonError("Not exist userId and jobId", false);
                return;
            }

            String returnJson = null;
            returnJson = "{\"Result\":\"success\", \"Message\":null}";
            response.getOutputStream().write(returnJson.getBytes());
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (IOException | InterruptedException e) {
            PrintStack.logging(logger, e);
            throw new RestException(ErrCode.INTERNAL_SERVER_ERROR);
        }
    }
    
    public void printJsonRerun() {
        logger.info("Source.mountpoint : {}", jsonRerun.getSource().getMountPoint());
        logger.info("Source.endpoint : {}", jsonRerun.getSource().getEndPoint());
        logger.info("Source.access : {}", jsonRerun.getSource().getAccess());
        logger.info("Source.secret : {}", jsonRerun.getSource().getSecret());
        logger.info("Source.bucket : {}", jsonRerun.getSource().getBucket());
        logger.info("Source.prefix : {}", jsonRerun.getSource().getPrefix());
        logger.info("Source.move_size : {}", jsonRerun.getSource().getMoveSize());
        logger.info("target.endpoint : {}", jsonRerun.getTarget().getEndPoint());
        logger.info("target.access : {}", jsonRerun.getTarget().getAccess());
        logger.info("target.secret : {}", jsonRerun.getTarget().getSecret());
        logger.info("target.bucket : {}", jsonRerun.getTarget().getBucket());
        logger.info("target.prefix : {}", jsonRerun.getTarget().getPrefix());
    }
    
    public void saveSourceConfFile(String fileName) throws IOException {
        try {
            String fullPath = null;
            String path = RestConfig.getInstance().getPath();
            if (path.charAt(path.length() - 1) == '/') {
                fullPath = path + fileName;
            } else {
                fullPath = path + "/" + fileName;
            }
            FileWriter fileWriter = new FileWriter(fullPath, false);
            if (jsonRerun.getSource().getMountPoint() == null) {
                fileWriter.write("mountpoint=\n");
            } else {
                fileWriter.write("mountpoint=" + jsonRerun.getSource().getMountPoint() + "\n");
            }
            if (jsonRerun.getSource().getEndPoint() == null) {
                fileWriter.write("endpoint=\n");
            } else {
                fileWriter.write("endpoint=" + jsonRerun.getSource().getEndPoint() + "\n");
            }
            if (jsonRerun.getSource().getAccess() == null) {
                fileWriter.write("access=\n");
            } else {
                fileWriter.write("access=" + jsonRerun.getSource().getAccess() + "\n");
            }
            if (jsonRerun.getSource().getSecret() == null) {
                fileWriter.write("secret=\n");
            } else {
                fileWriter.write("secret=" + jsonRerun.getSource().getSecret() + "\n");
            }
            if (jsonRerun.getSource().getBucket() == null) {
                fileWriter.write("bucket=\n");
            } else {
                fileWriter.write("bucket=" + jsonRerun.getSource().getBucket() + "\n");
            }
            if (jsonRerun.getSource().getPrefix() == null) {
                fileWriter.write("prefix=\n");
            } else {
                fileWriter.write("prefix=" + jsonRerun.getSource().getPrefix() + "\n");
            }
            if (jsonRerun.getSource().getMoveSize() == null) {
                fileWriter.write("move_size=\n");
            } else {
                fileWriter.write("move_size=" + jsonRerun.getSource().getMoveSize() + "\n");
            }
            
            fileWriter.close();
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    public void saveTargetConfFile(String fileName) throws IOException {
        try {
            String fullPath = null;
            String path = RestConfig.getInstance().getPath();
            if (path.charAt(path.length() - 1) == '/') {
                fullPath = path + fileName;
            } else {
                fullPath = path + "/" + fileName;
            }
            FileWriter fileWriter = new FileWriter(fullPath, false);
            if (jsonRerun.getTarget().getEndPoint() == null) {
                fileWriter.write("endpoint=\n");
            } else {
                fileWriter.write("endpoint=" + jsonRerun.getTarget().getEndPoint() + "\n");
            }
            if (jsonRerun.getTarget().getAccess() == null) {
                fileWriter.write("access=\n");
            } else {
                fileWriter.write("access=" + jsonRerun.getTarget().getAccess() + "\n");
            }
            if (jsonRerun.getTarget().getSecret() == null) {
                fileWriter.write("secret=\n");
            } else {
                fileWriter.write("secret=" + jsonRerun.getTarget().getSecret() + "\n");
            }
            if (jsonRerun.getTarget().getBucket() == null) {
                fileWriter.write("bucket=\n");
            } else {
                fileWriter.write("bucket=" + jsonRerun.getTarget().getBucket() + "\n");
            }
            if (jsonRerun.getTarget().getPrefix() == null) {
                fileWriter.write("prefix=\n");
            } else {
                fileWriter.write("prefix=" + jsonRerun.getTarget().getPrefix() + "\n");
            }

            fileWriter.close();
        } catch (IOException e) {
            throw new IOException(e);
        }
    }
}
