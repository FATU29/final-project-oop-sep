package com.sep.framework.codegen;

import com.sep.framework.database.DatabaseContext;

/**
 * Builder Pattern: Builder cho CodeGenerator
 * Cho phép tạo CodeGenerator với cấu hình linh hoạt
 */
public class CodeGeneratorBuilder {
    
    private DatabaseContext dbContext;
    private String outputPath;
    private String packageName = "com.sep.generated";
    private boolean usePackage = true; // Default: use package
    
    public CodeGeneratorBuilder(DatabaseContext dbContext) {
        this.dbContext = dbContext;
    }
    
    /**
     * Set output path (hỗ trợ Windows, Linux, macOS)
     */
    public CodeGeneratorBuilder outputPath(String path) {
        this.outputPath = path;
        return this;
    }
    
    /**
     * Set package name
     */
    public CodeGeneratorBuilder packageName(String packageName) {
        this.packageName = packageName;
        return this;
    }
    
    /**
     * Set whether to use package structure
     * If false, code will be generated without package (flat structure)
     */
    public CodeGeneratorBuilder usePackage(boolean usePackage) {
        this.usePackage = usePackage;
        return this;
    }
    
    /**
     * Build CodeGenerator
     */
    public CodeGenerator build() {
        if (outputPath == null) {
            // Default: tạo trong thư mục generated của project
            outputPath = System.getProperty("user.dir") + 
                        System.getProperty("file.separator") + 
                        "generated";
        }
        
        CodeGenerator generator = new CodeGenerator(dbContext);
        generator.setOutputPath(outputPath);
        generator.setPackageName(packageName);
        generator.setUsePackage(usePackage);
        
        return generator;
    }
}

