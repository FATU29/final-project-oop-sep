package com.sep.framework.patterns;

import com.sep.framework.crud.BaseCrudForm;
import com.sep.framework.database.DatabaseContext;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory Pattern: Tạo các form CRUD
 * Cho phép tạo form một cách linh hoạt dựa trên table name
 */
public class FormFactory {
    
    private static FormFactory instance;
    private Map<String, Class<? extends BaseCrudForm>> formRegistry;
    private DatabaseContext dbContext;
    
    private FormFactory(DatabaseContext dbContext) {
        this.dbContext = dbContext;
        this.formRegistry = new HashMap<>();
    }
    
    /**
     * Singleton Pattern: Lấy instance
     */
    public static synchronized FormFactory getInstance(DatabaseContext dbContext) {
        if (instance == null) {
            instance = new FormFactory(dbContext);
        }
        return instance;
    }
    
    /**
     * Đăng ký form class cho một table
     */
    public void registerForm(String tableName, Class<? extends BaseCrudForm> formClass) {
        formRegistry.put(tableName.toLowerCase(), formClass);
    }
    
    /**
     * Factory Method: Tạo form cho table
     */
    public BaseCrudForm createForm(String tableName) {
        String key = tableName.toLowerCase();
        
        // Kiểm tra xem có form đã đăng ký không
        if (formRegistry.containsKey(key)) {
            try {
                Class<? extends BaseCrudForm> formClass = formRegistry.get(key);
                return formClass.getDeclaredConstructor(DatabaseContext.class, String.class)
                    .newInstance(dbContext, tableName);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create form for table: " + tableName, e);
            }
        }
        
        // Tạo generic form nếu chưa có form đặc biệt
        return new GenericCrudForm(dbContext, tableName);
    }
    
    /**
     * Generic CRUD Form implementation
     */
    private static class GenericCrudForm extends BaseCrudForm {
        public GenericCrudForm(DatabaseContext dbContext, String tableName) {
            super(dbContext, tableName);
        }
    }
}

