package com.sep.framework.ioc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Singleton Pattern: IoC Container
 * Custom Inversion of Control container để quản lý dependencies
 */
public class ServiceContainer {
    private static ServiceContainer instance;
    private Map<Class<?>, Object> singletons = new ConcurrentHashMap<>();
    private Map<Class<?>, Supplier<?>> factories = new ConcurrentHashMap<>();
    private Map<Class<?>, Class<?>> implementations = new ConcurrentHashMap<>();
    
    private ServiceContainer() {
        // Private constructor để đảm bảo Singleton
    }
    
    /**
     * Singleton Pattern: Lấy instance duy nhất
     */
    public static synchronized ServiceContainer getInstance() {
        if (instance == null) {
            instance = new ServiceContainer();
        }
        return instance;
    }
    
    /**
     * Đăng ký singleton instance
     */
    public <T> void registerSingleton(Class<T> serviceClass, T instance) {
        singletons.put(serviceClass, instance);
    }
    
    /**
     * Đăng ký factory method để tạo instance
     */
    public <T> void registerFactory(Class<T> serviceClass, Supplier<T> factory) {
        factories.put(serviceClass, factory);
    }
    
    /**
     * Đăng ký implementation cho interface
     */
    public <T> void register(Class<T> serviceClass, Class<? extends T> implementationClass) {
        implementations.put(serviceClass, implementationClass);
    }
    
    /**
     * Resolve service từ container
     */
    @SuppressWarnings("unchecked")
    public <T> T resolve(Class<T> serviceClass) {
        // Kiểm tra singleton
        if (singletons.containsKey(serviceClass)) {
            return (T) singletons.get(serviceClass);
        }
        
        // Kiểm tra factory
        if (factories.containsKey(serviceClass)) {
            Supplier<?> factory = factories.get(serviceClass);
            T instance = (T) factory.get();
            // Cache như singleton nếu cần
            return instance;
        }
        
        // Kiểm tra implementation mapping
        if (implementations.containsKey(serviceClass)) {
            Class<?> implClass = implementations.get(serviceClass);
            try {
                T instance = (T) implClass.getDeclaredConstructor().newInstance();
                // Cache như singleton
                singletons.put(serviceClass, instance);
                return instance;
            } catch (Exception e) {
                throw new RuntimeException("Failed to create instance of " + implClass.getName(), e);
            }
        }
        
        // Thử tạo trực tiếp nếu là concrete class
        try {
            T instance = serviceClass.getDeclaredConstructor().newInstance();
            singletons.put(serviceClass, instance);
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Cannot resolve service: " + serviceClass.getName(), e);
        }
    }
    
    /**
     * Kiểm tra service đã được đăng ký chưa
     */
    public boolean isRegistered(Class<?> serviceClass) {
        return singletons.containsKey(serviceClass) || 
               factories.containsKey(serviceClass) || 
               implementations.containsKey(serviceClass);
    }
    
    /**
     * Xóa tất cả registrations
     */
    public void clear() {
        singletons.clear();
        factories.clear();
        implementations.clear();
    }
}

