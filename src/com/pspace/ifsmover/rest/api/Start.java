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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import com.pspace.ifsmover.rest.RestConfig;
import com.pspace.ifsmover.rest.S3Config;
import com.pspace.ifsmover.rest.Utils;
import com.google.common.base.Strings;
import com.pspace.ifsmover.rest.PrintStack;
import com.pspace.ifsmover.rest.data.DataStart;
import com.pspace.ifsmover.rest.data.format.JsonStart;
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

public class Start extends MoverRequest {
    private static final Logger logger = LoggerFactory.getLogger(Start.class);
    private JsonStart jsonStart = null;
    private String command = null;

    public Start(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
    }

    @Override
    public void process() throws RestException {
        // TODO Auto-generated method stub
        logger.info("Start ...");

        try {
            DataStart dataStart = new DataStart(request.getInputStream());
            try {
                dataStart.extract();
            } catch (Exception e) {
                throw new RestException(ErrCode.BAD_REQUEST);
            }
            
            jsonStart = dataStart.getJsonStart();

            if (jsonStart.getType().equalsIgnoreCase(Repository.SWIFT)) {
                throw new RestException(ErrCode.NOT_IMPLEMENTED);
            }

            if (Strings.isNullOrEmpty(jsonStart.getUserId())) {
                logger.error("UserId is null or empty");
                setReturnJaonError("UserId is null or empty", true);
                return;
            }
        
            // Check
            S3Config config = null;
            config = new S3Config(jsonStart.getSource().getMountPoint(),
                                  jsonStart.getSource().getEndPoint(), 
                                  jsonStart.getSource().getAccess(), 
                                  jsonStart.getSource().getSecret(), 
                                  jsonStart.getSource().getBucket(), 
                                  jsonStart.getSource().getPrefix());

            RepositoryFactory factory = new RepositoryFactory();
            Repository sourceRepository = factory.getSourceRepository(jsonStart.getType());
            sourceRepository.setConfig(config, true);
            int result = sourceRepository.check();
            if (result != 0) {
                setReturnJaonError(sourceRepository.getErrMessage(), true);
                return;
            }

            config = new S3Config(null,
                                  jsonStart.getTarget().getEndPoint(), 
                                  jsonStart.getTarget().getAccess(), 
                                  jsonStart.getTarget().getSecret(), 
                                  jsonStart.getTarget().getBucket(), 
                                  jsonStart.getTarget().getPrefix());
            IfsS3 ifsS3 = new IfsS3();
            ifsS3.setConfig(config, false);
            result = ifsS3.check();
            if (result != 0) {
                setReturnJaonError(ifsS3.getErrMessage(), true);
                return;
            }

            String uuid = UUID.randomUUID().toString();
            String sourceFileName = "source-" + uuid;
            String targetFileName = "target-" + uuid;

            printJsonStart();
            saveSourceConfFile(sourceFileName);
            saveTargetConfFile(targetFileName);

            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("win")) {
                command = "python ifs_mover -t=" + jsonStart.getType() + " -source=" + sourceFileName + " -target=" + targetFileName;
            } else {
                command = "./ifs_mover -t=" + jsonStart.getType() + " -source=" + sourceFileName + " -target=" + targetFileName;
            }
            
            logger.info("command : {}", command);
            File file = new File(RestConfig.getInstance().getPath());
            Process process = Runtime.getRuntime().exec(command, null, file);
            
            // deleteJobIdFile();
            String jobId = getJobIdFromFile(uuid);
            logger.info("JobId : {}", jobId);

            Map<String, Object> info = null;
            info = Utils.getDBInstance().getJobInfo(jobId);

            if (info == null) {
                logger.error("Can't find job({})", jobId);
                throw new RestException(ErrCode.BAD_REQUEST);
            }
            int jobState = (int) info.get(DBManager.JOB_TABLE_COLUMN_JOB_STATE);
            logger.info("job state : {}", info.get(DBManager.JOB_TABLE_COLUMN_JOB_STATE));
            logger.info("job error desc : {}", info.get(DBManager.JOB_TABLE_COLUMN_ERROR_DESC));
            Utils.getDBInstance().insertUserMatchJob(jsonStart.getUserId(), jobId);
            
            String returnJson = null;
            if (jobState == DBManager.JOB_STATE_ERROR) {
                returnJson = "{\"Result\":\"failed\", \"Message\":\"" + info.get(DBManager.JOB_TABLE_COLUMN_ERROR_DESC) + "\", \"JobId\":0}";
            } else {
                returnJson = "{\"Result\":\"success\", \"Message\":null, \"JobId\":" + jobId + "}";
            }
            response.getOutputStream().write(returnJson.getBytes());
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (IOException e) {
            PrintStack.logging(logger, e);
            throw new RestException(ErrCode.INTERNAL_SERVER_ERROR);
        }
    }

    public void printJsonStart() {
        logger.info("UserId : {}", jsonStart.getUserId());
        logger.info("Type : {}", jsonStart.getType());
        logger.info("Source.mountpoint : {}", jsonStart.getSource().getMountPoint());
        logger.info("Source.endpoint : {}", jsonStart.getSource().getEndPoint());
        logger.info("Source.access : {}", jsonStart.getSource().getAccess());
        logger.info("Source.secret : {}", jsonStart.getSource().getSecret());
        logger.info("Source.bucket : {}", jsonStart.getSource().getBucket());
        logger.info("Source.prefix : {}", jsonStart.getSource().getPrefix());
        logger.info("Source.move_size : {}", jsonStart.getSource().getMoveSize());
        logger.info("target.endpoint : {}", jsonStart.getTarget().getEndPoint());
        logger.info("target.access : {}", jsonStart.getTarget().getAccess());
        logger.info("target.secret : {}", jsonStart.getTarget().getSecret());
        logger.info("target.bucket : {}", jsonStart.getTarget().getBucket());
        logger.info("target.prefix : {}", jsonStart.getTarget().getPrefix());
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
            if (jsonStart.getSource().getMountPoint() == null) {
                fileWriter.write("mountpoint=\n");
            } else {
                fileWriter.write("mountpoint=" + jsonStart.getSource().getMountPoint() + "\n");
            }
            if (jsonStart.getSource().getEndPoint() == null) {
                fileWriter.write("endpoint=\n");
            } else {
                fileWriter.write("endpoint=" + jsonStart.getSource().getEndPoint() + "\n");
            }
            if (jsonStart.getSource().getAccess() == null) {
                fileWriter.write("access=\n");
            } else {
                fileWriter.write("access=" + jsonStart.getSource().getAccess() + "\n");
            }
            if (jsonStart.getSource().getSecret() == null) {
                fileWriter.write("secret=\n");
            } else {
                fileWriter.write("secret=" + jsonStart.getSource().getSecret() + "\n");
            }
            if (jsonStart.getSource().getBucket() == null) {
                fileWriter.write("bucket=\n");
            } else {
                fileWriter.write("bucket=" + jsonStart.getSource().getBucket() + "\n");
            }
            if (jsonStart.getSource().getPrefix() == null) {
                fileWriter.write("prefix=\n");
            } else {
                fileWriter.write("prefix=" + jsonStart.getSource().getPrefix() + "\n");
            }
            if (jsonStart.getSource().getMoveSize() == null) {
                fileWriter.write("move_size=\n");
            } else {
                fileWriter.write("move_size=" + jsonStart.getSource().getMoveSize() + "\n");
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
            if (jsonStart.getTarget().getEndPoint() == null) {
                fileWriter.write("endpoint=\n");
            } else {
                fileWriter.write("endpoint=" + jsonStart.getTarget().getEndPoint() + "\n");
            }
            if (jsonStart.getTarget().getAccess() == null) {
                fileWriter.write("access=\n");
            } else {
                fileWriter.write("access=" + jsonStart.getTarget().getAccess() + "\n");
            }
            if (jsonStart.getTarget().getSecret() == null) {
                fileWriter.write("secret=\n");
            } else {
                fileWriter.write("secret=" + jsonStart.getTarget().getSecret() + "\n");
            }
            if (jsonStart.getTarget().getBucket() == null) {
                fileWriter.write("bucket=\n");
            } else {
                fileWriter.write("bucket=" + jsonStart.getTarget().getBucket() + "\n");
            }
            if (jsonStart.getTarget().getPrefix() == null) {
                fileWriter.write("prefix=\n");
            } else {
                fileWriter.write("prefix=" + jsonStart.getTarget().getPrefix() + "\n");
            }

            fileWriter.close();
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    // private void deleteJobIdFile() {
    //     File file = new File(RestConfig.getInstance().getPath() + "/.jobId");
    //     if (file.exists()) {
    //         file.delete();
    //     }
    // }

    private String getJobIdFromFile(String uuid) throws IOException {
        File file = new File(RestConfig.getInstance().getPath() + "/.jobId" + uuid);
        while (!file.exists()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
        }
        BufferedReader br = new BufferedReader(new FileReader(file));
        String jobId = br.readLine();
        file.delete();
        return jobId;
    }
}
