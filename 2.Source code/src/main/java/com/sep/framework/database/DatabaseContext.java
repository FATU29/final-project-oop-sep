package com.sep.framework.database;

import java.sql.Connection;

/**
 * Context class cho Strategy Pattern
 * Cho phép thay đổi database strategy tại runtime
 */
public class DatabaseContext {
    private DatabaseStrategy strategy;
    private Connection connection;
    private String connectionString;
    private String username;
    private String password;
    
    public DatabaseContext(DatabaseStrategy strategy) {
        this.strategy = strategy;
    }
    
    /**
     * Thay đổi strategy tại runtime
     */
    public void setStrategy(DatabaseStrategy strategy) {
        this.strategy = strategy;
        // Đóng connection cũ nếu có
        if (this.connection != null) {
            try {
                this.strategy.disconnect(this.connection);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }
    
    public void setCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    public Connection getConnection() throws Exception {
        if (connection == null || connection.isClosed()) {
            if (username != null && password != null) {
                connection = strategy.connect(connectionString, username, password);
            } else {
                connection = strategy.connect(connectionString);
            }
        }
        return connection;
    }
    
    public void close() throws Exception {
        if (connection != null) {
            strategy.disconnect(connection);
            connection = null;
        }
    }
    
    // Delegate methods
    public java.util.List<String> getTables() throws Exception {
        return strategy.getTables(getConnection());
    }
    
    public java.util.List<ColumnInfo> getColumns(String tableName) throws Exception {
        return strategy.getColumns(getConnection(), tableName);
    }
    
    public java.util.List<java.util.Map<String, Object>> getAll(String tableName) throws Exception {
        return strategy.getAll(getConnection(), tableName);
    }
    
    public int insert(String tableName, java.util.Map<String, Object> data) throws Exception {
        return strategy.insert(getConnection(), tableName, data);
    }
    
    public int update(String tableName, java.util.Map<String, Object> data, String whereClause) throws Exception {
        return strategy.update(getConnection(), tableName, data, whereClause);
    }
    
    public int delete(String tableName, String whereClause) throws Exception {
        return strategy.delete(getConnection(), tableName, whereClause);
    }
    
    public String getPrimaryKey(String tableName) throws Exception {
        return strategy.getPrimaryKey(getConnection(), tableName);
    }
}

