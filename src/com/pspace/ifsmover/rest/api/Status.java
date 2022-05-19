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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;
import com.pspace.ifsmover.rest.DBManager;
import com.pspace.ifsmover.rest.PrintStack;
import com.pspace.ifsmover.rest.exception.ErrCode;
import com.pspace.ifsmover.rest.exception.RestException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class Status extends MoverRequest {
    private static final Logger logger = LoggerFactory.getLogger(Status.class);
    private String userId = null;
    private String returnJson = null;

    private static final long UNIT_G = (1024 * 1024 * 1024);
	private static final long UNIT_M = (1024 * 1024);
	private static final long UNIT_K = 1024;
    
    public Status(HttpServletRequest request, HttpServletResponse response, String userId) {
        super(request, response);
        this.userId = userId;
    }

    @Override
    public void process() throws RestException {
        // TODO Auto-generated method stub
        logger.info("Status ...");
        logger.info("userId : {}", userId);

        String jobId;
		int jobState;
		String jobType;
		String sourcePoint;
		String targetPoint;
		long objectsCount;
		long objectsSize;
		long movedObjectsCount;
		long movedObjectsSize;
		long failedCount;
		long failedSize;
		long skipObjectsCount;
		long skipObjectsSize;
		String startTime;
		String endTime;
		String errorDesc;
		
		double unitSize = 0.0;
		double unitMove = 0.0;
		double unitFailed = 0.0;
		double unitSkip = 0.0;
		double percent = 0.0;

        int jobCount = 0;
        try {
            List<Map<String, String>> list = DBManager.status(userId);
            if (list == null || list.size() == 0) {
                logger.info("No Jobs were created.");
                returnJson = "{\"Result\":\"failed\", \"Message\":\"No Jobs were created.\", \"Items\":[]}";
            } else {
                returnJson = "{\"Result\":\"success\", \"Message\":null, \"Items\":[";
                logger.info("status list size : {}", list.size());
                for (Map<String, String> info : list) {
                    
                    jobId = info.get(DBManager.JOB_TABLE_COLUMN_JOB_ID);
                    jobState = Integer.parseInt(info.get(DBManager.JOB_TABLE_COLUMN_JOB_STATE));
                    jobType = info.get(DBManager.JOB_TABLE_COLUMN_JOB_TYPE);
                    sourcePoint = info.get(DBManager.JOB_TABLE_COLUMN_SOURCE_POINT);
                    targetPoint = info.get(DBManager.JOB_TABLE_COLUMN_TARGET_POINT);
                    objectsCount = Long.parseLong(info.get(DBManager.JOB_TABLE_COLUMN_OBJECTS_COUNT));
                    objectsSize = Long.parseLong(info.get(DBManager.JOB_TABLE_COLUMN_OBJECTS_SIZE));
                    movedObjectsCount = Long.parseLong(info.get(DBManager.JOB_TABLE_COLUMN_MOVED_OBJECTS_COUNT));
                    movedObjectsSize = Long.parseLong(info.get(DBManager.JOB_TABLE_COLUMN_MOVED_OBJECTS_SIZE));
                    failedCount = Long.parseLong(info.get(DBManager.JOB_TABLE_COLUMN_FAILED_COUNT));
                    failedSize = Long.parseLong(info.get(DBManager.JOB_TABLE_COLUMN_FAILED_SIZE));
                    skipObjectsCount = Long.parseLong(info.get(DBManager.JOB_TABLE_COLUMN_SKIP_OBJECTS_COUNT));
                    skipObjectsSize = Long.parseLong(info.get(DBManager.JOB_TABLE_COLUMN_SKIP_OBJECTS_SIZE));
                    startTime = info.get(DBManager.JOB_TABLE_COLUMN_START);
                    endTime = info.get(DBManager.JOB_TABLE_COLUMN_END);
                    errorDesc = info.get(DBManager.JOB_TABLE_COLUMN_ERROR_DESC);

                    if (jobState == DBManager.JOB_STATE_REMOVE) {
                        logger.info("JobId : {} -- removed job.");
                        continue;
                    }

                    jobCount++;

                    returnJson += "{\"JobId\":" + info.get(DBManager.JOB_TABLE_COLUMN_JOB_ID) + ",";

                    switch (jobState) {
                    case DBManager.JOB_STATE_INIT:
                        returnJson += "\"Status\":\"INIT\",";
                        break;
                        
                    case DBManager.JOB_STATE_MOVE:
                        returnJson += "\"Status\":\"MOVE\",";
                        break;
                        
                    case DBManager.JOB_STATE_COMPLETE:
                        returnJson += "\"Status\":\"COMPLETE\",";
                        break;
                        
                    case DBManager.JOB_STATE_STOP:
                        returnJson += "\"Status\":\"STOP\",";
                        break;
                        
                    case DBManager.JOB_STATE_RERUN_INIT:
                        returnJson += "\"Status\":\"RERUN INIT\",";
                        break;
                        
                    case DBManager.JOB_STATE_RERUN_MOVE:
                        returnJson += "\"Status\":\"RERUN MOVE\",";
                        break;
                        
                    case DBManager.JOB_STATE_ERROR:
                        returnJson += "\"Status\":\"ERROR\",";
                        break;
                        
                    default:
                        logger.error("Undefined state : {}", jobState);
                        throw new RestException(ErrCode.INTERNAL_SERVER_ERROR);
                    }

                    if (jobState == DBManager.JOB_STATE_ERROR) {
                        setRerunJson(sourcePoint, targetPoint, startTime, endTime, errorDesc, 0, null, 0, null, 0, null, 0, null, null);
                        logger.info("jobId = {}", jobId);
                        logger.info("job state = {}", jobState);
                        logger.info("error desc = {}", errorDesc);
                        continue;
                    }

                    if (jobState == DBManager.JOB_STATE_INIT) {
                        String strObjectSize = null;
                        unitSize = (double)objectsSize / UNIT_G;
                        
                        if (unitSize > 1.0) {
                            strObjectSize = String.format("%,.2fG", unitSize);
                        } else {
                            unitSize = (double)objectsSize / UNIT_M;
                            if (unitSize > 1.0) {
                                strObjectSize = String.format("%,.2fM", unitSize);
                            } else {
                                unitSize = (double)objectsSize / UNIT_K;
                                if (unitSize > 1.0) {
                                    strObjectSize = String.format("%,.2fK", unitSize);
                                } else {
                                    strObjectSize = String.format("%,d", objectsSize);
                                }
                            }
                        }
                        setRerunJson(sourcePoint, targetPoint, startTime, endTime, errorDesc, objectsCount, strObjectSize, 0, null, 0, null, 0, null, null);
                    } else if (jobState == DBManager.JOB_STATE_RERUN_INIT) {
                        String strObjectSize = null;
                        String strSkipSize = null;
                        String strFailSize = null;
                        unitSize = (double)objectsSize / UNIT_G;
                        
                        if (unitSize > 1.0) {
                            strObjectSize = String.format("%,.2fG", unitSize);
                        } else {
                            unitSize = (double)objectsSize / UNIT_M;
                            if (unitSize > 1.0) {
                                strObjectSize = String.format("%,.2fM", unitSize);
                            } else {
                                unitSize = (double)objectsSize / UNIT_K;
                                if (unitSize > 1.0) {
                                    strObjectSize = String.format("%,.2fK", unitSize);
                                } else {
                                    strObjectSize = String.format("%,d", objectsSize);
                                }
                            }
                        }
                        
                        if (skipObjectsCount > 0) {
                            unitSkip = (double)skipObjectsSize / UNIT_G;
                            
                            if (unitSkip > 1.0) {
                                strSkipSize = String.format("%,.2fG", unitSkip);
                            } else {
                                unitSkip = (double)skipObjectsSize / UNIT_M;
                                if (unitSkip > 1.0) {
                                    strSkipSize = String.format("%,.2fM", unitSkip);
                                } else {
                                    unitSkip = (double)skipObjectsSize / UNIT_K;
                                    if (unitSkip > 1.0) {
                                        strSkipSize = String.format("%,.2fK", unitSkip);
                                    } else {
                                        strSkipSize = String.format("%,d", skipObjectsSize);
                                    }
                                }
                            }
                        } 
                        
                        if (failedCount > 0) {
                            unitFailed = (double)failedSize / UNIT_G;
                            if (unitFailed > 1.0) {
                                strFailSize = String.format("%,.2fG", unitFailed);
                            } else {
                                unitFailed = (double)failedSize / UNIT_M;
                                if (unitFailed > 1.0) {
                                    strFailSize = String.format("%,.2fM", unitFailed);
                                } else {
                                    unitFailed = (double)failedSize / UNIT_K;
                                    if (unitFailed > 1.0) {
                                        strFailSize = String.format("%,.2fK", unitFailed);
                                    } else {
                                        strSkipSize = String.format("%,d", failedSize);
                                    }
                                }
                            }
                        }
                        setRerunJson(sourcePoint, targetPoint, startTime, endTime, errorDesc, objectsCount, strObjectSize, 0, null, skipObjectsCount, strSkipSize, failedCount, strFailSize, null);
                    } else {
                        String strObjectSize = null;
                        String strMoveSize = null;
                        String strSkipSize = null;
                        String strFailSize = null;
                        String strPercent = null;
                        
                        if (objectsCount == 0) {
                            percent = 0.0;
                        } else {
                            percent = (((double)skipObjectsSize + (double)movedObjectsSize + (double)failedSize) / (double) objectsSize) * 100;
                            strPercent = String.format("%.2f", percent);
                        }
                        
                        unitSize = (double)objectsSize / UNIT_G;
                        
                        if (unitSize > 1.0) {
                            strObjectSize = String.format("%,.2fG", unitSize);
                            unitMove = (double)movedObjectsSize / UNIT_G;
                            if (unitMove > 1.0) {
                                strMoveSize = String.format("%,.2fG", unitMove);
                            } else {
                                unitMove = (double) movedObjectsSize / UNIT_M;
                                if (unitMove > 1.0) {
                                    strMoveSize = String.format("%,.2fM", unitMove);
                                } else {
                                    unitMove = (double) movedObjectsSize / UNIT_K;
                                    if (unitMove > 1.0) {
                                        strMoveSize = String.format("%,.2fK", unitMove);
                                    } else {
                                        strMoveSize = String.format("%,d", movedObjectsSize);
                                    }
                                }
                            }
                        } else {
                            unitSize = (double)objectsSize / UNIT_M;
                            if (unitSize > 1.0) {
                                strObjectSize = String.format("%,.2fM", unitSize);
                                unitMove = (double)movedObjectsSize / UNIT_M;
                                if (unitMove > 1.0) {
                                    strMoveSize = String.format("%,.2fM", unitMove);
                                } else {
                                    unitMove = (double)movedObjectsSize / UNIT_K;
                                    if (unitMove > 1.0) {
                                        strMoveSize = String.format("%,.2fK", unitMove);
                                    } else {
                                        strMoveSize = String.format("%,d", movedObjectsSize);
                                    }
                                }
                            } else {
                                unitSize = (double)objectsSize / UNIT_K;
                                if (unitSize > 1.0) {
                                    strObjectSize = String.format("%,.2fK", unitSize);
                                    unitMove = (double)movedObjectsSize / UNIT_K;
                                    if (unitMove > 1.0) {
                                        strMoveSize = String.format("%,.2fK", unitMove);
                                    } else {
                                        strMoveSize = String.format("%,d", movedObjectsSize);
                                    }
                                } else {
                                    strObjectSize = String.format("%,d", objectsSize);
                                    strMoveSize = String.format("%,d", movedObjectsSize);
                                }
                            }
                        }
                        
                        if (skipObjectsCount > 0) {
                            unitSkip = (double)skipObjectsSize / UNIT_G;
                            if (unitSkip > 1.0) {
                                strSkipSize = String.format("%,.2fG", unitSkip);
                            } else {
                                unitSkip = (double)skipObjectsSize / UNIT_M;
                                if (unitSkip > 1.0) {
                                    strSkipSize = String.format("%,.2fM", unitSkip);
                                } else {
                                    unitSkip = (double)skipObjectsSize / UNIT_K;
                                    if (unitSkip > 1.0) {
                                        strSkipSize = String.format("%,.2fK", unitSkip);
                                    } else {
                                        strSkipSize = String.format("%,d", skipObjectsSize);
                                    }
                                }
                            }
                        }
                        
                        if (failedCount > 0) {
                            unitFailed = (double)failedSize / UNIT_G;
                            if (unitFailed > 1.0) {
                                strFailSize = String.format("%,.2fG", unitFailed);
                            } else {
                                unitFailed = (double)failedSize / UNIT_M;
                                if (unitFailed > 1.0) {
                                    strFailSize = String.format("%,.2fM", unitFailed);
                                } else {
                                    unitFailed = (double)failedSize / UNIT_K;
                                    if (unitFailed > 1.0) {
                                        strFailSize = String.format("%,.2fK", unitFailed);
                                    } else {
                                        strFailSize = String.format("%,d", failedSize);
                                    }
                                }
                            }
                        }
                        setRerunJson(sourcePoint, targetPoint, startTime, endTime, errorDesc, objectsCount, strObjectSize, movedObjectsCount, strMoveSize, skipObjectsCount, strSkipSize, failedCount, strFailSize, strPercent);
                    }
        
                    logger.info("jobId = {}", jobId);
                    
                    if (jobState == DBManager.JOB_STATE_INIT) {
                        logger.info("job state = INIT");
                    } else if (jobState == DBManager.JOB_STATE_MOVE) {
                        logger.info("job state = MOVE");
                    } else if (jobState == DBManager.JOB_STATE_COMPLETE) {
                        logger.info("job state = COMPLETE");
                    } else if (jobState == DBManager.JOB_STATE_STOP) {
                        logger.info("job state = STOP");
                    } else if (jobState == DBManager.JOB_STATE_RERUN_INIT) {
                        logger.info("job state = RERUN INIT");
                    } else if (jobState == DBManager.JOB_STATE_RERUN_MOVE) {
                        logger.info("job state = RERUN MOVE");
                    }
                    logger.info("job type = {}", jobType);
                    logger.info("source = {}", sourcePoint);
                    logger.info("target = {}", targetPoint);
                    logger.info("objects count = {}", objectsCount);
                    logger.info("objects size = {}", objectsSize);
                    logger.info("moved objects count = {}", movedObjectsCount);
                    logger.info("moved objects size = {}", movedObjectsSize);
                    logger.info("failed count = {}", failedCount);
                    logger.info("failed size = {}", failedSize);
                    logger.info("skip objects count = {}", skipObjectsCount);
                    logger.info("skip objects size = {}", skipObjectsSize);
                    logger.info("start time = {}", startTime);
                    logger.info("end time = {}", endTime);
                    logger.info("error desc = {}", errorDesc);
                }
                returnJson += "]}";

                if (jobCount == 0) {
                    logger.info("No Jobs were created.");
                    returnJson = "{\"Result\":\"failed\", \"Message\":\"No Jobs were created.\", \"Items\":[]}";
                }
            } 
        } catch (Exception e) {
            PrintStack.logging(logger, e);
            throw new RestException(ErrCode.INTERNAL_SERVER_ERROR);
        }

        try {
            response.getOutputStream().write(returnJson.getBytes());
        } catch (IOException e) {
            PrintStack.logging(logger, e);
            throw new RestException(ErrCode.INTERNAL_SERVER_ERROR);
        }
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void setRerunJson(String sourcePoint, 
                              String targetPoint,
                              String startTime,
                              String endTime,
                              String errorDesc,
                              long objectCount,
                              String objectSize,
                              long movedCount,
                              String movedSize,
                              long skippedCount,
                              String skippedSize,
                              long failedCount,
                              String failedSize,
                              String progress) {
        returnJson += "\"Source\":\"" + sourcePoint + "\", \"Target\":\"" + targetPoint + "\",";
        if (!Strings.isNullOrEmpty(startTime)) {
            returnJson += "\"StartTime\":\"" + startTime + "\",";
        } else {
            returnJson += "\"StartTime\":null,";
        }
        if (!Strings.isNullOrEmpty(endTime)) {
            returnJson += "\"EndTime\":\"" + endTime + "\",";
        } else {
            returnJson += "\"EndTime\":null,";
        }
        if (!Strings.isNullOrEmpty(errorDesc)) {
            returnJson += "\"ErrorDesc\":\"" + errorDesc + "\",";
        } else {
            returnJson += "\"ErrorDesc\":null,";
        }
        if (objectCount > 0) {
            returnJson += "\"TotalCount\":" + objectCount + ", \"TotalSize\":\"" + objectSize + "\",";
        } else {
            returnJson += "\"TotalCount\":0, \"TotalSize\":null,";
        }
        if (movedCount > 0) {
            returnJson += "\"MovedCount\":" + movedCount + ", \"MovedSize\":\"" + movedSize + "\",";
        } else {
            returnJson += "\"MovedCount\":0, \"MovedSize\":null,";
        }
        if (skippedCount > 0) {
            returnJson += "\"SkippedCount\":" + skippedCount + ", \"SkippedSize\":\"" + skippedSize + "\",";
        } else {
            returnJson += "\"SkippedCount\":0, \"SkippedSize\":null,";
        }
        if (failedCount > 0) {
            returnJson += "\"FailedCount\":" + failedCount + ", \"FailedSize\":\"" + failedSize + "\",";
        } else {
            returnJson += "\"FailedCount\":0, \"FailedSize\":null,";
        }
        if (!Strings.isNullOrEmpty(progress)) {
            returnJson += "\"Progress\":\"" + progress + "\"},";
        } else {
            returnJson += "\"Progress\":null},";
        }
    }
}
