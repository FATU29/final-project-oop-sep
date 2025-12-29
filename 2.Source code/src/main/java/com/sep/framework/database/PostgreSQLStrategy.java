package com.sep.framework.database;

import java.sql.*;
import java.util.*;

/**
 * Strategy Pattern: Implementation cho PostgreSQL database
 */
public class PostgreSQLStrategy implements DatabaseStrategy {
    
    @Override
    public Connection connect(String connectionString) throws Exception {
        return DriverManager.getConnection(connectionString);
    }
    
    @Override
    public Connection connect(String connectionString, String username, String password) throws Exception {
        return DriverManager.getConnection(connectionString, username, password);
    }
    
    @Override
    public void disconnect(Connection connection) throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
    
    @Override
    public List<String> getTables(Connection connection) throws Exception {
        List<String> tables = new ArrayList<>();
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet rs = metaData.getTables(null, "public", null, new String[]{"TABLE"});
        
        while (rs.next()) {
            tables.add(rs.getString("TABLE_NAME"));
        }
        rs.close();
        return tables;
    }
    
    @Override
    public List<ColumnInfo> getColumns(Connection connection, String tableName) throws Exception {
        List<ColumnInfo> columns = new ArrayList<>();
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet rs = metaData.getColumns(null, "public", tableName, null);
        
        // Lấy primary keys
        Set<String> primaryKeys = new HashSet<>();
        ResultSet pkRs = metaData.getPrimaryKeys(null, "public", tableName);
        while (pkRs.next()) {
            primaryKeys.add(pkRs.getString("COLUMN_NAME"));
        }
        pkRs.close();
        
        while (rs.next()) {
            String columnName = rs.getString("COLUMN_NAME");
            String typeName = rs.getString("TYPE_NAME");
            int columnSize = rs.getInt("COLUMN_SIZE");
            int nullable = rs.getInt("NULLABLE");
            String defaultValue = rs.getString("COLUMN_DEF");
            
            columns.add(new ColumnInfo(
                columnName,
                typeName,
                columnSize,
                nullable == DatabaseMetaData.columnNullable,
                primaryKeys.contains(columnName),
                defaultValue
            ));
        }
        rs.close();
        return columns;
    }
    
    @Override
    public ResultSet executeQuery(Connection connection, String query) throws Exception {
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(query);
    }
    
    @Override
    public int executeUpdate(Connection connection, String query) throws Exception {
        try (Statement stmt = connection.createStatement()) {
            return stmt.executeUpdate(query);
        }
    }
    
    @Override
    public List<Map<String, Object>> getAll(Connection connection, String tableName) throws Exception {
        List<Map<String, Object>> results = new ArrayList<>();
        String query = "SELECT * FROM \"" + tableName + "\"";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = rs.getObject(i);
                    row.put(columnName, value);
                }
                results.add(row);
            }
        }
        return results;
    }
    
    @Override
    public int insert(Connection connection, String tableName, Map<String, Object> data) throws Exception {
        if (data.isEmpty()) {
            return 0;
        }
        
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        List<Object> params = new ArrayList<>();
        
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (columns.length() > 0) {
                columns.append(", ");
                values.append(", ");
            }
            columns.append("\"").append(entry.getKey()).append("\"");
            values.append("?");
            params.add(entry.getValue());
        }
        
        String query = String.format("INSERT INTO \"%s\" (%s) VALUES (%s)", 
            tableName, columns.toString(), values.toString());
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            return pstmt.executeUpdate();
        }
    }
    
    @Override
    public int update(Connection connection, String tableName, Map<String, Object> data, String whereClause) throws Exception {
        if (data.isEmpty()) {
            return 0;
        }
        
        StringBuilder setClause = new StringBuilder();
        List<Object> params = new ArrayList<>();
        
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (setClause.length() > 0) {
                setClause.append(", ");
            }
            setClause.append("\"").append(entry.getKey()).append("\" = ?");
            params.add(entry.getValue());
        }
        
        String query = String.format("UPDATE \"%s\" SET %s WHERE %s", 
            tableName, setClause.toString(), whereClause);
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            return pstmt.executeUpdate();
        }
    }
    
    @Override
    public int delete(Connection connection, String tableName, String whereClause) throws Exception {
        String query = String.format("DELETE FROM \"%s\" WHERE %s", tableName, whereClause);
        return executeUpdate(connection, query);
    }
    
    @Override
    public String getPrimaryKey(Connection connection, String tableName) throws Exception {
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet rs = metaData.getPrimaryKeys(null, "public", tableName);
        
        if (rs.next()) {
            String pk = rs.getString("COLUMN_NAME");
            rs.close();
            return pk;
        }
        rs.close();
        return null;
    }
}

