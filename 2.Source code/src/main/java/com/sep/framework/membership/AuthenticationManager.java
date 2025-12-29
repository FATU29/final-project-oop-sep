package com.sep.framework.membership;

/**
 * Singleton Pattern: Authentication Manager
 * Quản lý trạng thái đăng nhập của user hiện tại
 */
public class AuthenticationManager {
    
    private static AuthenticationManager instance;
    private User currentUser;
    private MembershipProvider membershipProvider;
    
    private AuthenticationManager() {
        // Private constructor for Singleton
    }
    
    /**
     * Lấy instance duy nhất (Singleton)
     */
    public static synchronized AuthenticationManager getInstance() {
        if (instance == null) {
            instance = new AuthenticationManager();
        }
        return instance;
    }
    
    /**
     * Set membership provider
     */
    public void setMembershipProvider(MembershipProvider provider) {
        this.membershipProvider = provider;
    }
    
    /**
     * Đăng nhập user
     */
    public boolean login(String username, String password) throws Exception {
        if (membershipProvider == null) {
            throw new IllegalStateException("MembershipProvider chưa được khởi tạo");
        }
        
        if (membershipProvider.validateUser(username, password)) {
            currentUser = membershipProvider.getUser(username);
            return true;
        }
        return false;
    }
    
    /**
     * Đăng xuất
     */
    public void logout() {
        currentUser = null;
    }
    
    /**
     * Kiểm tra user đã đăng nhập chưa
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Lấy user hiện tại
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Lấy username của user hiện tại
     */
    public String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : null;
    }
}

