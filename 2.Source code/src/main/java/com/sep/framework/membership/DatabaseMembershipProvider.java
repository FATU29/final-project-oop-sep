package com.sep.framework.membership;

import com.sep.framework.database.DatabaseContext;
import com.sep.framework.database.DatabaseStrategy;

import java.security.MessageDigest;
import java.util.*;

/**
 * Database-backed Membership Provider
 * Implementation của MembershipProvider sử dụng database
 */
public class DatabaseMembershipProvider implements MembershipProvider {
    
    private DatabaseContext dbContext;
    private static final String USERS_TABLE = "sep_users";
    private static final String ROLES_TABLE = "sep_roles";
    private static final String USER_ROLES_TABLE = "sep_user_roles";
    
    public DatabaseMembershipProvider(DatabaseContext dbContext) {
        this.dbContext = dbContext;
        initializeTables();
    }
    
    /**
     * Khởi tạo các bảng cần thiết nếu chưa tồn tại
     */
    private void initializeTables() {
        try {
            // Tạo bảng users
            String createUsersTable = "CREATE TABLE IF NOT EXISTS " + USERS_TABLE + " (" +
                "user_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(100) UNIQUE NOT NULL, " +
                "password VARCHAR(255) NOT NULL, " +
                "email VARCHAR(255), " +
                "is_active BOOLEAN DEFAULT TRUE, " +
                "created_date DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "last_login_date DATETIME" +
                ")";
            
            dbContext.getConnection().createStatement().execute(createUsersTable);
            
            // Tạo bảng roles
            String createRolesTable = "CREATE TABLE IF NOT EXISTS " + ROLES_TABLE + " (" +
                "role_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "role_name VARCHAR(100) UNIQUE NOT NULL" +
                ")";
            
            dbContext.getConnection().createStatement().execute(createRolesTable);
            
            // Tạo bảng user_roles
            String createUserRolesTable = "CREATE TABLE IF NOT EXISTS " + USER_ROLES_TABLE + " (" +
                "user_id INT, " +
                "role_id INT, " +
                "PRIMARY KEY (user_id, role_id), " +
                "FOREIGN KEY (user_id) REFERENCES " + USERS_TABLE + "(user_id), " +
                "FOREIGN KEY (role_id) REFERENCES " + ROLES_TABLE + "(role_id)" +
                ")";
            
            dbContext.getConnection().createStatement().execute(createUserRolesTable);
            
            // Tạo default roles
            createDefaultRoles();
            
        } catch (Exception e) {
            System.err.println("Error initializing membership tables: " + e.getMessage());
        }
    }
    
    private void createDefaultRoles() throws Exception {
        String[] defaultRoles = {"Admin", "User", "Guest"};
        for (String roleName : defaultRoles) {
            try {
                Map<String, Object> role = new HashMap<>();
                role.put("role_name", roleName);
                dbContext.insert(ROLES_TABLE, role);
            } catch (Exception e) {
                // Role đã tồn tại, bỏ qua
            }
        }
    }
    
    /**
     * Hash password đơn giản (trong production nên dùng BCrypt hoặc Argon2)
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    @Override
    public boolean createUser(String username, String password, String email) throws Exception {
        if (userExists(username)) {
            return false;
        }
        
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("password", hashPassword(password));
        userData.put("email", email);
        userData.put("is_active", true);
        
        int result = dbContext.insert(USERS_TABLE, userData);
        return result > 0;
    }
    
    @Override
    public boolean validateUser(String username, String password) throws Exception {
        String query = "SELECT * FROM " + USERS_TABLE + " WHERE username = ? AND is_active = TRUE";
        try (java.sql.PreparedStatement pstmt = dbContext.getConnection().prepareStatement(query)) {
            pstmt.setString(1, username);
            java.sql.ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                String hashedPassword = hashPassword(password);
                
                if (storedPassword.equals(hashedPassword)) {
                    // Cập nhật last login
                    updateLastLogin(username);
                    return true;
                }
            }
            return false;
        }
    }
    
    private void updateLastLogin(String username) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("last_login_date", new Date());
        dbContext.update(USERS_TABLE, data, "username = '" + username + "'");
    }
    
    @Override
    public User getUser(String username) throws Exception {
        String query = "SELECT * FROM " + USERS_TABLE + " WHERE username = ?";
        try (java.sql.PreparedStatement pstmt = dbContext.getConnection().prepareStatement(query)) {
            pstmt.setString(1, username);
            java.sql.ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            return null;
        }
    }
    
    @Override
    public User getUserById(int userId) throws Exception {
        String query = "SELECT * FROM " + USERS_TABLE + " WHERE user_id = ?";
        try (java.sql.PreparedStatement pstmt = dbContext.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, userId);
            java.sql.ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            return null;
        }
    }
    
    private User mapResultSetToUser(java.sql.ResultSet rs) throws Exception {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        user.setActive(rs.getBoolean("is_active"));
        user.setCreatedDate(rs.getTimestamp("created_date"));
        user.setLastLoginDate(rs.getTimestamp("last_login_date"));
        user.setRoles(getRolesForUser(user.getUsername()));
        return user;
    }
    
    @Override
    public boolean updateUser(User user) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("username", user.getUsername());
        data.put("email", user.getEmail());
        data.put("is_active", user.isActive());
        
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            data.put("password", hashPassword(user.getPassword()));
        }
        
        int result = dbContext.update(USERS_TABLE, data, "user_id = " + user.getUserId());
        return result > 0;
    }
    
    @Override
    public boolean deleteUser(String username) throws Exception {
        // Xóa user roles trước
        User user = getUser(username);
        if (user != null) {
            String deleteUserRoles = "DELETE FROM " + USER_ROLES_TABLE + " WHERE user_id = " + user.getUserId();
            dbContext.getConnection().createStatement().execute(deleteUserRoles);
            
            // Xóa user
            int result = dbContext.delete(USERS_TABLE, "username = '" + username + "'");
            return result > 0;
        }
        return false;
    }
    
    @Override
    public boolean changePassword(String username, String oldPassword, String newPassword) throws Exception {
        if (!validateUser(username, oldPassword)) {
            return false;
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("password", hashPassword(newPassword));
        
        int result = dbContext.update(USERS_TABLE, data, "username = '" + username + "'");
        return result > 0;
    }
    
    @Override
    public String resetPassword(String username) throws Exception {
        String newPassword = UUID.randomUUID().toString().substring(0, 8);
        Map<String, Object> data = new HashMap<>();
        data.put("password", hashPassword(newPassword));
        
        dbContext.update(USERS_TABLE, data, "username = '" + username + "'");
        return newPassword;
    }
    
    @Override
    public boolean userExists(String username) throws Exception {
        String query = "SELECT COUNT(*) FROM " + USERS_TABLE + " WHERE username = ?";
        try (java.sql.PreparedStatement pstmt = dbContext.getConnection().prepareStatement(query)) {
            pstmt.setString(1, username);
            java.sql.ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        }
    }
    
    @Override
    public List<User> getAllUsers() throws Exception {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM " + USERS_TABLE;
        try (java.sql.Statement stmt = dbContext.getConnection().createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }
    
    @Override
    public int getUserCount() throws Exception {
        String query = "SELECT COUNT(*) FROM " + USERS_TABLE;
        try (java.sql.Statement stmt = dbContext.getConnection().createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }
    
    @Override
    public boolean addUserToRole(String username, String roleName) throws Exception {
        User user = getUser(username);
        if (user == null) {
            return false;
        }
        
        // Lấy role_id
        String getRoleQuery = "SELECT role_id FROM " + ROLES_TABLE + " WHERE role_name = ?";
        int roleId;
        try (java.sql.PreparedStatement pstmt = dbContext.getConnection().prepareStatement(getRoleQuery)) {
            pstmt.setString(1, roleName);
            java.sql.ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) {
                return false; // Role không tồn tại
            }
            roleId = rs.getInt("role_id");
        }
        
        // Thêm vào user_roles
        Map<String, Object> userRole = new HashMap<>();
        userRole.put("user_id", user.getUserId());
        userRole.put("role_id", roleId);
        
        try {
            dbContext.insert(USER_ROLES_TABLE, userRole);
            return true;
        } catch (Exception e) {
            // Đã tồn tại
            return false;
        }
    }
    
    @Override
    public boolean removeUserFromRole(String username, String roleName) throws Exception {
        User user = getUser(username);
        if (user == null) {
            return false;
        }
        
        String query = "DELETE ur FROM " + USER_ROLES_TABLE + " ur " +
                      "JOIN " + ROLES_TABLE + " r ON ur.role_id = r.role_id " +
                      "WHERE ur.user_id = ? AND r.role_name = ?";
        
        try (java.sql.PreparedStatement pstmt = dbContext.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, user.getUserId());
            pstmt.setString(2, roleName);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    @Override
    public List<String> getRolesForUser(String username) throws Exception {
        List<String> roles = new ArrayList<>();
        String query = "SELECT r.role_name FROM " + ROLES_TABLE + " r " +
                      "JOIN " + USER_ROLES_TABLE + " ur ON r.role_id = ur.role_id " +
                      "JOIN " + USERS_TABLE + " u ON ur.user_id = u.user_id " +
                      "WHERE u.username = ?";
        
        try (java.sql.PreparedStatement pstmt = dbContext.getConnection().prepareStatement(query)) {
            pstmt.setString(1, username);
            java.sql.ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                roles.add(rs.getString("role_name"));
            }
        }
        return roles;
    }
    
    @Override
    public boolean isUserInRole(String username, String roleName) throws Exception {
        List<String> roles = getRolesForUser(username);
        return roles.contains(roleName);
    }
}

