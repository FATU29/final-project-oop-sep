package com.sep.framework.test;

import com.sep.framework.database.DatabaseContext;
import com.sep.framework.database.MySQLStrategy;
import com.sep.framework.codegen.CodeGenerator;
import com.sep.framework.codegen.CodeGeneratorBuilder;

/**
 * Test Code Generator
 */
public class TestCodeGenerator {
    
    public static void main(String[] args) {
        try {
            System.out.println("=== Testing Code Generator ===\n");
            
            // Setup database
            DatabaseContext dbContext = new DatabaseContext(new MySQLStrategy());
            dbContext.setConnectionString(
                "jdbc:mysql://localhost:3306/sep_demo?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
            );
            dbContext.setCredentials("sep_user", "sep_password");
            
            // Test connection
            System.out.println("Testing database connection...");
            java.util.List<String> tables = dbContext.getTables();
            System.out.println("Found " + tables.size() + " tables: " + tables);
            
            // Generate code
            String outputPath = System.getProperty("user.dir") + "/generated_test";
            String packageName = "com.sep.generated";
            
            System.out.println("\nGenerating code to: " + outputPath);
            System.out.println("Package: " + packageName);
            
            CodeGenerator generator = new CodeGeneratorBuilder(dbContext)
                .outputPath(outputPath)
                .packageName(packageName)
                .build();
            
            // Generate for first table
            if (!tables.isEmpty()) {
                String tableName = tables.get(0);
                System.out.println("\nGenerating code for table: " + tableName);
                generator.generateForTable(tableName);
                System.out.println("\n=== Generation completed! ===");
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

