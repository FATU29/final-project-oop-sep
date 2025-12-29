package com.sep.framework.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

/**
 * Strategy Pattern: Interface cho các database providers khác nhau
 * Cho phép framework hỗ trợ nhiều loại database (MySQL, PostgreSQL, SQLite, ...)
 */
public interface DatabaseStrategy {
    
    /**
     * Kết nối đến database
     */
    Connection connect(String connectionString) throws Exception;
    
    /**
     * Kết nối đến database với username và password
     */
    default Connection connect(String connectionString, String username, String password) throws Exception {
        return connect(connectionString);
    }
    
    /**
     * Đóng kết nối
     */
    void disconnect(Connection connection) throws Exception;
    
    /**
     * Lấy danh sách tất cả các bảng trong database
     */
    List<String> getTables(Connection connection) throws Exception;
    
    /**
     * Lấy thông tin các cột của một bảng
     */
    List<ColumnInfo> getColumns(Connection connection, String tableName) throws Exception;
    
    /**
     * Thực thi query và trả về ResultSet
     */
    ResultSet executeQuery(Connection connection, String query) throws Exception;
    
    /**
     * Thực thi update/insert/delete
     */
    int executeUpdate(Connection connection, String query) throws Exception;
    
    /**
     * Lấy tất cả dữ liệu từ bảng
     */
    List<Map<String, Object>> getAll(Connection connection, String tableName) throws Exception;
    
    /**
     * Thêm mới một record
     */
    int insert(Connection connection, String tableName, Map<String, Object> data) throws Exception;
    
    /**
     * Cập nhật một record
     */
    int update(Connection connection, String tableName, Map<String, Object> data, String whereClause) throws Exception;
    
    /**
     * Xóa một record
     */
    int delete(Connection connection, String tableName, String whereClause) throws Exception;
    
    /**
     * Lấy primary key của bảng
     */
    String getPrimaryKey(Connection connection, String tableName) throws Exception;
}

