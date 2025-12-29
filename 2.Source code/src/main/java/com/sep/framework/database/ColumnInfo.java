package com.sep.framework.database;

/**
 * Thông tin về một cột trong database
 */
public class ColumnInfo {
    private String name;
    private String type;
    private int size;
    private boolean nullable;
    private boolean isPrimaryKey;
    private String defaultValue;
    
    public ColumnInfo(String name, String type, int size, boolean nullable, boolean isPrimaryKey, String defaultValue) {
        this.name = name;
        this.type = type;
        this.size = size;
        this.nullable = nullable;
        this.isPrimaryKey = isPrimaryKey;
        this.defaultValue = defaultValue;
    }
    
    public String getName() {
        return name;
    }
    
    public String getType() {
        return type;
    }
    
    public int getSize() {
        return size;
    }
    
    public boolean isNullable() {
        return nullable;
    }
    
    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }
    
    public String getDefaultValue() {
        return defaultValue;
    }
    
    /**
     * Chuyển đổi database type sang Java type
     */
    public Class<?> getJavaType() {
        String lowerType = type.toLowerCase();
        if (lowerType.contains("int")) {
            return Integer.class;
        } else if (lowerType.contains("bigint")) {
            return Long.class;
        } else if (lowerType.contains("decimal") || lowerType.contains("numeric") || lowerType.contains("float") || lowerType.contains("double")) {
            return Double.class;
        } else if (lowerType.contains("bool")) {
            return Boolean.class;
        } else if (lowerType.contains("date") || lowerType.contains("time")) {
            return java.util.Date.class;
        } else {
            return String.class;
        }
    }
}

