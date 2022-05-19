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
package com.pspace.ifsmover.rest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteConfig;

public class DBManager {
    private static final Logger logger = LoggerFactory.getLogger(DBManager.class);
    
    private static Connection con;
	private static final String JDBC = "org.sqlite.JDBC";
	private static final String DB_FILE_URL = "jdbc:sqlite:";
    private static final String DB_FILE_NAME = "/ifs-mover.db";
	
	private static final int CACHE_SIZE = 10000;
	private static final int WAIT_TIMEOUT = 20000;

    private static final String CREATE_USER_MATCH_JOB_TABLE =
			"CREATE TABLE IF NOT EXISTS 'UserMatchJob' (\n"
            + "'match_id' INTEGER NOT NULL,\n"
            + "'user_id' TEXT,\n"
			+ "'job_id' INTEGER NOT NULL,\n"
			+ "PRIMARY KEY('match_id' AUTOINCREMENT));";

    private static final String SQL_SELECT_JOB_STATUS = "SELECT A.job_id, A.job_state, A.job_type, A.source_point, A.target_point, A.objects_count, A.objects_size, A.moved_objects_count, A.moved_objects_size, A.failed_count, A.failed_size, A.skip_objects_count, A.skip_objects_size, A.start, A.end, A.error_desc FROM JOB A INNER JOIN UserMatchJob B ON A.job_id = B.job_id WHERE B.user_id = '";
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
	public static final String JOB_TABLE_COLUMN_START = "start";
	public static final String JOB_TABLE_COLUMN_END = "end";
	public static final String JOB_TABLE_COLUMN_ERROR_DESC = "error_desc";

    private static final String SQL_GET_JOB_INFO = "SELECT job_state, job_type, error_desc FROM JOB WHERE job_id = ";
    private static final String SQL_INSERT_USERMATCHJOB = "INSERT INTO UserMatchJob(user_id, job_id) VALUES(?, ?)";
    private static final String SQL_GET_USERMATCHJOB = "SELECT match_id FROM UserMatchJob WHERE user_id='";
    private static final String SQL_GET_USERMATCHJOB_JOBID = "' and job_id='";
    
    private static final String SQL_DELETE_USERMATCHJOB = "DELETE FROM UserMatchJob WHERE match_id = ";

	public static final int JOB_STATE_INIT = 0;
	public static final int JOB_STATE_MOVE = 1;
	public static final int JOB_STATE_COMPLETE = 4;
	public static final int JOB_STATE_STOP = 5;
    public static final int JOB_STATE_REMOVE = 6;
	public static final int JOB_STATE_RERUN_INIT = 7;
	public static final int JOB_STATE_RERUN_MOVE = 8;
	public static final int JOB_STATE_ERROR = 10;
    
    public static void init() {
		try {
			Class.forName(JDBC);
			SQLiteConfig config = new SQLiteConfig();
			config.setCacheSize(CACHE_SIZE);
			config.setBusyTimeout(WAIT_TIMEOUT);
			config.setTransactionMode(SQLiteConfig.TransactionMode.DEFERRED);
    		config.setLockingMode(SQLiteConfig.LockingMode.NORMAL);
    		config.setSynchronous(SQLiteConfig.SynchronousMode.FULL);
    		config.setJournalMode(SQLiteConfig.JournalMode.WAL);
			config.setEncoding(SQLiteConfig.Encoding.UTF_8);
			DBManager.con = DriverManager.getConnection(getDBURL(), config.toProperties());
		} catch (SQLException | ClassNotFoundException e) {
			logger.error(e.getMessage());
		}

        createUserMatchJobTable();
	}

    private static String getDBURL() {
        return DB_FILE_URL + Config.getInstance().getPath() + DB_FILE_NAME;
    }
	
	private static Connection getReadConnection() {
		Connection con = null;
		try {
			Class.forName(JDBC);
			SQLiteConfig config = new SQLiteConfig();
			config.setReadOnly(true);
			con = DriverManager.getConnection(getDBURL(), config.toProperties());
		} catch (SQLException | ClassNotFoundException e) {
			logger.error(e.getMessage());
		}

		return con;
	}

	public static void open() {
		if (DBManager.con == null) {
			try {
				Class.forName(JDBC);
				DBManager.con = DriverManager.getConnection(getDBURL());
                init();
			} catch (SQLException | ClassNotFoundException e) {
				logger.error(e.getMessage());
			}
		}
	}

	public static void close() {
		if (DBManager.con != null) {
			try {
				DBManager.con.close();
			} catch (SQLException e) {
				logger.error(e.getMessage());
			}
			DBManager.con = null;
		}
	}
	
	public static Connection getConnection() {
		open();
		return DBManager.con;
	}

    public static void createUserMatchJobTable() {
		open();

		try (Statement stmt = con.createStatement()) {
			stmt.execute(CREATE_USER_MATCH_JOB_TABLE);
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}
	}

    public static Map<String, String> getJobInfo(String jobId) {
        Connection con = null;
		con = getReadConnection();

        String sql = SQL_GET_JOB_INFO + jobId;
        Map<String, String> info = null;
        try (Statement stmt = con.createStatement();
			 ResultSet rs = stmt.executeQuery(sql);) {
			if (rs.next()) {
                info = new HashMap<String, String>();
                info.put(JOB_TABLE_COLUMN_JOB_STATE, rs.getString(1));
				info.put(JOB_TABLE_COLUMN_JOB_TYPE, rs.getString(2));
                info.put(JOB_TABLE_COLUMN_ERROR_DESC, rs.getString(3));
			}
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}

        close();

		return info;
    }

    public static void insertUserMatchJob(String userId, String jobId) {
        Connection con = null;
		con = getConnection();

		try (PreparedStatement pstmt = con.prepareStatement(SQL_INSERT_USERMATCHJOB);) {
            pstmt.setString(1, userId);
			pstmt.setInt(2, Integer.parseInt(jobId));
			pstmt.executeUpdate();
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}

        close();
    }

    public static long getUserMatchJob(String userId, String jobId) {
		Connection con = null;
		con = getReadConnection();

        long matchId = -1L;
		final String sql = SQL_GET_USERMATCHJOB + userId + SQL_GET_USERMATCHJOB_JOBID + jobId + "'";
		try (Statement stmt = con.createStatement();
			 ResultSet rs = stmt.executeQuery(sql);) {
			if (rs.next()) {
				matchId = rs.getLong(1);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}

        close();

		return matchId;
    }

    public static void deleteUserMatchJob(long matchId) {
        Connection con = null;
		con = getConnection();

		String sql = SQL_DELETE_USERMATCHJOB + matchId;
		try (Statement stmt = con.createStatement()) {
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}

        close();
    }

    public static List<Map<String, String>> status(String userId) {
		Connection con = null;
		con = getReadConnection();

		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		Map<String, String> info = null;

		if (con == null) {
			return list;
		}

		try (Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(SQL_SELECT_JOB_STATUS + userId + "' ORDER BY A.job_id");) {
			while (rs.next()) {
				info = new HashMap<String, String>();
				
				info.put(JOB_TABLE_COLUMN_JOB_ID, rs.getString(1));
				info.put(JOB_TABLE_COLUMN_JOB_STATE, rs.getString(2));
				info.put(JOB_TABLE_COLUMN_JOB_TYPE, rs.getString(3));
				info.put(JOB_TABLE_COLUMN_SOURCE_POINT, rs.getString(4));
				info.put(JOB_TABLE_COLUMN_TARGET_POINT, rs.getString(5));
				info.put(JOB_TABLE_COLUMN_OBJECTS_COUNT, rs.getString(6));
				info.put(JOB_TABLE_COLUMN_OBJECTS_SIZE, rs.getString(7));
				info.put(JOB_TABLE_COLUMN_MOVED_OBJECTS_COUNT, rs.getString(8));
				info.put(JOB_TABLE_COLUMN_MOVED_OBJECTS_SIZE, rs.getString(9));
				info.put(JOB_TABLE_COLUMN_FAILED_COUNT, rs.getString(10));
				info.put(JOB_TABLE_COLUMN_FAILED_SIZE, rs.getString(11));
				info.put(JOB_TABLE_COLUMN_SKIP_OBJECTS_COUNT, rs.getString(12));
				info.put(JOB_TABLE_COLUMN_SKIP_OBJECTS_SIZE, rs.getString(13));
				info.put(JOB_TABLE_COLUMN_START, rs.getString(14));
				info.put(JOB_TABLE_COLUMN_END, rs.getString(15));
				info.put(JOB_TABLE_COLUMN_ERROR_DESC, rs.getString(16));

				list.add(info);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}

        close();
		
		return list;
	}
}
