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
package com.pspace.ifsmover.rest.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface DBManager {
    public static final String SQL_SELECT_JOB_STATUS = "SELECT A.job_id, A.job_state, A.job_type, A.source_point, A.target_point, A.objects_count, A.objects_size, A.moved_objects_count, A.moved_objects_size, A.failed_count, A.failed_size, A.skip_objects_count, A.skip_objects_size, A.delete_objects_count, A.delete_objects_size, A.start, A.end, A.error_desc FROM JOB A INNER JOIN UserMatchJob B ON A.job_id = B.job_id WHERE B.user_id = '";
    public static final String JOB_TABLE_COLUMN_JOB_ID = "job_id";
	public static final String JOB_TABLE_COLUMN_JOB_STATE = "job_state";
	public static final String JOB_TABLE_COLUMN_PID = "pid";
	public static final String JOB_TABLE_COLUMN_JOB_TYPE = "job_type";
	public static final String JOB_TABLE_COLUMN_SOURCE_POINT = "source_point";
	public static final String JOB_TABLE_COLUMN_TARGET_POINT = "target_point";
	public static final String JOB_TABLE_COLUMN_OBJECTS_COUNT = "objects_count";
	public static final String JOB_TABLE_COLUMN_OBJECTS_SIZE = "objects_size";
	public static final String JOB_TABLE_COLUMN_MOVED_OBJECTS_COUNT = "moved_objects_count";
	public static final String JOB_TABLE_COLUMN_MOVED_OBJECTS_SIZE = "moved_objects_size";
	public static final String JOB_TABLE_COLUMN_FAILED_COUNT = "failed_count";
	public static final String JOB_TABLE_COLUMN_FAILED_SIZE = "failed_size";
	public static final String JOB_TABLE_COLUMN_SKIP_OBJECTS_COUNT = "skip_objects_count";
	public static final String JOB_TABLE_COLUMN_SKIP_OBJECTS_SIZE = "skip_objects_size";
	public static final String JOB_TABLE_COLUMN_DELETE_OBJECT_COUNT = "delete_objects_count";
	public static final String JOB_TABLE_COLUMN_DELETE_OBJECT_SIZE = "delete_objects_size";
	public static final String JOB_TABLE_COLUMN_START = "start";
	public static final String JOB_TABLE_COLUMN_END = "end";
	public static final String JOB_TABLE_COLUMN_ERROR_DESC = "error_desc";
    public static final String MATCH_TABLE_COLUMN_MATCH_ID = "match_id";

    public static final String SQL_GET_JOB_INFO = "SELECT job_state, job_type, error_desc FROM JOB WHERE job_id = ";
    public static final String SQL_INSERT_USERMATCHJOB = "INSERT INTO UserMatchJob(user_id, job_id) VALUES(?, ?)";
    public static final String SQL_GET_USERMATCHJOB = "SELECT match_id FROM UserMatchJob WHERE user_id='";
    public static final String SQL_GET_USERMATCHJOB_JOBID = "' and job_id='";
    
    public static final String SQL_DELETE_USERMATCHJOB = "DELETE FROM UserMatchJob WHERE match_id = ";

	public static final int JOB_STATE_INIT = 0;
	public static final int JOB_STATE_MOVE = 1;
	public static final int JOB_STATE_COMPLETE = 4;
	public static final int JOB_STATE_STOP = 5;
    public static final int JOB_STATE_REMOVE = 6;
	public static final int JOB_STATE_RERUN_INIT = 7;
	public static final int JOB_STATE_RERUN_MOVE = 8;
	public static final int JOB_STATE_ERROR = 10;
    
    public void init(String dbUrl, String dbPort, String dbName, String userName, String passwd,  int poolSize) throws Exception;
    public Map<String, Object> getJobInfo(String jobId);
    public void insertUserMatchJob(String userId, String jobId);
    public int getUserMatchJob(String userId, String jobId);
    public void deleteUserMatchJob(long matchId);
    public List<HashMap<String, Object>> status(String userId);
}
