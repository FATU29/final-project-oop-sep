package com.sep.framework.patterns;

import java.util.ArrayList;
import java.util.List;

/**
 * Observable class cho data binding
 * Observer Pattern implementation
 */
public class ObservableData {
    private List<DataBindingObserver> observers = new ArrayList<>();
    private Object data;
    
    public ObservableData(Object data) {
        this.data = data;
    }
    
    /**
     * Đăng ký observer
     */
    public void addObserver(DataBindingObserver observer) {
        observers.add(observer);
    }
    
    /**
     * Hủy đăng ký observer
     */
    public void removeObserver(DataBindingObserver observer) {
        observers.remove(observer);
    }
    
    /**
     * Thông báo cho tất cả observers
     */
    protected void notifyObservers(String property, Object oldValue, Object newValue) {
        for (DataBindingObserver observer : observers) {
            observer.onDataChanged(this, property, oldValue, newValue);
        }
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        Object oldValue = this.data;
        this.data = data;
        notifyObservers("data", oldValue, data);
    }
}

