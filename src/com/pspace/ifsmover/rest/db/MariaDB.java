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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class MariaDB implements DBManager {
    protected Logger logger;
    private static HikariConfig config = new HikariConfig();
	private static HikariDataSource ds;
    
    private static final String CREATE_DATABASE = "CREATE DATABASE IF NOT EXISTS ";
    private static final String CREATE_USER_MATCH_JOB_TABLE =
			"CREATE TABLE IF NOT EXISTS `UserMatchJob` (\n"
            + "`match_id` INT NOT NULL AUTO_INCREMENT,\n"
            + "`user_id` TEXT,\n"
			+ "`job_id` INT NOT NULL,\n"
			+ "PRIMARY KEY(`match_id`))ENGINE=InnoDB DEFAULT CHARSET=utf8;";

    private MariaDB() {
        logger = LoggerFactory.getLogger(MariaDB.class);
	}

    public static MariaDB getInstance() {
        return LazyHolder.INSTANCE;
    }

    private static class LazyHolder {
        private static final MariaDB INSTANCE = new MariaDB();
    }

    public List<HashMap<String, Object>> select(String query, List<Object> params) throws Exception {
        List<HashMap<String, Object>> rmap = null; 
        
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rset = null;
        try {
			conn = ds.getConnection();
			pstmt = conn.prepareStatement(query);

            int index = 1;
			if(params != null) {
            	for(Object p : params) {
                	pstmt.setObject(index, p);
                	index++;
            	}
			}

            // logger.debug(pstmt.toString());
			rset = pstmt.executeQuery();

            ResultSetMetaData md = rset.getMetaData();
            int columns = md.getColumnCount();
            int init = 0;
            while (rset.next()) {
                if(init == 0) {
                    rmap = new ArrayList<HashMap<String, Object>>();
                    init++;
                }

                HashMap<String, Object> map = null; 
                map = new HashMap<String, Object>(columns);
                for(int i=1; i<=columns; ++i) {
					map.put(md.getColumnName(i), rset.getObject(i));
				}
                rmap.add(map);
            }
		} catch (SQLException e) {
			logger.error(e.getMessage());
			throw new Exception("Error");
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new Exception("Error");
		} finally {
			if ( rset != null ) try { rset.close(); } catch (Exception e) {logger.error(e.getMessage()); throw new Exception("Error");}
			if ( pstmt != null ) try { pstmt.close(); } catch (Exception e) {logger.error(e.getMessage()); throw new Exception("Error");}
			if ( conn != null ) try { conn.close(); } catch (Exception e) {logger.error(e.getMessage()); throw new Exception("Error");}
		}

        return rmap;
    }

	private void execute(String query, List<Object> params) throws Exception {
        try (Connection conn = ds.getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(query);
			) {

            int index = 1;
			if(params != null) {
            	for(Object p : params) {
                	pstmt.setObject(index, p);
                	index++;
            	}
			}

			// logger.debug(pstmt.toString());
			pstmt.execute();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			throw new Exception("Error");
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new Exception("Error");
		}
    }
    
    @Override
    public void init(String dbUrl, String dbPort, String dbName, String userName, String passwd, int poolSize)
            throws Exception {
        // TODO Auto-generated method stub
        String jdbcUrl = "jdbc:mariadb://" + dbUrl + ":" + dbPort + "/" + dbName + "?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8";

		config.setJdbcUrl(jdbcUrl);
		config.setUsername(userName);
		config.setPassword(passwd);
		config.setDriverClassName("org.mariadb.jdbc.Driver");
		config.setConnectionTestQuery("select 1");
		config.addDataSourceProperty("maxPoolSize" , poolSize );
		config.addDataSourceProperty("minPoolSize" , poolSize );
		config.setPoolName("ifsmover");
		config.setMaximumPoolSize(poolSize);
		config.setMinimumIdle(poolSize);

		ds = new HikariDataSource(config);
		
		createDB(dbName, userName, passwd);

        createTable();
    }

    private void createDB(String dbname, String userName, String userPasswd) throws Exception {
		String query = CREATE_DATABASE + dbname + ";";
		execute(query, null);
    }

    private void createTable() throws Exception {
		String query = CREATE_USER_MATCH_JOB_TABLE;
		execute(query, null);
	}

    @Override
    public Map<String, Object> getJobInfo(String jobId) {
        // TODO Auto-generated method stub
        Map<String, Object> info = new HashMap<String, Object>();	

        String sql = SQL_GET_JOB_INFO + jobId;

		List<HashMap<String, Object>> resultList = null;
        try {
			resultList = select(sql, null);
			if (resultList != null) {
				info.put(JOB_TABLE_COLUMN_JOB_STATE, (int)resultList.get(0).get(JOB_TABLE_COLUMN_JOB_STATE));
				info.put(JOB_TABLE_COLUMN_JOB_TYPE, (String)resultList.get(0).get(JOB_TABLE_COLUMN_JOB_TYPE));
				info.put(JOB_TABLE_COLUMN_ERROR_DESC, (String) resultList.get(0).get(JOB_TABLE_COLUMN_ERROR_DESC));
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

        return info;
    }

    @Override
    public void insertUserMatchJob(String userId, String jobId) {
        // TODO Auto-generated method stub
        String sql = SQL_INSERT_USERMATCHJOB;
		List<Object> params = new ArrayList<Object>();
		params.add(userId);
		params.add(jobId);

        try {
			execute(sql, params);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
    }

    @Override
    public int getUserMatchJob(String userId, String jobId) {
        // TODO Auto-generated method stub
        int matchId = 0;
        String sql = SQL_GET_USERMATCHJOB + userId + SQL_GET_USERMATCHJOB_JOBID + jobId + "'";
        List<HashMap<String, Object>> resultList = null;
        try {
			resultList = select(sql, null);
            if (resultList != null) {
                if (resultList.get(0).get(MATCH_TABLE_COLUMN_MATCH_ID) != null) {
                    matchId = (int) resultList.get(0).get(MATCH_TABLE_COLUMN_MATCH_ID);
                }
            }
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

        return matchId;
    }

    @Override
    public void deleteUserMatchJob(long matchId) {
        // TODO Auto-generated method stub
        String sql = SQL_DELETE_USERMATCHJOB + matchId;
        try {
			execute(sql, null);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
    }

    @Override
    public List<HashMap<String, Object>> status(String userId) {
        // TODO Auto-generated method stub
        try {
			return select(SQL_SELECT_JOB_STATUS + userId + "' ORDER BY A.job_id", null);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		return null;
    }
    
}
