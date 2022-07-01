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

import com.pspace.ifsmover.rest.RestConfig;

public class Sqlite implements DBManager {
    private static final Logger logger = LoggerFactory.getLogger(Sqlite.class);
    
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
    
	private Sqlite() {
	}

    public static Sqlite getInstance() {
        return LazyHolder.INSTANCE;
    }

    private static class LazyHolder {
        private static final Sqlite INSTANCE = new Sqlite();
    }

	@Override
	public void init(String dbUrl, String dbPort, String dbName, String userName, String passwd, int poolSize)
			throws Exception {
		// TODO Auto-generated method stub
		init();
		createUserMatchJobTable();
	}

    private static void init() {
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
			Sqlite.con = DriverManager.getConnection(getDBURL(), config.toProperties());
		} catch (SQLException | ClassNotFoundException e) {
			logger.error(e.getMessage());
		}
	}

    private static String getDBURL() {
        return DB_FILE_URL + RestConfig.getInstance().getPath() + DB_FILE_NAME;
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
		if (Sqlite.con == null) {
			try {
				Class.forName(JDBC);
				Sqlite.con = DriverManager.getConnection(getDBURL());
                init();
			} catch (SQLException | ClassNotFoundException e) {
				logger.error(e.getMessage());
			}
		}
	}

	public static void close() {
		if (Sqlite.con != null) {
			try {
				Sqlite.con.close();
			} catch (SQLException e) {
				logger.error(e.getMessage());
			}
			Sqlite.con = null;
		}
	}
	
	public static Connection getConnection() {
		open();
		return Sqlite.con;
	}

    public static void createUserMatchJobTable() {
		open();

		try (Statement stmt = con.createStatement()) {
			stmt.execute(CREATE_USER_MATCH_JOB_TABLE);
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}
	}

	@Override
    public Map<String, Object> getJobInfo(String jobId) {
        Connection con = null;
		con = getReadConnection();

        String sql = SQL_GET_JOB_INFO + jobId;
        Map<String, Object> info = null;
        try (Statement stmt = con.createStatement();
			 ResultSet rs = stmt.executeQuery(sql);) {
			if (rs.next()) {
                info = new HashMap<String, Object>();
                info.put(JOB_TABLE_COLUMN_JOB_STATE, rs.getInt(1));
				info.put(JOB_TABLE_COLUMN_JOB_TYPE, rs.getString(2));
                info.put(JOB_TABLE_COLUMN_ERROR_DESC, rs.getString(3));
			}
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}

        close();

		return info;
    }

	@Override
    public void insertUserMatchJob(String userId, String jobId) {
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

	@Override
    public int getUserMatchJob(String userId, String jobId) {
		Connection con = null;
		con = getReadConnection();

        int matchId = 0;
		final String sql = SQL_GET_USERMATCHJOB + userId + SQL_GET_USERMATCHJOB_JOBID + jobId + "'";
		try (Statement stmt = con.createStatement();
			 ResultSet rs = stmt.executeQuery(sql);) {
			if (rs.next()) {
				matchId = rs.getInt(1);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}

        close();

		return matchId;
    }

	@Override
    public void deleteUserMatchJob(long matchId) {
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

	@Override
    public List<HashMap<String, Object>> status(String userId) {
		Connection con = null;
		con = getReadConnection();

		List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> info = null;

		if (con == null) {
			return list;
		}

		try (Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(SQL_SELECT_JOB_STATUS + userId + "' ORDER BY A.job_id");) {
			while (rs.next()) {
				info = new HashMap<String, Object>();
				
				info.put(JOB_TABLE_COLUMN_JOB_ID, rs.getInt(1));
				info.put(JOB_TABLE_COLUMN_JOB_STATE, rs.getInt(2));
				info.put(JOB_TABLE_COLUMN_JOB_TYPE, rs.getString(3));
				info.put(JOB_TABLE_COLUMN_SOURCE_POINT, rs.getString(4));
				info.put(JOB_TABLE_COLUMN_TARGET_POINT, rs.getString(5));
				info.put(JOB_TABLE_COLUMN_OBJECTS_COUNT, rs.getLong(6));
				info.put(JOB_TABLE_COLUMN_OBJECTS_SIZE, rs.getLong(7));
				info.put(JOB_TABLE_COLUMN_MOVED_OBJECTS_COUNT, rs.getLong(8));
				info.put(JOB_TABLE_COLUMN_MOVED_OBJECTS_SIZE, rs.getLong(9));
				info.put(JOB_TABLE_COLUMN_FAILED_COUNT, rs.getLong(10));
				info.put(JOB_TABLE_COLUMN_FAILED_SIZE, rs.getLong(11));
				info.put(JOB_TABLE_COLUMN_SKIP_OBJECTS_COUNT, rs.getLong(12));
				info.put(JOB_TABLE_COLUMN_SKIP_OBJECTS_SIZE, rs.getLong(13));
				info.put(JOB_TABLE_COLUMN_DELETE_OBJECT_COUNT, rs.getLong(14));
				info.put(JOB_TABLE_COLUMN_DELETE_OBJECT_SIZE, rs.getLong(15));
				info.put(JOB_TABLE_COLUMN_START, rs.getString(16));
				info.put(JOB_TABLE_COLUMN_END, rs.getString(17));
				info.put(JOB_TABLE_COLUMN_ERROR_DESC, rs.getString(18));

				list.add(info);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}

        close();
		
		return list;
	}

	
}
