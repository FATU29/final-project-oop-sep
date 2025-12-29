package com.sep.framework.membership;

import java.util.List;

/**
 * Membership Provider Interface
 * Tương tự ASP.NET Membership Provider
 */
public interface MembershipProvider {
    
    /**
     * Tạo user mới
     */
    boolean createUser(String username, String password, String email) throws Exception;
    
    /**
     * Xác thực user
     */
    boolean validateUser(String username, String password) throws Exception;
    
    /**
     * Lấy thông tin user
     */
    User getUser(String username) throws Exception;
    
    /**
     * Lấy user theo ID
     */
    User getUserById(int userId) throws Exception;
    
    /**
     * Cập nhật thông tin user
     */
    boolean updateUser(User user) throws Exception;
    
    /**
     * Xóa user
     */
    boolean deleteUser(String username) throws Exception;
    
    /**
     * Thay đổi password
     */
    boolean changePassword(String username, String oldPassword, String newPassword) throws Exception;
    
    /**
     * Reset password
     */
    String resetPassword(String username) throws Exception;
    
    /**
     * Kiểm tra user có tồn tại không
     */
    boolean userExists(String username) throws Exception;
    
    /**
     * Lấy danh sách tất cả users
     */
    List<User> getAllUsers() throws Exception;
    
    /**
     * Lấy số lượng users
     */
    int getUserCount() throws Exception;
    
    /**
     * Thêm user vào role
     */
    boolean addUserToRole(String username, String roleName) throws Exception;
    
    /**
     * Xóa user khỏi role
     */
    boolean removeUserFromRole(String username, String roleName) throws Exception;
    
    /**
     * Lấy danh sách roles của user
     */
    List<String> getRolesForUser(String username) throws Exception;
    
    /**
     * Kiểm tra user có trong role không
     */
    boolean isUserInRole(String username, String roleName) throws Exception;
}

